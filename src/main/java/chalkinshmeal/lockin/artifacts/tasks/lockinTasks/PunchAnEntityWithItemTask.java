package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

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

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class PunchAnEntityWithItemTask extends LockinTask {
    private static final String configKey = "punchAnEntityWithItemTask";
    private static final String normalKey1 = "entityTypes";
    private static final String normalKey2 = "materials";
    private final EntityType entityType;
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public PunchAnEntityWithItemTask(EntityType entityType, Material material) {
        super();
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
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey1)) {
            for (String valueStr : configHandler.getListFromKey(configKey + "." + normalKey1 + "." + tierStr)) {
                EntityType.valueOf(valueStr);
            }
        }
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey2)) {
            for (String valueStr : configHandler.getListFromKey(configKey + "." + normalKey2 + "." + tierStr)) {
                Material.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new PunchAnEntityWithItemTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<PunchAnEntityWithItemTask> getTasks(int tier) {
        List<PunchAnEntityWithItemTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey1 + "." + tier), taskCount);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey2 + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, Math.min(entityTypeStrs.size(), materialStrs.size()));

        Collections.shuffle(materialStrs);

        for (int i = 0; i < loopCount; i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new PunchAnEntityWithItemTask(entityType, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDamageByEntityEvent(event);
    }
}

