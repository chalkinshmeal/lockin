package chalkinshmeal.lockout.artifacts.tasks.specific;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;

public class GrowWheatWithBonemealTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public GrowWheatWithBonemealTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler, LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.name = "Grow wheat using bonemeal";
        this.item = new ItemStack(Material.WHEAT_SEEDS);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new GrowWheatWithBonemealTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getBlockData() instanceof Ageable)) return;
        Material blockType = block.getType();
        if (blockType != Material.WHEAT) return;
        Material itemInHand = player.getInventory().getItemInMainHand().getType();
        if (itemInHand != Material.BONE_MEAL) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class GrowWheatWithBonemealTaskPlayerInteractListener implements Listener {
    private final GrowWheatWithBonemealTask task;

    public GrowWheatWithBonemealTaskPlayerInteractListener(GrowWheatWithBonemealTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerInteractEvent(event);
    }
}

