package com.caferush.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.caferush.game.CafeRush;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Café Rush");
        config.setWindowedMode(900, 900);
        config.useVsync(true);
        new Lwjgl3Application(new CafeRush(), config);
    }
}
