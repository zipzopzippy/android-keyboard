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

## Release

`.github/workflows/personal-build.yml` builds, signs and publishes a release on GitHub Actions on
every push to `personal`. Releases are tagged `personal-<FUTO version>-personal-<number>`, where the
number (001, 002, ...) is one above the highest numbered tag. To run it by hand:

```sh
gh workflow run personal-build.yml --ref personal
```

Optional inputs: `-f notes="..."` for the "New in this release" text and `-f base_version=0.1.31`
after moving to a newer FUTO version. It fails rather than publishes if the APK wouldn't update the
previous release (different signing certificate or lower `versionCode`), and if the commit is
already released. If the newest `personal-*` tag has no release with an APK, it skips that check
and warns.

The key is in the repository secrets `PERSONAL_KEYSTORE_BASE64`, `PERSONAL_KEYSTORE_PASSWORD`,
`PERSONAL_KEY_ALIAS` and `PERSONAL_KEY_PASSWORD`.
