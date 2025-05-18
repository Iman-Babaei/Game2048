package com.akbar.game2048;

import javafx.scene.input.KeyCode;

public enum Direction {
    UP(0,-1) , RIGHT(1,0), DOWN(0,1), LEFT(-1,0);
    private int x;
    private int y;
    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Direction{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
    public static Direction valueFor(KeyCode keyCode) {
        return valueOf(keyCode.getName());
    }
}
