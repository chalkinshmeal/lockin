package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class StayAboveHungerTask extends lockinTask {
    private static final String configKey = "stayAboveHungerTask";
    private static final String normalKey1 = "minHunger";
    private static final String normalKey2 = "maxHunger";
    private final int targetHunger;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public StayAboveHungerTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                           lockinRewardHandler lockinRewardHandler, int targetHunger) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.targetHunger = targetHunger;
        this.name = "Let your hunger fall to " + ((float) this.targetHunger / 2);
        this.item = new ItemStack(Material.GOLDEN_CARROT);
        this.isPunishment = true;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        this.configHandler.getInt(configKey + "." + normalKey1, 1);
        this.configHandler.getInt(configKey + "." + normalKey2, 1);
    }

    public void addListeners() {
		this.listeners.add(new StayAboveHungerTaskFoodLevelChangeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<StayAboveHungerTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<StayAboveHungerTask> tasks = new ArrayList<>();
        int minHunger = configHandler.getInt(configKey + "." + normalKey1, 1);
        int maxHunger = configHandler.getInt(configKey + "." + normalKey2, 20);
        int targetHunger = Utils.getRandNum(minHunger, maxHunger);
        tasks.add(new StayAboveHungerTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, targetHunger));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int newFoodLevel = event.getFoodLevel();
        System.out.println("Food level " + newFoodLevel + " (compared to " + this.targetHunger + ")");
        if (newFoodLevel > this.targetHunger) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class StayAboveHungerTaskFoodLevelChangeEventListener implements Listener {
    private final StayAboveHungerTask task;

    public StayAboveHungerTaskFoodLevelChangeEventListener(StayAboveHungerTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onFoodLevelChangeEvent(event);
    }
}