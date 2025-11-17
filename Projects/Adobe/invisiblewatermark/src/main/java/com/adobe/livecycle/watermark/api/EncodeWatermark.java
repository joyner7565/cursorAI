/*     */ package com.adobe.livecycle.watermark.api;
/*     */ 
/*     */ import com.adobe.internal.pdftoolkit.core.types.ASNumber;
/*     */ import com.adobe.internal.pdftoolkit.core.types.ASString;
/*     */ import com.adobe.internal.pdftoolkit.pdf.document.PDFDocument;
/*     */ import com.adobe.internal.pdftoolkit.pdf.document.PDFOpenOptions;
/*     */ import com.adobe.internal.pdftoolkit.pdf.graphics.PDFRectangle;
/*     */ import com.adobe.internal.pdftoolkit.pdf.page.PDFPage;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.Content;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;
/*     */ import com.adobe.internal.pdftoolkit.pdf.contentmodify.ContentWriter;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.Instruction;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.InstructionFactory;
/*     */ import com.adobe.internal.pdftoolkit.pdf.content.OperandStack;
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
/*     */ public class EncodeWatermark
/*     */ {
/*     */   boolean verbose = false;
/*     */   
/*     */   public PDFFile encode(PDFFile pdfFile, String watermarkBits) throws Exception {
/*  44 */     if (watermarkBits.length() == 0) {
/*  45 */       throw new Exception("Error: Empty watermark");
/*     */     }
/*  47 */     for (int i = 0; i < watermarkBits.length(); i++) {
/*  48 */       if (watermarkBits.charAt(i) != '0' && watermarkBits.charAt(i) != '1') {
/*  49 */         throw new Exception("Error: This is a bit string; values can only be 0 or 1.");
/*     */       }
/*     */     } 
/*  52 */     int wmBitPos = 0;
/*  53 */     int pageNumber = 0;
/*     */     
/*  55 */     while (pageNumber < pdfFile.getNumPages()) {
/*     */       
/*  57 */       ContentWriter newPageContentWriter = ContentWriter.newInstance(PDFDocument.newInstance(PDFOpenOptions.newInstance()));
/*     */       
/*  59 */       PDFPage pdfPage = pdfFile.getPage(pageNumber);
/*  60 */       ContentReader pageContentReader = WatermarkUtils.PDFPageToContentReader(pdfPage);
/*     */       
/*  62 */       PDFRectangle pageCropBox = pdfPage.getCropBox();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  68 */       double bottom = pageCropBox.bottom();
/*  69 */       double top = pageCropBox.top();
/*     */       
/*  71 */       int numLinesInPage = 0;
/*  72 */       boolean countedCurrentLine = false;
/*     */       
/*  74 */       double tx = 0.0D, ty = 0.0D;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  83 */       double xScaling = 1.0D, yScaling = 1.0D;
/*     */       
/*  85 */       double lp = 0.0D;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  92 */       double userUnit = 0.013888888888888888D;
/*     */       
/*  94 */       boolean firstTmOp = true;
/*     */       
/*  96 */       while (pageContentReader.hasNext()) {
/*  97 */         Instruction instr = pageContentReader.next();
/*     */ 
/*     */         
/* 100 */         String operator = instr.getOperator().asString();
/*     */ 
/*     */         
/* 103 */         OperandStack operands = instr.getOperands();
/*     */         
/* 105 */         if (this.verbose) {
/* 106 */           System.out.println("Instruction: " + instr.toString());
/*     */         }
/*     */ 
/*     */         
/* 110 */         if (operator.equals("TJ")) {
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 115 */           if (!countedCurrentLine) {
/* 116 */             numLinesInPage++;
/*     */             
/* 118 */             if (this.verbose) {
/* 119 */               System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
/*     */             }
/*     */             
/* 122 */             countedCurrentLine = true;
/*     */ 
/*     */ 
/*     */ 
/*     */             
/* 127 */             if (wmBitPos < watermarkBits.length() && 
/* 128 */               numLinesInPage % 2 == 0) {
/* 129 */               if (watermarkBits.charAt(wmBitPos) == '1') {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/* 138 */                 newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, 0.5D / yScaling));
/*     */ 
/*     */ 
/*     */ 
/*     */               
/*     */               }
/*     */               else {
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/* 149 */                 newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, -0.5D / yScaling));
/*     */               } 
/* 151 */               wmBitPos++;
/*     */             }
/*     */           
/*     */           }
/*     */         
/* 156 */         } else if (operator.equals("Tj")) {
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 161 */           if (!countedCurrentLine)
/*     */           {
/* 163 */             String line = operands.popString().asString();
/* 164 */             operands.pushString(new ASString(line));
/*     */ 
/*     */ 
/*     */             
/* 168 */             if (line.trim().length() > 0) {
/* 169 */               numLinesInPage++;
/*     */               
/* 171 */               if (this.verbose) {
/* 172 */                 System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
/*     */               }
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 178 */               if (wmBitPos < watermarkBits.length() && 
/* 179 */                 numLinesInPage % 2 == 0) {
/* 180 */                 if (watermarkBits.charAt(wmBitPos) == '1') {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */                   
/* 190 */                   newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, 0.5D / yScaling));
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/*     */                 }
/*     */                 else {
/*     */ 
/*     */ 
/*     */ 
/*     */                   
/* 201 */                   newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, -0.5D / yScaling));
/*     */                 } 
/* 203 */                 wmBitPos++;
/*     */               } 
/*     */             } 
/*     */ 
/*     */ 
/*     */             
/* 209 */             countedCurrentLine = true;
/*     */           }
/*     */         
/*     */         }
/* 213 */         else if (operator.equals("'")) {
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
/* 238 */           ty -= lp * yScaling;
/* 239 */           countedCurrentLine = false;
/*     */           
/* 241 */           numLinesInPage++;
/*     */           
/* 243 */           if (this.verbose) {
/* 244 */             System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
/*     */           }
/*     */           
/* 247 */           countedCurrentLine = true;
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 252 */           if (wmBitPos < watermarkBits.length() && 
/* 253 */             numLinesInPage % 2 == 0) {
/* 254 */             if (watermarkBits.charAt(wmBitPos) == '1') {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 263 */               newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, 0.5D / yScaling));
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             }
/*     */             else {
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 274 */               newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, -0.5D / yScaling));
/*     */             } 
/* 276 */             wmBitPos++;
/*     */           
/*     */           }
/*     */         
/*     */         }
/* 281 */         else if (operator.equals("\"")) {
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
/* 310 */           ty -= lp * yScaling;
/* 311 */           countedCurrentLine = false;
/*     */           
/* 313 */           numLinesInPage++;
/*     */           
/* 315 */           if (this.verbose) {
/* 316 */             System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
/*     */           }
/*     */           
/* 319 */           countedCurrentLine = true;
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 324 */           if (wmBitPos < watermarkBits.length() && 
/* 325 */             numLinesInPage % 2 == 0) {
/* 326 */             if (watermarkBits.charAt(wmBitPos) == '1') {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 335 */               newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, 0.5D / yScaling));
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             }
/*     */             else {
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 346 */               newPageContentWriter.write(InstructionFactory.newTextPosition(0.0D, -0.5D / yScaling));
/*     */             } 
/* 348 */             wmBitPos++;
/*     */           
/*     */           }
/*     */         
/*     */         }
/* 353 */         else if (operator.equals("Td")) {
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
/* 365 */           double newTy = operands.popNumber().doubleValue();
/* 366 */           double newTx = operands.popNumber().doubleValue();
/*     */           
/* 368 */           operands.pushASNumber(new ASNumber(newTx));
/* 369 */           operands.pushASNumber(new ASNumber(newTy));
/*     */           
/* 371 */           if (newTy > 0.0D) {
/* 372 */             throw new Exception("Error: Page render not flowing downwards. Can't handle this type of pdf.");
/*     */           }
/* 374 */           if (ty + newTy < bottom || ty + newTy > top) {
/* 375 */             throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
/*     */           }
/*     */           
/* 378 */           if (newTy != 0.0D)
/*     */           {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */             
/* 387 */             countedCurrentLine = false;
/*     */           }
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
/* 400 */           ty += newTy * yScaling;
/*     */         }
/* 402 */         else if (operator.equals("TD")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 412 */           double newTy = operands.popNumber().doubleValue();
/* 413 */           double newTx = operands.popNumber().doubleValue();
/*     */           
/* 415 */           operands.pushASNumber(new ASNumber(newTx));
/* 416 */           operands.pushASNumber(new ASNumber(newTy));
/*     */           
/* 418 */           if (newTy > 0.0D) {
/* 419 */             throw new Exception("Error: Page render not flowing downwards. Can't handle this type of pdf.");
/*     */           }
/*     */           
/* 422 */           if (newTy != 0.0D)
/*     */           {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */             
/* 431 */             countedCurrentLine = false;
/*     */           }
/*     */ 
/*     */ 
/*     */           
/* 436 */           lp = -newTy;
/*     */           
/* 438 */           ty += newTy * yScaling;
/*     */           
/* 440 */           if (ty < bottom || ty > top) {
/* 441 */             throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
/*     */           }
/*     */         }
/* 444 */         else if (operator.equals("T*")) {
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
/* 469 */           ty -= lp * yScaling;
/*     */           
/* 471 */           countedCurrentLine = false;
/*     */         }
/* 473 */         else if (operator.equals("Tm")) {
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
/* 491 */           double f = operands.popNumber().doubleValue();
/* 492 */           double e = operands.popNumber().doubleValue();
/* 493 */           double d = operands.popNumber().doubleValue();
/* 494 */           double c = operands.popNumber().doubleValue();
/* 495 */           double b = operands.popNumber().doubleValue();
/* 496 */           double a = operands.popNumber().doubleValue();
/*     */           
/* 498 */           operands.pushASNumber(new ASNumber(a));
/* 499 */           operands.pushASNumber(new ASNumber(b));
/* 500 */           operands.pushASNumber(new ASNumber(c));
/* 501 */           operands.pushASNumber(new ASNumber(d));
/* 502 */           operands.pushASNumber(new ASNumber(e));
/* 503 */           operands.pushASNumber(new ASNumber(f));
/*     */           
/* 505 */           if (b != 0.0D || c != 0.0D) {
/* 506 */             throw new Exception("Error: Rotation/shearing found! Can't handle this type of PDF.");
/*     */           }
/*     */           
/* 509 */           if (a < 0.0D || d < 0.0D) {
/* 510 */             throw new Exception("Error: x or y scaling is -ve. Not handling this case yet.");
/*     */           }
/*     */           
/* 513 */           if (f < bottom || f > top) {
/* 514 */             throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
/*     */           }
/*     */           
/* 517 */           if (firstTmOp) {
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
/* 530 */             ty = f;
/* 531 */             xScaling = a;
/* 532 */             yScaling = d;
/*     */             
/* 534 */             firstTmOp = false;
/*     */ 
/*     */ 
/*     */           
/*     */           }
/*     */           else {
/*     */ 
/*     */ 
/*     */             
/* 543 */             if (f > ty) {
/* 544 */               throw new Exception("Error: Page is being rendered upwards! Can't handle this type of PDF.");
/*     */             }
/* 546 */             if (f != ty)
/*     */             {
/*     */ 
/*     */               
/* 550 */               countedCurrentLine = false;
/*     */               
/* 552 */               ty = f;
/* 553 */               xScaling = a;
/* 554 */               yScaling = d;
/*     */             }
/*     */           
/*     */           } 
/* 558 */         } else if (operator.equals("TL")) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 566 */           lp = operands.popNumber().doubleValue();
/*     */           
/* 568 */           operands.pushASNumber(new ASNumber(lp));
/*     */           
/* 570 */           if (lp < 0.0D) {
/* 571 */             throw new Exception("Error: 'leading value' in TL can only be positive");
/*     */           }
/*     */         }
/* 574 */         else if ("re".equals(operator) || 
/* 575 */           "l".equals(operator) || 
/* 576 */           "c".equals(operator) || 
/* 577 */           "v".equals(operator) || 
/* 578 */           "y".equals(operator) || 
/* 579 */           "h".equals(operator) || 
/* 580 */           "m".equals(operator)) {
/*     */           
/* 582 */           throw new Exception("Error: Sorry, I am not yet taught how to handle PDF with 'Path Construction'! Call my master.");
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 587 */         newPageContentWriter.write(instr);
/*     */       } 
/*     */       
/* 590 */       if (this.verbose) {
/* 591 */         System.out.println("(info: number of visible lines in page " + pageNumber + " is " + numLinesInPage + ")");
/*     */       }
/* 593 */       pageNumber++;
/*     */       
/* 595 */       Content newPageContent = newPageContentWriter.close();
/* 596 */       pdfFile.setPageContent(pdfPage, newPageContent);
/*     */     } 
/*     */ 
/*     */     
/* 600 */     if (wmBitPos < watermarkBits.length()) {
/* 601 */       throw new Exception("Error: Need more pages before continuing encoding");
/*     */     }
/*     */     
/* 604 */     return pdfFile;
/*     */   }
/*     */ }


/* Location:              /Users/tysonbowman/Downloads/InvisibleWatermarking/InvisibleWatermark/install/invisible-watermark-bundle-1.0-SNAPSHOT.jar!/invisible-watermark-core-1.0.0.jar!/com/adobe/livecycle/sample/watermark/api/EncodeWatermark.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */