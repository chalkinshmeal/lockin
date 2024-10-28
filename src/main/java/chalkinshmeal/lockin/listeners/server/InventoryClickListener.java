package chalkinshmeal.lockin.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import chalkinshmeal.lockin.artifacts.compass.lockinCompass;

public class InventoryClickListener implements Listener {
    private final lockinCompass lockinCompass;

    public InventoryClickListener(lockinCompass lockinCompass) {
        this.lockinCompass = lockinCompass;
    }

    /** Event Handler */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        this.lockinCompass.onInventoryClickEvent(event);
    }
}