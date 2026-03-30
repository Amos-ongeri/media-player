module org.amos.mediaplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;


    opens org.amos.mediaplayer to javafx.fxml;
    exports org.amos.mediaplayer;
    exports controller;
    opens controller to javafx.fxml;
}