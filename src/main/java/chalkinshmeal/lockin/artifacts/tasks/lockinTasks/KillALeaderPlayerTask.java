package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.rewards.types.RestoreToOneLifeReward;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;

public class KillALeaderPlayerTask extends LockinTask {
    private final LockinTeamHandler lockinTeamHandler;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public KillALeaderPlayerTask(LockinTeamHandler lockinTeamHandler, LockinScoreboard lockinScoreboard) {
        super();
        this.lockinTeamHandler = lockinTeamHandler;
        this.name = "Have a leader player die";
        this.item = new ItemStack(Material.IRON_SWORD);
        this.reward = new RestoreToOneLifeReward(plugin, lockinTeamHandler, lockinScoreboard);
        this.isCatchUpTask = true;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new KillALeaderPlayerTaskEntityDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<KillALeaderPlayerTask> getTasks(LockinTeamHandler lockinTeamHandler, LockinScoreboard lockinScoreboard) {
        List<KillALeaderPlayerTask> tasks = new ArrayList<>();
        tasks.add(new KillALeaderPlayerTask(lockinTeamHandler, lockinScoreboard));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (!this.lockinTeamHandler.getLeadingTeamPlayers().contains(player)) return;
        if (!this.lockinTeamHandler.getTeamPlayersWithNoLives().contains(killer)) return;

        this.complete(killer);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class KillALeaderPlayerTaskEntityDeathEventListener implements Listener {
    private final KillALeaderPlayerTask task;

    public KillALeaderPlayerTaskEntityDeathEventListener(KillALeaderPlayerTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDeathEvent(event);
    }
}
