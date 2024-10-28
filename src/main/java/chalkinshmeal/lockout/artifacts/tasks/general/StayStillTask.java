package chalkinshmeal.lockout.artifacts.tasks.general;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class StayStillTask extends LockoutTask {
    private static final String configKey = "stayStillTask";
    private static final String normalKey1 = "minSeconds";
    private static final String normalKey2 = "maxSeconds";
    private static final String punishmentKey1 = "punishmentMinSeconds";
    private static final String punishmentKey2 = "punishmentMaxSeconds";
    private final int targetSeconds;
    private Map<Player, Location> playerLocations;
    private Map<Player, Long> stayTimers;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public StayStillTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, int targetSeconds, boolean isPunishment) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetSeconds = targetSeconds;
        this.name = "Stay still for " + this.targetSeconds + " seconds.";
        this.item = new ItemStack(Material.BAKED_POTATO);
        this.isPunishment = isPunishment;
        this.playerLocations = new HashMap<>();
        this.stayTimers = new HashMap<>();
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        this.configHandler.getInt(configKey + "." + normalKey1, 1);
        this.configHandler.getInt(configKey + "." + normalKey2, 1);
        this.configHandler.getInt(configKey + "." + punishmentKey1, 1);
        this.configHandler.getInt(configKey + "." + punishmentKey2, 1);
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
    public static List<StayStillTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler, boolean isPunishment) {
        List<StayStillTask> tasks = new ArrayList<>();
        int targetSeconds = -1;
        if (isPunishment) {
            int minSeconds = configHandler.getInt(configKey + "." + punishmentKey1, 100);
            int maxSeconds = configHandler.getInt(configKey + "." + punishmentKey2, 100);
            targetSeconds = Utils.getRandNum(minSeconds, maxSeconds);
        }
        else {
            int minSeconds = configHandler.getInt(configKey + "." + normalKey1, 10);
            int maxSeconds = configHandler.getInt(configKey + "." + normalKey2, 10);
            targetSeconds = Utils.getRandNum(minSeconds, maxSeconds);
        }
        tasks.add(new StayStillTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetSeconds, isPunishment));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
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
                if (isComplete()) {
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
        if (this.task.isComplete()) return;
        this.task.onPlayerMoveEvent(event);
    }
}