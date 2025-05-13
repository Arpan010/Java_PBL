package com.supreme.SmartQuestionAnalyser;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class App {

    // Main method - entry point of the program
    public static void main(String[] args) {

        // File paths (you can change the file names here)
        String pdfFilePath = "src/main/resources/sample.pdf";
        String logFilePath = "src/main/resources/log.txt";

        // Create an object of this class to call its methods
        App analyser = new App();

        // Process the PDF file to extract and save questions
        analyser.processPDF(pdfFilePath, logFilePath);
    }

    // This method reads the PDF file and extracts the text from it
    public String extractTextFromPDF(String pdfFilePath) throws IOException {
        File pdfFile = new File(pdfFilePath);

        // Load the PDF document using PDFBox
        try (PDDocument document = PDDocument.load(pdfFile)) {
            // Extract text using PDFTextStripper
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // This method finds all unique questions from the extracted text
    public Set<String> extractUniqueQuestions(String text) {
        Set<String> uniqueQuestions = new LinkedHashSet<>();

        // This pattern finds lines ending with a question mark
        String regex = "(?m)([A-Z][^?]*\\?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // Add each found question to the set
        while (matcher.find()) {
            String question = matcher.group(1).trim();
            uniqueQuestions.add(question);
        }

        return uniqueQuestions;
    }

    // This method writes all the questions into a log file
    public void writeQuestionsToLogFile(Set<String> questions, String logFilePath) throws IOException {
        FileWriter fileWriter = new FileWriter(logFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        // Write each question on a new line
        for (String question : questions) {
            bufferedWriter.write(question);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }

    // This method combines all steps: read, extract, and write
    public void processPDF(String pdfFilePath, String logFilePath) {
        try {
            // Step 1: Extract text from the PDF
            String text = extractTextFromPDF(pdfFilePath);

            // Step 2: Find unique questions
            Set<String> questions = extractUniqueQuestions(text);

            // Step 3: Save the questions
            if (questions.isEmpty()) {
                System.out.println("No questions found in the PDF.");
            } else {
                System.out.println("Found " + questions.size() + " unique questions.");
                writeQuestionsToLogFile(questions, logFilePath);
                System.out.println("Questions saved to " + logFilePath);
            }

        } catch (IOException e) {
            System.err.println("Error while processing PDF: " + e.getMessage());
        }
    }
}
