package echotrace.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import echotrace.config.Config;
import echotrace.config.Lang;
import echotrace.core.Trace;
import echotrace.core.TraceManager;
import echotrace.core.target.PlayerTarget;
import echotrace.depend.VanishPlugins;
import echotrace.util.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TraceCommand {

    public static int execute(CommandContext<CommandSourceStack> ctx, boolean hasPointsArg) {

        final CommandSourceStack src = ctx.getSource();
        if (!(src.getExecutor() instanceof Player player)) return SINGLE_SUCCESS; // shouldn't be possible to achieve
        final PlayerSelectorArgumentResolver r = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);

        Player target;
        try {
            target = r.resolve(src).getFirst();
        } catch (CommandSyntaxException e) {
            target = null;
        }

        if (target == null || VanishPlugins.isVanished(target)) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_no_target);
            return SINGLE_SUCCESS;
        }
        if (target.getWorld() != player.getWorld()) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_wrong_world.replace("{Player}", target.getName()));
            return SINGLE_SUCCESS;
        }

        int pointCount = hasPointsArg ? IntegerArgumentType.getInteger(ctx, "points") : Config.default_points;

        TraceManager.registerTrace(new Trace(player, new PlayerTarget(target), pointCount));
        MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_success);
        return SINGLE_SUCCESS;
    }
}
