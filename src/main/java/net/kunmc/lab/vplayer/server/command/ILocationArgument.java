package net.kunmc.lab.vplayer.server.command;

import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public interface ILocationArgument {
    Vector getPosition(Location source);

    Vector getRotation(Location source);

    default BlockVector getBlockPos(Location source) {
        return new BlockVector(this.getPosition(source));
    }

    boolean isXRelative();

    boolean isYRelative();

    boolean isZRelative();
}
