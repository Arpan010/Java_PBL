package com.smartapp.smartanalyser.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFService {
    
    public List<String> extractQuestionsFromPDF(File pdfFile) throws IOException {
        List<String> questions = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Split text into questions
            // This is a simple implementation - you might need to adjust the regex
            // based on the actual format of your PDF questions
            Pattern pattern = Pattern.compile("(?:\\d+\\.\\s*|Q\\d+\\.\\s*|Question\\s*\\d+\\.\\s*)(.*?)(?=\\d+\\.\\s*|Q\\d+\\.\\s*|Question\\s*\\d+\\.\\s*|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            
            while (matcher.find()) {
                String question = matcher.group(1).trim();
                if (!question.isEmpty()) {
                    questions.add(question);
                }
            }
            
            // If no questions found with the pattern, try splitting by lines
            if (questions.isEmpty()) {
                String[] lines = text.split("\\r?\\n");
                StringBuilder currentQuestion = new StringBuilder();
                
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    if (line.matches("\\d+\\..*|Q\\d+\\..*|Question\\s*\\d+\\..*")) {
                        // New question starts
                        if (currentQuestion.length() > 0) {
                            questions.add(currentQuestion.toString().trim());
                            currentQuestion = new StringBuilder();
                        }
                        currentQuestion.append(line);
                    } else if (currentQuestion.length() > 0) {
                        currentQuestion.append(" ").append(line);
                    }
                }
                
                // Add the last question
                if (currentQuestion.length() > 0) {
                    questions.add(currentQuestion.toString().trim());
                }
            }
        }
        
        return questions;
    }
}
