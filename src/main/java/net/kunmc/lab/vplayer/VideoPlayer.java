package net.kunmc.lab.vplayer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class VideoPlayer extends JavaPlugin {

    public static final String MODID = "net/kunmc/lab/vplayer";

    // Directly reference a log4j logger.
    public static Logger LOGGER;

    public VideoPlayer() {
        ProxyServer proxy = new ProxyServer();
        proxy.registerEvents();
    }

    @Override
    public void onEnable() {
        LOGGER = getLogger();
    }

}
