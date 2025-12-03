#!/bin/bash
# Script to install Adobe JARs into local Maven repository
# This eliminates the systemPath warning

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Installing Adobe JARs to local Maven repository..."
echo ""

# Install pdfcore.jar
echo "Installing pdfcore.jar..."
mvn install:install-file \
  -Dfile=libs/pdfcore.jar \
  -DgroupId=com.adobe.internal \
  -DartifactId=pdfcore \
  -Dversion=1.0 \
  -Dpackaging=jar \
  -DgeneratePom=true

# Install pdfservices.jar
echo "Installing pdfservices.jar..."
mvn install:install-file \
  -Dfile=libs/pdfservices.jar \
  -DgroupId=com.adobe.internal \
  -DartifactId=pdfservices \
  -Dversion=1.0 \
  -Dpackaging=jar \
  -DgeneratePom=true

# Install rideau.jar
echo "Installing rideau.jar..."
mvn install:install-file \
  -Dfile=libs/rideau.jar \
  -DgroupId=com.adobe.internal \
  -DartifactId=rideau \
  -Dversion=1.0 \
  -Dpackaging=jar \
  -DgeneratePom=true

echo ""
echo "✅ Done! Adobe JARs installed to local Maven repository"
echo ""
echo "Now you can:"
echo "1. Update pom.xml to remove systemPath entries"
echo "2. Run: mvn clean compile"
echo "3. Use: ./run.sh encode db.txt input.pdf userid output.pdf"

