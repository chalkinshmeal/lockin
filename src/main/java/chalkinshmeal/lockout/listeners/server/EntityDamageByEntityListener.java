package chalkinshmeal.lockout.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import chalkinshmeal.lockout.artifacts.game.GameHandler;

public class EntityDamageByEntityListener implements Listener {
    private final GameHandler gameHandler;

    public EntityDamageByEntityListener(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        this.gameHandler.onEntityDamageByEntityEvent(event);
    }
}