package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;



public class BreedEntitiesTask extends LockinTask {
    private static final String configKey = "breedEntitiesTask";
    private static final String normalKey = "entityTypes";
    private final EntityType entityType;
    private final int amount;
    private final Map<Player, Integer> bredEntities;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public BreedEntitiesTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                            LockinRewardHandler lockinRewardHandler, EntityType entityType, int amount) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.entityType = entityType;
        this.amount = amount;
        this.bredEntities = new HashMap<>();
        this.name = "Breed " + this.amount + " " + Utils.getReadableEntityTypeName(entityType);
        this.item = new ItemStack(Material.LEAD);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : this.configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                EntityType.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new BreedEntitiesTaskEntityBreedEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<BreedEntitiesTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler) {
        List<BreedEntitiesTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + normalKey), taskCount);

        if (entityTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < taskCount; i++) {
            String entityTypeStr = entityTypeStrs.get(i);
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + normalKey + "." + entityTypeStr, 1);
            tasks.add(new BreedEntitiesTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, entityType, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityBreedEvent(EntityBreedEvent event) {
        // Check if the breeding was initiated by a player
        if (!(event.getBreeder() instanceof Player)) return;
        Player player = (Player) event.getBreeder();
        
        if (event.getMother().getType() != this.entityType) return;
        this.bredEntities.put(player, this.bredEntities.getOrDefault(player, 0) + 1);

        if (this.bredEntities.get(player) < this.amount) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class BreedEntitiesTaskEntityBreedEventListener implements Listener {
    private final BreedEntitiesTask task;

    public BreedEntitiesTaskEntityBreedEventListener(BreedEntitiesTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityBreedEvent(EntityBreedEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityBreedEvent(event);
    }
}
