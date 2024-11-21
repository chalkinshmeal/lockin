package chalkinshmeal.lockin.artifacts.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.general.*;
import chalkinshmeal.lockin.artifacts.tasks.specific.*;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LockinTaskHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final LockinCompass lockinCompass;
    private LockinRewardHandler lockinRewardHandler;
    private LockinTeamHandler lockinTeamHandler;
    private int tasksPerTier;
    private List<LockinTask> tasks;

    public LockinTaskHandler(JavaPlugin plugin, ConfigHandler configHandler, LockinCompass lockinCompass,
                                LockinScoreboard lockinScoreboard, LockinTeamHandler lockinTeamHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockinCompass = lockinCompass;
        this.lockinRewardHandler = new LockinRewardHandler(this.plugin);
        this.lockinTeamHandler = lockinTeamHandler;
        this.tasks = new ArrayList<>();
        this.tasksPerTier = this.configHandler.getInt("tasksPerTier", 9);

        LockinTask.initStaticVariables(plugin, configHandler, this, lockinRewardHandler, lockinTeamHandler);
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public void setTasksPerTier(int tasksPerTier) { this.tasksPerTier = tasksPerTier; }
    public List<LockinTask> getTasks() { return new ArrayList<>(this.tasks); }
    public boolean hasOneTeamCompletedAllTasks() {
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            boolean hasCompletedAllTasks = true;
            for (LockinTask task : this.tasks) {
                if (!task.hasCompleted(teamName)) hasCompletedAllTasks = false;
            }
            if (hasCompletedAllTasks) return true;
        }
        return false;
    }
    public boolean areAllTasksDone() { 
        for (LockinTask task : this.tasks) {
            if (!task.haveAllTeamsCompleted()) return false;
        }
        return true;
    }

    //---------------------------------------------------------------------------------------------
	// Task methods
    //---------------------------------------------------------------------------------------------
    public void stopCurrentTasks() {
        for (LockinTask task : this.tasks) { task.stop(); }
    }

    // Update the list of tasks for this lockin challenge
    // Return true if successful, false if not
    public boolean updateTaskList(int tier) {
        this.stopCurrentTasks();

        List<LockinTask> allTasks = new ArrayList<>();
        //try {
            // General tasks
            allTasks.addAll(ActivateBlockTask.getTasks(tier));
            allTasks.addAll(BreakItemsTask.getTasks(tier));
            allTasks.addAll(BrewPotionTask.getTasks(tier));
            allTasks.addAll(BreedEntitiesTask.getTasks(tier));
            allTasks.addAll(BlockArrowWithShieldTask.getTasks(tier));
            allTasks.addAll(CatchFishTask.getTasks(tier));
            allTasks.addAll(CraftItemTask.getTasks(tier));
            allTasks.addAll(CreateEntityTask.getTasks(tier));
            allTasks.addAll(DestroyItemTask.getTasks(tier));
            allTasks.addAll(DieTask.getTasks(tier));
            allTasks.addAll(DrinkMilkToCurePoisonTask.getTasks(tier));
            allTasks.addAll(EatTask.getTasks(tier));
            allTasks.addAll(EatItemTask.getTasks(tier));
            allTasks.addAll(EnchantItemTask.getTasks(tier));
            allTasks.addAll(EnterBiomeTask.getTasks(tier));
            allTasks.addAll(EnterBoatWithPassengerTask.getTasks(tier));
            allTasks.addAll(EnterNetherTask.getTasks(tier));
            allTasks.addAll(EquipItemTask.getTasks(tier));
            allTasks.addAll(GetExpLevelTask.getTasks(tier));
            allTasks.addAll(GetSpecificHealthTask.getTasks(tier));
            allTasks.addAll(GrowWheatWithBonemealTask.getTasks(tier));
            allTasks.addAll(InteractItemTask.getTasks(tier));
            allTasks.addAll(JumpTask.getTasks(tier));
            allTasks.addAll(KillEntitiesTask.getTasks(tier));
            allTasks.addAll(KillEntityWithItemTask.getTasks(tier));
            allTasks.addAll(KillEntityWithStatusEffectTask.getTasks(tier));
            allTasks.addAll(KillLeftySkeletonTask.getTasks(tier));
            allTasks.addAll(LaunchFireworkTask.getTasks(tier));
            allTasks.addAll(LightTNTTask.getTasks(tier));
            allTasks.addAll(ObtainItemGroupTask.getTasks(tier));
            allTasks.addAll(ObtainItemWithStringTask.getTasks(tier));
            allTasks.addAll(ObtainItemsTask.getTasks(tier));
            allTasks.addAll(PlaceBookOnLecternTask.getTasks(tier));
            allTasks.addAll(PlaceFlowerInPotTask.getTasks(tier));
            allTasks.addAll(PlaceItemInItemFrameTask.getTasks(tier));
            allTasks.addAll(PlaceItemsTask.getTasks(tier));
            allTasks.addAll(PunchAnEntityWithItemTask.getTasks(tier));
            allTasks.addAll(ReceivePotionEffectTypeTask.getTasks(tier));
            allTasks.addAll(RepairIronGolemTask.getTasks(tier));
            allTasks.addAll(RideEntityTask.getTasks(tier));
            allTasks.addAll(ShearColoredSheepTask.getTasks(tier));
            allTasks.addAll(ShearSheepTask.getTasks(tier));
            allTasks.addAll(ShootBlockTask.getTasks(tier));
            allTasks.addAll(ShootProjectileTask.getTasks(tier));
            allTasks.addAll(SleepInColoredBedTask.getTasks(tier));
            allTasks.addAll(SmeltItemsTask.getTasks(tier));
            allTasks.addAll(SneakOnBlockTask.getTasks(tier));
            allTasks.addAll(SpecificDeathTask.getTasks(tier));
            allTasks.addAll(StandOnBlockTask.getTasks(tier));
            allTasks.addAll(StandOnCoordinateTask.getTasks(tier));
            allTasks.addAll(StayStillTask.getTasks(tier));
            allTasks.addAll(TeleportWithAnEnderpearlTask.getTasks(tier));
            allTasks.addAll(TouchBlockTask.getTasks(tier));
            allTasks.addAll(UseEyeOfEnderTask.getTasks(tier));
            allTasks.addAll(UseNametagTask.getTasks(tier));
            allTasks.addAll(UseSpyglassTask.getTasks(tier));
            allTasks.addAll(WearFullDyedLeatherArmorTask.getTasks(tier));
            allTasks.addAll(WearFullIronArmorTask.getTasks(tier));
        //}
        //catch (Exception e) {
        //    this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
        //    return false;
        //}

        // Randomly get items
        this.tasks = Utils.getRandomItems(allTasks, Math.min(this.tasksPerTier, allTasks.size()));
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (LockinTask task : this.tasks) {
            task.init();
        }

        return true;
    }

    public void complete(LockinTask task, Player completedPlayer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text()
                .append(Component.text(completedPlayer.getName(), NamedTextColor.GOLD))
                .append(Component.text(" has completed the task ", NamedTextColor.GRAY))
                .append(Component.text(task.name, task.nameColor)));
            if (task.reward != null) {
                player.sendMessage(Component.text()
                    .append(Component.text("  Reward: ", NamedTextColor.GRAY))
                    .append(Component.text(task.reward.getDescription(), NamedTextColor.LIGHT_PURPLE)));
            }
            Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL);
        }

        this.lockinCompass.updateTasksInventory(this);
    }

    //---------------------------------------------------------------------------------------------
	// Listener methods
    //---------------------------------------------------------------------------------------------
	public void registerListeners() {
        for (LockinTask task : this.tasks) {
            task.registerListeners();
        }
	}

    public void unRegisterListeners() {
        for (LockinTask task : this.tasks) { task.unRegisterListeners(); }
    }
}