package com.caferush.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Machines {

    static Sound ding = Gdx.audio.newSound(Gdx.files.internal("sounds/ding-101492.mp3"));

    public static abstract class Machine extends Thread {
        protected volatile boolean isBusy = false;
        protected String choice;
        protected final String name;
        protected long processTime;

        /* Properties of Machine in Tiled Map */
        protected String machineLayer;
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

        public Machine(String name, int machineId, String machineLayer, String machineType, String optionsLayer,
                       String optionsBoxLayer, String optionsHoverBoxLayer, String produceDisplayLayer,
                       String produceDisplayBoxLayer, long processTime, int displayX, int displayY) {
            this.name = name;
            this.machineId = machineId;
            this.machineLayer = machineLayer;
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
                System.out.println(name + " is currently busy.");
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
                Thread.sleep(processTime / 2);

                setStatusColor(map, this, " Yellow ");
                Thread.sleep(processTime / 2);

                ding.play();
                setStatusColor(map, this, " Green ");
                Thread.sleep(5000); // simulate user collecting

                // Clear visuals
                TiledMapTileLayer displayLayer = (TiledMapTileLayer) map.getLayers().get(this.produceDisplayLayer);
                displayLayer.getCell(displayX, displayY).setTile(null);

                String[] colors = {" Green ", " Yellow ", " Red "};
                for (String color : colors) {
                    TiledMapTileLayer boxLayer = (TiledMapTileLayer) map.getLayers().get(this.produceDisplayBoxLayer + color + this.machineId);
                    boxLayer.setVisible(false);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.isBusy = false;
            }
        }
    }

    public static class CoffeeMaker extends Machine {
        protected final float x, y;

        public CoffeeMaker(String name, int machineID, int x, int y, int displayX, int displayY) {
            super(name,
                    machineID,
                    "Coffee Makers",
                    "coffee_maker",
                    "Coffee Choices",
                    "Coffee Choices Box",
                    "Coffee Choices Hover Box",
                    "Coffee Produce Display",
                    "Coffee Produce Display Box",
                    5000,
                    displayX,
                    displayY);
            this.x = x;
            this.y = y;
        }

        public float getX() { return this.x; }
        public float getY() { return this.y; }
    }

    public static class PastryMaker extends Machine {
        protected final int[] tileIDs = {49, 50, 61, 62};

        public PastryMaker(String name, int displayX, int displayY) {
            super(name,
                    1,
                    "Machines and Furniture",
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
        protected int[] tileIds;

        public Oven(String name, int machineID, int[] tileIds, int displayX, int displayY) {
            super(name,
                    machineID,
                    "Machines and Furniture",
                    "oven",
                    "Cooked Choices",
                    "Cooked Choices Box",
                    "Cooked Choices Hover Box",
                    "Cooked Produce Display",
                    "Cooked Produce Display Box",
                    20000,
                    displayX,
                    displayY);
            this.tileIds = tileIds;
        }
    }

    /* Machine Timer Visualization */
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


    /* Shows hovered tile among options */
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

    /* Removes cell */
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

    /* Hides options after transferring to another machine or walking away far from machine */
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

    /* honestly, don't need this anymore */
    public static String checkMachineTypeAtExact(TiledMap map, int tileX, int tileY) {
        String machineType = checkExactMachineAt(map, tileX, tileY, "Coffee Makers", "coffee_maker");
        if (machineType != null) return machineType;

        machineType = checkExactMachineAt(map, tileX, tileY, "Machines and Furniture", "oven");
        if (machineType != null) return machineType;

        machineType = checkExactMachineAt(map, tileX, tileY, "Machines and Furniture", "pastry");
        if (machineType != null) return machineType;

        return null;
    }

    /* Request: Please make this interact with specified tiles in Machine Threads
     * Coffee_maker(3): One tile only
     * Oven(2): 4 different tiles
     * Pastry(1): 4 different tiles
     * */
    private static String checkExactMachineAt(TiledMap map, int tileX, int tileY,
                                              String layerName, String machineType) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) return null;

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        if (cell == null || cell.getTile() == null) return null;

        String machineProp = cell.getTile().getProperties().get("machine", String.class);
        if (machineType.equals(machineProp)) {
            return machineType;
        }

        return null;
    }
}