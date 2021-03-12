package net.kunmc.lab.vplayer.common.util;

import net.kunmc.lab.vplayer.VideoPlayer;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.logging.Level;

public interface VNMS {
    <T> void register(String name, Class<T> type, Supplier<T> factory);

    static String getNMSVersion() {
        String v = Bukkit.getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    static VNMS getNMS() {
        try {
            String version = getNMSVersion();

            Class<?> serializerVoidClass = Class.forName("net.minecraft.server." + version + ".ArgumentSerializerVoid");
            Class<?> serializerClass = Class.forName("net.minecraft.server." + version + ".ArgumentSerializer");
            Class<?> registryClass = Class.forName("net.minecraft.server." + version + ".ArgumentRegistry");

            Constructor<?> serializerVoidConstructor = serializerVoidClass.getConstructor(Supplier.class);
            Method registryRegister = registryClass.getMethod("a", String.class, Class.class, serializerClass);

            return new VNMS() {
                @Override
                public <T> void register(String name, Class<T> type, Supplier<T> factory) {
                    try {
                        try {
                            Object serializerObject = serializerVoidConstructor.newInstance(factory);
                            registryRegister.invoke(null, name, type, serializerObject);
                        } catch (InvocationTargetException e) {
                            if (!(e.getCause() instanceof IllegalArgumentException))
                                throw e;
                        }
                    } catch (ReflectiveOperationException e) {
                        VideoPlayer.LOGGER.log(Level.WARNING, "NMS failed (ArgumentSerializer invoke): ", e);
                    }
                }
            };
        } catch (ReflectiveOperationException e) {
            VideoPlayer.LOGGER.log(Level.WARNING, "NMS failed (ArgumentSerializer init): ", e);
        }
        return null;
    }
}
