package net.kunmc.lab.vplayer.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;

public class LocalLocationArgument implements ILocationArgument {
    private final double left;
    private final double up;
    private final double forwards;

    public LocalLocationArgument(double leftIn, double upIn, double forwardsIn) {
        this.left = leftIn;
        this.up = upIn;
        this.forwards = forwardsIn;
    }

    public Vector getPosition(Location source) {
        Vector vec2f = new Vector(source.getYaw(), source.getPitch(), 0);
        Vector vec3d = source.toVector();
        float f = (float) Math.cos((vec2f.getY() + 90.0F) * ((float) Math.PI / 180F));
        float f1 = (float) Math.sin((vec2f.getY() + 90.0F) * ((float) Math.PI / 180F));
        float f2 = (float) Math.cos(-vec2f.getX() * ((float) Math.PI / 180F));
        float f3 = (float) Math.sin(-vec2f.getX() * ((float) Math.PI / 180F));
        float f4 = (float) Math.cos((-vec2f.getX() + 90.0F) * ((float) Math.PI / 180F));
        float f5 = (float) Math.sin((-vec2f.getX() + 90.0F) * ((float) Math.PI / 180F));
        Vector vec3d1 = new Vector((double) (f * f2), (double) f3, (double) (f1 * f2));
        Vector vec3d2 = new Vector((double) (f * f4), (double) f5, (double) (f1 * f4));
        Vector vec3d3 = vec3d1.clone().crossProduct(vec3d2).multiply(-1.0D);
        double d0 = vec3d1.getX() * this.forwards + vec3d2.getX() * this.up + vec3d3.getX() * this.left;
        double d1 = vec3d1.getY() * this.forwards + vec3d2.getY() * this.up + vec3d3.getY() * this.left;
        double d2 = vec3d1.getZ() * this.forwards + vec3d2.getZ() * this.up + vec3d3.getZ() * this.left;
        return new Vector(vec3d.getX() + d0, vec3d.getY() + d1, vec3d.getZ() + d2);
    }

    public Vector getRotation(Location source) {
        return new Vector();
    }

    public boolean isXRelative() {
        return true;
    }

    public boolean isYRelative() {
        return true;
    }

    public boolean isZRelative() {
        return true;
    }

    public static LocalLocationArgument parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        double d0 = parseCoord(reader, i);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            double d1 = parseCoord(reader, i);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                double d2 = parseCoord(reader, i);
                return new LocalLocationArgument(d0, d1, d2);
            } else {
                reader.setCursor(i);
                throw Vec3Argument.POS_INCOMPLETE.createWithContext(reader);
            }
        } else {
            reader.setCursor(i);
            throw Vec3Argument.POS_INCOMPLETE.createWithContext(reader);
        }
    }

    private static double parseCoord(StringReader reader, int start) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw LocationPart.EXPECTED_DOUBLE.createWithContext(reader);
        } else if (reader.peek() != '^') {
            reader.setCursor(start);
            throw Vec3Argument.POS_MIXED_TYPES.createWithContext(reader);
        } else {
            reader.skip();
            return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0D;
        }
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof LocalLocationArgument)) {
            return false;
        } else {
            LocalLocationArgument locallocationargument = (LocalLocationArgument) p_equals_1_;
            return this.left == locallocationargument.left && this.up == locallocationargument.up && this.forwards == locallocationargument.forwards;
        }
    }

    public int hashCode() {
        return Objects.hash(this.left, this.up, this.forwards);
    }
}
