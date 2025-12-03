#!/bin/bash
# Script to run the Invisible Watermark Tool
# Ensures Adobe JARs are included in classpath

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

# Build classpath - start with compiled classes
CP="target/classes"

# Try to use Maven to get dependencies from .m2/repository
echo "Building classpath..."
MAVEN_CP=$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout 2>/dev/null || echo "")

if [ -n "$MAVEN_CP" ]; then
    # Maven classpath available - use it
    CP="$CP:$MAVEN_CP"
    echo "✅ Using Maven dependencies from .m2/repository"
else
    # Fallback to libs/ folder if Maven fails
    echo "⚠️  Maven classpath unavailable, using libs/ folder"
    if [ -f "libs/pdfcore.jar" ]; then
        CP="$CP:libs/pdfcore.jar"
    fi
    if [ -f "libs/pdfservices.jar" ]; then
        CP="$CP:libs/pdfservices.jar"
    fi
    if [ -f "libs/rideau.jar" ]; then
        CP="$CP:libs/rideau.jar"
    fi
fi

# Add any additional dependencies from target/libs if they exist
if [ -d "target/libs" ]; then
    for jar in target/libs/*.jar; do
        if [ -f "$jar" ]; then
            CP="$CP:$jar"
        fi
    done
fi

# Debug: Print classpath if verbose
if [ "$VERBOSE" = "1" ]; then
    echo "Classpath: $CP"
fi

# Run the application
java -cp "$CP" com.adobe.livecycle.watermark.InvisibleWatermarkTool "$@"
