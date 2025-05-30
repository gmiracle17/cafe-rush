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
import com.badlogic.gdx.utils.ObjectMap;


/**
 * Main game class for Cafe Rush
 *
 * */
public class CafeRush extends ApplicationAdapter implements InputProcessor {

    private GameMenu gameMenu;
    private GameControls gameControls;
    private Instructions instructions;
    private boolean isMenuActive = true;
    private boolean isInstructionsActive = false;
    private boolean isPaused = false;
    private boolean isFirstStart = true;

    private Music bgm;
    private boolean mute = false;

    private static final float VIRTUAL_WIDTH = 1000;
    private static final float VIRTUAL_HEIGHT = 750;
    private static final float CHARACTER_SPEED = 200f;
    private static final float UNIT_SCALE = 4f;
    private static final float CHARACTER_HITBOX_REDUCTION = 0.5f;
    private static final float CHARACTER_SCALE = 5f;
    private static final float ANIMATION_FRAME_DURATION = 0.1f;


    private OrderHandling orderHandling;
    private CustomerHandler customerHandler;
    private Inventory inventory;
    // Position inventory in the middle bottom of the screen
    private static final float INVENTORY_X = VIRTUAL_WIDTH / 2 - (32 * 8 * 1.5f) / 2; // Center horizontally (considering 8 slots)
    private static final float INVENTORY_Y = 20; 
    private static final float INVENTORY_SCALE = 1.5f;

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

    private ObjectMap<Integer, CustomerHandler.Customer> occupiedSeats = new ObjectMap<>();

    @Override
    public void create() {
        initializeGame();
        occupiedSeats = new ObjectMap<>();
    }

    private void initializeGame() {
        // Clear occupied seats map
        if (occupiedSeats == null) {
            occupiedSeats = new ObjectMap<>();
        } else {
            occupiedSeats.clear();
        }

        // Create new camera and viewport if they don't exist
        if (camera == null) {
            camera = new OrthographicCamera();
        }
        if (viewport == null) {
            viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT + 75, camera);
        }
        viewport.apply();

        // Load and set up map
        tiledMap = new TmxMapLoader().load("Cafe Map.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        // Set up camera position based on map dimensions
        int mapWidth = tiledMap.getProperties().get("width", Integer.class);
        int mapHeight = tiledMap.getProperties().get("height", Integer.class) - 1;
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

        // Initialize machines
        CoffeeMaker1 = new Machines.CoffeeMaker("CoffeeMaker1", 1, 6, 10, 6, 11);
        CoffeeMaker2 = new Machines.CoffeeMaker("CoffeeMaker2", 2, 7, 10, 7, 11);
        CoffeeMaker3 = new Machines.CoffeeMaker("CoffeeMaker3", 3, 8, 10, 8, 11);
        Pastry1 = new Machines.PastryMaker("Pastry1", 7, 9);
        Oven1 = new Machines.Oven("Oven1", 1, new int[]{15, 16, 27, 28}, 12, 11);
        Oven2 = new Machines.Oven("Oven2", 2, new int[]{17, 18, 29, 30}, 14, 11);
        machinesList = new Machines.Machine[]{CoffeeMaker1, CoffeeMaker2, CoffeeMaker3, Pastry1, Oven1, Oven2};

        // For collision detection
        TextureRegion frame = walkDown.getKeyFrames()[0];
        characterWidth = frame.getRegionWidth() * CHARACTER_SCALE;
        characterHeight = frame.getRegionHeight() * CHARACTER_SCALE;

        // Initialize game systems
        orderHandling = new OrderHandling();
        inventory = new Inventory(orderHandling, tiledMap, "Inventory");
        Machines.setInventory(inventory);
        customerHandler = new CustomerHandler(orderHandling);
        customerHandler.addCustomer(800, 210);

        // Create SpriteBatch if it doesn't exist
        if (batch == null) {
            batch = new SpriteBatch();
        }

        // Set input processor if not set
        if (Gdx.input.getInputProcessor() == null) {
            Gdx.input.setInputProcessor(this);
        }

        // Initialize or update BGM
        if (bgm == null) {
            bgm = Gdx.audio.newMusic(Gdx.files.internal("sounds/bgm.mp3"));
            bgm.setLooping(true);
            if (!mute) bgm.setVolume(0.2f);
            bgm.play();
        }

        // Initialize UI components if they don't exist
        if (gameMenu == null) {
            gameMenu = new GameMenu(new GameMenu.MenuListener() {
                @Override
                public void onStartGame() {
                    if (!isFirstStart) {
                        // Dispose current game resources
                        disposeGameResources();
                        // Reinitialize game
                        initializeGame();
                    }
                    isMenuActive = false;
                    isPaused = false;
                    isFirstStart = false;
                }

                @Override
                public void onResumeGame() {
                    isMenuActive = false;
                    isPaused = false;
                    // Resume all machines
                    for (Machines.Machine machine : machinesList) {
                        machine.resumeProcess();
                    }
                }

                @Override
                public void onExitGame() {
                    Gdx.app.exit();
                }
            });
        }

        if (instructions == null) {
            instructions = new Instructions(new Instructions.InstructionListener() {
                @Override
                public void onBackToGame() {
                    isInstructionsActive = false;
                }
            });
        }

        if (gameControls == null) {
            gameControls = new GameControls(new GameControls.ControlsListener() {
                @Override
                public void onLeaveGame() {
                    isMenuActive = true;
                    isPaused = true;
                }
                @Override
                public void onShowInstructions() {
                    isInstructionsActive = true;
                }
                @Override
                public void onControlBGM() {
                    mute = !mute;
                    if (mute) {
                        bgm.setVolume(0f);
                    } else {
                        bgm.setVolume(0.2f);
                    }
                    gameControls.setMute(mute);
                }
            });
        }
    }

    private void disposeGameResources() {
        // Dispose of all game resources
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) ((OrthogonalTiledMapRenderer) tiledMapRenderer).dispose();
        if (frontTexture != null) frontTexture.dispose();
        if (backTexture != null) backTexture.dispose();
        if (sideTexture != null) sideTexture.dispose();
        if (customerHandler != null) {
            customerHandler.dispose();
        }
        if (orderHandling != null) {
            orderHandling.dispose();
        }
        if (inventory != null) {
            inventory.dispose();
        }
        // Clear occupied seats
        if (occupiedSeats != null) {
            occupiedSeats.clear();
        }
        // Don't dispose batch here as it's needed for menu rendering
    }

    @Override
    public void render() {
        if (isMenuActive) {
            // Render the game menu
            gameMenu.render(batch);
            return;
        }

        if (isInstructionsActive) {
            instructions.render(batch);
            return;
        }

        if (isPaused) {
            return;
        }

        float delta = Gdx.graphics.getDeltaTime();
        
        // Update customer states and handle lost patience
        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (customer.hasLostPatience()) {
                // Clear the occupied seat if customer was seated
                if (customer.currentSeatId != -1) {
                    occupiedSeats.remove(customer.currentSeatId);
                }
            }
        }
        customerHandler.update(delta);

        Gdx.gl.glClearColor(0.76f, 0.7f, 0.64f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle character movement and interaction
        boolean moved = handleCharacterInput(delta);

        if (moved) {
            Machines.hideAllOptions(tiledMap, machinesList);
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

        // Always render ALL seats first
        MapLayer seatLayer = tiledMap.getLayers().get("Seats");
        if (seatLayer != null) {
            for (MapObject obj : seatLayer.getObjects()) {
                if (obj instanceof TiledMapTileMapObject) {
                    TiledMapTileMapObject seatObj = (TiledMapTileMapObject) obj;
                    batch.draw(seatObj.getTextureRegion(),
                            seatObj.getX() * UNIT_SCALE,
                            seatObj.getY() * UNIT_SCALE,
                            seatObj.getTextureRegion().getRegionWidth() * UNIT_SCALE,
                            seatObj.getTextureRegion().getRegionHeight() * UNIT_SCALE);
                }
            }
        }

        // Draw character
        batch.draw(currentFrame, characterPosition.x, characterPosition.y,
            currentFrame.getRegionWidth() * CHARACTER_SCALE,
            currentFrame.getRegionHeight() * CHARACTER_SCALE);

        // Draw customers
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

        // Render inventory items
        if (inventory != null) {
            inventory.render(batch);
        }
        
        batch.end();

        for (int index : foregroundIndices) {
            tiledMapRenderer.render(new int[] {index});
        }

        // Update and render game controls
        gameControls.render(batch);

        // Handle inventory drag and drop
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            inventory.handleInput(touchPos.x, touchPos.y, true, tiledMap);
        } else {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            inventory.handleInput(touchPos.x, touchPos.y, false, tiledMap);
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
                String foundType = Machines.checkMachineTypeAtExact(tiledMap, x, y);
                if (foundType != null && foundType.equals(machineType)) {
                    return foundType;
                }
            }
        }
        return null;
    }

    /* Shows options of machine if near and clicked */
    private void handleMachineInteraction() {
        Machines.hideAllOptions(tiledMap, machinesList);

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
        disposeGameResources();
        if (batch != null) batch.dispose();
        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
        }
        if (gameMenu != null) gameMenu.dispose();
        if (instructions != null) instructions.dispose();
        if (gameControls != null) gameControls.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        // Escape key to toggle menu and pause
        if (keycode == Input.Keys.ESCAPE) {
            isMenuActive = !isMenuActive;
            isPaused = isMenuActive;
            
            // Pause/resume all machines
            for (Machines.Machine machine : machinesList) {
                if (isPaused) {
                    machine.pauseProcess();
                } else {
                    machine.resumeProcess();
                }
            }
            return true;
        }

        if (isPaused) {
            return false;
        }

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

        // 'E' key to collect orders and serve customers
        if (keycode == Input.Keys.E) {
            // First try to collect any completed orders
            if (collectNearbyOrder()) {
                return true;
            }
            // If no orders to collect, try to serve customers
            serveNearbyCustomers();
            return true;
        }

        if (keycode == Input.Keys.NUM_1)
            tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
        if (keycode == Input.Keys.NUM_2)
            tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());

        return false;
    }

    // method to collect nearby orders
    private boolean collectNearbyOrder() {
        float tileSize = 16 * UNIT_SCALE;
        int characterTileX = (int)(characterPosition.x / tileSize);
        int characterTileY = (int)(characterPosition.y / tileSize);
        int radius = 2; // Same radius as machine interaction

        for (Machines.Machine machine : machinesList) {
            if (machine.orderReady) {
                // Check if player is within radius of machine tiles
                String machineType = machine.machineType;
                for (int y = characterTileY - radius; y <= characterTileY + radius; y++) {
                    for (int x = characterTileX - radius; x <= characterTileX + radius; x++) {
                        String foundType = Machines.checkMachineTypeAtExact(tiledMap, x, y);
                        if (foundType != null && foundType.equals(machineType)) {
                            // Try to collect the order
                            if (inventory.addOrder(machine.choice)) {
                                // Clear the machine's display
                                TiledMapTileLayer displayLayer = (TiledMapTileLayer) tiledMap.getLayers().get(machine.produceDisplayLayer);
                                if (displayLayer != null && displayLayer.getCell(machine.displayX, machine.displayY) != null) {
                                    displayLayer.getCell(machine.displayX, machine.displayY).setTile(null);
                                }

                                // Hide the status boxes
                                String[] colors = {" Green ", " Yellow ", " Red "};
                                for (String color : colors) {
                                    TiledMapTileLayer boxLayer = (TiledMapTileLayer) tiledMap.getLayers().get(machine.produceDisplayBoxLayer + color + machine.machineId);
                                    if (boxLayer != null) {
                                        boxLayer.setVisible(false);
                                    }
                                }

                                machine.orderReady = false;
                                machine.isBusy = false;
                                System.out.println("Order collected from " + machine.name);
                                return true;
                            } else {
                                System.out.println("Inventory is full!");
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void serveNearbyCustomers() {
        float tileSize = 16 * UNIT_SCALE;
        float interactionRange = tileSize * 2; // 2 tiles range for serving

        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (!customer.isSeated) continue;

            // Calculate distance between player and customer
            float distanceX = Math.abs(characterPosition.x - customer.position.x);
            float distanceY = Math.abs(characterPosition.y - customer.position.y);

            if (distanceX <= interactionRange && distanceY <= interactionRange) {
                // Get customer's order from OrderHandling
                String customerOrder = orderHandling.getOrderForCustomer(customer);
                if (customerOrder != null) {
                    // Try to serve the order from inventory
                    if (inventory.serveOrder(customerOrder)) {
                        // Order successfully served
                        orderHandling.completeOrder(customer);
                        
                        // Free up the seat
                        if (customer.currentSeatId != -1) {
                            occupiedSeats.remove(customer.currentSeatId);
                        }
                        
                        // Remove the served customer completely
                        customerHandler.removeCustomer(customer);
                        
                        // Enable random spawning of new customer
                        customerHandler.customerServed();
                        
                        System.out.println("Successfully served customer!");
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 worldCoords = viewport.unproject(new Vector2(screenX, screenY));

        int tileX = (int) (worldCoords.x / (16 * UNIT_SCALE));
        int tileY = (int) (worldCoords.y / (16 * UNIT_SCALE));

        Machines.handleOptionsHover(tiledMap, tileX, tileY, machinesList);
        return false;
    }

    private void handleOptionClick(int tileX, int tileY, Machines.Machine[] machines, TiledMap tiledMap) {
        boolean processed = false;

        // Handle normal machine interaction
        for (Machines.Machine machine : machines) {
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

        if (!processed) {
            System.out.println("No available machine to process the order.");
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);

        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (customer.beingDragged) {
                customer.beingDragged = false;
                boolean wasSeated = false;

                MapLayer seatLayer = tiledMap.getLayers().get("Seats");
                if (seatLayer != null) {
                    for (MapObject obj : seatLayer.getObjects()) {
                        if (obj instanceof TiledMapTileMapObject) {
                            TiledMapTileMapObject seat = (TiledMapTileMapObject) obj;
                            int seatId = seat.getProperties().get("Seat", -1, Integer.class);

                            // Skip if seat is occupied
                            if (occupiedSeats.containsKey(seatId)) {
                                continue;
                            }

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
                                
                                // Reset customer state and properly seat them
                                customer.stopAllTimers();
                                customerHandler.seatCustomer(customer);
                                
                                // Mark seat as occupied
                                occupiedSeats.put(seatId, customer);
                                customer.currentSeatId = seatId;
                                
                                int randomOrder = MathUtils.random(orderHandling.getMenuItems().length - 1);
                                orderHandling.addOrder(seat, randomOrder, customer);

                                wasSeated = true;
                                break;
                            }
                        }
                    }
                }

                // Return to spawn if no seat found
                if (!wasSeated) {
                    customer.position.set(800, 210);
                    // Don't reset timers if returning to spawn without being seated
                }
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

        if (isInstructionsActive) {
            if (instructions.touchDown(screenX, screenY)) {
                return true;
            }
        }

        if (gameControls.touchDown(screenX, screenY)) {
            return true; // Leave button clicked
        }

        if (button != Input.Buttons.LEFT) {
            return false;
        }

        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);
        Vector2 worldPosition = new Vector2(clickedPosition.x, clickedPosition.y);

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
        handleOptionClick(tileX, tileY, machinesList, tiledMap);

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