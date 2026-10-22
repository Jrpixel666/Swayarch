package dev.omalauncher.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import dev.omalauncher.upgrade.writeAtomically
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "manjaro-themes"

/** Where in the assets the themes are, as scripts/import-manjaro-sway.py writes them. */
private const val ASSET_DIR = "manjaro-themes"

/** Bumped when the imported themes or their rendering change, so existing installs get them again. */
private const val VERSION = "1"

/**
 * Manjaro Sway's themes (manjaro-sway-settings' /usr/share/sway/themes, imported by
 * scripts/import-manjaro-sway.py), installed next to Omarchy's in its themes directory so
 * every theme menu, the guest's configs and the live wallpaper treat them like any other.
 * Each one's wallpaper is Manjaro Sway's generated background (generate-bg.sh), drawn here
 * from its SVG: a portrait crop for the phone, and the original 16:9 for landscape.
 */
object ManjaroThemes {
    const val DEFAULT = "manjaro-sway"

    fun names(context: Context): List<String> = context.assets.list(ASSET_DIR).orEmpty().sorted()

    fun isManjaro(name: String) = name.startsWith("manjaro-")

    /** Copies any theme missing from [themesDir] (or from an older [VERSION]); quick when they're all there. Blocking. */
    fun install(
        context: Context,
        themesDir: File,
    ) {
        for (name in names(context)) {
            val dir = File(themesDir, name)
            val marker = File(dir, ".swayarch-version")
            if (runCatching { marker.readText().trim() }.getOrNull() == VERSION && File(dir, "colors.toml").isFile) continue
            runCatching { installOne(context, name, dir) }
                .onSuccess { marker.writeText(VERSION + "\n") }
                .onFailure { Log.e(TAG, "theme $name not installed", it) }
        }
    }

    private fun installOne(
        context: Context,
        name: String,
        dir: File,
    ) {
        val svg = context.assets.open("$ASSET_DIR/$name/wallpaper.svg").bufferedReader().use { it.readText() }
        val colors = context.assets.open("$ASSET_DIR/$name/colors.toml").use { it.readBytes() }
        val background = ThemeColors.parse(String(colors))["background"] ?: "#141a1b"
        val backgrounds = File(dir, "backgrounds")
        backgrounds.mkdirs()
        // The background first: a theme dir counts as installed once colors.toml is there.
        writeAtomically(File(backgrounds, "1-manjaro-sway.png"), render(svg, background, PORTRAIT, 1440, 2560))
        writeAtomically(File(backgrounds, "2-manjaro-sway-wide.png"), render(svg, background, WIDE, 2560, 1440))
        writeAtomically(File(dir, "colors.toml"), colors)
    }

    /** The SVG's 3840x2160 canvas: the crown and root sit right of center, around (2697, 1115). */
    private val WIDE = floatArrayOf(0f, 0f, 3840f, 2160f)

    /** A 9:16 window on it, centered on the logo; past the canvas' edges the theme's background fills in. */
    private val PORTRAIT = floatArrayOf(2697f - 810f, 1115f - 1440f, 1620f, 2880f)

    private fun render(
        svg: String,
        background: String,
        viewBox: FloatArray,
        width: Int,
        height: Int,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.parseColor(background))
        SVG.getFromString(svg).renderToCanvas(
            canvas,
            RenderOptions()
                .viewBox(viewBox[0], viewBox[1], viewBox[2], viewBox[3])
                .viewPort(0f, 0f, width.toFloat(), height.toFloat()),
        )
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        bitmap.recycle()
        return out.toByteArray()
    }
}
