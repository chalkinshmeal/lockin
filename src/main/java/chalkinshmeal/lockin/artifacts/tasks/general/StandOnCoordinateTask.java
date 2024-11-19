package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class StandOnCoordinateTask extends LockinTask {
    private static final String configKey = "standOnCoordinateTask";
    private static final String normalKey = "radius";
    private final Location targetLocation;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public StandOnCoordinateTask(Location targetLocation) {
        super();
        this.targetLocation = targetLocation;
        this.name = "Be at coordinates X=" + (int) this.targetLocation.getX() + ", Z=" + (int) this.targetLocation.getZ();
        this.item = new ItemStack(Material.MAP);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new StandOnCoordinateTaskPlayerMoveEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<StandOnCoordinateTask> getTasks(int tier) {
        List<StandOnCoordinateTask> tasks = new ArrayList<>();
        int targetRadius = configHandler.getInt(configKey + "." + normalKey + "." + tier, -1);
        if (targetRadius == -1) return tasks;

        World world = Bukkit.getWorld("world");
        Location spawnLocation = world.getSpawnLocation();
        Location targetLocation = Utils.getRandomLocation(world, spawnLocation.getX(), spawnLocation.getZ(), targetRadius);
        tasks.add(new StandOnCoordinateTask(targetLocation));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        int currentX = location.getBlockX();
        int currentZ = location.getBlockZ();
        if (currentX != (int) this.targetLocation.getX() || currentZ != (int) this.targetLocation.getZ()) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class StandOnCoordinateTaskPlayerMoveEventListener implements Listener {
    private final StandOnCoordinateTask task;

    public StandOnCoordinateTaskPlayerMoveEventListener(StandOnCoordinateTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerMoveEvent(event);
    }
}