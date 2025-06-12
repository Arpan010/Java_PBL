package smart_app_0.smart_analyse.models;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private int id;
    private String questionText;
    private String answer;
    private List<String> categories;
    
    public Question() {
        this.categories = new ArrayList<>();
    }
    
    public Question(String questionText) {
        this.questionText = questionText;
        this.categories = new ArrayList<>();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    
    public void addCategory(String category) {
        if (!this.categories.contains(category)) {
            this.categories.add(category);
        }
    }
    
    @Override
    public String toString() {
        return questionText;
    }
}
