package com.akbar.game2048;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

public class Main2 extends Application {
    @Override
    public void start(Stage stage) throws Exception {


        GamePane root = new GamePane();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/game.css")).toExternalForm());
        stage.setTitle("Game2048");


        stage.setScene(scene);
        Bounds gameBounds = root.getGameManager().getLayoutBounds();
        int MARGIN = GamePane.getMargin();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double factor = Math.min(visualBounds.getWidth()/(gameBounds.getWidth()+MARGIN)
                , visualBounds.getHeight()/(gameBounds.getHeight()+MARGIN));
        stage.setMinHeight(gameBounds.getHeight()/2);
        stage.setMinWidth(gameBounds.getWidth()/2);
        stage.setHeight((gameBounds.getHeight()+MARGIN)*factor);
        stage.setWidth((gameBounds.getWidth()+MARGIN)*factor);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
