package com.supreme.SmartQuestionAnalyser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class to analyze questions, classify them into topics, manage a question bank,
 * and recommend practice questions based on identified topics.
 */
public class QuestionAnalyser {
    // Stores questions from training.txt to avoid duplicates
    private final Set<String> trainingQuestions;
    // Maps each topic to a list of keywords for classification
    private final Map<String, List<String>> topicToKeywordsMap;
    // Stores unique topics found during analysis
    private List<String> uniqueTopicsList;

    /**
     * Initializes the analyser by loading training questions and topic keywords.
     */
    public QuestionAnalyser() {
        trainingQuestions = new HashSet<>();
        topicToKeywordsMap = new HashMap<>();
        uniqueTopicsList = new ArrayList<>();
        loadTrainingQuestions();
        loadTopicKeywords();
    }

    /**
     * Loads questions from training.txt into the trainingQuestions set.
     */
    private void loadTrainingQuestions() {
        File trainingFile = new File("src/main/resources/training.txt");
        if (!trainingFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(trainingFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    trainingQuestions.add(trimmedLine);
                }
            }
        } catch (IOException exception) {
            // Silently handle errors to keep functionality intact
        }
    }

    /**
     * Loads topic-to-keyword mappings from topic_keywords.txt.
     */
    private void loadTopicKeywords() {
        File keywordsFile = new File("src/main/resources/topic_keywords.txt");
        if (!keywordsFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(keywordsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }
                String[] parts = trimmedLine.split(":");
                if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    continue;
                }
                String topic = parts[0].trim();
                String[] keywordsArray = parts[1].split(",");
                List<String> keywordsList = new ArrayList<>();
                for (String keyword : keywordsArray) {
                    String trimmedKeyword = keyword.trim();
                    if (!trimmedKeyword.isEmpty()) {
                        keywordsList.add(trimmedKeyword);
                    }
                }
                topicToKeywordsMap.put(topic, keywordsList);
            }
        } catch (IOException exception) {
            // Silently handle errors
        }
    }

    /**
     * Analyzes questions from log.txt, classifies them, updates files, and logs topics.
     */
    public void analyze() {
        List<String> questions = readQuestionsFromFile("src/main/resources/log.txt");
        Set<String> uniqueTopics = new TreeSet<>(); // Use TreeSet for sorted topics

        for (String question : questions) {
            List<String> matchedTopics = findTopicsForQuestion(question);
            uniqueTopics.addAll(matchedTopics);

            // Add new questions to training and topic files
            if (!trainingQuestions.contains(question)) {
                appendQuestionToTrainingFile(question);
                for (String topic : matchedTopics) {
                    appendQuestionToTopicFile(question, topic);
                }
                trainingQuestions.add(question);
            }
        }

        writeTopicsToLogFile(uniqueTopics);
        uniqueTopicsList = new ArrayList<>(uniqueTopics);
    }

    /**
     * Reads questions from a specified file.
     * @param filePath Path to the file
     * @return List of questions
     */
    private List<String> readQuestionsFromFile(String filePath) {
        List<String> questions = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return questions;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    questions.add(trimmedLine);
                }
            }
        } catch (IOException exception) {
            // Silently handle errors
        }
        return questions;
    }

    /**
     * Finds topics for a question by matching keywords.
     * @param question The question to classify
     * @return List of topics that match the question
     */
    private List<String> findTopicsForQuestion(String question) {
        List<String> matchedTopics = new ArrayList<>();
        String questionLowerCase = question.toLowerCase();

        for (Map.Entry<String, List<String>> entry : topicToKeywordsMap.entrySet()) {
            String topic = entry.getKey();
            List<String> keywords = entry.getValue();
            for (String keyword : keywords) {
                if (questionLowerCase.contains(keyword.toLowerCase())) {
                    matchedTopics.add(topic);
                    break; // One keyword match is enough for the topic
                }
            }
        }
        return matchedTopics;
    }

    /**
     * Appends a question to training.txt.
     * @param question The question to add
     */
    private void appendQuestionToTrainingFile(String question) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/training.txt", true))) {
            writer.write(question);
            writer.newLine();
        } catch (IOException exception) {
            // Silently handle errors
        }
    }

    /**
     * Appends a question to a topic-specific file.
     * @param question The question to add
     * @param topic The topic file name
     */
    private void appendQuestionToTopicFile(String question, String topic) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/" + topic + ".txt", true))) {
            writer.write(question);
            writer.newLine();
        } catch (IOException exception) {
            // Silently handle errors
        }
    }

    /**
     * Writes unique topics to topics_log.txt.
     * @param topics Set of topics to write
     */
    private void writeTopicsToLogFile(Set<String> topics) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/topics_log.txt"))) {
            for (String topic : topics) {
                writer.write(topic);
                writer.newLine();
            }
        } catch (IOException exception) {
            // Silently handle errors
        }
    }

    /**
     * Returns the list of unique topics found during analysis.
     * @return List of topics
     */
    public List<String> getTopics() {
        return uniqueTopicsList != null ? new ArrayList<>(uniqueTopicsList) : new ArrayList<>();
    }

    /**
     * Retrieves up to a specified number of questions from a topic file.
     * @param topic The topic (e.g., "arrays")
     * @param count Maximum number of questions to retrieve
     * @return List of questions
     */
    public List<String> getSimilarQuestions(String topic, int count, Set<String> skipQuestions) {
        List<String> questions = readQuestionsFromFile("src/main/resources/" + topic + ".txt");
        List<String> filtered = questions.stream()
            .filter(q -> !skipQuestions.contains(q))
            .collect(Collectors.toList());

        if (filtered.size() <= count) return filtered;
        return filtered.subList(0, count);
    }

    /**
     * Recommends up to 5 practice questions for each topic in topics_log.txt or uniqueTopicsList.
     */
    public void recommendQuestions() {
        List<String> topics = readTopicsFromFile("src/main/resources/topics_log.txt");
        if (topics.isEmpty() && uniqueTopicsList != null) {
            topics = new ArrayList<>(uniqueTopicsList);
        }

        if (topics.isEmpty()) {
            System.out.println("No topics available for recommendation.");
            return;
        }

        System.out.println("Recommended Practice Questions:");
        for (String topic : topics) {
            List<String> questions = getSimilarQuestions(topic, 5);
            System.out.println("\nTopic: " + topic);
            if (questions.isEmpty()) {
                System.out.println("  No questions available for this topic.");
            } else {
                for (int i = 0; i < questions.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + questions.get(i));
                }
            }
        }
    }
    public List<String> getSimilarQuestionsExcluding(String topic, int count, Set<String> excludeQuestions) {
        List<String> all = readQuestionsFromFile("src/main/resources/" + topic + ".txt");

        List<String> filtered = all.stream()
            .filter(q -> !excludeQuestions.contains(q.trim()))
            .collect(Collectors.toList());

        if (filtered.size() <= count) return filtered;
        return filtered.subList(0, count);
    }

    /**
     * Reads topics from a specified file.
     * @param filePath Path to the topics file
     * @return List of topics
     */
    private List<String> readTopicsFromFile(String filePath) {
        List<String> topics = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return topics;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    topics.add(trimmedLine);
                }
            }
        } catch (IOException exception) {
            // Silently handle errors
        }
        return topics;
    }

    /**
     * Main method to run the question analyser and recommend questions.
     * @param args Command-line arguments (unused)
     */
    public static void main(String[] args) {
        QuestionAnalyser analyser = new QuestionAnalyser();
        analyser.analyze();
        System.out.println("Topics:");
        for (String topic : analyser.getTopics()) {
            System.out.println(topic);
        }
        analyser.recommendQuestions();
    }
}
