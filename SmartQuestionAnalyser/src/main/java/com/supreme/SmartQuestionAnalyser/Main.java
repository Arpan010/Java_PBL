package com.supreme.SmartQuestionAnalyser;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class Main extends Application {

    private final String LOG_PATH = "src/main/resources/log.txt";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Question Analyser");

        Label title = new Label("Smart Question Analyser");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label instruction = new Label("Upload a Java question paper (PDF):");
        instruction.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        Button selectPDFBtn = new Button("Upload PDF");
        selectPDFBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");

        ScrollPane scrollPane = new ScrollPane(outputArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        selectPDFBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File selectedPDF = fileChooser.showOpenDialog(primaryStage);

            if (selectedPDF != null) {
                try {
                    // Extract questions
                    App app = new App();
                    app.processPDF(selectedPDF.getAbsolutePath(), LOG_PATH);

                    // Analyze and classify
                    QuestionAnalyser analyser = new QuestionAnalyser();
                    analyser.analyze();

                    // Load recent questions from log to avoid showing as recommendations
                    Set<String> uploadedQuestions = readLogQuestions(LOG_PATH);
                    StringBuilder outputText = new StringBuilder();

                    List<String> topics = analyser.getTopics();
                    if (topics.isEmpty()) {
                        outputText.append("No topics detected.\n");
                    } else {
                        outputText.append("Detected Topics: ").append(topics).append("\n\n");
                        outputText.append("Recommended Practice Questions:\n\n");

                        for (String topic : topics) {
                            outputText.append("Topic: ").append(topic).append("\n");
                            List<String> recommendations = analyser.getSimilarQuestionsExcluding(topic, 5, uploadedQuestions);
                            if (recommendations.isEmpty()) {
                                outputText.append("  No practice questions available.\n\n");
                            } else {
                                for (int i = 0; i < recommendations.size(); i++) {
                                    outputText.append("  ").append(i + 1).append(". ").append(recommendations.get(i)).append("\n");
                                }
                                outputText.append("\n");
                            }
                        }
                    }

                    outputArea.setText(outputText.toString());

                } catch (Exception ex) {
                    outputArea.setText("Error: " + ex.getMessage());
                }
            }
        });

        VBox layout = new VBox(15, title, instruction, selectPDFBtn, scrollPane);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
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
