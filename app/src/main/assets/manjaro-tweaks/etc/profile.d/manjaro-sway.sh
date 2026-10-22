# Manjaro Sway's session environment (etc/skel/.profile), minus what only sway itself uses.
# Installed by Swayarch; edit ~/.config/profile.d/*.sh for your own.
export MOZ_ENABLE_WAYLAND=1
export MOZ_DBUS_REMOTE=1
export GTK_CSD=0
if [ -e /usr/lib/libgtk-nocsd.so ]; then
	export LD_PRELOAD="/usr/lib/libgtk-nocsd.so${LD_PRELOAD:+:$LD_PRELOAD}"
fi
export QT_QPA_PLATFORM="wayland"
export QT_WAYLAND_DISABLE_WINDOWDECORATION="1"
export _JAVA_AWT_WM_NONREPARENTING=1
export ELECTRON_OZONE_PLATFORM_HINT=wayland
export OPENCV_LOG_LEVEL=ERROR
alias ssh="TERM=xterm-256color ssh"
if [ -n "$(ls "$HOME"/.config/profile.d 2>/dev/null)" ]; then
	for f in "$HOME"/.config/profile.d/*; do . "$f"; done
fi
