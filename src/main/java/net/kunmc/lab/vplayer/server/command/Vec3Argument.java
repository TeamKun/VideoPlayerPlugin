package net.kunmc.lab.vplayer.server.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kunmc.lab.vplayer.common.util.VUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class Vec3Argument implements ArgumentType<ILocationArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
    public static final SimpleCommandExceptionType POS_INCOMPLETE = new SimpleCommandExceptionType(new LiteralMessage("座標が足りません"));
    public static final SimpleCommandExceptionType POS_MIXED_TYPES = new SimpleCommandExceptionType(new LiteralMessage("座標の種類が混在しています"));
    private final boolean centerIntegers;

    public Vec3Argument(boolean centerIntegersIn) {
        this.centerIntegers = centerIntegersIn;
    }

    public static Vec3Argument vec3() {
        return new Vec3Argument(true);
    }

    public static Vec3Argument vec3(boolean centerIntegersIn) {
        return new Vec3Argument(centerIntegersIn);
    }

    public static Vector getVec3(CommandContext<Object> context, String name) throws CommandSyntaxException {
        CommandSender sender = VUtils.getSender(context);
        return context.getArgument(name, ILocationArgument.class).getPosition(VUtils.getEyeLocation(sender));
    }

    public static ILocationArgument getLocation(CommandContext<Object> context, String name) {
        return context.getArgument(name, ILocationArgument.class);
    }

    public ILocationArgument parse(StringReader p_parse_1_) throws CommandSyntaxException {
        return (ILocationArgument) (p_parse_1_.canRead() && p_parse_1_.peek() == '^' ? LocalLocationArgument.parse(p_parse_1_) : LocationInput.parseDouble(p_parse_1_, this.centerIntegers));
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_listSuggestions_1_, SuggestionsBuilder p_listSuggestions_2_) {
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
