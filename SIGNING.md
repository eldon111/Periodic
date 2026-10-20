# Release signing and updating CI APKs

CI builds a **release APK signed with one stable keystore**. Android only allows
installing over an existing app when the signer matches and `versionCode` is
higher, so testers can update without uninstalling.

## Create the keystore and GitHub secrets

```bash
keytool -genkeypair -v -keystore release.keystore -alias periodic \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 release.keystore   # macOS: base64 -i release.keystore
```

Add repository secrets (Settings > Secrets and variables > Actions):

- `ANDROID_KEYSTORE_BASE64` – output of the base64 command
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Never commit the keystore. Keep a backup: losing it means testers must uninstall once.

## versionCode

CI uses `GITHUB_RUN_NUMBER` (increases every run). Local builds default to `1`.

## Local release build

Export `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
`ANDROID_KEY_PASSWORD`, then run `./gradlew assembleRelease`. Missing variables
fail the build with a message listing them.

## Troubleshooting

- `INSTALL_FAILED_UPDATE_INCOMPATIBLE` / signature mismatch: the installed app was
  signed with a different key (e.g. an old debug build). Uninstall once, then
  future CI APKs update in place.
- `INSTALL_FAILED_VERSION_DOWNGRADE`: the installed `versionCode` is higher than the
  new APK's (e.g. a local build with a larger value). Uninstall or install a newer CI build.
