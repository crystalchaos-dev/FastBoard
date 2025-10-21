#!/bin/bash

set -euo pipefail

# Run Spotless check first
#echo "Running Spotless check..."
#if ! ./gradlew spotlessCheck; then
#    echo "Spotless check failed. Running spotlessApply..."
#    ./gradlew spotlessApply
#    git add .
#    git commit -m "chore: apply spotless formatting"
#fi

echo "Checking if build succeeds..."
if ! ./gradlew clean build; then
    echo "Initial build failed. Aborting release process."
    exit 1
fi

# Get the current version from gradle.properties
version=$(grep -oP 'version=\K.*(?=-SNAPSHOT)' gradle.properties)

# Remove the "-SNAPSHOT" suffix from gradle.properties
sed -i 's/^\(version=\).*-SNAPSHOT$/\1'"$version"'/' gradle.properties
sed -i 's/^\(projectVersion=\).*-SNAPSHOT$/\1'"$version"'/' gradle.properties

# Create a git tag for the version and push changes
git add gradle.properties
git commit -m "release: $version"
git tag "v$version" -m "Release version $version"
git push origin "v$version"

# Fetch tags and build the project
git fetch --tags origin
./gradlew clean build

# Create a GitHub release
#gh release create "v$version" --latest --verify-tag --generate-notes --title "v$version"

# Publish the release artifact
./gradlew publish # No signing (hopefully)

# Increment the version and add the "-SNAPSHOT" suffix to gradle.properties
new_version=$(echo "$version" | awk -F. '{$NF++;print}' | sed 's/ /./g')-SNAPSHOT
new_versionWithoutSnapshot=$(echo "$version" | awk -F. '{$NF++;print}' | sed 's/ /./g')
sed -i 's/^\(version=\).*$/\1'"$new_version"'/' gradle.properties
sed -i 's/^\(projectVersion=\).*$/\1'"$new_versionWithoutSnapshot"'/' gradle.properties

# Commit the change to gradle.properties and push
git add gradle.properties
git commit -m "snapshot: $new_version"
git push
