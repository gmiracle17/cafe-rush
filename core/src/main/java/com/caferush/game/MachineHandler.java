package com.caferush.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;

public class MachineHandler {

    public static void handleMachineClick(TiledMap tiledMap, float unitScale, Vector3 screenCoords) {
        Vector3 worldCoords = new Vector3(screenCoords.x, screenCoords.y, 0);
        int tileX = (int) (worldCoords.x / (16 * unitScale));
        int tileY = (int) (worldCoords.y / (16 * unitScale));

        hideAllOptions(tiledMap);

        checkMachineClick(
                tiledMap,
                tileX,
                tileY,
                "Coffee Makers",
                "coffee_maker",
                "Coffee Choices",
                "Coffee Choices Box"
        );

        checkMachineClick(
                tiledMap,
                tileX,
                tileY,
                "Machines and Furniture",
                "oven",
                "Cooked Choices",
                "Cooked Choices Box"
        );

        checkMachineClick(
                tiledMap,
                tileX,
                tileY,
                "Machines and Furniture",
                "pastry",
                "Pastry Choices",
                "Pastry Choices Box"
        );
    }

    private static void checkMachineClick(
            TiledMap map,
            int tileX,
            int tileY,
            String layerName,
            String expectedMachineType,
            String optionsLayerName,
            String optionsBoxLayerName
    ) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) return;

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        if (cell == null || cell.getTile() == null) return;

        MapProperties props = cell.getTile().getProperties();
        if (expectedMachineType.equals(props.get("machine", String.class))) {
            showOptions(map, optionsLayerName, optionsBoxLayerName);
            System.out.println(expectedMachineType + " clicked! Options layer is now visible.");
        }
    }

    private static void showOptions(TiledMap map, String optionsLayerName, String optionsBoxLayerName) {
        MapLayer optionsLayer = map.getLayers().get(optionsLayerName);
        MapLayer optionsBox = map.getLayers().get(optionsBoxLayerName);

        if (optionsLayer != null) optionsLayer.setVisible(true);
        if (optionsBox != null) optionsBox.setVisible(true);
    }

    private static void hideAllOptions(TiledMap map) {
        String[] layersToHide = {
                "Coffee Choices", "Coffee Choices Box",
                "Cooked Choices", "Cooked Choices Box",
                "Pastry Choices", "Pastry Choices Box"
        };

        for (String layerName : layersToHide) {
            MapLayer layer = map.getLayers().get(layerName);
            if (layer != null) layer.setVisible(false);
        }
    }
}
