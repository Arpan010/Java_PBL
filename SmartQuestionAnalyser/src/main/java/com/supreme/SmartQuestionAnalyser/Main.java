package com.supreme.SmartQuestionAnalyser;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.collections.FXCollections;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Main extends Application {

    private final String LOG_PATH = "src/main/resources/log.txt";
    private final String SYLLABUS_PATH = "src/main/resources/syllabus.txt";
    private final String TRAINING_PATH = "src/main/resources/training.txt";
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    private ScrollPane resultsScrollPane;
    private VBox resultsContainer;
    private StackPane contentPane;
    private Set<String> topicsList;

    @Override
    public void start(Stage primaryStage) {
        // Load topics from syllabus.txt
        loadTopicsFromSyllabus();
        
        primaryStage.setTitle("Smart Question Analyser");
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));
        mainLayout.setStyle("-fx-background-color: #f5f7fa;");
        
        // Create header
        HBox header = createHeader();
        mainLayout.setTop(header);
        
        // Create sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);
        
        // Create content area
        contentPane = createContentPane();
        mainLayout.setCenter(contentPane);
        
        // Create footer
        HBox footer = createFooter();
        mainLayout.setBottom(footer);
        
        // Ensure resources directory exists
        createRequiredDirectories();
        
        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Loads topics from syllabus.txt for question classification
     */
    private void loadTopicsFromSyllabus() {
        topicsList = new HashSet<>();
        File syllabusFile = new File(SYLLABUS_PATH);
        
        // Create syllabus file if it doesn't exist with default topics
        if (!syllabusFile.exists()) {
            try {
                createRequiredDirectories();
                Files.write(
                    Paths.get(SYLLABUS_PATH),
                    Arrays.asList("arrays,strings,linked_lists,stacks,queues,trees,graphs,sorting,searching,dynamic_programming,bit_manipulation,recursion,backtracking,greedy_algorithms,hashing"),
                    StandardOpenOption.CREATE
                );
            } catch (IOException e) {
                System.err.println("Failed to create default syllabus.txt: " + e.getMessage());
            }
        }
        
        // Read topics from syllabus file
        try (BufferedReader reader = new BufferedReader(new FileReader(syllabusFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] topics = line.split(",");
                for (String topic : topics) {
                    String trimmedTopic = topic.trim();
                    if (!trimmedTopic.isEmpty()) {
                        topicsList.add(trimmedTopic);
                        // Ensure topic file exists
                        createTopicFileIfNotExists(trimmedTopic);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read syllabus.txt: " + e.getMessage());
        }
    }
    
    /**
     * Creates a topic file if it doesn't exist yet
     */
    private void createTopicFileIfNotExists(String topic) {
        File topicFile = new File("src/main/resources/" + topic + ".txt");
        if (!topicFile.exists()) {
            try {
                topicFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Failed to create topic file for " + topic + ": " + e.getMessage());
            }
        }
    }
    

    /**
     * Creates required directories and files if they don't exist
     */
    private void createRequiredDirectories() {
        try {
            // Create resources directory if it doesn't exist
            Files.createDirectories(Paths.get("src/main/resources"));
            
            // Create necessary files if they don't exist
            Files.createFile(Paths.get(LOG_PATH)); // Simply create the file
            Files.createFile(Paths.get(TRAINING_PATH)); // Simply create the file
                
            // Create topic_keywords.txt if it doesn't exist
            File keywordsFile = new File("src/main/resources/topic_keywords.txt");
            if (!keywordsFile.exists()) {
                StringBuilder keywordsContent = new StringBuilder();
                for (String topic : topicsList) {
                    keywordsContent.append(topic).append(": ").append(topic).append("\n");
                }
                Files.write(
                    Paths.get("src/main/resources/topic_keywords.txt"),
                    keywordsContent.toString().getBytes(),
                    StandardOpenOption.CREATE // This is correct for Files.write
                );
            }
        } catch (FileAlreadyExistsException e) {
            // File already exists, ignore
        } catch (IOException e) {
            System.err.println("Failed to create directories or files: " + e.getMessage());
        }
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setSpacing(10);
        header.setStyle("-fx-background-color: #2c3e50;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        // App logo/icon (placeholder)
        Rectangle logoPlaceholder = new Rectangle(40, 40);
        logoPlaceholder.setFill(Color.web("#3498db"));
        logoPlaceholder.setArcWidth(10);
        logoPlaceholder.setArcHeight(10);
        
        // Title
        Label title = new Label("Smart Question Analyser");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");
        
        header.getChildren().addAll(logoPlaceholder, title);
        
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20));
        sidebar.setSpacing(15);
        sidebar.setStyle("-fx-background-color: #34495e;");
        
        Label sidebarTitle = new Label("Actions");
        sidebarTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sidebarTitle.setStyle("-fx-text-fill: white;");
        
        // Upload button
        Button uploadButton = createSidebarButton("Upload Question Paper", "#3498db");
        uploadButton.setOnAction(e -> handleUploadButton());
        
        // Manage Topics button
        Button manageTopicsButton = createSidebarButton("Manage Topics", "#2ecc71");
        manageTopicsButton.setOnAction(e -> showTopicsManager());
        
        // View Question Bank button
        Button viewBankButton = createSidebarButton("View Question Bank", "#e74c3c");
        viewBankButton.setOnAction(e -> showQuestionBank());
        
        // Help button
        Button helpButton = createSidebarButton("Help", "#f39c12");
        helpButton.setOnAction(e -> showHelp());
        
        // Status indicator
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(20, 0, 0, 0));
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #ecf0f1;");
        
        progressIndicator = new ProgressIndicator(-1);
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(30, 30);
        
        statusBox.getChildren().addAll(statusLabel, progressIndicator);
        
        sidebar.getChildren().addAll(
            sidebarTitle, 
            uploadButton, 
            manageTopicsButton,
            viewBankButton,
            helpButton,
            statusBox
        );
        
        return sidebar;
    }
    
    private Button createSidebarButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(210);
        button.setPrefHeight(40);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
        
        // Add hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;"
            )
        );
        
        return button;
    }
    
    private StackPane createContentPane() {
        StackPane contentPane = new StackPane();
        contentPane.setPadding(new Insets(20));
        contentPane.setStyle("-fx-background-color: white;");
        
        // Welcome screen
        VBox welcomeScreen = createWelcomeScreen();
        
        // Results container (initially empty)
        resultsContainer = new VBox();
        resultsContainer.setSpacing(20);
        resultsContainer.setPadding(new Insets(10));
        resultsContainer.setVisible(false);
        
        resultsScrollPane = new ScrollPane(resultsContainer);
        resultsScrollPane.setFitToWidth(true);
        resultsScrollPane.setVisible(false);
        resultsScrollPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-background: white;" +
            "-fx-border-color: transparent;"
        );
        
        contentPane.getChildren().addAll(welcomeScreen, resultsScrollPane);
        
        return contentPane;
    }
    
    private VBox createWelcomeScreen() {
        VBox welcomeScreen = new VBox();
        welcomeScreen.setSpacing(20);
        welcomeScreen.setAlignment(Pos.CENTER);
        
        Label welcomeTitle = new Label("Welcome to Smart Question Analyser");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        welcomeTitle.setStyle("-fx-text-fill: #2c3e50;");
        
        // Icon placeholder
        Rectangle iconPlaceholder = new Rectangle(120, 120);
        iconPlaceholder.setFill(Color.web("#3498db"));
        iconPlaceholder.setArcWidth(20);
        iconPlaceholder.setArcHeight(20);
        
        Label welcomeText = new Label(
            "Upload a Java question paper (PDF) to analyze topics and get recommended practice questions."
        );
        welcomeText.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
        welcomeText.setWrapText(true);
        welcomeText.setMaxWidth(500);
        welcomeText.setAlignment(Pos.CENTER);
        
        Button uploadBtn = new Button("Upload PDF");
        uploadBtn.setPrefWidth(200);
        uploadBtn.setPrefHeight(50);
        uploadBtn.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16px;"
        );
        uploadBtn.setOnAction(e -> handleUploadButton());
        
        Label topicsLabel = new Label("Available Topics");
        topicsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        topicsLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Display available topics from syllabus
        FlowPane topicsFlow = new FlowPane();
        topicsFlow.setHgap(10);
        topicsFlow.setVgap(8);
        topicsFlow.setPrefWrapLength(500);
        topicsFlow.setAlignment(Pos.CENTER);
        
        for (String topic : topicsList) {
            Label topicChip = new Label(topic);
            topicChip.setPadding(new Insets(5, 10, 5, 10));
            topicChip.setStyle(
                "-fx-background-color: #3498db;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 15;"
            );
            topicsFlow.getChildren().add(topicChip);
        }
        
        welcomeScreen.getChildren().addAll(welcomeTitle, iconPlaceholder, welcomeText, uploadBtn, topicsLabel, topicsFlow);
        return welcomeScreen;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #ecf0f1;");
        
        Label footerText = new Label("© 2025 Smart Question Analyser");
        footerText.setStyle("-fx-text-fill: #7f8c8d;");
        
        footer.getChildren().add(footerText);
        return footer;
    }
    
    private void handleUploadButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File selectedPDF = fileChooser.showOpenDialog(null);
        
        if (selectedPDF != null) {
            // Show loading state
            statusLabel.setText("Processing PDF...");
            progressIndicator.setVisible(true);
            
            // Use a separate thread for processing
            Thread processingThread = new Thread(() -> {
                try {
                    // Extract questions
                    App app = new App();
                    app.processPDF(selectedPDF.getAbsolutePath(), LOG_PATH);
                    
                    // Analyze and classify
                    QuestionAnalyser analyser = new QuestionAnalyser();
                    analyser.analyze();
                    
                    // Load recent questions from log to avoid showing as recommendations
                    Set<String> uploadedQuestions = readLogQuestions(LOG_PATH);
                    
                    List<String> topics = analyser.getTopics();
                    
                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        updateResultsUI(topics, analyser, uploadedQuestions);
                        
                        // Update status
                        statusLabel.setText("Analysis complete");
                        progressIndicator.setVisible(false);
                    });
                    
                } catch (Exception ex) {
                    // Handle errors on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        showError("Error processing PDF: " + ex.getMessage());
                        statusLabel.setText("Error");
                        progressIndicator.setVisible(false);
                    });
                }
            });
            
            processingThread.setDaemon(true);
            processingThread.start();
        }
    }
    
    private void updateResultsUI(List<String> topics, QuestionAnalyser analyser, Set<String> uploadedQuestions) {
        // Clear previous results
        resultsContainer.getChildren().clear();
        
        // Make results visible
        resultsContainer.setVisible(true);
        resultsScrollPane.setVisible(true);
        
        // Create header for results
        Label resultsHeader = new Label("Analysis Results");
        resultsHeader.setFont(Font.font("System", FontWeight.BOLD, 24));
        resultsHeader.setStyle("-fx-text-fill: #2c3e50;");
        
        // Create upload info section
        Label uploadInfo = new Label("Successfully processed and analyzed the question paper");
        uploadInfo.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px;");
        
        resultsContainer.getChildren().addAll(resultsHeader, uploadInfo);
        
        // Show detected topics
        TitledPane topicsPane = createTopicsPane(topics);
        resultsContainer.getChildren().add(topicsPane);
        
        // Show recommendations for each topic
        if (!topics.isEmpty()) {
            Label recommendationsHeader = new Label("Recommended Practice Questions");
            recommendationsHeader.setFont(Font.font("System", FontWeight.BOLD, 20));
            recommendationsHeader.setStyle("-fx-text-fill: #2c3e50; -fx-padding: 10 0 5 0;");
            resultsContainer.getChildren().add(recommendationsHeader);
            
            // Create tabbed recommendations
            TabPane recommendationTabs = new TabPane();
            recommendationTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            
            // Add a tab for each topic
            for (String topic : topics) {
                Tab topicTab = new Tab(topic);
                
                // Create recommendation content for this topic
                VBox recommendationContent = createRecommendationContent(topic, analyser, uploadedQuestions, 10); // Show 10 per topic
                
                ScrollPane scrollContent = new ScrollPane(recommendationContent);
                scrollContent.setFitToWidth(true);
                scrollContent.setStyle("-fx-background: white; -fx-background-color: white;");
                
                topicTab.setContent(scrollContent);
                recommendationTabs.getTabs().add(topicTab);
            }
            
            resultsContainer.getChildren().add(recommendationTabs);
            
            // Add statistics section
            TitledPane statsPane = createStatisticsPane(topics, analyser);
            resultsContainer.getChildren().add(statsPane);
        }
        
        // Add action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(20, 0, 10, 0));
        actionButtons.setAlignment(Pos.CENTER);
        
        Button exportButton = new Button("Export Results");
        exportButton.setStyle(
            "-fx-background-color: #27ae60;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        exportButton.setOnAction(e -> exportResults(topics, analyser, uploadedQuestions));
        
        Button newAnalysisButton = new Button("New Analysis");
        newAnalysisButton.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        newAnalysisButton.setOnAction(e -> resetUI());
        
        actionButtons.getChildren().addAll(exportButton, newAnalysisButton);
        resultsContainer.getChildren().add(actionButtons);
        
        // Animate transition
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), resultsScrollPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    private TitledPane createTopicsPane(List<String> topics) {
        VBox topicsContent = new VBox(10);
        topicsContent.setPadding(new Insets(10));
        
        if (topics.isEmpty()) {
            Label noTopicsLabel = new Label("No topics detected.");
            noTopicsLabel.setStyle("-fx-text-fill: #e74c3c;");
            topicsContent.getChildren().add(noTopicsLabel);
        } else {
            FlowPane topicsFlow = new FlowPane();
            topicsFlow.setHgap(10);
            topicsFlow.setVgap(10);
            
            for (String topic : topics) {
                Label topicLabel = new Label(topic);
                topicLabel.setPadding(new Insets(8, 12, 8, 12));
                topicLabel.setStyle(
                    "-fx-background-color: #3498db;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 4;" +
                    "-fx-font-weight: bold;"
                );
                
                topicsFlow.getChildren().add(topicLabel);
            }
            
            topicsContent.getChildren().add(topicsFlow);
        }
        
        TitledPane topicsPane = new TitledPane("Detected Topics", topicsContent);
        topicsPane.setExpanded(true);
        
        // Style the TitledPane
        topicsPane.setStyle(
            "-fx-text-fill: #2c3e50;" +
            "-fx-font-weight: bold;"
        );
        
        return topicsPane;
    }
    
    private VBox createRecommendationContent(String topic, QuestionAnalyser analyser, Set<String> uploadedQuestions, int maxQuestions) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        List<String> recommendations = analyser.getSimilarQuestionsExcluding(topic, maxQuestions, uploadedQuestions);
        
        if (recommendations.isEmpty()) {
            Label noRecommendationsLabel = new Label("No practice questions available for " + topic);
            noRecommendationsLabel.setStyle("-fx-text-fill: #7f8c8d;");
            content.getChildren().add(noRecommendationsLabel);
        } else {
            for (int i = 0; i < recommendations.size(); i++) {
                VBox questionCard = createQuestionCard(i + 1, recommendations.get(i));
                content.getChildren().add(questionCard);
            }
        }
        
        return content;
    }
    
    private VBox createQuestionCard(int number, String question) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-radius: 5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
        );
        
        Label numberLabel = new Label("Question " + number);
        numberLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        Label questionLabel = new Label(question);
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        card.getChildren().addAll(numberLabel, questionLabel);
        
        return card;
    }
    
    private TitledPane createStatisticsPane(List<String> topics, QuestionAnalyser analyser) {
        VBox statsContent = new VBox(15);
        statsContent.setPadding(new Insets(10));
        
        Label totalQuestionsLabel = new Label("Total Questions: " + countQuestionsInTrainingFile());
        totalQuestionsLabel.setStyle("-fx-font-weight: bold;");
        
        Label topicsCountLabel = new Label("Topics Found: " + topics.size());
        topicsCountLabel.setStyle("-fx-font-weight: bold;");
        
        statsContent.getChildren().addAll(totalQuestionsLabel, topicsCountLabel);
        
        TitledPane statsPane = new TitledPane("Statistics", statsContent);
        statsPane.setExpanded(false);
        
        return statsPane;
    }
    
    private int countQuestionsInTrainingFile() {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(TRAINING_PATH))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            // Ignore
        }
        return count;
    }
    
    private void exportResults(List<String> topics, QuestionAnalyser analyser, Set<String> uploadedQuestions) {
        try {
            File exportFile = new File("analysis_export.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile))) {
                writer.write("SMART QUESTION ANALYSER - ANALYSIS RESULTS\n");
                writer.write("==========================================\n\n");
                
                writer.write("DETECTED TOPICS:\n");
                writer.write("----------------\n");
                for (String topic : topics) {
                    writer.write("- " + topic + "\n");
                }
                
                writer.write("\nRECOMMENDED PRACTICE QUESTIONS:\n");
                writer.write("------------------------------\n");
                
                for (String topic : topics) {
                    writer.write("\nTOPIC: " + topic + "\n");
                    
                    List<String> recommendations = analyser.getSimilarQuestionsExcluding(topic, 10, uploadedQuestions);
                    if (recommendations.isEmpty()) {
                        writer.write("  No practice questions available.\n");
                    } else {
                        for (int i = 0; i < recommendations.size(); i++) {
                            writer.write("  " + (i + 1) + ". " + recommendations.get(i) + "\n");
                        }
                    }
                }
                
                writer.write("\n\nExported on: " + new java.util.Date());
            }
            
            showInfo("Export Successful", "Results exported to " + exportFile.getAbsolutePath());
            
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private void showTopicsManager() {
        VBox topicsManagerContent = new VBox(15);
        topicsManagerContent.setPadding(new Insets(20));
        topicsManagerContent.setStyle("-fx-background-color: white;");

        Label managerTitle = new Label("Manage Topics");
        managerTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        managerTitle.setStyle("-fx-text-fill: #2c3e50;");

        // List of current topics
        Label currentTopicsLabel = new Label("Current Topics:");
        currentTopicsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ListView<String> topicsListView = new ListView<>(FXCollections.observableArrayList(topicsList));
        topicsListView.setPrefHeight(200);

        // Add new topic
        HBox addTopicBox = new HBox(10);
        TextField newTopicField = new TextField();
        newTopicField.setPromptText("Enter new topic");
        Button addTopicButton = new Button("Add Topic");
        addTopicButton.setStyle(
            "-fx-background-color: #27ae60;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        addTopicButton.setOnAction(e -> {
            String newTopic = newTopicField.getText().trim();
            if (!newTopic.isEmpty() && !topicsList.contains(newTopic)) {
                topicsList.add(newTopic);
                topicsListView.getItems().add(newTopic);
                createTopicFileIfNotExists(newTopic);
                // Update syllabus.txt
                updateSyllabusFile();
                newTopicField.clear();
                showInfo("Success", "Topic '" + newTopic + "' added successfully.");
            } else if (topicsList.contains(newTopic)) {
                showError("Topic '" + newTopic + "' already exists.");
            }
        });
        addTopicBox.getChildren().addAll(newTopicField, addTopicButton);

        // Remove topic
        Button removeTopicButton = new Button("Remove Selected Topic");
        removeTopicButton.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        removeTopicButton.setOnAction(e -> {
            String selectedTopic = topicsListView.getSelectionModel().getSelectedItem();
            if (selectedTopic != null) {
                topicsList.remove(selectedTopic);
                topicsListView.getItems().remove(selectedTopic);
                // Update syllabus.txt
                updateSyllabusFile();
                showInfo("Success", "Topic '" + selectedTopic + "' removed successfully.");
            } else {
                showError("Please select a topic to remove.");
            }
        });

        topicsManagerContent.getChildren().addAll(
            managerTitle,
            currentTopicsLabel,
            topicsListView,
            addTopicBox,
            removeTopicButton
        );

        // Replace content pane
        contentPane.getChildren().clear();
        contentPane.getChildren().add(topicsManagerContent);
    }
    
    private void updateSyllabusFile() {
        try {
            Files.write(
                Paths.get(SYLLABUS_PATH),
                String.join(",", topicsList).getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            showError("Failed to update syllabus.txt: " + e.getMessage());
        }
    }
    
    private void showQuestionBank() {
        VBox questionBankContent = new VBox(15);
        questionBankContent.setPadding(new Insets(20));
        questionBankContent.setStyle("-fx-background-color: white;");

        Label bankTitle = new Label("Question Bank");
        bankTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        bankTitle.setStyle("-fx-text-fill: #2c3e50;");

        // Dropdown to select topic
        Label selectTopicLabel = new Label("Select Topic:");
        selectTopicLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<String> topicComboBox = new ComboBox<>(FXCollections.observableArrayList(topicsList));
        topicComboBox.setPromptText("Choose a topic");
        topicComboBox.setPrefWidth(200);

        // Display questions
        VBox questionsContainer = new VBox(10);
        ScrollPane questionsScrollPane = new ScrollPane(questionsContainer);
        questionsScrollPane.setFitToWidth(true);
        questionsScrollPane.setPrefHeight(400);
        questionsScrollPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-background: white;" +
            "-fx-border-color: transparent;"
        );

        topicComboBox.setOnAction(e -> {
            String selectedTopic = topicComboBox.getValue();
            if (selectedTopic != null) {
                questionsContainer.getChildren().clear();
                List<String> questions = loadQuestionsForTopic(selectedTopic);
                if (questions.isEmpty()) {
                    Label noQuestionsLabel = new Label("No questions available for " + selectedTopic);
                    noQuestionsLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    questionsContainer.getChildren().add(noQuestionsLabel);
                } else {
                    for (int i = 0; i < questions.size(); i++) {
                        VBox questionCard = createQuestionCard(i + 1, questions.get(i));
                        questionsContainer.getChildren().add(questionCard);
                    }
                }
            }
        });

        questionBankContent.getChildren().addAll(
            bankTitle,
            selectTopicLabel,
            topicComboBox,
            questionsScrollPane
        );

        // Replace content pane
        contentPane.getChildren().clear();
        contentPane.getChildren().add(questionBankContent);
    }
    
    private List<String> loadQuestionsForTopic(String topic) {
        List<String> questions = new ArrayList<>();
        File topicFile = new File("src/main/resources/" + topic + ".txt");
        if (topicFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(topicFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        questions.add(line.trim());
                    }
                }
            } catch (IOException e) {
                showError("Failed to load questions for " + topic + ": " + e.getMessage());
            }
        }
        return questions;
    }
    
    private void showHelp() {
        VBox helpContent = new VBox(15);
        helpContent.setPadding(new Insets(20));
        helpContent.setStyle("-fx-background-color: white;");

        Label helpTitle = new Label("Help & Documentation");
        helpTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        helpTitle.setStyle("-fx-text-fill: #2c3e50;");

        TextArea helpText = new TextArea(
            "Smart Question Analyser Help\n" +
            "===========================\n\n" +
            "1. Upload Question Paper\n" +
            "   - Click 'Upload Question Paper' to select a PDF file.\n" +
            "   - The system will analyze the paper and detect topics.\n\n" +
            "2. Manage Topics\n" +
            "   - Add or remove topics to customize the syllabus.\n\n" +
            "3. View Question Bank\n" +
            "   - Browse questions by topic from the question bank.\n\n" +
            "4. Export Results\n" +
            "   - After analysis, export the results to a text file.\n\n" +
            "For support, contact: support@smartquestionanalyser.com"
        );
        helpText.setEditable(false);
        helpText.setWrapText(true);
        helpText.setPrefHeight(400);
        helpText.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

        helpContent.getChildren().addAll(helpTitle, helpText);

        // Replace content pane
        contentPane.getChildren().clear();
        contentPane.getChildren().add(helpContent);
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void resetUI() {
        resultsContainer.setVisible(false);
        resultsScrollPane.setVisible(false);
        statusLabel.setText("Ready");
    }
    
    private Set<String> readLogQuestions(String path) {
        Set<String> questions = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                questions.add(line.trim());
            }
        } catch (IOException e) {
            // Ignore
        }
        return questions;
    }

    public static void main(String[] args) {
        launch(args);
    }
}