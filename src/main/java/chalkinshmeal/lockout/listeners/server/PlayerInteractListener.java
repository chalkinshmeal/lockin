package chalkinshmeal.lockout.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;

public class PlayerInteractListener implements Listener {
    private final LockoutCompass lockoutCompass;

    public PlayerInteractListener(LockoutCompass lockoutCompass) {
        this.lockoutCompass = lockoutCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.lockoutCompass.onPlayerInteractEvent(event);
    }
}