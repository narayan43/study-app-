#!/usr/bin/env bash
# ==============================================================================
# ExamPrep CSV - Next Release Version & Code Calculator
#
# Generates MAJOR.MINOR.PATCH version numbers and monotonically incrementing
# version codes based on existing git tags.
#
# Rules:
# - Format: MAJOR.MINOR.PATCH
# - Initial version: 1.0.1 (code 2)
# - Each release: PATCH + 1
# - When PATCH reaches 10: MINOR + 1, PATCH = 0 (e.g. 1.0.9 -> 1.1.0)
# - versionCode: number of existing release tags + 2
# ==============================================================================

set -euo pipefail

# Fetch tags if in a git repository
if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    TAGS=$(git tag -l "v*.*.*" 2>/dev/null || true)
else
    TAGS=""
fi

VALID_TAGS=$(printf "%s\n" "$TAGS" | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' || true)

if [ -z "$VALID_TAGS" ]; then
    NEXT_VERSION="1.0.1"
    NEXT_CODE=2
else
    LATEST_TAG=$(printf "%s\n" "$VALID_TAGS" | sort -V | tail -n 1)
    CLEAN_TAG="${LATEST_TAG#v}"

    MAJOR=$(echo "$CLEAN_TAG" | cut -d'.' -f1)
    MINOR=$(echo "$CLEAN_TAG" | cut -d'.' -f2)
    PATCH=$(echo "$CLEAN_TAG" | cut -d'.' -f3)

    NEW_PATCH=$(( 10#$PATCH + 1 ))
    NEW_MINOR=$(( 10#$MINOR ))
    NEW_MAJOR=$(( 10#$MAJOR ))

    if [ "$NEW_PATCH" -ge 10 ]; then
        NEW_PATCH=0
        NEW_MINOR=$(( NEW_MINOR + 1 ))
    fi

    NEXT_VERSION="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"

    TAG_COUNT=$(printf "%s\n" "$VALID_TAGS" | wc -l | tr -d ' ')
    NEXT_CODE=$(( TAG_COUNT + 2 ))
fi

echo "Calculated next release: version=$NEXT_VERSION, code=$NEXT_CODE"

if [ -n "${GITHUB_OUTPUT:-}" ]; then
    echo "version=$NEXT_VERSION" >> "$GITHUB_OUTPUT"
    echo "code=$NEXT_CODE" >> "$GITHUB_OUTPUT"
fi
