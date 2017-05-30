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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 10;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public static final String TAG = "puzzle";

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            for(int i=0; i<NUM_SHUFFLE_STEPS; i++){
                int len = puzzleBoard.neighbours().size();
                int ranNum= Math.abs(random.nextInt(len) -1);

                puzzleBoard = puzzleBoard.neighbours().get(ranNum);
                invalidate();
            }
            puzzleBoard.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            Log.d(TAG, "onTouchEvent: if");
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouchEvent: switch case");
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        PriorityQueue<PuzzleBoard> boardPriorityQueue = new PriorityQueue<>(1000,new Comparator<PuzzleBoard>() {
            @Override
            public int compare(PuzzleBoard puzzleBoard, PuzzleBoard t1) {
                if(puzzleBoard.priority() > t1.priority()) return 1;
                else if(puzzleBoard.priority() < t1.priority()) return -1;
                else return 0;
            }
        });
        puzzleBoard.steps = 0;
        puzzleBoard.previousBoard = null;
        boardPriorityQueue.add(puzzleBoard);

        ArrayList<PuzzleBoard> solutions = new ArrayList<>();

        while(!boardPriorityQueue.isEmpty()){
            PuzzleBoard tempBoard = boardPriorityQueue.poll();
            if(!tempBoard.resolved()){
                boardPriorityQueue.addAll(tempBoard.neighbours());
                for(PuzzleBoard temp : tempBoard.neighbours()){
                    if(!boardPriorityQueue.contains(temp))
                        boardPriorityQueue.add(temp);
                }
            }
            else {
                PuzzleBoard solBoard = tempBoard;
                while(!solBoard.equals(puzzleBoard)){
                    solutions.add(solBoard);
                    solBoard = solBoard.previousBoard;
                }
                solutions.add(puzzleBoard);
                Collections.reverse(solutions);
                break;
            }
        }

        animation = solutions;
        invalidate();

    }
}
