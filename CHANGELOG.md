# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased] - YYYY-MM-DD

### Added
- Initial project structure for Snake Game.
- Basic MainActivity with login and registration buttons.
- User model (`User.java`).
- Firebase Realtime Database integration (`MyFBDB.java`) for user management (currently commented out).
- File-based user storage (`UserFileStorage.java`) as the active method.
- Separate registration activities for File (`RegActivityFile.java` - active) and Firebase (`RegActivityFB.java` - inactive).
- Test user (`testuser`/`abc123`) created on first run using file storage.
- Initial documentation files (`README.md`, `ARCHITECTURE.md`, `TASKS.md`, `CHANGELOG.md`).
- Gradle setup for Android, Kotlin, and Firebase.

### Changed
- Split original `RegActivity` into `RegActivityFile` and `RegActivityFB`.
- `MainActivity` now launches `RegActivityFile`.
- Updated `AndroidManifest.xml` for new activities.
- Resolved initial Gradle plugin issues.
- Configured `google-services.json` for the correct Firebase project and package name. 