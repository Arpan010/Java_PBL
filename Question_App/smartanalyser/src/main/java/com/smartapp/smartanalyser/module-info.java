
module smartanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires java.desktop;
    
    // Use automatic module names for non-modular dependencies
    requires org.apache.pdfbox;
    //requires json;
    
    opens com.smartapp.smartanalyser to javafx.fxml;
    opens com.smartapp.smartanalyser.controllers to javafx.fxml;
    
    exports com.smartapp.smartanalyser;
    exports com.smartapp.smartanalyser.controllers;
    exports com.smartapp.smartanalyser.models;
    exports com.smartapp.smartanalyser.services;
}