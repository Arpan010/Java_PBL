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
    
    // Updated model names (these are confirmed working models)
    private static final String MODEL_NAME = "NousResearch/Nous-Hermes-2-Mixtral-8x7B-DPO";
    // Alternative models you can try:
    // private static final String MODEL_NAME = "mistralai/Mixtral-8x7B-Instruct-v0.1";
    // private static final String MODEL_NAME = "meta-llama/Llama-2-70b-chat-hf";
    
    public Question analyzeQuestion(String questionText) {
        Question question = new Question(questionText);
        
        try {
            // Get categories using Together AI
            List<String> categories = getCategoriesFromAPI(questionText);
            question.setCategories(categories);
            
            // Get answer using Together AI
            String answer = getAnswerFromAPI(questionText);
            question.setAnswer(answer);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback categories if API fails
            question.addCategory("Uncategorized");
            question.setAnswer("Could not generate answer due to an error: " + e.getMessage());
        }
        
        return question;
    }
    
    private List<String> getCategoriesFromAPI(String questionText) throws Exception {
        String prompt = "Identify the main topics or categories for this question. " +
                "Return only a comma-separated list of 2-4 categories. Question: " + questionText;
        
        String response = callTogetherAI(prompt);
        
        // Parse comma-separated categories
        if (response != null && !response.isEmpty()) {
            return Arrays.asList(response.split("\\s*,\\s*"));
        }
        
        return new ArrayList<>();
    }
    
    private String getAnswerFromAPI(String questionText) throws Exception {
        String prompt = "Provide a detailed answer to this question: " + questionText;
        return callTogetherAI(prompt);
    }
    
    public String getResponseForQuery(String query) {
        try {
            String prompt = "Answer this question in detail: " + query;
            return callTogetherAI(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process your query. Error: " + e.getMessage();
        }
    }
    
    private String callTogetherAI(String prompt) throws Exception {
        // Check if API key is set
        if (TOGETHER_API_KEY.equals("YOUR_TOGETHER_API_KEY")) {
            throw new Exception("Please set your Together AI API key in the TOGETHER_API_KEY constant");
        }
        
        URL url = new URL(TOGETHER_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + TOGETHER_API_KEY);
        connection.setDoOutput(true);
        
        // Updated JSON format for Together AI
        String jsonInputString = "{"
                + "\"model\": \"" + MODEL_NAME + "\","
                + "\"messages\": ["
                + "  {\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}"
                + "],"
                + "\"max_tokens\": 512,"
                + "\"temperature\": 0.7,"
                + "\"top_p\": 0.7,"
                + "\"top_k\": 50,"
                + "\"repetition_penalty\": 1,"
                + "\"stop\": [\"<|eot_id|>\"]"
                + "}";
        
        System.out.println("Request JSON: " + jsonInputString); // Debug output
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode); // Debug output
        
        StringBuilder response = new StringBuilder();
        
        if (responseCode == 200) {
            // Success - read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } else {
            // Error - read error stream
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            throw new Exception("API request failed with response code: " + responseCode + ". Error: " + response.toString());
        }
        
        System.out.println("API Response: " + response.toString()); // Debug output
        
        // Extract content from Together AI response
        Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"(?=\\s*[,}])");
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