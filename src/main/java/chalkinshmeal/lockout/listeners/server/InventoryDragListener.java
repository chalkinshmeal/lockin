package chalkinshmeal.lockout.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;

public class InventoryDragListener implements Listener {
    private final LockoutCompass lockoutCompass;

    public InventoryDragListener(LockoutCompass lockoutCompass) {
        this.lockoutCompass = lockoutCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        this.lockoutCompass.onInventoryDragEvent(event);
    }
}