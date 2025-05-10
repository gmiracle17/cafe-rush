package com.caferush.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class OrderHandling {
    public static class SeatZone {
        public Rectangle bounds;  
        public Vector2 position; 
        public boolean occupied;

        public SeatZone(Rectangle bounds, Vector2 position) {
            this.bounds = bounds;
            this.position = position;
            this.occupied = false;
        }
    }

    private Array<SeatZone> seatZones = new Array<>();

    public OrderHandling(TiledMap tiledMap, float unitScale) {
        TiledMapTileLayer seatLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Seats");
        
        if (seatLayer != null) {
            int tileWidth = (int) (seatLayer.getTileWidth() * unitScale);
            int tileHeight = (int) (seatLayer.getTileHeight() * unitScale);

            // Find all tiles marked as seats
            for (int y = 0; y < seatLayer.getHeight(); y++) {
                for (int x = 0; x < seatLayer.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = seatLayer.getCell(x, y);
                    
                    if (cell != null && cell.getTile() != null) {
                        Boolean isSeat = cell.getTile().getProperties().get("isChair", Boolean.class);
                        if (isSeat != null && isSeat) {
                            Rectangle bounds = new Rectangle(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
                            seatZones.add(new SeatZone(bounds, new Vector2(x * tileWidth, y * tileHeight)));
                        }
                    }
                }
            }
        }
    }

    // Try to place a customer in a seat
        public Vector2 trySeatCustomer(Vector2 dropPosition) {
        for (SeatZone seat : seatZones) {
            if (seat.bounds.contains(dropPosition) && !seat.occupied) {
                seat.occupied = true;
                return seat.position;
            }
        }
        return null; // No available seat
    }

    public Array<SeatZone> getSeatZones() {
        return seatZones;
    }
}