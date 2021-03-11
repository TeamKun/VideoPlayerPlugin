package net.kunmc.lab.vplayer.server.command;

import com.google.common.base.Strings;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kunmc.lab.vplayer.ProxyServer;
import net.kunmc.lab.vplayer.common.model.Display;
import net.kunmc.lab.vplayer.common.model.Quad;
import net.kunmc.lab.vplayer.common.util.VUtils;
import net.kunmc.lab.vplayer.common.video.VDisplay;
import net.kunmc.lab.vplayer.server.video.VDisplayManagerServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class VPlayerCommand {
    public static void register(Function<String, PluginCommand> commandSupplier) {
        ProxyServer.getCommodore().register(commandSupplier.apply("vdisplay"),
                LiteralArgumentBuilder.literal("vdisplay")
                        .then(LiteralArgumentBuilder.literal("list")
                                .executes(ctx -> {
                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();

                                    BaseComponent[] component = state.listNames().stream()
                                            .map(msg -> {
                                                ComponentBuilder text = new ComponentBuilder(msg);
                                                Optional.ofNullable(state.get(msg))
                                                        .map(Display::getQuad)
                                                        .ifPresent(e -> {
                                                            Vector vec = e.vertices[0];
                                                            text.event(new HoverEvent(
                                                                    HoverEvent.Action.SHOW_TEXT,
                                                                    new ComponentBuilder(String.format("クリックでTP: %s(x:%.1f, y:%.1f, z:%.1f)", msg, vec.getX(), vec.getY(), vec.getZ())).create()
                                                            ));
                                                            text.event(new ClickEvent(
                                                                    ClickEvent.Action.RUN_COMMAND,
                                                                    String.format("/tp %f %f %f", vec.getX(), vec.getY(), vec.getZ())
                                                            ));
                                                        });
                                                return text.create();
                                            })
                                            .collect(VUtils.joining(new ComponentBuilder(", ").create()));

                                    CommandSender sender = VUtils.getSender(ctx.getSource());
                                    sender.sendMessage(
                                            new ComponentBuilder().color(ChatColor.GREEN)
                                                    .append(new ComponentBuilder("[かめすたMod] ").color(ChatColor.LIGHT_PURPLE).create())
                                                    .append(component)
                                                    .create()
                                    );

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(LiteralArgumentBuilder.literal("create")
                                .then(name()
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");

                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                            VDisplay display = state.create(name);

                                            Location location = VUtils.getLocation(VUtils.getSender(ctx.getSource()));
                                            if (location != null)
                                                state.setQuad(name, getQuad(ctx, display.getQuad(), location.toVector(), null, true, .1));

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.literal("destroy")
                                .then(name()
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");

                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                            state.destroy(name);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )

                        )
                        .then(LiteralArgumentBuilder.literal("position")
                                .then(name()
                                        .then(LiteralArgumentBuilder.literal("pos1")
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();

                                                    VDisplay display = state.get(name);
                                                    if (display == null)
                                                        throw new CommandException("ディスプレイが見つかりません。");

                                                    Location location = VUtils.getLocation(VUtils.getSender(ctx.getSource()));
                                                    if (location != null)
                                                        state.setQuad(name, getQuad(ctx, display.getQuad(), location.toVector(), null, true, .1));

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(RequiredArgumentBuilder.argument("pos", Vec3Argument.vec3())
                                                        .executes(ctx -> {
                                                            String name = StringArgumentType.getString(ctx, "name");
                                                            ILocationArgument pos = Vec3Argument.getLocation(ctx, "pos");

                                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();

                                                            VDisplay display = state.get(name);
                                                            if (display == null)
                                                                throw new CommandException("ディスプレイが見つかりません。");

                                                            Location eyeLocation = VUtils.getEyeLocation(VUtils.getSender(ctx.getSource()));
                                                            if (eyeLocation != null)
                                                                state.setQuad(name, getQuad(ctx, display.getQuad(), pos.getPosition(eyeLocation), null, false, .1));

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                        .then(LiteralArgumentBuilder.literal("pos2")
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();

                                                    VDisplay display = state.get(name);
                                                    if (display == null)
                                                        throw new CommandException("ディスプレイが見つかりません。");

                                                    Location location = VUtils.getLocation(VUtils.getSender(ctx.getSource()));
                                                    if (location != null)
                                                        state.setQuad(name, getQuad(ctx, display.getQuad(), null, location.toVector(), true, .1));

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(RequiredArgumentBuilder.argument("pos", Vec3Argument.vec3())
                                                        .executes(ctx -> {
                                                            String name = StringArgumentType.getString(ctx, "name");
                                                            ILocationArgument pos = Vec3Argument.getLocation(ctx, "pos");

                                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();

                                                            VDisplay display = state.get(name);
                                                            if (display == null)
                                                                throw new CommandException("ディスプレイが見つかりません。");

                                                            Location eyeLocation = VUtils.getEyeLocation(VUtils.getSender(ctx.getSource()));
                                                            if (eyeLocation != null)
                                                                state.setQuad(name, getQuad(ctx, display.getQuad(), null, pos.getPosition(eyeLocation), false, .1));

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )

                        )
        );
        ProxyServer.getCommodore().register(commandSupplier.apply("vplayer"),
                LiteralArgumentBuilder.literal("vplayer")
                        .then(name()
                                .then(LiteralArgumentBuilder.literal("video")
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");

                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();

                                            VDisplay display = state.get(name);
                                            if (display == null)
                                                throw new CommandException("ディスプレイが見つかりません。");

                                            CommandSender sender = VUtils.getSender(ctx.getSource());
                                            sender.sendMessage(
                                                    new ComponentBuilder().color(ChatColor.GREEN)
                                                            .append(new ComponentBuilder("[かめすたMod] ").color(ChatColor.LIGHT_PURPLE).create())
                                                            .append(new ComponentBuilder(Strings.nullToEmpty(display.fetchState().file)).create())
                                                            .create()
                                            );

                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(RequiredArgumentBuilder.argument("url", StringArgumentType.string())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String url = StringArgumentType.getString(ctx, "url");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                                    state.dispatchState(name, s -> {
                                                        s.file = url;
                                                        s.time = 0;
                                                        s.paused = true;
                                                        return s;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(LiteralArgumentBuilder.literal("play")
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");

                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                            state.dispatchState(name, s -> {
                                                s.time = 0;
                                                s.paused = false;
                                                return s;
                                            });

                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(RequiredArgumentBuilder.argument("url", StringArgumentType.string())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String url = StringArgumentType.getString(ctx, "url");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                                    state.dispatchState(name, s -> {
                                                        s.file = url;
                                                        s.time = 0;
                                                        s.paused = false;
                                                        return s;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(LiteralArgumentBuilder.literal("pause")
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");

                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                            state.dispatchState(name, s -> {
                                                s.paused = !s.paused;
                                                return s;
                                            });

                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(RequiredArgumentBuilder.argument("paused", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    boolean paused = BoolArgumentType.getBool(ctx, "paused");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                                    state.dispatchState(name, s -> {
                                                        s.paused = paused;
                                                        return s;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(LiteralArgumentBuilder.literal("stop")
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");

                                            VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                            state.dispatchState(name, s -> {
                                                s.file = null;
                                                s.paused = true;
                                                return s;
                                            });

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(LiteralArgumentBuilder.literal("seek")
                                        .then(RequiredArgumentBuilder.argument("time", VTimeArgumentType.timeArg())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    VTimeArgumentType.VTime time = VTimeArgumentType.getTime(ctx, "time");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                                    state.dispatchState(name, s -> {
                                                        if (s.duration > 0)
                                                            s.time = VUtils.clamp(s.time, 0, s.duration) + VUtils.clamp(time.getTime(s.duration), 0, s.duration);
                                                        else
                                                            s.time += time.getTime(s.duration);
                                                        return s;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                                .then(LiteralArgumentBuilder.literal("time")
                                        .then(RequiredArgumentBuilder.argument("time", VTimeArgumentType.timeArg())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    VTimeArgumentType.VTime time = VTimeArgumentType.getTime(ctx, "time");

                                                    VDisplayManagerServer state = ProxyServer.getDisplayManager();
                                                    state.dispatchState(name, s -> {
                                                        s.time = time.getTime(s.duration);
                                                        return s;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
        );
    }

    @Nonnull
    public static Quad getQuad(CommandContext<Object> context, @Nullable Quad prev, @Nullable Vector pos1, @Nullable Vector pos2, boolean align, double padding) {
        CommandSender sender = VUtils.getSender(context.getSource());

        Location location = (sender instanceof Entity)
                ? ((Entity) sender).getLocation()
                : (sender instanceof BlockCommandSender)
                ? ((BlockCommandSender) sender).getBlock().getLocation()
                : null;

        World.Environment dimension = Optional.ofNullable(prev)
                .map(e -> e.dimension)
                .orElseGet(() ->
                        Optional.ofNullable(location)
                                .map(e -> e.getWorld().getEnvironment())
                                .orElse(World.Environment.NORMAL)
                );

        if (pos1 == null && pos2 == null)
            pos1 = Optional.ofNullable(location).map(Location::toVector).orElseGet(Vector::new);

        if (prev != null) {
            if (pos1 == null)
                pos1 = prev.vertices[0];
            else if (pos2 == null)
                pos2 = prev.vertices[2];
        } else {
            if (pos1 == null)
                pos1 = pos2.subtract(new Vector(16, -9, 0));
            else if (pos2 == null)
                pos2 = pos1.add(new Vector(16, -9, 0));
        }

        double offset = .01;
        if (align) {
            double p1x = pos1.getX() < pos2.getX() ? Math.floor(pos1.getX()) + padding : Math.ceil(pos1.getX()) - padding;
            double p2x = pos1.getX() > pos2.getX() ? Math.floor(pos2.getX()) + padding : Math.ceil(pos2.getX()) - padding;
            double p1y = pos1.getY() < pos2.getY() ? Math.floor(pos1.getY()) + padding : Math.ceil(pos1.getY()) - padding;
            double p2y = pos1.getY() > pos2.getY() ? Math.floor(pos2.getY()) + padding : Math.ceil(pos2.getY()) - padding;
            double p1z = pos1.getZ() < pos2.getZ() ? Math.floor(pos1.getZ()) + padding : Math.ceil(pos1.getZ()) - padding;
            double p2z = pos1.getZ() > pos2.getZ() ? Math.floor(pos2.getZ()) + padding : Math.ceil(pos2.getZ()) - padding;

            BlockVector b1 = pos1.toBlockVector();
            BlockVector b2 = pos2.toBlockVector();
            if (b1.getX() == b2.getX()) {
                if (pos1.getZ() > pos2.getZ())
                    p1x = p2x = b1.getX() + offset;
                else
                    p1x = p2x = b1.getX() + (1 - offset);
            } else if (b1.getZ() == b2.getZ()) {
                if (pos1.getX() < pos2.getX())
                    p1z = p2z = b1.getZ() + offset;
                else
                    p1z = p2z = b1.getZ() + (1 - offset);
            }

            pos1 = new Vector(p1x, p1y, p1z);
            pos2 = new Vector(p2x, p2y, p2z);
        }

        return new Quad(
                dimension,
                new Vector(pos1.getX(), pos1.getY(), pos1.getZ()),  // left top
                new Vector(pos1.getX(), pos2.getY(), pos1.getZ()),  // left bottom
                new Vector(pos2.getX(), pos2.getY(), pos2.getZ()),  // right bottom
                new Vector(pos2.getX(), pos1.getY(), pos2.getZ())   // right top
        );
    }

    public static RequiredArgumentBuilder<Object, String> name() {
        return RequiredArgumentBuilder.argument("name", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    VDisplayManagerServer state = ProxyServer.getDisplayManager();

                    state.listNames().forEach(builder::suggest);

                    return builder.buildFuture();
                });
    }
}
