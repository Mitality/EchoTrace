package echotrace.commands.subcommands;

import echotrace.Main;
import echotrace.commands.SubCommand;
import echotrace.config.Config;
import echotrace.config.Lang;
import echotrace.depend.VanishPlugins;
import echotrace.util.MessageUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bukkit.Bukkit.getServer;

public class TraceCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if (args.length < 2) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.echotrace_trace_usage);
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.echotrace_trace_no_target.replace("{Player}", args[1]));
            return true;
        }

        if (target.getLocation().getWorld() != player.getLocation().getWorld()) {
            MessageUtils.notifySender(sender, Config.prefix + Lang.echotrace_trace_wrong_world.replace("{Player}", args[1]));
            return true;
        }

        int pointCount = Config.default_points;
        if (args.length > 2) {
            try {
                pointCount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
            }
        }
        final int targetPointCount = pointCount;

        Location loc = player.getEyeLocation();
        AtomicInteger taskId = new AtomicInteger();

        taskId.set(getServer().getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {

            Vector currentDir = loc.getDirection().normalize();
            int pointCount = 0;

            @Override
            public void run() {

                if (target.getLocation().getWorld() != player.getLocation().getWorld()) {
                    getServer().getScheduler().cancelTask(taskId.get());
                    return;
                }

                long count = 0;
                while (count < Config.tracing_count) {

                    Vector toTarget = target.getEyeLocation().toVector().subtract(loc.toVector());

                    if (toTarget.lengthSquared() < 0.25) {
                        if (Config.client_side) {
                            player.spawnParticle(Config.on_hit_particle_type, loc, Config.on_hit_particle_count, 0.0, 0.0, 0.0, Config.on_hit_particle_speed, null, true);
                            player.playSound(loc, Config.on_hit_sound_type, SoundCategory.MASTER, (float) Config.on_hit_sound_volume, (float) Config.on_hit_sound_pitch);
                        } else {
                            player.getWorld().spawnParticle(Config.on_hit_particle_type, loc, Config.on_hit_particle_count, 0.0, 0.0, 0.0, Config.on_hit_particle_speed, null, true);
                            player.getWorld().playSound(loc, Config.on_hit_sound_type, SoundCategory.MASTER, (float) Config.on_hit_sound_volume, (float) Config.on_hit_sound_pitch);
                        }
                        getServer().getScheduler().cancelTask(taskId.get());
                        return;
                    }

                    Vector targetDir = toTarget.clone().normalize();
                    if (Config.turn_rate <= 0) {
                        currentDir = targetDir;
                    } else {
                        currentDir = turnTowards(currentDir, targetDir, Config.turn_rate);
                    }
                    loc.add(currentDir.clone().multiply(Config.tracing_step_size));

                    if (Config.client_side) {
                        player.spawnParticle(Config.tracing_particle_type, loc, Config.tracing_particle_count, 0.0, 0.0, 0.0, Config.tracing_particle_speed, null, true);
                        player.playSound(loc, Config.tracing_sound_type, SoundCategory.MASTER, (float) Config.tracing_sound_volume, (float) Config.tracing_sound_pitch);
                    } else {
                        player.getWorld().spawnParticle(Config.tracing_particle_type, loc, Config.tracing_particle_count, 0.0, 0.0, 0.0, Config.tracing_particle_speed, null, true);
                        player.getWorld().playSound(loc, Config.tracing_sound_type, SoundCategory.MASTER, (float) Config.tracing_sound_volume, (float) Config.tracing_sound_pitch);
                    }

                    pointCount++;

                    if (targetPointCount > 0 && pointCount + 1 > targetPointCount) {
                        getServer().getScheduler().cancelTask(taskId.get());
                        return;
                    }

                    count++;
                }

            }
        }, 0L, Config.tracing_interval).getTaskId());

        MessageUtils.notifySender(sender, Config.prefix + Lang.echotrace_trace_success);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !VanishPlugins.isVanished(player))
                    .map(Player::getName).toList();
        }
        if (args.length == 3) {
            return List.of("<point count>");
        }
        return List.of();
    }

    @Override
    public String permission() {
        return "echotrace.trace";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    private Vector turnTowards(Vector from, Vector to, double maxDeg) {

        Vector a = from.clone().normalize();
        Vector b = to.clone().normalize();

        double dot = Math.max(-1.0, Math.min(1.0, a.dot(b)));
        double angle = Math.toDegrees(Math.acos(dot));
        if (angle <= maxDeg || angle == 0.0) return b;

        double t = maxDeg / angle; // 0..1
        Vector blended = a.multiply(1.0 - t).add(b.multiply(t));
        if (blended.lengthSquared() == 0.0) {
            // 180° edge case (pick any perpendicular)
            Vector perp = new Vector(-a.getZ(), 0, a.getX());
            if (perp.lengthSquared() == 0.0) perp = new Vector(0, 1, 0);
            return perp.normalize();
        }
        return blended.normalize();
    }

}
