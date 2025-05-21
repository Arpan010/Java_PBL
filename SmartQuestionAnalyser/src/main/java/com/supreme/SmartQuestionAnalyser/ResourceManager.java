package com.supreme.SmartQuestionAnalyser;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ResourceManager {
    private static final String SYLLABUS_PATH = "resources/syllabus.txt";
    private static final String LOG_PATH = "resources/log.txt";
    private static final String TRAINING_PATH = "resources/training.txt";
    private static final String KEYWORDS_PATH = "resources/topic_keywords.txt";

    public static void ensureResourceFiles() {
        try {
            Files.createDirectories(Paths.get("resources/topics"));
            createIfNotExists(TRAINING_PATH);
            createIfNotExists(KEYWORDS_PATH);
            createIfNotExists(SYLLABUS_PATH);
            createIfNotExists(LOG_PATH);
        } catch (IOException ignored) {}
    }

    private static void createIfNotExists(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            Files.createDirectories(p.getParent());
            Files.createFile(p);
        }
    }

    public static Set<String> readLogQuestions(String pathStr) {
        Set<String> set = new HashSet<>();
        Path p = Paths.get(pathStr);
        if (Files.exists(p)) {
            try (BufferedReader r = Files.newBufferedReader(p)) {
                String l;
                while ((l = r.readLine()) != null) set.add(l.trim());
            } catch (IOException ignored) {}
        }
        return set;
    }

    public static void saveSyllabus(List<String> topicsList) {
        try {
            Files.writeString(Paths.get(SYLLABUS_PATH), String.join(",", topicsList));
        } catch (IOException ignored) {}
    }
} 