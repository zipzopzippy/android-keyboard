# Personal build

See [MODIFICATIONS.md](MODIFICATIONS.md) for what differs from FUTO Keyboard.

## Install on GrapheneOS

1. Install `FUTO-Keyboard-personal.apk`. It installs next to the official FUTO Keyboard.
2. Allow **Network** for it if you want Emoji Kitchen stickers; everything else works offline.
3. Enable it in Settings → System → Keyboard → On-screen keyboard.

## Build

```sh
git submodule update --init --recursive
(cd java/assets/futo-swipe && git lfs pull)   # swipe typing models
./gradlew assemblePersonalRelease
# APK: build/outputs/apk/personal/release/
```

It is signed with the key in `keystore.properties` (git-ignored). Keep that key: updates only
install over the app when they are signed with the same key.
