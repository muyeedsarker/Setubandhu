# সেতুবন্ধু — Release APK Build Guide

This project is configured for a signed Android release APK.

## GitHub Actions

Every push to `main` and every manual workflow run builds an installable release APK and uploads it as the `Setubandhu-release-apk` artifact.

Open **Actions → Build Setubandhu APK → Run workflow** to start a build manually.

## Local release signing

For a permanent production signing identity, create your own keystore using the scripts in `scripts/` and keep the JKS/passwords private.

- App: **সেতুবন্ধু (Setubandhu)**
- Application ID: `com.setubandhu.app`
- Version name: `1.0.1`
- Version code: `3`
- Min Android: 7.0 (API 24)
- Target Android: API 34
- Release: R8 shrinking + resource shrinking

Never upload `release-key.jks` or `keystore.properties` to GitHub.
