# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased] - YYYY-MM-DD

### Added
- Initial project structure for Snake Game.
- Basic MainActivity with login and registration buttons.
- User model (`User.java`).
- Firebase Realtime Database integration (`MyFBDB.java`) for user management.
- File-based user storage (`UserFileStorage.java`) as the primary method with Firebase backup.
- Registration activity for File and Firebase storage (`RegActivityFile.java`).
- Test user (`testuser`/`abc123`) created on first run using file storage.
- Initial documentation files (`README.md`, `ARCHITECTURE.md`, `TASKS.md`, `CHANGELOG.md`).
- Gradle setup for Android, Kotlin, and Firebase.
- Core gameplay logic in `GameActivity` and `GameManager` (SurfaceView).
- On-screen direction controls.
- Score display.
- Collision detection (wall and self).
- Back button in `GameActivity` to return to `MainActivity`.
- Restart button in `GameActivity` shown on game over.
- Settings screen (`settings.java`) for customization options.
- Snake color customization with several options.
- Background music toggle.
- High score tracking (personal and global) via Firebase.
- Username display during gameplay.
- Stats bar showing player name, current score, personal best, and global high score.
- Proper activity navigation with state preservation.
- Confirmation dialog when exiting a game in progress.

### Changed
- Improved Firebase connectivity by setting explicit database URL.
- Enhanced error handling for Firebase operations.
- Improved user authentication with both local and Firebase checks.
- Enhanced UI with progress indicators during login.
- Updated activity navigation to prevent unexpected returns to login screen.
- Implemented ActivityResultLauncher for proper navigation between GameActivity and settings.
- Fixed navigation issues between MainActivity and settings.
- Improved back button handling to prevent accidental game exits.
- Added proper activity lifecycle management for all screens.

### Fixed
- Fixed issue where players would be unexpectedly returned to login screen during gameplay.
- Fixed settings navigation to preserve the state of the calling activity.
- Properly handled activity transitions to maintain game state.
- Resolved Firebase callback issues in score tracking.
- Fixed UI state preservation when moving between screens. 