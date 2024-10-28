package chalkinshmeal.lockin.artifacts.tasks.specific;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;

public class DrinkMilkToCurePoisonTask extends lockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public DrinkMilkToCurePoisonTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler, lockinRewardHandler lockinRewardHandler) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
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
        if (this.task.isComplete()) return;
        this.task.onPlayerItemConsumeEvent(event);
    }
}

