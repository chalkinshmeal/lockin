package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class DrinkMilkToCurePoisonTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public DrinkMilkToCurePoisonTask() {
        super();
        this.name = "Drink milk to cure the poison effect";
        this.item = new ItemStack(Material.MILK_BUCKET);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new DrinkMilkToCurePoisonTaskListener(this));
    }

    public static List<DrinkMilkToCurePoisonTask> getTasks(int tier) {
        if (tier != 3) return new ArrayList<>();
        List<DrinkMilkToCurePoisonTask> tasks = new ArrayList<>();
        tasks.add(new DrinkMilkToCurePoisonTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() != Material.MILK_BUCKET) return;
        if (!player.hasPotionEffect(PotionEffectType.POISON)) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class DrinkMilkToCurePoisonTaskListener implements Listener {
    private final DrinkMilkToCurePoisonTask task;

    public DrinkMilkToCurePoisonTaskListener(DrinkMilkToCurePoisonTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerItemConsumeEvent(event);
    }
}

