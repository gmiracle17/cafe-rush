package com.caferush.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;



public class CafeRush extends ApplicationAdapter implements InputProcessor {

    TiledMap tiledMap;
    OrthogonalTiledMapRenderer tMR;
    
    private final float unitScale = 4f;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Vector2 spritePosition;
    private TextureRegion positioninSheet;
    private boolean beingDragged = false;
    private Vector2 offset = new Vector2();
    private OrderHandling orderHandling;
    private Array<RectangleMapObject> seatList = new Array<>();

    public static class SeatZone {
        public Rectangle bounds;
        public Vector2 position;
    
        public SeatZone(Rectangle bounds, Vector2 position) {
            this.bounds = bounds;
            this.position = position;
        }
    }    

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();


        int tileWidth = 16;
        int tileHeight = 16;
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        tiledMap = new TmxMapLoader().load("cafe-rush-maps/Cafe with Product Options.tmx");
        tMR = new OrthogonalTiledMapRenderer(tiledMap, unitScale);
        orderHandling = new OrderHandling(tiledMap, unitScale); 

        MapLayer seatLayer = tiledMap.getLayers().get("Seats"); 
        MapObjects seats = seatLayer.getObjects();  
        
        // center map
        float mapWidth = (tiledMap.getProperties().get("width", Integer.class)) * 16 * unitScale;
        float mapHeight = (tiledMap.getProperties().get("height", Integer.class)) * 16 * unitScale;
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        camera.update();

        // extract specific sprite from spritesheet
        Texture sheet = new Texture("cafe-rush-maps/images/cat-black-front.png");

        int col = 0;
        int row = 0;
        
        positioninSheet = new TextureRegion(sheet, col * tileWidth, row * tileHeight, tileWidth, tileHeight);
        spritePosition = new Vector2(800, 210);
        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(this); 
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        tMR.setView(camera);
        tMR.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(positioninSheet, spritePosition.x, spritePosition.y, 16 * unitScale, 16 * unitScale);
    
        for (RectangleMapObject seat : seatList) {  
        Rectangle seatRectangle = seat.getRectangle();  
}  
        batch.end();
}


    @Override
public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (beingDragged) {
        beingDragged = false;
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);

        // Try to seat customer and get seat position if successful
        Vector2 seatPosition = orderHandling.trySeatCustomer(new Vector2(clickedPosition.x, clickedPosition.y));
        if (seatPosition != null) {
            spritePosition.set(seatPosition.x, seatPosition.y - 10); // Snap to seat
        } else {
            spritePosition.set(800, 210); // Return to start
        }
        return true;
    }
    return false;
}

        @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);
        
        if (customerClicked(new Vector2(clickedPosition.x, clickedPosition.y))) {
            beingDragged = true;
            offset.set(
                clickedPosition.x - spritePosition.x,
                clickedPosition.y - spritePosition.y
            );
            return true;
        }
    return false;
}

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (beingDragged) {
            
            Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
            camera.unproject(clickedPosition);
            
            
            spritePosition.set(clickedPosition.x - offset.x, clickedPosition.y - offset.y);
            return true;
        }
        return false;
    }

        private boolean customerClicked(Vector2 worldConvert) {
            float width = 16 * unitScale;
            float height = 16 * unitScale;
            return worldConvert.x >= spritePosition.x && worldConvert.x <= spritePosition.x + width &&
                worldConvert.y >= spritePosition.y && worldConvert.y <= spritePosition.y + height;
        }

        @Override
        public void dispose() {
            tMR.dispose();
        }


        @Override
        public boolean keyDown(int keycode) { return false; }
        @Override
        public boolean keyUp(int keycode) { return false; }
        @Override
        public boolean keyTyped(char character) { return false; }
        @Override
        public boolean mouseMoved(int screenX, int screenY) { return false; }
        @Override
        public boolean scrolled(float amountX, float amountY) {return false;}
        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {return false;}
    }