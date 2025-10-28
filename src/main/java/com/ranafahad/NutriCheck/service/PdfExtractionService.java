package com.ranafahad.NutriCheck.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.rendering.PDFRenderer;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class PdfExtractionService {

    /**
     * Main entry: extract text from PDF (using text extraction or OCR fallback).
     */
    public String extractText(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // If the text is meaningful, return it
            if (text != null && text.trim().length() > 20) {
                return text;
            }

            // Otherwise fallback to OCR
            return ocrExtract(file);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error extracting PDF text: " + e.getMessage();
        }
    }

    /**
     * OCR extraction for scanned/image-based PDFs.
     */
    private String ocrExtract(File file) throws IOException, TesseractException {
        Tesseract tesseract = new Tesseract();

        // Automatically detect or extract tessdata directory
        File tessdataDir = resolveTessdataDirectory();
        tesseract.setDatapath(tessdataDir.getAbsolutePath());
        tesseract.setLanguage("eng+hun");

        // --- FIX: Render PDF pages to images first ---
        StringBuilder fullText = new StringBuilder();

        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                String text = tesseract.doOCR(image);
                fullText.append(text).append("\n");
            }
        }

        return fullText.toString();
    }


    /**
     * Find or extract the tessdata directory.
     * Works in IDE, packaged JAR, or Docker.
     */
    private File resolveTessdataDirectory() throws IOException {
        // Check external folder first (for local dev or Docker bind mounts)
        File external = new File("tessdata");
        if (external.exists() && external.isDirectory()) {
            return external;
        }

        // Try to extract from resources (for packaged JARs)
        URL resource = getClass().getResource("/tessdata/eng.traineddata");
        if (resource != null) {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "tessdata");
            if (!tempDir.exists()) tempDir.mkdirs();

            // Copy both English and Hungarian traineddata files
            for (String lang : new String[]{"eng.traineddata", "hun.traineddata"}) {
                try (InputStream in = getClass().getResourceAsStream("/tessdata/" + lang)) {
                    if (in == null) continue; // skip if not found
                    File outFile = new File(tempDir, lang);
                    Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return tempDir;
        }

        throw new IOException("tessdata folder not found in resources or project root.");
    }
}