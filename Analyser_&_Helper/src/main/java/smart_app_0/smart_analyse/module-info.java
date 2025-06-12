module smart_app_0.smart_analyse {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires org.apache.pdfbox;
    
    opens smart_app_0.smart_analyse to javafx.fxml;
    opens smart_app_0.smart_analyse.controllers to javafx.fxml;
    
    exports smart_app_0.smart_analyse;
    exports smart_app_0.smart_analyse.controllers;
    exports smart_app_0.smart_analyse.models;
    exports smart_app_0.smart_analyse.services;
}
