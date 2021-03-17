package net.kunmc.lab.vplayer;

import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPI;
import net.kunmc.lab.vplayer.server.command.VPlayerCommand;
import net.kunmc.lab.vplayer.server.command.VTimeArgumentType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.logging.Logger;

public class VideoPlayer extends JavaPlugin implements Listener {

    public static final String MODID = "vplayer";

    // Directly reference a log4j logger.
    public static Logger LOGGER;

    private ProxyServer proxy;

    @Override
    public void onLoad() {
        LOGGER = getLogger();

        CommandAPI.onLoad(true); //Load with verbose output

        VPlayerCommand.register(Brigadier.getCommandDispatcher());
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);

        getServer().getPluginManager().registerEvents(this, this);

        proxy = new ProxyServer();
        proxy.registerEvents(this);
        proxy.onServerStart(this);
    }

    @EventHandler
    public void onPluginDisabled(PluginDisableEvent event) {
        if (event.getPlugin() != this)
            return;

        proxy.onServerClose();
    }

}
