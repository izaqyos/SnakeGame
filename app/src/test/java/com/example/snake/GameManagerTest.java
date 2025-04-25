package com.example.snake;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GameManagerTest {

    @Mock
    Context mockContext;
    @Mock
    Resources mockResources;
    @Mock
    Bitmap mockBitmap; // Mock bitmap to avoid file system access

    private GameManager gameManager;

    // Helper to access private field for verification
    private GameManager.Direction getCurrentDirection(GameManager gm) {
        try {
            java.lang.reflect.Field field = GameManager.class.getDeclaredField("currentDirection");
            field.setAccessible(true);
            return (GameManager.Direction) field.get(gm);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() {
        // Mock context and resources needed by GameManager constructor
        when(mockContext.getResources()).thenReturn(mockResources);
        // Prevent actual resource loading within the mocked Resources object
        when(mockResources.getIdentifier(anyString(), anyString(), anyString())).thenReturn(0); 
        when(mockResources.getDrawable(anyInt(), eq(null))).thenReturn(null); 
        
        // Define behavior for the regular mockBitmap *outside* static mocking scope
        when(mockBitmap.getWidth()).thenReturn(40); // Provide dummy dimensions
        when(mockBitmap.getHeight()).thenReturn(40);

        // Mock the static BitmapFactory methods
        try (MockedStatic<BitmapFactory> mockedBitmapFactory = mockStatic(BitmapFactory.class);
             MockedStatic<Bitmap> mockedBitmapStatic = mockStatic(Bitmap.class)) { // Also mock static Bitmap methods
             
            mockedBitmapFactory.when(() -> BitmapFactory.decodeResource(eq(mockResources), anyInt()))
                             .thenReturn(mockBitmap);
            
            // Mock the static Bitmap.createScaledBitmap method
            mockedBitmapStatic.when(() -> Bitmap.createScaledBitmap(eq(mockBitmap), anyInt(), anyInt(), anyBoolean()))
                             .thenReturn(mockBitmap); // Return the same mock bitmap

            // Create GameManager instance *inside* the try-with-resources block for static mocks
            gameManager = new GameManager(mockContext, 800, 600);
        }
        assertNotNull("GameManager should be created", gameManager); // Verify creation
    }

    @Test
    public void initialState_DirectionIsRight() {
        assertEquals("Initial direction should be RIGHT", GameManager.Direction.RIGHT, getCurrentDirection(gameManager));
    }

    @Test
    public void setDirection_ChangesDirectionCorrectly() {
        // From RIGHT (initial)
        gameManager.setDirection(GameManager.Direction.UP);
        assertEquals("Direction should be UP", GameManager.Direction.UP, getCurrentDirection(gameManager));

        gameManager.setDirection(GameManager.Direction.LEFT);
        assertEquals("Direction should be LEFT", GameManager.Direction.LEFT, getCurrentDirection(gameManager));

        gameManager.setDirection(GameManager.Direction.DOWN);
        assertEquals("Direction should be DOWN", GameManager.Direction.DOWN, getCurrentDirection(gameManager));

        // Setting RIGHT from DOWN should work
        gameManager.setDirection(GameManager.Direction.RIGHT);
        assertEquals("Direction should be RIGHT", GameManager.Direction.RIGHT, getCurrentDirection(gameManager));
    }

    @Test
    public void setDirection_PreventsReversal() {
        // Initial: RIGHT
        gameManager.setDirection(GameManager.Direction.LEFT); // Try to reverse
        assertEquals("Reversing RIGHT to LEFT should be prevented", GameManager.Direction.RIGHT, getCurrentDirection(gameManager));

        // Go UP
        gameManager.setDirection(GameManager.Direction.UP);
        assertEquals("Direction should be UP", GameManager.Direction.UP, getCurrentDirection(gameManager));
        gameManager.setDirection(GameManager.Direction.DOWN); // Try to reverse
        assertEquals("Reversing UP to DOWN should be prevented", GameManager.Direction.UP, getCurrentDirection(gameManager));

        // Go LEFT
        gameManager.setDirection(GameManager.Direction.LEFT);
        assertEquals("Direction should be LEFT", GameManager.Direction.LEFT, getCurrentDirection(gameManager));
        gameManager.setDirection(GameManager.Direction.RIGHT); // Try to reverse
        assertEquals("Reversing LEFT to RIGHT should be prevented", GameManager.Direction.LEFT, getCurrentDirection(gameManager));

        // Go DOWN
        gameManager.setDirection(GameManager.Direction.DOWN);
        assertEquals("Direction should be DOWN", GameManager.Direction.DOWN, getCurrentDirection(gameManager));
        gameManager.setDirection(GameManager.Direction.UP); // Try to reverse
        assertEquals("Reversing DOWN to UP should be prevented", GameManager.Direction.DOWN, getCurrentDirection(gameManager));
    }
} 