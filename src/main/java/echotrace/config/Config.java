package echotrace.config;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static String prefix;
    public static String language;

    public static int update_check_interval;
    public static boolean releases_only;

    public static boolean client_side;

    public static Particle tracing_particle_type;
    public static double tracing_particle_speed;
    public static int tracing_particle_count;

    public static Sound tracing_sound_type;
    public static double tracing_sound_volume;
    public static double tracing_sound_pitch;

    public static double tracing_step_size;
    public static long tracing_interval;
    public static long tracing_count;

    public static Particle on_hit_particle_type;
    public static double on_hit_particle_speed;
    public static int on_hit_particle_count;

    public static Sound on_hit_sound_type;
    public static double on_hit_sound_volume;
    public static double on_hit_sound_pitch;

    public static int default_points;
    public static double turn_rate;

    public static void load(FileConfiguration config) {

        prefix = config.getString("prefix", "&8&l[&3&lEchoTrace&8&l] &r");
        language = config.getString("language", "en");

        update_check_interval = config.getInt("update-check-interval", 12);
        releases_only = config.getBoolean("releases-only", true);

        client_side = config.getBoolean("client-side", true);

        tracing_particle_type = Particle.valueOf(config.getString("tracing.particle.type", "SOUL_FIRE_FLAME"));
        tracing_particle_speed = config.getDouble("tracing.particle.speed", 0.0);
        tracing_particle_count = config.getInt("tracing.particle.count", 1);

        tracing_sound_type = Sound.valueOf(config.getString("tracing.sound.type", "BLOCK_SCULK_CATALYST_BLOOM"));
        tracing_sound_volume = config.getDouble("tracing.sound.volume", 0.75);
        tracing_sound_pitch = config.getDouble("tracing.sound.pitch", 0.5);

        tracing_step_size = config.getDouble("tracing.step-size", 0.5);
        tracing_interval = config.getLong("tracing.interval", 1);
        tracing_count = config.getLong("tracing.count", 1);
        if (tracing_count <= 0) tracing_count = Long.MAX_VALUE;

        on_hit_particle_type = Particle.valueOf(config.getString("on-hit.particle.type", "SOUL_FIRE_FLAME"));
        on_hit_particle_speed = config.getDouble("on-hit.particle.speed", 0.25);
        on_hit_particle_count = config.getInt("on-hit.particle.count", 10);

        on_hit_sound_type = Sound.valueOf(config.getString("on-hit.sound.type", "BLOCK_SCULK_SHRIEKER_BREAK"));
        on_hit_sound_volume = config.getDouble("on-hit.sound.volume", 0.75);
        on_hit_sound_pitch = config.getDouble("on-hit.sound.pitch", 0.25);

        default_points = config.getInt("default-points", -1);
        turn_rate = config.getDouble("turn-rate", 7.5);

    }

}
