package com.akbar.game2048;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
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
        return "com.akbar.game2048.Location{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Location other = (Location) o;
        if (x != other.getX()) return false;
        return y == other.getY();
    }

    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + x;
        hash = 97 * hash + y;
        return hash;
    }

    public boolean isValidFor(int gridSize) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    /*برای جایگذاری یک کاشی در خانه ای که میخواهیم با کمتر از نصف شماره ان خانه رو
    * حرکت بدهیم تا دقیقه وسط قرار بگیرد*/

    public double getLayoutX(float cellSize) {
        return (x * cellSize) + (cellSize / 2);
    }

    public double getLayoutY(float cellSize) {
        return (y * cellSize) + (cellSize / 2);
    }

    public Location offset(Direction direction) {
        return new Location(x+direction.getX(),y+direction.getY());
    }
}
