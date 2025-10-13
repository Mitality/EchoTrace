package echotrace.commands.subcommands;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import echotrace.Main;
import echotrace.config.Config;
import echotrace.config.Lang;
import echotrace.core.Trace;
import echotrace.core.TraceManager;
import echotrace.core.target.BlockTarget;
import echotrace.core.target.EntityTarget;
import echotrace.core.target.PlayerTarget;
import echotrace.core.target.PositionTarget;
import echotrace.util.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class TraceCommand {

    private static final Map<UUID, List<MyScheduledTask>> ACTIVE = new ConcurrentHashMap<>();

    private static void addTask(UUID uuid, MyScheduledTask task) {
        ACTIVE.computeIfAbsent(uuid, k -> Collections.synchronizedList(new ArrayList<>())).add(task);
    }

    private static void removeTask(UUID uuid, MyScheduledTask task) {
        List<MyScheduledTask> list = ACTIVE.get(uuid);
        if (list != null) {
            list.remove(task);
            if (list.isEmpty()) ACTIVE.remove(uuid);
        }
    }

    private static void cancelAll(UUID uuid) {
        List<MyScheduledTask> list = ACTIVE.remove(uuid);
        if (list == null || list.isEmpty()) return;
        for (MyScheduledTask task : list) {
            task.cancel();
        }
    }

    public static int executeEntity(CommandContext<CommandSourceStack> ctx, boolean hasPointsArg) {

        final CommandSourceStack src = ctx.getSource();
        final Player player = (Player) src.getExecutor();
        if (player == null) return SINGLE_SUCCESS;

        final EntitySelectorArgumentResolver resolver = ctx
                .getArgument("entity", EntitySelectorArgumentResolver.class);

        try {
            int pointCount = hasPointsArg ?
                    IntegerArgumentType.getInteger(ctx, "points") : Config.default_points;

            final List<Entity> targets = resolver.resolve(src);

            int count = 0;
            boolean ww = false;
            for (Entity target : targets) {
                if (player.getWorld() != target.getWorld()) {
                    ww = true;
                    continue;
                }
                TraceManager.registerTrace(new Trace(player, new EntityTarget(target), pointCount));
                count++;
            }

            if (count > 0) MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_success
                    .replace("{Count}",  String.valueOf(count)));
            else if (ww) MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_wrong_world);
            else MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_no_target);
            return SINGLE_SUCCESS;

        } catch (CommandSyntaxException e) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_no_target);
            return SINGLE_SUCCESS;
        }
    }

    public static int executePlayer(CommandContext<CommandSourceStack> ctx, boolean hasPointsArg) {

        final CommandSourceStack src = ctx.getSource();
        final Player player = (Player) src.getExecutor();
        if (player == null) return SINGLE_SUCCESS;

        final String input = StringArgumentType.getString(ctx, "player").trim();

        final Player target = Bukkit.getPlayer(input);
        if (target == null) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_no_target);
            return SINGLE_SUCCESS;
        }
        if (!player.getWorld().equals(target.getWorld())) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_wrong_world);
            return SINGLE_SUCCESS;
        }

        final int pointCount = hasPointsArg ? IntegerArgumentType.getInteger(ctx, "points") : Config.default_points;

        TraceManager.registerTrace(new Trace(player, new PlayerTarget(target), pointCount));
        MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_success
                .replace("{Count}",  "1"));
        return SINGLE_SUCCESS;
    }

    public static int executePosition(CommandContext<CommandSourceStack> ctx, boolean hasPointsArg) {

        final CommandSourceStack src = ctx.getSource();
        final Player player = (Player) src.getExecutor();
        if (player == null) return SINGLE_SUCCESS;

        FinePositionResolver resolver = ctx
                .getArgument("position", FinePositionResolver.class);

        try {
            int pointCount = hasPointsArg ?
                    IntegerArgumentType.getInteger(ctx, "points") : Config.default_points;

            FinePosition target = resolver.resolve(src);

            TraceManager.registerTrace(new Trace(player, new PositionTarget(target.toLocation(player.getWorld())), pointCount));

            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_success
                    .replace("{Count}",  "1"));
            return SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_no_target);
            return SINGLE_SUCCESS;
        }
    }

    public static int executeBlock(CommandContext<CommandSourceStack> ctx, boolean hasRadius, boolean hasLimit, boolean hasPointsArg) {

        final CommandSourceStack src = ctx.getSource();
        final Player player = (Player) src.getExecutor();
        if (player == null) return SINGLE_SUCCESS;
        final UUID uuid = player.getUniqueId();

        String raw = rawArg(ctx, "type");
        if (raw == null || raw.isEmpty()) return SINGLE_SUCCESS;

        int nbtIdx = raw.indexOf("{"); // not supported (no nms)
        if (nbtIdx != -1) raw = raw.substring(0, nbtIdx).trim();

        final BlockData pattern;
        try {
            String s = raw.contains(":") ? raw : "minecraft:" + raw;
            pattern = Bukkit.createBlockData(s);
        } catch (IllegalArgumentException ex) {
            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_no_target);
            return SINGLE_SUCCESS;
        }

        final Material wantMat = pattern.getMaterial();
        final boolean hasProps = pattern.getAsString(false).contains("[");

        final double radius = hasRadius ? DoubleArgumentType.getDouble(ctx, "radius") : 25.0;
        final int limit = Math.max(1, hasLimit ? IntegerArgumentType.getInteger(ctx, "limit") : 1);
        final int points = hasPointsArg ? IntegerArgumentType.getInteger(ctx, "points") : Config.default_points;

        final var world = player.getWorld();
        final var loc = player.getLocation();
        final double cx = loc.getX(), cy = loc.getY(), cz = loc.getZ();
        final double r2 = radius * radius;
        final int R = (int) Math.ceil(radius);
        final int bx = (int) Math.floor(cx), by = (int) Math.floor(cy), bz = (int) Math.floor(cz);
        final int minY = Math.max(world.getMinHeight(), (int) Math.floor(cy - radius));
        final int maxY = Math.min(world.getMaxHeight() - 1, (int) Math.floor(cy + radius));

        Main.getScheduler().runTaskAsynchronously(() -> {
            final ArrayList<int[]> offsets = new ArrayList<>((2 * R + 1) * (2 * R + 1) * (2 * R + 1));
            for (int dy = -R; dy <= R; dy++) {
                for (int dx = -R; dx <= R; dx++) {
                    for (int dz = -R; dz <= R; dz++) {
                        offsets.add(new int[]{dx, dy, dz});
                    }
                }
            }

            offsets.sort((a, b) -> {
                double adx = (bx + a[0] + 0.5) - cx;
                double ady = (by + a[1] + 0.5) - cy;
                double adz = (bz + a[2] + 0.5) - cz;
                double bdx = (bx + b[0] + 0.5) - cx;
                double bdy = (by + b[1] + 0.5) - cy;
                double bdz = (bz + b[2] + 0.5) - cz;
                double a2 = adx * adx + ady * ady + adz * adz;
                double b2 = bdx * bdx + bdy * bdy + bdz * bdz;
                return Double.compare(a2, b2);
            });

            Main.getScheduler().execute(() -> {
                final Iterator<int[]> it = offsets.iterator();
                final int[] found = {0};

                AtomicReference<MyScheduledTask> taskReference = new AtomicReference<>();
                MyScheduledTask task = Main.getScheduler().runTaskTimer(() -> {

                    if (!player.isOnline()) {
                        MyScheduledTask self = taskReference.get();
                        if (self != null) self.cancel();
                        removeTask(uuid, self);
                        return;
                    }

                    int processed = 0;
                    while (processed++ < Math.max(1, Config.batch_size) && it.hasNext()) {
                        int[] d = it.next();
                        final int x = bx + d[0];
                        final int y = by + d[1];
                        final int z = bz + d[2];

                        if (y < minY || y > maxY) continue;

                        final double dx = (x + 0.5) - cx;
                        final double dy = (y + 0.5) - cy;
                        final double dz = (z + 0.5) - cz;
                        if (dx * dx + dy * dy + dz * dz > r2) continue;

                        if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                        final Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                        final Block b = chunk.getBlock(x & 15, y, z & 15);

                        if (b.getType() != wantMat) continue;
                        if (hasProps && !b.getBlockData().matches(pattern)) continue;

                        TraceManager.registerTrace(new Trace(player, new BlockTarget(b), points));
                        if (++found[0] >= limit) {
                            MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_success.replace("{Count}", String.valueOf(found[0])));
                            MyScheduledTask self = taskReference.get();
                            if (self != null) self.cancel();
                            removeTask(uuid, self);
                            return;
                        }
                    }

                    if (!it.hasNext()) {
                        MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_success.replace("{Count}", String.valueOf(found[0])));
                        MyScheduledTask self = taskReference.get();
                        if (self != null) self.cancel();
                        removeTask(uuid, self);
                    }
                }, 1L, 1L);
                taskReference.set(task);
                addTask(uuid, task);
            });
        });

        MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_scan);
        return SINGLE_SUCCESS;
    }

    private static @Nullable String rawArg(CommandContext<CommandSourceStack> ctx, String name) {
        for (ParsedCommandNode<CommandSourceStack> nodes : ctx.getNodes()) {
            if (nodes.getNode() instanceof ArgumentCommandNode<?,?> arg && arg.getName().equals(name)) {
                StringRange range = nodes.getRange();
                return range.get(ctx.getInput());
            }
        }
        return null;
    }

    public static int cancel(CommandContext<CommandSourceStack> ctx) {

        final CommandSourceStack src = ctx.getSource();
        final Player player = (Player) src.getExecutor();
        if (player == null) return SINGLE_SUCCESS;

        cancelAll(player.getUniqueId());
        TraceManager.unregisterTracesFrom(player);

        MessageUtils.notifyPlayer(player, Config.prefix + Lang.echotrace_trace_cancel);
        return SINGLE_SUCCESS;
    }

}
