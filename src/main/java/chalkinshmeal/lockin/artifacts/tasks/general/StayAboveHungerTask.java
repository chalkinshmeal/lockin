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

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;

public class StayAboveHungerTask extends LockinTask {
    private static final String configKey = "stayAboveHungerTask";
    private static final String normalKey = "hunger";
    private final int targetHunger;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public StayAboveHungerTask(int targetHunger) {
        super();
        this.targetHunger = targetHunger;
        this.name = "Let your hunger fall to " + ((float) this.targetHunger / 2);
        this.item = new ItemStack(Material.GOLDEN_CARROT);
        this.isPunishment = true;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new StayAboveHungerTaskFoodLevelChangeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<StayAboveHungerTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, int tier) {
        List<StayAboveHungerTask> tasks = new ArrayList<>();
        int targetHunger = configHandler.getInt(configKey + "." + normalKey + "." + tier, 1);
        tasks.add(new StayAboveHungerTask(targetHunger));
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onFoodLevelChangeEvent(event);
    }
}