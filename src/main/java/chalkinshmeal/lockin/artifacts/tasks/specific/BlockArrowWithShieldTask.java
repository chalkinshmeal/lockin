package chalkinshmeal.lockin.artifacts.tasks.specific;

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

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;



public class BlockArrowWithShieldTask extends lockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public BlockArrowWithShieldTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                          lockinRewardHandler lockinRewardHandler) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
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
    public static List<BlockArrowWithShieldTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<BlockArrowWithShieldTask> tasks = new ArrayList<>();
        tasks.add(new BlockArrowWithShieldTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler));
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
        if (this.task.isComplete()) return;
        this.task.onEntityDamageByEntityEvent(event);
    }
}
