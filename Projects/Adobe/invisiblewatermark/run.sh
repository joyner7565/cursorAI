#!/bin/bash
# Script to run the Invisible Watermark Tool with PDFBox
# Uses Maven to build the classpath automatically

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if compiled classes exist
if [ ! -d "target/classes" ]; then
    echo "❌ Error: Project not compiled. Run 'mvn compile' first."
    exit 1
fi

# Print usage if no arguments
if [ $# -eq 0 ]; then
    echo "Usage:"
    echo "  $0 encode <dbfilepath> <pdfinputfile> <watermarkid> <pdfoutputfile>"
    echo "  $0 decode <dbfilepath> <pdfinputfile> <pdfwatermarkfile>"
    echo ""
    echo "Example:"
    echo "  $0 encode db.txt input.pdf \"userid123\" output.pdf"
    echo "  $0 decode db.txt input.pdf output.pdf"
    exit 1
fi

# Build classpath using Maven (includes PDFBox from Maven Central)
CP=$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)
CP="target/classes:$CP"

# Run the application
java -cp "$CP" com.adobe.livecycle.watermark.InvisibleWatermarkTool "$@"
