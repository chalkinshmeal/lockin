package chalkinshmeal.lockout.artifacts.countdown;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockout.data.ConfigHandler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CountdownBossBar {
    private final JavaPlugin plugin;
    private final BossBar bossBar;
    private final Map<Player, BossBar> playerBossBars;
    private int totalTime; // Total time in seconds

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public CountdownBossBar(JavaPlugin plugin, ConfigHandler configHandler, int totalTime) {
        this.plugin = plugin;
        this.totalTime = totalTime;
        this.bossBar = BossBar.bossBar(Component.text("Initializing...", NamedTextColor.WHITE), (float) 1.0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        this.playerBossBars = new HashMap<>();
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar playerBossBar = playerBossBars.get(player);
            if (playerBossBar != null) {
                player.hideBossBar(playerBossBar);
            }
        }
    }

    //---------------------------------------------------------------------------------------------
    // Task methods 
    //---------------------------------------------------------------------------------------------
    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (totalTime <= 0) {
                    this.cancel();
                    update(Component.text("Time's up!", NamedTextColor.RED));
                    return;
                }

                totalTime--;
                int minutes = totalTime / 60;
                int seconds = totalTime % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);

                update(Component.text(timeString, NamedTextColor.GREEN));
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

}
