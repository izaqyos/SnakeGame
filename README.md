# Find The Diff Game

An Android game where players need to find differences between images. The game features user authentication, score tracking, and leaderboards using Firebase.

## Features

- User Registration and Authentication
- Secure Password Management
- Score Tracking and Personal Best Records
- Global Leaderboard
- Multiple Game Levels
- Progressive Difficulty

## Technical Stack

- Android (Kotlin)
- Firebase Authentication
- Firebase Realtime Database
- MVVM Architecture
- Jetpack Components
- Unit Testing (JUnit, Mockito)
- UI Testing (Espresso)

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/snake/
│   │   │   ├── data/
│   │   │   │   ├── models/
│   │   │   │   ├── repositories/
│   │   │   │   └── datasources/
│   │   │   ├── di/
│   │   │   ├── domain/
│   │   │   │   ├── usecases/
│   │   │   │   └── repositories/
│   │   │   ├── presentation/
│   │   │   │   ├── auth/
│   │   │   │   ├── game/
│   │   │   │   └── leaderboard/
│   │   │   └── utils/
│   │   └── res/
│   └── test/
└── build.gradle
```

## Firebase Schema

### Users Collection
```json
{
  "users": {
    "userId": {
      "username": "string",
      "email": "string",
      "createdAt": "timestamp"
    }
  }
}
```

### Scores Collection
```json
{
  "scores": {
    "userId": {
      "highestScore": "number",
      "totalGamesPlayed": "number",
      "lastPlayed": "timestamp"
    }
  }
}
```

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Create a Firebase project and add the `google-services.json` file
4. Configure Firebase Authentication and Realtime Database
5. Build and run the project

## Testing

The project includes comprehensive test coverage:
- Unit tests for ViewModels, UseCases, and Repositories
- Integration tests for Firebase operations
- UI tests for critical user flows

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details 