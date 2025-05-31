package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CustomerHandler {
    public Array<Customer> customers;
    private final Array<Texture> characterSprites;
    private final Array<TextureRegion> customerSprites;
    private final OrderHandling orderHandling;

    // Spawn point patience bubble textures
    private Texture spawnBubbleNormal;
    private Texture spawnBubbleModerate;
    private Texture spawnBubbleMinimal;

    private final CustomerSpawner spawnerThread;
    private volatile boolean isRunning = false;
    private final Object customersLock = new Object();

    private float minSpawnDelay = 3.0f; // minimum seconds between spawns
    private float maxSpawnDelay = 8.0f; // maximum seconds between spawns
    private int maxCustomers = 9; // maximum customers (8 seated + 1 waiting)
    private static final float SPAWN_RADIUS = 30f;
    public float spawnX = 800;
    public float spawnY = 210;
    private volatile boolean canSpawnNewCustomer = true;

    private static Sound angryMeow;
    private static Sound pop;

    public CustomerHandler(OrderHandling orderHandling) {
        this.orderHandling = orderHandling;
        customers = new Array<>();
        characterSprites = new Array<>();
        customerSprites = new Array<>();
        loadTextures();
        loadSounds();

        spawnerThread = new CustomerSpawner();
        // Don't start the thread immediately
        isRunning = false;
    }

    private void loadSounds() {
        try {
            if (angryMeow == null) {
                angryMeow = Gdx.audio.newSound(Gdx.files.internal("sfx/angry-meow.mp3"));
            }
            if (pop == null) {
                pop = Gdx.audio.newSound(Gdx.files.internal("sfx/pop-39222.mp3"));
            }
        } catch (Exception e) {
            System.err.println("Error loading customer sounds: " + e.getMessage());
        }
    }

    public Array<Customer> getCustomers() {
        synchronized(customersLock) {
            return customers;
        }
    }

    private void loadTextures() {
        Texture catBlackSheet = new Texture("pngs/cat-black-front.png");
        Texture catOrangeSheet = new Texture("pngs/cat-orange-front.png");
        Texture boss1Sheet = new Texture("pngs/cat-office-brown-front-idle.png");
        Texture boss2Sheet = new Texture("pngs/cat-office-brown2-front-idle.png");
        Texture boss3Sheet = new Texture("pngs/cat-office-white-front-idle.png");

        characterSprites.add(catBlackSheet);
        characterSprites.add(catOrangeSheet);
        characterSprites.add(boss1Sheet);
        characterSprites.add(boss2Sheet);
        characterSprites.add(boss3Sheet);

        int spriteWidth = 16;
        int spriteHeight = 16;

        customerSprites.add(new TextureRegion(catBlackSheet, 0, 0, spriteWidth, spriteHeight));
        customerSprites.add(new TextureRegion(catOrangeSheet, 0, 0, spriteWidth, spriteHeight));
        customerSprites.add(new TextureRegion(boss1Sheet, 0, 0, spriteWidth, spriteHeight));
        customerSprites.add(new TextureRegion(boss2Sheet, 0, 0, spriteWidth, spriteHeight));
        customerSprites.add(new TextureRegion(boss3Sheet, 0, 0, spriteWidth, spriteHeight));

        // Load spawn patience bubble textures
        loadSpawnBubbleTextures();
    }

    private void loadSpawnBubbleTextures() {
        spawnBubbleNormal = new Texture("pngs/waiting-normal.png");
        spawnBubbleModerate = new Texture("pngs/waiting-moderate.png");
        spawnBubbleMinimal = new Texture("pngs/waiting-minimal.png");
    }

    public void addCustomer(float x, float y) {
        synchronized(customersLock) {
            Customer customer;
            TextureRegion chosenSprite = null;
            if (customerSprites.size > 0) {
                chosenSprite = customerSprites.random();
            }
            // If boss sprite, assign shorter patience times
            if (chosenSprite != null && isBossSprite(chosenSprite)) {
                customer = new Customer(15f, 30f); // Boss patience times
            } else {
                customer = new Customer(); // Regular customer
            }
            customer.position.set(x, y);
            customer.sprite = chosenSprite;
            // Initialize patience timers and values before adding to list
            customer.remainingPatienceTime = customer.getWaitingforSeatTime();
            customer.maxPatienceTime = customer.getWaitingforSeatTime();
            customer.remainingWaitingforSeatTime = customer.getWaitingforSeatTime();
            customer.maxWaitingforSeatTime = customer.getWaitingforSeatTime();

            customers.add(customer);
            System.out.println("New customer arrived!");

            // Start spawn patience timer (not seated yet)
            customer.startWaitingforSeatTimer();
        }
    }

    private boolean isBossSprite(TextureRegion sprite) {
        for (int i = 2; i <= 4; i++) {
            if (customerSprites.get(i) == sprite) {
                return true;
            }
        }
        return false;
    }

    public void removeCustomer(Customer customer) {
        synchronized(customersLock) {
            customer.stopAllTimers();
            customers.removeValue(customer, true);

            // Clear the occupied seat if customer was seated
            if (customer.currentSeatId != -1) {
                System.out.println("Customer left from seat " + customer.currentSeatId);
                customer.currentSeatId = -1;
            }

            if (orderHandling != null) {
                orderHandling.removeOrderByCustomer(customer);
            }

            // Enable spawning when a customer is removed
            if (customers.size < maxCustomers) {
                canSpawnNewCustomer = true;
            }
        }
    }

    public void seatCustomer(Customer customer) {
        if (customer != null && !customer.isSeated) {
            customer.isSeated = true;
            customer.stopWaitingforSeatTimer(); // Stop spawn timer
            
            // Initialize order timer values
            customer.remainingWaitingforOrderTime = customer.getWaitingforOrderTime();
            customer.maxWaitingforOrderTime = customer.getWaitingforOrderTime();
            
            System.out.println("Customer seated and ready to order!");
            // Start the order timer
            customer.startWaitingforOrderTimer();
        }
    }

    public void update(float deltaTime) {
        synchronized(customersLock) {
            for (int i = customers.size - 1; i >= 0; i--) {
                Customer customer = customers.get(i);
                if (customer.hasLostPatience()) {
                    removeCustomer(customer);
                    long soundAngryMeow = angryMeow.play();
                    if (customer.isSeated) {
                        angryMeow.setVolume(soundAngryMeow, 0.2f);
                    } else {
                        angryMeow.setVolume(soundAngryMeow, 0.2f);
                    }
                }
            }
        }
    }

    // Method to render spawn point patience bubbles
    public void renderSpawnPatience(SpriteBatch batch, float UNIT_SCALE) {
    synchronized(customersLock) {
        for (int i = 0; i < customers.size; i++) {
            Customer customer = customers.get(i);
            if (!customer.isSeated) {
                renderCustomerSpawnBubble(batch, customer, UNIT_SCALE);
            }
        }
    }
}

    private void renderCustomerSpawnBubble(SpriteBatch batch, Customer customer, float UNIT_SCALE) {
        float bubbleScale = 1.5f;
        float bubbleOffsetX = 70f;
        float bubbleOffsetY = -290f;

        float bubbleX = (customer.position.x + bubbleOffsetX);
        float bubbleY = (customer.position.y + bubbleOffsetY);

        Texture bubbleToDraw = getSpawnPatienceBubble(customer);

        if (bubbleToDraw != null) {
            float scaledWidth = bubbleToDraw.getWidth() * bubbleScale;
            float scaledHeight = bubbleToDraw.getHeight() * bubbleScale;

            batch.draw(bubbleToDraw, bubbleX - scaledWidth/2f, bubbleY, scaledWidth, scaledHeight);
        }
    }

    private Texture getSpawnPatienceBubble(Customer customer) {
        if (customer.maxPatienceTime > 0) {
            float patienceRatio = customer.remainingPatienceTime / customer.maxPatienceTime;

            if (patienceRatio < 0.30f) {
                return spawnBubbleMinimal;
            } else if (patienceRatio < 0.50f) {
                return spawnBubbleModerate;
            } else {
                return spawnBubbleNormal;
            }
        }
        return spawnBubbleNormal;
    }

    private boolean isSpawnPointClear(float x, float y) {
    for (int i = 0; i < customers.size; i++) {
        Customer customer = customers.get(i);
        if (customer.position.dst(x, y) < SPAWN_RADIUS) {
            return false;
        }
    }
    return true;
}

    private class CustomerSpawner extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Only proceed if spawning is enabled
                    if (!isRunning) {
                        Thread.sleep(100); // Short sleep to prevent busy waiting
                        continue;
                    }

                    // Random delay between spawns
                    float delay = MathUtils.random(minSpawnDelay, maxSpawnDelay);
                    Thread.sleep((long)(delay * 1000));

                    synchronized(customersLock) {
                        // Only spawn if:
                        // 1. Below max customers
                        // 2. Spawn point is clear
                        // 3. We're allowed to spawn new customers
                        // 4. Spawning is still enabled
                        if (customers.size < maxCustomers && 
                            isSpawnPointClear(spawnX, spawnY) && 
                            canSpawnNewCustomer && 
                            isRunning) {

                            long soundPop = pop.play();
                            pop.setVolume(soundPop, 0.8f);
                            addCustomer(spawnX, spawnY);
                            
                            // If we've reached max customers, stop spawning until a customer is served
                            if (customers.size >= maxCustomers) {
                                canSpawnNewCustomer = false;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private class CustomerPatienceTimer extends Thread {
        private Customer customer;
        private volatile boolean timerRunning = true;
        private volatile boolean isPaused = false;
        private float patienceTime;
        private String timerType; // "spawn" or "seated"

        public CustomerPatienceTimer(Customer customer, float patienceTime, String timerType) {
            this.customer = customer;
            this.patienceTime = patienceTime;
            this.timerType = timerType;
        }

        @Override
        public void run() {
            try {
                while (timerRunning && patienceTime > 0) {
                    if (!isPaused) {
                        Thread.sleep(100); // Update every 100ms
                        patienceTime -= 0.1f;
                        
                        // Update the appropriate timer based on type
                        if (timerType.equals("spawn")) {
                            customer.remainingWaitingforSeatTime = patienceTime;
                            customer.remainingPatienceTime = patienceTime;
                        } else if (timerType.equals("seated")) {
                            customer.remainingWaitingforOrderTime = patienceTime;
                            customer.remainingPatienceTime = patienceTime;
                        }

                        // Check if patience is lost
                        if (patienceTime <= 0) {
                            customer.losePatience();
                            break;
                        }
                    } else {
                        Thread.sleep(100); // Keep checking pause state
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void pauseTimer() {
            isPaused = true;
        }

        public void resumeTimer() {
            isPaused = false;
        }

        public void stopTimer() {
            timerRunning = false;
            interrupt();
        }
    }

    public class Customer {
        public Vector2 position = new Vector2();
        public Vector2 offset = new Vector2();
        public TextureRegion sprite;
        public boolean beingDragged;
        public boolean isSeated;
        public int currentSeatId = -1; // Track which seat the customer is occupying

        public float maxPatienceTime;
        public float remainingPatienceTime;

        public float maxWaitingforSeatTime;
        public float remainingWaitingforSeatTime;

        public volatile float remainingWaitingforOrderTime;
        public volatile float maxWaitingforOrderTime;


        // Patience system with separate timers
        private float patienceAtSpawn;   // waits 30 seconds at spawn point
        private float patienceAtSeat;    // waits 60 seconds after seated
        private CustomerPatienceTimer spawnPatienceTimer;
        private CustomerPatienceTimer seatedPatienceTimer;
        private volatile boolean hasLostPatience = false;

        public Customer() {
            this.isSeated = false;
            this.beingDragged = false;
            this.hasLostPatience = false;
            // Initialize patience values
            this.patienceAtSpawn = 30f;   // waits 30 seconds at spawn point
            this.patienceAtSeat = 60f;    // waits 60 seconds after seated
            this.remainingPatienceTime = patienceAtSpawn;
            this.maxPatienceTime = patienceAtSpawn;
            this.remainingWaitingforSeatTime = patienceAtSpawn;
            this.maxWaitingforSeatTime = patienceAtSpawn;
        }

        public Customer(float patienceAtSpawn, float patienceAtSeat) {
            this.isSeated = false;
            this.beingDragged = false;
            this.hasLostPatience = false;
            this.patienceAtSpawn = patienceAtSpawn;   // Custom patience time for spawn
            this.patienceAtSeat = patienceAtSeat;     // Custom patience time for seated
            this.remainingPatienceTime = patienceAtSpawn;
            this.maxPatienceTime = patienceAtSpawn;
            this.remainingWaitingforSeatTime = patienceAtSpawn;
            this.maxWaitingforSeatTime = patienceAtSpawn;
        }



        public void startWaitingforSeatTimer() {
            if (spawnPatienceTimer == null) {
                // Make sure patience values are properly set
                this.remainingPatienceTime = patienceAtSpawn;
                this.maxPatienceTime = patienceAtSpawn;
                this.remainingWaitingforSeatTime = patienceAtSpawn;
                this.maxWaitingforSeatTime = patienceAtSpawn;

                spawnPatienceTimer = new CustomerPatienceTimer(this, patienceAtSpawn, "spawn");
                spawnPatienceTimer.start();
            }
        }

        public void stopWaitingforSeatTimer() {
            if (spawnPatienceTimer != null) {
                spawnPatienceTimer.stopTimer();
                spawnPatienceTimer = null;
            }
        }

        public void startWaitingforOrderTimer() {
            if (seatedPatienceTimer != null) {
                seatedPatienceTimer.stopTimer();
            }
            
            // Initialize timer values if not already set
            if (maxWaitingforOrderTime <= 0) {
                maxWaitingforOrderTime = getWaitingforOrderTime();
                remainingWaitingforOrderTime = maxWaitingforOrderTime;
            }
            
            seatedPatienceTimer = new CustomerPatienceTimer(this, remainingWaitingforOrderTime, "seated");
            seatedPatienceTimer.start();
        }

        public void stopWaitingforOrderTimer() {
            if (seatedPatienceTimer != null) {
                seatedPatienceTimer.stopTimer();
                seatedPatienceTimer = null;
            }
        }

        public void stopAllTimers() {
            stopWaitingforOrderTimer();
            stopWaitingforSeatTimer();
        }

        public void losePatience() {
            hasLostPatience = true;
        }

        public boolean hasLostPatience() {
            return hasLostPatience;
        }

        public float getWaitingforSeatTime() {
            return patienceAtSpawn;
        }

        public float getWaitingforOrderTime() {
            return patienceAtSeat;
        }

        public void setWaitingforSeatTime(float time) {
            this.patienceAtSpawn = time;
        }

        public void setWaitingforOrderTime(float time) {
            this.patienceAtSeat = time;
        }

        // Helper methods to check timer state
        public boolean isInSpawnPhase() {
            return !isSeated && spawnPatienceTimer != null;
        }

        public boolean isInSeatedPhase() {
            return isSeated && seatedPatienceTimer != null;
        }

        public String getCurrentPhase() {
            if (isInSeatedPhase()) return "seated";
            if (isInSpawnPhase()) return "spawn";
            return "none";
        }

        public void pauseAllTimers() {
            if (spawnPatienceTimer != null) {
                spawnPatienceTimer.pauseTimer();
            }
            if (seatedPatienceTimer != null) {
                seatedPatienceTimer.pauseTimer();
            }
        }

        public void resumeAllTimers() {
            if (spawnPatienceTimer != null) {
                spawnPatienceTimer.resumeTimer();
            }
            if (seatedPatienceTimer != null) {
                seatedPatienceTimer.resumeTimer();
            }
        }
    }

    public void setSpawnDelay(float min, float max) {
        this.minSpawnDelay = min;
        this.maxSpawnDelay = max;
    }

    public void setMaxCustomers(int max) {
        this.maxCustomers = max;
    }

    public void dispose() {
        isRunning = false;

        if (spawnerThread != null) {
            spawnerThread.interrupt();
            try {
                spawnerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized(customersLock) {
            for (Customer customer : customers) {
                customer.stopAllTimers();
            }
        }

        for (Texture texture : characterSprites) {
            texture.dispose();
        }

        // Dispose spawn bubble textures
        if (spawnBubbleNormal != null) spawnBubbleNormal.dispose();
        if (spawnBubbleModerate != null) spawnBubbleModerate.dispose();
        if (spawnBubbleMinimal != null) spawnBubbleMinimal.dispose();
        
        // Dispose sounds
        if (angryMeow != null) {
            angryMeow.dispose();
            angryMeow = null;
        }
        if (pop != null) {
            pop.dispose();
            pop = null;
        }
    }

    public void customerServed() {
        // Allow spawning of new customers when one is served
        canSpawnNewCustomer = true;
    }

    // Add methods to control spawning
    public void startSpawning() {
        synchronized(customersLock) {
            // Clear any existing customers when starting fresh
            customers.clear();
            canSpawnNewCustomer = true;
        }
        isRunning = true;
        if (!spawnerThread.isAlive()) {
            spawnerThread.start();
        }
    }

    public void stopSpawning() {
        isRunning = false;
    }

    public void resumeSpawning() {
        synchronized(customersLock) {
            canSpawnNewCustomer = true;
        }
        isRunning = true;
    }

    public void pauseAllCustomerTimers() {
        synchronized(customersLock) {
            for (Customer customer : customers) {
                customer.pauseAllTimers();
            }
        }
    }

    public void resumeAllCustomerTimers() {
        synchronized(customersLock) {
            for (Customer customer : customers) {
                customer.resumeAllTimers();
            }
        }
    }
}