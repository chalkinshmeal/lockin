package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;


public class SmeltItemsTask extends LockoutTask {
    private static final String configKey = "smeltItemsTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final int amount;
    private final Map<Player, Integer> smeltedCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public SmeltItemsTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                            LockoutRewardHandler lockoutRewardHandler, Material material, int amount) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.material = material;
        this.amount = amount;
        this.smeltedCounts = new HashMap<>();
        this.name = "Obtain " + this.amount + " " + Utils.getReadableMaterialName(material) + " by smelting";
        this.item = new ItemStack(this.material);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String materialStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            Material.valueOf(materialStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new SmeltItemsTaskFurnaceExtractEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<SmeltItemsTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<SmeltItemsTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + normalKey), taskCount);
        int loopCount = taskCount;

        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + normalKey + "." + materialStr, 1);
            tasks.add(new SmeltItemsTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, material, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onFurnaceExtractEvent(FurnaceExtractEvent event) {
        var player = event.getPlayer();
        var itemsExtracted = event.getItemAmount();
        if (event.getItemType() != this.material) return;

        smeltedCounts.put(player, smeltedCounts.getOrDefault(player, 0) + itemsExtracted);
        if (this.smeltedCounts.get(player) < this.amount) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class SmeltItemsTaskFurnaceExtractEventListener implements Listener {
    private final SmeltItemsTask task;

    public SmeltItemsTaskFurnaceExtractEventListener(SmeltItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onFurnaceExtractEvent(FurnaceExtractEvent event) {
        if (this.task.isComplete()) return;
        this.task.onFurnaceExtractEvent(event);
    }
}
