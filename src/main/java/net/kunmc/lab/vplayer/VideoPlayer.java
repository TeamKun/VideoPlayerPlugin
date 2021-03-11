package net.kunmc.lab.vplayer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class VideoPlayer extends JavaPlugin {

    public static final String MODID = "vplayer";

    //public static JavaPlugin plugin;

    // Directly reference a log4j logger.
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        //plugin = this;

        ProxyServer proxy = new ProxyServer();
        proxy.registerEvents(this);
        proxy.onServerStart(this);
    }

}
