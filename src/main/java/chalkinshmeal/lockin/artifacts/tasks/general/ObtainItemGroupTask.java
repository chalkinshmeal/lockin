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
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class ObtainItemGroupTask extends LockinTask {
    private static final String configKey = "obtainItemGroupTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final int amount;
    private final List<Material> validMaterials;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ObtainItemGroupTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                           LockinRewardHandler lockinRewardHandler, Material material, String description, int amount, 
                           List<Material> validMaterials) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.material = material;
        this.amount = amount;
        this.validMaterials = validMaterials;
        this.name = description;
        this.item = new ItemStack(this.material);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String materialStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey + tierStr)) {
                Material.valueOf(materialStr);
                for (String validMaterialStr : this.configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr + "." + materialStr + ".materials")) {
                    Material.valueOf(validMaterialStr);
                }
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new ObtainItemGroupTaskEntityPickupItemEventListener(this));
		this.listeners.add(new ObtainItemGroupTaskInventoryClickEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ObtainItemGroupTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, int tier) {
        List<ObtainItemGroupTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = taskCount;

        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            String description = configHandler.getString(configKey + "." + subKey + "." + tier + "." + materialStr + ".description", "Not found");
            int amount = configHandler.getInt(configKey + "." + subKey + "." + tier + "." + materialStr + ".amount", 1);
            List<Material> validMaterials = configHandler.getMaterialsFromKey(configKey + "." + subKey + "." + tier + "." + materialStr + ".materials");
            tasks.add(new ObtainItemGroupTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, material, description, amount, validMaterials));
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

        if (Utils.getMaterialCount(player, validMaterials, itemType) < this.amount) return;
        this.complete(player);
    }

    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        Material itemType = event.getCurrentItem().getType();
        if (Utils.getMaterialCount(player, validMaterials, itemType) < this.amount) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ObtainItemGroupTaskEntityPickupItemEventListener implements Listener {
    private final ObtainItemGroupTask task;

    public ObtainItemGroupTaskEntityPickupItemEventListener(ObtainItemGroupTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityPickupItemEvent(event);
    }
}

class ObtainItemGroupTaskInventoryClickEventListener implements Listener {
    private final ObtainItemGroupTask task;

    public ObtainItemGroupTaskInventoryClickEventListener(ObtainItemGroupTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (this.task.isComplete()) return;
        this.task.onInventoryClickEvent(event);
    }
}