package src;

import java.io.*;
import java.util.Properties;

public class HighScoreManager {
    private double highScoreEasy = 0;
    private double highScoreMedium = 0;
    private double highScoreHard = 0;

    public double getHighScore(int difficulty) {
        switch (difficulty) {
            case 0: return highScoreEasy;
            case 1: return highScoreMedium;
            case 2: return highScoreHard;
            default: return highScoreEasy;
        }
    }

    public void setHighScore(double score, int difficulty) {
        switch (difficulty) {
            case 0: highScoreEasy = score; break;
            case 1: highScoreMedium = score; break;
            case 2: highScoreHard = score; break;
        }
    }

    public void loadHighScores() {
        Properties properties = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/resources/highscores.properties")) {
            properties.load(input);
            highScoreEasy = Double.parseDouble(properties.getProperty("highScoreEasy", "0"));
            highScoreMedium = Double.parseDouble(properties.getProperty("highScoreMedium", "0"));
            highScoreHard = Double.parseDouble(properties.getProperty("highScoreHard", "0"));
        } catch (IOException e) {
            highScoreEasy = 0;
            highScoreMedium = 0;
            highScoreHard = 0;
        }
    }

    public void saveHighScores() {
        Properties properties = new Properties();
        properties.setProperty("highScoreEasy", Double.toString(highScoreEasy));
        properties.setProperty("highScoreMedium", Double.toString(highScoreMedium));
        properties.setProperty("highScoreHard", Double.toString(highScoreHard));

        try (OutputStream output = new FileOutputStream("resources/highscores.properties")) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
