package com.akbar.game2048;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridOperator {
    public static final int DEFAULT_GRID_SIZE = 4  ;
    private int gridSize;
    private List<Integer> traversalX;
    private List<Integer> traversalY;

    public GridOperator(int gridSize) {
        this.gridSize = gridSize;
        traversalX = IntStream.range(0,gridSize).boxed().collect(Collectors.toList());
        traversalY = IntStream.range(0,gridSize).boxed().collect(Collectors.toList());
    }

    public GridOperator() {
        this(DEFAULT_GRID_SIZE);
    }

    public void sortGrid(Direction direction) {
        traversalX.sort(direction.equals(Direction.RIGHT) ? Collections.reverseOrder() : Integer::compareTo);
        traversalY.sort(direction.equals(Direction.DOWN) ? Collections.reverseOrder() : Integer::compareTo);
    }

    public int getGridSize() {
        return gridSize;
    }

    public int traverseGrid(IntBinaryOperator func){
        AtomicInteger atomicInteger =new AtomicInteger();
        traversalX.forEach(tx->{
            traversalY.forEach(ty->{
                atomicInteger.addAndGet(func.applyAsInt(tx,ty));
            });
        });
        return atomicInteger.get();
    }

    public boolean isValidLocation(Location thisLocation) {
        return thisLocation.getX()>=0 && thisLocation.getX()<gridSize && thisLocation.getY()>=0 && thisLocation.getY()<gridSize;
    }
}
