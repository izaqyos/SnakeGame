package com.example.snake;

import android.content.Context;
import android.graphics.Bitmap; // Already here, good
import android.graphics.BitmapFactory; // <<-- IMPORT THIS
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
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
    private GameActivity gameActivity; // Reference to GameActivity

    private Canvas myCanvas;
    private SurfaceHolder holder;
    private Paint bgPaint;
    private Paint snakePaint; // Snake's appearance is controlled by this Paint object
    private Paint foodPaint;
    private Paint eyePaint;
    private Bitmap appleBitmap; // For the original loaded apple image
    private Bitmap scaledAppleBitmap; // <<-- TYPO CORRECTED (was < before) For the scaled apple image
    private Paint gameOverPaint;
    private int segmentSize = 40;

    private int appleDisplaySize = 50;
    private Thread thread = null;
    private volatile boolean running = false;
    private volatile boolean isGameOver = false;
    private volatile boolean surfaceReady = false; // Flag for surface readiness
    private Direction currentDirection = Direction.RIGHT;
    private int score = 0;
    private LinkedList<Point> snakeSegments = new LinkedList<>();
    private Point foodPosition = new Point();
    private Random random = new Random();
    private final long FRAME_RATE_MS = 200;
    private Paint gameOverFlashPaint; // <<-- ADD THIS: For the flash effect
    private volatile boolean showGameOverFlash = false; // <<-- ADD THIS: To control flash visibility
    private int gameOverFlashFramesRemaining = 0;    // <<-- ADD THIS: Duration of the flash in frames
    private final int GAME_OVER_FLASH_TOTAL_FRAMES = 3; // <<-- ADD THIS: How many frames the flash lasts (e.g., 3 frames = 0.6 seconds if 5FPS)
    private boolean justBecameGameOver = false;


    // Updated Constructor to accept GameActivity
    public GameManager(Context context, GameActivity activity) {
        super(context);
        this.gameActivity = activity;
        holder = getHolder();
        holder.addCallback(this); // Register for surface events

        // Initialize Paints
        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#D3D3D3"));

        snakePaint = new Paint();
        loadAndApplySnakeColor(); // Assumes this method is present and working

        foodPaint = new Paint(); // Still useful as a fallback

        foodPaint.setColor(Color.parseColor("#F44336")); // Default red for fallback
        eyePaint = new Paint();
        eyePaint.setColor(Color.BLACK);

        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.parseColor("#ba1160"));
        gameOverPaint.setTextSize(100);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);
        gameOverFlashPaint = new Paint();

        loadGameAssets(); //

        Log.i("GameManager", "GameManager constructed, waiting for surface.");
    }

    private void loadGameAssets() { // <<-- NEW METHOD TO LOAD AND SCALE APPLE
        try {
            // Load the original apple bitmap from drawable resources
            // IMPORTANT: Replace 'R.drawable.your_apple_image_name'
            // with the actual resource ID of your apple image file.
            // For example, if your image is 'my_apple.png' in res/drawable,
            // use R.drawable.my_apple
            appleBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.imageedit_19_6984634571);

            if (appleBitmap != null) {
                // Scale the loaded bitmap to your segmentSize
                scaledAppleBitmap = Bitmap.createScaledBitmap(appleBitmap,
                        appleDisplaySize, // <<-- USE appleDisplaySize HERE
                        appleDisplaySize, // <<-- AND HERE
                        true);
                Log.i("GameManager", "Apple image loaded and scaled successfully.");
            } else {
                Log.e("GameManager", "Failed to decode apple image from resources. Check R.drawable.your_apple_image_name");
                scaledAppleBitmap = null; // Ensure it's null if loading failed
            }
        } catch (Exception e) {
            Log.e("GameManager", "Error loading/scaling apple image: " + e.getMessage());
            scaledAppleBitmap = null; // Ensure it's null on error
            e.printStackTrace();
        }
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    private void loadAndApplySnakeColor() {
        if (snakePaint == null) {
            snakePaint = new Paint();
            Log.w("GameManager", "snakePaint was null during loadAndApplySnakeColor. Reinitialized.");
        }
        int chosenColor = PrefsManager.getSnakeColor(getContext());
        snakePaint.setColor(chosenColor);
        Log.i("GameManager", "Snake color updated to: " + String.format("#%06X", (0xFFFFFF & chosenColor)));
    }

    public void refreshAppearance() {
        loadAndApplySnakeColor();
        // If food appearance could also be changed in settings, you might reload/re-tint it here too.
        Log.i("GameManager", "Appearance settings refreshed via refreshAppearance().");
    }


    // ... (startGameLoop, stopGameLoop, pause, resume, setDirection methods remain the same) ...
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
            Log.i("GameManager", "Game resumed (running set to true).");
        } else {
            Log.i("GameManager", "Resume called but surface not ready.");
        }
    }

    public void setDirection(Direction newDirection) {
        // Prevent immediate 180-degree turns
        if ((currentDirection == Direction.UP && newDirection == Direction.DOWN) ||
                (currentDirection == Direction.DOWN && newDirection == Direction.UP) ||
                (currentDirection == Direction.LEFT && newDirection == Direction.RIGHT) ||
                (currentDirection == Direction.RIGHT && newDirection == Direction.LEFT)) {
            return;
        }
        currentDirection = newDirection;
        Log.d("GameManager", "Direction set to: " + newDirection);
    }


    private void initGame() {
        Log.d("GameManager", "Initializing game state with dimensions: " + scrWidth + "x" + scrHeight);
        if (scrWidth == 0 || scrHeight == 0) {
            Log.e("GameManager", "Cannot initGame: dimensions are zero.");
            return;
        }

        loadAndApplySnakeColor();

        snakeSegments.clear();
        score = 0;
        currentDirection = Direction.RIGHT;
        isGameOver = false;

        int startX = (scrWidth / segmentSize / 2) * segmentSize;
        int startY = (scrHeight / segmentSize / 2) * segmentSize;

        startX = Math.max(segmentSize * 2, startX);
        startY = Math.max(0, startY);

        if (startX >= (2 * segmentSize) && startX < scrWidth && startY < scrHeight) {
            snakeSegments.addFirst(new Point(startX - (2 * segmentSize), startY));
            snakeSegments.addFirst(new Point(startX - segmentSize, startY));
            snakeSegments.addFirst(new Point(startX, startY));
            placeFood();
            if (gameActivity != null) {
                gameActivity.updateScore(score);
            }
            Log.d("GameManager", "Game initialized. Head at: (" + snakeSegments.getFirst().x + "," + snakeSegments.getFirst().y + ")");
        } else {
            Log.e("GameManager", "Could not initialize snake within bounds. StartX=" + startX + ", StartY=" + startY);
            isGameOver = true;
            if (gameActivity != null) {
                gameActivity.showRestartButton();
            }
        }
    }

    private void placeFood() {
        if (scrWidth == 0 || scrHeight == 0) return;
        int numBlocksWide = scrWidth / segmentSize;
        int numBlocksHigh = scrHeight / segmentSize;
        if (numBlocksWide <=0 || numBlocksHigh <=0) {
            Log.e("GameManager", "Cannot place food: invalid block dimensions.");
            return;
        }

        boolean placedOnSnake;
        do {
            placedOnSnake = false;
            int foodX = random.nextInt(numBlocksWide) * segmentSize;
            int foodY = random.nextInt(numBlocksHigh) * segmentSize;
            foodPosition.set(foodX, foodY);

            for (Point segment : snakeSegments) {
                if (segment.x == foodPosition.x && segment.y == foodPosition.y) {
                    placedOnSnake = true;
                    break;
                }
            }
        } while (placedOnSnake) ;
        Log.d("GameManager", "Food placed at: (" + foodPosition.x + ", " + foodPosition.y + ")");
    }

    private void updateGame() {
        // The initial check: if game is already over (and flash sequence is not active for updates),
        // no game logic updates should occur. The flash countdown and display
        // are handled in run() and drawSurface() respectively.
        if (isGameOver) {
            return;
        }

        // Ensure snakeSegments is not empty before trying to get its first element
        if (snakeSegments.isEmpty()) {
            Log.e("GameManager", "updateGame called with empty snakeSegments. This shouldn't happen in normal gameplay.");
            // Potentially set isGameOver here if this is an unrecoverable state
            if (!isGameOver) {
                isGameOver = true;
                justBecameGameOver = true; // Trigger flash for this error state too
                if (gameActivity != null) gameActivity.showRestartButton();
            }
            return;
        }

        Point head = snakeSegments.getFirst(); // Current head
        int newX = head.x;
        int newY = head.y;

        switch (currentDirection) {
            case UP:    newY -= segmentSize; break;
            case DOWN:  newY += segmentSize; break;
            case LEFT:  newX -= segmentSize; break;
            case RIGHT: newX += segmentSize; break;
        }

        // Check for collisions (borders)
        if (newX < 0 || newX >= scrWidth || newY < 0 || newY >= scrHeight) {
            if (!isGameOver) { // Check if it wasn't already game over to trigger flash once
                isGameOver = true;
                justBecameGameOver = true; // <<-- SET FLAG TO TRIGGER FLASH
                if (gameActivity != null) {
                    gameActivity.showRestartButton();
                }
            }
            Log.w("GameManager", "Collision with border.");
            return; // Exit after game over
        }

        // Check for collisions (self)
        // This loop correctly checks if the new head position (newX, newY)
        // would collide with any existing segment of the snake.
        for (Point segment : snakeSegments) { // Simpler iteration
            if (newX == segment.x && newY == segment.y) {
                if (!isGameOver) { // Check if it wasn't already game over to trigger flash once
                    isGameOver = true;
                    justBecameGameOver = true; // <<-- SET FLAG TO TRIGGER FLASH
                    if (gameActivity != null) {
                        gameActivity.showRestartButton();
                    }
                }
                Log.w("GameManager", "Collision with self.");
                return; // Exit after game over
            }
        }

        // If no collisions, move the snake: Add new head
        Point newHead = new Point(newX, newY);
        snakeSegments.addFirst(newHead);

        // Check for food consumption
        if (newHead.x == foodPosition.x && newHead.y == foodPosition.y) {
            score++;
            if (gameActivity != null) {
                gameActivity.updateScore(score);
            }
            Log.i("GameManager", "Food eaten! Score: " + score);
            placeFood(); // Place new food
            // Don't remove tail segment - snake grows
        } else {
            // No food eaten, remove tail (if snake has segments to remove)
            if (!snakeSegments.isEmpty()) { // Defensive check, though should always have segments here
                snakeSegments.removeLast();
            }
        }
    }
    // In GameManager.java

    // In GameManager.java

    private void drawSurface() {
        if (holder.getSurface().isValid()) {
            myCanvas = holder.lockCanvas();
            if (myCanvas == null) {
                return; // Critical check
            }

            // 1. Draw Background
            myCanvas.drawRect(0, 0, scrWidth, scrHeight, bgPaint);

            // 2. Draw Food (Apple)
            if (scaledAppleBitmap != null) {
                float drawX = foodPosition.x;
                float drawY = foodPosition.y;

                // Assuming appleDisplaySize is a field in your GameManager.
                if (appleDisplaySize > segmentSize) { // appleDisplaySize should be a field like 'private int appleDisplaySize = 50;'
                    drawX = foodPosition.x - (appleDisplaySize - segmentSize) / 2.0f;
                    drawY = foodPosition.y - (appleDisplaySize - segmentSize) / 2.0f;
                } else if (appleDisplaySize < segmentSize) {
                    drawX = foodPosition.x + (segmentSize - appleDisplaySize) / 2.0f;
                    drawY = foodPosition.y + (segmentSize - appleDisplaySize) / 2.0f;
                }
                myCanvas.drawBitmap(scaledAppleBitmap, drawX, drawY, null);
            } else {
                // Fallback: If bitmap failed to load or scale, draw the original red square
                myCanvas.drawRect(foodPosition.x, foodPosition.y,
                        foodPosition.x + segmentSize, foodPosition.y + segmentSize,
                        foodPaint);
            }

            // 3. Draw Snake Segments
            for (Point segment : snakeSegments) {
                myCanvas.drawRect(segment.x, segment.y,
                        segment.x + segmentSize, segment.y + segmentSize,
                        snakePaint); // snakePaint has the user-chosen color
            }

            // 4. Draw Snake Eyes
            // Ensure eyePaint is initialized in your constructor
            if (!snakeSegments.isEmpty() && !isGameOver() && eyePaint != null) {
                Point head = snakeSegments.getFirst();

                float eyeRadius = segmentSize / 7f;
                float eyeVerticalOffsetWhenHorizontal = segmentSize / 3.5f;
                float eyeHorizontalOffsetWhenVertical = segmentSize / 3.5f;
                float eyeForwardGazeOffset = segmentSize / 4f;

                float eye1x = 0, eye1y = 0, eye2x = 0, eye2y = 0;

                switch (currentDirection) {
                    case RIGHT:
                        eye1x = head.x + segmentSize - eyeForwardGazeOffset;
                        eye2x = eye1x;
                        eye1y = head.y + (segmentSize / 2f) - eyeVerticalOffsetWhenHorizontal;
                        eye2y = head.y + (segmentSize / 2f) + eyeVerticalOffsetWhenHorizontal;
                        break;
                    case LEFT:
                        eye1x = head.x + eyeForwardGazeOffset;
                        eye2x = eye1x;
                        eye1y = head.y + (segmentSize / 2f) - eyeVerticalOffsetWhenHorizontal;
                        eye2y = head.y + (segmentSize / 2f) + eyeVerticalOffsetWhenHorizontal;
                        break;
                    case UP:
                        eye1x = head.x + (segmentSize / 2f) - eyeHorizontalOffsetWhenVertical;
                        eye2x = head.x + (segmentSize / 2f) + eyeHorizontalOffsetWhenVertical;
                        eye1y = head.y + eyeForwardGazeOffset;
                        eye2y = eye1y;
                        break;
                    case DOWN:
                        eye1x = head.x + (segmentSize / 2f) - eyeHorizontalOffsetWhenVertical;
                        eye2x = head.x + (segmentSize / 2f) + eyeHorizontalOffsetWhenVertical;
                        eye1y = head.y + segmentSize - eyeForwardGazeOffset;
                        eye2y = eye1y;
                        break;
                }

                // Draw the main part of the eyes
                myCanvas.drawCircle(eye1x, eye1y, eyeRadius, eyePaint);
                myCanvas.drawCircle(eye2x, eye2y, eyeRadius, eyePaint);

                // Optional: Draw Pupils
                if (eyePaint.getColor() == Color.WHITE) {
                    Paint pupilPaint = new Paint();
                    pupilPaint.setColor(Color.BLACK);
                    float pupilRadius = eyeRadius / 2.5f;
                    myCanvas.drawCircle(eye1x, eye1y, pupilRadius, pupilPaint);
                    myCanvas.drawCircle(eye2x, eye2y, pupilRadius, pupilPaint);
                }
            }

            // 5. DRAW GAME OVER FLASH (Moved to here)
            // Ensure gameOverFlashPaint is initialized in constructor
            // Ensure showGameOverFlash and gameOverFlashFramesRemaining are managed in run() loop
            if (showGameOverFlash && gameOverFlashPaint != null) { // Check gameOverFlashPaint for safety
                myCanvas.drawRect(0, 0, scrWidth, scrHeight, gameOverFlashPaint);
                // Log.d("GameManager", "Drawing flash, frames left: " + gameOverFlashFramesRemaining); // Use your field name
            }

            // 6. Draw Game Over Message (if applicable)
            if (isGameOver()) { // Using the getter
                myCanvas.drawText("Game Over!", scrWidth / 2, scrHeight / 2, gameOverPaint);
            }

            // 7. Unlock Canvas and Post
            holder.unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            if (justBecameGameOver) {
                showGameOverFlash = true;
                gameOverFlashFramesRemaining = GAME_OVER_FLASH_TOTAL_FRAMES;
                justBecameGameOver = false; // Reset the trigger
                Log.d("GameManager", "Flash effect initiated.");
            }

            if (showGameOverFlash) {
                gameOverFlashFramesRemaining--;
                if (gameOverFlashFramesRemaining <= 0) {
                    showGameOverFlash = false;
                    Log.d("GameManager", "Flash effect ended.");
                }
            }
            if (!isGameOver && surfaceReady) {
                updateGame();
                drawSurface();
            } else if (isGameOver && surfaceReady) {
                drawSurface();
            }

            long timeThisFrame = System.currentTimeMillis() - startTime;
            long sleepTime = FRAME_RATE_MS - timeThisFrame;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e("GameManager", "Thread interrupted: " + e.getMessage());
                }
            }
        }
        Log.i("GameManager", "Exiting run loop (running is false).");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("GameManager", "Surface created.");
        // It's generally safer to set surfaceReady = true in surfaceChanged
        // when dimensions are confirmed, but if initGame() is robust enough,
        // or assets don't depend on dimensions, it can be set here.
        // For now, keeping your existing logic where surfaceChanged handles readiness and init.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("GameManager", "Surface changed: " + width + "x" + height);
        if (width > 0 && height > 0) {
            scrWidth = width;
            scrHeight = height;
            surfaceReady = true;
            initGame();
            startGameLoop();
        } else {
            Log.w("GameManager", "Surface changed with invalid dimensions.");
            surfaceReady = false;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("GameManager", "Surface destroyed.");
        surfaceReady = false;
        stopGameLoop();
    }

    public void restartGame() {
        initGame();
    }
}