package net.kunmc.lab.vplayer.server.patch;

import net.kunmc.lab.vplayer.common.patch.VideoPatch;
import net.kunmc.lab.vplayer.common.patch.VideoPatchEvent;
import net.kunmc.lab.vplayer.common.patch.VideoPatchOperation;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.List;

public class VideoPatchRecieveEventServer extends VideoPatchEvent {
    private Player player;

    public VideoPatchRecieveEventServer(VideoPatchOperation operation, List<VideoPatch> patches, Player player) {
        super(operation, patches);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    private static final HandlerList handlers = new HandlerList();

    @Nonnull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
