package com.adobe.livecycle.watermark.api;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes invisible watermarks in PDF documents using PDFBox
 * Uses line spacing manipulation to encode binary data
 */
public class EncodeWatermark {
    boolean verbose = false;
    private static final double WATERMARK_OFFSET = 0.5; // Points to shift text

    public PDFFile encode(PDFFile pdfFile, String watermarkBits) throws Exception {
        if (watermarkBits.isEmpty()) {
            throw new Exception("Error: Empty watermark");
        }
        
        // Validate watermark is binary
        for (int i = 0; i < watermarkBits.length(); i++) {
            if (watermarkBits.charAt(i) != '0' && watermarkBits.charAt(i) != '1') {
                throw new Exception("Error: This is a bit string; values can only be 0 or 1.");
            }
        }

        int wmBitPos = 0;
        int totalPages = pdfFile.getNumPages();

        for (int pageNumber = 0; pageNumber < totalPages && wmBitPos < watermarkBits.length(); pageNumber++) {
            PDPage page = pdfFile.getPage(pageNumber);
            
            // Parse content stream tokens
            List<Object> tokens = WatermarkUtils.getPageTokens(page);
            List<Object> newTokens = new ArrayList<>();
            
            int lineCount = 0;
            boolean justCountedLine = false;
            
            for (int i = 0; i < tokens.size(); i++) {
                Object token = tokens.get(i);
                newTokens.add(token);
                
                // Detect text showing operators
                if (token instanceof Operator) {
                    Operator op = (Operator) token;
                    String opName = op.getName();
                    
                    // Text showing operators that indicate a line of text
                    if (isTextShowingOperator(opName)) {
                        if (!justCountedLine) {
                            lineCount++;
                            justCountedLine = true;
                            
                            if (verbose) {
                                System.out.println("Line " + lineCount + " on page " + pageNumber);
                            }
                            
                            // Encode watermark on every other line
                            if (wmBitPos < watermarkBits.length() && lineCount % 2 == 0) {
                                char bit = watermarkBits.charAt(wmBitPos);
                                double offset = (bit == '1') ? WATERMARK_OFFSET : -WATERMARK_OFFSET;
                                
                                // Insert a text positioning operator to shift the line
                                newTokens.add(COSInteger.get(0));
                                newTokens.add(new COSFloat((float) offset));
                                newTokens.add(Operator.getOperator("Td"));
                                
                                wmBitPos++;
                                
                                if (verbose) {
                                    System.out.println("  Encoded bit " + bit + " (offset: " + offset + ")");
                                }
                            }
                        }
                    }
                    // Text positioning operators reset the line counter
                    else if (isTextPositioningOperator(opName)) {
                        justCountedLine = false;
                    }
                }
            }
            
            // Write modified tokens back to page
            WatermarkUtils.setPageTokens(page, newTokens, pdfFile.getDocument());
            
            if (verbose) {
                System.out.println("Page " + pageNumber + ": " + lineCount + " lines, encoded " + wmBitPos + " bits so far");
            }
        }

        if (wmBitPos < watermarkBits.length()) {
            throw new Exception("Error: Need more pages to encode all watermark bits. Encoded " + 
                              wmBitPos + " of " + watermarkBits.length() + " bits.");
        }

        return pdfFile;
    }
    
    private boolean isTextShowingOperator(String opName) {
        return opName.equals("Tj") || opName.equals("TJ") || 
               opName.equals("'") || opName.equals("\"");
    }
    
    private boolean isTextPositioningOperator(String opName) {
        return opName.equals("Td") || opName.equals("TD") || 
               opName.equals("T*") || opName.equals("Tm");
    }
}
