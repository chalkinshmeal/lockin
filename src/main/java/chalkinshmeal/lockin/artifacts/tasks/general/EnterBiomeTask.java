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

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.utils.Utils;

public class EnterBiomeTask extends LockinTask {
    private static final String configKey = "enterBiomeTask";
    private static final String normalKey = "biomes";
    private final Biome biome;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EnterBiomeTask(Biome biome, Material material) {
        super();
        this.biome = biome;
        this.name = "Enter a " + Utils.getReadableBiomeName(biome) + " biome";
        this.item = new ItemStack(material);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                Material.valueOf(valueStr);
                Biome.valueOf(valueStr);
                String materialStr = configHandler.getString(configKey + "." + normalKey + "." + valueStr, "NotAvailable");
                Material.valueOf(materialStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new EnterBiomeTaskPlayerItemConsumeListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EnterBiomeTask> getTasks(int tier) {
        List<EnterBiomeTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = normalKey;
        List<String> biomeStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey + "." + tier), taskCount);
        int loopCount = Math.min(taskCount, biomeStrs.size());

        for (int i = 0; i < loopCount; i++) {
            Biome biome = Biome.valueOf(biomeStrs.get(i));
            Material material = Material.valueOf(configHandler.getString(configKey + "." + subKey + "." + tier + "." + biomeStrs.get(i), "NotAvailable"));
            tasks.add(new EnterBiomeTask(biome, material));
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
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onPlayerMoveEvent(event);
    }
}

