package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class ReceivePotionEffectTypeTask extends LockoutTask {
    private static final String configKey = "receivePotionEffectTypeTask";
    private static final String normalKey = "potionEffectTypes";
    private final PotionEffectType potionEffectType;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public ReceivePotionEffectTypeTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                         LockoutRewardHandler lockoutRewardHandler, PotionEffectType potionEffectType) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.potionEffectType = potionEffectType;
        this.name = "Receive the " + Utils.getReadablePotionEffectTypeName(this.potionEffectType) + " potion effect";
        this.item = new ItemStack(Utils.getSplashPotionFromPotionEffectType(potionEffectType));
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String potionEffectTypeStr : this.configHandler.getListFromKey(configKey + "." + normalKey)) {
            Utils.getPotionEffectTypeFromStr(potionEffectTypeStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new ReceivePotionEffectTypeTaskPlayerCraftListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ReceivePotionEffectTypeTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<ReceivePotionEffectTypeTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> potionEffectTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);

        if (potionEffectTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < taskCount; i++) {
            PotionEffectType potionEffectType = Utils.getPotionEffectTypeFromStr(potionEffectTypeStrs.get(i));
            tasks.add(new ReceivePotionEffectTypeTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, potionEffectType));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Check if the event is caused by the player receiving a new effect
        if (event.getNewEffect() == null || event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;

        PotionEffect newEffect = event.getNewEffect();
        if (newEffect.getType() != this.potionEffectType) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ReceivePotionEffectTypeTaskPlayerCraftListener implements Listener {
    private final ReceivePotionEffectTypeTask task;

    public ReceivePotionEffectTypeTaskPlayerCraftListener(ReceivePotionEffectTypeTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityPotionEffectEvent(event);
    }
}

