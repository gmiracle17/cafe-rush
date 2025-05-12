package com.caferush.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;


/**
 * Main game class for Cafe Rush
 * Request: Please make the range for clicking closer and for coffee maker, base it on specified coffee maker locations
 */
public class CafeRush extends Game implements InputProcessor {


    private static final float VIRTUAL_WIDTH = 1000;
    private static final float VIRTUAL_HEIGHT = 720;
    private static final float CHARACTER_SPEED = 200f;
    private static final float UNIT_SCALE = 4f;
    private static final float CHARACTER_HITBOX_REDUCTION = 0.5f;
    private static final float CHARACTER_SCALE = 5f;
    private static final float ANIMATION_FRAME_DURATION = 0.1f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private SpriteBatch batch;

    private Vector2 characterPosition;
    private float characterWidth;
    private float characterHeight;
    private float stateTime;

    private Texture frontTexture, backTexture, sideTexture;
    private Animation<TextureRegion> walkDown, walkUp, walkLeft, walkRight;
    private Animation<TextureRegion> currentAnimation;

    private boolean nearMachine = false;
    private String nearMachineType = null;

    /* All Machines */
    Machines.Machine CoffeeMaker1 = new Machines.CoffeeMaker("CoffeeMaker1", 1,6, 10, 6, 11);
    Machines.Machine CoffeeMaker2 = new Machines.CoffeeMaker("CoffeeMaker2", 2,7, 10, 7, 11);
    Machines.Machine CoffeeMaker3 = new Machines.CoffeeMaker("CoffeeMaker3", 3,8, 10, 8, 11);

    Machines.Machine Pastry1 = new Machines.PastryMaker("Pastry1", 7,9);

    Machines.Machine Oven1 = new Machines.Oven("Oven1", 1, new int[]{15, 16, 27, 28}, 12,11);
    Machines.Machine Oven2 = new Machines.Oven("Oven2", 2, new int[]{17, 18, 29, 30}, 14,11);

    private Machines.Machine[] machinesList;

    @Override
    public void create() {

        camera = new OrthographicCamera();
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        // Load and set up map
        tiledMap = new TmxMapLoader().load("Cafe Map.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        // Set up camera position based on map dimensions
        int mapWidth = tiledMap.getProperties().get("width", Integer.class);
        int mapHeight = tiledMap.getProperties().get("height", Integer.class);
        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);

        float mapPixelWidth = mapWidth * tileWidth * UNIT_SCALE;
        float mapPixelHeight = mapHeight * tileHeight * UNIT_SCALE;
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0);
        camera.update();

        // Load character textures
        frontTexture = new Texture("pngs/cat-waiter-front.png");
        backTexture = new Texture("pngs/cat-waiter-back.png");
        sideTexture = new Texture("pngs/cat-waiter-side.png");

        // Create character animations
        walkDown = createAnimation(frontTexture);
        walkUp = createAnimation(backTexture);
        walkRight = createAnimation(sideTexture);
        walkLeft = flipAnimation(walkRight);

        // Initialize character state
        currentAnimation = walkDown;
        characterPosition = new Vector2(757, 512);
        stateTime = 0f;

        machinesList = new Machines.Machine[]{CoffeeMaker1, CoffeeMaker2, CoffeeMaker3, Pastry1, Oven1, Oven2};

        // for collision detection
        TextureRegion frame = walkDown.getKeyFrames()[0];
        characterWidth = frame.getRegionWidth() * CHARACTER_SCALE;
        characterHeight = frame.getRegionHeight() * CHARACTER_SCALE;

        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        Gdx.gl.glClearColor(0.76f, 0.7f, 0.64f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle character movement and interaction
        boolean moved = handleCharacterInput(delta);

        if (moved) {
            MachineHandler.hideAllOptions(tiledMap, machinesList);
        }

        checkNearbyMachines();

        if (!moved) {
            currentAnimation = walkDown;
        }

        stateTime += delta;
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);

        // Render map and character
        camera.update();
        int[] foregroundIndices = {3, 13, 14, 15, 26, 31, 32, 33};

        // Create an array for background layers (everything except the foreground one)
        ArrayList<Integer> backgroundLayerIndices = new ArrayList<>();
        for (int i = 0; i < tiledMap.getLayers().getCount(); i++) {
            boolean isForeground = false;
            for (int index : foregroundIndices) {
                if (i == index) {
                    isForeground = true;
                    break;
                }
            }
            if (!isForeground) backgroundLayerIndices.add(i);
        }

        // Convert to int[]
        int[] backgroundArray = backgroundLayerIndices.stream().mapToInt(Integer::intValue).toArray();

        // Render background layers
        tiledMapRenderer.setView(camera);

        for (int index : backgroundArray) {
            tiledMapRenderer.render(new int[] {index});
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(currentFrame, characterPosition.x, characterPosition.y,
                currentFrame.getRegionWidth() * CHARACTER_SCALE,
                currentFrame.getRegionHeight() * CHARACTER_SCALE);
        batch.end();

        for (int index : foregroundIndices) {
            tiledMapRenderer.render(new int[] {index});
        }
    }


    //animation from a sprite sheet

    private Animation<TextureRegion> createAnimation(Texture sheet) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / 4, sheet.getHeight() / 2);
        TextureRegion[] frames = new TextureRegion[7];
        int index = 0;

        for (int col = 0; col < 4; col++) {
            if (col == 3) continue; // Skip empty frame
            frames[index++] = tmp[0][col];
        }

        for (int col = 0; col < 4; col++) {
            frames[index++] = tmp[1][col];
        }

        return new Animation<>(ANIMATION_FRAME_DURATION, frames);
    }
    // horizontal animation
    private Animation<TextureRegion> flipAnimation(Animation<TextureRegion> original) {
        TextureRegion[] flipped = new TextureRegion[original.getKeyFrames().length];
        for (int i = 0; i < original.getKeyFrames().length; i++) {
            flipped[i] = new TextureRegion(original.getKeyFrames()[i]);
            flipped[i].flip(true, false);
        }
        return new Animation<>(ANIMATION_FRAME_DURATION, flipped);
    }

    private boolean handleCharacterInput(float delta) {
        boolean moved = false;
        Vector2 oldPosition = new Vector2(characterPosition);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            characterPosition.y += CHARACTER_SPEED * delta;
            currentAnimation = walkUp;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            characterPosition.y -= CHARACTER_SPEED * delta;
            currentAnimation = walkDown;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            characterPosition.x -= CHARACTER_SPEED * delta;
            currentAnimation = walkLeft;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            characterPosition.x += CHARACTER_SPEED * delta;
            currentAnimation = walkRight;
            moved = true;
        }

        if (moved && checkCollision()) {
            characterPosition.set(oldPosition);
            moved = false;
        }

        return moved;
    }

    private boolean checkCollision() {
        float tileSize = 16 * UNIT_SCALE;

        float hitboxWidth = characterWidth * (1 - CHARACTER_HITBOX_REDUCTION);
        float hitboxHeight = characterHeight * (1 - CHARACTER_HITBOX_REDUCTION);

        float left = characterPosition.x + (characterWidth - hitboxWidth) / 2;
        float right = characterPosition.x + (characterWidth + hitboxWidth) / 2;
        float bottom = characterPosition.y + (characterHeight - hitboxHeight) / 2;
        float top = characterPosition.y + (characterHeight + hitboxHeight) / 2;

        int leftTile = (int)(left / tileSize - 0.11f);
        int rightTile = (int)(right / tileSize + 0.11f);
        int bottomTile = (int)(bottom / tileSize - 0.26f);
        int topTile = (int)(top / tileSize - 0.26f);

        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");
        if (collisionLayer == null) return false;

        for (int y = bottomTile; y <= topTile; y++) {
            for (int x = leftTile; x <= rightTile; x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null) {
                    return true;
                }
            }
        }

        return false;
    }


    private void checkNearbyMachines() {
        float tileSize = 16 * UNIT_SCALE;

        int characterTileX = (int)(characterPosition.x / tileSize);
        int characterTileY = (int)(characterPosition.y / tileSize);

        nearMachine = false;
        nearMachineType = null;

        nearMachineType = checkForSpecificMachine(characterTileX, characterTileY, 2, "coffee_maker");
        if (nearMachineType != null) {
            nearMachine = true;
            return;
        }

        nearMachineType = checkForSpecificMachine(characterTileX, characterTileY, 1, "oven");
        if (nearMachineType != null) {
            nearMachine = true;
            return;
        }

        nearMachineType = checkForSpecificMachine(characterTileX, characterTileY, 1, "pastry");
        if (nearMachineType != null) {
            nearMachine = true;
            return;
        }
    }

    /* Please remove checkMachineTypeAtExact and make it just get machineType of machine*/
    private String checkForSpecificMachine(int centerX, int centerY, int radius, String machineType) {
        for (int y = centerY - radius; y <= centerY + radius; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                String foundType = MachineHandler.checkMachineTypeAtExact(tiledMap, x, y);
                if (foundType != null && foundType.equals(machineType)) {
                    return foundType;
                }
            }
        }
        return null;
    }

    /* Shows options of machine if near and clicked */
    private void handleMachineInteraction() {
        MachineHandler.hideAllOptions(tiledMap, machinesList);

        if (nearMachineType != null) {
            for (Machines.Machine machine : machinesList) {
                if (machine.machineType.equalsIgnoreCase(nearMachineType) && !machine.isBusy) {
                    machine.showOptions(tiledMap);
                }
            }
        }
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
    public boolean keyDown(int keycode) {
        // space key to interact with machines
        if (keycode == Input.Keys.SPACE && nearMachine) {
            handleMachineInteraction();
            return true;
        }

        if (keycode == Input.Keys.NUM_1)
            tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
        if (keycode == Input.Keys.NUM_2)
            tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 worldCoords = viewport.unproject(new Vector2(screenX, screenY));

        int tileX = (int) (worldCoords.x / (16 * UNIT_SCALE));
        int tileY = (int) (worldCoords.y / (16 * UNIT_SCALE));

        MachineHandler.handleOptionsHover(tiledMap, tileX, tileY, machinesList);
        return false;
    }

    private void handleOptionClick(int tileX, int tileY) {
        boolean processed = false;

        for (Machines.Machine machine : machinesList) {
            if (machine.isBusy) {
                System.out.println(machine.name + " (ID: " + machine.machineId + ") is busy, skipping.");
                continue;
            }

            TiledMapTileLayer optionsLayer = (TiledMapTileLayer) tiledMap.getLayers().get(machine.optionsLayer);
            if (optionsLayer == null || !optionsLayer.isVisible()) continue;

            TiledMapTileLayer.Cell cell = optionsLayer.getCell(tileX, tileY);

            if (cell != null && cell.getTile() != null) {
                String order = cell.getTile().getProperties().get("order", String.class);

                if (order != null) {
                    boolean started = machine.startProcess(tiledMap, cell);

                    if (started) {
                        System.out.println("Started " + machine.name + " (ID: " + machine.machineId + ") with order: " + order);
                        machine.hideOptions();
                        processed = true;
                        break;
                    } else {
                        System.out.println(machine.name + " (ID: " + machine.machineId + ") is busy.");
                    }
                } else {
                    System.out.println("No 'order' property found on the clicked tile.");
                }
            }
        }

        // Optional: If no machine was available to process the order, you can handle it here
        if (!processed) {
            System.out.println("No available machine to process the order.");
        }
    }




    private Machines.Machine getMachineAt(int tileX, int tileY) {
        for (Machines.Machine machine : machinesList) {
            if (machine.machineLayer != null) {
                TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(machine.machineLayer);
                if (layer != null) {
                    TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                    if (cell != null && cell.getTile() != null) {
                        String prop = cell.getTile().getProperties().get("machine", String.class);
                        if (machine.machineType.equals(prop)) {
                            return machine;
                        }
                    }
                }
            }
        }
        return null;
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            Vector2 worldCoords = viewport.unproject(new Vector2(screenX, screenY));
            int tileX = (int) (worldCoords.x / (16 * UNIT_SCALE));
            int tileY = (int) (worldCoords.y / (16 * UNIT_SCALE));
            handleOptionClick(tileX, tileY);
        }
        return false;
    }


    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    public boolean scrolled(int amount) { return false; }
}