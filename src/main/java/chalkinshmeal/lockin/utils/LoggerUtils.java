package chalkinshmeal.lockin.utils;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class LoggerUtils {
    //-------------------------------------------------------------------------
    // Logger methods
    //-------------------------------------------------------------------------
    public static void info(String msg) {
        LoggerUtils.getLogger().info(
            "[" + LoggerUtils.getFileName() +
            "::" + LoggerUtils.getCallerMethodName(2) + "] " +
            msg);
    }

    //-------------------------------------------------------------------------
    // Private methods
    //-------------------------------------------------------------------------
    private static Logger getLogger() {
        return Bukkit.getServer().getLogger();
    }

    private static String getCallerMethodName(int skip) {
        return StackWalker.
            getInstance().
            walk(stream -> stream.skip(skip).findFirst().get()).
            getMethodName();
    }

    private static String getFileName() {
        // Get the current thread's stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // The current method is at index 1, the caller is at index 2
        if (stackTrace.length > 3) {
            StackTraceElement currentElement = stackTrace[3];
            return currentElement.getFileName().replace(".java", "");
        } else {
            return "";
        }
    }
}