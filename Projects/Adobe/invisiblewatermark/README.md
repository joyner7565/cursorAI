# Invisible Watermark Tool - Java 21

A tool to embed and extract invisible watermarks in PDF documents using imperceptible text line spacing adjustments.

## 🚀 Quick Start

### First Time Setup

**Step 1: Install Adobe JARs to Maven Repository**
```bash
cd /Users/joyner/Documents/Projects/Adobe/invisiblewatermark
./install-adobe-jars.sh
```

This eliminates Maven warnings and makes the JARs available to Maven.

**Step 2: Compile the Project**
```bash
mvn clean compile
```

**Step 3: Run the Tool**
```bash
# Add watermark
./run.sh encode db.txt input.pdf "userid@example.com" output.pdf

# Extract watermark
./run.sh decode db.txt input.pdf output.pdf
```

---

## 📖 Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Detailed usage guide and troubleshooting
- **[BUILD.md](BUILD.md)** - Build options and advanced configuration

---

## 🛠 Requirements

- **Java 21** (OpenJDK 21.0.2 LTS or later)
- Maven 3.x
- Adobe PDF libraries (included in `libs/` directory)

---

## 📝 Usage Examples

### Encode (Add Watermark)
```bash
./run.sh encode database.txt original.pdf "john.doe@company.com" watermarked.pdf
```

### Decode (Extract Watermark)
```bash
./run.sh decode database.txt original.pdf watermarked.pdf
# Output: Success: john.doe@company.com
```

---

## ⚙️ How It Works

The tool embeds invisible watermarks by making imperceptible adjustments to text line spacing in PDFs:

1. **Encoding**: Generates a unique binary code, maps it to your watermark ID, and subtly adjusts line spacing to encode the bits
2. **Decoding**: Compares the original and watermarked PDFs, extracts the binary code, and looks up the watermark ID

The changes are completely invisible to the human eye but can be reliably detected and decoded.

---

## 📁 Project Structure

```
invisiblewatermark/
├── libs/                          # Adobe PDF JARs
│   ├── pdfcore.jar
│   ├── pdfservices.jar
│   └── rideau.jar
├── src/main/java/                 # Source code
│   └── com/adobe/livecycle/watermark/
│       ├── InvisibleWatermarkTool.java    # Main entry point
│       └── api/
│           ├── EncodeWatermark.java       # Watermark encoding logic
│           ├── DecodeWatermark.java       # Watermark decoding logic
│           ├── PDFFile.java               # PDF file operations
│           └── WatermarkUtils.java        # Utility methods
├── pom.xml                        # Maven configuration (Java 21)
├── run.sh                         # Run script with proper classpath
├── install-adobe-jars.sh          # Install Adobe JARs to Maven repo
├── README.md                      # This file
├── QUICKSTART.md                  # Detailed usage guide
└── BUILD.md                       # Build instructions
```

---

## 🔧 Development

### Compile
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Package
```bash
mvn package
```

### Eclipse Setup
1. Import as Maven project
2. Refresh project (F5) to pick up Java 21 settings
3. Run configurations will automatically include Adobe JARs

---

## 🐛 Troubleshooting

### "ClassNotFoundException" errors
**Solution:** Always use `./run.sh` to run the application. It includes all Adobe JARs in the classpath.

### "var cannot be resolved to a type"
**Solution:** Your IDE needs to be configured for Java 21. In Eclipse: Right-click project → Maven → Update Project (Alt+F5)

### Maven systemPath warnings
**Solution:** Run `./install-adobe-jars.sh` to install Adobe JARs to your local Maven repository.

See **[QUICKSTART.md](QUICKSTART.md)** for more troubleshooting tips.

---

## 📜 License

This code uses Adobe internal libraries. Check with Adobe for licensing terms.

---

## ✨ Features

- ✅ Java 21 with modern features (`var`, enhanced switch, try-with-resources)
- ✅ Completely invisible watermarks
- ✅ Reliable encoding and decoding
- ✅ Database-backed watermark ID mapping
- ✅ Command-line interface
- ✅ Eclipse-compatible project configuration

---

## 🎯 Next Steps

1. Run `./install-adobe-jars.sh` (one time)
2. Run `mvn clean compile`
3. Test with: `./run.sh encode test_db.txt resource/TestFile.pdf "test123" output.pdf`
4. Decode with: `./run.sh decode test_db.txt resource/TestFile.pdf output.pdf`

If you see "Success: test123", you're all set! 🎉

