package com.akbar.game2048;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;


public class GamePane extends StackPane {
    private GameManager gameManager;
    private Bounds gameBounds;
    private final static int MARGIN=36;
    static {
//        Font.loadFont("")
    }

    public GamePane() {
        gameManager=new GameManager();
        gameManager.setToolBar(createToolBar());
        gameBounds=gameManager.getLayoutBounds();

        getChildren().add(gameManager);

        getStyleClass().addAll("game-root");
        ChangeListener<Number> resize=(observable, oldValue, newValue) -> {
            final double scale = Math.min((getWidth() - MARGIN) / gameBounds.getWidth(), (getHeight() - MARGIN) / gameBounds.getHeight());
//            gameManager.setScale(scale);
            gameManager.setLayoutX((getWidth()-gameBounds.getWidth())/2d);
            gameManager.setLayoutY((getHeight()-gameBounds.getHeight())/2d);
        };
        widthProperty().addListener(resize);
        heightProperty().addListener(resize);

        addKeyHandler(this);
        addSwipeHandlers(this);
//        setFocusTraversable(true);

//        this.setOnMouseClicked(event -> requestFocus());
    }

    private void addSwipeHandlers(Node node) {
        node.setOnSwipeUp(event -> gameManager.move(Direction.UP));
        node.setOnSwipeDown(event -> gameManager.move(Direction.DOWN));
        node.setOnSwipeLeft(event -> gameManager.move(Direction.LEFT));
        node.setOnSwipeRight(event -> gameManager.move(Direction.RIGHT));
    }

    private void addKeyHandler(Node node) {
        node.setOnKeyPressed(event -> {
            final KeyCode keyCode = event.getCode();
            //todo do this with switch
            if (keyCode.equals(KeyCode.S)){
                gameManager.saveSession();
                return;
            }
            if (keyCode.equals(KeyCode.R)){
                gameManager.restoreSession();
                return;
            }
            if (keyCode.equals(KeyCode.P)){
                gameManager.pauseGame();
                return;
            }
            if (keyCode.equals(KeyCode.Q) || keyCode.equals(KeyCode.ESCAPE)){
                gameManager.quitGame();
                return;
            }
            if (keyCode.isArrowKey()){
                final Direction direction = Direction.valueFor(keyCode);
                gameManager.move(direction);
            }
        });
    }

    private HBox createToolBar() {
        final HBox toolbar = new HBox();
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setPadding(new Insets(10));
        Button btnItem1=createButtonItem("msave","Save session", t->gameManager.saveSession());
        Button btnItem2=createButtonItem("mRestore","Restore Session",t->gameManager.restoreSession());
        Button btnItem3=createButtonItem("mPause","Pause Game",t->gameManager.pauseGame());
        Button btnItem4=createButtonItem("mReplay","Try Again",t->gameManager.tryAgain());
        Button btnItem5=createButtonItem("mInfo","About the game",t->gameManager.aboutGame());
        Button btnItem6=createButtonItem("mQuit","Exit game",event -> gameManager.quitGame());
        toolbar.getChildren().setAll(btnItem1,btnItem2,btnItem3,btnItem4,btnItem5,btnItem6);
        return toolbar;
    }

    private Button createButtonItem(String symbol, String text, EventHandler<ActionEvent> t) {
        Button button=new Button();
        button.setPrefSize(40,40);
        button.setId(symbol);
        button.setOnAction(t);
        button.setTooltip(new Tooltip(text));
        return button;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
    public static int getMargin(){
        return MARGIN;
    }
}