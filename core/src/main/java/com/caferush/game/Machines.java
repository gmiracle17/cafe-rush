package com.caferush.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Machines {

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
                MachineHandler.setStatusColor(map, this, " Red ");
                Thread.sleep(processTime / 2);

                MachineHandler.setStatusColor(map, this, " Yellow ");
                Thread.sleep(processTime / 2);

                MachineHandler.setStatusColor(map, this, " Green ");
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

        public int[] getTileIDs() { return tileIDs; }
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
}