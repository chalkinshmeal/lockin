package chalkinshmeal.lockin.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

//-------------------------------------------------------------------------
// Task Utilities
// Note: If an argument is of type Runnable, this is a normal function that is either:
// - In a class, and prefixed with this:: (Ex: this::my_task)
// - In a class, and static
//-------------------------------------------------------------------------
public class TaskUtils {
    //-------------------------------------------------------------------------
    // Runs a task after a delay, returning its task ID
    //-------------------------------------------------------------------------
    public static int runDelayedTask(Plugin plugin, Runnable task, float delayTicks) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        return Bukkit.getScheduler().runTaskLater(plugin, task, (long) delayTicks).getTaskId();
    }

    //-------------------------------------------------------------------------
    // Runs a repeating task after a delay, returning its task ID
    //-------------------------------------------------------------------------
    public static int runRepeatingTask(Plugin plugin, Runnable task, float delayTicks, float periodTicks) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, task, (long) delayTicks, (long) periodTicks).getTaskId();
    }

    //-------------------------------------------------------------------------
    // Cancels a task given its task ID
    //-------------------------------------------------------------------------
    public static void cancelTask(int taskID) {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}
