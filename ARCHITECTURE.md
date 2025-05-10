# Architecture Overview

## 1. Current Architecture

The project uses a simple structure centered around activities for each screen (`MainActivity`, `GameActivity`, `settings`) with `GameActivity` hosting a custom `SurfaceView` (`GameManager`) for the game logic and rendering.

- **Presentation Layer**: 
    - `MainActivity.java` handles user authentication and registration using both local storage and Firebase.
    - `GameActivity.java` manages the game screen layout (`activity_game.xml`), displays the score and user stats, handles user input, and controls the lifecycle of `GameManager`.
    - `settings.java` provides customization options for the game appearance and sound settings.
    - Each activity's layout is defined using `ConstraintLayout`.
- **Game Logic/Rendering Layer**: `GameManager.java` extends `SurfaceView` and implements `Runnable` and `SurfaceHolder.Callback`. It contains the main game loop, handles snake movement, collision detection, food placement, scoring logic, and draws the game state.
- **Data Layer**:
    - `UserFileStorage.java` handles local user authentication and storage.
    - `MyFBDB.java` provides Firebase integration for user authentication and high score tracking.
    - `PrefsManager.java` manages game preferences such as snake color.
- **Navigation**: Properly managed activity transitions using `ActivityResultLauncher` for settings navigation and intent flags for other transitions.

## 2. Key Components

### Game Module
*   **`GameActivity.java`:** The main game activity.
    *   Sets up the layout (`activity_game.xml`).
    *   Initializes and adds the `GameManager` instance to the `FrameLayout`.
    *   Manages `GameManager`'s lifecycle (`onResume`, `onPause`).
    *   Handles button clicks to set the snake's direction via `gameManager.setDirection()`.
    *   Displays user stats: username, current score, personal high score, global high score.
    *   Interacts with Firebase to retrieve and update high scores.
    *   Uses `ActivityResultLauncher` for proper navigation to settings.
    *   Implements custom `onBackPressed` handling with confirmation dialog.
*   **`GameManager.java` (extends `SurfaceView`, implements `Runnable`, `SurfaceHolder.Callback`):**
    *   Manages the main game loop, game state, and rendering.
    *   Implements methods to refresh appearance based on settings changes.
    *   Updates the game UI based on player actions and game events.

### Settings Module
*   **`settings.java`:** Handles game customization options.
    *   Provides snake color selection options.
    *   Toggles background music.
    *   Properly handles navigation back to the calling activity (MainActivity or GameActivity).
    *   Uses `PrefsManager` to store and retrieve user preferences.

### User Authentication Module
*   **`MainActivity.java`:** Handles user login and registration.
    *   Authenticates against both local storage and Firebase.
    *   Provides progress indicators during authentication.
    *   Handles error scenarios with user feedback.
    *   Launches game with user credentials on successful login.
*   **`RegActivityFile.java`:** Handles user registration.
    *   Registers users in both local storage and Firebase.
    *   Validates user input and provides feedback.
*   **`UserFileStorage.java`:** Manages local user data.
    *   Stores user information in JSON format.
    *   Provides methods to check credentials and save users.
*   **`MyFBDB.java`:** Handles Firebase operations.
    *   Manages user authentication against Firebase.
    *   Tracks and updates high scores in the cloud database.
    *   Provides asynchronous callbacks for database operations.

### Core Gameplay Loop and Interaction

1.  **User Authentication:**
    *   User enters credentials in `MainActivity`.
    *   App checks credentials against local storage first, then Firebase if needed.
    *   On successful authentication, app launches `GameActivity` with the username.

2.  **Game Initialization:**
    *   `GameActivity` starts and sets up the UI with user information.
    *   It creates a `GameManager` instance and adds it to the surface container.
    *   It retrieves personal and global high scores from Firebase.
    *   It sets up control buttons and navigation.

3.  **Gameplay:**
    *   User controls the snake using direction buttons.
    *   `GameManager` handles game logic and updates the score.
    *   On score changes, `GameActivity` updates UI and checks for high score updates.
    *   High scores are saved to Firebase for persistence.

4.  **Game Over:**
    *   When collision occurs, `isGameOver` is set to true.
    *   `GameActivity` shows the restart button.
    *   User can restart or navigate back to `MainActivity`.

5.  **Settings Navigation:**
    *   User can access settings during gameplay or from the main menu.
    *   `settings` activity preserves the calling activity context.
    *   When returning from settings, the app restores the appropriate state.
    *   Settings changes (like snake color) are immediately applied.

6.  **Back Navigation:**
    *   Custom back button handling in `GameActivity` shows a confirmation dialog during active gameplay.
    *   Settings activity properly returns to the calling activity without disrupting state.
    *   MainActivity preserves login state when returning from settings.

## 3. Testing Strategy

*   Manual testing of gameplay and navigation.
*   Testing of Firebase integration and fallback to local storage.
*   Testing activity transitions and state preservation.
*   Testing error scenarios (network failures, login errors).

## 4. Security Considerations

*   **Passwords:** Currently stored and checked as plain text in both local storage and Firebase. This should be improved with hashing.
*   **Firebase Rules:** Proper database rules needed for production to restrict access appropriately.
*   **Input Validation:** Basic checks for empty fields and invalid inputs.
*   **Network Handling:** App can function with or without network connectivity.

## 5. Dependencies

Key dependencies include:
*   AndroidX libraries (appcompat, activity, constraintlayout)
*   Google Material Components
*   Firebase Realtime Database
*   Google Play Services plugin
