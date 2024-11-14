package chalkinshmeal.lockin.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class UseNametagTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public UseNametagTask() {
        super();
        this.name = "Use a nametag";
        this.item = new ItemStack(Material.NAME_TAG);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new UseNametagTaskPlayerInteractEntityEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<UseNametagTask> getTasks(int tier) {
        if (tier != 6) return new ArrayList<>();
        List<UseNametagTask> tasks = new ArrayList<>();
        tasks.add(new UseNametagTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if the item in hand is a name tag
        if (itemInHand == null || itemInHand.getType() != Material.NAME_TAG) return;

        // Check if the name tag has a name (not just an empty name tag)
        if (!itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasDisplayName()) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class UseNametagTaskPlayerInteractEntityEventListener implements Listener {
    private final UseNametagTask task;

    public UseNametagTaskPlayerInteractEntityEventListener(UseNametagTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerInteractEntityEvent(event);
    }
}
