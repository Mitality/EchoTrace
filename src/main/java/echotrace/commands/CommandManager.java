package echotrace.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import echotrace.commands.subcommands.ReloadCommand;
import echotrace.commands.subcommands.TraceCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

public class CommandManager {

    public static LiteralCommandNode<CommandSourceStack> echotrace() {
        return Commands.literal("echotrace")
                .then(reload())
                .then(traceSub())
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reload() {
        return Commands.literal("reload")
                .requires(src -> src.getSender().hasPermission("echotrace.reload"))
                .executes(ReloadCommand::execute);
    }

    public static LiteralCommandNode<CommandSourceStack> trace() {
        return Commands.literal("trace")
                .then(traceArgs())
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> traceSub() {
        return Commands.literal("trace").then(traceArgs())
                .requires(src -> src.getSender().hasPermission("echotrace.trace"));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> traceArgs() {
        return Commands.argument("target", ArgumentTypes.player())
                .requires(src -> src.getExecutor() instanceof Player)
                .then(Commands.argument("points", IntegerArgumentType.integer(1))
                        .executes(ctx -> TraceCommand.execute(ctx, true)))
                .executes(ctx -> TraceCommand.execute(ctx, false));
    }
}
