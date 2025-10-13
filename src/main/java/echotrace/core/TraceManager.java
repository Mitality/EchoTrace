package echotrace.core;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import echotrace.Main;
import echotrace.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TraceManager {

    private static MyScheduledTask heartbeat = null;
    private static final List<Trace> activeTraces = new ArrayList<>();

    public static void startHeartbeat() {
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }
        long interval = Math.max(1, Config.tracing_interval);
        heartbeat = Main.getScheduler()
                .runTaskTimerAsynchronously(TraceManager::advanceTraces, interval, interval);
    }

    public static void advanceTraces() {
        if (Bukkit.getServer().getTPS()[0] < Config.min_tps) return;
        synchronized (activeTraces) {
            activeTraces.removeIf(trace -> !trace.active());
            for (Trace trace : activeTraces) {
                trace.advance(Config.tracing_count);
            }
        }
    }

    public static void registerTrace(Trace trace) {
        synchronized (activeTraces) {
            activeTraces.add(trace);
        }
    }

    public static void unregisterTracesFrom(Player player) {
        synchronized (activeTraces) {
            activeTraces.removeIf(trace -> trace.getPlayer().getUniqueId() == player.getUniqueId());
        }
    }

    public static void unregisterAllTraces() {
        synchronized (activeTraces) {
            activeTraces.clear();
        }
    }

}
