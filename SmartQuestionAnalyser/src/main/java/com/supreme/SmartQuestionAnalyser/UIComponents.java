package com.supreme.SmartQuestionAnalyser;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UIComponents {
    private static VBox welcomeScreen;

    public static void setWelcomeScreen(VBox screen) {
        welcomeScreen = screen;
    }

    public static void playFadeIn(Node node) {
        FadeTransition ft = new FadeTransition(Duration.seconds(1), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public static HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(15));
        Label title = new Label("Smart Question Analyser");
        title.getStyleClass().add("header-title");
        header.getChildren().add(title);
        return header;
    }

    public static HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(8));
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label lbl = new Label("© 2025 Supreme SmartQuestionAnalyser");
        footer.getChildren().add(lbl);
        return footer;
    }

    public static Button createStyledButton(String text, EventHandler<ActionEvent> action) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(action);
        return btn;
    }

    public static Button createColorfulButton(String text, EventHandler<ActionEvent> action) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setStyle("-fx-background-color: " + getRandomPastelColor() + "; -fx-text-fill: #222; -fx-font-weight: bold;");
        btn.setOnAction(action);
        btn.setEffect(new DropShadow());
        return btn;
    }

    private static String getRandomPastelColor() {
        Random rand = new Random();
        int r = 180 + rand.nextInt(60);
        int g = 180 + rand.nextInt(60);
        int b = 180 + rand.nextInt(60);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    public static VBox createWelcomeScreen(EventHandler<ActionEvent> uploadAction) {
        VBox screen = new VBox(20);
        screen.setAlignment(Pos.CENTER);
        screen.getStyleClass().add("welcome-screen");
        Label welcomeLabel = new Label("Welcome to Smart Question Analyser");
        welcomeLabel.getStyleClass().add("welcome-label");
        screen.getChildren().addAll(welcomeLabel,
                createStyledButton("Upload PDF", uploadAction));
        welcomeScreen = screen;
        return screen;
    }

    public static VBox createTopicsManagerPane(List<String> topicsList, Set<String> analysedTopics, boolean analysisDone) {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.getStyleClass().add("topics-pane");

        Label title = new Label("Manage Topics");
        title.getStyleClass().add("section-title");

        VBox topicsBox = new VBox(10);
        topicsBox.setPadding(new Insets(10));

        ScrollPane topicsScrollPane = new ScrollPane(topicsBox);
        topicsScrollPane.setFitToWidth(true);
        topicsScrollPane.setPrefHeight(400);

        Button backBtn = createStyledButton("Back to Home", e -> {
            if (pane.getParent() != null) {
                ((StackPane) pane.getParent()).getChildren().forEach(n -> n.setVisible(false));
                welcomeScreen.setVisible(true);
            }
        });
        pane.getChildren().addAll(title, topicsScrollPane, backBtn);

        // Initialize the content immediately
        updateTopicsBox(topicsBox, analysedTopics, analysisDone);

        // Update when visibility changes
        pane.visibleProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                updateTopicsBox(topicsBox, analysedTopics, analysisDone);
            }
        });

        return pane;
    }

    private static void updateTopicsBox(VBox topicsBox, Set<String> analysedTopics, boolean analysisDone) {
        topicsBox.getChildren().clear();
        if (!analysisDone) {
            Label msg = new Label("Analysis not done yet. Please upload a PDF first.");
            msg.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            topicsBox.getChildren().add(msg);
        } else if (analysedTopics.isEmpty()) {
            Label msg = new Label("No topics found after analysis.");
            msg.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            topicsBox.getChildren().add(msg);
        } else {
            Label infoLabel = new Label("Click on a topic to view its questions:");
            infoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 10 0;");
            topicsBox.getChildren().add(infoLabel);
            
            for (String topic : analysedTopics) {
                Button topicBtn = createColorfulButton(topic, e -> showQuestionsDialog(topic));
                topicBtn.setMaxWidth(Double.MAX_VALUE);
                topicsBox.getChildren().add(topicBtn);
            }
        }
    }

    public static VBox createQuestionBankPane(List<String> topicsList) {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.getStyleClass().add("question-bank-pane");

        Label title = new Label("Question Bank");
        title.getStyleClass().add("section-title");

        ComboBox<String> subjectDrop = new ComboBox<>(FXCollections.observableArrayList("DSA", "Others"));
        subjectDrop.setPromptText("Select Subject");

        VBox dynamicContent = new VBox(10);
        ScrollPane dynamicScrollPane = new ScrollPane(dynamicContent);
        dynamicScrollPane.setFitToWidth(true);
        dynamicScrollPane.setPrefHeight(400);

        Button backBtn = createStyledButton("Back to Home", e -> {
            if (pane.getParent() != null) {
                ((StackPane) pane.getParent()).getChildren().forEach(n -> n.setVisible(false));
                welcomeScreen.setVisible(true);
            }
        });
        pane.getChildren().addAll(title, subjectDrop, dynamicScrollPane, backBtn);

        subjectDrop.setOnAction(e -> {
            dynamicContent.getChildren().clear();
            String selected = subjectDrop.getValue();
            if ("DSA".equals(selected)) {
                if (topicsList.isEmpty()) {
                    dynamicContent.getChildren().add(new Label("No topics found in Syllabus."));
                } else {
                    for (String topic : topicsList) {
                        Button topicBtn = createColorfulButton(topic, ev -> showQuestionsForTopic(topic, dynamicContent, subjectDrop));
                        dynamicContent.getChildren().add(topicBtn);
                    }
                }
            } else if ("Others".equals(selected)) {
                dynamicContent.getChildren().add(new Label("No topics available for 'Others'."));
            }
        });

        return pane;
    }

    public static VBox createHelpPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));
        Label title = new Label("Help");
        title.getStyleClass().add("section-title");
        TextArea area = new TextArea();
        area.setEditable(false);
        try { area.setText(Files.readString(Paths.get("resources/help.txt"))); } catch (IOException ex) {}
        area.setWrapText(true);
        area.setPrefHeight(400);

        ScrollPane helpScrollPane = new ScrollPane(area);
        helpScrollPane.setFitToWidth(true);
        helpScrollPane.setPrefHeight(400);

        Button backBtn = createStyledButton("Back to Home", e -> {
            if (pane.getParent() != null) {
                ((StackPane) pane.getParent()).getChildren().forEach(n -> n.setVisible(false));
                welcomeScreen.setVisible(true);
            }
        });
        pane.getChildren().addAll(title, helpScrollPane, backBtn);
        return pane;
    }

    public static VBox createStatisticsPane(QuestionAnalyser analyser, String logPath) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("stats-pane");
        Label title = new Label("Analysis Statistics");
        title.getStyleClass().add("section-title");
        int total = ResourceManager.readLogQuestions(logPath).size();
        Label totalLbl = new Label("Total Questions Found: " + total);
        Label topicsLbl = new Label("Topics Identified: " + String.join(", ", analyser.getTopics()));
        box.getChildren().addAll(title, totalLbl, topicsLbl);
        return box;
    }

    public static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private static void showQuestionsDialog(String topic) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Questions for: " + topic);
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);

        InputStream is = UIComponents.class.getResourceAsStream("/" + topic + ".txt");
        if (is == null) {
            content.getChildren().add(new Label("No questions found for this topic."));
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                List<String> questions = reader.lines().toList();
                if (questions.isEmpty()) {
                    content.getChildren().add(new Label("No questions found for this topic."));
                } else {
                    for (String q : questions) {
                        Label qLabel = new Label("• " + q);
                        content.getChildren().add(qLabel);
                    }
                }
            } catch (IOException ex) {
                content.getChildren().add(new Label("Error loading questions."));
            }
        }

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static void showQuestionsForTopic(String topic, VBox container, ComboBox<String> subjectDrop) {
        container.getChildren().clear();
        Label topicLabel = new Label("Questions for: " + topic);
        topicLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
        Button backBtn = createStyledButton("Back", e -> subjectDrop.getOnAction().handle(null));
        container.getChildren().addAll(topicLabel, backBtn);

        InputStream is = UIComponents.class.getResourceAsStream("/" + topic + ".txt");
        if (is == null) {
            container.getChildren().add(new Label("No questions found for this topic."));
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            List<String> questions = reader.lines().toList();
            if (questions.isEmpty()) {
                container.getChildren().add(new Label("No questions found for this topic."));
            } else {
                for (String q : questions) {
                    Label qLabel = new Label("• " + q);
                    container.getChildren().add(qLabel);
                }
            }
        } catch (IOException ex) {
            container.getChildren().add(new Label("Error loading questions."));
        }
    }

    private static void showPane(Node currentPane, Node targetPane) {
        currentPane.getParent().getChildrenUnmodifiable().forEach(n -> n.setVisible(false));
        targetPane.setVisible(true);
    }
} 