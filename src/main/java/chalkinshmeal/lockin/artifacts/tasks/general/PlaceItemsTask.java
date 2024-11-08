package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;



public class PlaceItemsTask extends LockinTask {
    private static final String configKey = "placeItemsTask";
    private static final String normalKey = "materials";
    private static final String punishmentKey = "punishmentMaterials";
    private final Material material;
    private final int amount;
    private final Map<Player, Integer> placedItems;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public PlaceItemsTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                          LockinRewardHandler lockinRewardHandler, Material material, int amount, boolean isPunishment) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.material = material;
        this.amount = amount;
        this.placedItems = new HashMap<>();
        this.name = "Place " + this.amount + " " + Utils.getReadableMaterialName(material);
        this.item = new ItemStack(this.material);
        this.isPunishment = isPunishment;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : this.configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                Material.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new PlaceItemsTaskBlockPlaceEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<PlaceItemsTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, boolean isPunishment) {
        List<PlaceItemsTask> tasks = new ArrayList<>();
        int taskCount = (isPunishment) ? -1 : configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = (isPunishment) ? punishmentKey : normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey), taskCount);
        int loopCount = (isPunishment) ? materialStrs.size() : taskCount;

        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + subKey + "." + materialStr, 1);
            tasks.add(new PlaceItemsTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, material, amount, isPunishment));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        // Return if material does not match
        Material itemType = event.getBlock().getType();
        if (itemType != this.material) return;

        // Return if 
        Player player = event.getPlayer();
        this.placedItems.put(player, this.placedItems.getOrDefault(player, 0) + 1);
        if (this.placedItems.get(player) < this.amount) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class PlaceItemsTaskBlockPlaceEventListener implements Listener {
    private final PlaceItemsTask task;

    public PlaceItemsTaskBlockPlaceEventListener(PlaceItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (this.task.isComplete()) return;
        this.task.onBlockPlaceEvent(event);
    }
}
