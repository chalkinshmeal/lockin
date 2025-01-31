package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class EnterBoatWithPassengerTask extends LockinTask {
    private static final String configKey = "enterBoatWithPassengerTask";
    private static final String normalKey1 = "entityTypes";
    private static final String normalKey2 = "materials";
    private final EntityType entityType;
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EnterBoatWithPassengerTask(EntityType entityType, Material material) {
        super();
        this.entityType = entityType;
        this.material = material;
        this.name = "Enter a " + Utils.getReadableMaterialName(this.material) +
                    " with a " + Utils.getReadableEntityTypeName(this.entityType);
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
		this.listeners.add(new EnterBoatWithPassengerTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EnterBoatWithPassengerTask> getTasks(int tier) {
        List<EnterBoatWithPassengerTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> entityTypeStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey1 + "." + tier), taskCount);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey2 + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, Math.min(entityTypeStrs.size(), materialStrs.size()));
        Collections.shuffle(materialStrs);

        for (int i = 0; i < Math.min(loopCount, entityTypeStrs.size()); i++) {
            EntityType entityType = EntityType.valueOf(entityTypeStrs.get(i));
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new EnterBoatWithPassengerTask(entityType, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof Boat boat)) return;
        if (boat.getBoatMaterial() != this.material) return;
        if (boat.getPassengers().isEmpty()) return;

        // Check if any of the passengers matches the entity type
        boolean containsMob = false;
        for (Entity passenger : boat.getPassengers()) {
            if (passenger.getType() == this.entityType) {
                containsMob = true;
                break;
            }
        }
        if (!containsMob) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EnterBoatWithPassengerTaskPlayerInteractListener implements Listener {
    private final EnterBoatWithPassengerTask task;

    public EnterBoatWithPassengerTaskPlayerInteractListener(EnterBoatWithPassengerTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerInteractEntityEvent(event);
    }
}

