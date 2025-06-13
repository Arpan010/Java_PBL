package com.smartapp.smartanalyser.services;

import com.smartapp.smartanalyser.models.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "pbl_jdbc";
    private static final String USER = "Arpan";
    private static final String PASSWORD = "root";
    
    public void initializeDatabase() {
        try {
            // Create database if not exists
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);
            
            // Create questions table - removed answer column
            String createQuestionsTable = "CREATE TABLE IF NOT EXISTS questions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "question_text TEXT NOT NULL, " +
                    "topics_found TEXT NOT NULL)";
            stmt.executeUpdate(createQuestionsTable);
            
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveQuestion(Question question) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD);
            
            // Check if question already exists
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM questions WHERE question_text = ?");
            checkStmt.setString(1, question.getQuestionText());
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Question exists, update topics only
                int id = rs.getInt("id");
                question.setId(id);
                
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE questions SET topics_found = ? WHERE id = ?");
                updateStmt.setString(1, String.join(",", question.getCategories()));
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                System.out.println("Updated existing question with ID: " + id);
            } else {
                // Insert new question (only question_text and topics_found)
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO questions (question_text, topics_found) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, question.getQuestionText());
                insertStmt.setString(2, String.join(",", question.getCategories()));
                insertStmt.executeUpdate();
                
                // Get generated ID
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
                    System.out.println("Inserted new question with ID: " + question.getId());
                }
                
                insertStmt.close();
            }
            
            rs.close();
            checkStmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM questions");
            
            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setQuestionText(rs.getString("question_text"));
                // No answer field - only topics
                
                String topicsStr = rs.getString("topics_found");
                if (topicsStr != null && !topicsStr.isEmpty()) {
                    List<String> topics = Arrays.asList(topicsStr.split(","));
                    question.setCategories(topics);
                }
                
                questions.add(question);
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return questions;
    }
    
    public Question getQuestionById(int id) {
        Question question = null;
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM questions WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                question = new Question();
                question.setId(rs.getInt("id"));
                question.setQuestionText(rs.getString("question_text"));
                // No answer field
                
                String topicsStr = rs.getString("topics_found");
                if (topicsStr != null && !topicsStr.isEmpty()) {
                    List<String> topics = Arrays.asList(topicsStr.split(","));
                    question.setCategories(topics);
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return question;
    }
    
    // Additional method to get questions by topic
    public List<Question> getQuestionsByTopic(String topic) {
        List<Question> questions = new ArrayList<>();
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM questions WHERE topics_found LIKE ?");
            stmt.setString(1, "%" + topic + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setQuestionText(rs.getString("question_text"));
                
                String topicsStr = rs.getString("topics_found");
                if (topicsStr != null && !topicsStr.isEmpty()) {
                    List<String> topics = Arrays.asList(topicsStr.split(","));
                    question.setCategories(topics);
                }
                
                questions.add(question);
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return questions;
    }
    
    // Method to get all unique topics
    public List<String> getAllTopics() {
        List<String> allTopics = new ArrayList<>();
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT topics_found FROM questions");
            
            while (rs.next()) {
                String topicsStr = rs.getString("topics_found");
                if (topicsStr != null && !topicsStr.isEmpty()) {
                    String[] topics = topicsStr.split(",");
                    for (String topic : topics) {
                        String trimmedTopic = topic.trim();
                        if (!allTopics.contains(trimmedTopic)) {
                            allTopics.add(trimmedTopic);
                        }
                    }
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return allTopics;
    }
}