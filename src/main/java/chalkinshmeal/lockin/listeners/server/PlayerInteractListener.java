package chalkinshmeal.lockin.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import chalkinshmeal.lockin.artifacts.compass.lockinCompass;

public class PlayerInteractListener implements Listener {
    private final lockinCompass lockinCompass;

    public PlayerInteractListener(lockinCompass lockinCompass) {
        this.lockinCompass = lockinCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.lockinCompass.onPlayerInteractEvent(event);
    }
}