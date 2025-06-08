#!/bin/bash
set -e

TARGET_DIR="template-service-client/target/generated-sources/openapi/src/main/java"

find "$TARGET_DIR" -type f -name "*.java" | while read -r file; do
  # Replace annotation usages
  sed -i '' 's/@javax.annotation.Nullable/@org.jetbrains.annotations.Nullable/g' "$file"
  sed -i '' 's/@javax.annotation.Nonnull/@org.jetbrains.annotations.NotNull/g' "$file"
  # Remove old imports
  sed -i '' '/import javax.annotation.Nullable;/d' "$file"
  sed -i '' '/import javax.annotation.Nonnull;/d' "$file"
  # Add JetBrains imports if not present
  if ! grep -q 'import org.jetbrains.annotations.Nullable;' "$file"; then
    sed -i '' '/package .*/a\
import org.jetbrains.annotations.Nullable;
' "$file"
  fi
  if ! grep -q 'import org.jetbrains.annotations.NotNull;' "$file"; then
    sed -i '' '/package .*/a\
import org.jetbrains.annotations.NotNull;
' "$file"
  fi
done 