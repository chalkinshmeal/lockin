package chalkinshmeal.lockin.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;



public class KillEntityWithStatusEffectTask extends lockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public KillEntityWithStatusEffectTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                          lockinRewardHandler lockinRewardHandler) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.name = "Kill an entity with a status effect";
        this.item = new ItemStack(Material.SPLASH_POTION);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new KillEntityWithStatusEffectTaskEntityDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<KillEntityWithStatusEffectTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<KillEntityWithStatusEffectTask> tasks = new ArrayList<>();
        tasks.add(new KillEntityWithStatusEffectTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getActivePotionEffects().isEmpty()) return;
        if (!(entity.getKiller() instanceof Player)) return;

        Player player = entity.getKiller();
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class KillEntityWithStatusEffectTaskEntityDeathEventListener implements Listener {
    private final KillEntityWithStatusEffectTask task;

    public KillEntityWithStatusEffectTaskEntityDeathEventListener(KillEntityWithStatusEffectTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDeathEvent(event);
    }
}
