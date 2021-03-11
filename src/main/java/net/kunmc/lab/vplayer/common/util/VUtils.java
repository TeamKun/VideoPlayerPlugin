package net.kunmc.lab.vplayer.common.util;

import net.kunmc.lab.vplayer.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.stream.Collector;

public class VUtils {
    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    public static CommandSender getSender(Object source) {
        return ProxyServer.getCommodore().getBukkitSender(source);
    }

    public static Location getLocation(CommandSender sender) {
        return (sender instanceof Entity)
                ? ((Entity) sender).getLocation()
                : (sender instanceof BlockCommandSender)
                ? ((BlockCommandSender) sender).getBlock().getLocation()
                : null;
    }

    public static Location getEyeLocation(CommandSender sender) {
        return (sender instanceof LivingEntity)
                ? ((Player) sender).getEyeLocation()
                : getLocation(sender);
    }

    public static Collector<BaseComponent[], ?, BaseComponent[]> joining() {
        return Collector.of(
                ComponentBuilder::new,
                ComponentBuilder::append,
                (r1, r2) -> r1.append(r2.create()),
                ComponentBuilder::create);
    }

    public static Collector<BaseComponent[], ?, BaseComponent[]> joining(BaseComponent[] joiner) {
        return Collector.of(
                ComponentBuilder::new,
                (r1, r2) -> {
                    if (!r1.getParts().isEmpty())
                        r1.append(joiner);
                    r1.append(r2);
                },
                (r1, r2) -> r1.append(r2.create()),
                ComponentBuilder::create);
    }
}
