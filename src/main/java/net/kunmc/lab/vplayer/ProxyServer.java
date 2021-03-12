package net.kunmc.lab.vplayer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.kunmc.lab.vplayer.common.model.PlayState;
import net.kunmc.lab.vplayer.common.network.PacketContainer;
import net.kunmc.lab.vplayer.common.patch.VideoPatch;
import net.kunmc.lab.vplayer.common.patch.VideoPatchOperation;
import net.kunmc.lab.vplayer.common.util.Timer;
import net.kunmc.lab.vplayer.server.network.PacketDispatcherServer;
import net.kunmc.lab.vplayer.server.patch.VideoPatchRecieveEventServer;
import net.kunmc.lab.vplayer.server.patch.VideoPatchSendEventServer;
import net.kunmc.lab.vplayer.server.video.VDisplayManagerServer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyServer implements Listener {

    private static Server server;

    private static VDisplayManagerServer displayManager;

    public static Server getServer() {
        return server;
    }

    public static VDisplayManagerServer getDisplayManager() {
        return displayManager;
    }

    public void registerEvents(Plugin plugin) {
        // Register ourselves for server and other game events we are interested in
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Tick
        new BukkitRunnable() {
            @Override
            public void run() {
                onTick();
            }
        }.runTaskTimer(plugin, 0, 0);

        // Packet
        PacketDispatcherServer.register(plugin);
    }

    public void onServerStart(JavaPlugin plugin) {
        server = plugin.getServer();

        displayManager = VDisplayManagerServer.get(plugin.getDataFolder());
        displayManager.read();
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        getDisplayManager().write();
    }

    public void onTick() {
        Timer.tick();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        VDisplayManagerServer state = getDisplayManager();
        PacketContainer packet = new PacketContainer(VideoPatchOperation.SYNC, state.list().stream()
                .map(p -> new VideoPatch(p.getUUID(), p.getQuad(), p.fetchState())).collect(Collectors.toList()));
        PacketDispatcherServer.send(player, packet);
    }

    @EventHandler
    public void onServerPatchSend(VideoPatchSendEventServer event) {
        if (getServer() == null)
            return;

        PacketContainer packet = new PacketContainer(event.getOperation(), event.getPatches());
        getServer().getOnlinePlayers()
                .forEach(p -> PacketDispatcherServer.send(p, packet));
    }

    private final Table<UUID, UUID, Double> durationTable = HashBasedTable.create();

    @EventHandler
    public void onServerPatchReceive(VideoPatchRecieveEventServer event) {
        if (event.getOperation() == VideoPatchOperation.UPDATE) {
            VDisplayManagerServer state = getDisplayManager();

            event.getPatches().forEach(e -> {
                Optional.ofNullable(state.get(e.getId())).ifPresent(d -> {
                    UUID displayId = d.getUUID();
                    UUID playerId = event.getPlayer().getPlayerProfile().getId();
                    PlayState newState = e.getState();
                    if (newState != null && playerId != null) {
                        float duration = newState.duration;
                        if (duration >= 0)
                            durationTable.put(displayId, playerId, (double) duration);
                    }
                    durationTable.row(displayId).values().stream()
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet()
                            .stream()
                            .max(Map.Entry.comparingByValue())
                            .ifPresent(f -> {
                                float key = (float) (double) f.getKey();
                                PlayState playState = d.fetchState();
                                if (playState.duration != key) {
                                    playState.duration = key;
                                    d.dispatchState(playState);
                                }
                            });
                });
            });
        }
    }

}
