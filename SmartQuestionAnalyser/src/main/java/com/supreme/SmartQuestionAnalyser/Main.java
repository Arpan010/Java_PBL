package com.supreme.SmartQuestionAnalyser;
// Main Application
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.util.stream.Collectors;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {

    private static final String SYLLABUS_PATH = "resources/syllabus.txt";
    private static final String LOG_PATH = "resources/log.txt";
    private static final String TRAINING_PATH = "resources/training.txt";
    private static final String KEYWORDS_PATH = "resources/topic_keywords.txt";

    private BorderPane root;
    private StackPane contentPane;
    private VBox welcomeScreen;
    private ScrollPane resultsScrollPane;
    private TabPane resultsTabPane;
    private VBox topicsManagerPane;
    private VBox questionBankPane;
    private VBox helpPane;
    private ProgressIndicator bottomSpinner;
    private Label statusLabel;

    private List<String> topicsList = new ArrayList<>();
    private Set<String> analysedTopics = new LinkedHashSet<>();
    private boolean analysisDone = false;

    @Override
    public void start(Stage primaryStage) {
        ResourceManager.ensureResourceFiles();
        loadSyllabusTopics();

        root = new BorderPane();
        root.setTop(UIComponents.createHeader());
        root.setLeft(createSidebar());
        root.setCenter(createContentPane());
        root.setBottom(UIComponents.createFooter());

        Scene scene = new Scene(root, 1000, 700);
        URL css = getClass().getResource("/styles/application.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        primaryStage.setTitle("Smart Question Analyser");
        primaryStage.setScene(scene);
        primaryStage.show();
        UIComponents.playFadeIn(root);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_CENTER);

        Button uploadButton = UIComponents.createStyledButton("Upload PDF", e -> handleUploadButton());
        Button topicsButton = UIComponents.createStyledButton("Manage Topics", e -> showPane(topicsManagerPane));
        Button bankButton = UIComponents.createStyledButton("Question Bank", e -> showPane(questionBankPane));
        Button helpButton = UIComponents.createStyledButton("Help", e -> showPane(helpPane));
        Button resetButton = UIComponents.createStyledButton("Reset", e -> showPane(welcomeScreen));

        sidebar.getChildren().addAll(uploadButton, topicsButton, bankButton, helpButton, resetButton);

        statusLabel = new Label("Ready");
        bottomSpinner = new ProgressIndicator();
        bottomSpinner.setVisible(false);

        VBox statusBox = new VBox(5, statusLabel, bottomSpinner);
        statusBox.setAlignment(Pos.CENTER);
        sidebar.getChildren().add(statusBox);

        return sidebar;
    }

    private StackPane createContentPane() {
        contentPane = new StackPane();
        contentPane.getStyleClass().add("content-pane");

        welcomeScreen = UIComponents.createWelcomeScreen(e -> handleUploadButton());
        topicsManagerPane = UIComponents.createTopicsManagerPane(topicsList, analysedTopics, analysisDone);
        questionBankPane = UIComponents.createQuestionBankPane(topicsList);
        helpPane = UIComponents.createHelpPane();

        resultsTabPane = new TabPane();
        resultsScrollPane = new ScrollPane(resultsTabPane);
        resultsScrollPane.setFitToWidth(true);

        contentPane.getChildren().addAll(
                welcomeScreen,
                topicsManagerPane,
                questionBankPane,
                helpPane,
                resultsScrollPane
        );
        showPane(welcomeScreen);
        return contentPane;
    }

    private void handleUploadButton() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files","*.pdf"));
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            setStatus("Processing PDF...");
            bottomSpinner.setVisible(true);
            CompletableFuture.runAsync(() -> {
                App app = new App();
                app.processPDF(f.getPath(), LOG_PATH);
                QuestionAnalyser analyser = new QuestionAnalyser();
                analyser.analyze();
                analysedTopics.clear();
                analysedTopics.addAll(analyser.getTopics());
                analysisDone = true;
                javafx.application.Platform.runLater(() -> {
                    bottomSpinner.setVisible(false);
                    setStatus("Analysis Complete");
                    topicsManagerPane = UIComponents.createTopicsManagerPane(topicsList, analysedTopics, analysisDone);
                    contentPane.getChildren().set(1, topicsManagerPane);
                    displayResults(analyser);
                });
            });
        }
    }

    private void displayResults(QuestionAnalyser analyser) {
        resultsTabPane.getTabs().clear();
        resultsTabPane.getTabs().add(new Tab("Statistics", UIComponents.createStatisticsPane(analyser, LOG_PATH)));
        Set<String> excl = ResourceManager.readLogQuestions(LOG_PATH);
        for (String topic : analyser.getTopics()) {
            VBox pane = new VBox(10);
            analyser.getSimilarQuestionsExcluding(topic, 5, excl)
                    .forEach(q -> pane.getChildren().add(new Label("• " + q)));
            ScrollPane topicScroll = new ScrollPane(pane);
            topicScroll.setFitToWidth(true);
            resultsTabPane.getTabs().add(new Tab(topic, topicScroll));
        }
        Button expBtn = new Button("Export Results");
        expBtn.setOnAction(e -> exportResults(analyser));
        resultsTabPane.getTabs().add(new Tab("Export", new VBox(expBtn)));
        showPane(resultsScrollPane);
    }

    private void loadSyllabusTopics() {
        topicsList.clear();
        try (InputStream is = getClass().getResourceAsStream("/syllabus.txt")) {
            if (is == null) {
                System.err.println("syllabus.txt resource not found!");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String content = reader.lines().collect(Collectors.joining());
            String[] topics = content.split(",");
            for (String topic : topics) {
                topic = topic.trim();
                if (!topic.isEmpty()) {
                    topicsList.add(topic);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportResults(QuestionAnalyser analyser) {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get("analysis_results.txt"))) {
            w.write("Analysis Results\n");
            w.write("Topics: " + String.join(", ", analyser.getTopics()) + "\n\n");
            Set<String> excl = ResourceManager.readLogQuestions(LOG_PATH);
            for (String topic : analyser.getTopics()) {
                w.write("Topic: " + topic + "\n");
                List<String> qs = analyser.getSimilarQuestionsExcluding(topic, 5, excl);
                for (String q : qs) w.write("- " + q + "\n");
                w.write("\n");
            }
            UIComponents.showInfo("Export Successful", "Results saved to analysis_results.txt");
        } catch (IOException e) {
            UIComponents.showError("Failed to export results: " + e.getMessage());
        }
    }

    private void setStatus(String s) {
        statusLabel.setText(s);
    }

    private void showPane(Node pane) {
        contentPane.getChildren().forEach(n -> n.setVisible(false));
        pane.setVisible(true);
        if (pane == topicsManagerPane) {
            topicsManagerPane = UIComponents.createTopicsManagerPane(topicsList, analysedTopics, analysisDone);
            contentPane.getChildren().set(1, topicsManagerPane);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
