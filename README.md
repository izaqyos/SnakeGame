# Snake Game

An Android implementation of the classic Snake game with user registration/login.

## Features

- **User Registration and Login:** Uses local file storage (`UserFileStorage.java`) via `MainActivity` and `RegActivityFile`.
- **Gameplay (in `GameActivity`):**
    - Classic snake movement controlled by on-screen buttons.
    - Snake grows by eating food.
    - Collision detection with walls and self.
    - Score tracking displayed on screen.
    - Game over state.
    - Adjustable game speed (currently set via `FRAME_RATE_MS` in `GameManager`).
    - Back button to return to the main (login/user) screen.
    - Restart button appears on Game Over.
- **Legacy/Inactive Code:** Firebase user storage (`MyFBDB.java`, `RegActivityFB.java`) exists but is not the primary path.

## Technical Stack

- Android (Java)
- Activities: `MainActivity` (Login/Reg), `RegActivityFile`, `GameActivity`.
- `SurfaceView` (`GameManager`) for game rendering and main loop within `GameActivity`.
- Local JSON file for user storage (`UserFileStorage`).
- `ConstraintLayout` for UI structure.
- `LinkedList` for snake body management.

## Project Structure

```
SnakeGame/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/snake/
│   │   │   ├── MainActivity.java       # Login/Registration Screen, navigates to GameActivity
│   │   │   ├── RegActivityFile.java  # User Registration (File storage)
│   │   │   ├── GameActivity.java       # Hosts GameManager, displays score, handles controls
│   │   │   ├── GameManager.java      # Game logic, drawing, loop (SurfaceView)
│   │   │   ├── Direction.java        # Enum for movement directions
│   │   │   ├── User.java             # User Data Model
│   │   │   ├── UserFileStorage.java  # Handles user persistence to local file
│   │   │   ├── MyFBDB.java           # Handles Firebase DB interactions (Legacy/Inactive)
│   │   │   └── RegActivityFB.java      # Firebase Registration Activity (Legacy/Inactive)
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml # Login/Registration screen layout
│   │   │   │   ├── activity_game.xml # Game screen layout (Score, SurfaceView container, Buttons)
│   │   │   │   ├── activity_reg_file.xml # Registration screen layout
│   │   │   │   └── ... (legacy layouts: dialog_help, activity_reg_fb, etc.)
│   │   │   └── ... (drawables, values, etc.)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json        # Firebase config (Legacy/Inactive)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── README.md
├── ARCHITECTURE.md
├── TASKS.md
└── CHANGELOG.md
```

## Setup Instructions

1.  Clone the repository: `git clone <repository-url>`
2.  Open the project in Android Studio.
3.  Sync Gradle files (`File` > `Sync Project with Gradle Files`).
4.  Build and run the project (`Run` > `Run 'app'`). The app will start at the login screen.

(Firebase setup instructions kept for reference but not required for the primary file-based login).

## Testing

- **Manual Testing:** Run the app. Register a user, then log in. Play the game.
- **Automated Testing:** Instrumented tests exist for legacy components (`UserFileStorage`, `MyFBDB`) but no tests are currently implemented for the core game logic (`GameManager`, `GameActivity`) or login/registration flow (`MainActivity`).

(Instructions for running legacy instrumented tests and viewing reports kept for reference).

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details 