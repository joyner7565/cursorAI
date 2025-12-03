package com.adobe.livecycle.watermark.api;

import com.adobe.internal.io.ByteArrayByteWriter;
import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.internal.io.RandomAccessFileByteWriter;
import com.adobe.internal.pdftoolkit.pdf.content.Content;
import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;
import com.adobe.internal.pdftoolkit.pdf.document.PDFDocument;
import com.adobe.internal.pdftoolkit.pdf.document.PDFOpenOptions;
import com.adobe.internal.pdftoolkit.pdf.document.PDFSaveIncrementalOptions;
import com.adobe.internal.pdftoolkit.pdf.document.PDFSaveOptions;
import com.adobe.internal.pdftoolkit.pdf.page.PDFPage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PDFFile {
    private PDFDocument pdfDoc = null;

    public PDFFile(String inputPDFPath) throws Exception {
        var file = new File(inputPDFPath);
        var fis = new FileInputStream(file);
        var inputStreamByteReader = new InputStreamByteReader(fis);
        
        this.pdfDoc = PDFDocument.newInstance((ByteReader)inputStreamByteReader, PDFOpenOptions.newInstance());
        
        inputStreamByteReader.close();
        fis.close();
    }

    public PDFFile(InputStream inputPDFStream) throws Exception {
        var inputStreamByteReader = new InputStreamByteReader(inputPDFStream);
        
        this.pdfDoc = PDFDocument.newInstance((ByteReader)inputStreamByteReader, PDFOpenOptions.newInstance());
        inputStreamByteReader.close();
    }

    public void close() throws Exception {
        if (this.pdfDoc != null) {
            this.pdfDoc.close();
        }
    }

    public PDFPage getPage(int pageNum) throws Exception {
        return this.pdfDoc.requirePages().getPage(pageNum);
    }

    public void setPageContent(int pageNum, Content newPageContent) throws Exception {
        getPage(pageNum).setContents(newPageContent.getContentStream());
    }

    public void setPageContent(PDFPage pdfPage, Content newPageContent) throws Exception {
        pdfPage.setContents(newPageContent.getContentStream());
    }

    public ContentReader getPageContentReader(int pageNum) throws Exception {
        var content = Content.newInstance(getPage(pageNum));
        return ContentReader.newInstance(content);
    }

    public void saveAsAndClose(String outputPDFPath) throws Exception {
        var outputFile = new File(outputPDFPath);
        if (outputFile.exists()) {
            outputFile.delete();
        } else {
            var parentDir = new File(outputFile.getParent());
            parentDir.mkdir();
        }

        var outputPdfFile = new RandomAccessFile(outputPDFPath, "rw");
        var randomAccessFileByteWriter = new RandomAccessFileByteWriter(outputPdfFile);
        
        this.pdfDoc.saveAndClose((ByteWriter)randomAccessFileByteWriter, (PDFSaveOptions)PDFSaveIncrementalOptions.newInstance());
        
        randomAccessFileByteWriter.close();
        outputPdfFile.close();
        
        this.pdfDoc.close();
        this.pdfDoc = null;
    }

    public byte[] getFileContentsAndClose() throws Exception {
        var byteWriter = new ByteArrayByteWriter();

        this.pdfDoc.save((ByteWriter)byteWriter, (PDFSaveOptions)PDFSaveIncrementalOptions.newInstance());
        this.pdfDoc.close();
        this.pdfDoc = null;
        return byteWriter.toByteArray();
    }

    public int getNumRevisions() {
        return this.pdfDoc.getNumRevisions();
    }

    public String getDocTitle() throws Exception {
        return this.pdfDoc.getDocumentInfo().getTitle();
    }

    public String getDocSubject() throws Exception {
        return this.pdfDoc.getDocumentInfo().getSubject();
    }

    public int getNumPages() throws Exception {
        return this.pdfDoc.requirePages().getNumPages();
    }
}
