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
import com.badlogic.gdx.utils.Array;




public class CafeRush extends ApplicationAdapter implements InputProcessor {

    TiledMap tiledMap;
    OrthogonalTiledMapRenderer tMR;
    
    
    private final float unitScale = 4f;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private OrderHandling orderHandling;
    private Array<Texture> characterSprites;
    private Array<Customer> customers;
    private float customerSpawnTimer = 0;
    private final float SPAWN_INTERVAL = 5f; // Spawn a customer every 5 seconds
    private boolean canSpawnNewCustomer = true; // Control whether new customers can spawn
    

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
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);

        tiledMap = new TmxMapLoader().load("cafe-rush-maps/Cafe Map.tmx");
        tMR = new OrthogonalTiledMapRenderer(tiledMap, unitScale);
        
        // center map
        float mapWidth = (tiledMap.getProperties().get("width", Integer.class)) * 16 * unitScale;
        float mapHeight = (tiledMap.getProperties().get("height", Integer.class)) * 16 * unitScale;
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        camera.update();

        characterSprites = new Array<>();
        characterSprites.add(new Texture("cafe-rush-maps/images/cat-black-front.png")); // Example 1
        characterSprites.add(new Texture("cafe-rush-maps/images/cat-orange-front.png")); // Example 2
        customers = new Array<>(); // Initialize the customers array
        
        // Spawn initial customer
        spawnCustomer();
        
        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(this); 
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Check if any customer is being dragged
        boolean anyCustomerBeingDragged = false;
        for (Customer customer : customers) {
            if (customer.beingDragged) {
                anyCustomerBeingDragged = true;
                break;
            }
        }
        
        // Only allow spawning if no customers are being dragged and enough time has passed
        canSpawnNewCustomer = !anyCustomerBeingDragged;
        
        // Update customer spawn timer
        if (canSpawnNewCustomer) {
            customerSpawnTimer += deltaTime;
            if (customerSpawnTimer >= SPAWN_INTERVAL) {
                spawnCustomer();
                customerSpawnTimer = 0;
            }
        }
        
        // Update all customers and remove those who have left
        for (int i = customers.size - 1; i >= 0; i--) {
            Customer customer = customers.get(i);
            if (customer.update(deltaTime)) {
                customers.removeIndex(i);
                Gdx.app.log("CUSTOMER", "Customer left! Remaining: " + customers.size);
            }
        }
        
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
        
        // Draw all customers and their wait timers if not seated
        for (Customer customer : customers) {
            batch.draw(customer.sprite, 
                     customer.position.x, 
                     customer.position.y, 
                     16 * unitScale, 
                     16 * unitScale);
                     
        }
        
        if (orderHandling != null) {
            orderHandling.renderOrders(batch, unitScale);
        }
        batch.end();
    }

    public class Customer {
        public TextureRegion sprite;
        public Vector2 position;
        public boolean isSeated;
        public boolean beingDragged;
        public Vector2 offset = new Vector2();
        public float waitTime = 0; // Track how long customer has been waiting
        public final float MAX_WAIT_TIME = 30f; // Leave after 30 seconds if not seated

        public Customer(Texture characterSprite) {
            int frameCol = MathUtils.random(0, 0); 
            int frameRow = MathUtils.random(0, 0); 
            this.sprite = new TextureRegion(characterSprite, frameCol * 16, frameRow * 16, 16, 16);
            this.position = new Vector2(800, 210); // Spawn point
            this.isSeated = false;
            this.beingDragged = false;
        }
        
        // Update customer state - returns true if customer should be removed
        public boolean update(float deltaTime) {
            if (!isSeated && !beingDragged) {
                waitTime += deltaTime;
                if (waitTime >= MAX_WAIT_TIME) {
                    // Customer waited too long and is leaving
                    return true;
                }
            }
            return false;
        }
    }

    private void spawnCustomer() {
        // Check if we can spawn a new customer
        if (customers.size >= 5) return; // Limit to 5 customers
        
        // Check if any customers are waiting at spawn point and not being handled
        boolean customersWaiting = false;
        for (Customer customer : customers) {
            if (!customer.isSeated && !customer.beingDragged && 
                Math.abs(customer.position.x - 800) < 10 && 
                Math.abs(customer.position.y - 210) < 10) {
                customersWaiting = true;
                break;
            }
        }
        
        // Only spawn if no one is waiting at spawn point
        if (!customersWaiting) {
            Texture randomSheet = characterSprites.random();
            customers.add(new Customer(randomSheet));
            
            // Log new customer arrival
            Gdx.app.log("CUSTOMER", "New customer arrived! Total: " + customers.size);
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);

        // Find which customer is being dragged
        for (Customer customer : customers) {
            if (customer.beingDragged) {
                customer.beingDragged = false;
                
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

                            // Check if dropped on the seat bounds
                            if (seatBounds.contains(clickedPosition.x, clickedPosition.y)) {
                                customer.position.set(
                                    seat.getX() * unitScale + (seatBounds.width / 2) - 30,
                                    seat.getY() * unitScale + (seatBounds.height / 2)
                                );
                                customer.isSeated = true; 

                                // Add an order for this customer
                                int pickOrder = MathUtils.random(orderHandling.getMenuItems().length - 1);
                                orderHandling.addOrder(seat, pickOrder);
                                Gdx.app.log("ORDERS", "Added order #" + pickOrder + 
                                    " at seat: " + seat.getX() + "," + seat.getY());
                                return true;
                            }
                        }
                    }
                }
                // If no seat then return to spawn point
                customer.position.set(800, 210);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);
        Vector2 worldPosition = new Vector2(clickedPosition.x, clickedPosition.y);
        
        // Check if any customer was clicked
        for (Customer customer : customers) {
            // Only allow dragging customers that aren't seated
            if (!customer.isSeated && customerClicked(customer, worldPosition)) {
                customer.beingDragged = true;
                customer.offset.set(
                    clickedPosition.x - customer.position.x,
                    clickedPosition.y - customer.position.y
                );
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 clickedPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(clickedPosition);
        
        // Update position for the customer being dragged
        for (Customer customer : customers) {
            if (customer.beingDragged) {
                customer.position.set(
                    clickedPosition.x - customer.offset.x, 
                    clickedPosition.y - customer.offset.y
                );
                return true;
            }
        }
        return false;
    }

    private boolean customerClicked(Customer customer, Vector2 worldPosition) {
        float width = 16 * unitScale;
        float height = 16 * unitScale;
        return worldPosition.x >= customer.position.x && 
               worldPosition.x <= customer.position.x + width &&
               worldPosition.y >= customer.position.y && 
               worldPosition.y <= customer.position.y + height;
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