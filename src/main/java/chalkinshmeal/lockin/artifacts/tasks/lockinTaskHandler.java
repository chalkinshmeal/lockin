package chalkinshmeal.lockin.artifacts.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.lockinCompass;
import chalkinshmeal.lockin.artifacts.rewards.lockinRewardHandler;
import chalkinshmeal.lockin.artifacts.scoreboard.lockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.general.ActivateBlockTask;
import chalkinshmeal.lockin.artifacts.tasks.general.BreakItemsTask;
import chalkinshmeal.lockin.artifacts.tasks.general.BreedEntitiesTask;
import chalkinshmeal.lockin.artifacts.tasks.general.CraftItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.CreateEntityTask;
import chalkinshmeal.lockin.artifacts.tasks.general.DestroyItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.DieTask;
import chalkinshmeal.lockin.artifacts.tasks.general.EatItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.EatTask;
import chalkinshmeal.lockin.artifacts.tasks.general.EnterBiomeTask;
import chalkinshmeal.lockin.artifacts.tasks.general.EquipArmorTask;
import chalkinshmeal.lockin.artifacts.tasks.general.EquipItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.GetExpLevelTask;
import chalkinshmeal.lockin.artifacts.tasks.general.GetSpecificHealthTask;
import chalkinshmeal.lockin.artifacts.tasks.general.HoldItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.InteractItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.JumpTask;
import chalkinshmeal.lockin.artifacts.tasks.general.KillEntitiesTask;
import chalkinshmeal.lockin.artifacts.tasks.general.KillEntityWithItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ObtainItemGroupTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ObtainItemWithStringTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ObtainItemsTask;
import chalkinshmeal.lockin.artifacts.tasks.general.StayStillTask;
import chalkinshmeal.lockin.artifacts.tasks.general.TouchBlockTask;
import chalkinshmeal.lockin.artifacts.tasks.general.PlaceFlowerInPotTask;
import chalkinshmeal.lockin.artifacts.tasks.general.PlaceItemInItemFrameTask;
import chalkinshmeal.lockin.artifacts.tasks.general.PlaceItemsTask;
import chalkinshmeal.lockin.artifacts.tasks.general.PunchAnEntityWithItemTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ReceivePotionEffectTypeTask;
import chalkinshmeal.lockin.artifacts.tasks.general.RideEntityTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ShearColoredSheepTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ShearSheepTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ShootBlockTask;
import chalkinshmeal.lockin.artifacts.tasks.general.ShootProjectileTask;
import chalkinshmeal.lockin.artifacts.tasks.general.SleepInColoredBedTask;
import chalkinshmeal.lockin.artifacts.tasks.general.SmeltItemsTask;
import chalkinshmeal.lockin.artifacts.tasks.general.SneakOnBlockTask;
import chalkinshmeal.lockin.artifacts.tasks.general.SpecificDeathTask;
import chalkinshmeal.lockin.artifacts.tasks.general.StandOnBlockTask;
import chalkinshmeal.lockin.artifacts.tasks.general.StandOnCoordinateTask;
import chalkinshmeal.lockin.artifacts.tasks.general.StayAboveHealthTask;
import chalkinshmeal.lockin.artifacts.tasks.general.StayAboveHungerTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.BlockArrowWithShieldTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.BrewPotionTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.CatchFishTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.DrinkMilkToCurePoisonTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.EnchantItemTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.EnterBoatWithPassengerTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.EnterNetherTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.GrowWheatWithBonemealTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.KillEntityWithStatusEffectTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.KillLeftySkeletonTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.LaunchFireworkTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.LightTNTTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.PlaceBookOnLecternTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.RepairIronGolemTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.TeleportWithAnEnderpearlTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.UseEyeOfEnderTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.UseNametagTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.UseSpyglassTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.WearFullDyedLeatherArmorTask;
import chalkinshmeal.lockin.artifacts.tasks.specific.WearFullIronArmorTask;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class lockinTaskHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final lockinCompass lockinCompass;
    private final lockinScoreboard lockinScoreboard;
    private lockinRewardHandler lockinRewardHandler;
    private final int maxlockinTasks;
    private final int maxPunishmentTasks;
    private List<lockinTask> tasks;

    public lockinTaskHandler(JavaPlugin plugin, ConfigHandler configHandler, lockinCompass lockinCompass, lockinScoreboard lockinScoreboard) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockinCompass = lockinCompass;
        this.lockinScoreboard = lockinScoreboard;
        this.lockinRewardHandler = new lockinRewardHandler(this.plugin);
        this.tasks = new ArrayList<>();
        this.maxlockinTasks = this.configHandler.getInt("taskCount", 27);
        this.maxPunishmentTasks = this.configHandler.getInt("punishmentCount", 3);
    }

    // Create the list of tasks for this lockin challenge
    // Return true if successful, false if not
    public boolean CreateTaskList() {
        this.lockinRewardHandler = new lockinRewardHandler(this.plugin);

        List<lockinTask> allTasks = new ArrayList<>();
        List<lockinTask> punishmentTasks = new ArrayList<>();
        try {
            // General tasks
            allTasks.addAll(ActivateBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(BreakItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(BrewPotionTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(BreedEntitiesTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(BlockArrowWithShieldTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(CatchFishTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(CraftItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(CreateEntityTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(DestroyItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(DieTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(EatTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(EatItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(EnchantItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(EnterBiomeTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(EnterBoatWithPassengerTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(EnterNetherTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(EquipItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(GetExpLevelTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(GetSpecificHealthTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(InteractItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(JumpTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(KillEntitiesTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(KillEntityWithItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(KillEntityWithStatusEffectTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(KillLeftySkeletonTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(LaunchFireworkTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(ObtainItemGroupTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(ObtainItemWithStringTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false, false));
            allTasks.addAll(PlaceBookOnLecternTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(PlaceFlowerInPotTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(PlaceItemInItemFrameTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(PlaceItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(PunchAnEntityWithItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(ReceivePotionEffectTypeTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(RepairIronGolemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(RideEntityTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(ShearColoredSheepTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(ShearSheepTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(ShootBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(ShootProjectileTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(SleepInColoredBedTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(SmeltItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(SneakOnBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(SpecificDeathTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(StandOnBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(StandOnCoordinateTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(StayStillTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(TouchBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            allTasks.addAll(UseEyeOfEnderTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(UseNametagTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(UseSpyglassTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            allTasks.addAll(WearFullDyedLeatherArmorTask.getTasks(plugin, configHandler, this, lockinRewardHandler));

            // Specific tasks
            allTasks.add(new WearFullIronArmorTask(plugin, configHandler, this, lockinRewardHandler));
            allTasks.add(new TeleportWithAnEnderpearlTask(plugin, configHandler, this, lockinRewardHandler));
            allTasks.add(new GrowWheatWithBonemealTask(plugin, configHandler, this, lockinRewardHandler));
            allTasks.add(new LightTNTTask(plugin, configHandler, this, lockinRewardHandler));
            allTasks.add(new DrinkMilkToCurePoisonTask(plugin, configHandler, this, lockinRewardHandler));

            // Punishment tasks
            punishmentTasks.addAll(BreakItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(CraftItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(DieTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(EatTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(EatItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(EnterBiomeTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(EquipArmorTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            punishmentTasks.addAll(GetExpLevelTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(HoldItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            punishmentTasks.addAll(KillEntitiesTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(JumpTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true, false));
            punishmentTasks.addAll(ObtainItemGroupTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(ObtainItemWithStringTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(PlaceItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(SpecificDeathTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(StandOnBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(StayAboveHealthTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            punishmentTasks.addAll(StayAboveHungerTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            punishmentTasks.addAll(StayStillTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
            punishmentTasks.addAll(TouchBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, true));
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
            return false;
        }

        // Randomly get items
        this.tasks = Utils.getRandomItems(allTasks, Math.min(this.maxlockinTasks - this.maxPunishmentTasks, allTasks.size()));
        this.tasks.addAll(Utils.getRandomItems(punishmentTasks, Math.min(this.maxPunishmentTasks, punishmentTasks.size())));
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (lockinTask task : this.tasks) {
            task.init();
        }

        return true;
    }

    public void CreateSuddenDeathTaskList() {
        this.lockinRewardHandler = new lockinRewardHandler(this.plugin);

        List<lockinTask> suddenDeathTasks = new ArrayList<>();
        try {
            suddenDeathTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false, true));
        }

        catch (Exception e) {
            this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
            return;
        }

        // Randomly get items
        this.tasks = suddenDeathTasks;
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (lockinTask task : this.tasks) {
            task.init();
        }
    }

    //---------------------------------------------------------------------------------------------
	// Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public List<lockinTask> GetTasks() { return this.tasks; }
    public boolean areAllTasksDone() { 
        for (lockinTask task : this.tasks) {
            if (task.isPunishment) continue;
            if (!task.isComplete()) return false;
        }
        return true;
    }

    public boolean areSuddenDeathTasksDone() {
        int requiredPoints = (int) Math.ceil(this.tasks.size() / 2);
        for (String teamName : this.lockinScoreboard.getTeamNames()) {
            if (this.lockinScoreboard.getScore(teamName) >= requiredPoints) return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------------------------
	// Task methods
    //---------------------------------------------------------------------------------------------
    public void complete(lockinTask task, Player completedPlayer) {
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
        System.out.println("Task completed: " + task.name + ", Value: " + task.value);
        this.lockinScoreboard.addScore(completedPlayer, task.value);
    }

    //---------------------------------------------------------------------------------------------
	// Register all server-wide listeners
    //---------------------------------------------------------------------------------------------
	public void registerListeners() {
        for (lockinTask task : this.tasks) {
            task.registerListeners();
        }
	}

    public void unRegisterListeners() {
        for (lockinTask task : this.tasks) { task.unRegisterListeners(); }
    }
}