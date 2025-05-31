package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

public class GameMenu {
    private Texture backgroundTexture;
    private Texture startButtonTexture;
    private Texture resumeButtonTexture;
    private Texture exitButtonTexture;
    private Rectangle startButtonBounds;
    private Rectangle resumeButtonBounds;
    private Rectangle exitButtonBounds;
    private Vector2 startButtonPosition;
    private Vector2 resumeButtonPosition;
    private Vector2 exitButtonPosition;
    private boolean isFirstStart = true;

    private OrthographicCamera menuCamera;

    public interface MenuListener {
        void onStartGame();
        void onResumeGame();
        void onExitGame();
    }

    private MenuListener listener;

    private Sound buttonClickSound;
    private float soundVolume = 0.5f;

    public GameMenu(MenuListener listener) {
        this.listener = listener;

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Initialize separate camera for menu
        menuCamera = new OrthographicCamera();
        menuCamera.setToOrtho(false, screenWidth, screenHeight);
        menuCamera.update();

        // Load textures
        backgroundTexture = new Texture(Gdx.files.internal("Menu Background.png"));
        startButtonTexture = new Texture(Gdx.files.internal("start.png"));
        resumeButtonTexture = new Texture(Gdx.files.internal("resume.png"));
        exitButtonTexture = new Texture(Gdx.files.internal("exit.png"));

        // Load Sound Effects
        loadSounds();

        // Position buttons centered horizontally, with some vertical spacing
        int buttonWidth = 400;
        int buttonHeight = 100;
        startButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, screenHeight / 2f - 170);
        resumeButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, screenHeight / 2f - 280);
        exitButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, screenHeight / 2f - 390);

        startButtonBounds = new Rectangle(startButtonPosition.x, startButtonPosition.y, buttonWidth, buttonHeight);
        resumeButtonBounds = new Rectangle(resumeButtonPosition.x, resumeButtonPosition.y, buttonWidth, buttonHeight);
        exitButtonBounds = new Rectangle(exitButtonPosition.x, exitButtonPosition.y, buttonWidth, buttonHeight);
    }

       private void loadSounds() {
        try {
            buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("sounds/buttonclick3.mp3"));
        } catch (Exception e) {
            System.err.println("Error loading sound files: " + e.getMessage());
        }
    }

    public void render(SpriteBatch batch) {
        // Use the menu's own camera
        batch.setProjectionMatrix(menuCamera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(startButtonTexture, startButtonPosition.x, startButtonPosition.y, startButtonBounds.width, startButtonBounds.height);
        // Only show resume button if not first start
        if (!isFirstStart) {
            batch.draw(resumeButtonTexture, resumeButtonPosition.x, resumeButtonPosition.y, resumeButtonBounds.width, resumeButtonBounds.height);
        }
        batch.draw(exitButtonTexture, exitButtonPosition.x, exitButtonPosition.y, exitButtonBounds.width, exitButtonBounds.height);
        batch.end();
    }
    
    private void playButtonSound() {
        if (buttonClickSound != null) {
            buttonClickSound.play(soundVolume);
        }
    }

    public void dispose() {
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (startButtonTexture != null) startButtonTexture.dispose();
        if (resumeButtonTexture != null) resumeButtonTexture.dispose();
        if (exitButtonTexture != null) exitButtonTexture.dispose();
        if (buttonClickSound != null) buttonClickSound.dispose();
    }

    // Call this method from game touchDown or mouse click handling (pass screenX, screenY)
    public boolean touchDown(int screenX, int screenY) {
        int invertedY = Gdx.graphics.getHeight() - screenY;

        if (startButtonBounds.contains(screenX, invertedY)) {
             playButtonSound();
            if (listener != null) {
                listener.onStartGame();
                isFirstStart = false;
            }
            return true;
        }

        if (!isFirstStart && resumeButtonBounds.contains(screenX, invertedY)) {
            playButtonSound();
            if (listener != null) {
                listener.onResumeGame();
            }
            return true;
        }

        if (exitButtonBounds.contains(screenX, invertedY)) {
            playButtonSound();
            if (listener != null) listener.onExitGame();
            return true;
        }
        return false;
    }
}
