#!/bin/bash
# Script to manually create fat JAR with all dependencies

cd "$(dirname "$0")"

echo "Creating fat JAR with all Adobe dependencies..."

# Create temp directory
mkdir -p target/fat-jar-temp
cd target/fat-jar-temp

# Extract main JAR
echo "Extracting main JAR..."
jar xf ../invisiblewatermark-1.0-SNAPSHOT.jar

# Extract Adobe JARs
echo "Extracting Adobe JARs..."
jar xf ../../libs/pdfcore.jar
jar xf ../../libs/pdfservices.jar
jar xf ../../libs/rideau.jar

# Extract Maven dependencies
echo "Extracting Maven dependencies..."
for jarfile in ../libs/*.jar; do
    if [ -f "$jarfile" ]; then
        jar xf "$jarfile"
    fi
done

# Create manifest
echo "Creating manifest..."
cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: com.adobe.livecycle.watermark.InvisibleWatermarkTool
Created-By: Maven Assembly
EOF

# Create fat JAR
echo "Creating fat JAR..."
jar cmf META-INF/MANIFEST.MF ../invisiblewatermark-1.0-SNAPSHOT-jar-with-dependencies.jar .

cd ../..
echo "Cleaning up..."
rm -rf target/fat-jar-temp

echo ""
echo "✅ Done! Fat JAR created at:"
echo "   target/invisiblewatermark-1.0-SNAPSHOT-jar-with-dependencies.jar"
echo ""
echo "Run with:"
echo "   java -jar target/invisiblewatermark-1.0-SNAPSHOT-jar-with-dependencies.jar encode db.txt input.pdf userid output.pdf"

