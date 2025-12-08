package com.adobe.livecycle.watermark.api;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Utility methods for PDF watermarking with PDFBox
 */
public class WatermarkUtils {
    
    /**
     * Get the content stream tokens from a PDF page
     */
    public static List<Object> getPageTokens(PDPage page) throws IOException {
        PDFStreamParser parser = new PDFStreamParser(page);
        return parser.parse();
    }
    
    /**
     * Write tokens back to a page
     * Requires the PDDocument to create the new stream
     */
    public static void setPageTokens(PDPage page, List<Object> tokens, PDDocument document) throws IOException {
        // Create new content stream using the document
        PDStream newContent = new PDStream(document.getDocument());
        OutputStream out = newContent.createOutputStream(COSName.FLATE_DECODE);
        ContentStreamWriter writer = new ContentStreamWriter(out);
        writer.writeTokens(tokens);
        out.close();
        page.setContents(newContent);
    }
}
