# Implementation Tasks

(Based on initial template, adjusted for current state)

## Phase 1: Project Setup
- [X] Initialize Android project (Java)
- [X] Set up Firebase project (for potential future use/legacy code)
- [-] Configure Firebase Authentication (Not used)
- [X] Configure Firebase Realtime Database (Setup exists, `MyFBDB` created - legacy)
- [X] Set up project dependencies in build files (`.kts`, `.toml`)
- [-] Create basic project architecture (MVVM) -> Using simple Activity/SurfaceView structure
- [-] Implement DI with Hilt -> Not implemented

## Phase 2: User Authentication/Registration Implementation (Legacy/Inactive)
### Priority: Low (Currently unused)
- [X] Create User data model (`User.java`)
- [-] Implement AuthRepository -> Replaced by `UserFileStorage` (File) and `MyFBDB` (Firebase RTDB)
- [X] Implement basic user registration/login logic:
    - [X] Using local file storage (`UserFileStorage`, `RegActivityFile`)
    - [X] Using Firebase RTDB (`MyFBDB`, `RegActivityFB`)
- [-] Create authentication use cases -> Logic was in Activities/Helpers
- [-] LogoutUser -> Not implemented
- [-] ResetPassword -> Not implemented
- [X] Design and implement authentication screens (Exist but unused by core game):
    - [X] Login screen (was `activity_main.xml`)
    - [X] Registration screen (`activity_reg_file.xml`, `activity_reg_fb.xml`)
    - [-] Password reset screen -> Not implemented
- [X] Implement form validation (basic empty checks in legacy code)
- [ ] **TODO:** Implement secure password handling (hashing) - **If reactivated**
- [-] Add unit tests for authentication logic -> Not implemented
- [-] Add UI tests for authentication flows -> Not implemented

## Phase 3: User Profile and Score Management
### Priority: Medium (for future enhancements)
- [ ] Create Score data model
- [ ] Implement ScoreRepository (or similar for file/Firebase)
- [ ] Create score management use cases:
    - [ ] SaveScore (Persistent storage)
    - [ ] GetUserHighScore
    - [ ] UpdateUserProfile
- [ ] Design and implement profile screens:
    - [ ] User profile view
    - [ ] Score history
    - [ ] Settings
- [ ] Add unit tests for score management
- [ ] Add UI tests for profile screens

## Phase 4: Game Implementation
### Priority: High
- [X] Design basic game mechanics (Move, Eat, Grow, Collide)
- [X] Basic Game structure (`MainActivity`, `GameManager`, `Direction`)
- [X] Implement core game logic in `GameManager` (movement, collision, food, score)
- [X] Implement game loop using `SurfaceView` and `Runnable`
- [X] Handle game state (running, game over)
- [X] Manage snake body (`LinkedList<Point>`)
- [X] Place food randomly
- [X] Implement score calculation (increment on food)
- [X] Design and implement game screen (`activity_main.xml`):
    - [X] Score display (`TextView` updated by `MainActivity`)
    - [X] Game area (`FrameLayout` hosting `GameManager`)
    - [X] Control buttons (`LinearLayout` with `Button`s)
- [ ] Create game levels (future enhancement)
- [ ] Add difficulty progression (future enhancement)
- [ ] Design and implement results screen (future enhancement)
- [ ] Add unit tests for game logic (`GameManager`)
- [ ] Add UI tests for game flows (`MainActivity`)

## Phase 5: Leaderboard Implementation
### Priority: Low
- [ ] Create LeaderboardRepository (or similar)
- [ ] Implement leaderboard queries (Firebase RTDB or file based)
- [ ] Design and implement leaderboard screen
- [ ] Add real-time updates for scores (if using Firebase)
- [ ] Add pagination for leaderboard
- [ ] Add unit tests for leaderboard logic
- [ ] Add UI tests for leaderboard screen

## Phase 6: Polish and Optimization
### Priority: Medium
- [ ] Add loading indicators (if needed)
- [ ] Implement error handling (more robustly, e.g., surface creation issues)
- [ ] Add analytics (Firebase Analytics?)
- [ ] Optimize database queries (if using Firebase)
- [ ] Add caching (if needed)
- [ ] Implement offline support (if needed)
- [ ] Performance testing (especially drawing)
- [-] Security review (Password storage issue applies only to legacy auth code)

## Phase 7: Testing and Documentation
### Priority: Medium
- [ ] Complete unit test coverage (especially `GameManager`)
- [ ] Complete integration tests (if applicable)
- [ ] Complete UI tests (`MainActivity` game interaction)
- [X] Write API documentation (`ARCHITECTURE.md` updated)
- [X] Create user documentation (`README.md` updated)
- [X] Create Changelog (`CHANGELOG.md` - needs update for recent changes)
- [ ] Perform security testing (if legacy auth reactivated)
- [ ] Conduct performance testing

## Phase 8: Release Preparation
### Priority: Low
- [ ] Prepare release builds
- [ ] Create store listings
- [ ] Prepare marketing materials
- [ ] Set up crash reporting (e.g., Firebase Crashlytics)
- [ ] Configure analytics
- [ ] Plan staged rollout 