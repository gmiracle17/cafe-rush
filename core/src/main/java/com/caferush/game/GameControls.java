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

    private final Texture leaveButtonTexture;
    private final Texture helpButtonTexture;
    private final Texture pauseButtonTexture;
    private final Texture soundOnButtonTexture;
    private final Texture soundOffButtonTexture;

    private final Rectangle leaveButtonBounds;
    private final Rectangle helpButtonBounds;
    private final Rectangle pauseButtonBounds;
    private final Rectangle soundButtonBounds;

    private final Vector2 leaveButtonPosition;
    private final Vector2 helpButtonPosition;
    private final Vector2 pauseButtonPosition;
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

        leaveButtonTexture = new Texture(Gdx.files.internal("leave.png"));
        helpButtonTexture = new Texture(Gdx.files.internal("help.png"));
        pauseButtonTexture = new Texture(Gdx.files.internal("pause.png"));
        soundOnButtonTexture = new Texture(Gdx.files.internal("soundon.png"));
        soundOffButtonTexture = new Texture(Gdx.files.internal("soundoff.png"));

        int buttonWidth = 100;
        int buttonHeight = 60;

        leaveButtonPosition = new Vector2(36, 100);
        helpButtonPosition = new Vector2(890, 100);
        pauseButtonPosition = new Vector2(136, 100);
        soundButtonPosition = new Vector2(790, 100);

        leaveButtonBounds = new Rectangle(leaveButtonPosition.x, leaveButtonPosition.y, buttonWidth, buttonHeight);
        helpButtonBounds = new Rectangle(helpButtonPosition.x, helpButtonPosition.y, buttonWidth, buttonHeight);
        pauseButtonBounds = new Rectangle(pauseButtonPosition.x, pauseButtonPosition.y, buttonWidth, buttonHeight - 7);
        soundButtonBounds = new Rectangle(soundButtonPosition.x, soundButtonPosition.y, buttonWidth, buttonHeight - 5);
    }

    public void render(SpriteBatch batch) {
        batch.begin();

        String earningsText = earning + " / " + goal;
        String dayText = "Day: " + day;

        layout.setText(font, dayText);
        float dayX = 800 - layout.width / 2f;

        font.draw(batch, dayText, dayX, 566);

        layout.setText(font, earningsText);
        float earningsX = 800 - layout.width / 2f;

        font.draw(batch, earningsText, earningsX, 536);

        batch.draw(leaveButtonTexture, leaveButtonPosition.x, leaveButtonPosition.y, leaveButtonBounds.width, leaveButtonBounds.height);
        batch.draw(helpButtonTexture, helpButtonPosition.x, helpButtonPosition.y, helpButtonBounds.width, helpButtonBounds.height);
        batch.draw(pauseButtonTexture, pauseButtonPosition.x, pauseButtonPosition.y, pauseButtonBounds.width, pauseButtonBounds.height);

        Texture currentSoundTexture = mute ? soundOffButtonTexture : soundOnButtonTexture;
        batch.draw(currentSoundTexture, soundButtonPosition.x, soundButtonPosition.y, soundButtonBounds.width, soundButtonBounds.height);

        batch.end();
    }

    public boolean touchDown(int screenX, int screenY) {
        int invertedY = Gdx.graphics.getHeight() - screenY;

        if (screenX >= 60 && screenX <= 160 && invertedY >= 20 && invertedY <= 90) {
            if (listener != null) listener.onLeaveGame();
        }

        if (screenX >= 1140 && screenX <= 1234 && invertedY >= 20 && invertedY <= 90) {
            if (listener != null) listener.onShowInstructions();
        }

        if (screenX >= 1022 && screenX <= 1095 && invertedY >= 20 && invertedY <= 90) {
            if (listener != null) listener.onControlBGM();
        }

        if (screenX >= 186 && screenX <= 258 && invertedY >= 20 && invertedY <= 90) {
            if (listener != null) Gdx.app.log("Game Controls", "Pause Button Clicked!");
        }

        return false;
    }

    // call this pag dineliver order
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
        leaveButtonTexture.dispose();
        helpButtonTexture.dispose();
        pauseButtonTexture.dispose();
        soundOnButtonTexture.dispose();
        soundOffButtonTexture.dispose();
    }
}
