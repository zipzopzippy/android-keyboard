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
- Flatter emoji panel and search: no filled background behind the emoji grid, the Emoji Kitchen row
  or search fields, and "Clear recent emojis" moved from the panel header to Settings → Emoji &
  clipboard.
- Emoji search is a search icon button left of the "Emoji" title, and shows word suggestions while
  typing. The selected emoji category has a smaller highlight circle.
- Symbols page (`java/personal/assets/layouts/Special/symbols.yaml`): the row under the number row
  has 9 keys (`@ # $ % & - + ( )`, without `_`, which is still on long-press of `-`), so each symbol
  sits exactly over the letter that shows it as a hint. `_` takes the place of `℅` on the second
  symbols page (`symbols_shift.yaml`).
- **Reorganized settings.** The home screen lists one page per topic: Languages & models, Typing &
  autocorrect, Swipe & gestures, Layout & keys, Sound & vibration, Emoji & clipboard, Toolbar &
  actions, Voice input, Theme, Backup & restore, and Help & feedback. Settings moved to the page
  they belong on (for example, autocorrect and its backspace undo are now together, vibration and
  sound have their own page, spacebar and backspace gestures moved to the swipe page, and clipboard
  settings moved out of Toolbar & actions). Most titles and descriptions were rewritten in plainer
  English. "Check for updates manually" is hidden because this build has update checking off.
- Spelling underlines work again: `java/personal/res/xml/method.xml` drops
  `android:suppressesSpellChecker`, which FUTO sets because it has no spell checker of its own yet.
  That flag hides the system spell checker's underlines in every app while the keyboard is active.
- No long-press hints for the settings shortcut on the comma key or for `∅` on the `0` key
  (`java/src/org/futo/inputmethod/v2keyboard/LayoutEngine.kt`). Both are still in the long-press
  menus.
- A `personal` build flavor (`assemblePersonalRelease`): app ID `org.futo.inputmethod.latin.personal`,
  named "FUTO Keyboard Personal", with update checking off.
