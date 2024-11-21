package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.utils.EntityUtils;

public class KillOpposingTeamTask extends LockinTask {
    private final LockinTeamHandler lockinTeamHandler;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public KillOpposingTeamTask(LockinTeamHandler lockinTeamHandler) {
        super();
        this.lockinTeamHandler = lockinTeamHandler;
        this.name = "Kill all opposing teams";
        this.item = new ItemStack(Material.IRON_SWORD);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new KillOpposingTeamTaskEntityDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<KillOpposingTeamTask> getTasks(LockinTeamHandler lockinTeamHandler) {
        List<KillOpposingTeamTask> tasks = new ArrayList<>();
        tasks.add(new KillOpposingTeamTask(lockinTeamHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        event.setCancelled(true);

        // Set killed players to spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        // Complete for the surviving team if only one team is remaining
        List<String> teamsWithAlivePlayers = new ArrayList<>();
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            for (UUID uuid : this.lockinTeamHandler.getTeamPlayers(teamName)) {
                Player _player = EntityUtils.getPlayer(uuid);
                if (_player != null && _player.getGameMode() == GameMode.SURVIVAL) {
                    teamsWithAlivePlayers.add(teamName);
                    break;
                }
            }
        }

        if (teamsWithAlivePlayers.size() == 1) {
            String aliveTeam = teamsWithAlivePlayers.get(0);
            for (UUID uuid : this.lockinTeamHandler.getTeamPlayers(aliveTeam)) {
                this.complete(EntityUtils.getPlayer(uuid));
            }
        }
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class KillOpposingTeamTaskEntityDeathEventListener implements Listener {
    private final KillOpposingTeamTask task;

    public KillOpposingTeamTaskEntityDeathEventListener(KillOpposingTeamTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDeathEvent(event);
    }
}
