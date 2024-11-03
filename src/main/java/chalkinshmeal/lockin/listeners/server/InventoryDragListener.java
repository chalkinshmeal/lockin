package chalkinshmeal.lockin.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;

public class InventoryDragListener implements Listener {
    private final LockinCompass lockinCompass;

    public InventoryDragListener(LockinCompass lockinCompass) {
        this.lockinCompass = lockinCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        this.lockinCompass.onInventoryDragEvent(event);
    }
}