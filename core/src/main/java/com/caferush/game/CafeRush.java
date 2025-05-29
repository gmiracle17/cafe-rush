package com.caferush.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;


/**
 * Main game class for Cafe Rush
 *
 * */
public class CafeRush extends Game implements InputProcessor {

    private GameMenu gameMenu;
    private boolean isMenuActive = true;

    private Music bgm;

    private static final float VIRTUAL_WIDTH = 1000;
    private static final float VIRTUAL_HEIGHT = 720;
    private static final float CHARACTER_SPEED = 200f;
    private static final float UNIT_SCALE = 4f;
    private static final float CHARACTER_HITBOX_REDUCTION = 0.5f;
    private static final float CHARACTER_SCALE = 5f;
    private static final float ANIMATION_FRAME_DURATION = 0.1f;


    private OrderHandling orderHandling;
    private CustomerHandler customerHandler;

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
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT+75, camera);
        viewport.apply();

        // Load and set up map
        tiledMap = new TmxMapLoader().load("Cafe Map.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        // Set up camera position based on map dimensions
        int mapWidth = tiledMap.getProperties().get("width", Integer.class);
        int mapHeight = tiledMap.getProperties().get("height", Integer.class)-1;
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

        orderHandling = new OrderHandling();
        customerHandler = new CustomerHandler(orderHandling);    

        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(this);

        bgm = Gdx.audio.newMusic(Gdx.files.internal("sounds/bgm.mp3"));
        bgm.setLooping(true);
        bgm.setVolume(0.2f);
        bgm.play();

        // Game Menu
        gameMenu = new GameMenu(new GameMenu.MenuListener() {
            @Override
            public void onStartGame() {
                isMenuActive = false; // Switch to game
            }
            @Override
            public void onExitGame() {
                Gdx.app.exit(); // Exit the application
            }
        });
    }

    @Override
    public void render() {
        if (isMenuActive) {
            // Render the game menu
            gameMenu.render(batch);
            return; // Skip the rest of the render method
        }

        float delta = Gdx.graphics.getDeltaTime();
        customerHandler.update(delta);

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
        int[] foregroundIndices = {3, 4, 14, 15, 16, 28, 32, 33, 34};

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

        // render the seats (object layer)
        MapLayer seatLayer = tiledMap.getLayers().get("Seats");
        if (seatLayer != null) {
            for (MapObject obj : seatLayer.getObjects()) {
                if (obj instanceof TiledMapTileMapObject){
                    TiledMapTileMapObject seatObj = (TiledMapTileMapObject) obj;
                    batch.draw(seatObj.getTextureRegion(),
                            seatObj.getX() * UNIT_SCALE,
                            seatObj.getY() * UNIT_SCALE,
                            seatObj.getTextureRegion().getRegionWidth() * UNIT_SCALE,
                            seatObj.getTextureRegion().getRegionHeight() * UNIT_SCALE);
                }
            }
        }

        batch.draw(currentFrame, characterPosition.x, characterPosition.y,
            currentFrame.getRegionWidth() * CHARACTER_SCALE,
            currentFrame.getRegionHeight() * CHARACTER_SCALE);

        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (customer.sprite != null) {
                batch.draw(
                    customer.sprite,
                    customer.position.x,
                    customer.position.y,
                    customer.sprite.getRegionWidth() * UNIT_SCALE,
                    customer.sprite.getRegionHeight() * UNIT_SCALE
                );
            }
        }

        if (orderHandling != null) {
            orderHandling.renderOrders(batch, UNIT_SCALE);
        }

        customerHandler.renderSpawnPatience(batch, delta);
        
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
        if (customerHandler != null) customerHandler.dispose();
        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
        }
        if (gameMenu != null) gameMenu.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        // for game menu
        if (isMenuActive && keycode == Input.Keys.ENTER) {
            isMenuActive = false; // Switch to game mode
            return true;
        }

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
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);

        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (customer.beingDragged) {
                customer.beingDragged = false;

                MapLayer seatLayer = tiledMap.getLayers().get("Seats");
                if (seatLayer != null) {
                    for (MapObject obj : seatLayer.getObjects()) {
                        if (obj instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject seat = (TiledMapTileMapObject) obj;

                            // Calculate seat center and bounds
                            float seatCenterX = seat.getX() * UNIT_SCALE + (seat.getTextureRegion().getRegionWidth() * UNIT_SCALE)/2;
                            float seatCenterY = seat.getY() * UNIT_SCALE + (seat.getTextureRegion().getRegionHeight() * UNIT_SCALE)/2;

                            // Check if dropped near seat center
                            if (Math.abs(clickedPosition.x - seatCenterX) < 32 &&
                                Math.abs(clickedPosition.y - seatCenterY) < 32) {

                                // Snap to seat center
                                customer.position.set(
                                    seatCenterX - 30,
                                    seatCenterY
                                );
                                customer.isSeated = true;

                                int randomOrder = MathUtils.random(orderHandling.getMenuItems().length - 1);
                                orderHandling.addOrder(seat, randomOrder, customer);

                                return true;
                            }
                        }
                    }
                }
                // Return to spawn if no seat found
                customer.position.set(800, 210);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isMenuActive) {
            if (gameMenu.touchDown(screenX, screenY)) {
                return true; // Exit button clicked
            }
        }

        if (button != Input.Buttons.LEFT) {
            return false;
        }

        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);
        Vector2 worldPosition = new Vector2(clickedPosition.x, clickedPosition.y);

        // 1. Check if any customer was clicked
        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (!customer.isSeated && customerClicked(customer, worldPosition)) {
                customer.beingDragged = true;
                customer.offset.set(
                    clickedPosition.x - customer.position.x,
                    clickedPosition.y - customer.position.y
                );
                return true;
            }
        }

        Vector2 worldCoords = viewport.unproject(new Vector2(screenX, screenY));
        int tileX = (int) (worldCoords.x / (16 * UNIT_SCALE));
        int tileY = (int) (worldCoords.y / (16 * UNIT_SCALE));
        handleOptionClick(tileX, tileY);

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);
        
        // Update position for the customer being dragged
        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (customer.beingDragged) {
                customer.position.set(
                    clickedPosition.x - customer.offset.x, 
                    clickedPosition.y - customer.offset.y
                );
                return true;
            }
            if (orderHandling != null) {
                orderHandling.renderOrders(batch, UNIT_SCALE);
            }
            batch.end();
        }
        return false;
    }

    private boolean customerClicked(CustomerHandler.Customer customer, Vector2 worldPosition) {
        float width = 16 * UNIT_SCALE;
        float height = 16 * UNIT_SCALE;
        return worldPosition.x >= customer.position.x && 
               worldPosition.x <= customer.position.x + width &&
               worldPosition.y >= customer.position.y && 
               worldPosition.y <= customer.position.y + height;
    }


    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    public boolean scrolled(int amount) { return false; }
}