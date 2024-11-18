package chalkinshmeal.lockin.artifacts.countdown;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockin.data.ConfigHandler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CountdownBossBar {
    private final JavaPlugin plugin;
    private final BossBar bossBar;
    private final Map<Player, BossBar> playerBossBars;
    private final int totalTime; // Total time in seconds
    private int currentTime;
    private boolean isActive = true;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public CountdownBossBar(JavaPlugin plugin, ConfigHandler configHandler, int totalTime) {
        this.plugin = plugin;
        this.bossBar = BossBar.bossBar(Component.text("Initializing...", NamedTextColor.WHITE), (float) 1.0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        this.playerBossBars = new HashMap<>();
        this.totalTime = totalTime;
        this.currentTime = totalTime;
    }

    //---------------------------------------------------------------------------------------------
    // BossBar methods 
    //---------------------------------------------------------------------------------------------
    public void update(Component message) {
        bossBar.name(message);

        // Update all players with the boss bar
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar playerBossBar = playerBossBars.get(player);
            if (playerBossBar == null) {
                playerBossBar = BossBar.bossBar(message, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
                player.showBossBar(playerBossBar);
                playerBossBars.put(player, playerBossBar);
            } else {
                playerBossBar.name(message);
            }
        }
    }

    public void stop() {
        this.isActive = false;

        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar playerBossBar = playerBossBars.get(player);
            if (playerBossBar != null) {
                player.hideBossBar(playerBossBar);
            }
        }
    }

    public void reset() {
        this.currentTime = this.totalTime;
    }

    //---------------------------------------------------------------------------------------------
    // Task methods 
    //---------------------------------------------------------------------------------------------
    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }

                if (currentTime <= 0) {
                    update(Component.text("Time's up!", NamedTextColor.RED));
                }

                currentTime--;
                int minutes = currentTime / 60;
                int seconds = currentTime % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);

                update(Component.text(timeString, NamedTextColor.GREEN));
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

}