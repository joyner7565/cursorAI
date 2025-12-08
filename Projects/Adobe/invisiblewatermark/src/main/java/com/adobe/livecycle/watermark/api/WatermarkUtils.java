package com.adobe.livecycle.watermark.api;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.IOException;
import java.util.List;

/**
 * Utility methods for PDF watermarking with PDFBox
 */
public class WatermarkUtils {
    
    /**
     * Get the content stream tokens from a PDF page
     */
    public static List<Object> getPageTokens(PDPage page) throws IOException {
        var parser = new org.apache.pdfbox.contentstream.PDFStreamParser(page);
        parser.parse();
        return parser.getTokens();
    }
    
    /**
     * Write tokens back to a page
     */
    public static void setPageTokens(PDPage page, List<Object> tokens) throws IOException {
        var stream = new PDStream(page.getCOSObject().getCOSObject());
        var output = stream.createOutputStream();
        var writer = new org.apache.pdfbox.contentstream.PDFStreamWriter(output);
        
        for (Object token : tokens) {
            if (token instanceof Operator) {
                writer.writeToken((Operator) token);
            } else if (token instanceof COSBase) {
                writer.writeToken((COSBase) token);
            }
        }
        
        writer.close();
        output.close();
        
        page.setContents(stream);
    }
}
