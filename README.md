# Snake Game

An Android implementation of the classic Snake game.

## Features (Current & Planned)

- **User Registration and Login:** Currently uses local file storage (`UserFileStorage.java`). Firebase implementation (`RegActivityFB.java`, `MyFBDB.java`) is present but inactive.
- **Gameplay:** Basic game structure (`level1.java`, `GameManager.java`) exists but requires implementation.
- **Score Tracking:** Planned.
- **Leaderboard:** Planned.
- **Multiple Levels:** Planned.

## Technical Stack (Current)

- Android (Java)
- Local JSON file for user storage
- Firebase Realtime Database (code exists, inactive)
- Basic Activity/Layout structure

## Project Structure

(Structure differs from initial template, needs update)

```
SnakeGame/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/snake/
│   │   │   ├── MainActivity.java       # Login/Help Screen
│   │   │   ├── RegActivity.java        # Contains RegActivityFile class (File Registration)
│   │   │   ├── RegActivityFB.java      # Firebase Registration (Inactive)
│   │   │   ├── User.java             # User Data Model
│   │   │   ├── UserFileStorage.java  # Handles user persistence to local file
│   │   │   ├── MyFBDB.java           # Handles Firebase DB interactions (Inactive)
│   │   │   ├── level1.java           # Game Activity (Level 1)
│   │   │   ├── GameManager.java      # Game Logic SurfaceView
│   │   │   └── ... (other potential classes like level2)
│   │   ├── res/
│   │   │   ├── layout/               # XML Layouts (activity_main, activity_reg, activity_reg_fb, ...)
│   │   │   └── ... (drawables, values, etc.)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts              # App-level build script
│   └── google-services.json        # Firebase config
├── build.gradle.kts                  # Project-level build script
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── README.md
├── ARCHITECTURE.md
├── TASKS.md
└── CHANGELOG.md
```

## Firebase Schema (Planned/Inactive)

### Users Collection (`/Users`)
```json
{
  "users": {
    "generatedUserId": {
      "key": "generatedUserId", // Stored within the object as well
      "userName": "string",
      "password": "string", // Note: Stored as plain text currently
      "level": 0,
      "score": 0,
      "coins": 0
    }
  }
}
```

## Setup Instructions

1.  Clone the repository: `git clone <repository-url>`
2.  Open the project in Android Studio.
3.  **Firebase Setup (Required for `RegActivityFB` or future use):**
    *   Create a Firebase project at [https://console.firebase.google.com/](https://console.firebase.google.com/).
    *   Add an Android app to the project with the package name `com.example.snake`.
    *   Download the generated `google-services.json` file.
    *   Place the `google-services.json` file in the `SnakeGame/app/` directory, replacing the existing one if necessary.
    *   In the Firebase console, navigate to **Build** -> **Realtime Database**.
    *   Click **Create database** and choose a location.
    *   Start in **test mode** for initial development (allows open read/write access - **change rules for production!**).
4.  Sync Gradle files in Android Studio (`File` > `Sync Project with Gradle Files`).
5.  Build and run the project (`Run` > `Run 'app'`).

## Testing

Currently, no automated tests are implemented. Manual testing:
- Run the app.
- Register a new user using the 'Register' button (uses file storage).
- Log in with the registered user or the test user (`testuser`/`abc123`) using the 'Play' button.

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details 