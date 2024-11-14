package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class GetExpLevelTask extends LockinTask {
    private static final String configKey = "getExpLevelTask";
    private static final String normalKey = "level";
    private final int maxLevel;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public GetExpLevelTask(int maxLevel) {
        super();
        this.maxLevel = maxLevel;
        this.name = "Get to level " + this.maxLevel;
        this.item = new ItemStack(Material.EXPERIENCE_BOTTLE);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new GetExpLevelTaskPlayerLevelChangeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<GetExpLevelTask> getTasks(int tier) {
        List<GetExpLevelTask> tasks = new ArrayList<>();
        int targetLevel = configHandler.getInt(configKey + "." + normalKey + "." + tier, 10);
        tasks.add(new GetExpLevelTask(targetLevel));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();

        if (newLevel < this.maxLevel) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class GetExpLevelTaskPlayerLevelChangeEventListener implements Listener {
    private final GetExpLevelTask task;

    public GetExpLevelTaskPlayerLevelChangeEventListener(GetExpLevelTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerLevelChangeEvent(event);
    }
}