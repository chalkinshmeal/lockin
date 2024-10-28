package chalkinshmeal.lockin.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class WearFullDyedLeatherArmorTask extends lockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public WearFullDyedLeatherArmorTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler, lockinRewardHandler lockinRewardHandler) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.name = "Wear a full set of dyed leather armor";
        this.item = new ItemStack(Material.LEATHER_CHESTPLATE);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new WearFullDyedLeatherArmorTaskPlayerArmorChangeListener(this));
    }

    public static List<WearFullDyedLeatherArmorTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler,
                                                              lockinTaskHandler lockinTaskHandler, lockinRewardHandler lockinRewardHandler) {
        List<WearFullDyedLeatherArmorTask> tasks = new ArrayList<>();
        tasks.add(new WearFullDyedLeatherArmorTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();

        ItemStack[] armorContents = player.getInventory().getArmorContents();

        // Check if all armor pieces are dyed leather
        if (!(Utils.isDyedLeatherArmor(armorContents[0]) && // Helmet
              Utils.isDyedLeatherArmor(armorContents[1]) && // Chestplate
              Utils.isDyedLeatherArmor(armorContents[2]) && // Leggings
              Utils.isDyedLeatherArmor(armorContents[3]))) return;
        
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class WearFullDyedLeatherArmorTaskPlayerArmorChangeListener implements Listener {
    private final WearFullDyedLeatherArmorTask task;

    public WearFullDyedLeatherArmorTaskPlayerArmorChangeListener(WearFullDyedLeatherArmorTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerArmorChangeEvent(event);
    }
}

