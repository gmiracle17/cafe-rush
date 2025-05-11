package com.caferush.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
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
    private final float unitScale = 4f;

   public OrderHandling(TiledMap tiledMap, float unitScale) {
    MapLayer seatLayer = tiledMap.getLayers().get("Seats"); 
    if (seatLayer != null) {
        MapObjects seats = seatLayer.getObjects();
        Array<RectangleMapObject> seatList = seats.getByType(RectangleMapObject.class);
        
        for (RectangleMapObject seatObj : seatList) {
            Rectangle bounds = seatObj.getRectangle();
            // Adjust for unitScale if needed
            bounds.x *= unitScale;
            bounds.y *= unitScale;
            bounds.width *= unitScale;
            bounds.height *= unitScale;
            
            seatZones.add(new SeatZone(
                bounds,
                new Vector2(bounds.x, bounds.y)
            ));
        }
    }
}
    // Place customer in seat
    public Vector2 trySeatCustomer(Vector2 dropPosition) {
    for (SeatZone seat : seatZones) {
        // Convert seat bounds to world coordinates
        Rectangle worldBounds = new Rectangle(
            seat.bounds.x * unitScale,
            seat.bounds.y * unitScale,
            seat.bounds.width * unitScale,
            seat.bounds.height * unitScale
        );
        
        if (worldBounds.contains(dropPosition) && !seat.occupied) {
            seat.occupied = true;
            return new Vector2(worldBounds.x, worldBounds.y);
        }
    }
    return null;
}
    public Array<SeatZone> getSeatZones() {
        return seatZones;
    }
}