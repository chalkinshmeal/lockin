package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class GrowWheatWithBonemealTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public GrowWheatWithBonemealTask() {
        super();
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

    public static List<GrowWheatWithBonemealTask> getTasks(int tier) {
        if (tier != 2) return new ArrayList<>();
        List<GrowWheatWithBonemealTask> tasks = new ArrayList<>();
        tasks.add(new GrowWheatWithBonemealTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerInteractEvent(event);
    }
}

