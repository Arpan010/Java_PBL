package smart_app_0.smart_analyse.services;

import smart_app_0.smart_analyse.models.Question;

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
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "YOUR_OPENAI_API_KEY"; // Replace with your actual API key
    
    public Question analyzeQuestion(String questionText) {
        Question question = new Question(questionText);
        
        try {
            // Get categories using OpenAI API
            List<String> categories = getCategoriesFromAPI(questionText);
            question.setCategories(categories);
            
            // Get answer using OpenAI API
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
        
        String response = callOpenAI(prompt);
        
        // Parse comma-separated categories
        if (response != null && !response.isEmpty()) {
            return Arrays.asList(response.split("\\s*,\\s*"));
        }
        
        return new ArrayList<>();
    }
    
    private String getAnswerFromAPI(String questionText) throws Exception {
        String prompt = "Provide a detailed answer to this question, including relevant diagrams " +
                "or visual explanations if needed (described in text): " + questionText;
        
        return callOpenAI(prompt);
    }
    
    public String getResponseForQuery(String query) {
        try {
            String prompt = "Answer this question in detail: " + query;
            return callOpenAI(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process your query. Error: " + e.getMessage();
        }
    }
    
    private String callOpenAI(String prompt) throws Exception {
        URL url = new URL(OPENAI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        connection.setDoOutput(true);
        
        String jsonInputString = "{"
                + "\"model\": \"gpt-3.5-turbo\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"You are a helpful assistant that provides accurate and detailed information.\"},"
                + "  {\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}"
                + "],"
                + "\"temperature\": 0.7"
                + "}";
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        // Extract content from OpenAI response
        Pattern pattern = Pattern.compile("\"content\":\"(.*?)\"[,}]");
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
