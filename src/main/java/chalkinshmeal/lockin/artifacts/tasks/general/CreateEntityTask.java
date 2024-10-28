package chalkinshmeal.lockin.artifacts.tasks.general;

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

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class CreateEntityTask extends lockinTask {
    private static final String configKey = "createEntityTask";
    private static final String normalKey = "entityTypes";
    private final EntityType createType;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public CreateEntityTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                          lockinRewardHandler lockinRewardHandler, EntityType createType) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
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
    public static List<CreateEntityTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<CreateEntityTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);

        if (entityTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < Math.min(taskCount, entityTypeStrs.size()); i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            tasks.add(new CreateEntityTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, entityType));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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

