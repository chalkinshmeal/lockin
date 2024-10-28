package chalkinshmeal.lockin.listeners.server;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import chalkinshmeal.lockin.artifacts.compass.lockinCompass;
import chalkinshmeal.lockin.artifacts.game.GameHandler;
import chalkinshmeal.lockin.artifacts.scoreboard.lockinScoreboard;

public class PlayerJoinListener implements Listener {
    private final lockinCompass lockinCompass;
    private final lockinScoreboard lockinScoreboard;
    private final GameHandler gameHandler;

    public PlayerJoinListener(lockinCompass lockinCompass, lockinScoreboard lockinScoreboard, GameHandler gameHandler) {
        this.lockinCompass = lockinCompass;
        this.lockinScoreboard = lockinScoreboard;
        this.gameHandler = gameHandler;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.lockinCompass.giveCompass(event.getPlayer());
        if (this.gameHandler.isActive) {
            this.lockinScoreboard.showToPlayer(event.getPlayer());
        }
        else {
            event.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
    }
}