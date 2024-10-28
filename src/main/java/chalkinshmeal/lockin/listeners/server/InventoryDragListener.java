package chalkinshmeal.lockin.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import chalkinshmeal.lockin.artifacts.compass.lockinCompass;

public class InventoryDragListener implements Listener {
    private final lockinCompass lockinCompass;

    public InventoryDragListener(lockinCompass lockinCompass) {
        this.lockinCompass = lockinCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        this.lockinCompass.onInventoryDragEvent(event);
    }
}