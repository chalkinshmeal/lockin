package chalkinshmeal.lockout.artifacts.tasks.general;

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

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class GetSpecificHealthTask extends LockoutTask {
    private static final String configKey = "getSpecificHealthTask";
    private static final String normalKey1 = "minHealth";
    private static final String normalKey2 = "maxHealth";
    private final int targetHealth;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public GetSpecificHealthTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, int targetHealth) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetHealth = targetHealth;
        this.name = "Have your health be exactly " + ((float) this.targetHealth / 2) + " hearts";
        this.item = new ItemStack(Material.GOLDEN_APPLE);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        this.configHandler.getInt(configKey + "." + normalKey1, 1);
        this.configHandler.getInt(configKey + "." + normalKey2, 1);
    }

    public void addListeners() {
		this.listeners.add(new GetSpecificHealthTaskEntityDamageEventListener(this));
		this.listeners.add(new GetSpecificHealthTaskEntityRegainHealthEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<GetSpecificHealthTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<GetSpecificHealthTask> tasks = new ArrayList<>();
        int minHealth = configHandler.getInt(configKey + "." + normalKey1, 1);
        int maxHealth = configHandler.getInt(configKey + "." + normalKey2, 20);
        int targetHealth = Utils.getRandNum(minHealth, maxHealth);
        tasks.add(new GetSpecificHealthTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetHealth));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
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