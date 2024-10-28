package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class DieTask extends LockoutTask {
    private static final String configKey = "deathTask";
    private static final String normalKey1 = "minDeaths";
    private static final String normalKey2 = "maxDeaths";
    private static final String punishmentKey1 = "punishmentMinDeaths";
    private static final String punishmentKey2 = "punishmentMaxDeaths";
    private final int targetDeaths;
    private Map<Player, Integer> deathCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public DieTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, int targetDeaths, boolean isPunishment) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetDeaths = targetDeaths;
        this.name = "Die " + this.targetDeaths + " times";
        this.item = new ItemStack(Material.TOTEM_OF_UNDYING);
        this.isPunishment = isPunishment;
        this.deathCounts = new HashMap<>();
        this.applyAAnRules = false;
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
		this.listeners.add(new DieTaskPlayerDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<DieTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler, boolean isPunishment) {
        List<DieTask> tasks = new ArrayList<>();
        int targetDeaths = -1;
        if (isPunishment) {
            int minDeaths = configHandler.getInt(configKey + "." + punishmentKey1, 1);
            int maxDeaths = configHandler.getInt(configKey + "." + punishmentKey2, 1);
            targetDeaths = Utils.getRandNum(minDeaths, maxDeaths);
        }
        else {
            int minDeaths = configHandler.getInt(configKey + "." + normalKey1, 10);
            int maxDeaths = configHandler.getInt(configKey + "." + normalKey2, 10);
            targetDeaths = Utils.getRandNum(minDeaths, maxDeaths);
        }
        tasks.add(new DieTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetDeaths, isPunishment));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        this.deathCounts.put(player, this.deathCounts.getOrDefault(player, 0) + 1);
        if (this.deathCounts.get(player) < this.targetDeaths) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class DieTaskPlayerDeathEventListener implements Listener {
    private final DieTask task;

    public DieTaskPlayerDeathEventListener(DieTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerDeathEvent(event);
    }
}