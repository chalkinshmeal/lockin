package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class ActivateBlockTask extends lockinTask {
    private static final String configKey = "activateBlockTask";
    private static final String normalKey = "materials";
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ActivateBlockTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                         lockinRewardHandler lockinRewardHandler, Material material) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.material = material;
        this.name = "Activate a " + Utils.getReadableMaterialName(material);
        this.item = new ItemStack(this.material);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String materialStr : this.configHandler.getListFromKey(configKey + "." + normalKey)) {
            Material.valueOf(materialStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new ActivateBlockTaskPlayerCraftListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ActivateBlockTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<ActivateBlockTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);

        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < taskCount; i++) {
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new ActivateBlockTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (event.getOldCurrent() >= event.getNewCurrent()) return;

        for (Block adjacentBlock : Utils.getAdjacentBlocks(block)) {
            if (adjacentBlock.getType() == this.material)
                this.complete(Utils.getClosestPlayer(adjacentBlock.getLocation()));
        }
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ActivateBlockTaskPlayerCraftListener implements Listener {
    private final ActivateBlockTask task;

    public ActivateBlockTaskPlayerCraftListener(ActivateBlockTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        if (this.task.isComplete()) return;
        this.task.onBlockRedstoneEvent(event);
    }
}

