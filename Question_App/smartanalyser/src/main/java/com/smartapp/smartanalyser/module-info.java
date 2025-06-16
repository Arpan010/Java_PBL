module smartanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires java.desktop;
    
    requires org.apache.pdfbox;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    opens com.smartapp.smartanalyser to javafx.fxml;
    opens com.smartapp.smartanalyser.controllers to javafx.fxml;
    
    exports com.smartapp.smartanalyser;
    exports com.smartapp.smartanalyser.controllers;
    exports com.smartapp.smartanalyser.models;
    exports com.smartapp.smartanalyser.services;
}