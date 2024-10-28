package chalkinshmeal.lockout.artifacts.tasks.specific;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;

public class WearFullIronArmorTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public WearFullIronArmorTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler, LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Wear a full set of iron armor";
        this.item = new ItemStack(Material.IRON_CHESTPLATE);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new WearFullIronArmorTaskPlayerArmorChangeListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.IRON_HELMET
        && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.IRON_CHESTPLATE
        && player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.IRON_LEGGINGS
        && player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.IRON_BOOTS) {
            this.complete(event.getPlayer());
        }
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class WearFullIronArmorTaskPlayerArmorChangeListener implements Listener {
    private final WearFullIronArmorTask task;

    public WearFullIronArmorTaskPlayerArmorChangeListener(WearFullIronArmorTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerArmorChangeEvent(event);
    }
}

