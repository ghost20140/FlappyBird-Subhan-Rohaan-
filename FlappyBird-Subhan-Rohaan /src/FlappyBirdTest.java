package src;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FlappyBirdTest {

    private FlappyBird flappyBird;

    @BeforeEach
    public void setUp() {
        flappyBird = new FlappyBird();
        flappyBird.startGame(); // Start the game to initialize timers and other components
    }

    @Test
    public void testInitialBirdPosition() {
        assertEquals(flappyBird.bird.x, flappyBird.boardWidth / 8);
        assertEquals(flappyBird.bird.y, flappyBird.boardWidth / 2);
    }

    @Test
    public void testBirdMovement() {
        int initialY = flappyBird.bird.y;
        flappyBird.velocityY = -9;
        flappyBird.move();
        assertTrue(flappyBird.bird.y < initialY); // Bird should move up
    }

    @Test
    public void testGravityEffectOnBird() {
        int initialY = flappyBird.bird.y;
        flappyBird.velocityY = 0;
        flappyBird.move();
        assertTrue(flappyBird.bird.y > initialY); // Bird should move down due to gravity
    }

    @Test
    public void testCollisionDetection() {
        Bird bird = new Bird(50, 50, 34, 24, null);
        Pipe pipe = new Pipe(50, 50, 64, 512, null);
        assertTrue(flappyBird.collision(bird, pipe));
    }

    @Test
    public void testNoCollision() {
        Bird bird = new Bird(50, 50, 34, 24, null);
        Pipe pipe = new Pipe(200, 200, 64, 512, null);
        assertFalse(flappyBird.collision(bird, pipe));
    }

    @Test
    public void testPipePlacement() {
        flappyBird.placePipes();
        assertEquals(2, flappyBird.pipes.size());
    }

    @Test
    public void testGameStart() {
        flappyBird.startGame();
        assertTrue(flappyBird.gameLoop.isRunning());
        assertTrue(flappyBird.placePipeTimer.isRunning());
    }

    @Test
    public void testGameOver() {
        flappyBird.gameOver = true;
        flappyBird.actionPerformed(null);
        assertFalse(flappyBird.gameLoop.isRunning());
        assertFalse(flappyBird.placePipeTimer.isRunning());
    }

    @Test
    public void testToggleDarkMode() {
        boolean initialMode = flappyBird.darkMode;
        flappyBird.toggleDarkMode();
        assertNotEquals(initialMode, flappyBird.darkMode);
    }

    @Test
    public void testSetDifficultyEasy() {
        flappyBird.setDifficulty(0);
        assertEquals(-4, flappyBird.velocityX);
        assertEquals("Easy", flappyBird.highScoreDifficulty);
    }

    @Test
    public void testSetDifficultyMedium() {
        flappyBird.setDifficulty(1);
        assertEquals(-6, flappyBird.velocityX);
        assertEquals("Medium", flappyBird.highScoreDifficulty);
    }

    @Test
    public void testSetDifficultyHard() {
        flappyBird.setDifficulty(2);
        assertEquals(-8, flappyBird.velocityX);
        assertEquals("Hard", flappyBird.highScoreDifficulty);
    }
}
