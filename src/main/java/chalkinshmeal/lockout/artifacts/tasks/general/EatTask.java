package chalkinshmeal.lockout.artifacts.tasks.general;

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
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class EatTask extends LockoutTask {
    private static final String configKey = "eatTask";
    private static final String normalKey1 = "minConsumes";
    private static final String normalKey2 = "maxConsumes";
    private static final String punishmentKey1 = "punishmentMinConsumes";
    private static final String punishmentKey2 = "punishmentMaxConsumes";
    private final int targetConsumes;
    private Map<Player, Integer> consumeCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public EatTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, int targetConsumes, boolean isPunishment) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetConsumes = targetConsumes;
        this.name = "Eat " + this.targetConsumes + " items";
        this.item = new ItemStack(Material.COOKED_BEEF);
        this.isPunishment = isPunishment;
        this.consumeCounts = new HashMap<>();
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
		this.listeners.add(new EatItemsTaskPlayerItemConsumeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EatTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler, boolean isPunishment) {
        List<EatTask> tasks = new ArrayList<>();
        int targetConsumes = -1;
        if (isPunishment) {
            int minConsumes = configHandler.getInt(configKey + "." + punishmentKey1, 5);
            int maxConsumes = configHandler.getInt(configKey + "." + punishmentKey2, 5);
            targetConsumes = Utils.getRandNum(minConsumes, maxConsumes);
        }
        else {
            int minConsumes = configHandler.getInt(configKey + "." + normalKey1, 10);
            int maxConsumes = configHandler.getInt(configKey + "." + normalKey2, 50);
            targetConsumes = Utils.getRandNum(minConsumes, maxConsumes);
        }
        tasks.add(new EatTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetConsumes, isPunishment));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        this.consumeCounts.put(player, this.consumeCounts.getOrDefault(player, 0) + 1);
        if (this.consumeCounts.get(player) < this.targetConsumes) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EatItemsTaskPlayerItemConsumeEventListener implements Listener {
    private final EatTask task;

    public EatItemsTaskPlayerItemConsumeEventListener(EatTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerItemConsumeEvent(event);
    }
}