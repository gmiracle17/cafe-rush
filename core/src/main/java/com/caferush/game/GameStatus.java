package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

public class GameStatus {
    private Texture gameWonTexture;
    private Texture gameLostTexture;
    private Texture restartButtonTexture;
    private Texture resumeButtonTexture;
    private Texture menuButtonTexture;

    private Rectangle restartButtonBounds;
    private Rectangle resumeButtonBounds;
    private Rectangle menuButtonBounds;

    private Vector2 restartButtonPosition;
    private Vector2 resumeButtonPosition;
    private Vector2 menuButtonPosition;

    private final OrthographicCamera camera;

    private final StatusListener listener;
    private Sound buttonClickSound;
    private boolean isGameOver = true;

    public interface StatusListener {
        void onStartGame();
        void onResumeGame();
        void onBackToMenu();
    }

    public GameStatus(StatusListener listener) {
        this.listener = listener;

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.update();

        loadAssets();
        initializeBounds();
        loadSounds();
    }

    private void loadAssets() {
        gameWonTexture = new Texture(Gdx.files.internal("congratulations message.png"));
        gameLostTexture = new Texture(Gdx.files.internal("gameover message.png"));
        restartButtonTexture = new Texture(Gdx.files.internal("buttons/restart.png"));
        resumeButtonTexture = new Texture(Gdx.files.internal("buttons/resume.png"));
        menuButtonTexture = new Texture(Gdx.files.internal("buttons/menu.png"));
    }

    private void initializeBounds() {
        int buttonWidth = 500;
        int buttonHeight = 300;
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        restartButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, screenHeight / 2f - 340);
        resumeButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, screenHeight / 2f - 340);
        menuButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, screenHeight / 2f - 470);

        restartButtonBounds = new Rectangle(restartButtonPosition.x, restartButtonPosition.y, buttonWidth, buttonHeight);
        resumeButtonBounds = new Rectangle(resumeButtonPosition.x, resumeButtonPosition.y, buttonWidth, buttonHeight);
        menuButtonBounds = new Rectangle(menuButtonPosition.x, menuButtonPosition.y, buttonWidth, buttonHeight);
    }

    private void loadSounds() {
        try {
            buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("sfx/buttonclick3.mp3"));
        } catch (Exception e) {
            System.err.println("Error loading sound: " + e.getMessage());
        }
    }

    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Background based on game state
        Texture background = isGameOver ? gameLostTexture : gameWonTexture;
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Buttons based on game state
        if (isGameOver) {
            batch.draw(restartButtonTexture, restartButtonPosition.x, restartButtonPosition.y, restartButtonBounds.width, restartButtonBounds.height);
        } else {
            batch.draw(resumeButtonTexture, resumeButtonPosition.x, resumeButtonPosition.y, resumeButtonBounds.width, resumeButtonBounds.height);
        }

        // Menu button shown in both cases
        batch.draw(menuButtonTexture, menuButtonPosition.x, menuButtonPosition.y, menuButtonBounds.width, menuButtonBounds.height);

        batch.end();
    }

    private void playButtonSound() {
        if (buttonClickSound != null) {
            float soundVolume = 0.5f;
            buttonClickSound.play(soundVolume);
        }
    }

    public boolean touchDown(int screenX, int screenY) {
        int correctedY = Gdx.graphics.getHeight() - screenY;

        if (isGameOver && restartButtonBounds.contains(screenX, correctedY)) {
            playButtonSound();
            if (listener != null) listener.onStartGame();
            return true;
        }

        if (!isGameOver && resumeButtonBounds.contains(screenX, correctedY)) {
            playButtonSound();
            if (listener != null) listener.onResumeGame();
            return true;
        }

        if (menuButtonBounds.contains(screenX, correctedY)) {
            playButtonSound();
            if (listener != null) listener.onBackToMenu();
            return true;
        }

        return false;
    }

    public void dispose() {
        if (gameWonTexture != null) gameWonTexture.dispose();
        if (gameLostTexture != null) gameLostTexture.dispose();
        if (restartButtonTexture != null) restartButtonTexture.dispose();
        if (resumeButtonTexture != null) resumeButtonTexture.dispose();
        if (menuButtonTexture != null) menuButtonTexture.dispose();
        if (buttonClickSound != null) buttonClickSound.dispose();
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }
}