package com.smartapp.smartanalyser.controllers;

import com.smartapp.smartanalyser.models.Question;
import com.smartapp.smartanalyser.services.DatabaseService;
import com.smartapp.smartanalyser.services.PDFService;
import com.smartapp.smartanalyser.services.QuestionAnalyzerService;
import com.smartapp.smartanalyser.services.WikipediaService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private Tab homeTab;
    @FXML private Tab analysisTab;
    @FXML private Tab learnTab;
    @FXML private Tab dashboardTab;
    
    // Home Tab
    @FXML private Button uploadButton;
    @FXML private Label uploadStatusLabel;
    @FXML private ProgressBar uploadProgressBar;
    
    // Analysis Tab
    @FXML private ListView<Question> questionListView;
    @FXML private TextArea questionDetailsTextArea;
    @FXML private ListView<String> categoriesListView;
    
    // Learn Tab
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private WebView wikiWebView;
    @FXML private TextArea queryTextArea;
    @FXML private Button askButton;
    @FXML private TextArea responseTextArea;
    
    // Dashboard Tab
    @FXML private PieChart categoriesChart;
    @FXML private VBox dashboardContainer;
    
    private PDFService pdfService;
    private QuestionAnalyzerService analyzerService;
    private WikipediaService wikipediaService;
    private DatabaseService databaseService;
    private ObservableList<Question> questions = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pdfService = new PDFService();
        analyzerService = new QuestionAnalyzerService();
        wikipediaService = new WikipediaService();
        databaseService = new DatabaseService();
        
        // Initialize database
        databaseService.initializeDatabase();
        
        // Setup UI components
        setupHomeTab();
        setupAnalysisTab();
        setupLearnTab();
        setupDashboardTab();
    }
    
    private void setupHomeTab() {
        uploadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Question PDF");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
            
            if (selectedFile != null) {
                uploadStatusLabel.setText("Processing: " + selectedFile.getName());
                uploadProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                
                // Process PDF in background
                new Thread(() -> {
                    try {
                        List<String> extractedQuestions = pdfService.extractQuestionsFromPDF(selectedFile);
                        List<Question> analyzedQuestions = new ArrayList<>();
                        
                        for (String questionText : extractedQuestions) {
                            Question question = analyzerService.analyzeQuestion(questionText);
                            databaseService.saveQuestion(question);
                            analyzedQuestions.add(question);
                        }
                        
                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            questions.addAll(analyzedQuestions);
                            uploadStatusLabel.setText("Processed " + analyzedQuestions.size() + " questions");
                            uploadProgressBar.setProgress(1.0);
                            
                            // Switch to analysis tab
                            mainTabPane.getSelectionModel().select(analysisTab);
                            updateDashboard();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            uploadStatusLabel.setText("Error: " + e.getMessage());
                            uploadProgressBar.setProgress(0);
                        });
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
    
    private void setupAnalysisTab() {
        questionListView.setItems(questions);
        questionListView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getQuestionText().length() > 50 ? 
                            item.getQuestionText().substring(0, 50) + "..." : 
                            item.getQuestionText());
                }
            }
        });
        
        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                questionDetailsTextArea.setText(newVal.getQuestionText() + "\n\n" + newVal.getAnswer());
                categoriesListView.setItems(FXCollections.observableArrayList(newVal.getCategories()));
            }
        });
    }
    
    private void setupLearnTab() {
        searchButton.setOnAction(event -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                wikiWebView.getEngine().load(wikipediaService.getWikipediaUrl(searchTerm));
            }
        });
        
        askButton.setOnAction(event -> {
            String query = queryTextArea.getText().trim();
            if (!query.isEmpty()) {
                responseTextArea.setText("Processing your query...");
                
                new Thread(() -> {
                    String response = analyzerService.getResponseForQuery(query);
                    javafx.application.Platform.runLater(() -> {
                        responseTextArea.setText(response);
                    });
                }).start();
            }
        });
    }
    
    private void setupDashboardTab() {
        updateDashboard();
    }
    
    private void updateDashboard() {
        // Get all questions from database
        List<Question> allQuestions = databaseService.getAllQuestions();
        
        // Count categories
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Question question : allQuestions) {
            for (String category : question.getCategories()) {
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            }
        }
        
        // Create chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        
        categoriesChart.setData(pieChartData);
        categoriesChart.setTitle("Question Categories");
    }
}
