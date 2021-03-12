package net.kunmc.lab.vplayer.server.command;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;

public class VTimeArgument extends Argument {
    protected VTimeArgument(String nodeName) {
        super(nodeName, VTimeArgumentType.timeArg());
    }

    @Override
    public Class<?> getPrimitiveType() {
        return VTimeArgumentType.VTime.class;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.CUSTOM;
    }
}
