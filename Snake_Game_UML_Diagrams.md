# Snake Game UML Diagrams

## Class Diagram

```
+---------------------+     +---------------------+     +---------------------+
|    MainActivity     |     |    GameActivity     |     |    settings         |
+---------------------+     +---------------------+     +---------------------+
| - userFileStorage   |     | - gameManager       |     | - musicSwitch       |
| - myFBDB            |     | - scoreTextView     |     | - sharedPrefs       |
| - etUserName        |     | - usernameTextView  |     | - buttonColorGreen  |
| - etPassword        |     | - highScoreTextView |     | - buttonColorRed    |
| - loginStatusText   |     | - gameSurfaceContainer|   | - buttonColorBlue   |
| - loginProgressBar  |     | - restartButton     |     | - buttonColorYellow |
+---------------------+     | - currentUsername   |     | - buttonColorWhite  |
| + onCreate()        |     | - myFBDB            |     | - callingActivity   |
| + checkUser()       |     +---------------------+     +---------------------+
| + toRegister()      |     | + onCreate()        |     | + onCreate()        |
| + launchGame()      |     | + updateScore()     |     | + handleBackNav()   |
| + saveLoginToPrefs()|     | + loadHighScores()  |     | + initColorButtons()|
+---------------------+     | + showGameOverUI()  |     | + saveColorAndNotify|
         |                  | + onBackPressed()   |     | + updateMusic()     |
         |                  +---------------------+     +---------------------+
         |                           |                           |
         v                           v                           |
+---------------------+     +---------------------+              |
| UserFileStorage     |     |    GameManager     |<-------------+
+---------------------+     +---------------------+
| - context           |     | - gameActivity      |
| - usersList         |     | - currentDirection  |
+---------------------+     | - snakeSegments     |
| + saveUser()        |     | - foodPosition      |
| + checkUser()       |     | - score             |
| + userExists()      |     | - isGameOver        |
| + loadUsersFromFile()|    | - snakePaint        |
+---------------------+     +---------------------+
         ^                  | + run()             |
         |                  | + updateGame()      |
         |                  | + drawSurface()     |
+---------------------+     | + setDirection()    |
|      MyFBDB         |     | + handleGameOver()  |
+---------------------+     | + initGame()        |
| - database          |     | + restartGame()     |
| - myRef             |     | + refreshAppearance()|
| - usersArrayList    |     +---------------------+
+---------------------+              |
| + saveUser()        |              v
| + checkUser()       |     +---------------------+
| + userExistsAsync() |     |    PrefsManager     |
| + getUserHighScore()|     +---------------------+
| + getGlobalHighScore|     | + PREF_NAME         |
| + updateHighScore() |     | + KEY_SNAKE_COLOR   |
+---------------------+     +---------------------+
         ^                  | + saveSnakeColor()  |
         |                  | + getSnakeColor()   |
         |                  +---------------------+
+---------------------+
|       User          |
+---------------------+
| - userName          |
| - password          |
| - score             |
| - key               |
+---------------------+
| + getters/setters   |
+---------------------+
```

## Activity Lifecycle Sequence Diagram

```
   User        MainActivity       GameActivity       Settings        GameManager
    |               |                  |                |                |
    |---> Launch    |                  |                |                |
    |               |                  |                |                |
    |               |---> onCreate()   |                |                |
    |               |     Setup UI     |                |                |
    |               |                  |                |                |
    |---> Login     |                  |                |                |
    |               |                  |                |                |
    |               |---> checkUser()  |                |                |
    |               |     Auth Process |                |                |
    |               |                  |                |                |
    |               |---> launchGame() |                |                |
    |               |     Intent       |                |                |
    |               |----------------->|                |                |
    |               |                  |                |                |
    |               |                  |---> onCreate() |                |
    |               |                  |     Setup UI   |                |
    |               |                  |                |                |
    |               |                  |---> loadHighScores()            |
    |               |                  |                |                |
    |               |                  |---> new GameManager()           |
    |               |                  |-------------------->|           |
    |               |                  |                |    |---> initGame() |
    |               |                  |                |    |           |
    |               |                  |                |    |---> startGameLoop() |
    |---> Play Game |                  |                |    |           |
    |               |                  |                |    |---> run() |
    |               |                  |                |    |     Game Loop |
    |               |                  |                |    |           |
    |---> Settings  |                  |                |    |           |
    |               |                  |---> settingsLauncher.launch()   |
    |               |                  |--------------->|    |           |
    |               |                  |                |    |           |
    |               |                  |                |---> onCreate() |
    |               |                  |                |     Setup UI   |
    |               |                  |                |                |
    |---> Change Color                 |                |                |
    |               |                  |                |                |
    |               |                  |                |---> saveSnakeColorAndNotify() |
    |               |                  |                |                |
    |---> Back      |                  |                |                |
    |               |                  |                |                |
    |               |                  |                |---> handleBackNavigation() |
    |               |                  |                |     finish()   |
    |               |                  |<---------------|                |
    |               |                  |                |                |
    |               |                  |---> onActivityResult() |        |
    |               |                  |---> gameManager.refreshAppearance() |
    |               |                  |-------------------->|            |
    |               |                  |                |    |---> loadAndApplySnakeColor() |
    |---> Game Over |                  |                |    |           |
    |               |                  |                |    |---> handleGameOver() |
    |               |                  |                |    |           |
    |               |                  |<--------------------|           |
    |               |                  |---> showGameOverUI() |          |
    |               |                  |                |    |           |
    |---> Restart   |                  |                |    |           |
    |               |                  |---> gameManager.restartGame()   |
    |               |                  |-------------------->|           |
    |               |                  |                |    |---> initGame() |
    |---> Back      |                  |                |    |           |
    |               |                  |---> onBackPressed() |           |
    |               |                  |     Show Dialog |    |           |
    |               |                  |                |    |           |
    |---> Confirm Exit                 |                |    |           |
    |               |                  |                |    |           |
    |               |                  |---> navigateToMainActivity()    |
    |               |<-----------------|                |    |           |
    |               |                  |                |    |           |
```

## Authentication Sequence Diagram

```
   User        MainActivity     UserFileStorage      MyFBDB         Firebase
    |               |                  |                |                |
    |---> Input Credentials           |                |                |
    |               |                  |                |                |
    |               |---> checkUser() |                |                |
    |               |----------------->|                |                |
    |               |                  |---> Check Local Storage        |
    |               |                  |                |                |
    |               |<-----------------|                |                |
    |               |     Result       |                |                |
    |               |                  |                |                |
    |               |---> If not found, userExistsAsync()               |
    |               |---------------------------------->|                |
    |               |                  |                |---> Query User |
    |               |                  |                |--------------->|
    |               |                  |                |<---------------|
    |               |                  |                |     Result     |
    |               |<----------------------------------|                |
    |               |     Result       |                |                |
    |               |                  |                |                |
    |               |---> If exists, checkUserAsync()   |                |
    |               |---------------------------------->|                |
    |               |                  |                |---> Verify Pwd |
    |               |                  |                |--------------->|
    |               |                  |                |<---------------|
    |               |                  |                |     Result     |
    |               |<----------------------------------|                |
    |               |     Result       |                |                |
    |               |                  |                |                |
    |               |---> If valid, saveUser()          |                |
    |               |----------------->|                |                |
    |               |     Save to local|                |                |
    |               |                  |                |                |
    |               |---> launchGame() |                |                |
```

## Game Logic Sequence Diagram

```
 GameActivity     GameManager         PrefsManager     MyFBDB         Firebase
    |                  |                  |                |                |
    |---> initGame()   |                  |                |                |
    |----------------->|                  |                |                |
    |                  |---> getSnakeColor()              |                |
    |                  |----------------->|                |                |
    |                  |<-----------------|                |                |
    |                  |     Color        |                |                |
    |                  |                  |                |                |
    |                  |---> Setup Game State             |                |
    |                  |                  |                |                |
    |---> Game Loop    |                  |                |                |
    |                  |                  |                |                |
    |                  |---> updateGame() |                |                |
    |                  |     Move Snake   |                |                |
    |                  |     Check Collisions              |                |
    |                  |     Check Food   |                |                |
    |                  |                  |                |                |
    |                  |---> If Food Eaten                 |                |
    |                  |     Increment Score               |                |
    |                  |                  |                |                |
    |<-----------------|                  |                |                |
    |---> updateScore()|                  |                |                |
    |                  |                  |                |                |
    |                  |---> drawSurface()|                |                |
    |                  |     Render Game  |                |                |
    |                  |                  |                |                |
    |                  |---> If Collision |                |                |
    |                  |     handleGameOver()              |                |
    |                  |                  |                |                |
    |                  |---> If High Score                 |                |
    |                  |---------------------------------->|                |
    |                  |                  |                |---> Update Score |
    |                  |                  |                |--------------->|
    |                  |                  |                |<---------------|
    |                  |                  |                |     Result     |
    |                  |<----------------------------------|                |
    |<-----------------|                  |                |                |
    |---> showGameOverUI()                |                |                |
```

## Component Dependencies Diagram

```
+---------------+       +---------------+       +---------------+
|               |       |               |       |               |
| MainActivity  +-------> GameActivity  +-------> GameManager   |
|               |       |               <-------+               |
+-------+-------+       +-------+-------+       +-------+-------+
        |                       |                       |
        |                       |                       |
        v                       v                       v
+-------+-------+       +-------+-------+       +-------+-------+
|               |       |               |       |               |
| MyFBDB        <-------+ HighScores    |       | PrefsManager  |
|               +-------> Activity      |       |               |
+-------+-------+       +---------------+       +---------------+
        |                                               ^
        |                                               |
        v                                               |
+-------+-------+                               +-------+-------+
|               |                               |               |
| User          |                               | settings      |
|               |                               |               |
+---------------+                               +---------------+
```

## Notes on UML Diagram Usage

The UML diagrams above provide a comprehensive view of the Snake Game application's architecture:

1. **Class Diagram**: Shows the main classes, their attributes, methods, and relationships.

2. **Activity Lifecycle Sequence Diagram**: Illustrates the flow of activity transitions and how they interact during the app's lifecycle, from launch to game over scenarios.

3. **Authentication Sequence Diagram**: Details the authentication process, showing how credentials are verified against local storage and Firebase.

4. **Game Logic Sequence Diagram**: Focuses on the main game loop, collision detection, scoring, and interaction with Firebase for high score updates.

5. **Component Dependencies Diagram**: Provides a high-level overview of how different components in the application depend on each other.

These diagrams can be used for:
- Onboarding new developers to understand the application structure
- Planning future enhancements by understanding current component relationships
- Identifying potential areas for refactoring or optimization
- Documenting the implemented architecture for future reference 