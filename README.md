# Snake Game

An Android implementation of the classic Snake game with user registration/login and Firebase integration.

## Features

- **User Registration and Login:**
    - Uses local file storage (`UserFileStorage.java`) via `MainActivity` and `RegActivityFile`
    - Firebase Realtime Database integration for user authentication and high score tracking
    - Seamless fallback between local storage and cloud database
- **Gameplay (in `GameActivity`):**
    - Classic snake movement controlled by on-screen buttons
    - Snake grows by eating food
    - Collision detection with walls and self
    - Score tracking displayed on screen
    - Game over state with restart button
    - Back button confirmation to prevent accidental exits
    - High score tracking (personal and global)
    - Username display during gameplay
    - Settings for customization (snake color, music)
- **Settings and Customization:**
    - Change snake color with instant visual feedback
    - Toggle background music
    - Proper navigation between screens with state preservation

## Technical Stack

- Android (Java)
- Firebase Realtime Database for user data and high scores
- Activities: `MainActivity` (Login), `RegActivityFile` (Registration), `GameActivity` (Game), `settings` (Settings)
- `SurfaceView` (`GameManager`) for game rendering and main loop within `GameActivity`
- Local JSON file for user storage (`UserFileStorage`) with Firebase backup
- `ConstraintLayout` for UI structure
- `LinkedList` for snake body management
- `ActivityResultLauncher` for proper activity navigation

## Project Structure

```
SnakeGame/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/snake/
│   │   │   ├── MainActivity.java       # Login Screen, navigates to GameActivity
│   │   │   ├── RegActivityFile.java    # User Registration (File storage + Firebase)
│   │   │   ├── GameActivity.java       # Hosts GameManager, displays score, handles controls
│   │   │   ├── settings.java           # Settings screen for customization
│   │   │   ├── GameManager.java        # Game logic, drawing, loop (SurfaceView)
│   │   │   ├── PrefsManager.java       # Handles preferences (colors, settings)
│   │   │   ├── User.java               # User Data Model
│   │   │   ├── UserFileStorage.java    # Handles user persistence to local file
│   │   │   └── MyFBDB.java             # Handles Firebase DB interactions
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml   # Login screen layout
│   │   │   │   ├── activity_game.xml   # Game screen layout (Score, controls, etc.)
│   │   │   │   ├── activity_settings.xml # Settings screen layout
│   │   │   │   ├── activity_reg_file.xml # Registration screen layout
│   │   │   │   └── dialog_help.xml     # Help dialog layout
│   │   │   └── ... (drawables, values, etc.)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json        # Firebase config
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

1. Clone the repository: `git clone <repository-url>`
2. Open the project in Android Studio
3. Sync Gradle files (`File` > `Sync Project with Gradle Files`)
4. Build and run the project (`Run` > `Run 'app'`). The app will start at the login screen

### Firebase Setup (Required for full functionality)

1. Create a Firebase project at https://console.firebase.google.com/
2. Add your app to the Firebase project and download the updated `google-services.json`
3. Replace the existing `google-services.json` in the `app` directory
4. Enable Realtime Database in the Firebase console
5. Set database rules to allow read/write access (for development)

## Testing

- **Manual Testing:** Run the app. Register a user, then log in. Play the game. Check high score tracking.
- **Game Flow Testing:** Verify navigation between activities works correctly and state is preserved.
- **Network Testing:** Test the app with and without internet connection to verify fallback to local storage.

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details 