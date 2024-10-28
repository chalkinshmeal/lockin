package chalkinshmeal.lockin.artifacts.tasks.general;

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

public class EquipArmorTask extends lockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EquipArmorTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                         lockinRewardHandler lockinRewardHandler) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.name = "Equip armor";
        this.item = new ItemStack(Material.CHAINMAIL_BOOTS);
        this.isPunishment = true;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new EquipArmorTaskPlayerArmorChangeListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EquipArmorTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<EquipArmorTask> tasks = new ArrayList<>();
        tasks.add(new EquipArmorTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler));
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = event.getNewItem();
        if (newItem == null) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EquipArmorTaskPlayerArmorChangeListener implements Listener {
    private final EquipArmorTask task;

    public EquipArmorTaskPlayerArmorChangeListener(EquipArmorTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerArmorChangeEvent(PlayerArmorChangeEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerArmorChangeEvent(event);
    }
}

