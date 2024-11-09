package src;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // images
    Image backgroundImg;
    Image nightBackgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    boolean darkMode = false;

    // bird and pipe objects
    Bird bird;
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; // scaled by 1/6
    int pipeHeight = 512;
    int lastPipeX = boardWidth;

    // game logic
    int velocityX = -4; // Default velocity, will be set by difficulty selection
    int velocityY = 0; // move bird up/down speed.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    boolean gamePaused = false;
    double score = 0;
    String highScoreDifficulty = "Easy";

    JButton startButton;
    JButton retryButton;
    JButton difficultyButton;
    JButton darkModeButton;
    JMenuBar menuBar;

    long pauseStartTime;
    long pauseDuration = 0;

    int currentDifficulty = 0; // 0 for Easy, 1 for Medium, 2 for Hard

    HighScoreManager highScoreManager;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("/resources/images/flappybirdbg.png")).getImage();
        nightBackgroundImg = new ImageIcon(getClass().getResource("/resources/images/bg_night.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/resources/images/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/resources/images/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/resources/images/bottompipe.png")).getImage();

        // bird and pipes
        bird = new Bird(birdX, birdY, birdWidth, birdHeight, birdImg);
        pipes = new ArrayList<Pipe>();

        // Start button
        startButton = new JButton("Start");
        startButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 - 25, 100, 50);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        this.setLayout(null);
        this.add(startButton);

        // Retry button
        retryButton = new JButton("Retry");
        retryButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 - 25, 100, 50);
        retryButton.setVisible(false);
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
                startGame();
            }
        });
        this.add(retryButton);

        // Difficulty button
        difficultyButton = new JButton("Difficulty");
        difficultyButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 + 50, 100, 50);
        difficultyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDifficulty();
            }
        });
        this.add(difficultyButton);

        // Dark Mode button
        darkModeButton = new JButton("Dark Mode");
        darkModeButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 + 125, 100, 50);
        darkModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDarkMode();
            }
        });
        this.add(darkModeButton);

        // Menu bar for pause
        menuBar = new JMenuBar();
        JMenu menu = new JMenu("Game Paused");
        menuBar.add(menu);
        menuBar.setVisible(false);
        menuBar.setBounds(0, 0, boardWidth, 25);
        this.add(menuBar);

        // High score manager
        highScoreManager = new HighScoreManager();
        highScoreManager.loadHighScores();

        // Set initial difficulty to easy
        setDifficulty(0);
    }

    void startGame() {
        this.remove(startButton);
        retryButton.setVisible(false);
        difficultyButton.setVisible(false);
        darkModeButton.setVisible(false);
        this.revalidate();
        this.repaint();
        requestFocusInWindow(); // Ensure the panel gets focus when the game starts

        // Start game timers
        gameLoop = new Timer(1000 / 60, this); // how long it takes to start timer, milliseconds gone between frames
        gameLoop.start();
        if (placePipeTimer != null) {
            placePipeTimer.start();
        }
    }

    void selectDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(this, "Select Difficulty Level", "Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        setDifficulty(choice);
    }

    void setDifficulty(int choice) {
        currentDifficulty = choice;
        int pipeFrequency = 1500; // Default to Easy frequency
        switch (choice) {
            case 0:
                velocityX = -4; // Easy
                pipeFrequency = 1500;
                highScoreDifficulty = "Easy";
                break;
            case 1:
                velocityX = -6; // Medium
                pipeFrequency = 1000;
                highScoreDifficulty = "Medium";
                break;
            case 2:
                velocityX = -8; // Hard
                pipeFrequency = 750;
                highScoreDifficulty = "Hard";
                break;
            default:
                velocityX = -4; // Default to Easy
                pipeFrequency = 1500;
                highScoreDifficulty = "Easy";
                break;
        }

        // Update pipe placement timer
        if (placePipeTimer != null) {
            placePipeTimer.stop();
        }
        placePipeTimer = new Timer(pipeFrequency, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
    }

    void placePipes() {
        int openingSpace = boardHeight / 4 - 10; // Decreased the distance a little bit
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));

        Pipe topPipe = new Pipe(pipeX, randomPipeY, pipeWidth, pipeHeight, topPipeImg);
        Pipe bottomPipe = new Pipe(pipeX, topPipe.y + pipeHeight + openingSpace, pipeWidth, pipeHeight, bottomPipeImg);

        // Ensure pipes are placed with sufficient distance
        if (lastPipeX >= boardWidth) {
            pipes.add(topPipe);
            pipes.add(bottomPipe);
            lastPipeX = pipeX;
        }
    }

    void toggleDarkMode() {
        darkMode = !darkMode;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // background
        if (darkMode) {
            g.drawImage(nightBackgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
        } else {
            g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
        }

        // bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString("Score: " + (int) score, 10, 35);

        // high score
        g.drawString("High Score (" + highScoreDifficulty + "): " + highScoreManager.getHighScore(currentDifficulty), 10, 70);

        if (gameOver) {
            g.drawString("Game Over", 10, 105);
        }
    }

    public void move() {
        // bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // apply gravity to current bird.y, limit the bird.y to top of the canvas

        // pipes
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // 0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && // a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x && // a's top right corner passes b's top left corner
                a.y < b.y + b.height && // a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y; // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) { // called every x milliseconds by gameLoop timer
        if (!gamePaused) {
            move();
            repaint();
            if (gameOver) {
                if (score > highScoreManager.getHighScore(currentDifficulty)) {
                    highScoreManager.setHighScore(score, currentDifficulty);
                    highScoreManager.saveHighScores();
                }
                placePipeTimer.stop();
                gameLoop.stop();
                retryButton.setVisible(true); // Show retry button when game is over
                difficultyButton.setVisible(true); // Show difficulty button when game is over
                darkModeButton.setVisible(true); // Show dark mode button when game is over
            }
        }
    }

    void resetGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        lastPipeX = boardWidth; // Reset lastPipeX to ensure proper spacing of new pipes
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                // restart game by resetting conditions
                resetGame();
                startGame();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_P) {
            gamePaused = !gamePaused;
            if (gamePaused) {
                pauseStartTime = System.currentTimeMillis();
                placePipeTimer.stop();
                gameLoop.stop();
            } else {
                long pauseEndTime = System.currentTimeMillis();
                pauseDuration += pauseEndTime - pauseStartTime;
                placePipeTimer.start();
                gameLoop.start();
            }
            menuBar.setVisible(gamePaused);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
