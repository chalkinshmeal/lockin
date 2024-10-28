package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class InteractItemTask extends LockoutTask {
    private static final String configKey = "interactItemTask";
    private static final String normalKey = "materials";
    private static final String punishmentKey = "punishmentMaterials";
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public InteractItemTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                         LockoutRewardHandler lockoutRewardHandler, Material material, boolean isPunishment) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.material = material;
        this.name = "Interact with a " + Utils.getReadableMaterialName(material);
        this.item = new ItemStack(this.material);
        this.isPunishment = isPunishment;
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
		this.listeners.add(new InteractItemTaskPlayerCraftListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<InteractItemTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler, boolean isPunishment) {
        List<InteractItemTask> tasks = new ArrayList<>();
        int taskCount = (isPunishment) ? 10000 : configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = (isPunishment) ? punishmentKey : normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + subKey), taskCount);
        int loopCount = (isPunishment) ? materialStrs.size() : taskCount;

        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new InteractItemTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, material, isPunishment));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != this.material) return;

        Player player = event.getPlayer();
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class InteractItemTaskPlayerCraftListener implements Listener {
    private final InteractItemTask task;

    public InteractItemTaskPlayerCraftListener(InteractItemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerInteractEvent(event);
    }
}

