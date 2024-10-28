package chalkinshmeal.lockout.data;

import org.bukkit.plugin.java.JavaPlugin;

/*
 LootConfig class
 This class handles any manipulation of the config.yml for this plugin
 - getConfig() - returns a Map of the config
 - setConfig() - saves a Map of the config to file
 */
public class LootConfig extends ConfigHandler {
    // Constructor
    public LootConfig(JavaPlugin plugin) {
        super(plugin, "plugins/labyrinth/loot.yml");
    }

}
