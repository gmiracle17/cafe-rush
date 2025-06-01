package com.caferush.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import java.util.ArrayList;

public class Inventory {
    private static final int MAX_SLOTS = 8; // Maximum number of items that can be stored
    private Array<InventorySlot> slots;
    private final Texture menuSheet;
    private final TextureRegion[] orderImages;
    private final int[][] menuItems;
    private TiledMap map;
    private String inventoryLayer; // Name of your inventory layer in Tiled
    private int[] inventoryTileX; // X positions of inventory slots
    private int[] inventoryTileY; // Y positions of inventory slots

    // Track dragging state
    private boolean isDragging = false;
    private int draggedSlotIndex = -1;
    private float draggedItemX;
    private float draggedItemY;

    public Inventory(OrderHandling orderHandling, TiledMap map, String inventoryLayer) {
        this.map = map;
        this.inventoryLayer = inventoryLayer;
        this.slots = new Array<>(MAX_SLOTS);
        this.menuItems = orderHandling.getMenuItems();
        this.menuSheet = new Texture("images/Bar katto 2.0 icon sheet.png");
        
        // Initialize empty slots
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots.add(new InventorySlot());
        }

        // Initialize order images from the menu sheet
        orderImages = new TextureRegion[menuItems.length];
        for (int i = 0; i < menuItems.length; i++) {
            int[] item = menuItems[i];
            orderImages[i] = new TextureRegion(menuSheet, item[0], item[1], item[2], item[3]);
        }

        // Initialize inventory tile positions
        inventoryTileX = new int[MAX_SLOTS];
        inventoryTileY = new int[MAX_SLOTS];
        findInventoryTilePositions();
    }

    private void findInventoryTilePositions() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(inventoryLayer);
        if (layer == null) {
            System.out.println("Warning: Inventory layer not found!");
            return;
        }

        // Fixed positions for the 8 inventory slots
        int[] fixedX = {4, 5, 6, 7, 8, 9, 10, 11};
        int[] fixedY = {1, 1, 1, 1, 1, 1, 1, 1};

        // Assign the fixed positions
        for (int i = 0; i < MAX_SLOTS; i++) {
            inventoryTileX[i] = fixedX[i];
            inventoryTileY[i] = fixedY[i];

            Cell cell = layer.getCell(fixedX[i], fixedY[i]);
            if (cell == null) {
                System.out.println("Warning: No tile found at inventory position: " + fixedX[i] + ", " + fixedY[i]);
            }
        }
    }

    // Helper method to get menu index for an order type
    private int getMenuIndexForOrder(String orderType) {
        switch (orderType.toLowerCase()) {
            case "hot_choco": return 0;
            case "espresso": return 1;
            case "americano": return 2;
            case "bread": return 3;
            case "croissant": return 4;
            case "donut": return 5;
            case "shortcake": return 6;
            case "cupcake": return 7;
            case "crinkles": return 8;
            case "biscuit": return 9;
            default:
                // Only print warning for unknown order types
                System.out.println("Warning: Unknown order type: " + orderType);
                return -1;
        }
    }

    // Add an order to the inventory
    public boolean addOrder(String orderType) {
        int menuIndex = getMenuIndexForOrder(orderType);
        if (menuIndex == -1) {
            return false;
        }
        
        // Find first empty slot
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (!slots.get(i).isOccupied()) {
                slots.get(i).setOrder(orderType);
                return true;
            }
        }
        return false; // Inventory is full
    }

    // Remove an order from a specific slot
    public String removeOrder(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < slots.size && slots.get(slotIndex).isOccupied()) {
            String order = slots.get(slotIndex).getOrder();
            slots.get(slotIndex).clear(); // This only clears the order data, not the box
            return order;
        }
        return null;
    }

    // Check if inventory has a specific order
    public boolean hasOrder(String orderType) {
        for (InventorySlot slot : slots) {
            if (slot.isOccupied() && slot.getOrder().equals(orderType)) {
                return true;
            }
        }
        return false;
    }

    // Get the index of a slot containing a specific order
    public int findOrderSlot(String orderType) {
        for (int i = 0; i < slots.size; i++) {
            InventorySlot slot = slots.get(i);
            if (slot.isOccupied() && slot.getOrder().equals(orderType)) {
                return i;
            }
        }
        return -1;
    }

    // Render the inventory items
    public void render(SpriteBatch batch) {
        // These values control the size and positioning of inventory items
        float tileSize = 16 * 4;
        float itemSize = tileSize * 0.7f;
        float offsetX = (tileSize - itemSize) / 2;
        float offsetY = (tileSize - itemSize) / 2;
        float upwardOffset = 30f;
        
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (i == draggedSlotIndex && isDragging) continue;
            
            InventorySlot slot = slots.get(i);
            if (slot.isOccupied()) {
                String order = slot.getOrder();
                int menuIndex = getMenuIndexForOrder(order);
                if (menuIndex != -1) {
                    float x = (inventoryTileX[i] * tileSize) + offsetX;
                    float y = ((inventoryTileY[i] + 1) * tileSize) - offsetY - itemSize + upwardOffset; // Added upward offset
                    TextureRegion orderImage = orderImages[menuIndex];
                    batch.draw(orderImage, x, y, itemSize, itemSize);
                }
            }
        }
        
        // Render dragged item last (on top)
        if (isDragging && draggedSlotIndex != -1) {
            InventorySlot slot = slots.get(draggedSlotIndex);
            String order = slot.getOrder();
            int menuIndex = getMenuIndexForOrder(order);
            if (menuIndex != -1) {
                TextureRegion orderImage = orderImages[menuIndex];
                batch.draw(orderImage, draggedItemX - itemSize/2, draggedItemY - itemSize/2, itemSize, itemSize);
            }
        }
    }

    public void handleInput(float mouseX, float mouseY, boolean isMouseDown, TiledMap map) {
        float tileSize = 16 * 4;
        float itemSize = tileSize * 0.8f;
        float upwardOffset = 20f;
        
        if (isMouseDown && !isDragging) {
            // Check if mouse is over an inventory item
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots.get(i).isOccupied()) {
                    float itemX = (inventoryTileX[i] * tileSize) + (tileSize - itemSize) / 2;
                    float itemY = ((inventoryTileY[i] + 1) * tileSize) - (tileSize - itemSize) / 2 - itemSize + upwardOffset;
                    
                    if (mouseX >= itemX && mouseX <= itemX + itemSize &&
                        mouseY >= itemY && mouseY <= itemY + itemSize) {
                        isDragging = true;
                        draggedSlotIndex = i;
                        draggedItemX = mouseX;
                        draggedItemY = mouseY;
                        break;
                    }
                }
            }
        } else if (!isMouseDown && isDragging) {
            // Check if item is dropped over trash bin
            TiledMapTileLayer trashLayer = (TiledMapTileLayer) map.getLayers().get("Trashbin");
            if (trashLayer != null) {
                int tileX = (int) (mouseX / tileSize);
                int tileY = (int) (mouseY / tileSize);
                
                TiledMapTileLayer.Cell cell = trashLayer.getCell(tileX, tileY);
                if (cell != null) {
                    // Remove the item if dropped on trash bin
                    removeOrder(draggedSlotIndex);
                    System.out.println("Item removed from inventory");
                }
            }

            isDragging = false;
            draggedSlotIndex = -1;
        }

        if (isDragging) {
            draggedItemX = mouseX;
            draggedItemY = mouseY;
        }
    }

    // Method to check if inventory has a specific order and serve it to customer
    public boolean serveOrder(String orderType) {
        for (int i = 0; i < MAX_SLOTS; i++) {
            InventorySlot slot = slots.get(i);
            if (slot.isOccupied() && slot.getOrder().equals(orderType)) {
                // Remove the order from inventory
                removeOrder(i);
                return true;
            }
        }
        return false;
    }

    // Method to get all orders in inventory (for checking customer orders)
    public String[] getInventoryOrders() {
        ArrayList<String> orders = new ArrayList<>();
        for (InventorySlot slot : slots) {
            if (slot.isOccupied()) {
                orders.add(slot.getOrder());
            }
        }
        return orders.toArray(new String[0]);
    }

    public void dispose() {
        if (menuSheet != null) {
        menuSheet.dispose();
    }
    
    if (slots != null) {
        slots.clear();
    }
    
    if (orderImages != null) {
        for (int i = 0; i < orderImages.length; i++) {
            orderImages[i] = null;
        }
    }

    isDragging = false;
    draggedSlotIndex = -1;
    
    }

    private static class InventorySlot {
        private String order;
        private boolean occupied;

        public InventorySlot() {
            this.occupied = false;
            this.order = null;
        }

        public boolean isOccupied() {
            return occupied;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
            this.occupied = true;
        }

        public void clear() {
            this.order = null;
            this.occupied = false;
        }
    }
}
