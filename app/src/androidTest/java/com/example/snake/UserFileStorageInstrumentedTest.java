package com.example.snake;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UserFileStorageInstrumentedTest {

    private Context context;
    private UserFileStorage userFileStorage;
    private static final String TEST_FILENAME = "users.json"; // Match filename in UserFileStorage

    @Before
    public void setup() {
        // Get context of the app under test.
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Ensure a clean slate before each test
        deleteTestFile();
        userFileStorage = new UserFileStorage(context);
    }

    private void deleteTestFile() {
        File file = new File(context.getFilesDir(), TEST_FILENAME);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testUserIsCreatedAndChecked() {
        // The constructor should have created the test user
        assertTrue("Test user should exist after initialization", userFileStorage.userExists("testuser"));
        assertTrue("checkUser should return true for testuser/abc123", userFileStorage.checkUser("testuser", "abc123"));
    }

    @Test
    public void testCheckNonExistentUser() {
        assertFalse("checkUser should return false for non-existent user", userFileStorage.checkUser("nosuchuser", "password"));
    }

    @Test
    public void testCheckExistingUserWrongPassword() {
        assertTrue("Test user should exist", userFileStorage.userExists("testuser"));
        assertFalse("checkUser should return false for testuser with wrong password", userFileStorage.checkUser("testuser", "wrongpassword"));
    }

    @Test
    public void testSaveAndCheckNewUser() {
        String newUser = "newuser";
        String newPass = "newpass123";
        assertFalse("New user should not exist initially", userFileStorage.userExists(newUser));

        User user = new User(newUser, newPass, 0, 0, 0);
        userFileStorage.saveUser(user);

        assertTrue("New user should exist after saving", userFileStorage.userExists(newUser));
        assertTrue("checkUser should return true for newly saved user", userFileStorage.checkUser(newUser, newPass));
    }

     @Test
    public void testSaveExistingUser() {
        assertTrue("Test user should exist initially", userFileStorage.userExists("testuser"));

        // Try to save testuser again (should be ignored by saveUser logic)
        User user = new User("testuser", "newpassword", 1, 1, 1); 
        userFileStorage.saveUser(user); 

        // Verify the original password is still valid (meaning the save was ignored)
        assertTrue("checkUser should still return true for testuser with original password", userFileStorage.checkUser("testuser", "abc123"));
        assertFalse("checkUser should return false for testuser with attempted new password", userFileStorage.checkUser("testuser", "newpassword"));
    }
} 