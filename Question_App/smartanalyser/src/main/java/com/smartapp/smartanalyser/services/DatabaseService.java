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
            
            // Create questions table
            String createQuestionsTable = "CREATE TABLE IF NOT EXISTS questions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "question_text TEXT NOT NULL, " +
                    "answer TEXT, " +
                    "topics_found TEXT)";
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
                // Question exists, update it
                int id = rs.getInt("id");
                question.setId(id);
                
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE questions SET answer = ?, topics_found = ? WHERE id = ?");
                updateStmt.setString(1, question.getAnswer());
                updateStmt.setString(2, String.join(",", question.getCategories()));
                updateStmt.setInt(3, id);
                updateStmt.executeUpdate();
                updateStmt.close();
            } else {
                // Insert new question
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO questions (question_text, answer, topics_found) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, question.getQuestionText());
                insertStmt.setString(2, question.getAnswer());
                insertStmt.setString(3, String.join(",", question.getCategories()));
                insertStmt.executeUpdate();
                
                // Get generated ID
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
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
                question.setAnswer(rs.getString("answer"));
                
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
                question.setAnswer(rs.getString("answer"));
                
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
}
