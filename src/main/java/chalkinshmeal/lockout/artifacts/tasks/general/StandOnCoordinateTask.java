package chalkinshmeal.lockout.artifacts.tasks.general;

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
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class StandOnCoordinateTask extends LockoutTask {
    private static final String configKey = "standOnCoordinateTask";
    private static final String normalKey1 = "minRadius";
    private static final String normalKey2 = "maxRadius";
    private final Location targetLocation;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public StandOnCoordinateTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, Location targetLocation) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetLocation = targetLocation;
        this.name = "Be at coordinates X=" + (int) this.targetLocation.getX() + ", Z=" + (int) this.targetLocation.getZ();
        this.item = new ItemStack(Material.MAP);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        this.configHandler.getInt(configKey + "." + normalKey1, 100);
        this.configHandler.getInt(configKey + "." + normalKey2, 1000);
    }

    public void addListeners() {
		this.listeners.add(new StandOnCoordinateTaskPlayerMoveEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<StandOnCoordinateTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<StandOnCoordinateTask> tasks = new ArrayList<>();
        int minRadius = configHandler.getInt(configKey + "." + normalKey1, 100);
        int maxRadius = configHandler.getInt(configKey + "." + normalKey2, 1000);
        int targetRadius = Utils.getRandNum(minRadius, maxRadius);
        World world = Bukkit.getWorld("world");
        Location spawnLocation = world.getSpawnLocation();
        Location targetLocation = Utils.getRandomLocation(world, spawnLocation.getX(), spawnLocation.getZ(), targetRadius);
        tasks.add(new StandOnCoordinateTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetLocation));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
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
        if (this.task.isComplete()) return;
        this.task.onPlayerMoveEvent(event);
    }
}