/*     */ package com.adobe.livecycle.watermark.api;
/*     */ 
/*     */ import com.adobe.internal.io.ByteArrayByteWriter;
/*     */ import com.adobe.internal.io.ByteReader;
/*     */ import com.adobe.internal.io.ByteWriter;
/*     */ import com.adobe.internal.io.InputStreamByteReader;
/*     */ import com.adobe.internal.io.RandomAccessFileByteWriter;
/*.    */ import com.adobe.internal.pdftoolkit.pdf.content.Content;
import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;
/*     */ import com.adobe.internal.pdftoolkit.pdf.document.PDFDocument;
/*     */ import com.adobe.internal.pdftoolkit.pdf.document.PDFOpenOptions;
/*     */ import com.adobe.internal.pdftoolkit.pdf.document.PDFSaveIncrementalOptions;
/*     */ import com.adobe.internal.pdftoolkit.pdf.document.PDFSaveOptions;
/*     */ import com.adobe.internal.pdftoolkit.pdf.page.PDFPage;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.InputStream;
/*     */ import java.io.RandomAccessFile;
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
/*     */ public class PDFFile
/*     */ {
/*  37 */   private PDFDocument pdfDoc = null;
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
/*     */   public PDFFile(String inputPDFPath) throws Exception {
/*  52 */     File file = new File(inputPDFPath);
/*  53 */     FileInputStream fis = new FileInputStream(file);
/*  54 */     InputStreamByteReader inputStreamByteReader = new InputStreamByteReader(fis);
/*     */     
/*  56 */     this.pdfDoc = PDFDocument.newInstance((ByteReader)inputStreamByteReader, PDFOpenOptions.newInstance());
/*     */     
/*  58 */     inputStreamByteReader.close();
/*  59 */     inputStreamByteReader = null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public PDFFile(InputStream inputPDFStream) throws Exception {
/*  70 */     InputStreamByteReader inputStreamByteReader = new InputStreamByteReader(inputPDFStream);
/*     */     
/*  72 */     this.pdfDoc = PDFDocument.newInstance((ByteReader)inputStreamByteReader, PDFOpenOptions.newInstance());
/*  73 */     inputStreamByteReader.close();
/*  74 */     inputStreamByteReader = null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void close() throws Exception {
/*  84 */     if (this.pdfDoc != null) {
/*  85 */       this.pdfDoc.close();
/*     */     }
/*     */   }
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
/*     */   public PDFPage getPage(int pageNum) throws Exception {
/* 102 */     return this.pdfDoc.requirePages().getPage(pageNum);
/*     */   }
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
/*     */   public void setPageContent(int pageNum, Content newPageContent) throws Exception {
/* 118 */     getPage(pageNum).setContents(newPageContent.getContentStream());
/*     */   }
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
/*     */   public void setPageContent(PDFPage pdfPage, Content newPageContent) throws Exception {
/* 135 */     pdfPage.setContents(newPageContent.getContentStream());
/*     */   }
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
/*     */   public ContentReader getPageContentReader(int pageNum) throws Exception {
/* 151 */     Content content = Content.newInstance(getPage(pageNum));
/* 152 */     return ContentReader.newInstance(content);
/*     */   }
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
/*     */   public void saveAsAndClose(String outputPDFPath) throws Exception {
/* 175 */     File outputFile = new File(outputPDFPath);
/* 176 */     if (outputFile.exists()) {
/* 177 */       outputFile.delete();
/*     */     } else {
/*     */       
/* 180 */       File parentDir = new File(outputFile.getParent());
/* 181 */       parentDir.mkdir();
/*     */     } 
/*     */     
/* 184 */     RandomAccessFile outputPdfFile = new RandomAccessFile(outputPDFPath, "rw");
/* 185 */     RandomAccessFileByteWriter randomAccessFileByteWriter = new RandomAccessFileByteWriter(outputPdfFile);
/*     */     
/* 187 */     this.pdfDoc.saveAndClose((ByteWriter)randomAccessFileByteWriter, (PDFSaveOptions)PDFSaveIncrementalOptions.newInstance());
/*     */     
/* 189 */     this.pdfDoc.close();
/* 190 */     this.pdfDoc = null;
/*     */   }
/*     */ 
/*     */   
/*     */   public byte[] getFileContentsAndClose() throws Exception {
/* 195 */     ByteArrayByteWriter test = new ByteArrayByteWriter();
/*     */     
/* 197 */     this.pdfDoc.save((ByteWriter)test, (PDFSaveOptions)PDFSaveIncrementalOptions.newInstance());
/* 198 */     this.pdfDoc.close();
/* 199 */     this.pdfDoc = null;
/* 200 */     return test.toByteArray();
/*     */   }
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
/*     */   public int getNumRevisions() {
/* 213 */     return this.pdfDoc.getNumRevisions();
/*     */   }
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
/*     */   public String getDocTitle() throws Exception {
/* 226 */     return this.pdfDoc.getDocumentInfo().getTitle();
/*     */   }
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
/*     */   public String getDocSubject() throws Exception {
/* 239 */     return this.pdfDoc.getDocumentInfo().getSubject();
/*     */   }
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
/*     */   public int getNumPages() throws Exception {
/* 252 */     return this.pdfDoc.requirePages().getNumPages();
/*     */   }
/*     */ }


/* Location:              /Users/tysonbowman/Downloads/InvisibleWatermarking/InvisibleWatermark/install/invisible-watermark-bundle-1.0-SNAPSHOT.jar!/invisible-watermark-core-1.0.0.jar!/com/adobe/livecycle/sample/watermark/api/PDFFile.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */