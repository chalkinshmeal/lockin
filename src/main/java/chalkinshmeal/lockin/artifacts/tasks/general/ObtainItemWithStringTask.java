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

public class ObtainItemWithStringTask extends LockinTask {
    private static final String configKey = "obtainItemWithStringTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final String glob;
    private final int amount;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ObtainItemWithStringTask(Material material, String glob, int amount) {
        super();
        this.material = material;
        this.glob = glob;
        this.amount = amount;
        this.name = "Obtain " + this.amount + " items with '" + this.glob + "' in their name";
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
		this.listeners.add(new ObtainItemWithStringTaskEntityPickupItemEventListener(this));
		this.listeners.add(new ObtainItemWithStringTaskInventoryClickEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ObtainItemWithStringTask> getTasks(int tier) {
        List<ObtainItemWithStringTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, materialStrs.size());

        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            String glob = configHandler.getString(configKey + "." + subKey + "." + tier + "." + materialStr + ".string", "Not found");
            int amount = configHandler.getInt(configKey + "." + subKey + "." + tier + "." + materialStr + ".amount", 1);
            tasks.add(new ObtainItemWithStringTask(material, glob, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack addedItem = event.getItem().getItemStack();

        if (Utils.getMaterialGlobCount(player, this.glob, addedItem) < this.amount) return;
        this.complete(player);
    }

    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        ItemStack addedItem = event.getCurrentItem();
        if (Utils.getMaterialGlobCount(player, this.glob, addedItem) < this.amount) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ObtainItemWithStringTaskEntityPickupItemEventListener implements Listener {
    private final ObtainItemWithStringTask task;

    public ObtainItemWithStringTaskEntityPickupItemEventListener(ObtainItemWithStringTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityPickupItemEvent(event);
    }
}

class ObtainItemWithStringTaskInventoryClickEventListener implements Listener {
    private final ObtainItemWithStringTask task;

    public ObtainItemWithStringTaskInventoryClickEventListener(ObtainItemWithStringTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onInventoryClickEvent(event);
    }
}