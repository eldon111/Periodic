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

Encode it (the result is one long line):

- Linux: `base64 -w0 release.keystore > keystore.b64`
- macOS: `base64 -i release.keystore -o keystore.b64`
- Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Set-Content keystore.b64`

Add repository secrets (Settings > Secrets and variables > Actions > New repository secret):

- `ANDROID_KEYSTORE_BASE64` – contents of `keystore.b64`
- `ANDROID_KEYSTORE_PASSWORD` – keystore password
- `ANDROID_KEY_ALIAS` – e.g. `periodic`
- `ANDROID_KEY_PASSWORD` – key password

Or with the GitHub CLI:

```bash
gh secret set ANDROID_KEYSTORE_BASE64 < keystore.b64
gh secret set ANDROID_KEYSTORE_PASSWORD
gh secret set ANDROID_KEY_ALIAS
gh secret set ANDROID_KEY_PASSWORD
```

Then delete `keystore.b64`, never commit the keystore, and back up the keystore and
passwords in a password manager. Losing them means testers must uninstall once.

Verify: run the "Build APK" workflow twice, download `periodic-release-apk`, install
the first (uninstall any old debug build once), then install the second over it.
Pull requests from forks don't receive secrets, so their release build will fail
with "Release signing is not configured".

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
