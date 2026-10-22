package dev.omalauncher.linux

import android.content.Context
import dev.omalauncher.upgrade.writeAtomically
import java.io.File

/**
 * Manjaro Sway's tweaks that still mean something when the launcher, not sway, is the
 * compositor (imported by scripts/import-manjaro-sway.py): its helper commands (mjr,
 * termpaste), its theme sources in /usr/share/manjaro-sway, its Wayland session
 * environment in /etc/profile.d, and its app configs from /etc/skel, copied into the
 * home once. Files the launcher manages (foot, btop, the theme) are left to it.
 */
object ManjaroTweaks {
    private const val ASSET_DIR = "manjaro-tweaks"
    private const val SKEL = "etc/skel/"

    /** Blocking. */
    fun install(
        context: Context,
        proot: Proot,
    ) {
        for (path in list(context, ASSET_DIR)) {
            val rel = path.removePrefix("$ASSET_DIR/")
            if (rel == "SOURCE") continue
            val bytes = context.assets.open(path).use { it.readBytes() }
            val executable = rel.startsWith("usr/local/bin/")
            writeAtomically(File(proot.rootfs, rel), bytes, executable)
            // The user's copy, as useradd would make it from /etc/skel; never over one they have.
            if (rel.startsWith(SKEL)) {
                val own = File(proot.home, rel.removePrefix(SKEL))
                if (!own.exists()) writeAtomically(own, bytes)
            }
        }
    }

    /** Every file under [dir] in the assets; a name without children is a file. */
    private fun list(
        context: Context,
        dir: String,
    ): List<String> {
        val children = context.assets.list(dir).orEmpty()
        if (children.isEmpty()) return listOf(dir)
        return children.sorted().flatMap { list(context, "$dir/$it") }
    }
}
