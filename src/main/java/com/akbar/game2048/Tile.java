package com.akbar.game2048;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.util.Optional;
import java.util.Random;

public class Tile extends Label {
    private final int CEL_SIZE = 128;
    private final int PADDING = 13;
    private int value;
    private Location location;
    protected boolean merged;

    public Tile(int value) {
        this.value = value;
        merged = false;
        setText(value+"");
        int squareSize = CEL_SIZE - PADDING;
        setMinSize(squareSize, squareSize);
        setMaxSize(squareSize, squareSize);
        setPrefSize(squareSize, squareSize);
        setAlignment(Pos.CENTER);
        getStyleClass().addAll("game-label", "game-tile-"+value);
    }

    public int getValue() {
        return value;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static Tile newRandomTile() {
        int value = new Random().nextDouble()<0.9?2:4;
        return new Tile(value);
    }

    public static Tile newTile(int value) {
        return new Tile(value);
    }

    public void merge(Tile another) {      //برای ترکیب 2 تا tile باهم دیگه
        getStyleClass().remove("game-tile-"+value);
        this.value += another.value;
        setText(value+"");
        merged = true;
        getStyleClass().add("game-tile-"+value);
    }

    @Override
    public String toString() {
        return "Tile{" +
                "location=" + location +
                ", value=" + value +
                '}';
    }
    public boolean isMerged() {      //ببینیم برای اینکه باهم ترکیب شدن tile هامون یا ن
        return merged;
    }
    public void clearMerged() {
        merged = false;
    }

    public boolean isMergable(Optional<Tile> anotherTile) {     //ایا 2 tile میتوانند باهم ترکیب بشن یا ن
        return anotherTile.filter(tile -> tile.getValue() == getValue()).isPresent();
    }
}
