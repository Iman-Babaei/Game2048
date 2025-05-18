package com.akbar.game2048;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

    public class Main extends Application {
    private VBox root;
    @Override
    public void start(Stage stage) throws IOException {
        root = new VBox();
        root.setSpacing(6);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());
        stage.setTitle("Game2048");

        Tile tile2 = Tile.newTile(2);
        Tile tile4 = Tile.newTile(4);
        root.getChildren().addAll(tile2, tile4);

        stage.setScene(scene);
        stage.setMinHeight(500);
        stage.setMinWidth(500);
        stage.setHeight(500);
        stage.setWidth(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}