package net.kunmc.lab.vplayer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import net.kunmc.lab.vplayer.common.model.PlayState;
import net.kunmc.lab.vplayer.common.network.PacketContainer;
import net.kunmc.lab.vplayer.common.patch.VideoPatch;
import net.kunmc.lab.vplayer.common.patch.VideoPatchOperation;
import net.kunmc.lab.vplayer.common.util.Timer;
import net.kunmc.lab.vplayer.server.command.VPlayerCommand;
import net.kunmc.lab.vplayer.server.network.PacketDispatcherServer;
import net.kunmc.lab.vplayer.server.patch.VideoPatchRecieveEventServer;
import net.kunmc.lab.vplayer.server.patch.VideoPatchSendEventServer;
import net.kunmc.lab.vplayer.server.video.VDisplayManagerServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyServer implements Listener {

    private static Server server;

    private static Commodore commodore;

    public static Server getServer() {
        return server;
    }

    public static Commodore getCommodore() {
        return commodore;
    }

    public static VDisplayManagerServer getDisplayManager() {
        return VDisplayManagerServer.get(server.getWorld(DimensionType.OVERWORLD));
    }

    public void registerEvents(JavaPlugin plugin) {
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
        PacketDispatcherServer.register();
    }

    public void onServerStart(JavaPlugin plugin) {
        server = plugin.getServer();

        // check if brigadier is supported
        if (CommodoreProvider.isSupported()) {
            // get a commodore instance
            commodore = CommodoreProvider.getCommodore(plugin);
            // register your completions.
            VPlayerCommand.register(plugin::getCommand);
        }
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
        PacketDispatcherServer.send(((ServerPlayerEntity) player).connection.getNetworkManager(), packet);
    }

    @SubscribeEvent
    public void onServerPatchSend(VideoPatchSendEventServer event) {
        if (getServer() == null)
            return;

        PacketContainer packet = new PacketContainer(event.getOperation(), event.getPatches());
        getServer().getPlayerList().getPlayers().stream()
                .map(p -> p.connection)
                .filter(Objects::nonNull)
                .forEach(p -> PacketDispatcherServer.send(p.getNetworkManager(), packet));
    }

    private final Table<UUID, UUID, Double> durationTable = HashBasedTable.create();

    @SubscribeEvent
    public void onServerPatchReceive(VideoPatchRecieveEventServer event) {
        if (event.getOperation() == VideoPatchOperation.UPDATE) {
            VDisplayManagerServer state = getDisplayManager();

            event.getPatches().forEach(e -> {
                Optional.ofNullable(state.get(e.getId())).ifPresent(d -> {
                    UUID displayId = d.getUUID();
                    UUID playerId = event.getPlayer().getGameProfile().getId();
                    PlayState newState = e.getState();
                    if (newState != null) {
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
