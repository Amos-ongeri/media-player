module org.amos.mediaplayer {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.amos.mediaplayer to javafx.fxml;
    exports org.amos.mediaplayer;
    exports controller;
    opens controller to javafx.fxml;
}