
package com.smartapp.smartanalyser.services;

import com.smartapp.smartanalyser.models.Question;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionAnalyzerService {
    
    private static final String TOGETHER_API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String TOGETHER_API_KEY = "48ac1298eb7cb32febe78f4c118760705a5ec7e7b9d2a3f5837679256a65aee7"; // Replace with your actual API key
    
    // Using Llama-2-7b-chat-hf for fast responses
    private static final String MODEL_NAME = "mistralai/Mistral-7B-Instruct-v0.1";
    
    public Question analyzeQuestion(String questionText) {
        Question question = new Question(questionText);
        
        try {
            // Get categories using Llama-2-7b
            List<String> categories = getCategoriesFromAPI(questionText);
            question.setCategories(categories);
            
            // Get answer using Llama-2-7b
            String answer = getAnswerFromAPI(questionText);
            question.setAnswer(answer);
            
            System.out.println("Analyzed question: " + questionText.substring(0, Math.min(50, questionText.length())) + "...");
            System.out.println("Found topics: " + categories);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback categories if API fails
            question.addCategory("General");
            question.setAnswer("Could not generate answer due to an error: " + e.getMessage());
        }
        
        return question;
    }
    
    private List<String> getCategoriesFromAPI(String questionText) throws Exception {
        // Optimized prompt for Llama-2-7b
        String prompt = "Identify 2-3 main academic topics for this question. Return only comma-separated topics:\n\n" + questionText;
        
        String response = callTogetherAI(prompt, 60); // Short response for topics
        
        if (response != null && !response.isEmpty()) {
            // Clean up the response
            String cleanResponse = response.trim().replaceAll("^[^a-zA-Z]*", "").replaceAll("[^a-zA-Z,\\s]*$", "");
            List<String> categories = Arrays.asList(cleanResponse.split("\\s*,\\s*"));
            
            // Filter and clean categories
            List<String> cleanCategories = new ArrayList<>();
            for (String category : categories) {
                String clean = category.trim().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
                if (!clean.isEmpty() && clean.length() > 2) {
                    cleanCategories.add(clean);
                }
            }
            
            return cleanCategories.isEmpty() ? Arrays.asList("General") : cleanCategories;
        }
        
        return Arrays.asList("General");
    }
    
    private String getAnswerFromAPI(String questionText) throws Exception {
        // Optimized prompt for Llama-2-7b
        String prompt = "Answer this question clearly and concisely:\n\n" + questionText;
        
        return callTogetherAI(prompt, 300); // Moderate length for answers
    }
    
    public String getResponseForQuery(String query) {
        try {
            String prompt = "Provide a helpful answer to this question:\n\n" + query;
            return callTogetherAI(prompt, 400);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process your query. Error: " + e.getMessage();
        }
    }
    
    private String callTogetherAI(String prompt, int maxTokens) throws Exception {
        if (TOGETHER_API_KEY.equals("YOUR_TOGETHER_API_KEY")) {
            throw new Exception("Please set your Together AI API key in the TOGETHER_API_KEY constant");
        }
        
        URL url = new URL(TOGETHER_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + TOGETHER_API_KEY);
        connection.setDoOutput(true);
        
        // Optimized settings for Llama-2-7b-chat-hf
        String jsonInputString = "{"
                + "\"model\": \"" + MODEL_NAME + "\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"You are a helpful assistant that provides clear, accurate answers.\"},"
                + "  {\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}"
                + "],"
                + "\"max_tokens\": " + maxTokens + ","
                + "\"temperature\": 0.2," // Low temperature for consistent, focused responses
                + "\"top_p\": 0.9,"
                + "\"repetition_penalty\": 1.1"
                + "}";
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        
        if (responseCode != 200) {
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
            }
            throw new Exception("API Error " + responseCode + ": " + errorResponse.toString());
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        // Parse response from Llama-2-7b
        Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(response.toString());
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        
        return "No response generated";
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    private String unescapeJson(String text) {
        return text.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}