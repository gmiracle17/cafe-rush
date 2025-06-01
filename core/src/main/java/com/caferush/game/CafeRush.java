package com.caferush.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import com.badlogic.gdx.utils.ObjectMap;

// Main game class for Cafe Rush
public class CafeRush extends ApplicationAdapter implements InputProcessor {

    private GameMenu gameMenu;
    private GameControls gameControls;
    private Instructions instructions;
    private GameStatus gameStatus;
    private boolean isMenuActive = true;
    private boolean isInstructionsActive = false;
    private boolean isPaused = false;
    private boolean isFirstStart = true;

    private Music bgm;
    private boolean mute = false;

    private static final float VIRTUAL_WIDTH = 1000;
    private static final float VIRTUAL_HEIGHT = 800;
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
    Machines.Machine CoffeeMaker1, CoffeeMaker2, CoffeeMaker3, Pastry1, Oven1, Oven2;
    private Machines.Machine[] machinesList;

    private ObjectMap<Integer, CustomerHandler.Customer> occupiedSeats = new ObjectMap<>();

    private int currentDay = 1;
    private int currentEarnings = 0;
    private int earningGoal = 300;
    private float dayTimer = 180f; // 3 minutes default
    private float currentDayTime = dayTimer;
    private boolean isGameOver = false;
    private boolean isDayComplete = false;
    private BitmapFont font;

    @Override
    public void create() {
        initializeGame();
        occupiedSeats = new ObjectMap<>();
        font = new BitmapFont();
        font.getData().setScale(1.7f);  // Increase font size
    }

    private void initializeGame() {
        initializeGame(false);
    }

    private void initializeGame(boolean fromNextDay) {
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
            viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        }
        viewport.apply();

        // Initialize sounds
        Machines.initializeSounds();

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
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f - 50f, 0);
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
        CoffeeMaker1 = new Machines.CoffeeMaker("CoffeeMaker1", 1, 6, 11);
        CoffeeMaker2 = new Machines.CoffeeMaker("CoffeeMaker2", 2, 7, 11);
        CoffeeMaker3 = new Machines.CoffeeMaker("CoffeeMaker3", 3, 8, 11);
        Pastry1 = new Machines.PastryMaker("Pastry1", 7, 9);
        Oven1 = new Machines.Oven("Oven1", 1, 12, 11);
        Oven2 = new Machines.Oven("Oven2", 2, 14, 11);
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
            bgm = Gdx.audio.newMusic(Gdx.files.internal("sfx/bgm.mp3"));
            bgm.setLooping(true);
            if (!mute) bgm.setVolume(0.2f);
            bgm.play();
        }

        // Initialize UI components if they don't exist
        if (gameMenu == null) {
            gameMenu = new GameMenu(createMenuListener());
        }

        if (instructions == null) {
            instructions = new Instructions(createInstructionsListener());
        }

        if (gameControls == null) {
            gameControls = new GameControls(createControlsListener());
        }

        if (gameStatus == null) {
            gameStatus = new GameStatus(createStatusListener());
        } else {
            gameStatus.reloadAssets(); // Ensure textures are reloaded
        }

        // Only initialize day-related variables if not called from nextDay
        if (!fromNextDay) {
            currentDay = 1;
            currentEarnings = 0;
            earningGoal = 300;
        }
        
        currentDayTime = dayTimer;
        isGameOver = false;
        isDayComplete = false;
    }

    private void disposeGameResources() {
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) ((OrthogonalTiledMapRenderer) tiledMapRenderer).dispose();
        if (frontTexture != null) frontTexture.dispose();
        if (backTexture != null) backTexture.dispose();
        if (sideTexture != null) sideTexture.dispose();
        if (customerHandler != null) {
            customerHandler.dispose();
            customerHandler = null;
        }
        if (orderHandling != null) {
            orderHandling.dispose();
            orderHandling = null;
        }
        if (inventory != null) {
            inventory.dispose();
            inventory = null;
        }
        if (occupiedSeats != null) {
            occupiedSeats.clear();
        }
        Machines.dispose();
        if (gameStatus != null) {
            gameStatus.dispose();
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        // Clear the screen at the start of each frame
        Gdx.gl.glClearColor(0.76f, 0.7f, 0.64f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle menu state
        if (isMenuActive) {
            batch.begin();
            gameMenu.render(batch);
            // Only show controls if we're not in instructions and not in first start
            if (!isInstructionsActive && !isFirstStart) {
                gameControls.render(batch);
            }
            batch.end();
            return;
        }

        // Handle instructions state
        if (isInstructionsActive) {
            instructions.render(batch);
            return;
        }

        // Handle game over or day complete state
        if (isDayComplete || isGameOver) {
            gameStatus.render(batch);
            return;
        }

        // Game is running - update game state
        if (!isPaused) {
            // Update day timer
            currentDayTime -= delta;

            // Check if day is over
            if (currentDayTime <= 0) {
                endDay();
                return;  // Return after ending day to prevent further updates
            }

            // Update customers
            if (customerHandler != null) {
                ArrayList<CustomerHandler.Customer> customersToRemove = new ArrayList<>();
                for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
                    if (customer.isSeated && customer.remainingPatienceTime <= 0) {
                        customersToRemove.add(customer);
                    }
                }
                // Remove timed out customers
                for (CustomerHandler.Customer customer : customersToRemove) {
                    handleCustomerTimeout(customer);
                }
            }
        }

        // Rest of your render code for the game state...
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Define foreground layers
        int[] foregroundIndices = {3, 4, 13, 14, 15, 27, 31, 32, 33};

        // Create an array for background layers
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

        // Render background layers first
        tiledMapRenderer.setView(camera);
        for (int index : backgroundArray) {
            tiledMapRenderer.render(new int[] {index});
        }

        batch.begin();
        // Render seats
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
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);
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

        // Render orders and patience
        if (orderHandling != null) {
            orderHandling.renderOrders(batch, UNIT_SCALE);
        }
        customerHandler.renderSpawnPatience(batch, delta);

        // Render inventory
        if (inventory != null) {
            inventory.render(batch);
        }
        batch.end();

        // Render foreground layers last
        for (int index : foregroundIndices) {
            tiledMapRenderer.render(new int[] {index});
        }

        // Handle character movement
        boolean moved = handleCharacterInput(delta);
        if (moved) {
            Machines.hideAllOptions(tiledMap, machinesList);
        }
        checkNearbyMachines();
        if (!moved) {
            currentAnimation = walkDown;
        }

        // Update customers
        customerHandler.update(delta);

        // Handle inventory interaction
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

        // Render game controls on top of game
        batch.begin();
        gameControls.render(batch);
        batch.end();

        // Draw UI elements if game is running
        if (!isMenuActive && !isInstructionsActive) {
            batch.begin();
            // Use screen coordinates for UI
            batch.setProjectionMatrix(batch.getProjectionMatrix().setToOrtho2D(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
            font.setColor(Color.BLACK);

            // Left side of inventory: Day and Time
            font.draw(batch, "Day: " + currentDay, INVENTORY_X - 250, INVENTORY_Y + 65);

            // Format time
            int minutes = (int)(currentDayTime / 60);
            int seconds = (int)(currentDayTime % 60);
            String timeText = String.format("Time: %02d:%02d", minutes, seconds);
            font.draw(batch, timeText, INVENTORY_X - 253, INVENTORY_Y + 40);

            // Right side of inventory: Earnings on two lines
            float inventoryEndX = INVENTORY_X + (32 * 8 * 1.5f);  // Right edge of inventory
            font.draw(batch, "Profit: " + currentEarnings,
                    inventoryEndX + 90, INVENTORY_Y + 65);
            font.draw(batch, "Goal: " + earningGoal,
                    inventoryEndX + 90, INVENTORY_Y + 40);
            batch.end();
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

        nearMachineType = Machines.checkForSpecificMachine(tiledMap, characterTileX, characterTileY, 1, "oven");
        if (nearMachineType != null) {
            nearMachine = true;
            return;
        }

        nearMachineType = Machines.checkForSpecificMachine(tiledMap, characterTileX, characterTileY, 2, "coffee_maker");
        if (nearMachineType != null) {
            nearMachine = true;
            return;
        }

        nearMachineType = Machines.checkForSpecificMachine(tiledMap, characterTileX, characterTileY, 2, "pastry");
        if (nearMachineType != null) {
            nearMachine = true;
            return;
        }
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
        if (font != null) font.dispose();
        if (gameStatus != null) gameStatus.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        // Escape key to toggle menu and pause - only works after game has started
        if (keycode == Input.Keys.ESCAPE && !isFirstStart) {
            isMenuActive = !isMenuActive;
            isPaused = isMenuActive;

            // Pause/resume all machines and customer spawning
            if (isPaused) {
                for (Machines.Machine machine : machinesList) {
                    machine.pauseProcess();
                }
                if (customerHandler != null) {
                    customerHandler.stopSpawning();
                    customerHandler.pauseAllCustomerTimers();
                }
            } else {
                for (Machines.Machine machine : machinesList) {
                    machine.resumeProcess();
                }
                if (customerHandler != null) {
                    customerHandler.resumeSpawning();
                    customerHandler.resumeAllCustomerTimers();
                }
            }
            return true;
        }

        if (isPaused) {
            return false;
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

        // Add handling for game over state
        if (isGameOver && keycode == Input.Keys.ENTER) {
            isGameOver = false;
            isMenuActive = true;
            isPaused = true;
            initializeGame(); // Reset game state
            return true;
        }

        return false;
    }

    // method to collect nearby orders
    private boolean collectNearbyOrder() {
        float tileSize = 16 * UNIT_SCALE;
        int characterTileX = (int)(characterPosition.x / tileSize);
        int characterTileY = (int)(characterPosition.y / tileSize);
        int radius = 2; // Back to original 2 tile radius for better usability

        for (Machines.Machine machine : machinesList) {
            if (machine.orderReady) {
                // Check if player is within radius of machine tiles
                String machineType = machine.machineType;
                for (int y = characterTileY - radius; y <= characterTileY + radius; y++) {
                    for (int x = characterTileX - radius; x <= characterTileX + radius; x++) {
                        String foundType = Machines.checkForSpecificMachine(tiledMap, x, y, radius, machineType);
                        if (foundType != null && foundType.equals(machineType)) {
                            // Try to collect the order
                            if (inventory.addOrder(machine.choice)) {
                                System.out.println("Collected " + machine.choice + " from " + machine.name);
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
        float interactionRange = tileSize * 2;

        for (CustomerHandler.Customer customer : customerHandler.getCustomers()) {
            if (!customer.isSeated) continue;

            float distanceX = Math.abs(characterPosition.x - customer.position.x);
            float distanceY = Math.abs(characterPosition.y - customer.position.y);

            if (distanceX <= interactionRange && distanceY <= interactionRange) {
                String customerOrder = orderHandling.getOrderForCustomer(customer);
                if (customerOrder != null && inventory.serveOrder(customerOrder)) {
                    // Calculate earnings based on customer patience
                    int earnings = calculateEarnings(customer);
                    currentEarnings += earnings;
                    System.out.println("Customer served! Earned " + earnings + " coins. Total earnings: " + currentEarnings);

                    // Complete the order and handle customer
                    orderHandling.completeOrder(customer);
                    if (customer.currentSeatId != -1) {
                        occupiedSeats.remove(customer.currentSeatId);
                    }
                    customerHandler.removeCustomer(customer);
                    customerHandler.customerServed();
                }
            }
        }
    }

    private int calculateEarnings(CustomerHandler.Customer customer) {
        float patienceRatio = customer.remainingPatienceTime / customer.maxPatienceTime;
        if (patienceRatio > 0.66f) {  // Top third - white
            return 50;
        } else if (patienceRatio > 0.33f) {  // Middle third - yellow
            return 30;
        } else {  // Bottom third - red
            return 10;
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
                return true;
            }
            if (!isInstructionsActive && !isFirstStart && gameControls.touchDown(screenX, screenY)) {
                return true;
            }
        }

        if (isInstructionsActive) {
            if (instructions.touchDown(screenX, screenY)) {
                return true;
            }
        }

        // Add check for game over or day complete state
        if (isGameOver || isDayComplete) {
            if (gameStatus.touchDown(screenX, screenY)) {
                return true;
            }
            return false;  // Return false if the click wasn't on a button
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
        Machines.handleOptionClick(tileX, tileY, machinesList, tiledMap);

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

    private GameMenu.MenuListener createMenuListener() {
        return new GameMenu.MenuListener() {
            @Override
            public void onStartGame() {
                if (!isFirstStart) {
                    // Dispose current game resources
                    disposeGameResources();
                    // Reinitialize game
                    initializeGame();
                    // Reinitialize sounds
                    Machines.initializeSounds();
                }
                isMenuActive = false;
                isPaused = false;
                isFirstStart = false;
                // Start customer spawning when game starts
                if (customerHandler != null) {
                    customerHandler.startSpawning();
                }
                // Make sure timers are running
                if (customerHandler != null) {
                    customerHandler.resumeAllCustomerTimers();
                }
            }

            @Override
            public void onResumeGame() {
                isMenuActive = false;
                isPaused = false;
                // Resume all machines
                for (Machines.Machine machine : machinesList) {
                    machine.resumeProcess();
                }
                // Resume customer spawning and timers
                if (customerHandler != null) {
                    customerHandler.resumeSpawning();
                    customerHandler.resumeAllCustomerTimers();
                }
            }

            @Override
            public void onExitGame() {
                Gdx.app.exit();
            }
        };
    }

    private GameControls.ControlsListener createControlsListener() {
        return new GameControls.ControlsListener() {
            @Override
            public void onShowInstructions() {
                if (!isInstructionsActive) {  // Only proceed if not already in instructions
                    isInstructionsActive = true;
                    isMenuActive = false;
                    isPaused = true;
                    // Pause all systems when showing instructions
                    for (Machines.Machine machine : machinesList) {
                        machine.pauseProcess();
                    }
                    if (customerHandler != null) {
                        customerHandler.stopSpawning();
                        customerHandler.pauseAllCustomerTimers();
                    }
                }
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
        };
    }

    private Instructions.InstructionListener createInstructionsListener() {
        return new Instructions.InstructionListener() {
            @Override
            public void onBackToGame() {
                isInstructionsActive = false;
                isMenuActive = true;
                // Keep the game paused since we're going back to main menu
                isPaused = true;
            }
        };
    }

    private GameStatus.StatusListener createStatusListener() {
        return new GameStatus.StatusListener() {
            @Override
            public void onStartGame() {
                // Stop and clean up existing systems first
                if (customerHandler != null) {
                    customerHandler.stopSpawning();
                    customerHandler.dispose();
                }
                
                // Reset game state
                disposeGameResources();
                initializeGame();
                Machines.initializeSounds();
                
                // Reset all game states
                isGameOver = false;
                isDayComplete = false;
                isPaused = false;
                isMenuActive = false;
                isInstructionsActive = false;
                currentDay = 1;
                currentEarnings = 0;
                earningGoal = 300;
                currentDayTime = dayTimer;
                
                // Initialize new customer handler and start spawning
                customerHandler = new CustomerHandler(orderHandling);
                if (customerHandler != null) {
                    customerHandler.startSpawning();
                    customerHandler.resumeAllCustomerTimers();
                }
                
                // Initialize and resume all machines
                if (machinesList != null) {
                    for (Machines.Machine machine : machinesList) {
                        machine.isBusy = false;
                        machine.orderReady = false;
                        machine.resumeProcess();
                    }
                }
            }

            @Override
            public void onResumeGame() {
                isPaused = false;
                isDayComplete = false;
                nextDay();
            }
            
            @Override
            public void onBackToMenu() {
                // Stop and clean up existing systems first
                if (customerHandler != null) {
                    customerHandler.stopSpawning();
                    customerHandler.dispose();
                }
                
                // Reset game state
                disposeGameResources();
                initializeGame();
                
                // Reset to initial menu state
                isGameOver = false;
                isDayComplete = false;
                isMenuActive = true;  // Make sure menu is active
                isPaused = true;
                isFirstStart = true;  // Reset to first start to hide Resume button
                isInstructionsActive = false;  // Make sure instructions are not active
                currentDay = 1;
                currentEarnings = 0;
                earningGoal = 300;
                currentDayTime = dayTimer;
                
                // Initialize new systems but keep them paused
                customerHandler = new CustomerHandler(orderHandling);
                
                // Initialize machines in paused state
                if (machinesList != null) {
                    for (Machines.Machine machine : machinesList) {
                        machine.isBusy = false;
                        machine.orderReady = false;
                        machine.pauseProcess();
                    }
                }
                
                occupiedSeats.clear();

                // Make sure game menu is initialized and reset its state
                if (gameMenu != null) {
                    gameMenu.dispose();
                }
                gameMenu = new GameMenu(createMenuListener());
            }
        };
    }

    private void endDay() {
        if (currentEarnings >= earningGoal) {
            isDayComplete = true;
            isPaused = true;

            if (gameStatus != null) {
                gameStatus.setGameOver(false);
                gameStatus.reloadAssets();
            }
            currentDayTime = 0;

            // Pause all systems
            for (Machines.Machine machine : machinesList) {
                machine.pauseProcess();
            }
            if (customerHandler != null) {
                customerHandler.stopSpawning();
                customerHandler.pauseAllCustomerTimers();
            }
            // Remove automatic progression - let the menu handle it

        } else {
            isGameOver = true;
            isPaused = true;

            if (gameStatus != null) {
                gameStatus.setGameOver(true);
                gameStatus.reloadAssets();
            }
            currentDayTime = 0;

            if (customerHandler != null) {
                customerHandler.stopSpawning();
                customerHandler.pauseAllCustomerTimers();
            }
        }
    }

    private void nextDay() {
        // Save current day and goal
        currentDay++;
        int earningGoalIncrement = 50;
        earningGoal += earningGoalIncrement;  // Increment by 50
        
        // Stop all current systems
        if (customerHandler != null) {
            customerHandler.stopSpawning();
            customerHandler.dispose();
        }
        
        // Reset game resources but keep day number and goal
        disposeGameResources();
        initializeGame(true);  // Pass true to indicate this is called from nextDay
        
        // Reset earnings for new day
        currentEarnings = 0;
        currentDayTime = dayTimer;
        
        // Clear inventory
        if (inventory != null) {
            for (int i = 0; i < 8; i++) {  // 8 is MAX_SLOTS in Inventory
                inventory.removeOrder(i);
            }
        }

        // Reset character position
        characterPosition.set(757, 512);
        currentAnimation = walkDown;

        // Reset all machines
        if (machinesList != null) {
            for (Machines.Machine machine : machinesList) {
                machine.isBusy = false;
                machine.orderReady = false;
                machine.resumeProcess();
            }
        }

        // Clear all seats and orders
        occupiedSeats.clear();
        
        // Initialize new customer handler and start spawning
        customerHandler = new CustomerHandler(orderHandling);
        customerHandler.startSpawning();
        
        isDayComplete = false;
        isPaused = false;

        System.out.println("Starting Day " + currentDay + " with earning goal: " + earningGoal);
    }

    // When a customer's patience runs out, make sure to free their seat
    private void handleCustomerTimeout(CustomerHandler.Customer customer) {
        if (customer.currentSeatId != -1) {
            // Remove from occupied seats
            occupiedSeats.remove(customer.currentSeatId);

            // Remove their order and display
            orderHandling.completeOrder(customer);

            // Make sure to remove from the customer handler's list
            customerHandler.removeCustomer(customer);
        }
    }
}
