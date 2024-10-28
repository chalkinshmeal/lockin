package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class EnterBiomeTask extends lockinTask {
    private static final String configKey = "enterBiomeTask";
    private static final String normalKey = "biomes";
    private static final String punishmentKey = "punishmentBiomes";
    private final Biome biome;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EnterBiomeTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                          lockinRewardHandler lockinRewardHandler, Biome biome, Material material, boolean isPunishment) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.biome = biome;
        this.name = "Enter a " + Utils.getReadableBiomeName(biome) + " biome";
        this.item = new ItemStack(material);
        this.isPunishment = isPunishment;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String biomeStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            Biome.valueOf(biomeStr);
            String materialStr = this.configHandler.getString(configKey + "." + normalKey + "." + biomeStr, "NotAvailable");
            Material.valueOf(materialStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new EnterBiomeTaskPlayerItemConsumeListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EnterBiomeTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler, boolean isPunishment) {
        List<EnterBiomeTask> tasks = new ArrayList<>();
        int taskCount = (isPunishment) ? -1 : configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = (isPunishment) ? punishmentKey : normalKey;
        List<String> biomeStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey), taskCount);
        int loopCount = (isPunishment) ? biomeStrs.size() : taskCount;
        if (biomeStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }

        for (int i = 0; i < loopCount; i++) {
            Biome biome = Biome.valueOf(biomeStrs.get(i));
            Material material = Material.valueOf(configHandler.getString(configKey + "." + subKey + "." + biomeStrs.get(i), "NotAvailable"));
            tasks.add(new EnterBiomeTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, biome, material, isPunishment));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        if (currentBiome != this.biome) return;

        this.complete(event.getPlayer());
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EnterBiomeTaskPlayerItemConsumeListener implements Listener {
    private final EnterBiomeTask task;

    public EnterBiomeTaskPlayerItemConsumeListener(EnterBiomeTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (this.task.isComplete()) return;
        this.task.onPlayerMoveEvent(event);
    }
}

