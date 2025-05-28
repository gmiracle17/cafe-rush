package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.utils.ObjectMap;



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
        private final ObjectMap<OrderPosition, Integer> ordersPopup;
        private final Texture menuSheet;

    public OrderHandling() {

        orderBubble = new Texture(Gdx.files.internal("cafe-rush-maps/images/speech-bubble.png"));
        menuSheet = new Texture("cafe-rush-maps/images/Bar katto 2.0 icon sheet.png");
        ordersPopup = new ObjectMap<>();

        orderImage = new TextureRegion[menu_items.length];
        for (int i = 0; i < menu_items.length; i++) {
            int[] item = menu_items[i];
            orderImage[i] = new TextureRegion(menuSheet, item[X], item[Y], item[W], item[H]);
        }
    }
        public void addOrder(TextureMapObject seat, int menuItemIndex) {
            float orderX = seat.getProperties().get("orderpositionx", 0f, Float.class);
            float orderY = seat.getProperties().get("orderpositiony", 0f, Float.class);
            int seatId = seat.getProperties().get("Seat", -1, Integer.class);

            ordersPopup.put(new OrderPosition(seat.getX(), seat.getY(),orderX, orderY,seatId), menuItemIndex
    );
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

    public void renderOrders(SpriteBatch batch, float unitScale) {

        if (!batch.isDrawing()) {
        batch.begin();
    }

    try {
        float bubbleScale = 1.5f;
        float iconScale = 2.0f;
        float scaledWidth = orderBubble.getWidth() * bubbleScale;
        float scaledHeight = orderBubble.getHeight() * bubbleScale;
        

        for (ObjectMap.Entry<OrderPosition, Integer> entry : ordersPopup) {
            OrderPosition seatPos = entry.key;
            TextureRegion icon = orderImage[entry.value];
            float iconWidth = icon.getRegionWidth() * iconScale;
            float iconHeight = icon.getRegionHeight() * iconScale;
            
            float bubbleX = (seatPos.seatX + seatPos.orderX) * unitScale;
            float bubbleY = (seatPos.seatY + seatPos.orderY) * unitScale;
            
            // draw bubble      
            batch.draw(orderBubble, bubbleX, bubbleY, scaledWidth, scaledHeight);

            // position icon in bubble
            batch.draw(icon,
                bubbleX + (scaledWidth - iconWidth)/2f,  
                bubbleY + (scaledHeight - iconHeight)/2f + 9f, 
                iconWidth, iconHeight
            );
        }
    } finally {
        
    }
}

    public void completeOrder(OrderPosition seatPosition) {
        ordersPopup.remove(seatPosition);
    }

}
    
