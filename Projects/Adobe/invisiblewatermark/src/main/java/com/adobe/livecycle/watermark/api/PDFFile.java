package com.adobe.livecycle.watermark.api;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * PDFBox-based PDF file wrapper for watermarking operations
 */
public class PDFFile {
    private PDDocument document;
    private String filePath;

    /**
     * Load PDF from file path
     */
    public PDFFile(String inputPDFPath) throws IOException {
        this.filePath = inputPDFPath;
        var file = new File(inputPDFPath);
        this.document = Loader.loadPDF(file);
    }

    /**
     * Load PDF from input stream
     */
    public PDFFile(InputStream inputPDFStream) throws IOException {
        this.document = Loader.loadPDF(inputPDFStream.readAllBytes());
    }

    /**
     * Create from existing PDDocument
     */
    public PDFFile(PDDocument document) {
        this.document = document;
    }

    /**
     * Get the underlying PDDocument
     */
    public PDDocument getDocument() {
        return document;
    }

    /**
     * Close the PDF document
     */
    public void close() throws IOException {
        if (this.document != null) {
            this.document.close();
        }
    }

    /**
     * Get a specific page
     */
    public PDPage getPage(int pageNum) throws Exception {
        if (pageNum < 0 || pageNum >= getNumPages()) {
            throw new Exception("Invalid page number: " + pageNum);
        }
        return this.document.getPage(pageNum);
    }

    /**
     * Save to a new file and close
     */
    public void saveAsAndClose(String outputPDFPath) throws IOException {
        var outputFile = new File(outputPDFPath);
        
        // Create parent directory if needed
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        
        // Save the document
        this.document.save(outputFile);
        this.document.close();
        this.document = null;
    }

    /**
     * Get number of pages
     */
    public int getNumPages() throws Exception {
        return this.document.getNumberOfPages();
    }

    /**
     * Get document title from metadata
     */
    public String getDocTitle() throws Exception {
        PDDocumentInformation info = this.document.getDocumentInformation();
        String title = info.getTitle();
        
        // If no title in metadata, use filename
        if (title == null || title.isEmpty()) {
            if (filePath != null) {
                var file = new File(filePath);
                title = file.getName();
            } else {
                title = "Untitled";
            }
        }
        
        return title;
    }

    /**
     * Get document subject from metadata
     */
    public String getDocSubject() throws Exception {
        PDDocumentInformation info = this.document.getDocumentInformation();
        return info.getSubject();
    }
}
