package com.adobe.livecycle.watermark.api;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes invisible watermarks from PDF documents using PDFBox
 * Compares original and watermarked PDFs to extract embedded binary data
 */
public class DecodeWatermark {
    boolean verbose = false;
    private static final double EPSILON = 0.1; // Tolerance for float comparison

    public String decode(PDFFile orgPdfFile, PDFFile wmPdfFile) throws Exception {
        if (orgPdfFile.getNumPages() != wmPdfFile.getNumPages()) {
            throw new Exception("Error: Different number of pages in original and watermarked PDFs!");
        }

        var decodedBits = new StringBuilder();
        int totalPages = orgPdfFile.getNumPages();

        for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
            PDPage orgPage = orgPdfFile.getPage(pageNumber);
            PDPage wmPage = wmPdfFile.getPage(pageNumber);

            // Get line spacings from both documents
            List<Double> orgSpacings = extractLineSpacings(orgPage);
            List<Double> wmSpacings = extractLineSpacings(wmPage);

            if (orgSpacings.size() != wmSpacings.size()) {
                throw new Exception("Error: Different number of lines on page " + pageNumber + 
                                  " (original: " + orgSpacings.size() + 
                                  ", watermarked: " + wmSpacings.size() + ")");
            }

            // Compare spacings to decode bits
            for (int i = 0; i < orgSpacings.size() && i < wmSpacings.size(); i++) {
                double orgSpacing = orgSpacings.get(i);
                double wmSpacing = wmSpacings.get(i);
                
                double difference = wmSpacing - orgSpacing;
                
                if (Math.abs(difference) > EPSILON) {
                    // Detected a watermark bit
                    if (difference > 0) {
                        decodedBits.append('1');
                    } else {
                        decodedBits.append('0');
                    }
                    
                    if (verbose) {
                        System.out.println("Page " + pageNumber + ", line " + i + 
                                         ": difference = " + difference + 
                                         ", bit = " + decodedBits.charAt(decodedBits.length() - 1));
                    }
                }
            }
        }

        return decodedBits.toString();
    }

    /**
     * Extract line spacings from a page's content stream
     */
    private List<Double> extractLineSpacings(PDPage page) throws IOException {
        List<Double> spacings = new ArrayList<>();
        List<Object> tokens = WatermarkUtils.getPageTokens(page);
        
        double currentY = 0;
        double lastY = 0;
        int lineCount = 0;
        
        for (int i = 0; i < tokens.size(); i++) {
            Object token = tokens.get(i);
            
            if (token instanceof Operator) {
                Operator op = (Operator) token;
                String opName = op.getName();
                
                // Track text positioning
                if (opName.equals("Td") || opName.equals("TD")) {
                    // Td/TD: tx ty - Move text position
                    if (i >= 2 && tokens.get(i-1) instanceof COSNumber && tokens.get(i-2) instanceof COSNumber) {
                        double ty = ((COSNumber) tokens.get(i-1)).doubleValue();
                        currentY += ty;
                        
                        double spacing = Math.abs(currentY - lastY);
                        spacings.add(spacing);
                        lastY = currentY;
                    }
                }
                else if (opName.equals("Tm")) {
                    // Tm: a b c d e f - Set text matrix
                    if (i >= 6 && tokens.get(i-1) instanceof COSNumber) {
                        double f = ((COSNumber) tokens.get(i-1)).doubleValue();
                        currentY = f;
                        
                        if (lineCount > 0) {
                            double spacing = Math.abs(currentY - lastY);
                            spacings.add(spacing);
                        }
                        lastY = currentY;
                        lineCount++;
                    }
                }
                else if (isTextShowingOperator(opName)) {
                    lineCount++;
                }
            }
        }
        
        return spacings;
    }
    
    private boolean isTextShowingOperator(String opName) {
        return opName.equals("Tj") || opName.equals("TJ") || 
               opName.equals("'") || opName.equals("\"");
    }
}
