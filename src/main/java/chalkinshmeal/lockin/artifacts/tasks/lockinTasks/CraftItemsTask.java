package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class CraftItemsTask extends LockinTask {
    private static final String configKey = "craftItemsTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final int amount;
    private final Map<String, Integer> craftedItems;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public CraftItemsTask(Material material, int amount) {
        super();
        this.material = material;
        this.amount = amount;
        this.craftedItems = new HashMap<>();
        this.name = "Craft " + this.amount + " " + Utils.getReadableMaterialName(material);
        this.item = new ItemStack(this.material);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String materialStr : configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                Material.valueOf(materialStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new CraftItemsTaskCraftItemEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<CraftItemsTask> getTasks(int tier) {
        List<CraftItemsTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, materialStrs.size());

        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + subKey + "." + tier + "." + materialStr, 1);
            tasks.add(new CraftItemsTask(material, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onCraftItemEvent(CraftItemEvent event) {
        // Return if player did not craft
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Return if material does not match
        ItemStack craftedItem = event.getRecipe().getResult();
        if (craftedItem.getType() != this.material) return;

        String teamName = LockinTask.lockinTeamHandler.getTeamName(player);
        this.craftedItems.put(teamName, this.craftedItems.getOrDefault(teamName, 0) + craftedItem.getAmount());
        if (this.craftedItems.get(teamName) < this.amount) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class CraftItemsTaskCraftItemEventListener implements Listener {
    private final CraftItemsTask task;

    public CraftItemsTaskCraftItemEventListener(CraftItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onCraftItemEvent(CraftItemEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onCraftItemEvent(event);
    }
}