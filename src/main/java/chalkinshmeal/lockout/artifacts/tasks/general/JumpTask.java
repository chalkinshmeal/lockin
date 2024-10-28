package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class JumpTask extends LockoutTask {
    private static final String configKey = "jumpTask";
    private static final String normalKey1 = "minJumps";
    private static final String normalKey2 = "maxJumps";
    private static final String punishmentKey1 = "punishmentMinJumps";
    private static final String punishmentKey2 = "punishmentMaxJumps";
    private final int targetJumps;
    private Map<Player, Integer> jumpCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public JumpTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, int targetJumps, boolean isPunishment) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetJumps = targetJumps;
        this.name = "Jump " + this.targetJumps + " times";
        this.item = new ItemStack(Material.RABBIT_FOOT);
        this.isPunishment = isPunishment;
        this.jumpCounts = new HashMap<>();
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        this.configHandler.getInt(configKey + "." + normalKey1, 1);
        this.configHandler.getInt(configKey + "." + normalKey2, 1);
    }

    public void addListeners() {
		this.listeners.add(new JumpTaskPlayerJumpEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<JumpTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler, boolean isPunishment) {
        List<JumpTask> tasks = new ArrayList<>();
        int targetJumps = -1;
        if (isPunishment) {
            int minJumps = configHandler.getInt(configKey + "." + punishmentKey1, 100);
            int maxJumps = configHandler.getInt(configKey + "." + punishmentKey2, 100);
            targetJumps = Utils.getRandNum(minJumps, maxJumps);
        }
        else {
            int minJumps = configHandler.getInt(configKey + "." + normalKey1, 10);
            int maxJumps = configHandler.getInt(configKey + "." + normalKey2, 10);
            targetJumps = Utils.getRandNum(minJumps, maxJumps);
        }
        tasks.add(new JumpTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetJumps, isPunishment));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerJumpEvent(PlayerJumpEvent event) {
        Player player = event.getPlayer();

        this.jumpCounts.put(player, this.jumpCounts.getOrDefault(player, 0) + 1);
        if (this.jumpCounts.get(player) < this.targetJumps) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class JumpTaskPlayerJumpEventListener implements Listener {
    private final JumpTask task;

    public JumpTaskPlayerJumpEventListener(JumpTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerJumpEvent(PlayerJumpEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerJumpEvent(event);
    }
}