package com.example.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class GameManager extends SurfaceView implements Runnable, SurfaceHolder.Callback, QuoteFetcher.QuoteCallback {
    // Enum for directions
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Screen dimensions
    private int scrHeight;
    private int scrWidth;
    private GameActivity gameActivity;
    private String currentUsername; // <<-- הוספת שדה לשם המשתמש הנוכחי
    private MyFBDB myFBDB; // <<-- הוספת רפרנס ל-MyFBDB

    // Drawing objects
    private Canvas myCanvas;
    private SurfaceHolder holder;
    private Paint bgPaint;
    private Paint snakePaint;
    private Paint foodPaint;
    private Bitmap appleBitmap;
    private Bitmap scaledAppleBitmap;
    private int appleDisplaySize = 50;
    private Paint eyePaint;

    // Game Over related
    private Paint gameOverPaint;
    private Paint gameOverFlashPaint;
    private volatile boolean showGameOverFlash = false;
    private int gameOverFlashFramesRemaining = 0;
    private final int GAME_OVER_FLASH_TOTAL_FRAMES = 3;
    private boolean justBecameGameOver = false;

    // Quote Display
    private QuoteFetcher quoteFetcher;
    private volatile String gameOverQuote = null;
    private volatile String gameOverQuoteAuthor = null;
    private TextPaint quoteTextPaint;
    private StaticLayout quoteLayout = null;
    private final String DEFAULT_MOTIVATIONAL_MESSAGE = "תמשיך לנסות! אתה תצליח!";

    // Game state
    private int segmentSize = 40;
    private Thread thread = null;
    private volatile boolean running = false;
    private volatile boolean isGameOver = false;
    private volatile boolean surfaceReady = false;
    private Direction currentDirection = Direction.RIGHT;
    private int score = 0;
    private LinkedList<Point> snakeSegments = new LinkedList<>();
    private Point foodPosition = new Point();
    private Random random = new Random();
    private final long FRAME_RATE_MS = 200;

    public GameManager(Context context, GameActivity activity, String username, MyFBDB fbdb) { //מאתחל את כל הרכיבים הדרושים לפעולת המשחק
        super(context);
        this.gameActivity = activity;
        this.currentUsername = username; // שמירת שם המשתמש
        this.myFBDB = fbdb;             // שמירת רפרנס ל-MyFBD
        holder = getHolder();
        holder.addCallback(this);

        // Initialize Paints
        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#D3D3D3"));

        snakePaint = new Paint();
        loadAndApplySnakeColor();

        foodPaint = new Paint();
        foodPaint.setColor(Color.parseColor("#F44336"));

        eyePaint = new Paint();
        eyePaint.setColor(Color.BLACK);

        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.parseColor("#ba1160"));
        gameOverPaint.setTextSize(100);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);

        gameOverFlashPaint = new Paint();
        gameOverFlashPaint.setColor(Color.argb(128, 255, 105, 180));

        quoteFetcher = new QuoteFetcher();
        quoteTextPaint = new TextPaint();
        quoteTextPaint.setColor(Color.DKGRAY);
        quoteTextPaint.setTextSize(35);
        quoteTextPaint.setAntiAlias(true);

        loadGameAssets(); //העלאת התמונות
        Log.i("GameManager", "GameManager constructed for user: " + currentUsername);
    }

    private void loadGameAssets() {
        try {
            appleBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.imageedit_19_6984634571); // <<-- החליפי בשם הקובץ שלך
            if (appleBitmap != null) {
                scaledAppleBitmap = Bitmap.createScaledBitmap(appleBitmap, appleDisplaySize, appleDisplaySize, true);
            } else {
                Log.e("GameManager", "Failed to decode apple image.");
            }
        } catch (Exception e) {
            Log.e("GameManager", "Error loading apple image: " + e.getMessage());
        }
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    private void loadAndApplySnakeColor() {
        if (snakePaint == null) snakePaint = new Paint();
        snakePaint.setColor(PrefsManager.getSnakeColor(getContext()));
    }

    public void refreshAppearance() {
        loadAndApplySnakeColor();
    }

    @Override
    public void onQuoteFetched(String quote, String author) {
        if (isGameOver()) {
            Log.i("GameManager_QuoteDebug", "SUCCESS: Quote fetched: \"" + quote + "\" - " + author);
            this.gameOverQuote = "\"" + quote + "\"";
            this.gameOverQuoteAuthor = "- " + author;
            prepareQuoteLayout();
        } else {
            Log.i("GameManager_QuoteDebug", "SUCCESS: Quote fetched. Game NOT over, quote discarded.");
        }
    }

    @Override
    public void onFetchFailed(String error) {
        if (isGameOver()) {
            Log.e("GameManager_QuoteDebug", "FAILED: Failed to fetch quote: " + error);
            this.gameOverQuote = DEFAULT_MOTIVATIONAL_MESSAGE;
            this.gameOverQuoteAuthor = "";
            prepareQuoteLayout();
        } else {
            Log.e("GameManager_QuoteDebug", "FAILED: Failed to fetch quote. Game NOT over, default discarded.");
        }
    }

    private void prepareQuoteLayout() {
        if (scrWidth > 0 && gameOverQuote != null) {
            int textWidth = scrWidth - 80;
            String fullQuoteText = gameOverQuoteAuthor != null && !gameOverQuoteAuthor.isEmpty() ?
                    gameOverQuote + "\n" + gameOverQuoteAuthor : gameOverQuote;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                quoteLayout = StaticLayout.Builder.obtain(fullQuoteText, 0, fullQuoteText.length(), quoteTextPaint, textWidth)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0, 1.0f).setIncludePad(true).build();
            } else {
                quoteLayout = new StaticLayout(fullQuoteText, quoteTextPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            }
            Log.d("GameManager_QuoteDebug", "Quote layout prepared.");
        } else {
            quoteLayout = null;
            Log.d("GameManager_QuoteDebug", "Quote layout cannot be prepared.");
        }
    }

    private void startGameLoop() {
        if (thread == null || !thread.isAlive()) {
            running = true;
            thread = new Thread(this);
            thread.start();
            Log.i("GameManager", "Game loop thread started.");
        }
    }

    private void stopGameLoop() {
        running = false;
        boolean retry = true;
        while(retry) {
            try {
                if (thread != null) thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        Log.i("GameManager", "Game loop thread stopped.");
    }

    public void pause() {
        running = false;
        Log.i("GameManager", "Game paused.");
    }

    public void resume() {
        if (surfaceReady) {
            Log.i("GameManager", "Game resumed signal received.");
        } else {
            Log.i("GameManager", "Resume called but surface not ready.");
        }
    }

    public void setDirection(Direction newDirection) {
        if ((currentDirection == Direction.UP && newDirection == Direction.DOWN) || (currentDirection == Direction.DOWN && newDirection == Direction.UP) ||
                (currentDirection == Direction.LEFT && newDirection == Direction.RIGHT) || (currentDirection == Direction.RIGHT && newDirection == Direction.LEFT)) {
            return;
        }
        currentDirection = newDirection;
    }

    private void initGame() {
        Log.i("GameManager_QuoteDebug", "initGame called. Resetting quote variables.");
        loadAndApplySnakeColor();
        snakeSegments.clear();
        score = 0;
        currentDirection = Direction.RIGHT;
        isGameOver = false;

        showGameOverFlash = false;
        gameOverFlashFramesRemaining = 0;
        justBecameGameOver = false;

        gameOverQuote = null;
        gameOverQuoteAuthor = null;
        quoteLayout = null;

        if (scrWidth == 0 || scrHeight == 0) {
            Log.e("GameManager", "Cannot initGame fully: dimensions are zero.");
            return;
        }
        int startX = (scrWidth / segmentSize / 2) * segmentSize;
        int startY = (scrHeight / segmentSize / 2) * segmentSize;
        startX = Math.max(segmentSize * 2, startX);
        startY = Math.max(0, startY);

        if (startX >= (2 * segmentSize) && startX < scrWidth && startY < scrHeight) {
            snakeSegments.addFirst(new Point(startX - (2 * segmentSize), startY));
            snakeSegments.addFirst(new Point(startX - segmentSize, startY));
            snakeSegments.addFirst(new Point(startX, startY));
            placeFood();
            if (gameActivity != null) gameActivity.updateScore(score); // עדכון ראשוני של הניקוד ב-UI
            Log.d("GameManager", "Game initialized.");
        } else {
            Log.e("GameManager", "Could not initialize snake within bounds.");
            handleGameOver(); // קריאה למתודה המרכזית במקרה של שגיאת אתחול
        }
    }

    private void placeFood() {
        if (scrWidth == 0 || scrHeight == 0) return;
        int numBlocksWide = scrWidth / segmentSize;
        int numBlocksHigh = scrHeight / segmentSize;
        if (numBlocksWide <= 0 || numBlocksHigh <= 0) return;
        boolean placedOnSnake;
        do {
            placedOnSnake = false;
            foodPosition.set(random.nextInt(numBlocksWide) * segmentSize, random.nextInt(numBlocksHigh) * segmentSize);
            for (Point s : snakeSegments) if (s.x == foodPosition.x && s.y == foodPosition.y) { placedOnSnake = true; break; }
        } while (placedOnSnake);
    }

    // מתודה מרכזית לטיפול בסיום המשחק
    private void handleGameOver() {
        if (!isGameOver) {
            isGameOver = true;
            justBecameGameOver = true;
            Log.d("GameManager_QuoteDebug", "Game Over! Final Score for " + currentUsername + ": " + score);

            // שמירת הניקוד ב-Firebase
            if (myFBDB != null && currentUsername != null && !currentUsername.isEmpty()) {
                Log.i("GameManager_Firebase", "Attempting to update high score for user: " + currentUsername + " with score: " + score);
                myFBDB.updateUserHighScore(currentUsername, score, new MyFBDB.ScoreCallback() {
                    @Override
                    public void onResult(int updatedScore, Exception error) {
                        if (error != null) {
                            Log.e("GameManager_Firebase", "Error updating high score for " + currentUsername + ": " + error.getMessage());
                        } else {
                            Log.i("GameManager_Firebase", "High score for " + currentUsername + " updated in Firebase to: " + updatedScore);
                        }}});
            } else {
                Log.w("GameManager_Firebase", "Cannot update high score: myFBDB is " + (myFBDB == null ? "null" : "not null") +
                        ", currentUsername is " + (currentUsername == null || currentUsername.isEmpty() ? "null/empty" : currentUsername));
            }
            // בקשת ציטוט
            if (quoteFetcher != null && gameOverQuote == null) {
                Log.i("GameManager_QuoteDebug", "Fetching quote because game over and no quote exists.");
                quoteFetcher.fetchRandomQuote(this);
            }
            // הצגת כפתור הפעלה מחדש
            if (gameActivity != null) {
                gameActivity.showGameOverUI();
            }}}

    private void updateGame() { //אחראית על לוגיקת המשחק ועדכונה
        if (isGameOver) return;
        if (snakeSegments.isEmpty()) {
            Log.e("GameManager", "updateGame called with empty snakeSegments.");
            handleGameOver();
            return; }

        Point currentHead = snakeSegments.getFirst(); //ראש הנחש כיוון
        int newX = currentHead.x;
        int newY = currentHead.y;

        switch (currentDirection) { //שינוי כיוון התנועה
            case UP: newY -= segmentSize; break;
            case DOWN: newY += segmentSize; break;
            case LEFT: newX -= segmentSize; break;
            case RIGHT: newX += segmentSize; break; }

        if (newX < 0 || newX >= scrWidth || newY < 0 || newY >= scrHeight) {//בודק התנגשות בקיר
            Log.w("GameManager", "Collision with border.");
            handleGameOver();
            return;
        }
        for (Point segment : snakeSegments) {//בודק התנגשות עצמית
            if (newX == segment.x && newY == segment.y) {
                Log.w("GameManager", "Collision with self.");
                handleGameOver();
                return;
            }
        }
        Point newHeadPoint = new Point(newX, newY);//מוסיף עוד אורך לנחש
        snakeSegments.addFirst(newHeadPoint);

        if (newHeadPoint.x == foodPosition.x && newHeadPoint.y == foodPosition.y) { //במקרה והתפוח נאכל
            score++;
            if (gameActivity != null) gameActivity.updateScore(score); //  עדכון ה-UI של הניקוד הנוכחי
            Log.i("GameManager", "Food eaten! Score: " + score);
            placeFood();
        } else {
            snakeSegments.removeLast();
        }
    }

    private void drawSurface() {
        if (holder.getSurface().isValid()) {
            myCanvas = holder.lockCanvas();
            if (myCanvas == null) return;
            myCanvas.drawRect(0, 0, scrWidth, scrHeight, bgPaint); // רקע
            if (scaledAppleBitmap != null) { //ציור התפוח
                float drawX = foodPosition.x; float drawY = foodPosition.y;
                if (appleDisplaySize > segmentSize) {
                    drawX = foodPosition.x - (appleDisplaySize - segmentSize) / 2.0f;
                    drawY = foodPosition.y - (appleDisplaySize - segmentSize) / 2.0f;
                } else if (appleDisplaySize < segmentSize) {
                    drawX = foodPosition.x + (segmentSize - appleDisplaySize) / 2.0f;
                    drawY = foodPosition.y + (segmentSize - appleDisplaySize) / 2.0f;
                }
                myCanvas.drawBitmap(scaledAppleBitmap, drawX, drawY, null);
            } else {
                myCanvas.drawRect(foodPosition.x, foodPosition.y, foodPosition.x + segmentSize, foodPosition.y + segmentSize, foodPaint);
            }
            for (Point segment : snakeSegments) { //מוסיף ראש חדש לנחש
                myCanvas.drawRect(segment.x, segment.y, segment.x + segmentSize, segment.y + segmentSize, snakePaint);
            }
            if (!snakeSegments.isEmpty() && !isGameOver() && eyePaint != null) { //משנה את כיוון העיניים
                Point head = snakeSegments.getFirst();
                float eyeRadius = segmentSize / 7f;
                float eyeVOffH = segmentSize / 3.5f, eyeHOffV = segmentSize / 3.5f, eyeFwdOff = segmentSize / 4f;
                float e1x=0,e1y=0,e2x=0,e2y=0;
                switch(currentDirection){
                    case RIGHT: e1x=head.x+segmentSize-eyeFwdOff;e2x=e1x;e1y=head.y+(segmentSize/2f)-eyeVOffH;e2y=head.y+(segmentSize/2f)+eyeVOffH;break;
                    case LEFT:  e1x=head.x+eyeFwdOff;e2x=e1x;e1y=head.y+(segmentSize/2f)-eyeVOffH;e2y=head.y+(segmentSize/2f)+eyeVOffH;break;
                    case UP:    e1x=head.x+(segmentSize/2f)-eyeHOffV;e2x=head.x+(segmentSize/2f)+eyeHOffV;e1y=head.y+eyeFwdOff;e2y=e1y;break;
                    case DOWN:  e1x=head.x+(segmentSize/2f)-eyeHOffV;e2x=head.x+(segmentSize/2f)+eyeHOffV;e1y=head.y+segmentSize-eyeFwdOff;e2y=e1y;break;
                }
                myCanvas.drawCircle(e1x,e1y,eyeRadius,eyePaint); myCanvas.drawCircle(e2x,e2y,eyeRadius,eyePaint);
                if(eyePaint.getColor() != Color.BLACK){
                    Paint pupilPaint = new Paint(); pupilPaint.setColor(Color.BLACK);
                    float pupilRadius = eyeRadius / 2.5f;
                    myCanvas.drawCircle(e1x, e1y, pupilRadius, pupilPaint); myCanvas.drawCircle(e2x, e2y, pupilRadius, pupilPaint);
                }
            }
            if (showGameOverFlash && gameOverFlashPaint != null) { //פלאש
                myCanvas.drawRect(0, 0, scrWidth, scrHeight, gameOverFlashPaint);
            }
            if (isGameOver()) { //מתודת סיום המשחק
                float gameOverTextY = scrHeight / 2f;
                if (quoteLayout != null) {
                    gameOverTextY = (scrHeight / 2f) - (quoteLayout.getHeight() / 2f) - (gameOverPaint.getTextSize() / 2f) - 10;
                }
                myCanvas.drawText("Game Over!", scrWidth / 2f, gameOverTextY, gameOverPaint);
                if (quoteLayout != null) {
                    myCanvas.save();
                    float quoteY = gameOverTextY + gameOverPaint.getTextSize() * 0.5f + 20;
                    myCanvas.translate((scrWidth - quoteLayout.getWidth()) / 2f, quoteY);
                    quoteLayout.draw(myCanvas);
                    myCanvas.restore();
                }
            }
            holder.unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();
            if (justBecameGameOver) { //אחראי על ההבהוב
                showGameOverFlash = true;
                gameOverFlashFramesRemaining = GAME_OVER_FLASH_TOTAL_FRAMES;
                justBecameGameOver = false;
                Log.d("GameManager", "Flash effect initiated.");
            }
            if (showGameOverFlash) {
                gameOverFlashFramesRemaining--;
                if (gameOverFlashFramesRemaining <= 0) showGameOverFlash = false;
            }
            if (!isGameOver && surfaceReady) updateGame(); //שליטה בקצב פריימים
            if (surfaceReady) drawSurface();
            long timeThisFrame = System.currentTimeMillis() - startTime;
            long sleepTime = FRAME_RATE_MS - timeThisFrame;
            if (sleepTime > 0) try { Thread.sleep(sleepTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        Log.i("GameManager", "Exiting run loop.");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { Log.i("GameManager", "Surface created."); }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("GameManager", "Surface changed: " + width + "x" + height);
        if (width > 0 && height > 0) {
            scrWidth = width; scrHeight = height; surfaceReady = true;
            initGame(); startGameLoop();
            if (gameOverQuote != null && quoteLayout == null && isGameOver()) {
                Log.d("GameManager_QuoteDebug", "Surface changed, quote exists but layout is null. Preparing layout.");
                prepareQuoteLayout();
            }
        } else { surfaceReady = false; }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("GameManager", "Surface destroyed.");
        surfaceReady = false; stopGameLoop();
        if (quoteFetcher != null) quoteFetcher.shutdown();
    }

    public void restartGame() {
        Log.i("GameManager_QuoteDebug", "restartGame called.");
        initGame();
    }
}
