package chalkinshmeal.lockout.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;


public class RepairIronGolemTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public RepairIronGolemTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Repair an iron golem";
        this.item = new ItemStack(Material.IRON_BLOCK);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new RepairIronGolemTaskPlayerInteractEntityEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<RepairIronGolemTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<RepairIronGolemTask> tasks = new ArrayList<>();
        tasks.add(new RepairIronGolemTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.IRON_GOLEM) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() != Material.IRON_INGOT) return;

        Player player = event.getPlayer();
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class RepairIronGolemTaskPlayerInteractEntityEventListener implements Listener {
    private final RepairIronGolemTask task;

    public RepairIronGolemTaskPlayerInteractEntityEventListener(RepairIronGolemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerInteractEntityEvent(event);
    }
}
