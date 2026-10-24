# DexAuto (starter project)

A DeX-style desktop shell (taskbar + mirrored phone screen) meant to be
sideloaded into Android Auto via its "Unknown sources" developer setting —
the same unofficial mechanism used by community projects like AA Browser
and AA Anything.

## How to get a build without installing anything

1. Push this folder to a new GitHub repo (root of the repo = this folder).
2. GitHub Actions (`.github/workflows/build.yml`) builds automatically on
   every push to `main`.
3. Go to the repo's **Actions** tab → open the latest run → download the
   `dexauto-debug-apk` artifact → unzip it to get `app-debug.apk`.
4. Transfer that APK to your phone (Google Drive, email to yourself, etc.)
   and tap it to install (you'll need to allow your file manager/browser
   to install unknown apps — a normal Android permission prompt).

No Android Studio, no local Gradle, no PC required for the build itself.

## Enabling it inside Android Auto

1. On your phone: Settings → Apps → Android Auto → tap the "Version"
   row at the bottom 10 times to unlock Developer Settings.
2. Open the 3-dot menu in Android Auto → **Developer settings**.
3. Enable **Unknown sources**.
4. Plug into your car; DexAuto should appear in the AA app launcher.

## What actually works right now

- A dark, DeX-style shell (taskbar with a clock, "start/stop" control)
  renders inside the car screen.
- Tapping Start triggers Android's screen-capture consent dialog, then
  mirrors your phone's display into the shell via `MediaProjection`.

## What this does **not** do, and why

- **It is not real Samsung DeX.** DeX is a Samsung-proprietary display
  mode tied to HDMI/wireless-display output; there's no public API that
  lets a third-party app "turn on DeX" programmatically. This project
  reproduces the *look* (desktop + taskbar) using Android's standard
  `MediaProjection` screen-capture API, which works on any phone, not
  just Samsung ones.
- **The capture consent dialog cannot be skipped or pre-approved.** This
  is an Android OS security requirement, not a limitation of this code —
  every single app that mirrors your screen (including AA Mirror) hits
  the same dialog, every session.
- **The "Unknown sources" Android Auto toggle is an unofficial, unlisted
  mechanism.** It is not part of Google's public Android Auto API, isn't
  guaranteed to keep working across AA versions, and Google could change
  or remove it at any time without notice — exactly as happened to some
  older apps on Android 15 (several stopped showing up in the car list
  after that update).
- **Touch passthrough while mirroring is not wired up yet.** Right now
  this only *displays* the mirrored frame; routing touches on the car
  screen back to the phone's input system needs an Accessibility Service
  or `Instrumentation`-based injection, which is a separate, more
  involved piece — happy to add it next if you want actual control, not
  just viewing.

## Safety note

Mirroring/interacting with a full phone UI on a car screen while driving
is a real distraction risk — this is meant for use when parked, same as
AA Browser's own stated intent. Consider gating the Start button behind
a "parked" check if you plan to actually use this regularly.
