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

    private Texture leaveButtonTexture;
    private Texture helpButtonTexture;
    private Rectangle leaveButtonBounds;
    private Rectangle helpButtonBounds;
    private Vector2 leaveButtonPosition;
    private Vector2 helpButtonPosition;

    private final BitmapFont font;
    private final GlyphLayout layout;

    private int day;
    private int goal;
    private int earning;

    public interface ControlsListener {
        void onLeaveGame();
        void onShowInstructions();
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

        int buttonWidth = 100;
        float aspectRatio = (float) leaveButtonTexture.getHeight() / leaveButtonTexture.getWidth();
        int buttonHeight = (int) (buttonWidth * aspectRatio);

        leaveButtonPosition = new Vector2(36, 95);
        helpButtonPosition = new Vector2(890, 95);
        leaveButtonBounds = new Rectangle(leaveButtonPosition.x, leaveButtonPosition.y, buttonWidth, buttonHeight);
        helpButtonBounds = new Rectangle(helpButtonPosition.x, helpButtonPosition.y, buttonWidth, buttonHeight);
    }

    public void render(SpriteBatch batch) {
        batch.begin();

        // Center the day and earnings text
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

        batch.end();
    }

    public boolean touchDown(int screenX, int screenY) {
        int invertedY = Gdx.graphics.getHeight() - screenY;

        Gdx.app.log("GameControls", "Touch down at: " + screenX + ", " + invertedY);

        if (screenX >= 60 && screenX <= 160 && invertedY >= 20 && invertedY <= 90) {
            if (listener != null) listener.onLeaveGame();
        }

        if (screenX >= 1140 && screenX <= 1234 && invertedY >= 20 && invertedY <= 90) {
            if (listener != null) listener.onShowInstructions();
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

    public void dispose() {
        font.dispose();
        leaveButtonTexture.dispose();
        helpButtonTexture.dispose();
    }
}
