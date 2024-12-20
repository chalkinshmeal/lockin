package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class RideEntityTask extends LockinTask {
    private static final String configKey = "rideAnEntityTask";
    private static final String normalKey = "entityTypes";
    private final EntityType mountType;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public RideEntityTask(EntityType mountType) {
        super();
        this.mountType = mountType;
        this.name = "Ride a " + Utils.getReadableEntityTypeName(this.mountType);
        this.item = new ItemStack(Material.SADDLE);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                EntityType.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new RideEntityTaskListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<RideEntityTask> getTasks(int tier) {
        List<RideEntityTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, entityTypeStrs.size());

        for (int i = 0; i < loopCount; i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            tasks.add(new RideEntityTask(entityType));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityMountEvent(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getMount().getType() != this.mountType) return;
        this.complete((Player) event.getEntity());
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class RideEntityTaskListener implements Listener {
    private final RideEntityTask task;

    public RideEntityTaskListener(RideEntityTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityMountEvent(EntityMountEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityMountEvent(event);
    }
}

