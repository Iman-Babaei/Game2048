package com.akbar.game2048;


import com.akbar.game2048.*;
import javafx.animation.*;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameManager extends Group {
    public static final int FINAL_VALUE_TO_WIN = 2048;
    private static final Duration ANIMATION_EXISTING_TILE = Duration.millis(65);
    private static final Duration ANIMATION_NEWLY_ADDED_TILE = Duration.millis(125);
    private static final Duration ANIMATION_MERGED_TILE = Duration.millis(80);

    private volatile boolean movingTiles = false;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();
    private final Board board;
    private final GridOperator gridOperator;

    public GameManager() {
        this(GridOperator.DEFAULT_GRID_SIZE);
    }

    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        this.gridOperator = new GridOperator(gridSize);
        board = new Board(gridOperator);
        this.getChildren().add(this.board);


        board.resetGameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue){ initializeGameGrid();
                startGame();}
        });

        initializeGameGrid();
        startGame();
    }



    private void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        gridOperator.traverseGrid((x, y) -> {
            final Location thisLocation = new Location(x, y);
            locations.add(thisLocation);
            gameGrid.put(thisLocation, null);
            return 0;
        });
    }

    private void startGame() {
        Tile tile0 = Tile.newRandomTile();
        List<Location> randomLocations = new ArrayList<>(locations);
        Collections.shuffle(randomLocations);
        Iterator<Location> locs = randomLocations.stream().limit(2).iterator();
        tile0.setLocation(locs.next());

        Tile tile1 = null;
        if (new Random().nextFloat() <= 0.8f) {     // gives 80% chance to add a second tile
            tile1 = Tile.newRandomTile();
            if (tile1.getValue() == 4 && tile0.getValue() == 4) tile1 = Tile.newTile(2);
            tile1.setLocation(locs.next());
        }
        Arrays.asList(tile0, tile1).stream().filter(Objects::nonNull).forEach(tile -> gameGrid.put(tile.getLocation(), tile));
        redrawTilesInGameGrid();
        board.startGame();
    }

    private void redrawTilesInGameGrid() {
        gameGrid.values().stream().filter(Objects::nonNull).forEach(tile -> board.addTile(tile));
    }

    private void moveTiles(Direction direction) {
        synchronized (gameGrid) {
            if (movingTiles) return;
        }
        board.setPoints(0);
        mergedToBeRemoved.clear();
        ParallelTransition parallelTransition = new ParallelTransition();
        gridOperator.sortGrid(direction);
        final int tilesWereMoved = gridOperator.traverseGrid((x, y) -> {
            final Location thisLocation = new Location(x, y);
            Location farthestLocation = findFarthestLocation(thisLocation, direction);
            Optional<Tile> opTile = optionalTile(thisLocation);

            AtomicInteger result = new AtomicInteger();
            Location nextLocation = farthestLocation.offset(direction);
            optionalTile(nextLocation).filter(tile -> tile.isMergable(opTile) && !tile.isMerged())     //going to merge
                    .ifPresent(tile -> {
                        Tile tile1 = opTile.get();
                        tile.merge(tile1);
                        tile.toFront();
                        gameGrid.put(nextLocation, tile);
                        gameGrid.replace(thisLocation, null);
                        parallelTransition.getChildren().add(animateExistingTile(tile1, tile.getLocation()));
                        parallelTransition.getChildren().add(animateMergedTile(tile));
                        mergedToBeRemoved.add(tile1);

                        board.addPoints(tile.getValue());

                        if (tile.getValue() == FINAL_VALUE_TO_WIN) board.setGameWin(true);

                        result.set(1);
                    });
            if (result.get() == 0 && opTile.isPresent() && !farthestLocation.equals(thisLocation)) {        //not merged
                final Tile tile = opTile.get();
                parallelTransition.getChildren().add(animateExistingTile(tile, farthestLocation));

                gameGrid.put(farthestLocation, tile);
                gameGrid.replace(thisLocation, null);

                tile.setLocation(farthestLocation);

                result.set(1);
            }
            return result.get();
        });
        board.animateScore();
        if (parallelTransition.getChildren().size() > 0) {
            parallelTransition.setOnFinished(event -> {
                board.getGridGroup().getChildren().removeAll(mergedToBeRemoved);
                //reset merged after each movement
                gameGrid.values().stream().filter(Objects::nonNull).forEach(Tile::clearMerged);

                Location randomAvailableLocation = findRandomAvailableLocation();
                if (randomAvailableLocation == null && mergeMovementsAvailable() == 0) {
                    //game is over if there are no moves available
                    board.setGameOver(true);
                }else if (randomAvailableLocation!=null && tilesWereMoved>0){
                    synchronized (gameGrid){movingTiles=false;}
                    addAndAnimateRandomTile(randomAvailableLocation);
                }
            });
            synchronized (gameGrid){
                movingTiles=true;
            }
            parallelTransition.play();
        }
    }

    private void addAndAnimateRandomTile(Location randomAvailableLocation) {
        final Tile tile = board.addRandomTile(randomAvailableLocation);
        gameGrid.put(tile.getLocation(),tile);
        animateNewlyAddedTile(tile).play();
    }

    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final ScaleTransition scaleTransition = new ScaleTransition(ANIMATION_NEWLY_ADDED_TILE, tile);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        scaleTransition.setOnFinished(event -> {if (this.gameGrid.values().parallelStream().noneMatch(Objects::isNull) && mergeMovementsAvailable()==0) board.setGameOver(true);});
        return scaleTransition;
    }

    private int mergeMovementsAvailable() {
        final AtomicInteger pairsOfMergeableTiles = new AtomicInteger();

        Stream.of(Direction.UP, Direction.LEFT).parallel().forEach(direction -> {
            gridOperator.traverseGrid((x, y) -> {
                Location thisLocation = new Location(x, y);
                optionalTile(thisLocation).ifPresent(tile -> {
                    if (tile.isMergable(optionalTile(thisLocation.offset(direction))))
                        pairsOfMergeableTiles.incrementAndGet();
                });
                return 0;
            });

        });
        return pairsOfMergeableTiles.get();
    }

    private Location findRandomAvailableLocation() {
        List<Location> availableLocations = locations.stream().filter(location -> gameGrid.get(location) == null).collect(Collectors.toList());
        if (availableLocations.isEmpty()) return null;
        Collections.shuffle(availableLocations);
        Location randomLocation = availableLocations.get(new Random().nextInt(availableLocations.size()));
        return randomLocation;
    }

    private Animation animateMergedTile(Tile tile) {
        final ScaleTransition scale0 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);

        final ScaleTransition scale1 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale1.setToX(1);
        scale1.setToY(1);
        scale1.setInterpolator(Interpolator.EASE_OUT);
        return new SequentialTransition(scale0, scale1);
    }

    private Animation animateExistingTile(Tile tile, Location location) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(tile.layoutXProperty(), location.getLayoutX(board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        KeyValue kvY = new KeyValue(tile.layoutYProperty(), location.getLayoutY(board.CELL_SIZE) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        final KeyFrame kfX = new KeyFrame(ANIMATION_EXISTING_TILE, kvX);
        final KeyFrame kfY = new KeyFrame(ANIMATION_EXISTING_TILE, kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);
        return timeline;
    }

    private Optional<Tile> optionalTile(Location location) {
        return Optional.ofNullable(gameGrid.get(location));
    }

    private Location findFarthestLocation(Location thisLocation, Direction direction) {
        Location farthest;
        do {
            farthest = thisLocation;
            thisLocation = farthest.offset(direction);
        } while (gridOperator.isValidLocation(thisLocation) && !optionalTile(thisLocation).isPresent());
        return farthest;
    }


    /*************************************************************************/
/************************ Public methods *********************************/
/*************************************************************************/
    /**
     * Move the tiles according user input if overlay is not on
     * @param direction
     */
    public void move(Direction direction){
        if (!board.isLayerOn().get()) {
            moveTiles(direction);
        }
    }

    /**
     * Check if overlay covers the grid or not
     * @return boolean
     */
    public BooleanProperty isLayerOn() {
        return board.isLayerOn();
    }

    /**
     * Pauses the game time, covers the grid
     */
    public void pauseGame() {
        board.pauseGame();
    }

    /**
     * Quit the game with confirmation
     */
    public void quitGame() {
        board.quitGame();
    }

    /**
     * Ask to save the game from a properties file with confirmation
     */
    public void saveSession() {
        board.saveSession(gameGrid);
    }
    /**
     * Save the game to a properties file, without confirmation
     */


    /**
     * Ask to restore the game from a properties file with confirmation
     */
    public void restoreSession() {
        board.restoreSession(gameGrid);
    }

    /**
     * Restore the game from a properties file, without confirmation
     */
//    private void doRestoreSession() {
//        initializeGameGrid();
//        if (board.restoreSession(gameGrid)) {
//            redrawTilesInGameGrid();
//        }
//    }

    /**
     * Save actual record to a properties file
     */
    public void saveRecord() {
        board.saveRecord();
    }

    public void tryAgain() {
        board.tryAgain();
    }

    public void aboutGame() {
        board.aboutGame();
    }

    public void setToolBar(HBox toolbar){
        board.setToolBar(toolbar);
    }

    public void setHostServices(HostServices hostServices){
        board.setHostServices(hostServices);
    }
}