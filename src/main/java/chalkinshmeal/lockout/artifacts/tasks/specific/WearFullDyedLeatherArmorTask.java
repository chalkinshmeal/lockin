package chalkinshmeal.lockout.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

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
import chalkinshmeal.lockout.utils.Utils;

public class WearFullDyedLeatherArmorTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public WearFullDyedLeatherArmorTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler, LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Wear a full set of dyed leather armor";
        this.item = new ItemStack(Material.LEATHER_CHESTPLATE);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new WearFullDyedLeatherArmorTaskPlayerArmorChangeListener(this));
    }

    public static List<WearFullDyedLeatherArmorTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler,
                                                              LockoutTaskHandler lockoutTaskHandler, LockoutRewardHandler lockoutRewardHandler) {
        List<WearFullDyedLeatherArmorTask> tasks = new ArrayList<>();
        tasks.add(new WearFullDyedLeatherArmorTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();

        ItemStack[] armorContents = player.getInventory().getArmorContents();

        // Check if all armor pieces are dyed leather
        if (!(Utils.isDyedLeatherArmor(armorContents[0]) && // Helmet
              Utils.isDyedLeatherArmor(armorContents[1]) && // Chestplate
              Utils.isDyedLeatherArmor(armorContents[2]) && // Leggings
              Utils.isDyedLeatherArmor(armorContents[3]))) return;
        
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class WearFullDyedLeatherArmorTaskPlayerArmorChangeListener implements Listener {
    private final WearFullDyedLeatherArmorTask task;

    public WearFullDyedLeatherArmorTaskPlayerArmorChangeListener(WearFullDyedLeatherArmorTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerArmorChangeEvent(event);
    }
}

