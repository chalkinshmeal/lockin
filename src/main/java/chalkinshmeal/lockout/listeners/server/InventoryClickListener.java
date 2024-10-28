package chalkinshmeal.lockout.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;

public class InventoryClickListener implements Listener {
    private final LockoutCompass lockoutCompass;

    public InventoryClickListener(LockoutCompass lockoutCompass) {
        this.lockoutCompass = lockoutCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        this.lockoutCompass.onInventoryClickEvent(event);
    }
}