package echotrace.util;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Logger {

    /**
     * Send a debug message when error logging is enabled.
     * Output Format: [EchoTraceError - ClassThatCalledThis:line] Message
     * @param message The error message to log
     */
    public static void logErr(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[EchoTraceError - " + className + ":" + caller.getLineNumber() + "] " + message;
        Bukkit.getLogger().log(Level.SEVERE, prefixedMessage);
    }

    /**
     * Send a debug message when error logging is enabled.
     * Output Format: [EchoTraceError - ClassThatCalledThis:line] Message
     * @param throwable The error element to log, including its stack trace
     */
    public static void logErr(Throwable throwable) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefix = "[EchoTraceError - " + className + ":" + caller.getLineNumber() + "] ";
        Bukkit.getLogger().log(Level.SEVERE, prefix + throwable.getMessage(), throwable);
    }

}
