package chalkinshmeal.lockout.listeners.server;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;
import chalkinshmeal.lockout.artifacts.game.GameHandler;
import chalkinshmeal.lockout.artifacts.scoreboard.LockoutScoreboard;

public class PlayerJoinListener implements Listener {
    private final LockoutCompass lockoutCompass;
    private final LockoutScoreboard lockoutScoreboard;
    private final GameHandler gameHandler;

    public PlayerJoinListener(LockoutCompass lockoutCompass, LockoutScoreboard lockoutScoreboard, GameHandler gameHandler) {
        this.lockoutCompass = lockoutCompass;
        this.lockoutScoreboard = lockoutScoreboard;
        this.gameHandler = gameHandler;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.lockoutCompass.giveCompass(event.getPlayer());
        if (this.gameHandler.isActive) {
            this.lockoutScoreboard.showToPlayer(event.getPlayer());
        }
        else {
            event.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
    }
}