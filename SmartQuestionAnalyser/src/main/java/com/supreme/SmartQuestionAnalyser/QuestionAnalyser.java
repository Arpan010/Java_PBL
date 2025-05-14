package com.supreme.SmartQuestionAnalyser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class QuestionAnalyser {
    private Set<String> trainingSet;               // Stores existing questions from training.txt
    private Map<String, List<String>> topicKeywords; // Maps topics to their keywords
    private List<String> topics;                   // Stores unique topics found in log.txt

    /**
     * Constructor: Loads training.txt and topic_keywords.txt to initialize the analyser.
     * Assumes files are in a 'resources' folder in the project root.
     */
    public QuestionAnalyser() {
        // Initialize training set
        trainingSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("../resources/training.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                trainingSet.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading training.txt: " + e.getMessage());
        }

        // Initialize topic keywords
        topicKeywords = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("../resources/topic_keywords.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String topic = parts[0].trim();
                    List<String> keywords = Arrays.asList(parts[1].split(","));
                    topicKeywords.put(topic, keywords.stream().map(String::trim).collect(Collectors.toList()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading topic_keywords.txt: " + e.getMessage());
        }
    }

    /**
     * Performs the analysis: Reads log.txt, classifies questions, updates question bank,
     * extracts unique topics, and writes them to topics_log.txt.
     */
    public void analyze() {
        List<String> questions = readQuestions("../resources/log.txt");
        Set<String> uniqueTopics = new HashSet<>();

        for (String question : questions) {
            List<String> questionTopics = classifyQuestion(question);
            uniqueTopics.addAll(questionTopics);

            // Add unique questions to training.txt and topic files (optional)
            if (!trainingSet.contains(question)) {
                addToTraining(question);
                for (String topic : questionTopics) {
                    addToTopicFile(question, topic);
                }
                trainingSet.add(question);
            }
        }

        // Write unique topics to topics_log.txt
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("../resources/topics_log.txt"))) {
            for (String topic : uniqueTopics) {
                bw.write(topic);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to topics_log.txt: " + e.getMessage());
        }

        topics = new ArrayList<>(uniqueTopics);
    }

    /**
     * Reads questions from a specified file.
     * @param filePath Path to the file (e.g., "resources/log.txt")
     * @return List of questions
     */
    private List<String> readQuestions(String filePath) {
        List<String> questions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                questions.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
        }
        return questions;
    }

    /**
     * Classifies a question into topics based on keyword matching.
     * @param question The question to classify
     * @return List of topics the question belongs to
     */
    private List<String> classifyQuestion(String question) {
        List<String> questionTopics = new ArrayList<>();
        String lowerQuestion = question.toLowerCase();
        for (Map.Entry<String, List<String>> entry : topicKeywords.entrySet()) {
            String topic = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (lowerQuestion.contains(keyword.toLowerCase())) {
                    questionTopics.add(topic);
                    break; // One keyword match per topic is sufficient
                }
            }
        }
        return questionTopics;
    }

    /**
     * Appends a question to training.txt.
     * @param question The question to add
     */
    private void addToTraining(String question) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("../resources/training.txt", true))) {
            bw.write(question);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to training.txt: " + e.getMessage());
        }
    }

    /**
     * Appends a question to the specified topic file.
     * @param question The question to add
     * @param topic The topic file name (e.g., "array")
     */
    private void addToTopicFile(String question, String topic) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("../resources/" + topic + ".txt", true))) {
            bw.write(question);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to " + topic + ".txt: " + e.getMessage());
        }
    }

    /**
     * Returns the list of unique topics found during analysis.
     * @return List of topics
     */
    public List<String> getTopics() {
        return topics != null ? topics : new ArrayList<>();
    }

    /**
     * Retrieves up to 'count' questions from the specified topic file.
     * @param topic The topic (e.g., "array")
     * @param count Number of questions to retrieve (e.g., 10)
     * @return List of questions
     */
    public List<String> getSimilarQuestions(String topic, int count) {
        List<String> questions = readQuestions("../resources/" + topic + ".txt");
        if (questions.size() <= count) {
            return questions;
        } else {
            return questions.subList(0, count); // Returns first 'count' questions
        }
    }
}