package com.adobe.livecycle.watermark.api;

import com.adobe.internal.pdftoolkit.pdf.page.PDFPage;
import com.adobe.internal.pdftoolkit.pdf.content.Content;
import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;

public class WatermarkUtils {
    public static ContentReader PDFPageToContentReader(PDFPage pdfPage) throws Exception {
        var content = Content.newInstance(pdfPage);
        return ContentReader.newInstance(content);
    }
}
