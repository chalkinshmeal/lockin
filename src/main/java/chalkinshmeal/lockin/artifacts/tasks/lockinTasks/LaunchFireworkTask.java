package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;



public class LaunchFireworkTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public LaunchFireworkTask() {
        super();
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
    public static List<LaunchFireworkTask> getTasks(int tier) {
        if (tier != 3) return new ArrayList<>();
        List<LaunchFireworkTask> tasks = new ArrayList<>();
        tasks.add(new LaunchFireworkTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onProjectileLaunchEvent(event);
    }
}
