package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class KillEntityWithItemTask extends LockinTask {
    private static final String configKey = "killEntityWithItemTask";
    private static final String normalKey1 = "entityTypes";
    private static final String normalKey2 = "materials";
    private final EntityType entityType;
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public KillEntityWithItemTask(EntityType entityType, Material material) {
        super();
        this.entityType = entityType;
        this.material = material;
        this.name = "Kill a " + Utils.getReadableEntityTypeName(this.entityType) + " with a " + Utils.getReadableMaterialName(this.material);
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
		this.listeners.add(new KillEntityWithItemTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<KillEntityWithItemTask> getTasks(int tier) {
        List<KillEntityWithItemTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey1 + "." + tier), taskCount);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey2 + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, entityTypeStrs.size());
        Collections.shuffle(materialStrs);

        for (int i = 0; i < Math.min(loopCount, entityTypeStrs.size()); i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new KillEntityWithItemTask(entityType, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        EntityType entityType = event.getEntity().getType();
        if (entityType != this.entityType) return;

        if (!(event.getEntity().getKiller() instanceof Player)) return;
        Player player = event.getEntity().getKiller();

        Material itemInHand = player.getInventory().getItemInMainHand().getType();
        if (itemInHand != this.material) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class KillEntityWithItemTaskPlayerInteractListener implements Listener {
    private final KillEntityWithItemTask task;

    public KillEntityWithItemTaskPlayerInteractListener(KillEntityWithItemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDeathEvent(event);
    }
}

