package chalkinshmeal.lockin.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class WorldUtils {
    //-------------------------------------------------------------------------
    // World State
    //-------------------------------------------------------------------------
    public static List<World> getAllWorlds() {
        List<World> worlds = new ArrayList<>();
        worlds.add(Bukkit.getWorld("world"));
        worlds.add(Bukkit.getWorld("world_nether"));
        worlds.add(Bukkit.getWorld("world_the_end"));
        return worlds;
    }

    public static void resetWorldState() {
        for (World world : WorldUtils.getAllWorlds()) {
            world.setTime(1000);
        }
    }

    public static void setGameRule(@SuppressWarnings("rawtypes") GameRule gameRule, boolean state) {
        for (World world : WorldUtils.getAllWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
        }
    }

    //-------------------------------------------------------------------------
    // World Creation
    //-------------------------------------------------------------------------
    public static void createSpawnCage()
    {
        Location location = Bukkit.getWorld("world").getSpawnLocation();
        WorldUtils.createHollowCube(location);
    }

    public static void destroySpawnCage()
    {
        Location location = Bukkit.getWorld("world").getSpawnLocation();
        WorldUtils.destroyHollowCube(location);
    }

    // Create a 5x5 hollow cube of bedrock centered around the given location
    public static void createHollowCube(Location center) {
        World world = center.getWorld();
        int startX = center.getBlockX() - 2;  // Adjust to make it centered
        int startY = center.getBlockY() - 1;
        int startZ = center.getBlockZ() - 2;

        // Loop through a 5x5x5 area
        for (int x = startX; x < startX + 5; x++) {
            for (int y = startY; y < startY + 5; y++) {
                for (int z = startZ; z < startZ + 5; z++) {
                    boolean isEdge = (x == startX || x == startX + 4 || 
                                      y == startY || y == startY + 4 || 
                                      z == startZ || z == startZ + 4);

                    // Only place obsidian on the edges, leaving the inside hollow
                    if (isEdge) {
                        world.getBlockAt(x, y, z).setType(Material.BEDROCK);
                    }
                    else {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }

        // Light
        world.getBlockAt(startX+3, startY+3, startZ+3).setType(Material.LIGHT);
    }

    // Destroy cube made from createHollowCube
    public static void destroyHollowCube(Location center) {
        World world = center.getWorld();
        int startX = center.getBlockX() - 2;  // Adjust to make it centered
        int startY = center.getBlockY() - 1;
        int startZ = center.getBlockZ() - 2;

        // Loop through a 5x5x5 area
        for (int x = startX; x < startX + 5; x++) {
            for (int y = startY; y < startY + 5; y++) {
                for (int z = startZ; z < startZ + 5; z++) {
                    boolean isEdge = (x == startX || x == startX + 4 || 
                                      y == startY || y == startY + 4 || 
                                      z == startZ || z == startZ + 4);

                    // Only place obsidian on the edges, leaving the inside hollow
                    if (isEdge) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }
}
