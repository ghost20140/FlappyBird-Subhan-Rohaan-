package src;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

public class HighScoreManagerTest {

    private HighScoreManager highScoreManager;

    @BeforeEach
    public void setUp() {
        highScoreManager = new HighScoreManager();
    }

    @Test
    public void testGetHighScore() {
        highScoreManager.setHighScore(100.0, 0);
        highScoreManager.setHighScore(200.0, 1);
        highScoreManager.setHighScore(300.0, 2);

        assertEquals(100.0, highScoreManager.getHighScore(0));
        assertEquals(200.0, highScoreManager.getHighScore(1));
        assertEquals(300.0, highScoreManager.getHighScore(2));
    }

    @Test
    public void testSetHighScore() {
        highScoreManager.setHighScore(150.0, 0);
        assertEquals(150.0, highScoreManager.getHighScore(0));

        highScoreManager.setHighScore(250.0, 1);
        assertEquals(250.0, highScoreManager.getHighScore(1));

        highScoreManager.setHighScore(350.0, 2);
        assertEquals(350.0, highScoreManager.getHighScore(2));
    }

    @Test
    public void testLoadHighScores() {
        // Create a temporary properties file for testing
        Properties properties = new Properties();
        properties.setProperty("highScoreEasy", "123.0");
        properties.setProperty("highScoreMedium", "456.0");
        properties.setProperty("highScoreHard", "789.0");

        try (OutputStream output = new FileOutputStream("resources/highscores.properties")) {
            properties.store(output, null);
        } catch (IOException e) {
            fail("Failed to create temporary properties file for testing");
        }

        highScoreManager.loadHighScores();

        assertEquals(123.0, highScoreManager.getHighScore(0));
        assertEquals(456.0, highScoreManager.getHighScore(1));
        assertEquals(789.0, highScoreManager.getHighScore(2));
    }

    @Test
    public void testSaveHighScores() {
        highScoreManager.setHighScore(321.0, 0);
        highScoreManager.setHighScore(654.0, 1);
        highScoreManager.setHighScore(987.0, 2);

        highScoreManager.saveHighScores();

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("resources/highscores.properties")) {
            properties.load(input);
        } catch (IOException e) {
            fail("Failed to load properties file for testing");
        }

        assertEquals("321.0", properties.getProperty("highScoreEasy"));
        assertEquals("654.0", properties.getProperty("highScoreMedium"));
        assertEquals("987.0", properties.getProperty("highScoreHard"));
    }
}
