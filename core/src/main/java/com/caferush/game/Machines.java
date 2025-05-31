package com.caferush.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Machines {

    private static Sound ding;
    private static Inventory inventory;

    public static void initializeSounds() {
        if (ding == null) {
            ding = Gdx.audio.newSound(Gdx.files.internal("sfx/ding-101492.mp3"));
        }
    }

    public static void setInventory(Inventory inv) {
        inventory = inv;
    }

    public static abstract class Machine extends Thread {
        protected volatile boolean isBusy = false;
        protected String choice;
        protected final String name;
        protected long processTime;
        protected volatile boolean orderReady = false;
        protected volatile boolean isPaused = false;
        protected volatile long remainingTime = 0;

        protected String machineType;
        protected String optionsLayer;
        protected String optionsBoxLayer;
        protected String optionsHoverBoxLayer;
        protected String produceDisplayLayer;
        protected String produceDisplayBoxLayer;

        protected int displayX;
        protected int displayY;

        protected TiledMap map;

        protected final int machineId;

        public Machine(String name, int machineId, String machineType, String optionsLayer,
                       String optionsBoxLayer, String optionsHoverBoxLayer, String produceDisplayLayer,
                       String produceDisplayBoxLayer, long processTime, int displayX, int displayY) {
            this.name = name;
            this.machineId = machineId;
            this.machineType = machineType;
            this.optionsLayer = optionsLayer;
            this.optionsBoxLayer = optionsBoxLayer;
            this.optionsHoverBoxLayer = optionsHoverBoxLayer;
            this.produceDisplayLayer = produceDisplayLayer;
            this.produceDisplayBoxLayer = produceDisplayBoxLayer;
            this.processTime = processTime;
            this.displayX = displayX;
            this.displayY = displayY;
        }

        public void showOptions(TiledMap map) {
            this.map = map;

            if (!isBusy) {
                map.getLayers().get(this.optionsLayer).setVisible(true);
                map.getLayers().get(this.optionsBoxLayer).setVisible(true);
            }
        }

        public void hideOptions() {
            map.getLayers().get(this.optionsLayer).setVisible(false);
            map.getLayers().get(this.optionsBoxLayer).setVisible(false);
            map.getLayers().get(this.optionsHoverBoxLayer).setVisible(false);
        }

        public boolean startProcess(TiledMap map, TiledMapTileLayer.Cell optionCell) {
            // Check if the current machine is busy
            if (isBusy) {
                return false;  // Skip if this machine is busy
            }

            this.choice = optionCell.getTile().getProperties().get("order", String.class);
            this.isBusy = true;
            this.map = map;

            // Clone the tile to avoid shared reference
            TiledMapTileLayer.Cell displayCell = new TiledMapTileLayer.Cell();
            displayCell.setTile(optionCell.getTile());

            TiledMapTileLayer displayLayer = (TiledMapTileLayer) map.getLayers().get(this.produceDisplayLayer);
            displayLayer.setCell(this.displayX, this.displayY, null); // Clear display tile
            displayLayer.setCell(this.displayX, this.displayY, displayCell);
            displayLayer.setVisible(true);

            new Thread(this::runProcess).start();
            return true;
        }

        private void runProcess() {
            try {
                setStatusColor(map, this, " Red ");
                long startTime = System.currentTimeMillis();
                long halfTime = processTime / 2;
                remainingTime = processTime;

                // First half
                while (remainingTime > halfTime) {
                    if (!isPaused) {
                        Thread.sleep(100); // Sleep in small intervals to check pause state
                        remainingTime -= 100;
                    } else {
                        Thread.sleep(100); // Keep checking pause state
                    }
                }

                setStatusColor(map, this, " Yellow ");

                // Second half
                while (remainingTime > 0) {
                    if (!isPaused) {
                        Thread.sleep(100);
                        remainingTime -= 100;
                    } else {
                        Thread.sleep(100);
                    }
                }

                if (ding != null) {
                    long soundId = ding.play();
                    ding.setVolume(soundId, 0.5f);
                }
                setStatusColor(map, this, " Green ");
                orderReady = true;

                // Keep the order displayed until collected
                while (orderReady) {
                    if (!isPaused) {
                        Thread.sleep(100);
                    } else {
                        Thread.sleep(100);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                this.isBusy = false;
                // Don't set orderReady to false here, it should only be set to false when collected
            }
        }

        public void pauseProcess() {
            isPaused = true;
        }

        public void resumeProcess() {
            isPaused = false;
        }
    }

    public static class CoffeeMaker extends Machine {
        public CoffeeMaker(String name, int machineID, int displayX, int displayY) {
            super(name,
                    machineID,
                    "coffee_maker",
                    "Coffee Choices",
                    "Coffee Choices Box",
                    "Coffee Choices Hover Box",
                    "Coffee Produce Display",
                    "Coffee Produce Display Box",
                    5000,
                    displayX,
                    displayY);
        }
    }

    public static class PastryMaker extends Machine {
        public PastryMaker(String name, int displayX, int displayY) {
            super(name,
                    1,
                    "pastry",
                    "Pastry Choices",
                    "Pastry Choices Box",
                    "Pastry Choices Hover Box",
                    "Pastry Produce Display",
                    "Pastry Produce Display Box",
                    3000,
                    displayX,
                    displayY);
        }
    }

    public static class Oven extends Machine {
        public Oven(String name, int machineID, int displayX, int displayY) {
            super(name,
                    machineID,
                    "oven",
                    "Cooked Choices",
                    "Cooked Choices Box",
                    "Cooked Choices Hover Box",
                    "Cooked Produce Display",
                    "Cooked Produce Display Box",
                    20000,
                    displayX,
                    displayY);
        }
    }

    public static void setStatusColor(TiledMap map, Machines.Machine machine, String color) {
        TiledMapTileLayer boxLayer = (TiledMapTileLayer) map.getLayers().get(machine.produceDisplayBoxLayer + color + machine.machineId);

        TiledMapTileLayer.Cell originalBoxCell = boxLayer.getCell(machine.displayX, machine.displayY);
        clearUsedCells(boxLayer);

        if (originalBoxCell != null && originalBoxCell.getTile() != null) {
            TiledMapTileLayer.Cell newBoxCell = new TiledMapTileLayer.Cell();
            newBoxCell.setTile(originalBoxCell.getTile());
            boxLayer.setCell(machine.displayX, machine.displayY, newBoxCell);
            boxLayer.setVisible(true);
        } else {
            boxLayer.setVisible(false);
        }
    }

    public static void handleOptionsHover(TiledMap map, int tileX, int tileY, Machines.Machine[] machines) {
        boolean hoverLayerShown = false;

        for (Machines.Machine machine : machines) {

            TiledMapTileLayer optionsLayer = (TiledMapTileLayer) map.getLayers().get(machine.optionsLayer);
            TiledMapTileLayer optionsBoxLayer = (TiledMapTileLayer) map.getLayers().get(machine.optionsBoxLayer);
            TiledMapTileLayer hoverBoxLayer = (TiledMapTileLayer) map.getLayers().get(machine.optionsHoverBoxLayer);

            if (hoverBoxLayer == null) continue;

            clearUsedCells(hoverBoxLayer);
            hoverBoxLayer.setVisible(false);

            TiledMapTileLayer.Cell optionsCell = null;
            TiledMapTileLayer.Cell optionsBoxCell = null;

            if (optionsLayer != null && optionsLayer.isVisible()) optionsCell = optionsLayer.getCell(tileX, tileY);
            if (optionsBoxLayer != null && optionsBoxLayer.isVisible()) optionsBoxCell = optionsBoxLayer.getCell(tileX, tileY);

            TiledMapTileLayer.Cell hoverTileCell = null;

            if (optionsBoxCell != null && optionsBoxCell.getTile() != null) {
                hoverTileCell = optionsBoxCell;
            } else if (optionsCell != null && optionsCell.getTile() != null) {
                hoverTileCell = optionsCell;
            }

            if (hoverTileCell != null) {
                TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
                newCell.setTile(hoverTileCell.getTile());
                hoverBoxLayer.setCell(tileX, tileY, newCell);
                hoverBoxLayer.setVisible(true);
                hoverLayerShown = true;
            }
        }

        if (!hoverLayerShown) {
            for (Machines.Machine machine : machines) {
                TiledMapTileLayer hoverBoxLayer = (TiledMapTileLayer) map.getLayers().get(machine.optionsHoverBoxLayer);
                if (hoverBoxLayer != null) {
                    clearUsedCells(hoverBoxLayer);
                    hoverBoxLayer.setVisible(false);
                }
            }
        }
    }

    static void clearUsedCells(TiledMapTileLayer layer) {
        int width = layer.getWidth();
        int height = layer.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (layer.getCell(x, y) != null) {
                    layer.setCell(x, y, null);
                }
            }
        }
    }

    public static void hideAllOptions(TiledMap map, Machines.Machine[] machines) {
        for (Machines.Machine machine : machines) {
            MapLayer optionsLayer = map.getLayers().get(machine.optionsLayer);
            MapLayer optionsBoxLayer = map.getLayers().get(machine.optionsBoxLayer);
            MapLayer hoverBoxLayer = map.getLayers().get(machine.optionsHoverBoxLayer);

            optionsLayer.setVisible(false);
            optionsBoxLayer.setVisible(false);
            hoverBoxLayer.setVisible(false);
        }
    }

    public static String checkForSpecificMachine(TiledMap tiledMap, int centerX, int centerY, int radius, String machineType) {
        for (int y = centerY - radius; y <= centerY + radius; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("Machines and Furniture");
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    String foundType = cell.getTile().getProperties().get("machine", String.class);
                    if (foundType != null && foundType.equals(machineType)) {
                        return foundType; // Return the found machine type directly
                    }
                }
            }
        }
        return null;
    }

    public static void handleOptionClick(int tileX, int tileY, Machines.Machine[] machines, TiledMap tiledMap) {
        boolean processed = false;

        // Handle normal machine interaction
        for (Machines.Machine machine : machines) {
            if (machine.isBusy) {
                continue;
            }

            TiledMapTileLayer optionsLayer = (TiledMapTileLayer) tiledMap.getLayers().get(machine.optionsLayer);
            if (optionsLayer == null || !optionsLayer.isVisible()) continue;

            TiledMapTileLayer.Cell cell = optionsLayer.getCell(tileX, tileY);

            if (cell != null && cell.getTile() != null) {
                String order = cell.getTile().getProperties().get("order", String.class);

                if (order != null) {
                    boolean started = machine.startProcess(tiledMap, cell);

                    if (started) {
                        machine.hideOptions();
                        processed = true;
                        break;
                    }
                }
            }
        }

        if (!processed) {
            System.out.println("No available machine to process the order.");
        }
    }

    public static void dispose() {
        if (ding != null) {
            ding.dispose();
            ding = null;
        }
    }
}