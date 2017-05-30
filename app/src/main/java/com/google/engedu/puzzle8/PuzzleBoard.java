/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;


public class PuzzleBoard {
    public static final String TAG = "puzzle";
    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;
    int steps = 0;
    PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int parentHeight = 600;

        tiles = new ArrayList<>();

        for(int i = 0; i< NUM_TILES*NUM_TILES -1 ;i++){
            Bitmap temp = Bitmap.createBitmap(bitmap,i%NUM_TILES*(width/NUM_TILES),i/NUM_TILES*(height/NUM_TILES),width/3,height/3);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(temp,parentWidth/NUM_TILES,parentHeight/NUM_TILES,false);
            Log.d(TAG, "PuzzleBoard: width "+i%NUM_TILES + " height "+i/NUM_TILES);
            PuzzleTile newTile = new PuzzleTile(scaledBitmap,i);

            tiles.add(newTile);
        }
        tiles.add(null);
        draw(new Canvas());
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        steps = otherBoard.steps + 1;
        previousBoard = otherBoard;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> neighbourBoards = new ArrayList<>();
        int i;
        for(i=0; i<tiles.size(); i++){
            if(tiles.get(i) == null)
                break;
        }

        int tileX = i%NUM_TILES;
        int tileY= i/NUM_TILES;

        for(int[] x: NEIGHBOUR_COORDS){
            int nullX = tileX + x[0];
            int nullY = tileY + x[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES ){
                PuzzleBoard newBoard = new PuzzleBoard(this);
                newBoard.swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                neighbourBoards.add(newBoard);
            }
        }

        return neighbourBoards;

    }

    public int priority() {
        int prioritySum = steps;
        for(int i=0; i<NUM_TILES*NUM_TILES; i++){
            int origX = i%NUM_TILES;
            int origY = i/NUM_TILES;
            int newPos;
            if(tiles.get(i) == null)
                newPos = 9;
            else newPos = tiles.get(i).getNumber();
            int newX = newPos%NUM_TILES;
            int newY = newPos/NUM_TILES;

            prioritySum += Math.abs(newX - origX) + Math.abs(newY - origY);
        }
        return prioritySum;
    }

}
