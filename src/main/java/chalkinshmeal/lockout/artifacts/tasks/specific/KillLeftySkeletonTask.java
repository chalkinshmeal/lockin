package chalkinshmeal.lockout.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;



public class KillLeftySkeletonTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public KillLeftySkeletonTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Kill a lefty skeleton";
        this.item = new ItemStack(Material.SKELETON_SKULL);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new KillLeftySkeletonTaskEntityDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<KillLeftySkeletonTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<KillLeftySkeletonTask> tasks = new ArrayList<>();
        tasks.add(new KillLeftySkeletonTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) return;
        Skeleton skeleton = (Skeleton) event.getEntity();

        if (!(skeleton.getKiller() instanceof Player)) return;
        Player player = skeleton.getKiller();

        if (!skeleton.isLeftHanded()) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class KillLeftySkeletonTaskEntityDeathEventListener implements Listener {
    private final KillLeftySkeletonTask task;

    public KillLeftySkeletonTaskEntityDeathEventListener(KillLeftySkeletonTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDeathEvent(event);
    }
}
