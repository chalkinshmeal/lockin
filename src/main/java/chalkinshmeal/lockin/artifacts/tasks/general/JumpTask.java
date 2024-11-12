package chalkinshmeal.lockin.artifacts.tasks.general;

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

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;

public class JumpTask extends LockinTask {
    private static final String configKey = "jumpTask";
    private static final String normalKey = "jumps";
    private final int targetJumps;
    private Map<Player, Integer> jumpCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public JumpTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                           LockinRewardHandler lockinRewardHandler, int targetJumps) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.targetJumps = targetJumps;
        this.name = "Jump " + this.targetJumps + " times";
        this.item = new ItemStack(Material.RABBIT_FOOT);
        this.jumpCounts = new HashMap<>();
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new JumpTaskPlayerJumpEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<JumpTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, int tier) {
        List<JumpTask> tasks = new ArrayList<>();
        int targetJumps = configHandler.getInt(configKey + "." + normalKey + "." + tier, 10);
        tasks.add(new JumpTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, targetJumps));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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