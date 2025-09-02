package echotrace.config;

import org.bukkit.configuration.file.FileConfiguration;

public class Lang {

    // Command Manager
    public static String echotrace_usage;
    public static String echotrace_invalid;
    public static String echotrace_player_only;
    public static String echotrace_not_permitted;

    // Reload Command
    public static String echotrace_reload_success;
    public static String echotrace_reload_fail;

    // Trace Command
    public static String echotrace_trace_usage;
    public static String echotrace_trace_no_target;
    public static String echotrace_trace_wrong_world;
    public static String echotrace_trace_success;

    public static void load(FileConfiguration config) {

        // Command Manager
        echotrace_usage = config.getString("commands.echotrace.usage", "echotrace_usage");
        echotrace_invalid = config.getString("commands.echotrace.invalid", "echotrace_invalid");
        echotrace_player_only = config.getString("commands.echotrace.player-only", "echotrace_player_only");
        echotrace_not_permitted = config.getString("commands.echotrace.not-permitted", "echotrace_not_permitted");

        // Reload Command
        echotrace_reload_success = config.getString("commands.echotrace.reload.success", "echotrace_reload_success");
        echotrace_reload_fail = config.getString("commands.echotrace.reload.fail", "echotrace_reload_fail");

        // Trace Command
        echotrace_trace_usage = config.getString("commands.echotrace.trace.usage", "echotrace_trace_usage");
        echotrace_trace_no_target = config.getString("commands.echotrace.trace.no-target", "echotrace_trace_no_target");
        echotrace_trace_wrong_world = config.getString("commands.echotrace.trace.wrong-world", "echotrace_trace_wrong_world");
        echotrace_trace_success = config.getString("commands.echotrace.trace.success", "echotrace_trace_success");

    }
    
}
