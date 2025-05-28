package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.utils.ObjectMap;
import com.caferush.game.CustomerHandler.Customer;

public class OrderHandling {

    private static final int[][] menu_items = {
        {64,   0, 16, 16}, // hot choco
        {16,  0, 16, 16},  // espresso
        {32,  0, 16, 16},  // americano
        {16, 64, 16, 16},  // bread
        {0,  48, 16, 16},  // croissant
        {48, 80, 16, 16},  // doughnut
        {48, 192, 16, 16}, // shortcake
        {64, 144, 16, 16}, // cupcake
        {64, 112, 16, 16}, // crinkles
        {32, 112, 16, 16}  // biscuit
    };

    public int[][] getMenuItems() {
        return menu_items;
    }

    private static final int X = 0;
    private static final int Y = 1;
    private static final int W = 2;
    private static final int H = 3;

    private final Texture orderBubble;
    private final TextureRegion[] orderImage;
    private final ObjectMap<OrderPosition, OrderInfo> ordersPopup;
    private final Texture menuSheet;
    private Texture speechBubbleModerate;
    private Texture speechBubbleMinimal;

    public OrderHandling() {
        orderBubble = new Texture(Gdx.files.internal("pngs/speech-bubble.png"));
        menuSheet = new Texture("images/Bar katto 2.0 icon sheet.png");
        ordersPopup = new ObjectMap<>();
        loadSpeechBubbleTextures();

        orderImage = new TextureRegion[menu_items.length];
        for (int i = 0; i < menu_items.length; i++) {
            int[] item = menu_items[i];
            orderImage[i] = new TextureRegion(menuSheet, item[X], item[Y], item[W], item[H]);
        }
    }

    private void loadSpeechBubbleTextures() {
        speechBubbleModerate = new Texture("pngs/speech-bubble-moderatepatience.png");
        speechBubbleMinimal = new Texture("pngs/speech-bubble-minimalpatience.png"); // Fixed typo
    }

    public void addOrder(TiledMapTileMapObject seat, int menuItemIndex, Customer customer) {
        float orderX = seat.getProperties().get("orderpositionx", 0f, Float.class);
        float orderY = seat.getProperties().get("orderpositiony", 0f, Float.class);
        int seatId = seat.getProperties().get("Seat", -1, Integer.class);

        OrderPosition position = new OrderPosition(seat.getX(), seat.getY(), orderX, orderY, seatId);
        OrderInfo orderInfo = new OrderInfo(menuItemIndex, customer);
        ordersPopup.put(position, orderInfo);
    }

    public static class OrderPosition {
        public final float seatX, seatY;
        public final float orderX, orderY;
        public final int seatId;
        
        public OrderPosition(float x, float y, float ox, float oy, int id) {
            this.seatX = x;
            this.seatY = y;
            this.orderX = ox;
            this.orderY = oy;
            this.seatId = id;
        }
    }

    public static class OrderInfo {
        public final int menuItemIndex;
        public final Customer customer;
        
        public OrderInfo(int menuItemIndex, Customer customer) {
            this.menuItemIndex = menuItemIndex;
            this.customer = customer;
        }
    }

    public void renderOrders(SpriteBatch batch, float unitScale) {
        if (!batch.isDrawing()) {
            batch.begin();
        }

        try {
            float bubbleScale = 1.5f;
            float iconScale = 2.0f;
            float scaledWidth = orderBubble.getWidth() * bubbleScale;
            float scaledHeight = orderBubble.getHeight() * bubbleScale;

            for (ObjectMap.Entry<OrderPosition, OrderInfo> entry : ordersPopup) {
                OrderPosition seatPos = entry.key;
                OrderInfo orderInfo = entry.value;
                Customer customer = orderInfo.customer;
                TextureRegion icon = orderImage[orderInfo.menuItemIndex];
                
                float iconWidth = icon.getRegionWidth() * iconScale;
                float iconHeight = icon.getRegionHeight() * iconScale;
                
                float bubbleX = (seatPos.seatX + seatPos.orderX) * unitScale;
                float bubbleY = (seatPos.seatY + seatPos.orderY) * unitScale;
                

                Texture bubbleToDraw;
                    if (customer != null && customer.maxPatienceTime > 0) {
                        synchronized(customer) {
                        
                            if (customer.remainingWaitingforOrderTime / customer.maxWaitingforOrderTime < 0.30f) {
                                bubbleToDraw = speechBubbleMinimal;
                            } else if (customer.remainingWaitingforOrderTime / customer.maxWaitingforOrderTime < 0.50f) {
                                bubbleToDraw = speechBubbleModerate;
                            } else {
                                bubbleToDraw = orderBubble;
                            }
                        }
                        } else {
                            bubbleToDraw = orderBubble;
                        }

                // Draw the speech bubble
                batch.draw(bubbleToDraw, bubbleX, bubbleY, scaledWidth, scaledHeight);

                // Draw the menu item icon inside the bubble
                batch.draw(icon,
                    bubbleX + (scaledWidth - iconWidth)/2f,  
                    bubbleY + (scaledHeight - iconHeight)/2f + 9f, 
                    iconWidth, iconHeight
                );
            }
        } finally {

        }
    }

    public void removeOrderByCustomer(Customer customer) {
    // Find and remove any orders associated with this customer
    for (ObjectMap.Entry<OrderPosition, OrderInfo> entry : ordersPopup) {
        if (entry.value.customer == customer) {
            ordersPopup.remove(entry.key);
            break;
        }
    }
}


    public void dispose() {
        orderBubble.dispose();
        menuSheet.dispose();
        speechBubbleModerate.dispose();
        speechBubbleMinimal.dispose();
    }
}