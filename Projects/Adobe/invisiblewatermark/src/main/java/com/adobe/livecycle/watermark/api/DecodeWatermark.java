/*     */ package com.adobe.livecycle.watermark.api;
/*     */ 
/*     */ import com.adobe.internal.pdftoolkit.core.types.ASNumber;
/*     */ import com.adobe.internal.pdftoolkit.core.types.ASString;
/*     */ import com.adobe.internal.pdftoolkit.pdf.graphics.PDFRectangle;
/*     */ import com.adobe.internal.pdftoolkit.pdf.page.PDFPage;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.Instruction;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.OperandStack;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class DecodeWatermark
/*     */ {
/*     */   boolean verbose = false;
/*     */   
/*     */   public String decode(PDFFile orgPdfFile, PDFFile wmPdfFile) throws Exception {
/*  42 */     if (orgPdfFile.getNumPages() != wmPdfFile.getNumPages()) {
/*  43 */       throw new Exception("Error: Different no. of pages in org and watermarked pdf files. File tamper attempted?!");
/*     */     }
/*     */     
/*  46 */     String wmDecodedBits = new String("");
/*     */     
/*  48 */     int pageNumber = 0;
/*     */     
/*  50 */     while (pageNumber < orgPdfFile.getNumPages()) {
/*     */       
/*  52 */       PDFPage orgPdfPage = orgPdfFile.getPage(pageNumber);
/*  53 */       PDFPage wmPdfPage = wmPdfFile.getPage(pageNumber);
/*     */       
/*  55 */       ContentReader orgPageContentReader = WatermarkUtils.PDFPageToContentReader(orgPdfPage);
/*  56 */       ContentReader wmPageContentReader = WatermarkUtils.PDFPageToContentReader(wmPdfPage);
/*     */       
/*  58 */       PDFRectangle orgPageCropBox = orgPdfPage.getCropBox();
/*  59 */       PDFRectangle wmPageCropBox = wmPdfPage.getCropBox();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  65 */       ArrayList orgLineSpaces = computeLineSpacings(orgPageContentReader, orgPageCropBox, this.verbose);
/*  66 */       ArrayList wmLineSpaces = computeLineSpacings(wmPageContentReader, wmPageCropBox, this.verbose);
/*     */       
/*  68 */       if (orgLineSpaces.size() != wmLineSpaces.size()) {
/*  69 */         throw new Exception("Error: Different no. of lines in org and watermarked pdf files. File tamper attempted?!");
/*     */       }
/*     */       
/*  72 */       Iterator<Double> orgSpacesIter = orgLineSpaces.iterator();
/*  73 */       Iterator<Double> wmSpacesIter = wmLineSpaces.iterator();
/*     */ 
/*     */ 
/*     */       
/*  77 */       orgSpacesIter.next();
/*  78 */       wmSpacesIter.next();
/*     */       
/*  80 */       while (orgSpacesIter.hasNext() && wmSpacesIter.hasNext()) {
/*  81 */         Double orgLineSpace = orgSpacesIter.next();
/*  82 */         Double wmLineSpace = wmSpacesIter.next();
/*     */         
/*  84 */         if (wmLineSpace.doubleValue() < orgLineSpace.doubleValue()) {
/*     */           
/*  86 */           wmDecodedBits = wmDecodedBits.concat("1");
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*  91 */           if (orgSpacesIter.hasNext() && wmSpacesIter.hasNext()) {
/*  92 */             Double orgTempLineSpace = orgSpacesIter.next();
/*  93 */             Double wmTempLineSpace = wmSpacesIter.next();
/*     */             
/*  95 */             if (wmTempLineSpace.doubleValue() != orgTempLineSpace.doubleValue())
/*  96 */               throw new Exception("Error: Illegal line spacing condition met during decoding. File tamper attempted?!"); 
/*     */           } 
/*     */           continue;
/*     */         } 
/* 100 */         if (wmLineSpace.doubleValue() > orgLineSpace.doubleValue()) {
/*     */           
/* 102 */           wmDecodedBits = wmDecodedBits.concat("0");
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 107 */           if (orgSpacesIter.hasNext() && wmSpacesIter.hasNext()) {
/* 108 */             Double orgTempLineSpace = orgSpacesIter.next();
/* 109 */             Double wmTempLineSpace = wmSpacesIter.next();
/*     */             
/* 111 */             if (wmTempLineSpace.doubleValue() != orgTempLineSpace.doubleValue()) {
/* 112 */               throw new Exception("Error: Illegal line spacing condition met during decoding. File tamper attempted?!");
/*     */             }
/*     */           } 
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 121 */       pageNumber++;
/*     */     } 
/*     */     
/* 124 */     return wmDecodedBits;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private ArrayList computeLineSpacings(ContentReader pageContentReader, PDFRectangle pageCropBox, boolean verbose) throws Exception {
/* 130 */     double bottom = pageCropBox.bottom();
/* 131 */     double top = pageCropBox.top();
/*     */     
/* 133 */     int numLines = 0;
/* 134 */     boolean countedCurrentLine = false;
/*     */     
/* 136 */     double tx = 0.0D, ty = 0.0D;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 145 */     double xScaling = 1.0D, yScaling = 1.0D;
/*     */     
/* 147 */     double lp = 0.0D;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 154 */     double userUnit = 0.013888888888888888D;
/*     */     
/* 156 */     double lastLineSpace = 0.0D;
/* 157 */     ArrayList<Double> lineSpaces = new ArrayList();
/*     */     
/* 159 */     boolean firstTmOp = true;
/*     */     
/* 161 */     while (pageContentReader.hasNext()) {
/* 162 */       Instruction instr = pageContentReader.next();
/*     */ 
/*     */       
/* 165 */       String operator = instr.getOperator().asString();
/*     */ 
/*     */       
/* 168 */       OperandStack operands = instr.getOperands();
/*     */       
/* 170 */       if (verbose) {
/* 171 */         System.out.println("Instruction: " + instr.toString());
/*     */       }
/*     */ 
/*     */       
/* 175 */       if (operator.equals("TJ")) {
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 180 */         if (!countedCurrentLine) {
/* 181 */           numLines++;
/*     */           
/* 183 */           if (verbose) {
/* 184 */             System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
/*     */           }
/*     */           
/* 187 */           countedCurrentLine = true;
/*     */ 
/*     */           
/* 190 */           if (lastLineSpace < 0.0D) {
/* 191 */             throw new Exception("Error: Internal error - lastLineSpace is -ve.");
/*     */           }
/* 193 */           lineSpaces.add(new Double(lastLineSpace));
/* 194 */           lastLineSpace = 0.0D;
/*     */         } 
/*     */         continue;
/*     */       } 
/* 198 */       if (operator.equals("Tj")) {
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 203 */         if (!countedCurrentLine) {
/*     */           
/* 205 */           String line = operands.popString().asString();
/* 206 */           operands.pushString(new ASString(line));
/*     */ 
/*     */ 
/*     */           
/* 210 */           if (line.trim().length() > 0) {
/* 211 */             numLines++;
/*     */             
/* 213 */             if (verbose) {
/* 214 */               System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
/*     */             }
/*     */ 
/*     */             
/* 218 */             if (lastLineSpace < 0.0D) {
/* 219 */               throw new Exception("Error: Internal error - lastLineSpace is -ve.");
/*     */             }
/* 221 */             lineSpaces.add(new Double(lastLineSpace));
/* 222 */             lastLineSpace = 0.0D;
/*     */           } 
/*     */ 
/*     */           
/* 226 */           countedCurrentLine = true;
/*     */         } 
/*     */         continue;
/*     */       } 
/* 230 */       if (operator.equals("'")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 256 */         lastLineSpace += lp * yScaling;
/* 257 */         ty -= lp * yScaling;
/* 258 */         countedCurrentLine = false;
/*     */         
/* 260 */         numLines++;
/*     */         
/* 262 */         if (verbose) {
/* 263 */           System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
/*     */         }
/*     */         
/* 266 */         countedCurrentLine = true;
/*     */ 
/*     */         
/* 269 */         if (lastLineSpace < 0.0D) {
/* 270 */           throw new Exception("Error: Internal error - lastLineSpace is -ve.");
/*     */         }
/* 272 */         lineSpaces.add(new Double(lastLineSpace));
/* 273 */         lastLineSpace = 0.0D;
/*     */         continue;
/*     */       } 
/* 276 */       if (operator.equals("\"")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 305 */         lastLineSpace += lp * yScaling;
/* 306 */         ty -= lp * yScaling;
/* 307 */         countedCurrentLine = false;
/*     */         
/* 309 */         numLines++;
/*     */         
/* 311 */         if (verbose) {
/* 312 */           System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
/*     */         }
/*     */         
/* 315 */         countedCurrentLine = true;
/*     */ 
/*     */         
/* 318 */         if (lastLineSpace < 0.0D) {
/* 319 */           throw new Exception("Error: Internal error - lastLineSpace is -ve.");
/*     */         }
/* 321 */         lineSpaces.add(new Double(lastLineSpace));
/* 322 */         lastLineSpace = 0.0D;
/*     */         continue;
/*     */       } 
/* 325 */       if (operator.equals("Td")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 337 */         double newTy = operands.popNumber().doubleValue();
/* 338 */         double newTx = operands.popNumber().doubleValue();
/*     */         
/* 340 */         operands.pushASNumber(new ASNumber(newTx));
/* 341 */         operands.pushASNumber(new ASNumber(newTy));
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 356 */         if (ty + newTy < bottom || ty + newTy > top) {
/* 357 */           throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
/*     */         }
/*     */         
/* 360 */         if (newTy != 0.0D)
/*     */         {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 369 */           countedCurrentLine = false;
/*     */         }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 382 */         lastLineSpace -= newTy * yScaling;
/* 383 */         ty += newTy * yScaling; continue;
/*     */       } 
/* 385 */       if (operator.equals("TD")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 395 */         double newTy = operands.popNumber().doubleValue();
/* 396 */         double newTx = operands.popNumber().doubleValue();
/*     */         
/* 398 */         operands.pushASNumber(new ASNumber(newTx));
/* 399 */         operands.pushASNumber(new ASNumber(newTy));
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 414 */         if (newTy != 0.0D)
/*     */         {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 423 */           countedCurrentLine = false;
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 428 */         lp = -newTy;
/*     */         
/* 430 */         lastLineSpace -= newTy * yScaling;
/* 431 */         ty += newTy * yScaling;
/*     */         
/* 433 */         if (ty < bottom || ty > top)
/* 434 */           throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox."); 
/*     */         continue;
/*     */       } 
/* 437 */       if (operator.equals("T*")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 462 */         lastLineSpace += lp * yScaling;
/* 463 */         ty -= lp * yScaling;
/*     */         
/* 465 */         countedCurrentLine = false; continue;
/*     */       } 
/* 467 */       if (operator.equals("Tm")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 485 */         double f = operands.popNumber().doubleValue();
/* 486 */         double e = operands.popNumber().doubleValue();
/* 487 */         double d = operands.popNumber().doubleValue();
/* 488 */         double c = operands.popNumber().doubleValue();
/* 489 */         double b = operands.popNumber().doubleValue();
/* 490 */         double a = operands.popNumber().doubleValue();
/*     */         
/* 492 */         operands.pushASNumber(new ASNumber(a));
/* 493 */         operands.pushASNumber(new ASNumber(b));
/* 494 */         operands.pushASNumber(new ASNumber(c));
/* 495 */         operands.pushASNumber(new ASNumber(d));
/* 496 */         operands.pushASNumber(new ASNumber(e));
/* 497 */         operands.pushASNumber(new ASNumber(f));
/*     */         
/* 499 */         if (b != 0.0D || c != 0.0D) {
/* 500 */           throw new Exception("Error: Rotation/shearing found! Can't handle this type of PDF.");
/*     */         }
/*     */         
/* 503 */         if (a < 0.0D || d < 0.0D) {
/* 504 */           throw new Exception("Error: x or y scaling is -ve. Not handling this case yet.");
/*     */         }
/*     */         
/* 507 */         if (f < bottom || f > top) {
/* 508 */           throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
/*     */         }
/*     */         
/* 511 */         if (firstTmOp) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 524 */           ty = f;
/* 525 */           xScaling = a;
/* 526 */           yScaling = d;
/*     */           
/* 528 */           firstTmOp = false;
/*     */ 
/*     */ 
/*     */           
/*     */           continue;
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 537 */         if (f > ty) {
/* 538 */           throw new Exception("Error: Page is being rendered upwards! Can't handle this type of PDF.");
/*     */         }
/* 540 */         if (f != ty) {
/*     */ 
/*     */ 
/*     */           
/* 544 */           countedCurrentLine = false;
/*     */           
/* 546 */           lastLineSpace = ty - f;
/*     */           
/* 548 */           ty = f;
/* 549 */           xScaling = a;
/* 550 */           yScaling = d;
/*     */         } 
/*     */         continue;
/*     */       } 
/* 554 */       if (operator.equals("TL")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 562 */         lp = operands.popNumber().doubleValue();
/*     */         
/* 564 */         operands.pushASNumber(new ASNumber(lp));
/*     */         
/* 566 */         if (lp < 0.0D)
/* 567 */           throw new Exception("Error: 'leading value' in TL can only be positive"); 
/*     */         continue;
/*     */       } 
/* 570 */       if ("re".equals(operator) || 
/* 571 */         "l".equals(operator) || 
/* 572 */         "c".equals(operator) || 
/* 573 */         "v".equals(operator) || 
/* 574 */         "y".equals(operator) || 
/* 575 */         "h".equals(operator) || 
/* 576 */         "m".equals(operator))
/*     */       {
/* 578 */         throw new Exception("Error: Sorry, I am not yet taught how to handle PDF with 'Path Construction'! Call my master.");
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 586 */     return lineSpaces;
/*     */   }
/*     */ }


/* Location:              /Users/tysonbowman/Downloads/InvisibleWatermarking/InvisibleWatermark/install/invisible-watermark-bundle-1.0-SNAPSHOT.jar!/invisible-watermark-core-1.0.0.jar!/com/adobe/livecycle/sample/watermark/api/DecodeWatermark.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */