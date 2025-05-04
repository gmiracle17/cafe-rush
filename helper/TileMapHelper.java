package helper;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TMXMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TileMapHelper {

    private TiledMap tiledMap;

    public TileMapHelper(){

    }

    public OrthogonalTiledMapRenderer setupMap(){
        tiledMap = new TMXMapLoader().load(/* filename ng map */);
        return new OrthogonalTiledMapRenderer(tiledMap);

    }

}
