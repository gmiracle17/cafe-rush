package com.caferush.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CafeRush extends ApplicationAdapter implements InputProcessor {

    private static final float VIRTUAL_WIDTH = 1000;
    private static final float VIRTUAL_HEIGHT = 720;

    OrthographicCamera camera;
    Viewport viewport;
    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;

    Texture frontTexture, backTexture, sideTexture;
    Animation<TextureRegion> walkDown, walkUp, walkLeft, walkRight;
    Animation<TextureRegion> currentAnimation;

    SpriteBatch batch;
    Vector2 characterPosition;
    float characterSpeed = 200f;
    float stateTime;

    @Override
    public void create () {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        tiledMap = new TmxMapLoader().load("Cafe with Product Options.tmx");
        float unitScale = 4f;
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, unitScale);

        int mapWidth = tiledMap.getProperties().get("width", Integer.class);
        int mapHeight = tiledMap.getProperties().get("height", Integer.class);
        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);

        float mapPixelWidth = mapWidth * tileWidth * unitScale;
        float mapPixelHeight = mapHeight * tileHeight * unitScale;
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0);
        camera.update();

        frontTexture = new Texture("pngs/cat-black-front.png");
        backTexture = new Texture("pngs/cat-black-back.png");
        sideTexture = new Texture("pngs/cat-black-side.png");

        walkDown = createAnimation(frontTexture);
        walkUp = createAnimation(backTexture);
        walkRight = createAnimation(sideTexture);
        walkLeft = flipAnimation(walkRight);

        currentAnimation = walkDown; // idle animation
        characterPosition = new Vector2(500, 500);
        stateTime = 0f;

        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(this);
    }

    private Animation<TextureRegion> createAnimation(Texture sheet) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / 4, sheet.getHeight() / 2);
        TextureRegion[] frames = new TextureRegion[7];
        int index = 0;

        for (int col = 0; col < 4; col++) {
            if (col == 3) continue; // skip  yung empty frame
            frames[index++] = tmp[0][col];
        }

        for (int col = 0; col < 4; col++) {
            frames[index++] = tmp[1][col];
        }

        return new Animation<>(0.1f, frames);
    }


    private Animation<TextureRegion> flipAnimation(Animation<TextureRegion> original) {
        TextureRegion[] flipped = new TextureRegion[original.getKeyFrames().length];
        for (int i = 0; i < original.getKeyFrames().length; i++) {
            flipped[i] = new TextureRegion(original.getKeyFrames()[i]);
            flipped[i].flip(true, false);
        }
        return new Animation<>(0.1f, flipped);
    }

    @Override
    public void render () {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0.76f, 0.7f, 0.64f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean moved = handleCharacterInput(delta);

        if (!moved) {
            currentAnimation = walkDown; // face forward pag di gumagalaw
        }

        stateTime += delta;
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);

        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float scale = 5f; // size ng char
        batch.draw(currentFrame, characterPosition.x, characterPosition.y,
                currentFrame.getRegionWidth() * scale,
                currentFrame.getRegionHeight() * scale);
        batch.end();
    }

    private boolean handleCharacterInput(float delta) {
        boolean moved = false;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            characterPosition.y += characterSpeed * delta;
            currentAnimation = walkUp;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            characterPosition.y -= characterSpeed * delta;
            currentAnimation = walkDown;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            characterPosition.x -= characterSpeed * delta;
            currentAnimation = walkLeft;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            characterPosition.x += characterSpeed * delta;
            currentAnimation = walkRight;
            moved = true;
        }

        return moved;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) ((OrthogonalTiledMapRenderer) tiledMapRenderer).dispose();
        if (frontTexture != null) frontTexture.dispose();
        if (backTexture != null) backTexture.dispose();
        if (sideTexture != null) sideTexture.dispose();
        if (batch != null) batch.dispose();
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.NUM_1)
            tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
        if (keycode == Input.Keys.NUM_2)
            tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());
        return false;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    public boolean scrolled(int amount) { return false; }
}
