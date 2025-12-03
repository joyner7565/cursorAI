package com.adobe.livecycle.watermark.api;

import com.adobe.internal.pdftoolkit.core.types.ASNumber;
import com.adobe.internal.pdftoolkit.core.types.ASString;
import com.adobe.internal.pdftoolkit.pdf.document.PDFDocument;
import com.adobe.internal.pdftoolkit.pdf.document.PDFOpenOptions;
import com.adobe.internal.pdftoolkit.pdf.contentmodify.ContentWriter;
import com.adobe.internal.pdftoolkit.pdf.content.InstructionFactory;

public class EncodeWatermark {
    boolean verbose = false;

    public PDFFile encode(PDFFile pdfFile, String watermarkBits) throws Exception {
        if (watermarkBits.isEmpty()) {
            throw new Exception("Error: Empty watermark");
        }
        for (int i = 0; i < watermarkBits.length(); i++) {
            if (watermarkBits.charAt(i) != '0' && watermarkBits.charAt(i) != '1') {
                throw new Exception("Error: This is a bit string; values can only be 0 or 1.");
            }
        }
        int wmBitPos = 0;
        int pageNumber = 0;

        while (pageNumber < pdfFile.getNumPages()) {

            var newPageContentWriter = ContentWriter.newInstance(PDFDocument.newInstance(PDFOpenOptions.newInstance()));

            var pdfPage = pdfFile.getPage(pageNumber);
            var pageContentReader = WatermarkUtils.PDFPageToContentReader(pdfPage);

            var pageCropBox = pdfPage.getCropBox();

            double bottom = pageCropBox.bottom();
            double top = pageCropBox.top();

            int numLinesInPage = 0;
            boolean countedCurrentLine = false;

            double ty = 0.0;
            double xScaling = 1.0, yScaling = 1.0;
            double lp = 0.0;
            boolean firstTmOp = true;

            while (pageContentReader.hasNext()) {
                var instr = pageContentReader.next();

                var operator = instr.getOperator().asString();

                var operands = instr.getOperands();

                if (this.verbose) {
                    System.out.println("Instruction: " + instr);
                }

                if (operator.equals("TJ")) {

                    if (!countedCurrentLine) {
                        numLinesInPage++;

                        if (this.verbose) {
                            System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
                        }

                        countedCurrentLine = true;

                        if (wmBitPos < watermarkBits.length() && 
                            numLinesInPage % 2 == 0) {
                            if (watermarkBits.charAt(wmBitPos) == '1') {
                                newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, 0.5 / yScaling));
                            } else {
                                newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, -0.5 / yScaling));
                            }
                            wmBitPos++;
                        }
                    }

                } else if (operator.equals("Tj")) {

                    if (!countedCurrentLine) {
                        var line = operands.popString().asString();
                        operands.pushString(new ASString(line));

                        if (!line.trim().isEmpty()) {
                            numLinesInPage++;

                            if (this.verbose) {
                                System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
                            }

                            if (wmBitPos < watermarkBits.length() && 
                                numLinesInPage % 2 == 0) {
                                if (watermarkBits.charAt(wmBitPos) == '1') {
                                    newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, 0.5 / yScaling));
                                } else {
                                    newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, -0.5 / yScaling));
                                }
                                wmBitPos++;
                            }
                        }

                        countedCurrentLine = true;
                    }

                } else if (operator.equals("'")) {

                    ty -= lp * yScaling;
                    countedCurrentLine = false;

                    numLinesInPage++;

                    if (this.verbose) {
                        System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
                    }

                    countedCurrentLine = true;

                    if (wmBitPos < watermarkBits.length() && 
                        numLinesInPage % 2 == 0) {
                        if (watermarkBits.charAt(wmBitPos) == '1') {
                            newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, 0.5 / yScaling));
                        } else {
                            newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, -0.5 / yScaling));
                        }
                        wmBitPos++;
                    }

                } else if (operator.equals("\"")) {

                    ty -= lp * yScaling;
                    countedCurrentLine = false;

                    numLinesInPage++;

                    if (this.verbose) {
                        System.out.println("*** INCREMENTED NUMLINES TO " + numLinesInPage + " ***");
                    }

                    countedCurrentLine = true;

                    if (wmBitPos < watermarkBits.length() && 
                        numLinesInPage % 2 == 0) {
                        if (watermarkBits.charAt(wmBitPos) == '1') {
                            newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, 0.5 / yScaling));
                        } else {
                            newPageContentWriter.write(InstructionFactory.newTextPosition(0.0, -0.5 / yScaling));
                        }
                        wmBitPos++;
                    }

                } else if (operator.equals("Td")) {

                    double newTy = operands.popNumber().doubleValue();
                    double newTx = operands.popNumber().doubleValue();

                    operands.pushASNumber(new ASNumber(newTx));
                    operands.pushASNumber(new ASNumber(newTy));

                    if (newTy > 0.0) {
                        throw new Exception("Error: Page render not flowing downwards. Can't handle this type of pdf.");
                    }
                    if (ty + newTy < bottom || ty + newTy > top) {
                        throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
                    }

                    if (newTy != 0.0) {
                        countedCurrentLine = false;
                    }

                    ty += newTy * yScaling;
                } else if (operator.equals("TD")) {

                    double newTy = operands.popNumber().doubleValue();
                    double newTx = operands.popNumber().doubleValue();

                    operands.pushASNumber(new ASNumber(newTx));
                    operands.pushASNumber(new ASNumber(newTy));

                    if (newTy > 0.0) {
                        throw new Exception("Error: Page render not flowing downwards. Can't handle this type of pdf.");
                    }

                    if (newTy != 0.0) {
                        countedCurrentLine = false;
                    }

                    lp = -newTy;

                    ty += newTy * yScaling;

                    if (ty < bottom || ty > top) {
                        throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
                    }
                } else if (operator.equals("T*")) {

                    ty -= lp * yScaling;

                    countedCurrentLine = false;
                } else if (operator.equals("Tm")) {

                    double f = operands.popNumber().doubleValue();
                    double e = operands.popNumber().doubleValue();
                    double d = operands.popNumber().doubleValue();
                    double c = operands.popNumber().doubleValue();
                    double b = operands.popNumber().doubleValue();
                    double a = operands.popNumber().doubleValue();

                    operands.pushASNumber(new ASNumber(a));
                    operands.pushASNumber(new ASNumber(b));
                    operands.pushASNumber(new ASNumber(c));
                    operands.pushASNumber(new ASNumber(d));
                    operands.pushASNumber(new ASNumber(e));
                    operands.pushASNumber(new ASNumber(f));

                    if (b != 0.0 || c != 0.0) {
                        throw new Exception("Error: Rotation/shearing found! Can't handle this type of PDF.");
                    }

                    if (a < 0.0 || d < 0.0) {
                        throw new Exception("Error: x or y scaling is -ve. Not handling this case yet.");
                    }

                    if (f < bottom || f > top) {
                        throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
                    }

                    if (firstTmOp) {

                        ty = f;
                        xScaling = a;
                        yScaling = d;

                        firstTmOp = false;

                    } else {

                        if (f > ty) {
                            throw new Exception("Error: Page is being rendered upwards! Can't handle this type of PDF.");
                        }
                        if (f != ty) {

                            countedCurrentLine = false;

                            ty = f;
                            xScaling = a;
                            yScaling = d;
                        }

                    }
                } else if (operator.equals("TL")) {

                    lp = operands.popNumber().doubleValue();

                    operands.pushASNumber(new ASNumber(lp));

                    if (lp < 0.0) {
                        throw new Exception("Error: 'leading value' in TL can only be positive");
                    }
                } else if ("re".equals(operator) || 
                    "l".equals(operator) || 
                    "c".equals(operator) || 
                    "v".equals(operator) || 
                    "y".equals(operator) || 
                    "h".equals(operator) || 
                    "m".equals(operator)) {

                    throw new Exception("Error: Sorry, I am not yet taught how to handle PDF with 'Path Construction'! Call my master.");
                }

                newPageContentWriter.write(instr);
            }

            if (this.verbose) {
                System.out.println("(info: number of visible lines in page " + pageNumber + " is " + numLinesInPage + ")");
            }
            pageNumber++;

            var newPageContent = newPageContentWriter.close();
            pdfFile.setPageContent(pdfPage, newPageContent);
        }

        if (wmBitPos < watermarkBits.length()) {
            throw new Exception("Error: Need more pages before continuing encoding");
        }

        return pdfFile;
    }
}
