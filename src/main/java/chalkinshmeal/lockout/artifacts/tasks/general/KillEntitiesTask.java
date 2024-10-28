package chalkinshmeal.lockout.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;



public class KillEntitiesTask extends LockoutTask {
    private static final String configKey = "killEntitiesTask";
    private static final String normalKey = "entityTypes";
    private static final String punishmentKey = "punishmentEntityTypes";
    private final EntityType entityType;
    private final int amount;
    private final Map<Player, Integer> killedEntities;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public KillEntitiesTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                            LockoutRewardHandler lockoutRewardHandler, EntityType entityType, int amount, boolean isPunishment) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
        this.entityType = entityType;
        this.amount = amount;
        this.killedEntities = new HashMap<>();
        this.name = "Kill " + this.amount + " " + Utils.getReadableEntityTypeName(entityType);
        this.item = new ItemStack(Material.IRON_SWORD);
        this.isPunishment = isPunishment;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String entityTypeStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            EntityType.valueOf(entityTypeStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new KillEntitiesTaskEntityDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<KillEntitiesTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler,
                                                          LockoutRewardHandler lockoutRewardHandler, boolean isPunishment) {
        List<KillEntitiesTask> tasks = new ArrayList<>();
        int taskCount = (isPunishment) ? -1 : configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = (isPunishment) ? punishmentKey : normalKey;
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey), taskCount);
        int loopCount = (isPunishment) ? entityTypeStrs.size() : taskCount;

        if (entityTypeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            String entityTypeStr = entityTypeStrs.get(i);
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + subKey + "." + entityTypeStr, 1);
            tasks.add(new KillEntitiesTask(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler, entityType, amount, isPunishment));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        EntityType entityType = event.getEntity().getType();
        if (entityType != this.entityType) return;

        if (!(event.getEntity().getKiller() instanceof Player)) return;
        Player player = event.getEntity().getKiller();

        this.killedEntities.put(player, this.killedEntities.getOrDefault(player, 0) + 1);
        if (this.killedEntities.get(player) < this.amount) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class KillEntitiesTaskEntityDeathEventListener implements Listener {
    private final KillEntitiesTask task;

    public KillEntitiesTaskEntityDeathEventListener(KillEntitiesTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDeathEvent(event);
    }
}
