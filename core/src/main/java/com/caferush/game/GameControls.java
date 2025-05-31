package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Sound;

public class GameControls {

    private final Texture helpButtonTexture;
    private final Texture soundOnButtonTexture;
    private final Texture soundOffButtonTexture;

    private final Rectangle helpButtonBounds;
    private final Rectangle soundButtonBounds;

    private final Vector2 helpButtonPosition;
    private final Vector2 soundButtonPosition;

    private boolean mute;

    public interface ControlsListener {
        void onShowInstructions();
        void onControlBGM();
    }

    private ControlsListener listener;

    private Sound buttonClickSound;
    private float soundVolume = 0.5f;

    public GameControls(ControlsListener listener) {
        this.listener = listener;

        loadSounds();

        helpButtonTexture = new Texture(Gdx.files.internal("buttons/help.png"));
        soundOnButtonTexture = new Texture(Gdx.files.internal("buttons/sound-on.png"));
        soundOffButtonTexture = new Texture(Gdx.files.internal("buttons/sound-off.png"));

        int buttonWidth = 100;
        int buttonHeight = 70;

        helpButtonPosition = new Vector2(1140, 50);
        soundButtonPosition = new Vector2(1022, 50);

        helpButtonBounds = new Rectangle(helpButtonPosition.x, helpButtonPosition.y, buttonWidth, buttonHeight);
        soundButtonBounds = new Rectangle(soundButtonPosition.x, soundButtonPosition.y, buttonWidth, buttonHeight);
    }

    private void loadSounds() {
        try {
            buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("sfx/buttonclick4.mp3"));
        } catch (Exception e) {
            System.err.println("Error loading sound files: " + e.getMessage());
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(helpButtonTexture, helpButtonPosition.x, helpButtonPosition.y, helpButtonBounds.width, helpButtonBounds.height);
        Texture currentSoundTexture = mute ? soundOffButtonTexture : soundOnButtonTexture;
        batch.draw(currentSoundTexture, soundButtonPosition.x, soundButtonPosition.y, soundButtonBounds.width, soundButtonBounds.height);
    }

    private void playButtonSound() {
        if (buttonClickSound != null) {
            buttonClickSound.play(soundVolume);
        }
    }

    public boolean touchDown(int screenX, int screenY) {
        int invertedY = Gdx.graphics.getHeight() - screenY;

        // Handle sound button first
        if (soundButtonBounds.contains(screenX, invertedY)) {
            playButtonSound();
            if (listener != null) {
                listener.onControlBGM();
            }
            return true; // Return immediately after handling sound button
        }

        // Only check help button if sound button wasn't clicked
        if (helpButtonBounds.contains(screenX, invertedY)) {
            playButtonSound();
            if (listener != null) {
                listener.onShowInstructions();
            }
            return true;
        }

        return false;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public void dispose() {
        helpButtonTexture.dispose();
        soundOnButtonTexture.dispose();
        soundOffButtonTexture.dispose();
    }
}