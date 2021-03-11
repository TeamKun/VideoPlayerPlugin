package net.kunmc.lab.vplayer.common.data;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.World;

import java.io.IOException;

public class DimensionTypeAdaptor extends TypeAdapter<World.Environment> {
    @Override
    public void write(JsonWriter out, World.Environment value) throws IOException {
        out.beginObject();
        out.name("id").value(value.getId());
        out.endObject();
    }

    @Override
    public World.Environment read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        int id = in.nextInt();
        in.endObject();
        return World.Environment.getEnvironment(id);
    }
}
