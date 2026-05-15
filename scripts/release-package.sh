#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

BUMP="${1:-patch}"
shift || true

PUSH="true"
TAG="true"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-push)
      PUSH="false"
      shift
      ;;
    --no-tag)
      TAG="false"
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: ./scripts/release-package.sh [patch|minor|major|prepatch|preminor|premajor|prerelease|<version>] [--no-push] [--no-tag]"
      exit 1
      ;;
  esac
done

PKG_NAME="$(node -e "const fs=require('fs');const pkg=JSON.parse(fs.readFileSync('package.json','utf-8'));process.stdout.write(pkg.name);")"

echo "Releasing $PKG_NAME with bump: $BUMP"

npm version "$BUMP" --no-git-tag-version
NEW_VERSION="$(node -e "const fs=require('fs');const pkg=JSON.parse(fs.readFileSync('package.json','utf-8'));process.stdout.write(pkg.version);")"

echo "New version: $NEW_VERSION"

pnpm test
pnpm build

FILES=("package.json")
if [[ -f "pnpm-lock.yaml" ]]; then
  FILES+=("pnpm-lock.yaml")
fi

git add "${FILES[@]}"
git commit -m "chore(release): v$NEW_VERSION" -- "${FILES[@]}"

if [[ "$TAG" == "true" ]]; then
  git tag "v$NEW_VERSION"
fi

if [[ "$PUSH" == "true" ]]; then
  BRANCH="$(git rev-parse --abbrev-ref HEAD)"
  git push origin "$BRANCH"
  if [[ "$TAG" == "true" ]]; then
    git push origin "v$NEW_VERSION"
  fi
fi

echo "Done: $PKG_NAME v$NEW_VERSION"
