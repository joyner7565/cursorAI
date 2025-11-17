/*     */ package com.adobe.livecycle.watermark;
/*     */ import com.adobe.livecycle.watermark.api.DecodeWatermark;
/*     */ import com.adobe.livecycle.watermark.api.EncodeWatermark;
/*     */ import com.adobe.livecycle.watermark.api.PDFFile;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ 
/*     */ public class InvisibleWatermarkTool {
/*  11 */   private static HashMap codeTablet = new HashMap<>();
/*     */ 
/*     */   
/*     */   public static void main(String[] args) throws Exception {
/*  15 */     if (args.length != 4 && args.length != 5) {
/*  16 */       printUsage();
/*  17 */       System.exit(-1);
/*     */     } 
/*     */ 
/*     */     
/*  21 */     String dbFilePath = args[1];
/*     */     try {
/*  23 */       readDBFile(dbFilePath);
/*     */     }
/*  25 */     catch (IOException ioe) {
/*  26 */       System.out.println(ioe.getMessage());
/*  27 */       System.exit(-1);
/*     */     } 
/*     */     
/*  30 */     if (args[0].compareToIgnoreCase("encode") == 0) {
/*  31 */       if (args.length != 5) {
/*  32 */         printUsage();
/*  33 */         System.exit(-1);
/*     */       } 
/*  35 */       String inputPDFPath = args[2];
/*  36 */       String watermarkId = args[3];
/*  37 */       String outputPDFPath = args[4];
/*  38 */       encodeInvisibleWatermark(inputPDFPath, watermarkId, outputPDFPath);
/*     */     }
/*  40 */     else if (args[0].compareToIgnoreCase("decode") == 0) {
/*  41 */       if (args.length != 4) {
/*  42 */         printUsage();
/*  43 */         System.exit(-1);
/*     */       } 
/*  45 */       String inputPDFPath = args[2];
/*  46 */       String watermarkPDFPath = args[3];
/*  47 */       decodeInvisibleWatermark(inputPDFPath, watermarkPDFPath);
/*     */     } else {
/*     */       
/*  50 */       printUsage();
/*     */     } 
/*     */ 
/*     */     
/*  54 */     flushDBFile(dbFilePath);
/*     */   }
/*     */ 
/*     */   
/*     */   static void printUsage() {
/*  59 */     System.out.println("Usage: ");
/*  60 */     System.out.println("<program> encode <dbfilepath> <pdfinputfile> <watermarkid> <pdfoutputfile>");
/*  61 */     System.out.println("[or]");
/*  62 */     System.out.println("<program> decode <dbfilepath> <pdfinputfile> <pdfwatermarkfile>");
/*     */   }
/*     */ 
/*     */   
/*     */   static void flushDBFile(String dbFilePath) throws IOException {
/*  67 */     if (codeTablet.isEmpty()) {
/*     */       return;
/*     */     }
/*  70 */     File file = new File(dbFilePath);
/*     */     
/*  72 */     boolean justCreated = file.createNewFile();
/*     */     
/*  74 */     BufferedWriter dbFile = new BufferedWriter(new FileWriter(file));
/*     */     
/*  76 */     Iterator<String> titleIter = codeTablet.keySet().iterator();
/*  77 */     while (titleIter.hasNext()) {
/*  78 */       String title = titleIter.next();
/*  79 */       dbFile.write("TITLE:" + title + "\n");
/*     */       
/*  81 */       HashMap mappings = (HashMap)codeTablet.get(title);
/*  82 */       Iterator<String> mappingsIter = mappings.keySet().iterator();
/*  83 */       while (mappingsIter.hasNext()) {
/*  84 */         String code = mappingsIter.next();
/*  85 */         String id = (String)mappings.get(code);
/*     */         
/*  87 */         dbFile.write(String.valueOf(code) + " " + id + "\n");
/*     */       } 
/*     */       
/*  90 */       dbFile.write("\n");
/*     */     } 
/*     */     
/*  93 */     dbFile.close();
/*     */   }
/*     */ 
/*     */   
/*     */   static void readDBFile(String dbFilePath) throws IOException {
/*  98 */     File file = new File(dbFilePath);
/*     */     
/* 100 */     boolean justCreated = file.createNewFile();
/*     */     
/* 102 */     if (!justCreated) {
/* 103 */       BufferedReader dbFile = new BufferedReader(new FileReader(file));
/*     */       
/* 105 */       int AWAITING_TITLE = 1;
/* 106 */       int AWAITING_CODE = 2;
/*     */       
/* 108 */       int state = 1;
/*     */       
/* 110 */       codeTablet = new HashMap<>();
/*     */ 
/*     */       
/* 113 */       String title = ""; String line;
/* 114 */       while ((line = dbFile.readLine()) != null) {
/*     */         String[] tokens; HashMap<String, String> mappings;
/* 116 */         if (line.length() == 0) {
/* 117 */           state = 1;
/*     */           
/*     */           continue;
/*     */         } 
/* 121 */         switch (state) {
/*     */           case 1:
/* 123 */             if (!line.startsWith("TITLE:")) {
/* 124 */               throw new IOException("Error: Invalid db file!");
/*     */             }
/*     */             
/* 127 */             title = line.substring("TITLE:".length());
/* 128 */             if (title.length() == 0) {
/* 129 */               throw new IOException("Error: Invalid db file!");
/*     */             }
/* 131 */             codeTablet.put(title, new HashMap<>());
/* 132 */             state = 2;
/*     */ 
/*     */           
/*     */           case 2:
/* 136 */             tokens = line.split(" ");
/* 137 */             if (tokens.length != 2) {
/* 138 */               throw new IOException("Error: Invalid db file!");
/*     */             }
/*     */             
/* 141 */             mappings = (HashMap)codeTablet.get(title);
/* 142 */             if (mappings.containsKey(tokens[0])) {
/* 143 */               throw new IOException("Error: Invalid db file!");
/*     */             }
/*     */             
/* 146 */             mappings.put(tokens[0], tokens[1]);
/*     */         } 
/*     */ 
/*     */ 
/*     */       
/*     */       } 
/* 152 */       dbFile.close();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   static void encodeInvisibleWatermark(String inputPDFPath, String watermarkId, String outputPDFPath) throws Exception {
/* 158 */     if (watermarkId.indexOf(' ') != -1 || 
/* 159 */       watermarkId.indexOf('\\') != -1 || 
/* 160 */       watermarkId.indexOf('/') != -1 || 
/* 161 */       watermarkId.indexOf(':') != -1 || 
/* 162 */       watermarkId.indexOf('*') != -1 || 
/* 163 */       watermarkId.indexOf('?') != -1 || 
/* 164 */       watermarkId.indexOf('"') != -1 || 
/* 165 */       watermarkId.indexOf('<') != -1 || 
/* 166 */       watermarkId.indexOf('>') != -1 || 
/* 167 */       watermarkId.indexOf('|') != -1) {
/* 168 */       System.out.println("Error: Invalid watermark");
/*     */       
/*     */       return;
/*     */     } 
/*     */     
/* 173 */     PDFFile pdfFile = new PDFFile(inputPDFPath);
/*     */     
/* 175 */     String pdfTitle = pdfFile.getDocTitle();
/*     */     
/* 177 */     EncodeWatermark encoder = new EncodeWatermark();
/*     */     
/* 179 */     int wmBitsLength = 10;
/*     */     
/* 181 */     boolean isWmBitsUnique = false;
/* 182 */     String wmBits = "";
/*     */     
/* 184 */     while (!isWmBitsUnique) {
/* 185 */       wmBits = generateRandBits(wmBitsLength);
/*     */       
/* 187 */       if (codeTablet.containsKey(pdfTitle)) {
/* 188 */         HashMap hashMap = (HashMap)codeTablet.get(pdfTitle);
/* 189 */         if (!hashMap.containsKey(wmBits)) {
/* 190 */           isWmBitsUnique = true;
/*     */         }
/*     */         continue;
/*     */       } 
/* 194 */       isWmBitsUnique = true;
/*     */     } 
/*     */ 
/*     */     
/* 198 */     if (!codeTablet.containsKey(pdfTitle)) {
/* 199 */       codeTablet.put(pdfTitle, new HashMap<>());
/*     */     }
/* 201 */     HashMap<String, String> mappings = (HashMap)codeTablet.get(pdfTitle);
/* 202 */     mappings.put(wmBits, watermarkId);
/*     */     
/*     */     try {
/* 205 */       pdfFile = encoder.encode(pdfFile, wmBits);
/*     */     }
/* 207 */     catch (Exception e) {
/* 208 */       System.out.println(e.getMessage());
/*     */       
/* 210 */       pdfFile.close();
/*     */       
/*     */       return;
/*     */     } 
/* 214 */     pdfFile.saveAsAndClose(outputPDFPath);
/* 215 */     System.out.println("Success");
/*     */   }
/*     */ 
/*     */   
/*     */   static void decodeInvisibleWatermark(String inputPDFPath, String watermarkPDFPath) throws Exception {
/* 220 */     PDFFile orgPdfFile = null;
/*     */     try {
/* 222 */       orgPdfFile = new PDFFile(inputPDFPath);
/*     */     }
/* 224 */     catch (Exception e) {
/* 225 */       System.out.println("Error: could not read input PDF file!");
/*     */       
/*     */       return;
/*     */     } 
/* 229 */     PDFFile wmPdfFile = null;
/*     */     try {
/* 231 */       wmPdfFile = new PDFFile(watermarkPDFPath);
/*     */     }
/* 233 */     catch (Exception e) {
/* 234 */       System.out.println("Error: could not read watermarked PDF file!");
/*     */       
/*     */       return;
/*     */     } 
/*     */     
/* 239 */     String orgPdfTitle = orgPdfFile.getDocTitle();
/* 240 */     String wmPdfTitle = wmPdfFile.getDocTitle();
/* 241 */     if (!orgPdfTitle.equals(wmPdfTitle)) {
/* 242 */       orgPdfFile.close();
/* 243 */       wmPdfFile.close();
/*     */       
/* 245 */       System.out.println("Error: incorrect watermark file!");
/*     */       
/*     */       return;
/*     */     } 
/*     */     
/* 250 */     DecodeWatermark decoder = new DecodeWatermark();
/*     */     
/* 252 */     String decodedWatermark = "";
/*     */     try {
/* 254 */       decodedWatermark = decoder.decode(orgPdfFile, wmPdfFile);
/*     */     
/*     */     }
/* 257 */     catch (Exception e) {
/* 258 */       System.out.println(e.getMessage());
/*     */     } 
/*     */     
/* 261 */     orgPdfFile.close();
/* 262 */     wmPdfFile.close();
/*     */     
/* 264 */     if (decodedWatermark.length() == 0) {
/* 265 */       System.out.println("Error: identical files!");
/*     */       
/*     */       return;
/*     */     } 
/* 269 */     if (!codeTablet.containsKey(orgPdfTitle)) {
/* 270 */       System.out.println("Error: cannot find mapping in db file!");
/*     */     } else {
/*     */       
/* 273 */       HashMap mappings = (HashMap)codeTablet.get(orgPdfTitle);
/* 274 */       if (!mappings.containsKey(decodedWatermark)) {
/* 275 */         System.out.println("Error: cannot find mapping in db file!");
/*     */       } else {
/*     */         
/* 278 */         String watermarkId = (String)mappings.get(decodedWatermark);
/* 279 */         System.out.println("Success: " + watermarkId);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   static int getmenu() throws IOException {
/*     */     int choice;
/* 286 */     BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
/*     */     
/*     */     while (true) {
/* 289 */       System.out.println("1. Encode Invisible Watermark.");
/* 290 */       System.out.println("2. Decode Invisible Watermark.");
/* 291 */       System.out.println("3. Exit.");
/* 292 */       System.out.print("Enter your choice: ");
/*     */       
/* 294 */       String s = "";
/* 295 */       while (s.length() == 0) {
/* 296 */         s = console.readLine();
/*     */       }
/*     */       
/* 299 */       choice = 0;
/*     */       try {
/* 301 */         choice = Integer.parseInt(s);
/*     */       }
/* 303 */       catch (NumberFormatException numberFormatException) {}
/*     */ 
/*     */       
/* 306 */       if (choice < 1 || choice > 3) {
/* 307 */         System.out.println("Invalid choice");
/* 308 */         System.out.println(); continue;
/*     */       }  break;
/*     */     } 
/* 311 */     return choice;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   static String generateRandBits(int numBits) {
/* 317 */     Math.rint(0.0D);
/* 318 */     String randBits = "";
/*     */     
/* 320 */     int i = 0;
/* 321 */     while (i++ < numBits) {
/* 322 */       double d = Math.random();
/* 323 */       long val = Math.round(d);
/*     */       
/* 325 */       randBits = randBits.concat(Long.toString(val));
/*     */     } 
/* 327 */     return randBits;
/*     */   }
/*     */ }


/* Location:              /Users/tysonbowman/Downloads/InvisibleWatermarking/InvisibleWatermark/install/invisible-watermark-bundle-1.0-SNAPSHOT.jar!/invisible-watermark-core-1.0.0.jar!/com/adobe/livecycle/sample/watermark/InvisibleWatermarkTool.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */