package com.example.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.LinkedList;
import java.util.Random;

public class GameManager extends SurfaceView implements Runnable {
    // Enum for directions
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int scrHeight;
    private int scrWidth;
    private Context context;
    private Canvas myCanvas;
    private SurfaceHolder holder;
    private Paint bgPaint;
    private Paint snakePaint;
    private Paint foodPaint;
    private Paint scorePaint;
    private Paint gameOverPaint;
    private int segmentSize = 40;
    private Thread thread = null;
    private volatile boolean running = false;
    private volatile boolean isGameOver = false;
    private Direction currentDirection = Direction.RIGHT;
    private int score = 0;
    private LinkedList<Point> snakeSegments = new LinkedList<>();
    private Point foodPosition = new Point();
    private Random random = new Random();
    private long nextFrameTime;
    private final long FRAME_RATE_MS = 150;

    GameManager(Context context, int width, int height) {
        super(context);
        this.context = context;
        scrWidth = width;
        scrHeight = height;
        holder = getHolder();
        Log.i("GameManager", "Screen Dimensions: " + scrWidth + "x" + scrHeight);

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#C8E6C9"));

        snakePaint = new Paint();
        snakePaint.setColor(Color.parseColor("#4CAF50"));

        foodPaint = new Paint();
        foodPaint.setColor(Color.parseColor("#F44336"));

        scorePaint = new Paint();
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextSize(60);
        scorePaint.setTextAlign(Paint.Align.LEFT);
        
        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(100);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);

        initGame();
    }

    private void initGame() {
        Log.d("GameManager", "Initializing game state...");
        snakeSegments.clear();
        score = 0;
        currentDirection = Direction.RIGHT;
        isGameOver = false;

        int startX = (scrWidth / segmentSize / 2) * segmentSize;
        int startY = (scrHeight / segmentSize / 2) * segmentSize;
        
        snakeSegments.addFirst(new Point(startX - (2 * segmentSize), startY));
        snakeSegments.addFirst(new Point(startX - segmentSize, startY));
        snakeSegments.addFirst(new Point(startX, startY));

        placeFood();
        Log.d("GameManager", "Game initialized. Head at: (" + snakeSegments.getFirst().x + "," + snakeSegments.getFirst().y + ")");
    }

    private void placeFood() {
        int numBlocksWide = scrWidth / segmentSize;
        int numBlocksHigh = scrHeight / segmentSize;
        boolean placed = false;
        while (!placed) {
            int foodX = random.nextInt(numBlocksWide) * segmentSize;
            int foodY = random.nextInt(numBlocksHigh) * segmentSize;
            foodPosition.set(foodX, foodY);
            
            placed = true;
            for (Point segment : snakeSegments) {
                if (segment.x == foodPosition.x && segment.y == foodPosition.y) {
                    placed = false;
                    break;
                }
            }
        }
        Log.d("GameManager", "Food placed at: (" + foodPosition.x + ", " + foodPosition.y + ")");
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

    public void setDirection(Direction newDirection) {
        if (currentDirection == Direction.UP && newDirection == Direction.DOWN) return;
        if (currentDirection == Direction.DOWN && newDirection == Direction.UP) return;
        if (currentDirection == Direction.LEFT && newDirection == Direction.RIGHT) return;
        if (currentDirection == Direction.RIGHT && newDirection == Direction.LEFT) return;

        currentDirection = newDirection;
        Log.d("GameManager", "Direction set to: " + newDirection);
    }

    private void updateGame() {
        if (isGameOver) {
            return;
        }

        // Calculate new head position
        Point head = snakeSegments.getFirst();
        int newX = head.x;
        int newY = head.y;

        switch (currentDirection) {
            case UP:
                newY -= segmentSize;
                break;
            case DOWN:
                newY += segmentSize;
                break;
            case LEFT:
                newX -= segmentSize;
                break;
            case RIGHT:
                newX += segmentSize;
                break;
        }

        // Check for collisions (borders)
        if (newX < 0 || newX >= scrWidth || newY < 0 || newY >= scrHeight) {
            Log.w("GameManager", "Collision with border detected.");
            isGameOver = true;
            return;
        }

        // Check for collisions (self)
        // Start checking from the second segment
        for (int i = 1; i < snakeSegments.size(); i++) {
            Point segment = snakeSegments.get(i);
            if (newX == segment.x && newY == segment.y) {
                Log.w("GameManager", "Collision with self detected.");
                isGameOver = true;
                return;
            }
        }

        // Move the snake: Add new head
        Point newHead = new Point(newX, newY);
        snakeSegments.addFirst(newHead);

        // Check for food consumption
        if (newHead.x == foodPosition.x && newHead.y == foodPosition.y) {
            score++;
            Log.i("GameManager", "Food eaten! Score: " + score);
            placeFood(); // Place new food
            // Don't remove tail segment - snake grows
        } else {
            // No food eaten, remove tail
            snakeSegments.removeLast();
        }
    }

    private void drawSurface() {
        if (holder.getSurface().isValid()) {
            myCanvas = holder.lockCanvas();
            if (myCanvas == null) return; // Check if canvas is valid

            // Draw background
            myCanvas.drawPaint(bgPaint);

            // Draw Food
            myCanvas.drawRect(foodPosition.x, foodPosition.y, 
                              foodPosition.x + segmentSize, foodPosition.y + segmentSize, 
                              foodPaint);

            // Draw Snake
            for (Point segment : snakeSegments) {
                myCanvas.drawRect(segment.x, segment.y, 
                                  segment.x + segmentSize, segment.y + segmentSize, 
                                  snakePaint);
            }

            // Draw Score
            myCanvas.drawText("Score: " + score, 20, 70, scorePaint);

            // Draw Game Over message if applicable
            if (isGameOver) {
                myCanvas.drawText("Game Over!", scrWidth / 2, scrHeight / 2, gameOverPaint);
            }

            holder.unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            if (!isGameOver) {
                updateGame();
            }
            drawSurface();

            // Control frame rate
            long timeThisFrame = System.currentTimeMillis() - startTime;
            long timeToSleep = FRAME_RATE_MS - timeThisFrame;

            if (timeToSleep > 0) {
                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                     Log.e("GameManager", "Thread interrupted", e);
                     Thread.currentThread().interrupt(); // Preserve interrupt status
                     running = false; // Stop loop on interrupt
                }
            }
        }
        Log.i("GameManager", "Game loop finished.");
    }
}