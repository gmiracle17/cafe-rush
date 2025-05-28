package com.caferush.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class MachineHandler {

    private static Machines.Machine activeMachine = null;

    /* Shows options for machine clicked */
    public static void showOptions(TiledMap map, Machines.Machine machine) {
        activeMachine = machine;

        MapLayer optionsLayer = map.getLayers().get(machine.optionsLayer);
        MapLayer optionsBoxLayer = map.getLayers().get(machine.optionsBoxLayer);

        optionsLayer.setVisible(true);
        optionsBoxLayer.setVisible(true);
    }

    public static Machines.Machine getActiveMachine() {
        return activeMachine;
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