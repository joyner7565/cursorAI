# Build and Run Instructions for Java 21

## Prerequisites
- **Java 21** (OpenJDK 21.0.2 LTS or later)
- Maven 3.x (optional for building)

## ✅ RECOMMENDED: Use the Run Script (Simplest)

This is the **easiest and most reliable method** because it properly handles the Adobe JAR dependencies.

### Step 1: Compile (if not already compiled)
```bash
cd /Users/joyner/Documents/Projects/Adobe/invisiblewatermark
mvn compile
```

### Step 2: Run with the script
```bash
# Encode (add watermark)
./run.sh encode db.txt resource/TestFile.pdf "userid123" output.pdf

# Decode (extract watermark)
./run.sh decode db.txt resource/TestFile.pdf output.pdf
```

---

## Alternative: Manual Classpath

If you prefer not to use the script:

```bash
# Compile first
mvn compile

# Run with full classpath
java -cp "target/classes:libs/pdfcore.jar:libs/pdfservices.jar:libs/rideau.jar:target/libs/*" \
  com.adobe.livecycle.watermark.InvisibleWatermarkTool \
  encode db.txt resource/TestFile.pdf "userid123" output.pdf
```

---

## Alternative: Build Fat JAR (Manual)

If you want a single executable JAR with all dependencies:

```bash
# Step 1: Compile
mvn clean compile

# Step 2: Run the assembly script
./assembly-plugin.sh

# Step 3: Run the fat JAR
java -jar target/invisiblewatermark-1.0-SNAPSHOT-jar-with-dependencies.jar \
  encode db.txt resource/TestFile.pdf "userid123" output.pdf
```

---

## Usage Examples

### Encode (Add Watermark)
```bash
./run.sh encode database.txt input.pdf "user_email@example.com" watermarked_output.pdf
```

- `database.txt` - Database file that stores watermark mappings (will be created if it doesn't exist)
- `input.pdf` - Original PDF file
- `"user_email@example.com"` - Watermark ID (any string without special chars: space, \, /, :, *, ?, ", <, >, |)
- `watermarked_output.pdf` - Output PDF with invisible watermark

### Decode (Extract Watermark)
```bash
./run.sh decode database.txt input.pdf watermarked_output.pdf
```

- Compares the original and watermarked PDFs
- Extracts and displays the watermark ID
- Requires the same database file used during encoding

---

## Troubleshooting

### Error: "ClassNotFoundException"
**Solution:** Use `./run.sh` instead of running the JAR directly. The script includes all Adobe JARs in the classpath.

### Error: "var cannot be resolved to a type"
**Solution:** Make sure your IDE is configured for Java 21:
- Eclipse: Right-click project → Maven → Update Project (Alt+F5)
- IntelliJ: Right-click pom.xml → Maven → Reload Project

### Error: "Unable to locate a Java Runtime"
**Solution:** Ensure Java 21 is installed and in your PATH:
```bash
java -version  # Should show "openjdk version "21.0.2""
```

---

## Notes
- The watermark is **invisible** and embedded in the PDF's text line spacing
- The `database.txt` file maps internal codes to watermark IDs
- Keep the database file secure - it's needed to decode watermarks
- The code uses Java 21 features: `var`, enhanced switch, try-with-resources
