package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class BlockArrowWithShieldTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public BlockArrowWithShieldTask() {
        super();
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
    public static List<BlockArrowWithShieldTask> getTasks(int tier) {
        if (tier != 3) return new ArrayList<>();
        List<BlockArrowWithShieldTask> tasks = new ArrayList<>();
        tasks.add(new BlockArrowWithShieldTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDamageByEntityEvent(event);
    }
}
