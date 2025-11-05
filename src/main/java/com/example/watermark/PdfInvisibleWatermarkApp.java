package com.example.watermark;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.PDResources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Command line utility that embeds an "invisible" micro-dot watermark into a PDF document.
 * <p>
 * The watermark is rendered as a dense grid of sub-millimeter squares with very low opacity.
 * When the document is printed, the dots are virtually imperceptible to the naked eye, yet they
 * can be recovered by scanning the page at high resolution and thresholding for the grid positions.
 * </p>
 * <p>
 * Encoding strategy:
 * <ul>
 *     <li>The payload is the UTF-8 watermark message concatenated with a CRC32 checksum.</li>
 *     <li>The payload is repeated twice to make the grid robust against surface damage or clipping.</li>
 *     <li>Each bit is represented by the presence (1) or absence (0) of a semi-transparent dot at a grid cell.</li>
 *     <li>Four bright anchor dots are placed at the corners of the grid to simplify alignment during decoding.</li>
 * </ul>
 * </p>
 * <p>
 * Usage:
 * <pre>
 *     java -jar pdf-invisible-watermark.jar input.pdf output.pdf "Confidential Batch 1701"
 * </pre>
 * </p>
 */
public final class PdfInvisibleWatermarkApp {

    /** Default grid margin in points (1/72 inch). */
    private static final float GRID_MARGIN = 36f; // 0.5 inch
    /** Default spacing between grid cells in points. */
    private static final float GRID_SPACING = 12f; // ~1/6 inch (~4.2 mm)
    /** Size of each watermark dot in points. */
    private static final float DOT_SIZE = 1.5f;
    /** Opacity of data dots (0 = invisible, 1 = opaque). */
    private static final float DATA_ALPHA = 0.08f;
    /** Opacity of fiducial anchor dots. */
    private static final float FIDUCIAL_ALPHA = 0.25f;

    private PdfInvisibleWatermarkApp() {
        // Utility class
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar pdf-invisible-watermark.jar <input.pdf> <output.pdf> <watermark message>");
            System.exit(1);
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        String watermarkMessage = args[2];

        if (!Files.isReadable(input)) {
            System.err.printf("Input file '%s' is not readable.%n", input);
            System.exit(2);
        }

        try {
            new PdfInvisibleWatermarkApp().applyWatermark(input, output, watermarkMessage);
            System.out.printf("Watermarked PDF written to %s%n", output.toAbsolutePath());
        } catch (IOException e) {
            System.err.printf("Failed to watermark PDF: %s%n", e.getMessage());
            System.exit(3);
        }
    }

    private void applyWatermark(Path input, Path output, String watermarkMessage) throws IOException {
        byte[] payload = buildPayload(watermarkMessage);

        try (PDDocument document = PDDocument.load(input.toFile())) {
            for (PDPage page : document.getPages()) {
                embedPayload(document, page, payload);
            }

            Files.createDirectories(output.getParent() == null ? Paths.get(".") : output.getParent());
            document.save(output.toFile());
        }
    }

    private byte[] buildPayload(String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        CRC32 crc = new CRC32();
        crc.update(messageBytes);
        long checksum = crc.getValue();

        byte[] checksumBytes = new byte[4];
        checksumBytes[0] = (byte) ((checksum >> 24) & 0xFF);
        checksumBytes[1] = (byte) ((checksum >> 16) & 0xFF);
        checksumBytes[2] = (byte) ((checksum >> 8) & 0xFF);
        checksumBytes[3] = (byte) (checksum & 0xFF);

        byte[] payload = new byte[(messageBytes.length + checksumBytes.length) * 2];

        int offset = 0;
        for (int repeat = 0; repeat < 2; repeat++) {
            System.arraycopy(messageBytes, 0, payload, offset, messageBytes.length);
            offset += messageBytes.length;
            System.arraycopy(checksumBytes, 0, payload, offset, checksumBytes.length);
            offset += checksumBytes.length;
        }

        return payload;
    }

    private void embedPayload(PDDocument document, PDPage page, byte[] payload) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        float width = mediaBox.getWidth();
        float height = mediaBox.getHeight();

        float usableWidth = Math.max(0, width - 2 * GRID_MARGIN);
        float usableHeight = Math.max(0, height - 2 * GRID_MARGIN);

        int cols = Math.max(1, (int) Math.floor(usableWidth / GRID_SPACING));
        int rows = Math.max(1, (int) Math.floor(usableHeight / GRID_SPACING));
        int capacity = rows * cols;

        if (capacity == 0) {
            return;
        }

        List<Integer> bits = toBitList(payload);

        PDResources resources = page.getResources();
        if (resources == null) {
            resources = new PDResources();
            page.setResources(resources);
        }

        PDExtendedGraphicsState dataState = new PDExtendedGraphicsState();
        dataState.setNonStrokingAlphaConstant(DATA_ALPHA);
        resources.add(dataState);

        PDExtendedGraphicsState fiducialState = new PDExtendedGraphicsState();
        fiducialState.setNonStrokingAlphaConstant(FIDUCIAL_ALPHA);
        resources.add(fiducialState);

        try (PDPageContentStream contentStream = new PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true)) {

            contentStream.setNonStrokingColor(0f, 0f, 0f);

            placeFiducials(contentStream, dataState, fiducialState, rows, cols, mediaBox);

            int bitIndex = 0;

            contentStream.setGraphicsStateParameters(dataState);

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (bitIndex >= bits.size()) {
                        return;
                    }

                    if (bits.get(bitIndex) == 1) {
                        float x = GRID_MARGIN + col * GRID_SPACING;
                        float y = GRID_MARGIN + row * GRID_SPACING;
                        drawDot(contentStream, x, y);
                    }
                    bitIndex++;
                }
            }
        }
    }

    private void placeFiducials(PDPageContentStream contentStream,
                                 PDExtendedGraphicsState dataState,
                                 PDExtendedGraphicsState fiducialState,
                                 int rows,
                                 int cols,
                                 PDRectangle box) throws IOException {

        contentStream.setGraphicsStateParameters(fiducialState);

        float minX = GRID_MARGIN;
        float minY = GRID_MARGIN;
        float maxX = box.getWidth() - GRID_MARGIN;
        float maxY = box.getHeight() - GRID_MARGIN;

        drawDot(contentStream, minX, minY);
        drawDot(contentStream, maxX, minY);
        drawDot(contentStream, minX, maxY);
        drawDot(contentStream, maxX, maxY);

        contentStream.setGraphicsStateParameters(dataState);
    }

    private void drawDot(PDPageContentStream contentStream, float x, float y) throws IOException {
        float halfDot = DOT_SIZE / 2f;
        contentStream.addRect(x - halfDot, y - halfDot, DOT_SIZE, DOT_SIZE);
        contentStream.fill();
    }

    private List<Integer> toBitList(byte[] payload) {
        List<Integer> bits = new ArrayList<>(payload.length * 8);
        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                int value = (b >> bit) & 0x01;
                bits.add(value);
            }
        }
        return bits;
    }
}
