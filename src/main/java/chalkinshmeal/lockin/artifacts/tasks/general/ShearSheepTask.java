package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class ShearSheepTask extends LockinTask {
    private static final String configKey = "shearSheepTask";
    private static final String normalKey = "shears";
    private final int targetShears;
    private final Map<String, Integer> shearCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ShearSheepTask(int targetShears) {
        super();
        this.targetShears = targetShears;
        this.shearCounts = new HashMap<>();
        this.name = "Shear " + this.targetShears + " sheep";
        this.item = new ItemStack(Material.SHEARS);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new ShearSheepTaskPlayerShearEntityEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ShearSheepTask> getTasks(int tier) {
        List<ShearSheepTask> tasks = new ArrayList<>();
        int targetShears = configHandler.getInt(configKey + "." + normalKey + "." + tier, -1);
        if (targetShears == -1) return tasks;

        tasks.add(new ShearSheepTask(targetShears));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (!(event.getEntity() instanceof Sheep)) return;
        Player player = event.getPlayer();
        String teamName = LockinTask.lockinTeamHandler.getTeamName(player);

        shearCounts.put(teamName, shearCounts.getOrDefault(teamName, 0) + 1);
        if (shearCounts.get(teamName) < this.targetShears) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ShearSheepTaskPlayerShearEntityEventListener implements Listener {
    private final ShearSheepTask task;

    public ShearSheepTaskPlayerShearEntityEventListener(ShearSheepTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerShearEntityEvent(event);
    }
}