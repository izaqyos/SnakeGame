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

Currently, only basic instrumented tests for `UserFileStorage` and `MyFBDB` are implemented. Manual testing:
- Run the app.
- Register a new user using the 'Register' button (uses file storage).
- Log in with the registered user or the test user (`testuser`/`abc123`) using the 'Play' button.

### Running Instrumented Tests from Command Line

A helper script (`run_tests.sh`) is provided to simplify running tests from the command line.

**Using the Test Script:**

1.  **Prerequisites:**
    *   **Device/Emulator:** Ensure exactly one Android device is connected via USB (with USB Debugging enabled) OR one Android emulator is running.
    *   **Verify JDK Path (First time):** Open `run_tests.sh` and confirm the `STUDIO_JDK_PATH` variable points to the correct embedded JDK location within your Android Studio application bundle (usually `/Applications/Android Studio.app/Contents/jbr/Contents/Home`). Adjust if necessary.
2.  **Make Script Executable (First time):** In your terminal, from the project root directory (`SnakeGame`), run:
    ```bash
    chmod +x run_tests.sh
    ```
3.  **Run the Script:** From the project root directory, execute:
    ```bash
    ./run_tests.sh
    ```

This script will automatically:
*   Set the `JAVA_HOME` environment variable.
*   Ensure `gradlew` has execute permissions.
*   Run `./gradlew clean`.
*   Run `./gradlew connectedDebugAndroidTest`.
*   Report the success or failure status.

**Manual Command (Alternative):**

If you prefer not to use the script, you still need to ensure the prerequisites are met manually:

1.  **Device/Emulator:** As above.
2.  **JDK Configuration:** Ensure `JAVA_HOME` is set correctly in your terminal (see script for example path).
3.  **Gradle Wrapper Permissions (macOS/Linux):** Ensure `./gradlew` is executable (`chmod +x gradlew`).

Then, run the Gradle command directly:

```bash
./gradlew connectedDebugAndroidTest
```

### Viewing Test Reports

After running the instrumented tests (either via the script or manually), an HTML report is generated.

**Report Location:**
`app/build/reports/androidTests/connected/debug/index.html`

**Viewing Options:**

1.  **Open Directly (macOS):**
    From the project root directory, run:
    ```bash
    open app/build/reports/androidTests/connected/debug/index.html
    ```

2.  **Serve via Python 3:**
    Navigate to the report directory and start a simple HTTP server:
    ```bash
    cd app/build/reports/androidTests/connected/debug/
    python3 -m http.server
    # Now open http://localhost:8000 (or the port indicated) in your browser.
    # Press Ctrl+C to stop. Remember to cd back to the project root.
    ```

3.  **Serve via Node.js/npx:**
    If you have Node.js/npm, run this from the project root:
    ```bash
    npx serve app/build/reports/androidTests/connected/debug/
    # Open the localhost URL provided in the output (e.g., http://localhost:3000).
    # Press Ctrl+C to stop.
    ```

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details 