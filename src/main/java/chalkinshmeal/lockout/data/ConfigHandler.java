package chalkinshmeal.lockout.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 ConfigHandler class
 This class handles any manipulation of the config.yml for this plugin
 - getConfig() - returns a Map of the config
 - setConfig() - saves a Map of the config to file
 */
public class ConfigHandler {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private File file;

    // Constructor
    public ConfigHandler(JavaPlugin plugin) {
        this(plugin, "plugins/lockout/config.yml");
    }
    public ConfigHandler(JavaPlugin plugin, String configPath) {
        this.plugin = plugin;

        // Create file + directories, if necessary
        this.file = new File(configPath);
        this.file.getParentFile().mkdirs();
        this.plugin.saveDefaultConfig();

        this.config = new YamlConfiguration();
        try {
            this.config.load(this.file);
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /** Checkers */
    public boolean doesKeyExist(String key) {
        return this.config.contains(key);
    }

    /** Special getters */
    public List<Material> getMaterialsFromKey(String key) {
        try {
            List<String> materialStrs = this.getListFromKey(key);
            List<Material> materials = new ArrayList<>();
            for (String materialStr : materialStrs) {
                materials.add(Material.valueOf(materialStr.toUpperCase()));
            }
            return materials;
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return null;
        }
    }

    public List<String> getListFromKey(String key) {
        try {
            return this.config.getStringList(key);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return new ArrayList<>();
        }
    }

    public Material getMaterialFromKey(String key) {
        try {
            String material_str = (String) this.getValue(key);
            return Material.valueOf(material_str.toUpperCase());
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return null;
        }
    }
    public ArrayList<String> getKeyListFromKey(String key) {
        try {
            Set<String> keys = ((MemorySection) this.getValue(key)).getKeys(false);
            return new ArrayList<>(keys);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return new ArrayList<>();
        }
    }

    /** Getters */
    public Object getValue(String key) {
        try {
            return this.config.get(key);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return this.config.get(key);
        }
    }
    public int getInt(String key, int defaultVal) {
        try {
            String val = this.config.getString(key);
            if (val == null) return defaultVal;
            return this.config.getInt(key);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return defaultVal;
        }
    }
    public String getString(String key, String defaultVal) {
        try {
            String str = this.config.getString(key);
            if (str == null) return defaultVal;
            return str;
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return defaultVal;
        }
    }
    public float getFloat(String key, float defaultVal) {
        try { return (float) this.config.getDouble(key); }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not load '" + key + "': " + e);
            return defaultVal;
        }
    }
    public Location getLocation(String key) {
        String string = getString(key, null);
        if (string == null) return null;

        return this.toLocation(string);
    }


    /** Setters */
    public void setValue(String key, Object value) {
        try {
            this.config.set(key, value);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not set '" + key + "' to value '" + value + "'");
        }
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setLocation(String key, Location loc) {
        this.setValue(key, this.toString(loc));
    }

    public String toString(Location loc) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        String locStr = loc.getWorld().getName();
        locStr += "," + df.format(loc.getX());
        locStr += "," + df.format(loc.getY());
        locStr += "," + df.format(loc.getZ());
        return locStr;
    }
    public Location toLocation(String str) {
        List<String> strList = List.of(str.split(","));
        World world = plugin.getServer().getWorld(strList.get(0));
        double x = Double.parseDouble(strList.get(1));
        double y = Double.parseDouble(strList.get(2));
        double z = Double.parseDouble(strList.get(3));
        return new Location(world, x, y, z);
    }
}
