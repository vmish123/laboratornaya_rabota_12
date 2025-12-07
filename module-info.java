module org.example.javafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafx to javafx.fxml;
    exports org.example.javafx;

    opens org.example.javafx.FirstTask to javafx.fxml;
    exports org.example.javafx.FirstTask;

    opens org.example.javafx.SecondTask to javafx.fxml;
    exports org.example.javafx.SecondTask;

    opens org.example.javafx.ThirdTask to javafx.fxml;
    exports org.example.javafx.ThirdTask;
}
