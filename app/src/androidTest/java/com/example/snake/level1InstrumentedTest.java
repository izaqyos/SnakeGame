package com.example.snake;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class level1InstrumentedTest {

    private ActivityScenario<level1> scenario;

    // Helper to get GameManager instance from the activity
    private GameManager getGameManager(level1 activity) {
        return activity.myGameManager;
    }

    // Helper to get currentDirection via reflection (as in unit test)
    private GameManager.Direction getCurrentDirection(GameManager gm) {
         if (gm == null) return null; // Handle null GameManager
        try {
            java.lang.reflect.Field field = GameManager.class.getDeclaredField("currentDirection");
            field.setAccessible(true);
            return (GameManager.Direction) field.get(gm);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Fail test if reflection fails
            fail("Reflection failed: " + e.getMessage());
            return null; // Should not reach here
        }
    }

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(level1.class);
        // Poll for GameManager initialization instead of fixed sleep
        waitForGameManagerInitialization(5000); // Wait up to 5 seconds
    }

    private void waitForGameManagerInitialization(long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        final GameManager[] gmHolder = {null}; // Holder for GameManager

        while (System.currentTimeMillis() < endTime) {
            scenario.onActivity(activity -> {
                gmHolder[0] = getGameManager(activity);
            });
            if (gmHolder[0] != null) {
                // GameManager is initialized
                return;
            }
            try {
                // Short pause before next check
                Thread.sleep(100); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for GameManager");
            }
        }
        // Timeout reached
        fail("Timeout waiting for GameManager initialization after " + timeoutMs + "ms");
    }

    @Test
    public void clickUpButton_SetsDirectionUp() {
        scenario.onActivity(activity -> {
             GameManager gm = getGameManager(activity);
             assertNotNull("GameManager should not be null", gm);
             // Ensure initial state isn't UP if starting RIGHT
             assertNotEquals(GameManager.Direction.UP, getCurrentDirection(gm)); 
        });

        onView(withId(R.id.buttonUp)).perform(click());

        scenario.onActivity(activity -> {
             GameManager gm = getGameManager(activity);
            assertEquals("Direction should be UP after clicking button",
                         GameManager.Direction.UP, getCurrentDirection(gm));
        });
    }

    @Test
    public void clickDownButton_SetsDirectionDown() {
        scenario.onActivity(activity -> {
            GameManager gm = getGameManager(activity);
            assertNotNull("GameManager should not be null", gm);
            // Go UP first so DOWN is a valid move
            gm.setDirection(GameManager.Direction.UP);
            assertEquals(GameManager.Direction.UP, getCurrentDirection(gm)); 
        });

        onView(withId(R.id.buttonDown)).perform(click());

        scenario.onActivity(activity -> {
            GameManager gm = getGameManager(activity);
            assertEquals("Direction should be DOWN after clicking button",
                         GameManager.Direction.DOWN, getCurrentDirection(gm));
        });
    }

    @Test
    public void clickLeftButton_SetsDirectionLeft() {
         scenario.onActivity(activity -> {
             GameManager gm = getGameManager(activity);
             assertNotNull("GameManager should not be null", gm);
             // Ensure initial state is RIGHT
              assertEquals(GameManager.Direction.RIGHT, getCurrentDirection(gm)); 
        });

        onView(withId(R.id.buttonLeft)).perform(click());

        scenario.onActivity(activity -> {
             GameManager gm = getGameManager(activity);
            assertEquals("Direction should be LEFT after clicking button",
                         GameManager.Direction.LEFT, getCurrentDirection(gm));
        });
    }

    @Test
    public void clickRightButton_SetsDirectionRight() {
        scenario.onActivity(activity -> {
            GameManager gm = getGameManager(activity);
            assertNotNull("GameManager should not be null", gm);
            // Go UP first so RIGHT is a valid move
            gm.setDirection(GameManager.Direction.UP);
            assertEquals(GameManager.Direction.UP, getCurrentDirection(gm)); 
        });

        onView(withId(R.id.buttonRight)).perform(click());

        scenario.onActivity(activity -> {
             GameManager gm = getGameManager(activity);
            assertEquals("Direction should be RIGHT after clicking button",
                         GameManager.Direction.RIGHT, getCurrentDirection(gm));
        });
    }
} 