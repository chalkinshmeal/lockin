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

public class StayAboveHealthTask extends LockoutTask {
    private static final String configKey = "stayAboveHealthTask";
    private static final String normalKey1 = "minHealth";
    private static final String normalKey2 = "maxHealth";
    private final int targetHealth;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public StayAboveHealthTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                           LockoutRewardHandler lockoutRewardHandler, int targetHealth) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.targetHealth = targetHealth;
        this.name = "Let your health fall to or below " + ((float) this.targetHealth / 2) + " hearts";
        this.item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        this.isPunishment = true;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        this.configHandler.getInt(configKey + "." + normalKey1, 1);
        this.configHandler.getInt(configKey + "." + normalKey2, 1);
    }

    public void addListeners() {
		this.listeners.add(new StayAboveHealthTaskEntityDamageEventListener(this));
		this.listeners.add(new StayAboveHealthTaskEntityRegainHealthEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<StayAboveHealthTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<StayAboveHealthTask> tasks = new ArrayList<>();
        int minHealth = configHandler.getInt(configKey + "." + normalKey1, 1);
        int maxHealth = configHandler.getInt(configKey + "." + normalKey2, 20);
        int targetHealth = Utils.getRandNum(minHealth, maxHealth);
        tasks.add(new StayAboveHealthTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, targetHealth));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int healthAfterDamage = (int) Math.ceil(player.getHealth() - event.getFinalDamage());
        if (healthAfterDamage > this.targetHealth) return;

        this.complete(player);
    }

    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int healthAfterHealing = (int) Math.ceil(player.getHealth() + event.getAmount());
        if (healthAfterHealing > this.targetHealth) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class StayAboveHealthTaskEntityDamageEventListener implements Listener {
    private final StayAboveHealthTask task;

    public StayAboveHealthTaskEntityDamageEventListener(StayAboveHealthTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDamageEvent(event);
    }
}

class StayAboveHealthTaskEntityRegainHealthEventListener implements Listener {
    private final StayAboveHealthTask task;

    public StayAboveHealthTaskEntityRegainHealthEventListener(StayAboveHealthTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityRegainHealthEvent(event);
    }
}