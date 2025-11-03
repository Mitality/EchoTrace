package echotrace.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import echotrace.commands.subcommands.ReloadCommand;
import echotrace.commands.subcommands.TraceCommand;
import echotrace.config.Config;
import echotrace.depend.VanishPlugins;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Bukkit;
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
                .requires(src -> src.getExecutor() instanceof Player)
                .then(entityTraceArgs())
                .then(playerTraceArgs())
                .then(positionTraceArgs())
                .then(blockTraceArgs())
                .then(cancelTraceArgs())
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> traceSub() {
        return Commands.literal("trace")
                .requires(src -> src.getExecutor() instanceof Player)
                .then(entityTraceArgs())
                .then(playerTraceArgs())
                .then(positionTraceArgs())
                .then(blockTraceArgs())
                .then(cancelTraceArgs());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> entityTraceArgs() {
        return Commands.literal("entity")
                .requires(src -> src.getSender().hasPermission("echotrace.trace.entities"))
                .then(Commands.argument("entity", ArgumentTypes.entities())
                        .then(Commands.argument("points", LongArgumentType.longArg(1))
                                .executes(ctx -> TraceCommand.executeEntity(ctx, true)))
                        .executes(ctx -> TraceCommand.executeEntity(ctx, false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> playerTraceArgs() {
        return Commands.literal("player")
                .requires(src -> src.getSender().hasPermission("echotrace.trace.players"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, b) -> {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (!VanishPlugins.isVanished(p)) b.suggest(p.getName());
                            }
                            return b.buildFuture();
                        })
                        .then(Commands.argument("points", LongArgumentType.longArg(1))
                                .executes(ctx -> TraceCommand.executePlayer(ctx, true)))
                        .executes(ctx -> TraceCommand.executePlayer(ctx, false)));
    }


    private static LiteralArgumentBuilder<CommandSourceStack> positionTraceArgs() {
        return Commands.literal("position")
                .requires(src -> src.getSender().hasPermission("echotrace.trace.positions"))
                .then(Commands.argument("position", ArgumentTypes.finePosition(true))
                        .then(Commands.argument("points", LongArgumentType.longArg(1))
                                .executes(ctx -> TraceCommand.executePosition(ctx, true)))
                        .executes(ctx -> TraceCommand.executePosition(ctx, false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> blockTraceArgs() {
        return Commands.literal("block")
                .requires(src -> src.getSender().hasPermission("echotrace.trace.blocks"))
                .then(Commands.argument("type", ArgumentTypes.blockState())
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0, Math.max(1, Config.max_radius)))
                                .then(Commands.argument("limit", LongArgumentType.longArg(1))
                                        .then(Commands.argument("points", LongArgumentType.longArg(1))
                                                .executes(ctx -> TraceCommand.executeBlock(ctx, true, true, true)))
                                        .executes(ctx -> TraceCommand.executeBlock(ctx, true, true, false)))
                                .executes(ctx -> TraceCommand.executeBlock(ctx, true, false, false)))
                        .executes(ctx -> TraceCommand.executeBlock(ctx, false, false, false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cancelTraceArgs() {
        return Commands.literal("cancel").executes(TraceCommand::cancel);
    }
}
