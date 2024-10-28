package chalkinshmeal.lockout.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;



public class BlockArrowWithShieldTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public BlockArrowWithShieldTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                          LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Block an arrow with a shield";
        this.item = new ItemStack(Material.SHIELD);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new BlockArrowWithShieldTaskEntityDamageByEntityEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<BlockArrowWithShieldTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<BlockArrowWithShieldTask> tasks = new ArrayList<>();
        tasks.add(new BlockArrowWithShieldTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Arrow)) return;

        Player player = (Player) event.getEntity();

        // Check if the player is holding a shield
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (!(itemInMainHand.getType() == Material.SHIELD || itemInOffHand.getType() == Material.SHIELD)) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class BlockArrowWithShieldTaskEntityDamageByEntityEventListener implements Listener {
    private final BlockArrowWithShieldTask task;

    public BlockArrowWithShieldTaskEntityDamageByEntityEventListener(BlockArrowWithShieldTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDamageByEntityEvent(event);
    }
}
