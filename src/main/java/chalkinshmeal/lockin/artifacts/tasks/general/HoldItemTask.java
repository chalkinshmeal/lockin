package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class HoldItemTask extends lockinTask {
    private static final String configKey = "holdItemTask";
    private static final String punishmentKey = "punishmentMaterials";
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public HoldItemTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                         lockinRewardHandler lockinRewardHandler, Material material) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.material = material;
        this.name = "Hold " + Utils.getReadableMaterialName(material) + " in your hand";
        this.item = new ItemStack(this.material);
        this.isPunishment = true;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String materialStr : this.configHandler.getListFromKey(configKey + "." + punishmentKey)) {
            Material.valueOf(materialStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new HoldItemTaskPlayerCraftListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<HoldItemTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<HoldItemTask> tasks = new ArrayList<>();
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + punishmentKey), 10000);
        int loopCount = materialStrs.size();

        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new HoldItemTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        var inventory = player.getInventory();
        int newSlot = event.getNewSlot();
        ItemStack newItem = inventory.getItem(newSlot);

        if (newItem == null) return;
        if (newItem.getType() != this.material) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class HoldItemTaskPlayerCraftListener implements Listener {
    private final HoldItemTask task;

    public HoldItemTaskPlayerCraftListener(HoldItemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerItemHeldEvent(event);
    }
}

