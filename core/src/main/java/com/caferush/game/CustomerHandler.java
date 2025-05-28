package com.caferush.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CustomerHandler {
    public Array<Customer> customers; 
    private Array<Texture> characterSprites;    
    private Array<TextureRegion> customerSprites;
    private OrderHandling orderHandling;
    
    // Spawn point patience bubble textures
    private Texture spawnBubbleNormal;
    private Texture spawnBubbleModerate; 
    private Texture spawnBubbleMinimal;

    private CustomerSpawner spawnerThread;
    private volatile boolean isRunning = true;
    private final Object customersLock = new Object();
    
    private float minSpawnDelay = 3.0f; // minimum seconds between spawns
    private float maxSpawnDelay = 8.0f; // maximum seconds between spawns
    private int maxCustomers = 5; // maximum customers at once
    private static final float SPAWN_RADIUS = 30f;
    public float spawnX = 800; 
    public float spawnY = 210; 

    public CustomerHandler(OrderHandling orderHandling) {
        this.orderHandling = orderHandling;
        customers = new Array<>();
        characterSprites = new Array<>();
        customerSprites = new Array<>();
        loadTextures();
        
        spawnerThread = new CustomerSpawner();
        spawnerThread.start();
    } 
        
    public Array<Customer> getCustomers() {
        synchronized(customersLock) {
            return customers;
        }
    }
    
    private void loadTextures() {
        Texture catBlackSheet = new Texture("pngs/cat-black-front.png");
        Texture catOrangeSheet = new Texture("pngs/cat-orange-front.png");

        characterSprites.add(catBlackSheet);
        characterSprites.add(catOrangeSheet);

        int spriteWidth = 16;  
        int spriteHeight = 16; 

        customerSprites.add(new TextureRegion(catBlackSheet, 0*16, 0*16, spriteWidth, spriteHeight));
        customerSprites.add(new TextureRegion(catOrangeSheet, 0*16, 0*16, spriteWidth, spriteHeight));
        
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
            Customer customer = new Customer();
            customer.position.set(x, y);
            
            // Assign a random sprite
            if (customerSprites.size > 0) {
                customer.sprite = customerSprites.random();
            }
            
            customers.add(customer);
            
            // Start spawn patience timer (not seated yet)
            customer.startWaitingforSeatTimer();
        }
    }

    public void removeCustomer(Customer customer) {
        synchronized(customersLock) {
            customer.stopAllTimers();
            customers.removeValue(customer, true);
        
            if (orderHandling != null) {
                orderHandling.removeOrderByCustomer(customer);
            }
        }
    }
    
    public void seatCustomer(Customer customer) {
        if (customer != null && !customer.isSeated) {
            customer.isSeated = true;
            customer.stopWaitingforSeatTimer();// Stop spawn timer
            customer.startWaitingforOrderTimer(); // Start seated timer
            System.out.println("Customer seated - switching to order patience timer");
        }
    }
    
    public void update(float deltaTime) {
        synchronized(customersLock) {
            for (int i = customers.size - 1; i >= 0; i--) {
                Customer customer = customers.get(i);
                if (customer.hasLostPatience()) {
                    removeCustomer(customer);
                    if (customer.isSeated) {
                        System.out.println("Seated customer lost patience waiting for order!");
                    } else {
                        System.out.println("Customer lost patience waiting to be seated!");
                    }
                }
            }
        }
    }
    
    // Method to render spawn point patience bubbles
    public void renderSpawnPatience(SpriteBatch batch, float UNIT_SCALE) {
        synchronized(customersLock) {
            for (Customer customer : customers) {
                if (!customer.isSeated && customer.isInSpawnPhase()) {
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
        for (Customer customer : customers) {
            if (customer.position.dst(x, y) < SPAWN_RADIUS) {
                return false; 
            }
        }
        return true;
    }

    private class CustomerSpawner extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    // Random delay between spawns
                    float delay = MathUtils.random(minSpawnDelay, maxSpawnDelay);
                    Thread.sleep((long)(delay * 1000));
                    
                    // Check if we should spawn a new customer
                    boolean shouldSpawn = false;
                    synchronized(customersLock) {
                        shouldSpawn = (customers.size < maxCustomers && isRunning);
                    }

                    float spawnX = 800; 
                    float spawnY = 210; 
                    
                    if (shouldSpawn && isSpawnPointClear(spawnX, spawnY)) {
                        addCustomer(spawnX, spawnY);
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
    private float patienceTime;
    private String timerType; // "spawn" or "seated"
    
    public CustomerPatienceTimer(Customer customer, float patienceTime, String timerType) {
        this.customer = customer;
        this.patienceTime = patienceTime;
        this.timerType = timerType;
    }

    @Override
    public void run() {
        float updateInterval = 0.1f;
        
        // Update the appropriate timer based on type
        while (timerRunning) {
            try {
                Thread.sleep((long)(updateInterval * 1000));
                
                if (timerType.equals("spawn")) {
                    customer.remainingWaitingforSeatTime -= updateInterval;
                    customer.remainingPatienceTime = customer.remainingWaitingforSeatTime;
                    
                    if (customer.remainingWaitingforSeatTime <= 0) {
                        customer.losePatience();
                        break;
                    }
                } else if (timerType.equals("seated")) {
                    synchronized(customer) {
                        customer.remainingWaitingforOrderTime -= updateInterval;
                        customer.remainingPatienceTime = customer.remainingWaitingforOrderTime;
                    }
                    
                    if (customer.remainingWaitingforOrderTime <= 0) {
                        customer.losePatience();
                        break;
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopTimer() {
        timerRunning = false;
        this.interrupt();
    }
}

    public class Customer {
        public Vector2 position = new Vector2();
        public Vector2 offset = new Vector2();
        public TextureRegion sprite; 
        public boolean beingDragged;
        public boolean isSeated; 

        public float maxPatienceTime;
        public float remainingPatienceTime;

        public float maxWaitingforSeatTime;
        public float remainingWaitingforSeatTime;

        public volatile float remainingWaitingforOrderTime;
        public volatile float maxWaitingforOrderTime;


        // Patience system with separate timers
        private float patienceAtSpawn = 30f;   // waits 30 seconds at spawn point
        private float patienceAtSeat = 60f;    // waits 60 seconds after seated
        private CustomerPatienceTimer spawnPatienceTimer;
        private CustomerPatienceTimer seatedPatienceTimer;
        private volatile boolean hasLostPatience = false;

        public Customer() {
            this.isSeated = false;
            this.beingDragged = false;
        }
        
        public void startWaitingforSeatTimer() {
            if (spawnPatienceTimer == null) {
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
            if (seatedPatienceTimer == null) {
                // Set the variables that the timer and bubble rendering use
                this.remainingPatienceTime = patienceAtSeat;
                this.maxPatienceTime = patienceAtSeat;
                
                // Also set the specific waiting variables for consistency
                this.remainingWaitingforOrderTime = patienceAtSeat;
                this.maxWaitingforOrderTime = patienceAtSeat;

                seatedPatienceTimer = new CustomerPatienceTimer(this, patienceAtSeat, "seated");
                seatedPatienceTimer.start();
            }
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
    }
}