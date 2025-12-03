# ⚡ Quick Start Guide

## 🚨 IMPORTANT: How to Run (Avoid ClassNotFoundException)

The `ClassNotFoundException` error happens when you run the application **without** the Adobe JARs in the classpath.

### ✅ CORRECT Way to Run:

```bash
# Step 1: Compile once
cd /Users/joyner/Documents/Projects/Adobe/invisiblewatermark
mvn clean compile

# Step 2: Always use the run.sh script
./run.sh encode db.txt input.pdf "userid" output.pdf
./run.sh decode db.txt input.pdf output.pdf
```

### ❌ WRONG Ways (Will Cause Errors):

```bash
# ❌ DON'T run the JAR directly
java -jar target/invisiblewatermark-1.0-SNAPSHOT.jar ...

# ❌ DON'T run from Eclipse "Run As → Java Application" 
#    (unless you configure the classpath)

# ❌ DON'T use java -cp target/classes ...
#    (missing Adobe JARs)
```

---

## 📋 Complete Step-by-Step

### First Time Setup:
```bash
cd /Users/joyner/Documents/Projects/Adobe/invisiblewatermark

# Compile the code
mvn clean compile

# Make script executable (if needed)
chmod +x run.sh
```

### Encode (Add Invisible Watermark):
```bash
./run.sh encode database.txt input.pdf "john.doe@company.com" output_watermarked.pdf
```

**What happens:**
- Reads `input.pdf`
- Embeds invisible watermark with ID `"john.doe@company.com"`
- Creates `output_watermarked.pdf`
- Saves mapping to `database.txt`

### Decode (Extract Watermark):
```bash
./run.sh decode database.txt input.pdf output_watermarked.pdf
```

**What happens:**
- Compares original (`input.pdf`) and watermarked (`output_watermarked.pdf`)
- Extracts the watermark code
- Looks up the ID in `database.txt`
- Prints: `Success: john.doe@company.com`

---

## 🔧 Running from Eclipse/IDE

If you want to run from your IDE, you need to configure the classpath:

### Eclipse:
1. Right-click project → **Run As** → **Run Configurations**
2. Select your configuration
3. Go to **Classpath** tab
4. Click **User Entries** → **Add External JARs**
5. Add these three JARs:
   - `libs/pdfcore.jar`
   - `libs/pdfservices.jar`
   - `libs/rideau.jar`
6. Click **Apply** → **Run**

### IntelliJ IDEA:
1. **File** → **Project Structure** → **Modules**
2. Select your module → **Dependencies** tab
3. Click **+** → **JARs or directories**
4. Add all three Adobe JARs from `libs/` folder
5. Click **OK**

---

## 🐛 Troubleshooting

### Error: ClassNotFoundException: EncryptionImpl / ByteReader / PDFSaveOptions

**Cause:** Adobe JARs are not in the classpath

**Solution:** 
```bash
# Always use the run.sh script
./run.sh encode db.txt input.pdf "userid" output.pdf
```

### Error: "var cannot be resolved to a type"

**Cause:** IDE is using Java 8 instead of Java 21

**Solution:**
```bash
# In Eclipse:
Right-click project → Maven → Update Project (Alt+F5)

# Or verify Java version:
java -version  # Should show 21.0.2
```

### Error: "Project not compiled"

**Solution:**
```bash
mvn clean compile
```

### Script doesn't run: "Permission denied"

**Solution:**
```bash
chmod +x run.sh
```

---

## 📝 Notes

- The watermark is **completely invisible** - no visual changes to the PDF
- The watermark is embedded in text line spacing (imperceptible changes)
- Keep `database.txt` secure - it's needed to decode watermarks
- Watermark ID cannot contain: space, \, /, :, *, ?, ", <, >, |

---

## ✅ Verify It Works

Test with the sample file:

```bash
# Create test database and encode
./run.sh encode test_db.txt resource/TestFile.pdf "test_user_123" watermarked_test.pdf

# Decode it back
./run.sh decode test_db.txt resource/TestFile.pdf watermarked_test.pdf

# Expected output:
# Success: test_user_123
```

If you see "Success: test_user_123", everything is working! 🎉

