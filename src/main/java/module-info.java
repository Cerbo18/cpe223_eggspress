module com.eggspress {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    
    requires java.sql;

    opens com.eggspress to javafx.fxml;
    opens com.eggspress.controllers to javafx.fxml;

    exports com.eggspress;
    exports com.eggspress.controllers;
    exports com.eggspress.models;
    exports com.eggspress.repository;
    exports com.eggspress.config;
}
