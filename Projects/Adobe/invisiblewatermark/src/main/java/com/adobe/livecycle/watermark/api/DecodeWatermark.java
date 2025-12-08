package com.adobe.livecycle.watermark.api;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes invisible watermarks from PDF documents using PDFBox
 * Looks for the ±0.5 offset Td operators inserted during encoding
 */
public class DecodeWatermark {
    boolean verbose = false;
    private static final double WATERMARK_OFFSET = 0.5;
    private static final double EPSILON = 0.3; // Tolerance for detecting watermark

    public String decode(PDFFile orgPdfFile, PDFFile wmPdfFile) throws Exception {
        if (orgPdfFile.getNumPages() != wmPdfFile.getNumPages()) {
            throw new Exception("Error: Different number of pages in original and watermarked PDFs!");
        }

        var decodedBits = new StringBuilder();
        int totalPages = orgPdfFile.getNumPages();

        for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
            PDPage wmPage = wmPdfFile.getPage(pageNumber);

            // Extract watermark bits from this page by looking for our Td offsets
            List<Character> pageBits = extractWatermarkBits(wmPage);
            
            for (Character bit : pageBits) {
                decodedBits.append(bit);
            }
            
            if (verbose) {
                System.out.println("Page " + pageNumber + ": decoded " + pageBits.size() + " bits");
            }
        }

        return decodedBits.toString();
    }

    /**
     * Extract watermark bits by scanning for the small Td offsets we added
     * Pattern: COSNumber(0), COSNumber(±0.5), Operator("Td")
     */
    private List<Character> extractWatermarkBits(PDPage wmPage) throws Exception {
        List<Character> bits = new ArrayList<>();
        
        List<Object> wmTokens = WatermarkUtils.getPageTokens(wmPage);
        
        // Scan through tokens looking for watermark Td operators
        for (int i = 0; i < wmTokens.size(); i++) {
            Object token = wmTokens.get(i);
            
            if (token instanceof Operator) {
                Operator op = (Operator) token;
                String opName = op.getName();
                
                // Check if this is a Td operator with watermark pattern
                if (opName.equals("Td") && i >= 2) {
                    Object ty = wmTokens.get(i - 1);
                    Object tx = wmTokens.get(i - 2);
                    
                    if (tx instanceof COSNumber && ty instanceof COSNumber) {
                        float txVal = ((COSNumber) tx).floatValue();
                        float tyVal = ((COSNumber) ty).floatValue();
                        
                        // Check if this looks like a watermark offset (tx=0, ty=±0.5)
                        if (Math.abs(txVal) < EPSILON && 
                            Math.abs(Math.abs(tyVal) - WATERMARK_OFFSET) < EPSILON) {
                            
                            // This is a watermark! Decode the bit
                            if (tyVal > 0) {
                                bits.add('1');
                                if (verbose) {
                                    System.out.println("  Found watermark: ty=" + tyVal + " -> bit=1");
                                }
                            } else {
                                bits.add('0');
                                if (verbose) {
                                    System.out.println("  Found watermark: ty=" + tyVal + " -> bit=0");
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return bits;
    }
}
