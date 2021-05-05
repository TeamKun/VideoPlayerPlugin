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
        String name = null;
        int id = 0;
        while (in.hasNext()) {
            String type = in.nextName();
            switch (type) {
                case "name":
                    name = in.nextString();
                    break;
                case "id":
                    id = in.nextInt();
                    break;
            }
        }
        in.endObject();
        return World.Environment.getEnvironment(id);
    }
}
