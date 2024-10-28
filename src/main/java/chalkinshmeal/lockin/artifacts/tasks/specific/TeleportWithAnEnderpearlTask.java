package chalkinshmeal.lockin.artifacts.tasks.specific;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;

public class TeleportWithAnEnderpearlTask extends lockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public TeleportWithAnEnderpearlTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler, lockinRewardHandler lockinRewardHandler) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.name = "Teleport with an enderpearl";
        this.item = new ItemStack(Material.ENDER_PEARL);
        this.value = 1;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new TeleportWithAnEnderpearlTaskPlayerInteractListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        this.complete(event.getPlayer());
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class TeleportWithAnEnderpearlTaskPlayerInteractListener implements Listener {
    private final TeleportWithAnEnderpearlTask task;

    public TeleportWithAnEnderpearlTaskPlayerInteractListener(TeleportWithAnEnderpearlTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerTeleportEvent(event);
    }
}