package com.ranafahad.NutriCheck.controller;

import com.ranafahad.NutriCheck.model.NutritionData;
import com.ranafahad.NutriCheck.service.OpenAIService;
import com.ranafahad.NutriCheck.service.PdfExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfExtractionService pdfService;

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/extract")
    public ResponseEntity<?> extractFromPdf(@RequestParam("file") MultipartFile file) {
        try {
            File temp = File.createTempFile("upload", ".pdf");
            file.transferTo(temp);

            String text = pdfService.extractText(temp);
            System.out.println("=== EXTRACTED TEXT START ===");
            System.out.println(text);
            System.out.println("=== EXTRACTED TEXT END ===");
            NutritionData data = openAIService.extractNutrition(text);

            temp.delete();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}