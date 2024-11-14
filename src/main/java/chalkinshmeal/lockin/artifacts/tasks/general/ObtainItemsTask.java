package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class ObtainItemsTask extends LockinTask {
    private static final String configKey = "obtainItemsTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final int amount;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ObtainItemsTask(Material material, int amount) {
        super();
        this.material = material;
        this.amount = amount;
        this.name = "Obtain " + ((this.amount == 1 ? "a" : this.amount)) + " " + Utils.getReadableMaterialName(material);
        this.item = new ItemStack(this.material);
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
		this.listeners.add(new ObtainItemsTaskEntityPickupItemEventListener(this));
		this.listeners.add(new ObtainItemsTaskInventoryClickEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ObtainItemsTask> getTasks(int tier) {
        List<ObtainItemsTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = taskCount;

        if (materialStrs.size() == 0) {
            
            return tasks;
        }
        for (int i = 0; i < Math.min(loopCount, materialStrs.size()); i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + subKey + "." + tier + "." + materialStr, 1);
            tasks.add(new ObtainItemsTask(material, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Material itemType = event.getItem().getItemStack().getType();
        if (itemType != this.material) return;
        if (!Utils.hasMaterial(player, this.material, this.amount, event.getItem().getItemStack())) return;

        this.complete(player);
    }
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        Material itemType = event.getCurrentItem().getType();
        if (itemType != this.material) return;
        if (!Utils.hasMaterial(player, this.material, this.amount, event.getCurrentItem())) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ObtainItemsTaskEntityPickupItemEventListener implements Listener {
    private final ObtainItemsTask task;

    public ObtainItemsTaskEntityPickupItemEventListener(ObtainItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityPickupItemEvent(event);
    }
}

class ObtainItemsTaskInventoryClickEventListener implements Listener {
    private final ObtainItemsTask task;

    public ObtainItemsTaskInventoryClickEventListener(ObtainItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onInventoryClickEvent(event);
    }
}