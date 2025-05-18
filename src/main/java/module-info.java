module com.akbar.game2048 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.akbar.game2048 to javafx.fxml;
    exports com.akbar.game2048;
}