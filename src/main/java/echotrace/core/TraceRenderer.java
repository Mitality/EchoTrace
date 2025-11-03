package echotrace.core;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import echotrace.Main;
import echotrace.config.Config;
import echotrace.util.TraceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TraceRenderer {

    private static final Queue<RenderRequest> renderQueue = new ConcurrentLinkedQueue<>();
    private static MyScheduledTask heartbeat = null;

    public static void startHeartbeat() {
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }
        heartbeat = Main.getScheduler()
                .runTaskTimer(TraceRenderer::render, 1L, 1L);
    }

    public static void queueRender(Player player, Location location, boolean isHit) {

        if (location.getWorld() == null) return;
        World world = location.getWorld();

        if (!TraceUtils.isChunkLoaded(world, location)) return;

        if ( // Don't render points that are far away from any player
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(world))
                .mapToDouble(p -> p.getLocation().distance(location))
                .min().orElse(Double.MAX_VALUE) > Config.render_distance
        ) return;

        renderQueue.add(new RenderRequest(player, location.clone(), world, isHit));
    }

    public static void clearQueuedRenders(@NotNull UUID uuid) {
        renderQueue.removeIf(request -> request.player().getUniqueId().equals(uuid));
    }

    public static void render() {
        if (renderQueue.isEmpty()) return;
        RenderRequest request;
        long rendered = 0;
        while ((request = renderQueue.poll()) != null && rendered++ < Config.render_cap) {
            request.render();
        }
    }

    private record RenderRequest(Player player, Location location, World world, boolean isHit) {
        void render() {
            if (isHit) renderHit(player, location, world);
            else renderStep(player, location, world);
        }
    }

    private static void renderStep(Player player, Location location, World world) {
        if (Config.client_side) {
            player.spawnParticle(Config.tracing_particle_type, location, Config.tracing_particle_count, 0.0, 0.0, 0.0, Config.tracing_particle_speed, null, true);
            player.playSound(location, Config.tracing_sound_type, SoundCategory.MASTER, (float) Config.tracing_sound_volume, (float) Config.tracing_sound_pitch);
        } else {
            world.spawnParticle(Config.tracing_particle_type, location, Config.tracing_particle_count, 0.0, 0.0, 0.0, Config.tracing_particle_speed, null, true);
            world.playSound(location, Config.tracing_sound_type, SoundCategory.MASTER, (float) Config.tracing_sound_volume, (float) Config.tracing_sound_pitch);
        }
    }

    private static void renderHit(Player player, Location location, World world) {
        if (Config.client_side) {
            player.spawnParticle(Config.on_hit_particle_type, location, Config.on_hit_particle_count, 0.0, 0.0, 0.0, Config.on_hit_particle_speed, null, true);
            player.playSound(location, Config.on_hit_sound_type, SoundCategory.MASTER, (float) Config.on_hit_sound_volume, (float) Config.on_hit_sound_pitch);
        } else {
            world.spawnParticle(Config.on_hit_particle_type, location, Config.on_hit_particle_count, 0.0, 0.0, 0.0, Config.on_hit_particle_speed, null, true);
            world.playSound(location, Config.on_hit_sound_type, SoundCategory.MASTER, (float) Config.on_hit_sound_volume, (float) Config.on_hit_sound_pitch);
        }
    }
}
