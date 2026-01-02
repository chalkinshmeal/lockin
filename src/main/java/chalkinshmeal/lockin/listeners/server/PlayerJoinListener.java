package chalkinshmeal.lockin.listeners.server;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.game.GameHandler;

public class PlayerJoinListener implements Listener {
    private final LockinCompass lockinCompass;
    private final GameHandler gameHandler;

    public PlayerJoinListener(LockinCompass lockinCompass, GameHandler gameHandler) {
        this.lockinCompass = lockinCompass;
        this.gameHandler = gameHandler;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.lockinCompass.giveCompass(event.getPlayer());
        if (!this.gameHandler.isActive()) {
            event.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
    }
}