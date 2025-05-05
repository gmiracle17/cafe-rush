package com.caferush.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class CafeRush extends ApplicationAdapter implements InputProcessor {
    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private float unitScale = 3.5f;

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        tiledMap = new TmxMapLoader().load("cafe-rush-maps/Cafe with Product Options.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, unitScale);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        Vector3 worldCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int tileX = (int) (worldCoords.x / (16 * unitScale));
        int tileY = (int) (worldCoords.y / (16 * unitScale));
        MachineHandler.handleOptionsHover(tiledMap, tileX, tileY);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        MachineHandler.handleMachineClick(tiledMap, unitScale, worldCoords);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) (worldCoords.x / (16 * unitScale));
        int tileY = (int) (worldCoords.y / (16 * unitScale));

        MachineHandler.handleOptionsHover(tiledMap, tileX, tileY);

        return true;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int x, int y, int pointer) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}