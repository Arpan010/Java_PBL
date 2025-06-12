package smart_app_0.smart_analyse.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikipediaService {
    
    private static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php";
    
    public String getWikipediaUrl(String searchTerm) {
        return "https://en.wikipedia.org/wiki/" + searchTerm.replace(" ", "_");
    }
    
    public String getWikipediaContent(String searchTerm) {
        try {
            String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
            URL url = new URL(WIKIPEDIA_API_URL + "?action=query&format=json&prop=extracts&exintro=true&redirects=1&titles=" + encodedSearchTerm);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            // Parse JSON response to extract content
            // This is a simplified implementation
            String jsonResponse = response.toString();
            int extractStart = jsonResponse.indexOf("\"extract\":\"");
            if (extractStart != -1) {
                extractStart += 11; // Length of "\"extract\":\""
                int extractEnd = jsonResponse.indexOf("\"}", extractStart);
                if (extractEnd != -1) {
                    return jsonResponse.substring(extractStart, extractEnd)
                            .replace("\\\"", "\"")
                            .replace("\\n", "\n");
                }
            }
            
            return "No information found for " + searchTerm;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving information: " + e.getMessage();
        }
    }
}
