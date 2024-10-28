package chalkinshmeal.lockout.artifacts.tasks.specific;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.data.ConfigHandler;

public class TeleportWithAnEnderpearlTask extends LockoutTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockouttaskhandler
    //---------------------------------------------------------------------------------------------
    public TeleportWithAnEnderpearlTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler, LockoutRewardHandler lockoutRewardHandler) {
        super(plugin, configHandler, lockoutTaskHandler, lockoutRewardHandler);
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
    // Any listeners. Upon completion, LockoutTaskHandler.CompleteTask(player);
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