package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class GetSpecificHealthTask extends LockinTask {
    private static final String configKey = "getSpecificHealthTask";
    private static final String normalKey1 = "minHealth";
    private static final String normalKey2 = "maxHealth";
    private final int targetHealth;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public GetSpecificHealthTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                           LockinRewardHandler lockinRewardHandler, int targetHealth) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.targetHealth = targetHealth;
        this.name = "Have your health be exactly " + ((float) this.targetHealth / 2) + " hearts";
        this.item = new ItemStack(Material.GOLDEN_APPLE);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new GetSpecificHealthTaskEntityDamageEventListener(this));
		this.listeners.add(new GetSpecificHealthTaskEntityRegainHealthEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<GetSpecificHealthTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler) {
        List<GetSpecificHealthTask> tasks = new ArrayList<>();
        int minHealth = configHandler.getInt(configKey + "." + normalKey1, 1);
        int maxHealth = configHandler.getInt(configKey + "." + normalKey2, 20);
        int targetHealth = Utils.getRandNum(minHealth, maxHealth);
        tasks.add(new GetSpecificHealthTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, targetHealth));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int healthAfterDamage = (int) Math.ceil(player.getHealth() - event.getFinalDamage());
        if (healthAfterDamage != this.targetHealth) return;

        this.complete(player);
    }

    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int healthAfterHealing = (int) Math.ceil(player.getHealth() + event.getAmount());
        if (healthAfterHealing != this.targetHealth) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class GetSpecificHealthTaskEntityDamageEventListener implements Listener {
    private final GetSpecificHealthTask task;

    public GetSpecificHealthTaskEntityDamageEventListener(GetSpecificHealthTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDamageEvent(event);
    }
}

class GetSpecificHealthTaskEntityRegainHealthEventListener implements Listener {
    private final GetSpecificHealthTask task;

    public GetSpecificHealthTaskEntityRegainHealthEventListener(GetSpecificHealthTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityRegainHealthEvent(event);
    }
}