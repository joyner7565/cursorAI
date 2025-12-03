package com.adobe.livecycle.watermark.api;

import com.adobe.internal.pdftoolkit.core.types.ASNumber;
import com.adobe.internal.pdftoolkit.core.types.ASString;
import com.adobe.internal.pdftoolkit.pdf.graphics.PDFRectangle;
import com.adobe.internal.pdftoolkit.pdf.content.ContentReader;
import java.util.ArrayList;

public class DecodeWatermark {
    boolean verbose = false;

    public String decode(PDFFile orgPdfFile, PDFFile wmPdfFile) throws Exception {
        if (orgPdfFile.getNumPages() != wmPdfFile.getNumPages()) {
            throw new Exception("Error: Different no. of pages in org and watermarked pdf files. File tamper attempted?!");
        }

        var wmDecodedBits = new StringBuilder();

        int pageNumber = 0;

        while (pageNumber < orgPdfFile.getNumPages()) {

            var orgPdfPage = orgPdfFile.getPage(pageNumber);
            var wmPdfPage = wmPdfFile.getPage(pageNumber);

            var orgPageContentReader = WatermarkUtils.PDFPageToContentReader(orgPdfPage);
            var wmPageContentReader = WatermarkUtils.PDFPageToContentReader(wmPdfPage);

            var orgPageCropBox = orgPdfPage.getCropBox();
            var wmPageCropBox = wmPdfPage.getCropBox();

            var orgLineSpaces = computeLineSpacings(orgPageContentReader, orgPageCropBox, this.verbose);
            var wmLineSpaces = computeLineSpacings(wmPageContentReader, wmPageCropBox, this.verbose);

            if (orgLineSpaces.size() != wmLineSpaces.size()) {
                throw new Exception("Error: Different no. of lines in org and watermarked pdf files. File tamper attempted?!");
            }

            var orgSpacesIter = orgLineSpaces.iterator();
            var wmSpacesIter = wmLineSpaces.iterator();

            orgSpacesIter.next();
            wmSpacesIter.next();

            while (orgSpacesIter.hasNext() && wmSpacesIter.hasNext()) {
                var orgLineSpace = orgSpacesIter.next();
                var wmLineSpace = wmSpacesIter.next();

                if (wmLineSpace < orgLineSpace) {

                    wmDecodedBits.append("1");

                    if (orgSpacesIter.hasNext() && wmSpacesIter.hasNext()) {
                        var orgTempLineSpace = orgSpacesIter.next();
                        var wmTempLineSpace = wmSpacesIter.next();

                        if (!wmTempLineSpace.equals(orgTempLineSpace))
                            throw new Exception("Error: Illegal line spacing condition met during decoding. File tamper attempted?!");
                    }
                } else if (wmLineSpace > orgLineSpace) {

                    wmDecodedBits.append("0");

                    if (orgSpacesIter.hasNext() && wmSpacesIter.hasNext()) {
                        var orgTempLineSpace = orgSpacesIter.next();
                        var wmTempLineSpace = wmSpacesIter.next();

                        if (!wmTempLineSpace.equals(orgTempLineSpace)) {
                            throw new Exception("Error: Illegal line spacing condition met during decoding. File tamper attempted?!");
                        }
                    }
                }
            }

            pageNumber++;
        }

        return wmDecodedBits.toString();
    }

    private ArrayList<Double> computeLineSpacings(ContentReader pageContentReader, PDFRectangle pageCropBox, boolean verbose) throws Exception {
        double bottom = pageCropBox.bottom();
        double top = pageCropBox.top();

        int numLines = 0;
        boolean countedCurrentLine = false;

        double ty = 0.0;
        double xScaling = 1.0, yScaling = 1.0;
        double lp = 0.0;
        double lastLineSpace = 0.0;
        var lineSpaces = new ArrayList<Double>();
        boolean firstTmOp = true;

        while (pageContentReader.hasNext()) {
            var instr = pageContentReader.next();

            var operator = instr.getOperator().asString();

            var operands = instr.getOperands();

            if (verbose) {
                System.out.println("Instruction: " + instr);
            }

            if (operator.equals("TJ")) {

                if (!countedCurrentLine) {
                    numLines++;

                    if (verbose) {
                        System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
                    }

                    countedCurrentLine = true;

                    if (lastLineSpace < 0.0) {
                        throw new Exception("Error: Internal error - lastLineSpace is -ve.");
                    }
                    lineSpaces.add(lastLineSpace);
                    lastLineSpace = 0.0;
                }
            } else if (operator.equals("Tj")) {

                if (!countedCurrentLine) {

                    var line = operands.popString().asString();
                    operands.pushString(new ASString(line));

                    if (!line.trim().isEmpty()) {
                        numLines++;

                        if (verbose) {
                            System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
                        }

                        if (lastLineSpace < 0.0) {
                            throw new Exception("Error: Internal error - lastLineSpace is -ve.");
                        }
                        lineSpaces.add(lastLineSpace);
                        lastLineSpace = 0.0;
                    }

                    countedCurrentLine = true;
                }
            } else if (operator.equals("'")) {

                lastLineSpace += lp * yScaling;
                ty -= lp * yScaling;
                countedCurrentLine = false;

                numLines++;

                if (verbose) {
                    System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
                }

                countedCurrentLine = true;

                if (lastLineSpace < 0.0) {
                    throw new Exception("Error: Internal error - lastLineSpace is -ve.");
                }
                lineSpaces.add(lastLineSpace);
                lastLineSpace = 0.0;
            } else if (operator.equals("\"")) {

                lastLineSpace += lp * yScaling;
                ty -= lp * yScaling;
                countedCurrentLine = false;

                numLines++;

                if (verbose) {
                    System.out.println("*** INCREMENTED NUMLINES TO " + numLines + " ***");
                }

                countedCurrentLine = true;

                if (lastLineSpace < 0.0) {
                    throw new Exception("Error: Internal error - lastLineSpace is -ve.");
                }
                lineSpaces.add(lastLineSpace);
                lastLineSpace = 0.0;
            } else if (operator.equals("Td")) {

                double newTy = operands.popNumber().doubleValue();
                double newTx = operands.popNumber().doubleValue();

                operands.pushASNumber(new ASNumber(newTx));
                operands.pushASNumber(new ASNumber(newTy));

                if (ty + newTy < bottom || ty + newTy > top) {
                    throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
                }

                if (newTy != 0.0) {
                    countedCurrentLine = false;
                }

                lastLineSpace -= newTy * yScaling;
                ty += newTy * yScaling;
            } else if (operator.equals("TD")) {

                double newTy = operands.popNumber().doubleValue();
                double newTx = operands.popNumber().doubleValue();

                operands.pushASNumber(new ASNumber(newTx));
                operands.pushASNumber(new ASNumber(newTy));

                if (newTy != 0.0) {
                    countedCurrentLine = false;
                }

                lp = -newTy;

                lastLineSpace -= newTy * yScaling;
                ty += newTy * yScaling;

                if (ty < bottom || ty > top)
                    throw new Exception("Error: It's a crazy world outside CropBox. Please come back inside CropBox.");
            } else if (operator.equals("T*")) {

                lastLineSpace += lp * yScaling;
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

                        lastLineSpace = ty - f;

                        ty = f;
                        xScaling = a;
                        yScaling = d;
                    }
                }
            } else if (operator.equals("TL")) {

                lp = operands.popNumber().doubleValue();

                operands.pushASNumber(new ASNumber(lp));

                if (lp < 0.0)
                    throw new Exception("Error: 'leading value' in TL can only be positive");
            } else if ("re".equals(operator) || 
                "l".equals(operator) || 
                "c".equals(operator) || 
                "v".equals(operator) || 
                "y".equals(operator) || 
                "h".equals(operator) || 
                "m".equals(operator)) {

                throw new Exception("Error: Sorry, I am not yet taught how to handle PDF with 'Path Construction'! Call my master.");
            }
        }

        return lineSpaces;
    }
}
