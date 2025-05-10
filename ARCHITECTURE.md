# Architecture Overview

## 1. Current Architecture

The project uses a simple structure centered around `GameActivity` hosting a custom `SurfaceView` (`GameManager`) for the game logic and rendering, with `MainActivity` handling user authentication.

- **Presentation Layer**: 
    - `MainActivity.java` handles user authentication and registration.
    - `GameActivity.java` manages the game screen layout (`activity_game.xml`), displays the score (`TextView`), handles user input via buttons (including back and restart), and controls the lifecycle of the `GameManager`. `activity_game.xml` defines the layout using `ConstraintLayout`, including the score `TextView`, a `FrameLayout` container for the `SurfaceView`, direction controls, back button, and restart button.
- **Game Logic/Rendering Layer**: `GameManager.java` extends `SurfaceView` and implements `Runnable` and `SurfaceHolder.Callback`. It contains the main game loop (`run`), handles snake movement, collision detection, food placement, scoring logic (`updateGame`), and draws the game state (snake, food, background, game over message) onto the `Canvas` (`drawSurface`). It receives directional input from `GameActivity` and updates the score via a callback to `GameActivity`.
- **Data Layer (Legacy/Inactive)**: Code for user authentication and storage using local files (`UserFileStorage.java`) and Firebase Realtime Database (`MyFBDB.java`, `User.java`) exists but is not currently integrated into the main game flow.
- **Domain Layer**: No separate domain layer exists. Game logic is within `GameManager`, UI logic within `GameActivity`.

## 2. Key Components

### Game Module
*   **`GameActivity.java`:** The main entry point and host activity.
    *   Sets up the layout (`activity_game.xml`).
    *   Initializes and adds the `GameManager` instance to the `FrameLayout`.
    *   Manages `GameManager`'s lifecycle (`onResume`, `onPause`).
    *   Handles button clicks to set the snake's direction via `gameManager.setDirection()`.
    *   Displays the current score in a `TextView`, updated via `updateScore()` method called by `GameManager`.
*   **`GameManager.java` (extends `SurfaceView`, implements `Runnable`, `SurfaceHolder.Callback`):**
    *   Manages the `SurfaceHolder` lifecycle (`surfaceCreated`, `surfaceChanged`, `surfaceDestroyed`) to get canvas dimensions and control the game thread.
    *   Runs the main game loop in a separate `Thread` (`run`).
    *   Contains game state: `snakeSegments` (`LinkedList<Point>`), `foodPosition` (`Point`), `score` (int), `currentDirection` (`Direction`), `isGameOver` (boolean).
    *   Implements core game logic in `updateGame()`: calculates new head position, checks border/self-collision, handles food eating (increment score, call `gameActivity.updateScore()`, place new food, grow snake), moves snake by adding head/removing tail.
    *   Draws game elements (background, food, snake, game over text) onto the `Canvas` in `drawSurface()`, using dimensions obtained from `surfaceChanged`.
    *   Uses a fixed delay (`FRAME_RATE_MS`) in `run()` to control game speed.
*   **`Direction.java`:** An enum defining the possible movement directions (UP, DOWN, LEFT, RIGHT).
*   **`activity_game.xml`:** The layout file for the game screen.
    *   Uses `ConstraintLayout`.
    *   Contains `TextView` (`scoreTextView`) at the top.
    *   Contains `FrameLayout` (`gameSurfaceContainer`) in the center to hold the `GameManager`.
    *   Contains `LinearLayout` (`controlsLayout`) at the bottom with four `Button`s for directions.
    *   Includes a `Button` (`restartButton`) centered on the game surface, initially hidden.
    *   Includes an `ImageButton` (`backButton`) in the top-left corner.

### Core Gameplay Loop and Interaction (For New Developers)

1.  **Initialization:**
    *   `GameActivity` starts and sets its layout (`activity_game.xml`).
    *   It creates an instance of `GameManager` (a `SurfaceView`) and adds it to the `gameSurfaceContainer` FrameLayout.
    *   It finds UI elements: score `TextView`, direction `ImageButton`s, back `ImageButton`, and restart `Button`.
    *   It sets `OnClickListener`s for the direction buttons, which call `gameManager.setDirection()`.
    *   It sets an `OnClickListener` for the back button to navigate back to `MainActivity`.
    *   It sets an `OnClickListener` for the restart button to call `gameManager.restartGame()`.
2.  **Surface Ready:**
    *   When the `GameManager`'s surface is created and its size is known (`surfaceChanged`), it calls `initGame()`.
    *   `initGame()`: Resets the score, snake position/segments, places initial food, sets `isGameOver` to false, and calls `gameActivity.updateScore()`.
    *   `surfaceChanged` then calls `startGameLoop()` which creates and starts the game thread if not already running.
3.  **Game Loop (`GameManager.run()`):**
    *   This loop runs continuously on a background thread while `running` is true.
    *   Inside the loop, if the game is not over (`!isGameOver`) and the surface is ready:
        *   `updateGame()` is called:
            *   Calculates the snake's next head position based on `currentDirection`.
            *   Checks for border collisions (sets `isGameOver = true`, calls `gameActivity.showRestartButton()`).
            *   Checks for self-collisions (sets `isGameOver = true`, calls `gameActivity.showRestartButton()`).
            *   If no collision: Adds the new head. Checks if the new head is on food.
            *   If food eaten: Increments score, calls `gameActivity.updateScore()`, calls `placeFood()`, *doesn't* remove the tail (snake grows).
            *   If no food eaten: Removes the tail segment.
        *   `drawSurface()` is called:
            *   Locks the `Canvas`.
            *   Draws the background, food, snake segments.
            *   If `isGameOver` is true, draws "Game Over!" text.
            *   Unlocks and posts the `Canvas`.
    *   The loop then calculates the time taken for the update/draw and sleeps for `FRAME_RATE_MS` minus the elapsed time to control the speed (currently `200ms`).
4.  **Game Over:**
    *   When a collision occurs, `isGameOver` is set to true.
    *   The `GameManager` calls `gameActivity.showRestartButton()` to make the restart button visible.
    *   The `run()` loop stops calling `updateGame()` but continues calling `drawSurface()` (to keep showing the "Game Over!" message).
5.  **Restart:**
    *   User clicks the restart button.
    *   `GameActivity`'s listener calls `gameManager.restartGame()`.
    *   `gameManager.restartGame()` simply calls `initGame()` again, resetting the state.
    *   `GameActivity`'s listener also calls `hideRestartButton()`.
6.  **Pause/Resume:**
    *   `GameActivity.onPause()` calls `gameManager.pause()`, which sets `running = false`, stopping the `run()` loop's logic execution.
    *   `GameActivity.onResume()` calls `gameManager.resume()`, which sets `running = true`, allowing the loop logic to execute again. If the surface was destroyed and recreated, `surfaceChanged` will handle restarting the thread via `startGameLoop()`.
7.  **Back Navigation:**
    *   User clicks the back button.
    *   `GameActivity`'s listener creates an `Intent` for `MainActivity`, clears the task stack above it, starts `MainActivity`, and finishes `GameActivity`.

### Authentication/User Management (Legacy/Inactive)
- **Code:** `UserFileStorage.java`, `MyFBDB.java`, `User.java`, `RegActivityFile.java`, `RegActivityFB.java`.
- **Functionality:** Provides user registration/login via local file or Firebase RTDB.
- **Status:** Not connected to the current `GameActivity` game flow.

### UI Components
- Standard Android SDK components (`TextView`, `FrameLayout`, `LinearLayout`, `Button`) defined in `activity_game.xml`.
- `SurfaceView` (`GameManager`) used for custom drawing.

## 3. Testing Strategy

- Currently relies on manual execution and verification of the game.
- No automated tests (Unit, UI) implemented for the core game components (`GameActivity`, `GameManager`).
- Instrumented tests exist for legacy authentication components (`UserFileStorage`, `MyFBDB`).

## 4. Security Considerations

- **Passwords:** Currently stored and checked as plain text in both the local `users.json` file and potentially in Firebase via `MyFBDB`. **This is insecure and needs to be addressed (e.g., using hashing).**
- **Firebase Rules:** If using Firebase, the Realtime Database rules need to be configured for production to restrict access appropriately (currently likely requires test mode for `MyFBDB` to function).
- **Input Validation:** Basic checks for empty username/password exist.

## 5. Dependencies

(Refer to `app/build.gradle.kts` and `gradle/libs.versions.toml` for specific dependencies and versions.)

Key dependencies include:
- AndroidX libraries (appcompat, activity, constraintlayout)
- Google Material Components
- Firebase Realtime Database (`firebase-database`)
- Google Play Services plugin (`google-gms-google-services`)
- Kotlin standard library (via `org.jetbrains.kotlin.android` plugin)
