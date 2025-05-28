package com.caferush.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CustomerHandler {
    public Array<Customer> customers; 
    private Array<Texture> characterSprites;    
    private Array<TextureRegion> customerSprites;


    public CustomerHandler() {
        customers = new Array<>();
        characterSprites = new Array<>();
        customerSprites = new Array<>();
        loadTextures();   
    } 
        
    public Array<Customer> getCustomers() {
        return customers;

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
        Customer customer = new Customer();
        customer.position.set(x, y);
        
        // Assign a random sprite
        if (customerSprites.size > 0) {
            customer.sprite = customerSprites.random();
        }
        
        customers.add(customer);
    }

    public class Customer {
        public Vector2 position = new Vector2();
        public Vector2 offset = new Vector2();
        public TextureRegion sprite;
        public boolean beingDragged;
        public boolean isSeated;
    }

    public void dispose() {
        for (Texture texture : characterSprites) {
            texture.dispose();
        }
    }
}