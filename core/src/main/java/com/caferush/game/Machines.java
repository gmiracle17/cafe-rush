package com.caferush.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

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

        public Machine(String name, String machineLayer, String machineType, String optionsLayer,
                       String optionsBoxLayer, String optionsHoverBoxLayer, String produceDisplayLayer,
                       String produceDisplayBoxLayer, long processTime, int displayX, int displayY) {
            this.name = name;
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

        /* what i want to happen for the methods here...
         * starting process... will be like showChosenOption but remove the box layer for now
         * the idea is in first processTime/3 make produceDisplayBoxLayer + Red visible
         * in second processTime/3 + Yellow is visible
         * then lastly, if time = processTime make + Green visible*/
        public boolean startProcess(TiledMap map, Machines.Machine machine, TiledMapTileLayer.Cell optionCell) {
            if (isBusy) {
                System.out.println(name + " is currently busy."); // maybe display red machine layer for 3 seconds
                return false;
            } else {
                this.choice = optionCell.getTile().getProperties().get("order", String.class);
                this.isBusy = true;
                this.map = map;

                TiledMapTileLayer displayLayer = (TiledMapTileLayer) map.getLayers().get(machine.produceDisplayLayer);
                MachineHandler.clearUsedCells(displayLayer);
                displayLayer.setCell(machine.displayX, machine.displayY, optionCell);
                displayLayer.setVisible(true);

                new Thread(() -> runProcess()).start();
                return true;
            }
        }

        private void runProcess() {
            try {
                MachineHandler.setStatusColor(map, this, " Red");
                Thread.sleep(processTime / 2);

                MachineHandler.setStatusColor(map, this, " Yellow");
                Thread.sleep(processTime / 2);

                MachineHandler.setStatusColor(map, this, " Green");
                Thread.sleep(5000); // simulate user collecting

                // Clear visuals
                TiledMapTileLayer displayLayer = (TiledMapTileLayer) map.getLayers().get(this.produceDisplayLayer);
                displayLayer.setVisible(false);

                String[] colors = {" Green", " Yellow", " Red"};
                for (String color : colors) {
                    TiledMapTileLayer boxLayer = (TiledMapTileLayer) map.getLayers().get(this.produceDisplayBoxLayer + color);
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

        public CoffeeMaker(String name, int x, int y, int displayX, int displayY) {
            super(name,
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

        public Oven(String name, int[] tileIds, int displayX, int displayY) {
            super(name,
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