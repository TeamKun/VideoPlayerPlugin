package net.kunmc.lab.vplayer;

import dev.jorel.commandapi.CommandAPI;
import net.kunmc.lab.vplayer.server.command.VPlayerCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class VideoPlayer extends JavaPlugin {

    public static final String MODID = "vplayer";

    //public static JavaPlugin plugin;

    // Directly reference a log4j logger.
    public static Logger LOGGER;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(true); //Load with verbose output

        VPlayerCommand.register();
    }

    @Override
    public void onEnable() {
        LOGGER = getLogger();

        CommandAPI.onEnable(this);

        //plugin = this;

        ProxyServer proxy = new ProxyServer();
        proxy.registerEvents(this);
        proxy.onServerStart(this);
    }

}
