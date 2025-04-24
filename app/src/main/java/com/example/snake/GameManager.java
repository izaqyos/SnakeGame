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
    private int scrHeight;
    private int scrWidth;
    private Canvas myCanvas;
    private SurfaceHolder holder;
    private Bitmap snakeBitmap;
    private Bitmap leftArBitmap;
    private Paint bgPaint;
    private AnimatedSprite snake;
    private AnimatedSprite leftArrow;
    private Rect leftArrowRect;
    private Thread thread;
    private volatile boolean running = false;
    private int snakeXDirection = 1;
    private int snakeYDirection = 0;
    private int snakeSpeed = 10;

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

        // Load left arrow bitmap
        leftArBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.leftarrow); // Check this name carefully!
        if (leftArBitmap == null) {
            Log.e("GameManager", "Error loading left arrow bitmap!");
        } else {
            Log.d("GameManager", "Left arrow bitmap loaded successfully: " + leftArBitmap.getWidth() + "x" + leftArBitmap.getHeight());
        }
        int margin = 100;
        int bottomMargin = 600; // Try a larger value to move the arrow higher up

        leftArrow = new AnimatedSprite(margin, scrHeight - bottomMargin, leftArBitmap, scrWidth, scrHeight);

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

    public void onTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.d("GameManager", "Touch event at: x=" + x + ", y=" + y);

        if (leftArrow.inRect(x, y)) {
            Log.d("GameManager", "Left arrow touched!");
            snakeXDirection = -1;
            snakeYDirection = 0;
        }
    }

    private void drawSurface() {
        if (holder.getSurface().isValid()) {
            myCanvas = holder.lockCanvas();
            myCanvas.drawPaint(bgPaint);
            snake.draw(myCanvas);
            leftArrow.draw(myCanvas);
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