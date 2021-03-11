package net.kunmc.lab.vplayer.server.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VTimeArgumentType implements ArgumentType<VTimeArgumentType.VTime> {
    private static final Collection<String> unitDefaults = Arrays.asList("0d", "0s", "0t", "0");
    private static final SimpleCommandExceptionType exceptionInvalidUnit = new SimpleCommandExceptionType(new LiteralMessage("単位が不正です"));
    private static final DynamicCommandExceptionType exceptionTickCount = new DynamicCommandExceptionType(count -> new LiteralMessage(String.format("%s は範囲外の値です", count)));
    private static final Map<String, Float> units = new HashMap<>();

    public static VTimeArgumentType timeArg() {
        return new VTimeArgumentType();
    }

    public static VTime getTime(final CommandContext<?> context, final String name) {
        return context.getArgument(name, VTime.class);
    }

    public VTime parse(StringReader text) throws CommandSyntaxException {
        float f = text.readFloat();
        if (text.canRead() && text.peek() == '%') {
            text.readStringUntil('%');
            if (f < 0 || f > 100)
                throw exceptionTickCount.create(f);
            return new VTime(VTimeType.PERCENT, f);
        } else {
            float sec = 0;
            while (true) {
                String s = text.readUnquotedString();
                float i = units.getOrDefault(s, 0f);
                if (i == 0)
                    throw exceptionInvalidUnit.create();

                sec += f * i;

                text.skipWhitespace();
                if (text.canRead())
                    f = text.readFloat();
                else
                    break;
            }
            return new VTime(VTimeType.SECONDS, sec);
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder sb) {
        StringReader stringreader = new StringReader(sb.getRemaining());

        try {
            stringreader.readFloat();
        } catch (CommandSyntaxException var5) {
            return sb.buildFuture();
        }

        return suggest(units.keySet(), sb.createOffset(sb.getStart() + stringreader.getCursor()));
    }

    public Collection<String> getExamples() {
        return unitDefaults;
    }

    static {
        units.put("h", 60f * 60f);
        units.put("m", 60f);
        units.put("s", 1f);
        units.put("%", 0f);
        units.put("", 1f);
    }

    public static class VTime {
        public final VTimeType type;
        public final float value;

        public VTime(VTimeType type, float value) {
            this.type = type;
            this.value = value;
        }

        public float getTime(float duration) {
            if (type == VTimeType.PERCENT)
                if (duration <= 0)
                    return 0;
                else
                    return value / 100f * duration;
            return value;
        }
    }

    public enum VTimeType {
        SECONDS,
        PERCENT,
    }

    private static CompletableFuture<Suggestions> suggest(Iterable<String> strings, SuggestionsBuilder builder) {
        String s = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (String s1 : strings) {
            if (s1.toLowerCase(Locale.ROOT).startsWith(s)) {
                builder.suggest(s1);
            }
        }

        return builder.buildFuture();
    }
}
