package com.example.snake;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Ignore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@Ignore("Skipping Firebase tests for now - focusing on file storage.")
public class MyFBDBInstrumentedTest implements MyFBDB.DataLoadListener {

    private MyFBDB myFBDB;
    private CountDownLatch dataLoadLatch;
    private DatabaseError dbError = null;
    private static final long LATCH_TIMEOUT_SECONDS = 20;

    @Before
    public void setup() throws InterruptedException {
        Log.d("MyFBDBInstrumentedTest", "Setting up test...");
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp.initializeApp(appContext);
        Log.d("MyFBDBInstrumentedTest", "FirebaseApp initialized explicitly.");
        myFBDB = new MyFBDB();
        Log.d("MyFBDBInstrumentedTest", "MyFBDB instance created.");
        dataLoadLatch = new CountDownLatch(1);
        dbError = null;
        Log.d("MyFBDBInstrumentedTest", "Adding DataLoadListener...");
        myFBDB.addDataLoadListener(this);
        Log.d("MyFBDBInstrumentedTest", "DataLoadListener added.");

        Log.d("MyFBDBInstrumentedTest", "Waiting for initial data load...");
        if (!dataLoadLatch.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            Log.e("MyFBDBInstrumentedTest", "Timeout waiting for initial data load from Firebase.");
            fail("Timeout waiting for Firebase data load.");
        }
        Log.d("MyFBDBInstrumentedTest", "Initial data load signaled.");
        if (dbError != null) {
            fail("Firebase error during initial data load: " + dbError.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (myFBDB != null) {
            myFBDB.removeDataLoadListener(this);
        }
    }

    @Override
    public void onDataLoaded() {
        Log.d("MyFBDBInstrumentedTest", "onDataLoaded callback received.");
        dbError = null;
        dataLoadLatch.countDown();
    }

    @Override
    public void onDataLoadError(DatabaseError error) {
        Log.e("MyFBDBInstrumentedTest", "onDataLoadError callback received: " + error.getMessage());
        dbError = error;
        dataLoadLatch.countDown();
    }

    @Test
    public void testTestUserCanBeChecked() {
        assertTrue("checkUser should return true for testuser/abc123",
                   myFBDB.checkUser("testuser", "abc123"));
    }

    @Test
    public void testCheckNonExistentUserFB() {
        assertFalse("checkUser should return false for non-existent user in Firebase",
                   myFBDB.checkUser("nosuchuserfirebase", "password"));
    }

     @Test
    public void testCheckExistingUserWrongPasswordFB() {
        assertTrue("checkUser should find testuser", myFBDB.checkUser("testuser", "abc123"));
        assertFalse("checkUser should return false for testuser with wrong password in Firebase",
                   myFBDB.checkUser("testuser", "wrongpasswordfb"));
    }

    @Test
    public void testSaveAndCheckNewUserFB() throws InterruptedException {
        String newUser = "newuserfb_" + System.currentTimeMillis();
        String newPass = "newpassfb123";

        dataLoadLatch = new CountDownLatch(1);
        dbError = null;

        Log.d("MyFBDBInstrumentedTest", "Checking if user exists before save: " + newUser);
        assertFalse("New user should not exist initially in Firebase", myFBDB.userExists(new User(newUser, newPass, 0,0,0)));

        Log.d("MyFBDBInstrumentedTest", "Saving new user: " + newUser);
        User user = new User(newUser, newPass, 0, 0, 0);
        myFBDB.saveUser(user);

        Log.d("MyFBDBInstrumentedTest", "Waiting for data update after save for user: " + newUser);
        if (!dataLoadLatch.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
             Log.e("MyFBDBInstrumentedTest", "Timeout waiting for data update after saving user " + newUser);
            fail("Timeout waiting for Firebase data update after save.");
        }
        Log.d("MyFBDBInstrumentedTest", "Data update signaled after save.");
         if (dbError != null) {
            fail("Firebase error during data update after save: " + dbError.getMessage());
        }

        Log.d("MyFBDBInstrumentedTest", "Checking user after save and delay: " + newUser);
        assertTrue("checkUser should return true for newly saved user in Firebase after update",
                   myFBDB.checkUser(newUser, newPass));
    }
} 