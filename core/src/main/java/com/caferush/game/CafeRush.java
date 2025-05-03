package com.caferush.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class CafeRush extends ApplicationAdapter {
    @Override
    public void create() {
        Gdx.app.log("CafeRush", "Game Started!");
    }

    @Override
    public void render() {
        // Clear the screen with a dark gray color
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        Gdx.app.log("CafeRush", "Game Closed.");
    }
}
