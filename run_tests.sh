#!/bin/bash

# Script to set up environment and run Android unit and instrumented tests

# --- Configuration ---
# Adjust this path if your Android Studio installation differs
STUDIO_JDK_PATH="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# --- Script Start ---
echo "Starting test run script..."

# 1. Check if JAVA_HOME path exists
if [ ! -d "$STUDIO_JDK_PATH" ]; then
  echo "Error: Android Studio JDK path not found at: $STUDIO_JDK_PATH"
  echo "Please verify the path in the STUDIO_JDK_PATH variable within this script."
  exit 1
fi

# 2. Set JAVA_HOME
echo "Setting JAVA_HOME to: $STUDIO_JDK_PATH"
export JAVA_HOME="$STUDIO_JDK_PATH"

# 3. Verify gradlew exists and has execute permission
if [ ! -f "./gradlew" ]; then
  echo "Error: gradlew script not found in the current directory."
  echo "Please run this script from the project root directory (SnakeGame)."
  exit 1
fi

if [ ! -x "./gradlew" ]; then
  echo "Gradle wrapper (gradlew) is not executable. Attempting to set permission..."
  chmod +x ./gradlew
  if [ ! -x "./gradlew" ]; then
    echo "Error: Failed to set execute permission on gradlew."
    exit 1
  fi
  echo "Execute permission set for gradlew."
fi

# 4. Clean previous build (optional but recommended)
echo "Running ./gradlew clean..."
./gradlew clean
if [ $? -ne 0 ]; then
    echo "Error: './gradlew clean' failed."
    exit 1
fi

# 5. Run unit tests
echo "Running ./gradlew test..."
./gradlew test
UNIT_TEST_EXIT_CODE=$?

# 6. Run connected tests
echo "Running ./gradlew connectedDebugAndroidTest..."
./gradlew connectedDebugAndroidTest
CONNECTED_TEST_EXIT_CODE=$?

# 7. Report results
echo "--- Test Summary ---"
if [ $UNIT_TEST_EXIT_CODE -eq 0 ]; then
  echo "Unit Tests: PASSED"
else
  echo "Unit Tests: FAILED (Exit Code: $UNIT_TEST_EXIT_CODE)"
fi

if [ $CONNECTED_TEST_EXIT_CODE -eq 0 ]; then
  echo "Instrumented Tests: PASSED"
else
  # Note: Instrumented tests might report skipped tests which is not a failure
  # Check specific error codes if needed, but 0 usually means success.
  echo "Instrumented Tests: FAILED or SKIPPED (Exit Code: $CONNECTED_TEST_EXIT_CODE)"
fi
echo "--------------------"

# Exit with error if any test suite failed
if [ $UNIT_TEST_EXIT_CODE -ne 0 ] || [ $CONNECTED_TEST_EXIT_CODE -ne 0 ]; then
  echo "Overall test run failed."
  exit 1
else
  echo "Overall test run successful."
  exit 0
fi 