#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BUILD="true"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build)
      BUILD="false"
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: ./scripts/publish-package.sh [--no-build]"
      exit 1
      ;;
  esac
done

PKG_NAME="$(node -e "const fs=require('fs');const pkg=JSON.parse(fs.readFileSync('package.json','utf-8'));process.stdout.write(pkg.name);")"
PUBLISH_ACCESS="$(node -e "const fs=require('fs');const pkg=JSON.parse(fs.readFileSync('package.json','utf-8'));process.stdout.write(pkg.publishConfig?.access || 'public');")"

echo "Publishing $PKG_NAME"

if [[ "$BUILD" == "true" ]]; then
  pnpm build
fi

pnpm publish --access "$PUBLISH_ACCESS" --no-git-checks

echo "Done: published $PKG_NAME"
