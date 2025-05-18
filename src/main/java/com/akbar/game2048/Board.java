package com.akbar.game2048;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Board extends Group {
    public static final int CELL_SIZE = 128;
    private static final int BORDER_WIDTH = (14 + 2) / 2;
    private static final int TOP_HEIGHT = 92;
    private static final int GAP_HEIGHT = 50;
    private static final int TOOLBAR_HEIGHT = 80;


    //MAKE THESE AFTER USER INTERFACE CREATION
    private final IntegerProperty gameScoreProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameBestProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final BooleanProperty gameWonProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameAboutProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gamePauseProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameTryAgainProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameSaveProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameRestoreProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty gameQuitProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty layerOnProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty resetGame = new SimpleBooleanProperty(false);
    private final LongProperty timerProperty = new SimpleLongProperty(0);

    private LocalTime time;
    private Timeline timer;
    private final StringProperty clock = new SimpleStringProperty("00:00:00");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());


    //user interface controls
    private final VBox vGame = new VBox(0);
    private final Group gridGroup = new Group();

    private final HBox hTop = new HBox(0);
    private final VBox vScore = new VBox(-5);
    private final Label lblScore = new Label("0");
    private final Label lblBest = new Label("0");
    private final Label lblPoints = new Label();


    private final Button tryButton = new Button("Try again");
    private final Button continueButton = new Button("Keep going");
    private final Button continueNoButton = new Button("No, Keep going");
    private final Button saveButton = new Button("Save");
    private final Button restoreButton = new Button("Restore");
    private final Button quitButton = new Button("Quit");

    private final HBox hToolbar = new HBox();
    private HostServices hostServices;

    private final Label lblTime = new Label();

    private final int gridWidth;
    private final GridOperator gridOperator;
//    private final SessionManager sessionManager;


    private HBox overlay = new HBox();

    private HBox buttonsOverlay = new HBox();

    public Board(GridOperator gridOperator) {
        this.gridOperator = gridOperator;
        gridWidth = CELL_SIZE * gridOperator.getGridSize() + BORDER_WIDTH * 2;
//        sessionManager = new SessionManager(gridOperator);

        createScore();
        createGrid();
        initGameProperties();
    }

    public void pauseTimer() {
        timerProperty.set(LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
    }
    public void resumeTimer(){
        timerProperty.set(0);
    }

    private void initGameProperties() {

        timerProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.longValue() > 0l) {
                timer.pause();
            } else if (newValue.longValue() == 0) {
                time = LocalTime.now().minusNanos(oldValue.longValue());
                timer.play();
            }
        });


        setButtonProperties(tryButton, () -> btnTryAgain());
        setButtonProperties(continueButton, () -> keepGoing());
        setButtonProperties(continueNoButton, () -> keepGoing());
        // setButtonProperties(saveButton, () -> saveSession());
        // setButtonProperties(restoreButton, () -> restoreSession());
        setButtonProperties(quitButton, () -> quit());


        gameWonProperty.addListener(new Overlay2("You win!", "", continueButton, tryButton, "game-overlay-won", "game-lblWon", true));
        gameOverProperty.addListener(new Overlay2("Game Over!", "", tryButton, null, "game-overlay-over", "game-lblOver", false));
        gamePauseProperty.addListener(new Overlay2("Game Paused!", "", continueButton, null, "game-overlay-pause", "game-lblPause", true));
        gameTryAgainProperty.addListener(new Overlay2("Try Again?", "Current game will be deleted", tryButton, continueNoButton, "game-overlay-pause", "game-lblPause", true));
        gameSaveProperty.addListener(new Overlay2("Game saved", "Previous saved data will be overwritten", continueButton, null, "game-overlay-pause", "game-lblPause", true));
        gameRestoreProperty.addListener(new Overlay2("game restored", "Current game will be deleted", continueButton, null, "game-overlay-pause", "game-lblPause", true));
        gameAboutProperty.addListener(new Overlay2("About game", "this game created by mehdi adeli", continueButton, null, "game-overlay-pause", "game-lblPause", true));

        gameQuitProperty.addListener(new Overlay2("Quit Game?", "Non saved data will be lost", quitButton, continueNoButton, "game-overlay-quit", "game-lblQuit", true));
        restoreRecord();

        gameScoreProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > gameBestProperty.get()) gameBestProperty.set(newValue.intValue());
        });
        layerOnProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                getChildren().removeAll(overlay, buttonsOverlay);
                getParent().requestFocus();
            } else if (newValue) buttonsOverlay.getChildren().get(0).requestFocus();
        });
    }

    private void restoreRecord() {
//        RecordManager recordManager = new RecordManager(gridOperator.getGridSize());
//        gameBestProperty.set(recordManager.restoreRecord());
    }

    private void quit() {
        Platform.exit();
    }

    private void setButtonProperties(Button continueButton, Runnable action) {
        KeyCode enter = KeyCode.ENTER;
        KeyCode space = KeyCode.SPACE;
        continueButton.getStyleClass().add("game-button");
        continueButton.setOnTouchPressed(event -> action.run());
        continueButton.setOnMouseClicked(e -> action.run());
        continueButton.setOnKeyPressed(e -> {
            if (e.getCode().equals(enter) || e.getCode().equals(space))
                action.run();
        });
    }

    private void keepGoing() {
        layerOnProperty.set(false);
        gamePauseProperty.set(false);
        gameTryAgainProperty.set(false);
        gameSaveProperty.set(false);
        gameRestoreProperty.set(false);
        gameAboutProperty.set(false);
        gameQuitProperty.set(false);
        resumeTimer();
    }

    private void btnTryAgain() {
        layerOnProperty.set(false);
        doResetGame();
    }

    private void doResetGame() {
        doClearGame();
        resetGame.set(true);
    }

    private void doClearGame() {
        gridGroup.getChildren().removeIf(c -> c instanceof Tile);
        getChildren().removeAll(overlay, buttonsOverlay);


        layerOnProperty.set(false);
        gameScoreProperty.set(0);
        gameWonProperty.set(false);
        gameOverProperty.set(false);
        gameAboutProperty.set(false);
        gamePauseProperty.set(false);
        gameTryAgainProperty.set(false);
        gameSaveProperty.set(false);
        gameRestoreProperty.set(false);
        gameQuitProperty.set(false);

    }

    public void saveRecord() {
        RecordManager recordManager = new RecordManager(gridOperator.getGridSize());
        recordManager.saveRecord(gameScoreProperty.getValue());
    }

    private void createGrid() {
        gridOperator.traverseGrid((i, j) -> {
            gridGroup.getChildren().add(createCell(i, j));
            return 0;
        });
        gridGroup.getStyleClass().add("game-grid");
        gridGroup.setManaged(false);
        gridGroup.setLayoutX(BORDER_WIDTH);
        gridGroup.setLayoutY(BORDER_WIDTH);

        HBox hBottom = new HBox();
        hBottom.getStyleClass().add("game-backGrid");
        hBottom.setMinSize(gridWidth, gridWidth);
        hBottom.setPrefSize(gridWidth, gridWidth);
        hBottom.setMaxSize(gridWidth, gridWidth);

        // Clip hBottom to keep the dropshadow effects within the hBottom
        Rectangle rectangle = new Rectangle(gridWidth, gridWidth);
        hBottom.setClip(rectangle);
        hBottom.getChildren().add(gridGroup);
        vGame.getChildren().add(hBottom);

        //toolbar
        HBox hPadding = new HBox();
        hPadding.setMinSize(gridWidth, TOOLBAR_HEIGHT);
        hPadding.setPrefSize(gridWidth, TOOLBAR_HEIGHT);
        hPadding.setMaxSize(gridWidth, TOOLBAR_HEIGHT);

        hToolbar.setAlignment(Pos.CENTER);
        hToolbar.getStyleClass().add("game-backGrid");
        hToolbar.setMinSize(gridWidth, TOOLBAR_HEIGHT);
        hToolbar.setPrefSize(gridWidth, TOOLBAR_HEIGHT);
        hToolbar.setMaxSize(gridWidth, TOOLBAR_HEIGHT);

        vGame.getChildren().add(hPadding);
        vGame.getChildren().add(hToolbar);
    }

    private Rectangle createCell(int i, int j) {
        final double arcSize = CELL_SIZE / 6d;
        Rectangle cell = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        cell.setFill(Color.WHITE);
        cell.setStroke(Color.GRAY);
        cell.setArcHeight(arcSize);
        cell.setArcWidth(arcSize);
        cell.getStyleClass().add("game-grid-cell");
        return cell;
    }

    private void createScore() {
        Label lblTitle = new Label("2048");
        lblTitle.getStyleClass().addAll("game-label", "game-title");
        Label lblSubtitle = new Label("Mehdi Adeli");
        lblSubtitle.getStyleClass().addAll("game-label", "game-subtitle");
        HBox hFill = new HBox();
        HBox.setHgrow(hFill, Priority.ALWAYS);
        hFill.setAlignment(Pos.CENTER);
        VBox vScores = new VBox();
        HBox hScores = new HBox(5);

        vScore.setAlignment(Pos.CENTER);
        vScore.getStyleClass().add("game-vbox");
        Label lblTit = new Label("SCORE");
        lblTit.getStyleClass().addAll("game-label", "game-titScore");
        lblScore.getStyleClass().addAll("game-label", "game-score");
        lblScore.textProperty().bind(gameScoreProperty.asString());
        vScore.getChildren().addAll(lblTit, lblScore);

        VBox vRecord = new VBox(-5);
        vRecord.setAlignment(Pos.CENTER);
        vRecord.getStyleClass().add("game-vbox");
        Label lblTitBest = new Label("BEST");
        lblTitBest.getStyleClass().addAll("game-label", "game-titScore");
        lblBest.getStyleClass().addAll("game-label", "game-score");
        lblBest.textProperty().bind(gameBestProperty.asString());
        vRecord.getChildren().addAll(lblTitBest, lblBest);
        hScores.getChildren().addAll(vScore, vRecord);
        VBox vFill = new VBox();
        VBox.setVgrow(vFill, Priority.ALWAYS);
        vScores.getChildren().addAll(hScores, vFill);

        hTop.getChildren().addAll(lblTitle, hFill, vScores);
        hTop.setMinSize(gridWidth, TOP_HEIGHT);
        hTop.setPrefSize(gridWidth, TOP_HEIGHT);
        hTop.setMaxSize(gridWidth, TOP_HEIGHT);

        vGame.getChildren().add(hTop);

        HBox htime = new HBox();
        htime.setMinSize(gridWidth, GAP_HEIGHT);
        htime.setAlignment(Pos.BOTTOM_RIGHT);
        lblTime.getStyleClass().addAll("game-label", "game-time");
        lblTime.textProperty().bind(clock);

        timer = new Timeline(new KeyFrame(Duration.ZERO, e -> clock.set(LocalTime.now().minusNanos(time.toNanoOfDay()).format(formatter))), new KeyFrame(Duration.seconds(1)));
        timer.setCycleCount(Animation.INDEFINITE);

        htime.getChildren().addAll(lblTime);

        vGame.getChildren().add(htime);
        getChildren().add(vGame);

        lblPoints.getStyleClass().addAll("game-label", "game-points");
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setMinWidth(100);
        getChildren().add(lblPoints);

    }


    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void setToolBar(HBox toolbar) {
        toolbar.disableProperty().bind(layerOnProperty);
        toolbar.spacingProperty().bind(Bindings.divide(vGame.widthProperty(), 10));
        hToolbar.getChildren().add(toolbar);
    }

    public void tryAgain() {
        if (!gameTryAgainProperty.get()) gameTryAgainProperty.set(true);
    }

    private final Overlay2 wonListener = new Overlay2("You win!", "", continueButton, tryButton, "game-overlay-won", "game-lblWon", true);

    public void animateScore() {
        if (gameMovePoints.get() == 0) return;
        final Timeline timeline = new Timeline();
        lblPoints.setText("+" + gameMovePoints.getValue().toString());
        lblPoints.setOpacity(1);
        double posX = vScore.localToScene(vScore.getWidth() / 2d, 0).getX();
        lblPoints.setTranslateX(0);
        lblPoints.setTranslateX(lblPoints.sceneToLocal(posX, 0).getX() - lblPoints.getWidth() / 2d);
        lblPoints.setLayoutY(20);
        final KeyValue kv0 = new KeyValue(lblPoints.opacityProperty(), 0);
        final KeyValue kvY = new KeyValue(lblPoints.layoutYProperty(), 100);

        Duration animationDuration = Duration.millis(600);
        final KeyFrame kf0 = new KeyFrame(animationDuration, kv0);
        final KeyFrame kfy = new KeyFrame(animationDuration, kvY);

        timeline.getKeyFrames().add(kf0);
        timeline.getKeyFrames().add(kfy);

        timeline.play();
    }

    public void addTile(Tile tile) {
        final double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        final double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);
        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        gridGroup.getChildren().add(tile);
    }

    public Tile addRandomTile(Location randomLocation) {
        final Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);
        final double layoutX = tile.getLocation().getLayoutX(CELL_SIZE) - (tile.getMinWidth() / 2);
        final double layoutY = tile.getLocation().getLayoutY(CELL_SIZE) - (tile.getMinHeight() / 2);

        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);
        gridGroup.getChildren().add(tile);
        return tile;
    }

    public Group getGridGroup() {
        return gridGroup;
    }

    public void setPoints(int points) {
        gameMovePoints.set(points);
    }

    public int getPoints() {
        return gameMovePoints.get();
    }

    public void addPoints(int points) {
        gameMovePoints.set(gameMovePoints.getValue() + points);
        gameScoreProperty.set(gameScoreProperty.get() + points);
    }

    public void setGameOver(boolean gameOver) {
        gameOverProperty.set(gameOver);
    }

    public void setGameWin(boolean won) {
        if (!gameWonProperty.get()) gameWonProperty.set(won);
    }

    public void pauseGame() {
        if (!gamePauseProperty.get()) gamePauseProperty.set(true);
    }

    public void aboutGame() {
        if (!gameAboutProperty.get()) gameAboutProperty.set(true);
    }

    public void quitGame() {
        if (!gameQuitProperty.get()) gameQuitProperty.set(true);
    }

    public BooleanProperty isLayerOn() {
        return layerOnProperty;
    }

    public BooleanProperty resetGameProperty() {
        return resetGame;
    }


    public boolean saveSession() {
        if (!gameSaveProperty.get()) gameSaveProperty.set(true);
        return true;
    }

    public void saveSession(Map<Location, Tile> gameGrid) {
        SessionManager.saveSession(gameGrid, gameScoreProperty.getValue(), LocalTime.now().minusNanos(time.toNanoOfDay()).toNanoOfDay());
        gameSaveProperty.set(true);

    }

    public boolean restoreSession() {
        if (!gameRestoreProperty.get()) gameRestoreProperty.set(true);
        return true;
    }

    public boolean restoreSession(Map<Location, Tile> gameGrid) {
        doClearGame();
        timer.stop();
        StringProperty sTime = new SimpleStringProperty("");
        int score = SessionManager.restoreSession(gameGrid, sTime, this);
        if (score >= 0) {
            gameScoreProperty.set(score);
            gameWonProperty.set(false);
            gameGrid.forEach((location, tile) -> {
                if (tile != null && tile.getValue() >= GameManager.FINAL_VALUE_TO_WIN) {
                    gameWonProperty.removeListener(wonListener);
                    gameWonProperty.set(true);
                    gameWonProperty.addListener(wonListener);
                }
            });
            if (!sTime.get().isEmpty()) {
                time = LocalTime.now().minusNanos(Long.parseLong(sTime.get()));
            }

            gameRestoreProperty.set(true);
            timer.play();

            return true;
        }
        //not session found

        return false;
    }

    public void startGame() {
        restoreRecord();
        time = LocalTime.now();
        timer.playFromStart();
    }

    private class Overlay2 extends HBox implements ChangeListener<Boolean> {
        private final Button btn1, btn2;
        private final String message, warning;
        private final String style1, style2;
        private final boolean pause;

        private final VBox txtOverlay = new VBox(10);
        private final Label lOverText = new Label();
        private final Label lOverSubText = new Label();
        private final HBox buttonsOverlay = new HBox();

        public Overlay2(String message, String warning, Button btn1, Button btn2, String style1, String style2, boolean pause) {
            this.message = message;
            this.warning = warning;
            this.btn1 = btn1;
            this.btn2 = btn2;
            this.style1 = style1;
            this.style2 = style2;
            this.pause = pause;
            setMinSize(gridWidth, gridWidth);
            setAlignment(Pos.CENTER);
            setTranslateY(TOP_HEIGHT + GAP_HEIGHT);

            getChildren().setAll(txtOverlay);
            txtOverlay.setAlignment(Pos.CENTER);

            buttonsOverlay.setAlignment(Pos.CENTER);
            buttonsOverlay.setTranslateY(TOP_HEIGHT + GAP_HEIGHT + gridWidth);
            buttonsOverlay.setMinSize(gridWidth, gridWidth / 2);
            buttonsOverlay.setSpacing(10);

        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue) {
                if (pause)pauseTimer();
                getStyleClass().setAll("game-overlay", style1);
                lOverText.setText(message);
                lOverText.getStyleClass().setAll("game-label", style2);

                lOverSubText.setText(warning);
                lOverSubText.getStyleClass().setAll("game-label", "game-lblWarning");
                txtOverlay.getChildren().setAll(lOverText, lOverSubText);
                buttonsOverlay.getChildren().setAll(btn1);
                if (btn2 != null) buttonsOverlay.getChildren().add(btn2);
                if (!layerOnProperty.get()) {
                    Board.this.overlay = this;
                    Board.this.buttonsOverlay = buttonsOverlay;
                    Board.this.getChildren().addAll(this, buttonsOverlay);
                    layerOnProperty.set(true);
                }
                gameWonProperty.set(false);
            }
        }


    }
}