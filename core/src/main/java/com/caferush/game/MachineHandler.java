package com.caferush.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;

public class MachineHandler {

    /*
     * TO DO: instead of mouse click, let cat mc go near machine tas tsaka lalabas options
     * If no time: delivering to customer nalang trabaho ng cat mc ig idk
     * ok ok ok pi pi pi pi
     * */
    public static void handleMachineClick(TiledMap tiledMap, float unitScale, Vector3 screenCoords) {
        Vector3 worldCoords = new Vector3(screenCoords.x, screenCoords.y, 0);

        hideAllOptions(tiledMap);
        int tileX = (int) (worldCoords.x / (16 * unitScale));
        int tileY = (int) (worldCoords.y / (16 * unitScale));

        checkMachineClick(
                tiledMap,
                tileX,
                tileY,
                "Coffee Makers",
                "coffee_maker",
                "Coffee Choices",
                "Coffee Choices Box",
                "Coffee Choices Hover Box"
        );

        checkMachineClick(
                tiledMap,
                tileX,
                tileY,
                "Machines and Furniture",
                "oven",
                "Cooked Choices",
                "Cooked Choices Box",
                "Cooked Choices Hover Box"
        );

        checkMachineClick(
                tiledMap,
                tileX,
                tileY,
                "Machines and Furniture",
                "pastry",
                "Pastry Choices",
                "Pastry Choices Box",
                "Pastry Choices Hover Box"
        );
    }

    private static void checkMachineClick(
            TiledMap map,
            int tileX,
            int tileY,
            String layerName,
            String machineType,
            String optionsLayerName,
            String optionsBoxLayerName,
            String hoverBoxLayerName
    ) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) return;

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        if (cell == null || cell.getTile() == null) return;

        String machineProp = cell.getTile().getProperties().get("machine", String.class);
        if (machineType.equals(machineProp)) {
            showOptions(map, optionsLayerName, optionsBoxLayerName, hoverBoxLayerName);
        }
    }

    public static void showOptions(TiledMap map, String optionsLayerName, String optionsBoxLayerName, String hoverBoxLayerName) {
        MapLayer optionsLayer = map.getLayers().get(optionsLayerName);
        MapLayer optionsBoxLayer = map.getLayers().get(optionsBoxLayerName);
        MapLayer hoverBoxLayer = map.getLayers().get(hoverBoxLayerName);

        if (optionsLayer != null) optionsLayer.setVisible(true);
        if (optionsBoxLayer != null) optionsBoxLayer.setVisible(true);
        if (hoverBoxLayer != null) hoverBoxLayer.setVisible(false); // hidden initially, only shown on hover
    }

    public static void handleOptionsHover(TiledMap map, int tileX, int tileY) {
        String[][] layerGroups = {
                {"Coffee Choices", "Coffee Choices Box", "Coffee Choices Hover Box"},
                {"Cooked Choices", "Cooked Choices Box", "Cooked Choices Hover Box"},
                {"Pastry Choices", "Pastry Choices Box", "Pastry Choices Hover Box"}
        };

        boolean hoverLayerShown = false;

        for (String[] group : layerGroups) {
            String optionsLayerName = group[0];
            String optionsBoxLayerName = group[1];
            String hoverBoxLayerName = group[2];

            TiledMapTileLayer optionsLayer = (TiledMapTileLayer) map.getLayers().get(optionsLayerName);
            TiledMapTileLayer optionsBoxLayer = (TiledMapTileLayer) map.getLayers().get(optionsBoxLayerName);
            TiledMapTileLayer hoverBoxLayer = (TiledMapTileLayer) map.getLayers().get(hoverBoxLayerName);

            if (hoverBoxLayer == null) continue;

            clearUsedCells(hoverBoxLayer);
            hoverBoxLayer.setVisible(false);

            TiledMapTileLayer.Cell optionsCell = null;
            TiledMapTileLayer.Cell optionsBoxCell = null;

            if (optionsLayer != null && optionsLayer.isVisible())
                optionsCell = optionsLayer.getCell(tileX, tileY);

            if (optionsBoxLayer != null && optionsBoxLayer.isVisible())
                optionsBoxCell = optionsBoxLayer.getCell(tileX, tileY);

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
            for (String[] group : layerGroups) {
                TiledMapTileLayer hoverBoxLayer = (TiledMapTileLayer) map.getLayers().get(group[2]);
                if (hoverBoxLayer != null) {
                    clearUsedCells(hoverBoxLayer);
                    hoverBoxLayer.setVisible(false);
                }
            }
        }
    }

    private static void clearUsedCells(TiledMapTileLayer layer) {
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

    public static void hideAllOptions(TiledMap map) {
        String[] layersToHide = {
                "Coffee Choices", "Coffee Choices Box", "Coffee Choices Hover Box",
                "Cooked Choices", "Cooked Choices Box", "Cooked Choices Hover Box",
                "Pastry Choices", "Pastry Choices Box", "Pastry Choices Hover Box"
        };

        for (String layerName : layersToHide) {
            MapLayer layer = map.getLayers().get(layerName);
            if (layer != null) layer.setVisible(false);
        }
    }

    public static String checkMachineTypeAt(TiledMap map, int tileX, int tileY, int radius) {
        String machineType = checkMachineProximity(map, tileX, tileY, radius, "Coffee Makers", "coffee_maker");
        if (machineType != null) return machineType;

        machineType = checkMachineProximity(map, tileX, tileY, radius, "Machines and Furniture", "oven");
        if (machineType != null) return machineType;

        machineType = checkMachineProximity(map, tileX, tileY, radius, "Machines and Furniture", "pastry");
        if (machineType != null) return machineType;

        return null;
    }

    public static String checkMachineProximity(TiledMap map, int centerTileX, int centerTileY, int radius,
                                               String layerName, String machineType) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) return null;

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int yOffset = -radius; yOffset <= radius; yOffset++) {
                int checkX = centerTileX + xOffset;
                int checkY = centerTileY + yOffset;

                TiledMapTileLayer.Cell cell = layer.getCell(checkX, checkY);
                if (cell == null || cell.getTile() == null) continue;

                String machineProp = cell.getTile().getProperties().get("machine", String.class);
                if (machineType.equals(machineProp)) {
                    return machineType;
                }
            }
        }

        return null;
    }

    public static String checkMachineTypeAtExact(TiledMap map, int tileX, int tileY) {
        String machineType = checkExactMachineAt(map, tileX, tileY, "Coffee Makers", "coffee_maker");
        if (machineType != null) return machineType;

        machineType = checkExactMachineAt(map, tileX, tileY, "Machines and Furniture", "oven");
        if (machineType != null) return machineType;

        machineType = checkExactMachineAt(map, tileX, tileY, "Machines and Furniture", "pastry");
        if (machineType != null) return machineType;

        return null;
    }

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