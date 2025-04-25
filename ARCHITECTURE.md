# Architecture Overview

## 1. Current Architecture

The project uses a simple structure centered around `MainActivity` hosting a custom `SurfaceView` (`GameManager`) for the game logic and rendering.

- **Presentation Layer**: `MainActivity.java` manages the overall screen layout (`activity_main.xml`), displays the score (`TextView`), handles user input via buttons, and controls the lifecycle of the `GameManager`. `activity_main.xml` defines the layout using `ConstraintLayout`, including the score `TextView`, a `FrameLayout` container for the `SurfaceView`, and a `LinearLayout` for control buttons.
- **Game Logic/Rendering Layer**: `GameManager.java` extends `SurfaceView` and implements `Runnable` and `SurfaceHolder.Callback`. It contains the main game loop (`run`), handles snake movement, collision detection, food placement, scoring logic (`updateGame`), and draws the game state (snake, food, background, game over message) onto the `Canvas` (`drawSurface`). It receives directional input from `MainActivity` and updates the score via a callback to `MainActivity`.
- **Data Layer (Legacy/Inactive)**: Code for user authentication and storage using local files (`UserFileStorage.java`) and Firebase Realtime Database (`MyFBDB.java`, `User.java`) exists but is not currently integrated into the main game flow.
- **Domain Layer**: No separate domain layer exists. Game logic is within `GameManager`, UI logic within `MainActivity`.

## 2. Key Components

### Game Module
*   **`MainActivity.java`:** The main entry point and host activity.
    *   Sets up the layout (`activity_main.xml`).
    *   Initializes and adds the `GameManager` instance to the `FrameLayout`.
    *   Manages `GameManager`'s lifecycle (`onResume`, `onPause`).
    *   Handles button clicks to set the snake's direction via `gameManager.setDirection()`.
    *   Displays the current score in a `TextView`, updated via `updateScore()` method called by `GameManager`.
*   **`GameManager.java` (extends `SurfaceView`, implements `Runnable`, `SurfaceHolder.Callback`):**
    *   Manages the `SurfaceHolder` lifecycle (`surfaceCreated`, `surfaceChanged`, `surfaceDestroyed`) to get canvas dimensions and control the game thread.
    *   Runs the main game loop in a separate `Thread` (`run`).
    *   Contains game state: `snakeSegments` (`LinkedList<Point>`), `foodPosition` (`Point`), `score` (int), `currentDirection` (`Direction`), `isGameOver` (boolean).
    *   Implements core game logic in `updateGame()`: calculates new head position, checks border/self-collision, handles food eating (increment score, call `mainActivity.updateScore()`, place new food, grow snake), moves snake by adding head/removing tail.
    *   Draws game elements (background, food, snake, game over text) onto the `Canvas` in `drawSurface()`, using dimensions obtained from `surfaceChanged`.
    *   Uses a fixed delay (`FRAME_RATE_MS`) in `run()` to control game speed.
*   **`Direction.java`:** An enum defining the possible movement directions (UP, DOWN, LEFT, RIGHT).
*   **`activity_main.xml`:** The layout file for the game screen.
    *   Uses `ConstraintLayout`.
    *   Contains `TextView` (`scoreTextView`) at the top.
    *   Contains `FrameLayout` (`gameSurfaceContainer`) in the center to hold the `GameManager`.
    *   Contains `LinearLayout` (`controlsLayout`) at the bottom with four `Button`s for directions.

### Authentication/User Management (Legacy/Inactive)
- **Code:** `UserFileStorage.java`, `MyFBDB.java`, `User.java`, `RegActivityFile.java`, `RegActivityFB.java`.
- **Functionality:** Provides user registration/login via local file or Firebase RTDB.
- **Status:** Not connected to the current `MainActivity` game flow.

### UI Components
- Standard Android SDK components (`TextView`, `FrameLayout`, `LinearLayout`, `Button`) defined in `activity_main.xml`.
- `SurfaceView` (`GameManager`) used for custom drawing.

## 3. Testing Strategy

- Currently relies on manual execution and verification of the game.
- No automated tests (Unit, UI) implemented for the core game components (`MainActivity`, `GameManager`).
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

## 6. Project Timeline

1. **Week 1-2**: Project setup and authentication implementation
2. **Week 3-4**: Core game mechanics and basic UI
3. **Week 5-6**: Score management and leaderboard
4. **Week 7-8**: Testing, polish, and release preparation 