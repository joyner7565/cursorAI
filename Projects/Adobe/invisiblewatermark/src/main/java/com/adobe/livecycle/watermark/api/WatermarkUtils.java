/*    */ package com.adobe.livecycle.watermark.api;
/*    */ 
/*    */ import com.adobe.internal.pdftoolkit.pdf.page.PDFPage;
/*    */ import com.adobe.internal.pdftoolkit.pdf.content.Content;
/*    */ import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class WatermarkUtils
/*    */ {
/*    */   public static ContentReader PDFPageToContentReader(PDFPage pdfPage) throws Exception {
/* 23 */     Content content = Content.newInstance(pdfPage);
/* 24 */     ContentReader pageContentReader = ContentReader.newInstance(content);
/* 25 */     return pageContentReader;
/*    */   }
/*    */ }


/* Location:              /Users/tysonbowman/Downloads/InvisibleWatermarking/InvisibleWatermark/install/invisible-watermark-bundle-1.0-SNAPSHOT.jar!/invisible-watermark-core-1.0.0.jar!/com/adobe/livecycle/sample/watermark/api/WatermarkUtils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */