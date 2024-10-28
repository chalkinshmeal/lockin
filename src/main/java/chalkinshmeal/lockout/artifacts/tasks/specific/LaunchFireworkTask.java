package chalkinshmeal.lockout.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;



public class LaunchFireworkTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public LaunchFireworkTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Launch a firework";
        this.item = new ItemStack(Material.FIREWORK_ROCKET);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new LaunchFireworkTaskProjectileLaunchEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<LaunchFireworkTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<LaunchFireworkTask> tasks = new ArrayList<>();
        tasks.add(new LaunchFireworkTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Firework)) return;
        Firework firework = (Firework) event.getEntity();
        if (!(firework.getShooter() instanceof Player)) return;
        Player player = (Player) firework.getShooter();

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class LaunchFireworkTaskProjectileLaunchEventListener implements Listener {
    private final LaunchFireworkTask task;

    public LaunchFireworkTaskProjectileLaunchEventListener(LaunchFireworkTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (this.task.isComplete()) return;
        this.task.onProjectileLaunchEvent(event);
    }
}
