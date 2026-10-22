# Swayarch

An Android home screen with the look of [Manjaro Sway](https://github.com/manjaro-sway/manjaro-sway),
and an optional Manjaro ARM system next to your phone apps. Swayarch runs Manjaro under proot,
without root, and is its Wayland compositor: every Linux window opens as its own Android task,
with its own card in Recents.

Swayarch is a fork of [Omalauncher](https://github.com/SimonSchubert/omalauncher) by Simon Schubert
(MIT), with its OWL widget language and [widget store](https://github.com/SimonSchubert/omarchy-widgets).

- **Manjaro Sway's themes**: its default (Matcha green on Base16 Seti) and all eleven themes from
  `manjaro-sway-settings` (`/usr/share/sway/themes`: Catppuccin ×4, Dracula, Matcha ×4, Nordic,
  Atari Classic), each with Manjaro Sway's generated crown-and-root wallpaper in its colors. They
  recolor the launcher and the Linux apps (foot, btop, …). Omarchy's themes stay available too.
- **Manjaro Sway's tweaks** in the guest: `mjr`, `termpaste`, its Wayland session environment
  (`/etc/profile.d/manjaro-sway.sh`), its theme sources in `/usr/share/manjaro-sway`, and its
  helix/nvim/swappy/calcurse configs.
- **Widgets in OWL**, a small declarative language you edit on the phone; see
  [app/src/main/assets/widgets/README.md](app/src/main/assets/widgets/README.md).
- **Desktop only, or desktop and Manjaro**: picked on first run, and asked again at every start
  until Manjaro is installed (turn that off in settings › linux, or on the start screen).
- **Manjaro ARM, optional**: the weekly generic aarch64 rootfs from
  [manjaro-arm/rootfs](https://github.com/manjaro-arm/rootfs), on the `arm-stable` branch, with
  pacman, the AUR, terminal widgets, a file manager and coding agents.
- **No firmware or kernel modules**: they're useless under proot, so the unpack skips them, pacman
  never unpacks them (`NoExtract = usr/lib/firmware/* usr/lib/modules/*`), and the packages that
  own them (kernels, `linux-firmware`) are removed at setup.

Needs an arm64 phone with Android 10 or newer.

## Getting the APK

Every push builds it in GitHub Actions (`.github/workflows/build.yml`): open the latest **build**
run under Actions and download the `swayarch-apk` artifact. `swayarch-0.11.0.apk` is the release
build (R8-shrunk, signed with the debug key); install it with `adb install` or from the phone.
Pushing a `v*` tag also publishes the APKs as a GitHub release.

The same workflow's `manjaro-smoke` job unpacks the Manjaro ARM rootfs on an arm64 runner and runs
the app's setup steps there in a chroot (mirrors, keyrings, kernel removal, the package list).

## Building locally

```sh
./scripts/fetch-proot.sh          # Termux's proot -> app/src/main/jniLibs (needs the NDK and bsdtar)
./gradlew :app:assembleRelease
# optional: skip the rootfs download on the device
adb push Manjaro-ARM-aarch64-latest.tar.gz /sdcard/Android/data/dev.swayarch/files/
```

Tests: `./gradlew :owl:test :app:testDebugUnitTest`.

To pick up a newer Manjaro Sway release, unpack it and re-run the importer, e.g. for
[202610180719](https://github.com/manjaro-sway/manjaro-sway/archive/refs/tags/202610180719.tar.gz):

```sh
curl -L https://github.com/manjaro-sway/manjaro-sway/archive/refs/tags/202610180719.tar.gz | tar xz
scripts/import-manjaro-sway.py manjaro-sway-202610180719
```

It writes `app/src/main/assets/themes/manjaro-*.toml`, `assets/manjaro-themes/` and
`assets/manjaro-tweaks/`; bump `VERSION` in `theme/ManjaroThemes.kt` so installs refresh them.

## Using it

The bar at the bottom of home opens apps and asks coding agents; the app drawer is
`drawer.owl` in the widgets folder. In Linux windows a tap is a left click, a long press a right
click, a drag scrolls. Hardware keyboards: Super+Enter (terminal), Super+Space (the bar),
Super+1..9 (home pages), Super+W (close a Linux window).

More: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) (the compositor, rendering, GPU acceleration).

## Layout

- `app/`: the Android app (Kotlin, Jetpack Compose, and the JNI bridge in `src/main/cpp`)
- `owl/`: the widget language and the `owl` command line, plain Kotlin
- `scripts/import-manjaro-sway.py`: Manjaro Sway themes and tweaks -> assets
- `scripts/fetch-proot.sh`: builds proot

## License

MIT, see [LICENSE](LICENSE), for Omalauncher's and Swayarch's own code. Manjaro Sway's themes,
wallpaper art and scripts in `app/src/main/assets/manjaro-*` come from manjaro-sway-settings
(GPL); see [THIRD_PARTY.md](THIRD_PARTY.md) for those and the other bundled parts.

Swayarch is an independent project, not affiliated with or endorsed by Manjaro, Manjaro Sway,
Omarchy, Arch Linux, or Omalauncher's author.
