package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class RideEntityTask extends LockoutTask {
    private static final String configKey = "rideAnEntityTask";
    private static final String normalKey = "entityTypes";
    private final EntityType mountType;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public RideEntityTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler, EntityType mountType) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.mountType = mountType;
        this.name = "Ride a " + Utils.getReadableEntityTypeName(this.mountType);
        this.item = new ItemStack(Material.SADDLE);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String entityTypeStr : this.configHandler.getListFromKey(configKey + "." + normalKey)) {
            EntityType.valueOf(entityTypeStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new RideEntityTaskListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<RideEntityTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<RideEntityTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);
        if (entityTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }

        for (int i = 0; i < Math.min(taskCount, entityTypeStrs.size()); i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            tasks.add(new RideEntityTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, entityType));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
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
        if (this.task.isComplete()) return;
        this.task.onEntityMountEvent(event);
    }
}

