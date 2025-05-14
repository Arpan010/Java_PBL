package com.supreme.SmartQuestionAnalyser;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class javafxcode extends Application {
    private ListView<String> topicsListView = new ListView<>();
    private TextArea questionsTextArea = new TextArea();
    private QuestionAnalyser analyser = new QuestionAnalyser();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.getChildren().addAll(new Label("Topics:"), topicsListView, 
                                  new Label("Similar Questions:"), questionsTextArea);

        Button analyzeButton = new Button("Analyze");
        analyzeButton.setOnAction(e -> {
            analyser.analyze();
            topicsListView.getItems().setAll(analyser.getTopics());
        });
        root.getChildren().add(analyzeButton);

        topicsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                questionsTextArea.setText(String.join("\n", analyser.getSimilarQuestions(newVal, 10)));
            }
        });

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}