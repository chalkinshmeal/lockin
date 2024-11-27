package chalkinshmeal.lockin.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class EntityUtils {
    public static float getDistance(Entity e1, Entity e2) {
        return Utils.round((float) e1.getLocation().distance(e2.getLocation()), 2);
    }

    public static Player getPlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    // Fetches player name regardless of if they are online or offline
    public static String getPlayerName(UUID uuid) {
        if (uuid == null) return "No Player";
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
        return "No Player";
    }

    public static void resetPlayerState(Player player, boolean teleportToSpawn) {
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setFireTicks(0);
        player.setExp(0);
        player.setLevel(0);
        player.setGlowing(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());

        World world = Bukkit.getWorld("world");
        Location spawnLocation = world.getSpawnLocation();
        Location teleportLocation = new Location(
            world,
            spawnLocation.getX() + 0.5,
            spawnLocation.getY() + 0.1,
            spawnLocation.getZ() + 0.5);
        player.teleport(teleportLocation);
    }

}
