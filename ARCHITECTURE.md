# Architecture Overview

## 1. Current Architecture (Simplified)

The project currently uses a basic Activity-based structure without distinct layers like MVVM or Clean Architecture as initially planned in the template. Logic is primarily contained within Activities and specific helper classes.

- **Presentation Layer**: Standard Android Activities (`MainActivity`, `RegActivityFile`, `RegActivityFB`, `level1`) and XML layouts.
- **Data Layer**: Currently split between:
    - `UserFileStorage.java`: Active implementation for saving/loading users to a local JSON file (`users.json`).
    - `MyFBDB.java`: Inactive implementation for interacting with Firebase Realtime Database (primarily user CRUD).
- **Domain Layer**: No separate domain layer currently exists. Business logic (like user validation) is within Activities or the data helper classes.

## 2. Key Components

### Authentication/User Management
- **Active:** `UserFileStorage.java` handles creating, loading, and checking user credentials against a local JSON file.
- **Inactive:** `MyFBDB.java` provides methods to interact with the Firebase Realtime Database `/Users` node. `RegActivityFB.java` uses `MyFBDB`.
- **User Model:** `User.java` defines the user data structure.
- **UI:**
    - `MainActivity`: Login fields, navigates to registration or game.
    - `RegActivityFile`: Handles registration UI and logic for file storage.
    - `RegActivityFB`: Handles registration UI and logic for Firebase storage.

### Database (Storage)
- **Local File:** `users.json` stored in the app's internal storage, managed by `UserFileStorage`.
- **Firebase Realtime Database:** Configured in `google-services.json` and accessed via `MyFBDB.java`. The planned structure is a `/Users` node containing user objects keyed by Firebase's generated push ID.

### Game Module (Basic Structure)
- `level1.java`: Activity intended to host the game.
- `GameManager.java`: A `SurfaceView` likely intended to handle game rendering and core logic (implementation pending).

### UI Components
- Standard Android SDK components (Buttons, EditText, TextView, LinearLayout, ConstraintLayout) defined in XML.
- `Dialog` used for the Help screen in `MainActivity`.

## 3. Testing Strategy

- Currently, no automated testing frameworks (JUnit, Espresso) are implemented or configured.
- Testing relies on manual execution and verification.

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