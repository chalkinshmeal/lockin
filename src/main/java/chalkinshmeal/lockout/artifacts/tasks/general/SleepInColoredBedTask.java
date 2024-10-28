package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class SleepInColoredBedTask extends LockoutTask {
    private static final String configKey = "sleepInColoredBedTask";
    private static final String normalKey = "dyeColors";
    private final DyeColor dyeColor;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public SleepInColoredBedTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler, DyeColor dyeColor) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.dyeColor = dyeColor;
        this.name = "Sleep in a " + Utils.getReadableDyeColorName(this.dyeColor) + " colored bed";
        this.item = new ItemStack(Utils.getBedMaterial(this.dyeColor));
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String dyeColorStr : this.configHandler.getListFromKey(configKey + "." + normalKey)) {
            DyeColor.valueOf(dyeColorStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new SleepInColoredBedTaskListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<SleepInColoredBedTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<SleepInColoredBedTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> dyeColorStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);
        if (dyeColorStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }

        for (int i = 0; i < Math.min(taskCount, dyeColorStrs.size()); i++) {
            DyeColor dyeColor = DyeColor.valueOf(dyeColorStrs.get(i));
            tasks.add(new SleepInColoredBedTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, dyeColor));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Block bedBlock = event.getBed();

        // Check if the bed is made of the target color
        if (this.dyeColor != Utils.getDyeColorFromMaterial(bedBlock.getType())) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class SleepInColoredBedTaskListener implements Listener {
    private final SleepInColoredBedTask task;

    public SleepInColoredBedTaskListener(SleepInColoredBedTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerBedEnterEvent(event);
    }
}

