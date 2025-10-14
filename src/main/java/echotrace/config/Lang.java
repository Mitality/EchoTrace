package echotrace.config;

import org.bukkit.configuration.file.FileConfiguration;

public class Lang {

    // Reload Command
    public static String echotrace_reload_success;
    public static String echotrace_reload_failure;

    // Trace Command
    public static String echotrace_trace_no_target;
    public static String echotrace_trace_wrong_world;
    public static String echotrace_trace_cooldown;
    public static String echotrace_trace_scan;
    public static String echotrace_trace_check;
    public static String echotrace_trace_success;
    public static String echotrace_trace_cancel;

    public static void load(FileConfiguration config) {

        // Reload Command
        echotrace_reload_success = config.getString("commands.echotrace.reload.success", "echotrace_reload_success");
        echotrace_reload_failure = config.getString("commands.echotrace.reload.failure", "echotrace_reload_failure");

        // Trace Command
        echotrace_trace_no_target = config.getString("commands.echotrace.trace.no-target", "echotrace_trace_no_target");
        echotrace_trace_wrong_world = config.getString("commands.echotrace.trace.wrong-world", "echotrace_trace_wrong_world");
        echotrace_trace_cooldown = config.getString("commands.echotrace.trace.cooldown", "echotrace_trace_cooldown");
        echotrace_trace_scan = config.getString("commands.echotrace.trace.scan", "echotrace_trace_scan");
        echotrace_trace_check = config.getString("commands.echotrace.trace.check", "echotrace_trace_check");
        echotrace_trace_success = config.getString("commands.echotrace.trace.success", "echotrace_trace_success");
        echotrace_trace_cancel = config.getString("commands.echotrace.trace.cancel", "echotrace_trace_cancel");

    }
    
}
