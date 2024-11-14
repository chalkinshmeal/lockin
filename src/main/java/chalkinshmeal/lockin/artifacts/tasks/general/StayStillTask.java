package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class StayStillTask extends LockinTask {
    private static final String configKey = "stayStillTask";
    private static final String normalKey = "seconds";
    private final int targetSeconds;
    private Map<Player, Location> playerLocations;
    private Map<Player, Long> stayTimers;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public StayStillTask(int targetSeconds) {
        super();
        this.targetSeconds = targetSeconds;
        this.name = "Stay still for " + this.targetSeconds + " seconds.";
        this.item = new ItemStack(Material.BAKED_POTATO);
        this.playerLocations = new HashMap<>();
        this.stayTimers = new HashMap<>();
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new StayStillTaskPlayerMoveEventListener(this));
    }

    public void init() {
        super.init();
        this.startChecking();
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<StayStillTask> getTasks(int tier) {
        List<StayStillTask> tasks = new ArrayList<>();
        int targetSeconds = configHandler.getInt(configKey + "." + normalKey + "." + tier, 10);
        tasks.add(new StayStillTask(targetSeconds));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Check if the player has moved significantly
        if (from.distanceSquared(to) > 0.01) {
            playerLocations.put(player, to);
            stayTimers.put(player, System.currentTimeMillis());
        }
    }

    //---------------------------------------------------------------------------------------------
    // Repeated Tasks
    //---------------------------------------------------------------------------------------------
    private void startChecking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (haveAllTeamsCompleted()) {
                    this.cancel();
                    return;
                }

                long currentTime = System.currentTimeMillis();
                for (Map.Entry<Player, Long> entry : stayTimers.entrySet()) {
                    Player player = entry.getKey();
                    long lastMoveTime = entry.getValue();

                    // Check if the player has stayed in the same location for the required time
                    if ((currentTime - lastMoveTime) / 1000 >= targetSeconds) {
                        if (player != null) {
                            // Reset the timer to avoid multiple triggers
                            stayTimers.put(player, currentTime);
                            complete(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20); // Check every second
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class StayStillTaskPlayerMoveEventListener implements Listener {
    private final StayStillTask task;

    public StayStillTaskPlayerMoveEventListener(StayStillTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerMoveEvent(event);
    }
}