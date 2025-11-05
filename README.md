# PDF Invisible Watermark

This module provides a Java command line utility that embeds an **invisible but printable** watermark into an existing PDF file. The watermark is encoded as a dense micro-dot grid rendered with very low opacity so it is nearly imperceptible on screen or paper, yet it can be recovered from a high-resolution scan of the printed document.

## Building

```bash
mvn package
```

This produces `target/pdf-invisible-watermark-0.1.0.jar`.

## Usage

```bash
java -jar target/pdf-invisible-watermark-0.1.0.jar input.pdf output.pdf "Confidential Batch 1701"
```

- `input.pdf`: source document that will receive the watermark
- `output.pdf`: destination file written with the embedded watermark
- Watermark message: any UTF-8 string (the utility automatically appends a CRC32 checksum and repeats the payload twice for resilience)

## Watermark Encoding

- The payload (message + checksum) is converted into bits and laid out row-by-row across a virtual grid on every page.
- A bit value of `1` is drawn as a 1.5pt square rendered with 8% opacity black ink; `0` is left blank.
- Four stronger fiducial dots (25% opacity) are drawn at the corners of the grid to simplify alignment during decoding.
- Because the dots are so light, the pattern is effectively invisible to the naked eye but remains detectable after printing when contrast is increased.

## Decoding Guidance

1. Scan the printed page at ≥600 dpi.
2. Convert the image to grayscale and apply contrast stretching or histogram equalisation.
3. Detect the four fiducial markers to lock the grid orientation and scale.
4. Sample each grid cell; classify it as `1` if dot intensity exceeds surrounding background.
5. Reassemble the bit stream row-by-row, split it into bytes, and decode as UTF-8.
6. Validate the trailing CRC32; if it matches, the recovered message is correct.

The grid repeats the payload twice, so recovering a single consistent copy is sufficient to read the watermark even if part of the page is damaged.
