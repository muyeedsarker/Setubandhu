#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ -f release-key.jks ]; then
  echo "release-key.jks already exists. Refusing to overwrite it."
  exit 1
fi
read -rsp "New keystore password: " PASS; echo
read -rsp "Confirm keystore password: " PASS2; echo
[ "$PASS" = "$PASS2" ] || { echo "Passwords do not match."; exit 1; }
keytool -genkeypair -v \
  -keystore release-key.jks \
  -alias setubandhu \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=Setubandhu, OU=Mobile, O=Setubandhu, L=Gazipur, ST=Dhaka, C=BD"
cat > keystore.properties <<PROPS
storeFile=release-key.jks
storePassword=$PASS
keyAlias=setubandhu
keyPassword=$PASS
PROPS
chmod 600 keystore.properties
chmod 600 release-key.jks
echo "Created release-key.jks and keystore.properties. Keep both private and back them up securely."
