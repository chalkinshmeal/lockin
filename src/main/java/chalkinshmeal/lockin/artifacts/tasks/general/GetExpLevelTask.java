package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class GetExpLevelTask extends LockinTask {
    private static final String configKey = "getExpLevelTask";
    private static final String normalKey = "maxLevel";
    private static final String punishmentKey1 = "punishmentMinLevel";
    private static final String punishmentKey2 = "punishmentMaxLevel";
    private final int maxLevel;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public GetExpLevelTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                           LockinRewardHandler lockinRewardHandler, int maxLevel, boolean isPunishment) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.maxLevel = maxLevel;
        this.name = "Get to level " + this.maxLevel;
        this.item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        this.isPunishment = isPunishment;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new GetExpLevelTaskPlayerLevelChangeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<GetExpLevelTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, boolean isPunishment) {
        List<GetExpLevelTask> tasks = new ArrayList<>();
        int targetLevel = -1;
        if (isPunishment) {
            int minLevel = configHandler.getInt(configKey + "." + punishmentKey1, 1);
            int maxLevel = configHandler.getInt(configKey + "." + punishmentKey2, 2);
            targetLevel = Utils.getRandNum(minLevel, maxLevel);
        }
        else {
            int maxLevel = configHandler.getInt(configKey + "." + normalKey, 10);
            targetLevel = Utils.getRandNum(1, maxLevel);
        }
        tasks.add(new GetExpLevelTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, targetLevel, isPunishment));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();

        if (newLevel < this.maxLevel) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class GetExpLevelTaskPlayerLevelChangeEventListener implements Listener {
    private final GetExpLevelTask task;

    public GetExpLevelTaskPlayerLevelChangeEventListener(GetExpLevelTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerLevelChangeEvent(event);
    }
}