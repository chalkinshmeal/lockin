package chalkinshmeal.lockin.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;



public class CatchFishTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public CatchFishTask() {
        super();
        this.name = "Catch a fish with a fishing rod";
        this.item = new ItemStack(Material.FISHING_ROD);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new CatchFishTaskPlayerFishEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<CatchFishTask> getTasks(int tier) {
        if (tier != 2) return new ArrayList<>();
        List<CatchFishTask> tasks = new ArrayList<>();
        tasks.add(new CatchFishTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerFishEvent(PlayerFishEvent event) {
        if (event.getState() != State.CAUGHT_FISH) return;
        Player player = event.getPlayer();

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class CatchFishTaskPlayerFishEventListener implements Listener {
    private final CatchFishTask task;

    public CatchFishTaskPlayerFishEventListener(CatchFishTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerFishEvent(PlayerFishEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerFishEvent(event);
    }
}
