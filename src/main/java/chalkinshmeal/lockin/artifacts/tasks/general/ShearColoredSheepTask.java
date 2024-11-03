package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class ShearColoredSheepTask extends LockinTask {
    private static final String configKey = "shearColoredSheepTask";
    private static final String normalKey = "dyeColors";
    private final DyeColor dyeColor;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ShearColoredSheepTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                          LockinRewardHandler lockinRewardHandler, DyeColor dyeColor) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.dyeColor = dyeColor;
        this.name = "Shear a " + Utils.getReadableDyeColorName(this.dyeColor) + " colored sheep";
        this.item = new ItemStack(Material.SHEARS);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : this.configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                DyeColor.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new ShearColoredSheepTaskListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ShearColoredSheepTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler) {
        List<ShearColoredSheepTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> dyeColorStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);
        if (dyeColorStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }

        for (int i = 0; i < Math.min(taskCount, dyeColorStrs.size()); i++) {
            DyeColor dyeColor = DyeColor.valueOf(dyeColorStrs.get(i));
            tasks.add(new ShearColoredSheepTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, dyeColor));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (!(event.getEntity() instanceof Sheep)) return;

        Sheep sheep = (Sheep) event.getEntity();
        Player player = event.getPlayer();
        
        // Check if the sheep is the target color
        if (sheep.getColor() != this.dyeColor) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ShearColoredSheepTaskListener implements Listener {
    private final ShearColoredSheepTask task;

    public ShearColoredSheepTaskListener(ShearColoredSheepTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerShearEntityEvent(event);
    }
}

