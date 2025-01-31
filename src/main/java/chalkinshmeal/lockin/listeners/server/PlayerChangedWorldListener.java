package chalkinshmeal.lockin.listeners.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.game.GameHandler;
import chalkinshmeal.lockin.utils.LoggerUtils;

public class PlayerChangedWorldListener implements Listener {
    private final LockinCompass lockinCompass;
    private final GameHandler gameHandler;

    public PlayerChangedWorldListener(LockinCompass lockinCompass, GameHandler gameHandler) {
        this.lockinCompass = lockinCompass;
        this.gameHandler = gameHandler;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        if (this.gameHandler.isActive()) {
            //LoggerUtils.info("ChangedWorldEvent: " + event.getPlayer().getLocation());
            //this.lockinCompass.setLastKnownLocation(event.getFrom());
        }
    }
}