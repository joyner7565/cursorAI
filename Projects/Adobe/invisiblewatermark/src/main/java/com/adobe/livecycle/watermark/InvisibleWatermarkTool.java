package com.adobe.livecycle.watermark;

import com.adobe.livecycle.watermark.api.DecodeWatermark;
import com.adobe.livecycle.watermark.api.EncodeWatermark;
import com.adobe.livecycle.watermark.api.PDFFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class InvisibleWatermarkTool {
    private static Map<String, Map<String, String>> codeTablet = new HashMap<>();
/*     */ 

    public static void main(String[] args) throws Exception {
        if (args.length != 4 && args.length != 5) {
            printUsage();
            System.exit(-1);
        }

        var dbFilePath = args[1];
        try {
            readDBFile(dbFilePath);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            System.exit(-1);
        }

        if (args[0].equalsIgnoreCase("encode")) {
            if (args.length != 5) {
                printUsage();
                System.exit(-1);
            }
            var inputPDFPath = args[2];
            var watermarkId = args[3];
            var outputPDFPath = args[4];
            encodeInvisibleWatermark(inputPDFPath, watermarkId, outputPDFPath);
        } else if (args[0].equalsIgnoreCase("decode")) {
            if (args.length != 4) {
                printUsage();
                System.exit(-1);
            }
            var inputPDFPath = args[2];
            var watermarkPDFPath = args[3];
            decodeInvisibleWatermark(inputPDFPath, watermarkPDFPath);
        } else {
            printUsage();
        }

        flushDBFile(dbFilePath);
    }
/*     */ 

    static void printUsage() {
        System.out.println("Usage: ");
        System.out.println("<program> encode <dbfilepath> <pdfinputfile> <watermarkid> <pdfoutputfile>");
        System.out.println("[or]");
        System.out.println("<program> decode <dbfilepath> <pdfinputfile> <pdfwatermarkfile>");
    }
/*     */ 

    static void flushDBFile(String dbFilePath) throws IOException {
        if (codeTablet.isEmpty()) {
            return;
        }
        var file = new File(dbFilePath);
        
        file.createNewFile();
        
        try (var dbFile = new BufferedWriter(new FileWriter(file))) {
            for (var entry : codeTablet.entrySet()) {
                var title = entry.getKey();
                dbFile.write("TITLE:" + title + "\n");
                
                var mappings = entry.getValue();
                for (var mappingEntry : mappings.entrySet()) {
                    var code = mappingEntry.getKey();
                    var id = mappingEntry.getValue();
                    dbFile.write(code + " " + id + "\n");
                }
                
                dbFile.write("\n");
            }
        }
    }
/*     */ 

    static void readDBFile(String dbFilePath) throws IOException {
        var file = new File(dbFilePath);
        
        var justCreated = file.createNewFile();
        
        if (!justCreated) {
            try (var dbFile = new BufferedReader(new FileReader(file))) {
                final int AWAITING_TITLE = 1;
                final int AWAITING_CODE = 2;
                
                int state = AWAITING_TITLE;
                
                codeTablet = new HashMap<>();
                
                String title = "";
                String line;
                while ((line = dbFile.readLine()) != null) {
                    if (line.isEmpty()) {
                        state = AWAITING_TITLE;
                        continue;
                    }
                    
                    switch (state) {
                        case AWAITING_TITLE -> {
                            if (!line.startsWith("TITLE:")) {
                                throw new IOException("Error: Invalid db file!");
                            }
                            title = line.substring("TITLE:".length());
                            if (title.isEmpty()) {
                                throw new IOException("Error: Invalid db file!");
                            }
                            codeTablet.put(title, new HashMap<>());
                            state = AWAITING_CODE;
                        }
                        case AWAITING_CODE -> {
                            var tokens = line.split(" ");
                            if (tokens.length != 2) {
                                throw new IOException("Error: Invalid db file!");
                            }
                            var mappings = codeTablet.get(title);
                            if (mappings.containsKey(tokens[0])) {
                                throw new IOException("Error: Invalid db file!");
                            }
                            mappings.put(tokens[0], tokens[1]);
                        }
                    }
                }
            }
        }
    }
/*     */ 

    static void encodeInvisibleWatermark(String inputPDFPath, String watermarkId, String outputPDFPath) throws Exception {
        if (watermarkId.indexOf(' ') != -1 || 
            watermarkId.indexOf('\\') != -1 || 
            watermarkId.indexOf('/') != -1 || 
            watermarkId.indexOf(':') != -1 || 
            watermarkId.indexOf('*') != -1 || 
            watermarkId.indexOf('?') != -1 || 
            watermarkId.indexOf('"') != -1 || 
            watermarkId.indexOf('<') != -1 || 
            watermarkId.indexOf('>') != -1 || 
            watermarkId.indexOf('|') != -1) {
            System.out.println("Error: Invalid watermark");
            return;
        }

        var pdfFile = new PDFFile(inputPDFPath);
        
        var pdfTitle = pdfFile.getDocTitle();
        
        var encoder = new EncodeWatermark();
        
        // 2^20 = 1,048,576 unique codes (sufficient for 1M users)
        int wmBitsLength = 20;
        
        boolean isWmBitsUnique = false;
        String wmBits = "";
        
        while (!isWmBitsUnique) {
            wmBits = generateRandBits(wmBitsLength);
            
            if (codeTablet.containsKey(pdfTitle)) {
                var existingMappings = codeTablet.get(pdfTitle);
                if (!existingMappings.containsKey(wmBits)) {
                    isWmBitsUnique = true;
                }
            } else {
                isWmBitsUnique = true;
            }
        }

        codeTablet.computeIfAbsent(pdfTitle, k -> new HashMap<>());
        var mappings = codeTablet.get(pdfTitle);
        mappings.put(wmBits, watermarkId);
        
        try {
            pdfFile = encoder.encode(pdfFile, wmBits);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            pdfFile.close();
            return;
        }
        pdfFile.saveAsAndClose(outputPDFPath);
        System.out.println("Success");
    }
/*     */ 

    static void decodeInvisibleWatermark(String inputPDFPath, String watermarkPDFPath) throws Exception {
        PDFFile orgPdfFile;
        try {
            orgPdfFile = new PDFFile(inputPDFPath);
        } catch (Exception e) {
            System.out.println("Error: could not read input PDF file!");
            return;
        }
        
        PDFFile wmPdfFile;
        try {
            wmPdfFile = new PDFFile(watermarkPDFPath);
        } catch (Exception e) {
            System.out.println("Error: could not read watermarked PDF file!");
            return;
        }

        var orgPdfTitle = orgPdfFile.getDocTitle();
        var wmPdfTitle = wmPdfFile.getDocTitle();
        if (!orgPdfTitle.equals(wmPdfTitle)) {
            orgPdfFile.close();
            wmPdfFile.close();
            System.out.println("Error: incorrect watermark file!");
            return;
        }

        var decoder = new DecodeWatermark();
        
        String decodedWatermark = "";
        try {
            decodedWatermark = decoder.decode(orgPdfFile, wmPdfFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        orgPdfFile.close();
        wmPdfFile.close();

        if (decodedWatermark.isEmpty()) {
            System.out.println("Error: identical files!");
            return;
        }
        
        if (!codeTablet.containsKey(orgPdfTitle)) {
            System.out.println("Error: cannot find mapping in db file!");
        } else {
            var mappings = codeTablet.get(orgPdfTitle);
            if (!mappings.containsKey(decodedWatermark)) {
                System.out.println("Error: cannot find mapping in db file!");
            } else {
                var watermarkId = mappings.get(decodedWatermark);
                System.out.println("Success: " + watermarkId);
            }
        }
    }

    static int getmenu() throws IOException {
        int choice;
        try (var console = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("1. Encode Invisible Watermark.");
                System.out.println("2. Decode Invisible Watermark.");
                System.out.println("3. Exit.");
                System.out.print("Enter your choice: ");
                
                String s = "";
                while (s.isEmpty()) {
                    s = console.readLine();
                }
                
                choice = 0;
                try {
                    choice = Integer.parseInt(s);
                } catch (NumberFormatException ignored) {
                }

                if (choice < 1 || choice > 3) {
                    System.out.println("Invalid choice");
                    System.out.println();
                    continue;
                }
                break;
            }
        }
        return choice;
    }


    static String generateRandBits(int numBits) {
        var randBits = new StringBuilder();
        
        for (int i = 0; i < numBits; i++) {
            long val = Math.round(Math.random());
            randBits.append(val);
        }
        return randBits.toString();
    }
}