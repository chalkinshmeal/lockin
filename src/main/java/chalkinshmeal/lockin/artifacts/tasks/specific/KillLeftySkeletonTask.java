package chalkinshmeal.lockin.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;



public class KillLeftySkeletonTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public KillLeftySkeletonTask() {
        super();
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
    public static List<KillLeftySkeletonTask> getTasks(int tier) {
        if (tier != 4) return new ArrayList<>();
        List<KillLeftySkeletonTask> tasks = new ArrayList<>();
        tasks.add(new KillLeftySkeletonTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDeathEvent(event);
    }
}
