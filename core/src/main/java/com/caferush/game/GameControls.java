package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class GameControls {

    private final Texture helpButtonTexture;
    private final Texture soundOnButtonTexture;
    private final Texture soundOffButtonTexture;

    private final Rectangle helpButtonBounds;
    private final Rectangle soundButtonBounds;

    private final Vector2 helpButtonPosition;
    private final Vector2 soundButtonPosition;

    private final BitmapFont font;
    private final GlyphLayout layout;
    private boolean mute;

    private int day;
    private int goal;
    private int earning;

    public interface ControlsListener {
        void onLeaveGame();
        void onShowInstructions();
        void onControlBGM();
    }

    private ControlsListener listener;

    public GameControls(ControlsListener listener) {
        this.listener = listener;
        this.day = 1;
        this.goal = day * 100;
        this.earning = 0;

        font = new BitmapFont();
        font.getData().setScale(1.9f, 1.4f);
        font.setColor(Color.WHITE);

        layout = new GlyphLayout();

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

    public void render(SpriteBatch batch) {
        batch.draw(helpButtonTexture, helpButtonPosition.x, helpButtonPosition.y, helpButtonBounds.width, helpButtonBounds.height);
        Texture currentSoundTexture = mute ? soundOffButtonTexture : soundOnButtonTexture;
        batch.draw(currentSoundTexture, soundButtonPosition.x, soundButtonPosition.y, soundButtonBounds.width, soundButtonBounds.height);
    }

    public boolean touchDown(int screenX, int screenY) {
        int invertedY = Gdx.graphics.getHeight() - screenY;

        // Handle sound button first
        if (soundButtonBounds.contains(screenX, invertedY)) {
            if (listener != null) {
                listener.onControlBGM();
            }
            return true; // Return immediately after handling sound button
        }

        // Only check help button if sound button wasn't clicked
        if (helpButtonBounds.contains(screenX, invertedY)) {
            if (listener != null) {
                listener.onShowInstructions();
            }
            return true;
        }

        return false;
    }

    public void setEarning(int earning) {
        if (earning < goal) {
            this.earning = earning;
        } else { // reaches goal
            this.earning = 0;
            this.day++;
            this.goal = this.day * 100;
        }
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public void dispose() {
        font.dispose();
        helpButtonTexture.dispose();
        soundOnButtonTexture.dispose();
        soundOffButtonTexture.dispose();
    }
}
