package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.lockinTask;
import chalkinshmeal.lockin.artifacts.tasks.lockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;

public class ShootBlockTask extends lockinTask {
    private static final String configKey = "shootBlockTask";
    private static final String normalKey = "materials";
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public ShootBlockTask(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                       lockinRewardHandler lockinRewardHandler, Material material) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.material = material;
        this.name = "Hit a " + Utils.getReadableMaterialName(material) + " with an arrow";
        this.item = new ItemStack(this.material);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String materialStr : this.configHandler.getListFromKey(configKey + "." + normalKey)) {
            Material.valueOf(materialStr);
        }
    }

    public void addListeners() {
		this.listeners.add(new ShootBlockTaskPlayerItemConsumeListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<ShootBlockTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, lockinTaskHandler lockinTaskHandler,
                                                          lockinRewardHandler lockinRewardHandler) {
        List<ShootBlockTask> tasks = new ArrayList<>();
        int taskCount = configHandler.getInt(configKey + "." + maxTaskCount, 1);
        List<String> materialStrs = Utils.getRandomItems(configHandler.getListFromKey(configKey + "." + normalKey), taskCount);
        if (materialStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }

        for (int i = 0; i < taskCount; i++) {
            Material material = Material.valueOf(materialStrs.get(i));
            tasks.add(new ShootBlockTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, material));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getHitBlock() == null) return;
        if (event.getHitBlock().getType() != this.material) return;

        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Arrow)) return;

        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player)) return;

        Player player = (Player) shooter;
        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class ShootBlockTaskPlayerItemConsumeListener implements Listener {
    private final ShootBlockTask task;

    public ShootBlockTaskPlayerItemConsumeListener(ShootBlockTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (this.task.isComplete()) return;
        this.task.onProjectileHitEvent(event);
    }
}

