package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class PunchPlayerWithItemTask extends LockinTask {
    private static final String configKey = "punchPlayerWithItemTask";
    private static final String normalKey = "materials";
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public PunchPlayerWithItemTask(Material material) {
        super();
        this.material = material;
        this.name = "Punch an enemy player with a " + Utils.getReadableMaterialName(this.material);
        this.item = new ItemStack(this.material);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                Material.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new PunchPlayerWithItemTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<PunchPlayerWithItemTask> getTasks(int tier) {
        List<PunchPlayerWithItemTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, materialStrs.size());

        Collections.shuffle(materialStrs);

        for (int i = 0; i < loopCount; i++) {
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new PunchPlayerWithItemTask(material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (LockinTask.lockinTeamHandler.getTeamName(damager) == LockinTask.lockinTeamHandler.getTeamName(victim)) return;

        Material itemInHand = damager.getInventory().getItemInMainHand().getType();
        if (itemInHand != this.material) return;

        this.complete(damager);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class PunchPlayerWithItemTaskPlayerInteractListener implements Listener {
    private final PunchPlayerWithItemTask task;

    public PunchPlayerWithItemTaskPlayerInteractListener(PunchPlayerWithItemTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityDamageByEntityEvent(event);
    }
}

