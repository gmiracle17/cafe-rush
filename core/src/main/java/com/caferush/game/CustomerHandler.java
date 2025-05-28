package com.caferush.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CustomerHandler {
    public Array<Customer> customers; 
    private Array<Texture> characterSprites;    
    private Array<TextureRegion> customerSprites;
    private OrderHandling orderHandling;

    private CustomerSpawner spawnerThread;
    private volatile boolean isRunning = true;
    private final Object customersLock = new Object();
    
    private float minSpawnDelay = 3.0f; // minimum seconds between spawns
    private float maxSpawnDelay = 8.0f; // maximum seconds between spawns
    private int maxCustomers = 5; // maximum customers at once

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
            
            // Start patience timer for this customer
            customer.startPatienceTimer();
        }
    }

    public void removeCustomer(Customer customer) {
        synchronized(customersLock) {
            customer.stopPatienceTimer();
            customers.removeValue(customer, true);
        
        if (orderHandling != null) {
            orderHandling.removeOrderByCustomer(customer);
        }
        }
    }
    
    // Update method to be called from your game loop
    public void update(float deltaTime) {
        synchronized(customersLock) {
            // Check for customers whose patience has run out
            for (int i = customers.size - 1; i >= 0; i--) {
                Customer customer = customers.get(i);
                if (customer.hasLostPatience()) {
                    removeCustomer(customer);
                    System.out.println("Patience timer ran out!");
                }
            }
        }
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
                    
                    if (shouldSpawn) {
                        addCustomer(800, 210);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Customer Patience Timer Thread
    private class CustomerPatienceTimer extends Thread {
        private Customer customer;
        private volatile boolean timerRunning = true;
        private float patienceTime;
        
        public CustomerPatienceTimer(Customer customer) {
            this.customer = customer;
            this.patienceTime = 10.0f; 
            customer.maxPatienceTime = patienceTime;
            customer.remainingPatienceTime = patienceTime;
        }

        @Override
        public void run() {
            float updateInterval = 0.1f;
            while (timerRunning && customer.remainingPatienceTime > 0) {
                try {
                    Thread.sleep((long)(updateInterval * 1000));
                    customer.remainingPatienceTime -= updateInterval;
                    
                    // Debug output to see patience decreasing
                    if ((int)(customer.remainingPatienceTime * 10) % 10 == 0) {
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (timerRunning && customer.remainingPatienceTime <= 0) {
                customer.losePatience();
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

        // Patience system
        private CustomerPatienceTimer patienceTimer;
        private volatile boolean hasLostPatience = false;

        public Customer() {
            this.isSeated = false;
            this.beingDragged = false;
        }
        
        public void startPatienceTimer() {
            if (patienceTimer == null) {
                patienceTimer = new CustomerPatienceTimer(this);
                patienceTimer.start();
            }
        }
        
        public void stopPatienceTimer() {
            if (patienceTimer != null) {
                patienceTimer.stopTimer();
                patienceTimer = null;
            }
        }
        
        public void losePatience() {
            hasLostPatience = true;
        }
        
        public boolean hasLostPatience() {
            return hasLostPatience;
        }
    }
    
    // Configuration methods
    public void setSpawnDelay(float min, float max) {
        this.minSpawnDelay = min;
        this.maxSpawnDelay = max;
    }
    
    public void setMaxCustomers(int max) {
        this.maxCustomers = max;
    }

    public void dispose() {
        // Stop all threads
        isRunning = false;
        
        if (spawnerThread != null) {
            spawnerThread.interrupt();
            try {
                spawnerThread.join(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop all customer patience timers
        synchronized(customersLock) {
            for (Customer customer : customers) {
                customer.stopPatienceTimer();
            }
        }
        
        // Dispose textures
        for (Texture texture : characterSprites) {
            texture.dispose();
        }
    }
}