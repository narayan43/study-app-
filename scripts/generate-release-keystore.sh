#!/usr/bin/env bash
# ==============================================================================
# ExamPrep CSV - Release Keystore Setup Script
#
# Generates a stable release/upload keystore so every subsequent release is signed
# with the EXACT same cryptographic key.
# This prevents the Android "INSTALL_FAILED_UPDATE_INCOMPATIBLE" error and allows
# in-place app updates from GitHub releases without uninstalling.
# ==============================================================================

set -euo pipefail

KEYSTORE_FILE="release.keystore"
KEY_ALIAS="upload"
KEY_VALIDITY=10000

echo "=== ExamPrep CSV: Release Keystore Generator ==="

if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  Found existing $KEYSTORE_FILE in current directory."
    read -p "Do you want to overwrite it? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Keeping existing $KEYSTORE_FILE."
    else
        rm "$KEYSTORE_FILE"
    fi
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    read -sp "Enter password for the release keystore (min 6 characters): " PASSWORD
    echo
    read -sp "Confirm password: " PASSWORD_CONFIRM
    echo

    if [ "$PASSWORD" != "$PASSWORD_CONFIRM" ]; then
        echo "❌ Passwords do not match. Aborting."
        exit 1
    fi

    if [ ${#PASSWORD} -lt 6 ]; then
        echo "❌ Password must be at least 6 characters. Aborting."
        exit 1
    fi

    echo "Generating $KEYSTORE_FILE..."
    keytool -genkey -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity $KEY_VALIDITY \
        -storepass "$PASSWORD" \
        -keypass "$PASSWORD" \
        -dname "CN=ExamPrep, OU=ExamPrep Release, O=AI Studio, L=Delhi, ST=Delhi, C=IN"

    echo "✅ Keystore created at $(pwd)/$KEYSTORE_FILE"

    # Write key.properties for local builds
    cat <<EOF > key.properties
storeFile=$(pwd)/$KEYSTORE_FILE
storePassword=$PASSWORD
keyAlias=$KEY_ALIAS
keyPassword=$PASSWORD
EOF
    echo "✅ Created local 'key.properties' for ./gradlew assembleRelease"
fi

echo
echo "======================================================================"
echo "  GITHUB ACTIONS SECRETS SETUP (ONE-TIME):"
echo "  Add these secrets under: Repo Settings -> Secrets and variables -> Actions"
echo "======================================================================"
echo
echo "1. KEYSTORE_BASE64:"
base64 -w 0 "$KEYSTORE_FILE" 2>/dev/null || base64 "$KEYSTORE_FILE" | tr -d '\n'
echo
echo
echo "2. KEYSTORE_PASSWORD: (the password you entered above)"
echo "3. KEY_ALIAS: $KEY_ALIAS"
echo "4. KEY_PASSWORD: (the password you entered above)"
echo "======================================================================"
