package com.example.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameManager extends SurfaceView implements Runnable {
    // Enum for directions
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int scrHeight;
    private int scrWidth;
    private Canvas myCanvas;
    private SurfaceHolder holder;
    private Bitmap snakeBitmap;
    // private Bitmap leftArBitmap; // Commented out custom arrow bitmap
    private Paint bgPaint;
    private AnimatedSprite snake;
    // private AnimatedSprite leftArrow; // Commented out custom arrow sprite
    // private Rect leftArrowRect; // Commented out (if it was used)
    private Thread thread;
    private volatile boolean running = false;
    // Current direction state
    private int snakeXDirection = 1; // Start moving right
    private int snakeYDirection = 0;
    private int snakeSpeed = 10;
    // Keep track of the current direction enum value
    private Direction currentDirection = Direction.RIGHT;

    GameManager(Context context, int width, int height) {
        super(context);
        scrWidth = width;
        scrHeight = height;
        holder = getHolder();
        myCanvas = new Canvas();
        bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);

        // Load snake bitmap
        snakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pinksqr);
        snakeBitmap = Bitmap.createScaledBitmap(snakeBitmap, 40, 40, false);
        snake = new AnimatedSprite(0, 100, snakeBitmap, scrWidth, scrHeight);

        /* Commented out custom left arrow loading
        leftArBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.leftarrow); 
        if (leftArBitmap == null) {
            Log.e("GameManager", "Error loading left arrow bitmap!");
        } else {
            Log.d("GameManager", "Left arrow bitmap loaded successfully: " + leftArBitmap.getWidth() + "x" + leftArBitmap.getHeight());
        }
        int margin = 100;
        int bottomMargin = 600; 

        leftArrow = new AnimatedSprite(margin, scrHeight - bottomMargin, leftArBitmap, scrWidth, scrHeight);
        */
    }

    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stopThread() {
        running = false;
        try {
            if (thread != null) {
                thread.join();
            }
            thread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        running = false;
    }

    public void resume() {
        running = true;
        if (thread == null) {
            start();
        }
    }

    // Method to change the snake's direction
    public void setDirection(Direction newDirection) {
        // Prevent reversing direction directly
        if (currentDirection == Direction.UP && newDirection == Direction.DOWN) return;
        if (currentDirection == Direction.DOWN && newDirection == Direction.UP) return;
        if (currentDirection == Direction.LEFT && newDirection == Direction.RIGHT) return;
        if (currentDirection == Direction.RIGHT && newDirection == Direction.LEFT) return;

        // Update direction state variables
        switch (newDirection) {
            case UP:
                snakeXDirection = 0;
                snakeYDirection = -1; // Y decreases upwards
                break;
            case DOWN:
                snakeXDirection = 0;
                snakeYDirection = 1; // Y increases downwards
                break;
            case LEFT:
                snakeXDirection = -1;
                snakeYDirection = 0;
                break;
            case RIGHT:
                snakeXDirection = 1;
                snakeYDirection = 0;
                break;
        }
        currentDirection = newDirection; // Update the current direction
        Log.d("GameManager", "Direction set to: " + newDirection);
    }

    private void drawSurface() {
        if (holder.getSurface().isValid()) {
            myCanvas = holder.lockCanvas();
            myCanvas.drawPaint(bgPaint);
            snake.draw(myCanvas);
            // leftArrow.draw(myCanvas); // Commented out drawing custom arrow
            holder.unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void run() {
        while (running) {
            moveSnake();
            drawSurface();
            try {
                Thread.sleep(130);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveSnake() {
        snake.setX(snake.getX() + snakeXDirection * snakeSpeed);
        snake.setY(snake.getY() + snakeYDirection * snakeSpeed);

        // Basic boundary checking
        if (snake.getX() < 0) snake.setX(0);
        if (snake.getX() > scrWidth - snake.getSpriteWidth()) snake.setX(scrWidth - snake.getSpriteWidth());
        if (snake.getY() < 0) snake.setY(0);
        if (snake.getY() > scrHeight - snake.getSpriteHeight()) snake.setY(scrHeight - snake.getSpriteHeight());
    }
}