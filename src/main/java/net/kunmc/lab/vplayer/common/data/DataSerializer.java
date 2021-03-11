package net.kunmc.lab.vplayer.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.World;

import javax.annotation.Nullable;

public class DataSerializer {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(World.Environment.class, new DimensionTypeAdaptor())
            .create();

    public static String encode(Object object) {
        return gson.toJson(object);
    }

    public static void encode(Object object, JsonWriter writer) {
        if (object == null)
            gson.toJson(JsonNull.INSTANCE, writer);
        else
            gson.toJson(object, object.getClass(), writer);
    }

    @Nullable
    public static <T> T decode(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static <T> T decode(JsonReader json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }
}
