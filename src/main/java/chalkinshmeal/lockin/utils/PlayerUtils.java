package chalkinshmeal.lockin.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerUtils {
    // Fetches player name regardless of if they are online or offline
    public static String getPlayerName(UUID uuid) {
        // Check if the player is online
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        // If the player is not online, check offline data
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.getName() != null) {
            return offlinePlayer.getName();
        }

        // Return null if the player has never joined the server
        return null;
    }
}
