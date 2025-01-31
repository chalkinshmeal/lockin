package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.LoggerUtils;
import chalkinshmeal.lockin.utils.Utils;

public class ObtainItemWithEnchantmentTask extends LockinTask {
    private static final String configKey = "obtainItemWithEnchantmentTask";
    private static final String normalKey = "enchantments";
    private Enchantment enchantment;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ObtainItemWithEnchantmentTask(Enchantment enchantment) {
        super();
        this.enchantment = enchantment;
        this.name = "Obtain an item with " + Utils.getReadableEnchantmentName(this.enchantment);
        this.item = new ItemStack(Material.ENCHANTING_TABLE);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                Enchantment enchantment = Utils.getEnchantmentByString(valueStr);
                if (enchantment == null) {
                    LoggerUtils.info("ERROR: Invalid enchantment found: " + valueStr);
                }
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new ObtainItemWithEnchantmentTaskEntityPickupItemEventListener(this));
		this.listeners.add(new ObtainItemWithEnchantmentTaskInventoryClickEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ObtainItemWithEnchantmentTask> getTasks(int tier) {
        List<ObtainItemWithEnchantmentTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> enchantmentStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, enchantmentStrs.size());
        Collections.shuffle(enchantmentStrs);

        for (int i = 0; i < Math.min(loopCount, enchantmentStrs.size()); i++) {
            Enchantment enchantment = Utils.getEnchantmentByString(enchantmentStrs.get(i));
            tasks.add(new ObtainItemWithEnchantmentTask(enchantment));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        // Return if not a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Return if item doesn't contain target enchantment
        ItemStack item = event.getItem().getItemStack();
        if (!item.getEnchantments().containsKey(this.enchantment)) return;

        this.complete(player);
    }

    public void onInventoryClickEvent(InventoryClickEvent event) {
        // Return if not a player
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Return if no item clicked
        if (event.getCurrentItem() == null) return;

        // Return if item doesn't contain target enchantment
        ItemStack item = event.getCurrentItem();
        if (!item.getEnchantments().containsKey(this.enchantment)) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ObtainItemWithEnchantmentTaskEntityPickupItemEventListener implements Listener {
    private final ObtainItemWithEnchantmentTask task;

    public ObtainItemWithEnchantmentTaskEntityPickupItemEventListener(ObtainItemWithEnchantmentTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onEntityPickupItemEvent(event);
    }
}

class ObtainItemWithEnchantmentTaskInventoryClickEventListener implements Listener {
    private final ObtainItemWithEnchantmentTask task;

    public ObtainItemWithEnchantmentTaskInventoryClickEventListener(ObtainItemWithEnchantmentTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onInventoryClickEvent(event);
    }
}