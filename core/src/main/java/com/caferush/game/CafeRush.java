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
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.caferush.game.OrderHandling;



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
    private boolean isSeated = false;
    private OrderHandling orderHandling;
    

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
        orderHandling = new OrderHandling();


        int tileWidth = 16;
        int tileHeight = 16;
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        tiledMap = new TmxMapLoader().load("cafe-rush-maps/Cafe with Product Options.tmx");
        tMR = new OrthogonalTiledMapRenderer(tiledMap, unitScale);
        
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
        
        MapLayer seatLayer = tiledMap.getLayers().get("Seats");
            if (seatLayer != null) {
                for (MapObject obj : seatLayer.getObjects()) {
                    if (obj instanceof TextureMapObject) {
                        TextureMapObject seatObj = (TextureMapObject) obj;
                        batch.draw(seatObj.getTextureRegion(), 
                                seatObj.getX() * unitScale, 
                                seatObj.getY() * unitScale,
                                seatObj.getTextureRegion().getRegionWidth() * unitScale,
                                seatObj.getTextureRegion().getRegionHeight() * unitScale);
                    }
                }
            }
            batch.draw(positioninSheet, spritePosition.x, spritePosition.y, 16 * unitScale, 16 * unitScale);
        
        if (orderHandling != null) {
    orderHandling.renderOrders(batch, unitScale);
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
        MapLayer seatLayer = tiledMap.getLayers().get("Seats");
        if (seatLayer != null) {
            for (MapObject obj : seatLayer.getObjects()) {
                // Check if seat
                if (obj instanceof TextureMapObject) {
                    TextureMapObject seat = (TextureMapObject) obj;
                    
                    // Calculate seat bounds
                    Rectangle seatBounds = new Rectangle(
                        seat.getX() * unitScale,
                        seat.getY() * unitScale,
                        seat.getTextureRegion().getRegionWidth() * unitScale,
                        seat.getTextureRegion().getRegionHeight() * unitScale
                    );

                    // Check if nadrop dun sa bounds nung seat
                    if (seatBounds.contains(clickedPosition.x, clickedPosition.y)) {
                        spritePosition.set(
                            seat.getX() * unitScale + (seatBounds.width / 2)-30,
                            seat.getY() * unitScale + (seatBounds.height / 2)
                        );
                        isSeated = true; 

                        int pickOrder = MathUtils.random(orderHandling.getMenuItems().length - 1);
                        orderHandling.addOrder(seat,pickOrder);
                            Gdx.app.log("ORDERS", "Added order #" + pickOrder + 
                            " at seat: " + seat.getX() + "," + seat.getY());
                        return true;
                    }
                }
            }
        }
        // if no seat then return to spawn point
        spritePosition.set(800, 210);
        return true;
    }
    return false;
}

        @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isSeated) return false;

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