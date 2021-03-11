package net.kunmc.lab.vplayer.common.model;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Comparator;

public class Quad {
    public final World.Environment dimension;
    public final Vector[] vertices;

    public Quad(World.Environment dimensionIn, Vector... verticesIn) {
        Validate.isTrue(verticesIn.length >= 4, "VQuad needs 4 vertices");
        dimension = dimensionIn;
        vertices = verticesIn;
        Location loc;
    }

    public Vector getCenter() {
        return Arrays.stream(vertices).reduce((a, b) -> a.add(b).multiply(.5)).orElseGet(Vector::new);
    }

    public double getSize() {
        return Arrays.stream(vertices).findFirst().flatMap(p -> Arrays.stream(vertices).skip(1).map(p::distance).min(Comparator.naturalOrder())).orElse(Double.MAX_VALUE);
    }

    public double getNearestDistance(Vector from) {
        return Arrays.stream(vertices).map(from::distance).min(Comparator.naturalOrder()).orElse(Double.MAX_VALUE);
    }
}
