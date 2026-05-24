module cpe223.group8.eggspress {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    
    requires java.sql;
    requires java.base;

    opens cpe223.group8.eggspress to javafx.fxml;
    opens cpe223.group8.eggspress.controllers to javafx.fxml;

    exports cpe223.group8.eggspress;
    exports cpe223.group8.eggspress.controllers;
    exports cpe223.group8.eggspress.models;
    exports cpe223.group8.eggspress.repository;
    exports cpe223.group8.eggspress.config;
}
