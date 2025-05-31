package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

public class Instructions {
    private final Texture instructionsTexture;
    private final Texture resumeButtonTexture;
    private final Rectangle resumeButtonBounds;
    private final Vector2 resumeButtonPosition;

    private final OrthographicCamera instructionsCamera;

    public interface InstructionListener {
        void onBackToGame();
    }

    private Sound buttonClickSound;
    private float soundVolume = 0.5f;

    private final InstructionListener listener;

    public Instructions(InstructionListener listener) {
        this.listener = listener;

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Initialize separate camera for menu
        instructionsCamera = new OrthographicCamera();
        instructionsCamera.setToOrtho(false, screenWidth, screenHeight);
        instructionsCamera.update();

        // Load textures
        instructionsTexture = new Texture(Gdx.files.internal("instructions.png"));
        resumeButtonTexture = new Texture(Gdx.files.internal("buttons/resume.png"));

        loadSounds();

        // Position buttons centered horizontally at the bottom
        int buttonWidth = 400;
        int buttonHeight = 100;
        resumeButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, 20f);
        resumeButtonBounds = new Rectangle(resumeButtonPosition.x, resumeButtonPosition.y, buttonWidth, buttonHeight);
    }

    private void loadSounds() {
        try {
            buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("sfx/buttonclick4.mp3"));
        } catch (Exception e) {
            System.err.println("Error loading sound files: " + e.getMessage());
        }
    }

    public void render(SpriteBatch batch) {
        // Clear the screen
        Gdx.gl.glClearColor(0.76f, 0.7f, 0.64f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Use the menu's own camera
        batch.setProjectionMatrix(instructionsCamera.combined);

        batch.begin();
        batch.draw(instructionsTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(resumeButtonTexture, resumeButtonPosition.x, resumeButtonPosition.y, resumeButtonBounds.width, resumeButtonBounds.height);
        batch.end();
    }

    private void playButtonSound() {
        if (buttonClickSound != null) {
            buttonClickSound.play(soundVolume);
        }
    }

    public void dispose() {
        if (instructionsTexture != null) instructionsTexture.dispose();
        if (resumeButtonTexture != null) resumeButtonTexture.dispose();
    }

    // Call this method from game touchDown or mouse click handling (pass screenX, screenY)
    public boolean touchDown(int screenX, int screenY) {
        int invertedY = Gdx.graphics.getHeight() - screenY;

        if (resumeButtonBounds.contains(screenX, invertedY)) {
            playButtonSound();
            if (listener != null) listener.onBackToGame();
            return true;
        }

        return false;
    }
}