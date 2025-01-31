package chalkinshmeal.lockin.artifacts.tasks.lockinTasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class EatItemsTask extends LockinTask {
    private static final String configKey = "eatItemsTask";
    private static final String normalKey = "materials";
    private final Material material;
    private final int amount;
    private final Map<String, Integer> eatenItems;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EatItemsTask(Material material, int amount) {
        super();
        this.material = material;
        this.amount = amount;
        this.eatenItems = new HashMap<>();
        this.name = "Eat " + this.amount + " " + Utils.getReadableMaterialName(material);
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
		this.listeners.add(new EatItemsTaskPlayerItemConsumeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EatItemsTask> getTasks(int tier) {
        List<EatItemsTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> materialStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, materialStrs.size());

        for (int i = 0; i < loopCount; i++) {
            String materialStr = materialStrs.get(i);
            Material material = Material.valueOf(materialStrs.get(i));
            int amount = configHandler.getInt(configKey + "." + subKey + "." + tier + "." + materialStr, 1);
            tasks.add(new EatItemsTask(material, amount));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        // Return if player did not eat
        if (!(event.getPlayer() instanceof Player player)) return;

        // Return if material does not match
        ItemStack eatenItem = event.getItem();
        if (eatenItem.getType() != this.material) return;

        String teamName = LockinTask.lockinTeamHandler.getTeamName(player);
        this.eatenItems.put(teamName, this.eatenItems.getOrDefault(teamName, 0) + 1);
        if (this.eatenItems.get(teamName) < this.amount) return;
        this.complete(player);

    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EatItemsTaskPlayerItemConsumeEventListener implements Listener {
    private final EatItemsTask task;

    public EatItemsTaskPlayerItemConsumeEventListener(EatItemsTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerItemConsumeEvent(event);
    }
}