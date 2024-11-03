package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class ShearSheepTask extends LockinTask {
    private static final String configKey = "shearSheepTask";
    private static final String normalKey1 = "minShears";
    private static final String normalKey2 = "maxShears";
    private final int targetShears;
    private final Map<Player, Integer> shearCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ShearSheepTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                           LockinRewardHandler lockinRewardHandler, int targetShears) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.targetShears = targetShears;
        this.shearCounts = new HashMap<>();
        this.name = "Shear " + this.targetShears + " sheep";
        this.item = new ItemStack(Material.SHEARS);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new ShearSheepTaskPlayerShearEntityEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ShearSheepTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler) {
        List<ShearSheepTask> tasks = new ArrayList<>();
        int minShears = configHandler.getInt(configKey + "." + normalKey1, 1);
        int maxShears = configHandler.getInt(configKey + "." + normalKey2, 20);
        int targetShears = Utils.getRandNum(minShears, maxShears);
        tasks.add(new ShearSheepTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, targetShears));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (!(event.getEntity() instanceof Sheep)) return;
        Player player = event.getPlayer();

        shearCounts.put(player, shearCounts.getOrDefault(player, 0) + 1);
        if (shearCounts.get(player) < this.targetShears) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ShearSheepTaskPlayerShearEntityEventListener implements Listener {
    private final ShearSheepTask task;

    public ShearSheepTaskPlayerShearEntityEventListener(ShearSheepTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerShearEntityEvent(event);
    }
}