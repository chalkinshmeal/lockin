package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class EnterNetherTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EnterNetherTask() {
        super();
        this.name = "Enter the nether";
        this.item = new ItemStack(Material.NETHERRACK);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new EnterNetherTaskPlayerChangedWorldEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EnterNetherTask> getTasks(int tier) {
        if (tier != 4) return new ArrayList<>();
        List<EnterNetherTask> tasks = new ArrayList<>();
        tasks.add(new EnterNetherTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();
        
        // Check if the player has entered the Nether
        if (toWorld.getEnvironment() != World.Environment.NETHER) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EnterNetherTaskPlayerChangedWorldEventListener implements Listener {
    private final EnterNetherTask task;

    public EnterNetherTaskPlayerChangedWorldEventListener(EnterNetherTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerChangedWorldEvent(event);
    }
}
