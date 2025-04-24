# Implementation Tasks

(Based on initial template, adjusted for current state)

## Phase 1: Project Setup
- [X] Initialize Android project (Java, not Kotlin initially planned)
- [X] Set up Firebase project
- [ ] Configure Firebase Authentication (Not used yet, only RTDB)
- [X] Configure Firebase Realtime Database (Setup in console, `MyFBDB` created)
- [X] Set up project dependencies in build files (`.kts`, `.toml`)
- [ ] Create basic project architecture (MVVM) -> Currently simple Activity structure
- [ ] Implement DI with Hilt -> Not implemented

## Phase 2: User Authentication/Registration Implementation
### Priority: High
- [X] Create User data model (`User.java`)
- [ ] Implement AuthRepository -> Replaced by `UserFileStorage` (File) and `MyFBDB` (Firebase RTDB)
- [X] Implement basic user registration/login logic:
    - [X] Using local file storage (`UserFileStorage`, `RegActivityFile`) - **Active**
    - [X] Using Firebase RTDB (`MyFBDB`, `RegActivityFB`) - **Inactive**
- [ ] Create authentication use cases -> Logic currently in Activities/Helpers
- [ ] LogoutUser -> Not implemented
- [ ] ResetPassword -> Not implemented
- [X] Design and implement authentication screens:
    - [X] Login screen (`activity_main.xml`)
    - [X] Registration screen (`activity_reg.xml`, `activity_reg_fb.xml`)
    - [ ] Password reset screen -> Not implemented
- [X] Implement form validation (basic empty checks)
- [ ] **TODO:** Implement secure password handling (hashing)
- [ ] Add unit tests for authentication logic -> Not implemented
- [ ] Add UI tests for authentication flows -> Not implemented

## Phase 3: User Profile and Score Management
### Priority: Medium
- [ ] Create Score data model
- [ ] Implement ScoreRepository (or similar for file/Firebase)
- [ ] Create score management use cases:
    - [ ] SaveScore
    - [ ] GetUserHighScore
    - [ ] UpdateUserProfile
- [ ] Design and implement profile screens:
    - [ ] User profile view
    - [ ] Score history
    - [ ] Settings
- [ ] Add unit tests for score management
- [ ] Add UI tests for profile screens

## Phase 4: Game Implementation
### Priority: Medium
- [ ] Design game mechanics
- [X] Basic Game structure (`level1.java`, `GameManager.java`)
- [ ] Implement core game logic in `GameManager`
- [ ] Create game levels
- [ ] Add difficulty progression
- [ ] Implement score calculation
- [ ] Design and implement game screens:
    - [ ] Level selection
    - [X] Game play screen (`activity_level1.xml` - basic FrameLayout)
    - [ ] Results screen
- [ ] Add unit tests for game logic
- [ ] Add UI tests for game flows

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
### Priority: Low
- [ ] Add loading indicators
- [ ] Implement error handling (more robustly)
- [ ] Add analytics (Firebase Analytics?)
- [ ] Optimize database queries (if using Firebase)
- [ ] Add caching
- [ ] Implement offline support
- [ ] Performance testing
- [X] Security review (Password storage needs fixing!)

## Phase 7: Testing and Documentation
### Priority: Medium
- [ ] Complete unit test coverage
- [ ] Complete integration tests
- [ ] Complete UI tests
- [X] Write API documentation (`ARCHITECTURE.md` updated)
- [X] Create user documentation (`README.md` updated)
- [X] Create Changelog (`CHANGELOG.md` added)
- [ ] Perform security testing
- [ ] Conduct performance testing

## Phase 8: Release Preparation
### Priority: Low
- [ ] Prepare release builds
- [ ] Create store listings
- [ ] Prepare marketing materials
- [ ] Set up crash reporting (e.g., Firebase Crashlytics)
- [ ] Configure analytics
- [ ] Plan staged rollout 