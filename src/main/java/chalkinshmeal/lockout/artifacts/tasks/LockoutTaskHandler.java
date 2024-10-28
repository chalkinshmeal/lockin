package chalkinshmeal.lockout.artifacts.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;
import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.artifacts.scoreboard.LockoutScoreboard;
import chalkinshmeal.lockout.artifacts.tasks.general.ActivateBlockTask;
import chalkinshmeal.lockout.artifacts.tasks.general.BreakItemsTask;
import chalkinshmeal.lockout.artifacts.tasks.general.BreedEntitiesTask;
import chalkinshmeal.lockout.artifacts.tasks.general.CraftItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.CreateEntityTask;
import chalkinshmeal.lockout.artifacts.tasks.general.DestroyItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.DieTask;
import chalkinshmeal.lockout.artifacts.tasks.general.EatItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.EatTask;
import chalkinshmeal.lockout.artifacts.tasks.general.EnterBiomeTask;
import chalkinshmeal.lockout.artifacts.tasks.general.EquipArmorTask;
import chalkinshmeal.lockout.artifacts.tasks.general.EquipItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.GetExpLevelTask;
import chalkinshmeal.lockout.artifacts.tasks.general.GetSpecificHealthTask;
import chalkinshmeal.lockout.artifacts.tasks.general.HoldItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.InteractItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.JumpTask;
import chalkinshmeal.lockout.artifacts.tasks.general.KillEntitiesTask;
import chalkinshmeal.lockout.artifacts.tasks.general.KillEntityWithItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ObtainItemGroupTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ObtainItemWithStringTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ObtainItemsTask;
import chalkinshmeal.lockout.artifacts.tasks.general.StayStillTask;
import chalkinshmeal.lockout.artifacts.tasks.general.TouchBlockTask;
import chalkinshmeal.lockout.artifacts.tasks.general.PlaceFlowerInPotTask;
import chalkinshmeal.lockout.artifacts.tasks.general.PlaceItemInItemFrameTask;
import chalkinshmeal.lockout.artifacts.tasks.general.PlaceItemsTask;
import chalkinshmeal.lockout.artifacts.tasks.general.PunchAnEntityWithItemTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ReceivePotionEffectTypeTask;
import chalkinshmeal.lockout.artifacts.tasks.general.RideEntityTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ShearColoredSheepTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ShearSheepTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ShootBlockTask;
import chalkinshmeal.lockout.artifacts.tasks.general.ShootProjectileTask;
import chalkinshmeal.lockout.artifacts.tasks.general.SleepInColoredBedTask;
import chalkinshmeal.lockout.artifacts.tasks.general.SmeltItemsTask;
import chalkinshmeal.lockout.artifacts.tasks.general.SneakOnBlockTask;
import chalkinshmeal.lockout.artifacts.tasks.general.SpecificDeathTask;
import chalkinshmeal.lockout.artifacts.tasks.general.StandOnBlockTask;
import chalkinshmeal.lockout.artifacts.tasks.general.StandOnCoordinateTask;
import chalkinshmeal.lockout.artifacts.tasks.general.StayAboveHealthTask;
import chalkinshmeal.lockout.artifacts.tasks.general.StayAboveHungerTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.BlockArrowWithShieldTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.BrewPotionTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.CatchFishTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.DrinkMilkToCurePoisonTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.EnchantItemTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.EnterBoatWithPassengerTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.EnterNetherTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.GrowWheatWithBonemealTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.KillEntityWithStatusEffectTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.KillLeftySkeletonTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.LaunchFireworkTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.LightTNTTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.PlaceBookOnLecternTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.RepairIronGolemTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.TeleportWithAnEnderpearlTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.UseEyeOfEnderTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.UseNametagTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.UseSpyglassTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.WearFullDyedLeatherArmorTask;
import chalkinshmeal.lockout.artifacts.tasks.specific.WearFullIronArmorTask;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LockoutTaskHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final LockoutCompass lockoutCompass;
    private final LockoutScoreboard lockoutScoreboard;
    private LockoutRewardHandler lockoutRewardHandler;
    private final int maxLockoutTasks;
    private final int maxPunishmentTasks;
    private List<LockoutTask> tasks;

    public LockoutTaskHandler(JavaPlugin plugin, ConfigHandler configHandler, LockoutCompass lockoutCompass, LockoutScoreboard lockoutScoreboard) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockoutCompass = lockoutCompass;
        this.lockoutScoreboard = lockoutScoreboard;
        this.lockoutRewardHandler = new LockoutRewardHandler(this.plugin);
        this.tasks = new ArrayList<>();
        this.maxLockoutTasks = this.configHandler.getInt("taskCount", 27);
        this.maxPunishmentTasks = this.configHandler.getInt("punishmentCount", 3);
    }

    // Create the list of tasks for this lockout challenge
    // Return true if successful, false if not
    public boolean CreateTaskList() {
        this.lockoutRewardHandler = new LockoutRewardHandler(this.plugin);

        List<LockoutTask> allTasks = new ArrayList<>();
        List<LockoutTask> punishmentTasks = new ArrayList<>();
        try {
            // General tasks
            allTasks.addAll(ActivateBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(BreakItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(BrewPotionTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(BreedEntitiesTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(BlockArrowWithShieldTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(CatchFishTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(CraftItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(CreateEntityTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(DestroyItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(DieTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(EatTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(EatItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(EnchantItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(EnterBiomeTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(EnterBoatWithPassengerTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(EnterNetherTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(EquipItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(GetExpLevelTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(GetSpecificHealthTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(InteractItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(JumpTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(KillEntitiesTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(KillEntityWithItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(KillEntityWithStatusEffectTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(KillLeftySkeletonTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(LaunchFireworkTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(ObtainItemGroupTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(ObtainItemWithStringTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false, false));
            allTasks.addAll(PlaceBookOnLecternTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(PlaceFlowerInPotTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(PlaceItemInItemFrameTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(PlaceItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(PunchAnEntityWithItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(ReceivePotionEffectTypeTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(RepairIronGolemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(RideEntityTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(ShearColoredSheepTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(ShearSheepTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(ShootBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(ShootProjectileTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(SleepInColoredBedTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(SmeltItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(SneakOnBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(SpecificDeathTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(StandOnBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(StandOnCoordinateTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(StayStillTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(TouchBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false));
            allTasks.addAll(UseEyeOfEnderTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(UseNametagTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(UseSpyglassTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.addAll(WearFullDyedLeatherArmorTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));

            // Specific tasks
            allTasks.add(new WearFullIronArmorTask(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.add(new TeleportWithAnEnderpearlTask(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.add(new GrowWheatWithBonemealTask(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.add(new LightTNTTask(plugin, configHandler, this, lockoutRewardHandler));
            allTasks.add(new DrinkMilkToCurePoisonTask(plugin, configHandler, this, lockoutRewardHandler));

            // Punishment tasks
            punishmentTasks.addAll(BreakItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(CraftItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(DieTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(EatTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(EatItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(EnterBiomeTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(EquipArmorTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            punishmentTasks.addAll(GetExpLevelTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(HoldItemTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            punishmentTasks.addAll(KillEntitiesTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(JumpTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true, false));
            punishmentTasks.addAll(ObtainItemGroupTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(ObtainItemWithStringTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(PlaceItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(SpecificDeathTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(StandOnBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(StayAboveHealthTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            punishmentTasks.addAll(StayAboveHungerTask.getTasks(plugin, configHandler, this, lockoutRewardHandler));
            punishmentTasks.addAll(StayStillTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
            punishmentTasks.addAll(TouchBlockTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, true));
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
            return false;
        }

        // Randomly get items
        this.tasks = Utils.getRandomItems(allTasks, Math.min(this.maxLockoutTasks - this.maxPunishmentTasks, allTasks.size()));
        this.tasks.addAll(Utils.getRandomItems(punishmentTasks, Math.min(this.maxPunishmentTasks, punishmentTasks.size())));
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (LockoutTask task : this.tasks) {
            task.init();
        }

        return true;
    }

    public void CreateSuddenDeathTaskList() {
        this.lockoutRewardHandler = new LockoutRewardHandler(this.plugin);

        List<LockoutTask> suddenDeathTasks = new ArrayList<>();
        try {
            suddenDeathTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockoutRewardHandler, false, true));
        }

        catch (Exception e) {
            this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
            return;
        }

        // Randomly get items
        this.tasks = suddenDeathTasks;
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (LockoutTask task : this.tasks) {
            task.init();
        }
    }

    //---------------------------------------------------------------------------------------------
	// Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public List<LockoutTask> GetTasks() { return this.tasks; }
    public boolean areAllTasksDone() { 
        for (LockoutTask task : this.tasks) {
            if (task.isPunishment) continue;
            if (!task.isComplete()) return false;
        }
        return true;
    }

    public boolean areSuddenDeathTasksDone() {
        int requiredPoints = (int) Math.ceil(this.tasks.size() / 2);
        for (String teamName : this.lockoutScoreboard.getTeamNames()) {
            if (this.lockoutScoreboard.getScore(teamName) >= requiredPoints) return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------------------------
	// Task methods
    //---------------------------------------------------------------------------------------------
    public void complete(LockoutTask task, Player completedPlayer) {
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

        this.lockoutCompass.updateTasksInventory(this);
        System.out.println("Task completed: " + task.name + ", Value: " + task.value);
        this.lockoutScoreboard.addScore(completedPlayer, task.value);
    }

    //---------------------------------------------------------------------------------------------
	// Register all server-wide listeners
    //---------------------------------------------------------------------------------------------
	public void registerListeners() {
        for (LockoutTask task : this.tasks) {
            task.registerListeners();
        }
	}

    public void unRegisterListeners() {
        for (LockoutTask task : this.tasks) { task.unRegisterListeners(); }
    }
}