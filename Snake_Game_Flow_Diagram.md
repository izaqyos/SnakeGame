# Snake Game Application Flow

```mermaid
flowchart TD
    %% Main application entry and authentication flows
    Start([App Launch]) --> MainActivity
    MainActivity --> LoginCheck{Check User Credentials}
    LoginCheck -->|Local Storage| ValidLocal{Valid?}
    ValidLocal -->|Yes| StartGame
    ValidLocal -->|No| CheckFirebase{Check Firebase}
    CheckFirebase -->|Valid| StoreLocal[Save to Local Storage]
    StoreLocal --> StartGame
    CheckFirebase -->|Invalid| ShowError[Show Error Message]
    
    %% Registration flow
    MainActivity --> Register[RegisterActivity]
    Register --> SaveUser[Save User Credentials]
    SaveUser --> SaveToLocal[Save to Local Storage]
    SaveUser --> SaveToFirebase[Save to Firebase]
    SaveToLocal --> ReturnToLogin[Return to Login]
    SaveToFirebase --> ReturnToLogin
    ReturnToLogin --> MainActivity
    
    %% Game initialization and main loop
    StartGame --> GameActivity
    GameActivity --> InitGame[Initialize GameManager]
    GameActivity --> LoadHighScores[Load High Scores from Firebase]
    InitGame --> StartGameLoop[Start Game Loop]
    
    %% Main game loop
    StartGameLoop --> GameLoop[Game Loop]
    GameLoop --> CheckGameOver{Game Over?}
    CheckGameOver -->|No| UpdateGame[Update Game State]
    UpdateGame --> HandleInput[Process User Input]
    HandleInput --> CheckCollision{Check Collisions}
    CheckCollision -->|No Collision| CheckFood{Snake on Food?}
    CheckFood -->|Yes| IncrementScore[Increment Score]
    IncrementScore --> UpdateScoreUI[Update Score Display]
    UpdateScoreUI --> PlaceNewFood[Place New Food]
    PlaceNewFood --> DrawFrame[Draw Game Frame]
    CheckFood -->|No| MoveSnake[Move Snake]
    MoveSnake --> DrawFrame
    DrawFrame --> GameLoop
    
    %% Game over flow
    CheckCollision -->|Collision| GameOver[Game Over]
    CheckGameOver -->|Yes| GameOver
    GameOver --> SaveHighScore[Save High Score to Firebase]
    SaveHighScore --> ShowRestartButton[Show Restart UI]
    ShowRestartButton --> RestartOption{User Choice}
    RestartOption -->|Restart| InitGame
    RestartOption -->|Exit| MainActivity
    
    %% Settings flow
    GameActivity --> SettingsButton[Settings Button]
    SettingsButton --> SettingsActivity
    SettingsActivity --> ColorSelection[Change Snake Color]
    SettingsActivity --> MusicToggle[Toggle Background Music]
    ColorSelection --> SavePreferences[Save Preferences]
    MusicToggle --> SavePreferences
    SavePreferences --> ReturnToGame[Return to Game]
    ReturnToGame --> RefreshAppearance[Refresh Game Appearance]
    RefreshAppearance --> ResumeGame[Resume Game]
    
    %% Navigation handling
    GameActivity --> BackPressed[Back Button Pressed]
    BackPressed --> ConfirmExit{Confirm Exit?}
    ConfirmExit -->|Yes| MainActivity
    ConfirmExit -->|No| ResumeGame
    
    %% High scores
    GameActivity --> ViewHighScores[View High Scores]
    ViewHighScores --> HighScoresActivity
    HighScoresActivity --> LoadAllScores[Load All User Scores]
    LoadAllScores --> DisplayScores[Display Sorted Scores]
    DisplayScores --> ReturnOption{Return Option}
    ReturnOption -->|Back to Game| GameActivity
    ReturnOption -->|Main Menu| MainActivity

    %% Style definitions
    classDef activity fill:#f9f,stroke:#333,stroke-width:2px;
    classDef process fill:#bbf,stroke:#333,stroke-width:1px;
    classDef decision fill:#ffd,stroke:#333,stroke-width:1px;
    classDef data fill:#bfb,stroke:#333,stroke-width:1px;
    
    %% Apply styles
    class MainActivity,GameActivity,SettingsActivity,Register,HighScoresActivity activity;
    class InitGame,UpdateGame,DrawFrame,SaveHighScore,LoadHighScores,SavePreferences process;
    class LoginCheck,ValidLocal,CheckFirebase,CheckGameOver,CheckCollision,CheckFood,ConfirmExit,RestartOption,ReturnOption decision;
    class SaveToLocal,SaveToFirebase,StoreLocal,SaveUser data;
```

## Detailed Flow Description

### Authentication Flow
1. **App Launch**: The application starts with MainActivity
2. **Login Check**: User credentials are checked first in local storage
3. **Fallback Authentication**: If not in local storage, Firebase is checked
4. **Registration**: New users can register, saving data to both local storage and Firebase

### Game Initialization
1. **Start Game**: After authentication, GameActivity is launched
2. **Initialize**: GameManager initializes the game state (snake position, food, score)
3. **Load High Scores**: Personal and global high scores are loaded from Firebase

### Main Game Loop
1. **Game Loop**: Runs on a separate thread with fixed timing
2. **Update Game State**: 
   - Process user input (direction changes)
   - Check for collisions (walls/self)
   - Handle food consumption
   - Move snake
3. **Render**: Draw the updated game state on screen

### Game Over Flow
1. **Collision Detection**: Game ends when snake hits a wall or itself
2. **Save High Score**: If score is a new personal best, update Firebase
3. **User Options**: Player can restart the game or return to the main menu

### Settings and Customization
1. **Settings Screen**: Accessed from GameActivity
2. **Customization Options**: 
   - Snake color selection
   - Background music toggle
3. **Return Flow**: Settings are saved and immediately applied when returning to the game

### Navigation Handling
1. **Back Button**: Confirmation dialog prevents accidental exits
2. **Activity Transitions**: Managed to preserve state between screens
3. **Intent Handling**: Username and other data passed between activities

### High Score Tracking
1. **Personal Best**: Tracked and displayed during gameplay
2. **Global High Score**: Retrieved from Firebase to show top score
3. **High Scores Screen**: Displays leaderboard with all user scores 