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

public class GameManager extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    // Enum for directions
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Screen dimensions - obtained from surfaceChanged
    private int scrHeight;
    private int scrWidth;
    // private Context context; // Context obtained from GameActivity
    private GameActivity gameActivity; // Reference to GameActivity

    private Canvas myCanvas;
    private SurfaceHolder holder;
    private Paint bgPaint;
    private Paint snakePaint;
    private Paint foodPaint;
    // private Paint scorePaint; // Score drawing removed
    private Paint gameOverPaint;
    private int segmentSize = 40;
    private Thread thread = null;
    private volatile boolean running = false;
    private volatile boolean isGameOver = false;
    private volatile boolean surfaceReady = false; // Flag for surface readiness
    private Direction currentDirection = Direction.RIGHT;
    private int score = 0;
    private LinkedList<Point> snakeSegments = new LinkedList<>();
    private Point foodPosition = new Point();
    private Random random = new Random();
    // private long nextFrameTime; // Not used in current loop implementation
    private final long FRAME_RATE_MS = 200; // Slower frame rate (from 100)

    // Updated Constructor to accept GameActivity
    GameManager(Context context, GameActivity activity) {
        super(context);
        this.gameActivity = activity;
        holder = getHolder();
        holder.addCallback(this); // Register for surface events

        // Initialize Paints
        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#D3D3D3")); // Use background color from layout

        snakePaint = new Paint();
        snakePaint.setColor(Color.parseColor("#4CAF50"));

        foodPaint = new Paint();
        foodPaint.setColor(Color.parseColor("#F44336"));

        // scorePaint removed

        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(100);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);

        // Do not call initGame() here, wait for surfaceChanged
        Log.i("GameManager", "GameManager constructed, waiting for surface.");
    }

    // Renamed from start() for clarity
    private void startGameLoop() {
        if (thread == null || !thread.isAlive()) {
            running = true;
            isGameOver = false; // Ensure game isn't over when starting loop
            thread = new Thread(this);
            thread.start();
            Log.i("GameManager", "Game loop thread started.");
        }
    }

    // Renamed from stopThread() for clarity
    private void stopGameLoop() {
        running = false;
        boolean retry = true;
        while(retry) {
            try {
                if (thread != null) {
                    thread.join();
                }
                retry = false;
                thread = null; // Nullify thread after joining
                Log.i("GameManager", "Game loop thread stopped.");
            } catch (InterruptedException e) {
                Log.e("GameManager", "Error stopping thread: " + e.getMessage());
                Thread.currentThread().interrupt(); // Re-interrupt thread
            }
        }
    }

    // Called by GameActivity.onPause()
    public void pause() {
        running = false; // Stop the game loop logic execution
        Log.i("GameManager", "Game paused (running set to false).");
    }

    // Called by GameActivity.onResume()
    public void resume() {
        if (surfaceReady) { // Only resume game logic if surface is ready
           running = true; // Allow game loop logic to execute
           // If thread isn't running, surfaceChanged should restart it
           Log.i("GameManager", "Game resumed (running set to true).");
        } else {
           Log.i("GameManager", "Resume called but surface not ready.");
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

    private void initGame() {
        Log.d("GameManager", "Initializing game state with dimensions: " + scrWidth + "x" + scrHeight);
        if (scrWidth == 0 || scrHeight == 0) {
            Log.e("GameManager", "Cannot initGame: dimensions are zero.");
            return;
        }
        snakeSegments.clear();
        score = 0;
        currentDirection = Direction.RIGHT;
        isGameOver = false;

        // Center starting position based on actual surface dimensions
        int startX = (scrWidth / segmentSize / 2) * segmentSize;
        int startY = (scrHeight / segmentSize / 2) * segmentSize;

        // Ensure start position is within bounds if screen is very small
        startX = Math.max(segmentSize * 2, startX);
        startY = Math.max(0, startY);

        // Check bounds before adding segments
        if (startX < scrWidth && startX - segmentSize < scrWidth && startX - (2 * segmentSize) < scrWidth &&
            startY < scrHeight) {
            snakeSegments.addFirst(new Point(startX - (2 * segmentSize), startY));
            snakeSegments.addFirst(new Point(startX - segmentSize, startY));
            snakeSegments.addFirst(new Point(startX, startY));
            placeFood();
            gameActivity.updateScore(score); // Update score display in GameActivity
            Log.d("GameManager", "Game initialized. Head at: (" + snakeSegments.getFirst().x + "," + snakeSegments.getFirst().y + ")");
        } else {
             Log.e("GameManager", "Could not initialize snake within bounds.");
             isGameOver = true; // Set game over if init fails
        }
    }

    private void placeFood() {
        if (scrWidth == 0 || scrHeight == 0) return;
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
            gameActivity.updateScore(score); // << UPDATE SCORE IN GAMEACTIVITY
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

            // Draw background - Use surface dimensions
            myCanvas.drawRect(0, 0, scrWidth, scrHeight, bgPaint);

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

            // Draw Score - REMOVED
            // myCanvas.drawText("Score: " + score, 20, 70, scorePaint);

            // Draw Game Over message if applicable
            if (isGameOver) {
                // Use current dimensions for centering
                myCanvas.drawText("Game Over!", scrWidth / 2, scrHeight / 2, gameOverPaint);
            }

            holder.unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            if (!isGameOver && surfaceReady) { // Only update/draw if surface is ready
                updateGame();
                drawSurface();
            }

            // Frame rate control
            long timeThisFrame = System.currentTimeMillis() - startTime;
            long sleepTime = FRAME_RATE_MS - timeThisFrame;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Log.e("GameManager", "Thread interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Re-interrupt thread
                }
            }
        }
         Log.i("GameManager", "Exiting run loop (running is false).");
    }

    // SurfaceHolder.Callback methods
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("GameManager", "Surface created.");
        surfaceReady = true;
        // Dimensions might not be known yet, wait for surfaceChanged
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("GameManager", "Surface changed: " + width + "x" + height);
        if (width > 0 && height > 0) {
            scrWidth = width;
            scrHeight = height;
            surfaceReady = true;
            initGame(); // Initialize game state with correct dimensions
            startGameLoop(); // Start or ensure the game loop is running
        } else {
            Log.w("GameManager", "Surface changed with invalid dimensions.");
            surfaceReady = false;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("GameManager", "Surface destroyed.");
        surfaceReady = false;
        stopGameLoop(); // Stop the game thread cleanly
    }
}