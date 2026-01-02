package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.mc_plugin_lib.teams.Team;

public class EatTask extends LockinTask {
    private static final String configKey = "eatTask";
    private static final String normalKey = "consumes";
    private final int targetConsumes;
    private Map<Team, Integer> consumeCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EatTask(int targetConsumes) {
        super();
        this.targetConsumes = targetConsumes;
        this.name = "Eat " + this.targetConsumes + " items";
        this.item = new ItemStack(Material.COOKED_BEEF);
        this.consumeCounts = new HashMap<>();
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new EatTaskPlayerItemConsumeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EatTask> getTasks(int tier) {
        List<EatTask> tasks = new ArrayList<>();
        int targetConsumes = configHandler.getInt(configKey + "." + normalKey + "." + tier, -1);
        if (targetConsumes == -1) return tasks;

        tasks.add(new EatTask(targetConsumes));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Team team = LockinTask.teamHandler.getTeam(player);

        this.consumeCounts.put(team, this.consumeCounts.getOrDefault(team, 0) + 1);
        if (this.consumeCounts.get(team) < this.targetConsumes) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EatTaskPlayerItemConsumeEventListener implements Listener {
    private final EatTask task;

    public EatTaskPlayerItemConsumeEventListener(EatTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerItemConsumeEvent(event);
    }
}