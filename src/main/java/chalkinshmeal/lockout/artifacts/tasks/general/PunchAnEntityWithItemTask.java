package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;

public class PunchAnEntityWithItemTask extends LockoutTask {
    private static final String configKey = "punchAnEntityWithItemTask";
    private static final String normalKey1 = "entityTypes";
    private static final String normalKey2 = "materials";
    private final EntityType entityType;
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public PunchAnEntityWithItemTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                     LockoutRewardHandler lockoutRewardHandler, EntityType entityType, Material material) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.entityType = entityType;
        this.material = material;
        this.name = "Punch a " + Utils.getReadableEntityTypeName(this.entityType) + " with a " + Utils.getReadableMaterialName(this.material);
        this.item = new ItemStack(this.material);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String entityTypeStr : this.configHandler.getListFromKey(configKey + "." + normalKey1)) {
            EntityType.valueOf(entityTypeStr);
        }
        for (String materialStr : this.configHandler.getListFromKey(configKey + "." + normalKey2)) {
            Material.valueOf(materialStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new PunchAnEntityWithItemTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<PunchAnEntityWithItemTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler) {
        List<PunchAnEntityWithItemTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey1), taskCount);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey2), taskCount);

        Collections.shuffle(materialStrs);

        if (materialStrs.size() == 0 || entityTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < Math.min(taskCount, entityTypeStrs.size()); i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new PunchAnEntityWithItemTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, entityType, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getEntityType() != this.entityType) return;
        Player player = (Player) event.getDamager();
        Material itemInHand = player.getInventory().getItemInMainHand().getType();
        if (itemInHand != this.material) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class PunchAnEntityWithItemTaskPlayerInteractListener implements Listener {
    private final PunchAnEntityWithItemTask task;

    public PunchAnEntityWithItemTaskPlayerInteractListener(PunchAnEntityWithItemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDamageByEntityEvent(event);
    }
}

