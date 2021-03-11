package net.kunmc.lab.vplayer.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LocationInput implements ILocationArgument {
    private final LocationPart x;
    private final LocationPart y;
    private final LocationPart z;

    public LocationInput(LocationPart x, LocationPart y, LocationPart z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector getPosition(Location source) {
        Vector vec3d = source.toVector();
        return new Vector(this.x.get(vec3d.getX()), this.y.get(vec3d.getY()), this.z.get(vec3d.getZ()));
    }

    public Vector getRotation(Location source) {
        Vector vec2f = new Vector(source.getYaw(), source.getPitch(), 0);
        return new Vector((float) this.x.get((double) vec2f.getX()), (float) this.y.get((double) vec2f.getY()), 0);
    }

    public boolean isXRelative() {
        return this.x.isRelative();
    }

    public boolean isYRelative() {
        return this.y.isRelative();
    }

    public boolean isZRelative() {
        return this.z.isRelative();
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof LocationInput)) {
            return false;
        } else {
            LocationInput locationinput = (LocationInput) p_equals_1_;
            if (!this.x.equals(locationinput.x)) {
                return false;
            } else {
                return !this.y.equals(locationinput.y) ? false : this.z.equals(locationinput.z);
            }
        }
    }

    public static LocationInput parseInt(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        LocationPart locationpart = LocationPart.parseInt(reader);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            LocationPart locationpart1 = LocationPart.parseInt(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                LocationPart locationpart2 = LocationPart.parseInt(reader);
                return new LocationInput(locationpart, locationpart1, locationpart2);
            } else {
                reader.setCursor(i);
                throw Vec3Argument.POS_INCOMPLETE.createWithContext(reader);
            }
        } else {
            reader.setCursor(i);
            throw Vec3Argument.POS_INCOMPLETE.createWithContext(reader);
        }
    }

    public static LocationInput parseDouble(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
        int i = reader.getCursor();
        LocationPart locationpart = LocationPart.parseDouble(reader, centerIntegers);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            LocationPart locationpart1 = LocationPart.parseDouble(reader, false);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                LocationPart locationpart2 = LocationPart.parseDouble(reader, centerIntegers);
                return new LocationInput(locationpart, locationpart1, locationpart2);
            } else {
                reader.setCursor(i);
                throw Vec3Argument.POS_INCOMPLETE.createWithContext(reader);
            }
        } else {
            reader.setCursor(i);
            throw Vec3Argument.POS_INCOMPLETE.createWithContext(reader);
        }
    }

    /**
     * A location with a delta of 0 for all values (equivalent to <code>~ ~ ~</code> or <code>~0 ~0 ~0</code>)
     */
    public static LocationInput current() {
        return new LocationInput(new LocationPart(true, 0.0D), new LocationPart(true, 0.0D), new LocationPart(true, 0.0D));
    }

    public int hashCode() {
        int i = this.x.hashCode();
        i = 31 * i + this.y.hashCode();
        i = 31 * i + this.z.hashCode();
        return i;
    }
}
