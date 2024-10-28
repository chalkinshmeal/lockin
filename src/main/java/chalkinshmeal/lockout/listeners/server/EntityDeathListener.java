package chalkinshmeal.lockout.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import chalkinshmeal.lockout.artifacts.game.GameHandler;

public class EntityDeathListener implements Listener {
    private final GameHandler gameHandler;

    public EntityDeathListener(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        this.gameHandler.onEntityDeathEvent(event);
    }
}