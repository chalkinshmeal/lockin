package chalkinshmeal.lockin.artifacts.tasks.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;


public class SpecificDeathTask extends LockinTask {
    private static final String configKey = "specificDeathTask";
    private static final String normalKey = "damageCauses";
    private static final String punishmentKey = "punishmentDamageCauses";
    private static final String materialSubKey = "material";
    private static final String explanationSubKey = "explanation";
    private final DamageCause damageCause;
    private final Material material;

    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public SpecificDeathTask(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                            LockinRewardHandler lockinRewardHandler, DamageCause damageCause, Material material, String explanation, boolean isPunishment) {
        super(plugin, configHandler, lockinTaskHandler, lockinRewardHandler);
        this.damageCause = damageCause;
        this.material = material;
        this.name = explanation;
        this.item = new ItemStack(this.material);
        this.isPunishment = isPunishment;
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {
        for (String tierStr : this.configHandler.getKeyListFromKey(configKey + "." + normalKey)) {
            for (String valueStr : this.configHandler.getListFromKey(configKey + "." + normalKey + "." + tierStr)) {
                DamageCause.valueOf(valueStr);
            }
        }
    }

    public void addListeners() {
		this.listeners.add(new SpecificDeathTaskEntityDeathEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<SpecificDeathTask> getTasks(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                                          LockinRewardHandler lockinRewardHandler, boolean isPunishment) {
        List<SpecificDeathTask> tasks = new ArrayList<>();
        int taskCount = (isPunishment) ? -1 : configHandler.getInt(configKey + "." + maxTaskCount, 1);
        String subKey = (isPunishment) ? punishmentKey : normalKey;
        List<String> damageCauseStrs = Utils.getRandomItems(configHandler.getKeyListFromKey(configKey + "." + subKey), taskCount);
        int loopCount = (isPunishment) ? damageCauseStrs.size() : taskCount;
        if (damageCauseStrs.size() == 0) {
            plugin.getLogger().warning("Could not find any entries at config key '" + configKey + "'. Skipping " + configKey);
            return tasks;
        }

        for (int i = 0; i < loopCount; i++) {
            String damageCauseStr = damageCauseStrs.get(i);
            DamageCause damageCause = DamageCause.valueOf(damageCauseStrs.get(i));
            Material material = configHandler.getMaterialFromKey(configKey + "." + subKey + "." + damageCauseStr + "." + materialSubKey);
            String explanation = configHandler.getString(configKey + "." + subKey + "." + damageCauseStr + "." + explanationSubKey, "");
            tasks.add(new SpecificDeathTask(plugin, configHandler, lockinTaskHandler, lockinRewardHandler, damageCause, material, explanation, isPunishment));
        }
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (player.getLastDamageCause() == null) return;

        DamageCause cause = player.getLastDamageCause().getCause();
        if (cause != this.damageCause) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class SpecificDeathTaskEntityDeathEventListener implements Listener {
    private final SpecificDeathTask task;

    public SpecificDeathTaskEntityDeathEventListener(SpecificDeathTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (this.task.isComplete()) return;
        this.task.onEntityDeathEvent(event);
    }
}
