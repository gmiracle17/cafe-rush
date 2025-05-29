package com.caferush.game;

/**
 * mga plano ko gawin dito...
 * back to main menu,,,
 * dito iddisplay day
 * baka earning dito na rin not sure
 * baka mute bgm... idk
 *
 * so far problems... pag nagrreturn to main menu..... nagzzoomin
 * d pa dynamic un day number since tbf... wala pang earning system
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class GameControls {

    private final BitmapFont font;

    public GameControls() {
        // Default font tbh pede niyo palitan font may dnl akong ttf font na dogica.ttf but d ko mapagana
        // kailangan daw ata fnt ang import idk d ko alam how
        font = new BitmapFont();
        font.getData().setScale(1.9f,1.4f); // Make it larger if needed
        font.setColor(Color.valueOf("745D44"));
    }

    public void showDayNumber(SpriteBatch batch) {
        font.draw(batch, "Day: 1", 450, 800);
    }

    public boolean exitGame(SpriteBatch batch) {
        if (Gdx.input.justTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip y axis

            // Approximate width and height of the text "Day: 1"
            // You can get exact width using font methods, but roughly:
            float textX = 450;
            float textY = 800;
            float textWidth = font.getRegion().getRegionWidth() * font.getData().scaleX * 4f; // rough guess, adjust multiplier as needed
            float textHeight = font.getLineHeight() * font.getData().scaleY;

            // Check if click is inside "Day: 1" text area
            if (x >= 450 && x <= 500 + textWidth && y >= 800 && y <= 850) {
                return true;
            }
        }
        return false;
    }


    public void dispose() {
        font.dispose();
    }
}