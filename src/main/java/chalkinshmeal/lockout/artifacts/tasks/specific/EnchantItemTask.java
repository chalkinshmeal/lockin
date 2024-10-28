package chalkinshmeal.lockout.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;



public class EnchantItemTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public EnchantItemTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Enchant an item";
        this.item = new ItemStack(Material.ENCHANTING_TABLE);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new EnchantItemTaskEnchantItemEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EnchantItemTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<EnchantItemTask> tasks = new ArrayList<>();
        tasks.add(new EnchantItemTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEnchantItemEvent(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EnchantItemTaskEnchantItemEventListener implements Listener {
    private final EnchantItemTask task;

    public EnchantItemTaskEnchantItemEventListener(EnchantItemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEnchantItemEvent(EnchantItemEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEnchantItemEvent(event);
    }
}
