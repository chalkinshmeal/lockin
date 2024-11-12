package chalkinshmeal.lockin.artifacts.tasks.general;

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

public class EatTask extends LockinTask {
    private static final String configKey = "eatTask";
    private static final String normalKey = "consumes";
    private final int targetConsumes;
    private Map<Player, Integer> consumeCounts;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EatTask(int targetConsumes) {
        super();
        this.targetConsumes = targetConsumes;
        this.name = "Eat " + this.targetConsumes + " items";
        this.item = new ItemStack(Material.COOKED_BEEF);
        this.consumeCounts = new HashMap<>();
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
    }

    public void addListeners() {
		this.listeners.add(new EatItemsTaskPlayerItemConsumeEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EatTask> getTasks(int tier) {
        List<EatTask> tasks = new ArrayList<>();
        int targetConsumes = configHandler.getInt(configKey + "." + normalKey + "." + tier, 10);
        tasks.add(new EatTask(targetConsumes));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        this.consumeCounts.put(player, this.consumeCounts.getOrDefault(player, 0) + 1);
        if (this.consumeCounts.get(player) < this.targetConsumes) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EatItemsTaskPlayerItemConsumeEventListener implements Listener {
    private final EatTask task;

    public EatItemsTaskPlayerItemConsumeEventListener(EatTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerItemConsumeEvent(event);
    }
}