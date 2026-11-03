# Modified version

This is a **modified version of FUTO Keyboard** for personal, non-commercial use. It is not made or
endorsed by FUTO. The original is at https://github.com/futo-org/android-keyboard and is licensed
under the [FUTO Source First License 1.1](LICENSE.md), which also covers this version.

## Changes from FUTO Keyboard

- **Emoji Kitchen** (`java/src/org/futo/inputmethod/latin/uix/actions/emoji/kitchen/`): tapping an
  emoji in the emoji panel shows a row of Emoji Kitchen stickers mixing it with other emojis.
  Tapping one sends it to the app as an image. The stickers are Google's images and are downloaded
  from `www.gstatic.com` when shown, so this version requests the `INTERNET` permission. It can be
  turned off in Settings → Typing → Emoji Kitchen. Which pairs exist comes from
  `java/assets/emoji_kitchen/index.txt`, built by `tools/emoji_kitchen/build_index.py`.
- **"Dynamic Dark (FlorisBoard style)" theme**, the default here: near-black Material You
  background, borderless keys, flat rounded space and enter keys, muted enter color and fainter
  key hints.
- **Seamless suggestion bar** setting (on by default, next to "Key borders"): the suggestion bar
  uses the keyboard's color and has no dividing line.
- Key borders are off by default.
- **Reorganized settings.** The home screen lists one page per topic: Languages & models, Typing &
  autocorrect, Swipe & gestures, Layout & keys, Sound & vibration, Emoji & clipboard, Toolbar &
  actions, Voice input, Theme, Backup & restore, and Help & feedback. Settings moved to the page
  they belong on (for example, autocorrect and its backspace undo are now together, vibration and
  sound have their own page, spacebar and backspace gestures moved to the swipe page, and clipboard
  settings moved out of Toolbar & actions). Most titles and descriptions were rewritten in plainer
  English. "Check for updates manually" is hidden because this build has update checking off.
- A `personal` build flavor (`assemblePersonalRelease`): app ID `org.futo.inputmethod.latin.personal`,
  named "FUTO Keyboard Personal", with update checking off.
