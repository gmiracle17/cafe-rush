package com.caferush.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;

public class GameStatus {
    private Texture gameWonTexture;
    private Texture gameLostTexture;
    private Texture restartButtonTexture;
    private Texture resumeButtonTexture;
    private Texture menuButtonTexture;

    private Rectangle restartButtonBounds;
    private Rectangle resumeButtonBounds;
    private Rectangle menuButtonBounds;

    private Vector2 restartButtonPosition;
    private Vector2 resumeButtonPosition;
    private Vector2 menuButtonPosition;

    private final OrthographicCamera camera;

    private final StatusListener listener;
    private Sound buttonClickSound;
    private boolean isGameOver = true;

    public interface StatusListener {
        void onStartGame();
        void onResumeGame();
        void onBackToMenu();
    }

    public GameStatus(StatusListener listener) {
        this.listener = listener;

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.update();

        initializeBounds();
        reloadAssets();
        loadSounds();
    }

    private void initializeBounds() {
        // Make buttons slightly smaller than main menu
        int buttonWidth = 350;    // Reduced from 400
        int buttonHeight = 90;    // Reduced from 100
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Position buttons with proper spacing - match main menu exactly
        float topButtonY = screenHeight / 2f - 170;  // Same as main menu's start button
        float bottomButtonY = screenHeight / 2f - 280;  // Same as main menu's resume button

        // Position buttons with proper spacing
        restartButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, topButtonY);
        resumeButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, topButtonY);
        menuButtonPosition = new Vector2((screenWidth - buttonWidth) / 2f, bottomButtonY);

        // Create bounds with matching dimensions
        restartButtonBounds = new Rectangle(restartButtonPosition.x, restartButtonPosition.y, buttonWidth, buttonHeight);
        resumeButtonBounds = new Rectangle(resumeButtonPosition.x, resumeButtonPosition.y, buttonWidth, buttonHeight);
        menuButtonBounds = new Rectangle(menuButtonPosition.x, menuButtonPosition.y, buttonWidth, buttonHeight);

        System.out.println("Screen dimensions: " + screenWidth + "x" + screenHeight);
        System.out.println("Button dimensions: " + buttonWidth + "x" + buttonHeight);
        System.out.println("Restart button bounds: " + restartButtonBounds);
        System.out.println("Resume button bounds: " + resumeButtonBounds);
        System.out.println("Menu button bounds: " + menuButtonBounds);
    }

    public void reloadAssets() {
        // Dispose existing textures first
        disposeTextures();
        
        try {
            gameWonTexture = new Texture(Gdx.files.internal("congratulations message.png"));
            gameLostTexture = new Texture(Gdx.files.internal("gameover message.png"));
            restartButtonTexture = new Texture(Gdx.files.internal("buttons/restart.png"));
            resumeButtonTexture = new Texture(Gdx.files.internal("buttons/resume.png"));
            menuButtonTexture = new Texture(Gdx.files.internal("buttons/exit.png")); // Changed to exit button
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load game status textures: " + e.getMessage());
        }
    }

    private void loadSounds() {
        try {
            if (buttonClickSound == null) {
                buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("sfx/buttonclick3.mp3"));
            }
        } catch (Exception e) {
            System.err.println("Error loading button sound: " + e.getMessage());
        }
    }

    private void disposeTextures() {
        if (gameWonTexture != null) {
            gameWonTexture.dispose();
            gameWonTexture = null;
        }
        if (gameLostTexture != null) {
            gameLostTexture.dispose();
            gameLostTexture = null;
        }
        if (restartButtonTexture != null) {
            restartButtonTexture.dispose();
            restartButtonTexture = null;
        }
        if (resumeButtonTexture != null) {
            resumeButtonTexture.dispose();
            resumeButtonTexture = null;
        }
        if (menuButtonTexture != null) {
            menuButtonTexture.dispose();
            menuButtonTexture = null;
        }
    }

    public void dispose() {
        disposeTextures();
        if (buttonClickSound != null) {
            buttonClickSound.dispose();
            buttonClickSound = null;
        }
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
        if (gameWonTexture == null || gameLostTexture == null) {
            reloadAssets();
        }
    }

    public void render(SpriteBatch batch) {
        // Clear the screen first
        Gdx.gl.glClearColor(0.76f, 0.7f, 0.64f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Background based on game state
        Texture background = isGameOver ? gameLostTexture : gameWonTexture;
        if (background != null) {
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Calculate proper button height based on texture aspect ratio
        float buttonHeight;
        
        // Game Over screen: show Restart and Exit buttons
        if (isGameOver) {
            if (restartButtonTexture != null) {
                buttonHeight = restartButtonBounds.width * (restartButtonTexture.getHeight() / (float)restartButtonTexture.getWidth());
                // Center the button vertically within its bounds
                float yOffset = (restartButtonBounds.height - buttonHeight) / 2;
                batch.draw(restartButtonTexture, 
                          restartButtonPosition.x, 
                          restartButtonPosition.y + yOffset, 
                          restartButtonBounds.width, 
                          buttonHeight);
            }
        } else {
            // Day Complete screen: show Resume button
            if (resumeButtonTexture != null) {
                buttonHeight = resumeButtonBounds.width * (resumeButtonTexture.getHeight() / (float)resumeButtonTexture.getWidth());
                // Center the button vertically within its bounds
                float yOffset = (resumeButtonBounds.height - buttonHeight) / 2;
                batch.draw(resumeButtonTexture, 
                          resumeButtonPosition.x, 
                          resumeButtonPosition.y + yOffset, 
                          resumeButtonBounds.width, 
                          buttonHeight);
            }
        }

        // Always show Exit button at the bottom
        if (menuButtonTexture != null) {
            buttonHeight = menuButtonBounds.width * (menuButtonTexture.getHeight() / (float)menuButtonTexture.getWidth());
            // Center the button vertically within its bounds
            float yOffset = (menuButtonBounds.height - buttonHeight) / 2;
            batch.draw(menuButtonTexture, 
                      menuButtonPosition.x, 
                      menuButtonPosition.y + yOffset, 
                      menuButtonBounds.width, 
                      buttonHeight);
        }

        batch.end();
    }

    private void playButtonSound() {
        if (buttonClickSound != null) {
            float soundVolume = 0.5f;
            buttonClickSound.play(soundVolume);
        }
    }

    public boolean touchDown(int screenX, int screenY) {
        // Convert screen coordinates to camera coordinates
        Vector3 touch = new Vector3(screenX, screenY, 0);
        camera.unproject(touch);

        // Check if click is within menu button bounds
        if (touch.x >= menuButtonPosition.x && 
            touch.x <= menuButtonPosition.x + menuButtonBounds.width &&
            touch.y >= menuButtonPosition.y && 
            touch.y <= menuButtonPosition.y + menuButtonBounds.height) {
            
            playButtonSound();
            if (listener != null) {
                listener.onBackToMenu();
                return true;
            }
        }
        // Check restart button (game over screen)
        else if (isGameOver && 
                 touch.x >= restartButtonPosition.x && 
                 touch.x <= restartButtonPosition.x + restartButtonBounds.width &&
                 touch.y >= restartButtonPosition.y && 
                 touch.y <= restartButtonPosition.y + restartButtonBounds.height) {
            
            playButtonSound();
            if (listener != null) {
                listener.onStartGame();
                return true;
            }
        }
        // Check resume button (day complete screen)
        else if (!isGameOver && 
                 touch.x >= resumeButtonPosition.x && 
                 touch.x <= resumeButtonPosition.x + resumeButtonBounds.width &&
                 touch.y >= resumeButtonPosition.y && 
                 touch.y <= resumeButtonPosition.y + resumeButtonBounds.height) {
            
            playButtonSound();
            if (listener != null) {
                listener.onResumeGame();
                return true;
            }
        }

        return false;
    }
}