package chalkinshmeal.lockin.artifacts.tasks.general;

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

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class DieTask extends LockinTask {
    private static final String configKey = "deathTask";
    private static final String normalKey1 = "minDeaths";
    private static final String normalKey2 = "maxDeaths";
    private static final String punishmentKey1 = "punishmentMinDeaths";
    private static final String punishmentKey2 = "punishmentMaxDeaths";
    private final int targetDeaths;
    private Map<Player, Integer> deathCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public DieTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                           LockinRewardHandler lockinRewardHandler, int targetDeaths, boolean isPunishment) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
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
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new DieTaskPlayerDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<DieTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, boolean isPunishment) {
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
        tasks.add(new DieTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, targetDeaths, isPunishment));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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