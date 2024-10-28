package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class CreateEntityTask extends LockoutTask {
    private static final String configKey = "createEntityTask";
    private static final String normalKey = "entityTypes";
    private final EntityType createType;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public CreateEntityTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler, EntityType createType) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.createType = createType;
        this.name = "Create a " + Utils.getReadableEntityTypeName(this.createType);
        this.item = new ItemStack(Material.CARVED_PUMPKIN);
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
		this.listeners.add(new CreateEntityTaskListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<CreateEntityTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<CreateEntityTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);

        if (entityTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < Math.min(taskCount, entityTypeStrs.size()); i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            tasks.add(new CreateEntityTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, entityType));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        EntityType entityType = event.getEntityType();
        if (entityType != this.createType) return;

        Player player = Utils.getClosestPlayer(event.getLocation());

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class CreateEntityTaskListener implements Listener {
    private final CreateEntityTask task;

    public CreateEntityTaskListener(CreateEntityTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (this.task.isComplete()) return;
        this.task.onCreatureSpawnEvent(event);
    }
}

