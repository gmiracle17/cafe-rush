package com.caferush.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CafeRush extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private float boxX = 100;
    private float boxY = 100;
    private final float boxSize = 50;
    private final float moveSpeed = 200;

    private float screenWidth;
    private float screenHeight;

    @Override
    public void create() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        handleInput(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1); // Green box
        shapeRenderer.rect(boxX, boxY, boxSize, boxSize);
        shapeRenderer.end();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            boxY += moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            boxY -= moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            boxX -= moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            boxX += moveSpeed * delta;
        }

        // Keep box within screen boundaries
        boxX = Math.max(0, Math.min(boxX, screenWidth - boxSize));
        boxY = Math.max(0, Math.min(boxY, screenHeight - boxSize));
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
