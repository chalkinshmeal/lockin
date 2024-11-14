package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class BreakItemsTask extends LockinTask {
    private static final String configKey = "breakItemsTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final int amount;
    private final Map<Player, Integer> brokenItems;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public BreakItemsTask(Material material, int amount) {
        super();
        this.material = material;
        this.amount = amount;
        this.brokenItems = new HashMap<>();
        this.name = "Break " + this.amount + " " + Utils.getReadableMaterialName(material);
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
		this.listeners.add(new BreakItemsTaskBlockBreakEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<BreakItemsTask> getTasks(int tier) {
        List<BreakItemsTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = taskCount;

        if (materialStrs.size() == 0) {
            
            return tasks;
        }
        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + subKey + "." + tier + "." + materialStr, 1);
            tasks.add(new BreakItemsTask(material, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onBlockBreakEvent(BlockBreakEvent event) {
        // Return if material does not match
        Material itemType = event.getBlock().getType();
        if (itemType != this.material) return;

        // Return if 
        Player player = event.getPlayer();
        this.brokenItems.put(player, this.brokenItems.getOrDefault(player, 0) + 1);
        if (this.brokenItems.get(player) < this.amount) return;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class BreakItemsTaskBlockBreakEventListener implements Listener {
    private final BreakItemsTask task;

    public BreakItemsTaskBlockBreakEventListener(BreakItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onBlockBreakEvent(event);
    }
}
