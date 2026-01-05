package chalkinshmeal.lockin.artifacts.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.mc_plugin_lib.custom_tasks.CustomTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ActivateBlockTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.BlockArrowWithShieldTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.BreakItemsTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.BrewPotionTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.CatchFishTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.CreateEntityTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.DestroyItemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.DieTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.DrinkMilkToCurePoisonTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EatItemsTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EatTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EnchantItemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EnterBiomeTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EnterBoatWithPassengerTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EnterNetherTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.EquipItemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.GetExpLevelTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.GetSpecificHealthTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.GrowWheatWithBonemealTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.InteractItemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.KillBabyEntitiesTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.KillEntitiesTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.KillEntityWithStatusEffectTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.KillLeftySkeletonTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.LaunchFireworkTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.LightTNTTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ObtainItemGroupTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ObtainItemWithEnchantmentTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ObtainItemWithStringTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ObtainItemsTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.PlaceBookOnLecternTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.PlaceFlowerInPotTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.PlaceItemInItemFrameTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.PlaceItemsTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.PunchAnEntityWithItemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.PunchPlayerWithItemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ReceivePotionEffectTypeTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.RepairIronGolemTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.RideEntityTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ShearColoredSheepTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ShearSheepTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ShootBlockTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.ShootProjectileTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.SleepInColoredBedTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.SmeltItemsTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.SneakOnBlockTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.SpecificDeathTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.StandOnBlockTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.StandOnCoordinateTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.StayStillTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.TameEntityTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.TeleportWithAnEnderpearlTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.TouchBlockTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.UseEyeOfEnderTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.UseNametagTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.UseSpyglassTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.WearFullDyedLeatherArmorTask;
import chalkinshmeal.mc_plugin_lib.custom_tasks.tasks.WearFullIronArmorTask;
import chalkinshmeal.mc_plugin_lib.config.ConfigFile;
import chalkinshmeal.mc_plugin_lib.logging.LoggerUtils;
import chalkinshmeal.mc_plugin_lib.teams.Team;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CustomTaskHandler {
    private final JavaPlugin plugin;
    private final ConfigFile config;
    private final LockinCompass lockinCompass;
    private final TeamHandler teamHandler;
    public int tasksPerTier;
    private int tasksToCompletePerTier;
    public int taskTierLowerRange;
    private int currentTier;
    private List<CustomTask> tasks;
    private final boolean debug = false;
    private final int maxTier = 10;

    public CustomTaskHandler(JavaPlugin plugin, ConfigFile config, LockinCompass lockinCompass,
                                TeamHandler teamHandler) {
        this.plugin = plugin;
        this.config = config;
        this.lockinCompass = lockinCompass;
        this.teamHandler = teamHandler;
        this.tasks = new ArrayList<>();
        this.tasksPerTier = this.config.getInt("tasksPerTier", 5);
        this.tasksToCompletePerTier = this.config.getInt("tasksToCompletePerTier", 3);
        this.taskTierLowerRange = this.config.getInt("tasksTierLowerRange", 0);

        CustomTask.setPlugin(this.plugin);
        CustomTask.setConfig(this.config);
        CustomTask.setTeamHandler(this.teamHandler);
        CustomTask.setCustomTaskHandler(this);
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public void setCurrentTier(int currentTier) { this.currentTier = currentTier; }
    public int getTasksToCompletePerTier(int tier) {
        if (tier >= 10) {
            return 1;
        }
        else if (tier >= 6) {
            return 2;
        }
        else {
            return this.tasksToCompletePerTier;
        }
    }
    public void setTasksPerTier(int tasksPerTier) { this.tasksPerTier = tasksPerTier; }
    public List<CustomTask> getTasks() { return new ArrayList<>(this.tasks); }
    public boolean haveAllTeamsCompletedTheTier() {
        for (Team team : this.teamHandler.getTeams()) {
            int tasksCompleted = 0;
            for (CustomTask task : this.tasks) {
                if (task.hasCompleted(team.getKey())) tasksCompleted += 1;
            }
            if (tasksCompleted < this.getTasksToCompletePerTier(this.currentTier)) return false;
        }
        return true;
    }
    public boolean hasOneTeamCompletedTheTier() {
        for (Team team : this.teamHandler.getTeams()) {
            int tasksCompleted = 0;
            for (CustomTask task : this.tasks) {
                if (task.hasCompleted(team.getKey())) tasksCompleted += 1;
            }
            if (tasksCompleted >= this.getTasksToCompletePerTier(this.currentTier)) return true;
        }
        return false;
    }
    public boolean hasOneTeamCompletedAllTasks() {
        for (Team team : this.teamHandler.getTeams()) {
            boolean hasCompletedAllTasks = true;
            for (CustomTask task : this.tasks) {
                if (!task.hasCompleted(team.getKey())) hasCompletedAllTasks = false;
            }
            if (hasCompletedAllTasks) return true;
        }
        return false;
    }
    public boolean hasTeamCompletedAllTasks(Team team) {
        for (CustomTask task : this.tasks) {
            if (!task.hasCompleted(team.getKey())) return false;
        }
        return true;
    }
    public boolean haveAllTeamsCompletedAllTasks() {
        for (Team team : this.teamHandler.getTeams()) {
            for (CustomTask task : this.tasks) {
                if (!task.hasCompleted(team.getKey())) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean areAllTasksDone() { 
        for (CustomTask task : this.tasks) {
            if (!task.haveAllTeamsCompleted()) return false;
        }
        return true;
    }

    //---------------------------------------------------------------------------------------------
	// Task methods
    //---------------------------------------------------------------------------------------------
    public void stopCurrentTasks() {
        for (CustomTask task : this.tasks) { task.stop(); }
    }

    // Update the list of tasks for this lockin challenge
    // Return true if successful, false if not
    public boolean updateTaskList(int targetTier) {
        this.stopCurrentTasks();
        this.unRegisterListeners();

        List<CustomTask> allTasks = new ArrayList<>();
        int tierUpperRange = targetTier;
        int tierLowerRange = (targetTier == this.maxTier) ? targetTier : Math.max(1, targetTier - this.taskTierLowerRange);
        for (int tier = tierLowerRange; tier <= tierUpperRange; tier++) {
        //try {
            // General tasks
            allTasks.addAll(ActivateBlockTask.getTasks(tier));
            allTasks.addAll(BreakItemsTask.getTasks(tier));
            allTasks.addAll(BrewPotionTask.getTasks(tier));
            //allTasks.addAll(BreedEntitiesTask.getTasks(tier));
            allTasks.addAll(BlockArrowWithShieldTask.getTasks(tier));
            allTasks.addAll(CatchFishTask.getTasks(tier));
            //allTasks.addAll(CraftItemsTask.getTasks(tier));
            allTasks.addAll(CreateEntityTask.getTasks(tier));
            allTasks.addAll(DestroyItemTask.getTasks(tier));
            allTasks.addAll(DieTask.getTasks(tier));
            allTasks.addAll(DrinkMilkToCurePoisonTask.getTasks(tier));
            allTasks.addAll(EatTask.getTasks(tier));
            allTasks.addAll(EatItemsTask.getTasks(tier));
            allTasks.addAll(EnchantItemTask.getTasks(tier));
            allTasks.addAll(EnterBiomeTask.getTasks(tier));
            allTasks.addAll(EnterBoatWithPassengerTask.getTasks(tier));
            allTasks.addAll(EnterNetherTask.getTasks(tier));
            allTasks.addAll(EquipItemTask.getTasks(tier));
            allTasks.addAll(GetExpLevelTask.getTasks(tier));
            allTasks.addAll(GetSpecificHealthTask.getTasks(tier));
            allTasks.addAll(GrowWheatWithBonemealTask.getTasks(tier));
            allTasks.addAll(InteractItemTask.getTasks(tier));
            //allTasks.addAll(JumpTask.getTasks(tier));
            allTasks.addAll(KillBabyEntitiesTask.getTasks(tier));
            allTasks.addAll(KillEntitiesTask.getTasks(tier));
            //allTasks.addAll(KillEntityWithItemTask.getTasks(tier));
            allTasks.addAll(KillEntityWithStatusEffectTask.getTasks(tier));
            allTasks.addAll(KillLeftySkeletonTask.getTasks(tier));
            allTasks.addAll(LaunchFireworkTask.getTasks(tier));
            allTasks.addAll(LightTNTTask.getTasks(tier));
            allTasks.addAll(ObtainItemGroupTask.getTasks(tier));
            allTasks.addAll(ObtainItemWithEnchantmentTask.getTasks(tier));
            allTasks.addAll(ObtainItemWithStringTask.getTasks(tier));
            allTasks.addAll(ObtainItemsTask.getTasks(tier));
            allTasks.addAll(PlaceBookOnLecternTask.getTasks(tier));
            allTasks.addAll(PlaceFlowerInPotTask.getTasks(tier));
            allTasks.addAll(PlaceItemInItemFrameTask.getTasks(tier));
            allTasks.addAll(PlaceItemsTask.getTasks(tier));
            allTasks.addAll(PunchAnEntityWithItemTask.getTasks(tier));
            allTasks.addAll(PunchPlayerWithItemTask.getTasks(tier));
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
            allTasks.addAll(TameEntityTask.getTasks(tier));
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
        }

        // Randomly get items
        int taskAmount = Math.min(this.tasksPerTier, allTasks.size());
        if (targetTier == this.maxTier) taskAmount = 1;
        this.tasks = Utils.getRandomItems(allTasks, taskAmount);
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (CustomTask task : this.tasks) {
            task.start();
        }

        return true;
    }

    public void complete(CustomTask task, Player completedPlayer) {
        Team team = this.teamHandler.getTeam(completedPlayer);

        // Cosmetics
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text()
                .append(Component.text(completedPlayer.getName(), NamedTextColor.GOLD))
                .append(Component.text(" has completed the task ", NamedTextColor.GRAY))
                .append(Component.text(task.getDescription(), task.getDisplayColor())));
            Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL);
        }
        if (this.hasTeamCompletedAllTasks(team)) {
            for (Player player : teamHandler.getAllOnlinePlayers()) {
                player.sendMessage(Component.text()
                    .append(Component.text(team.getKey(), NamedTextColor.GOLD))
                    .append(Component.text(" has completed the tier", NamedTextColor.GRAY)));
                Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
            }
        }

        // Change item to gray stained glass pane, if completely done
        if (task.haveAllTeamsCompleted()) {
            task.setDisplayItem(Utils.setMaterial(task.getDisplayItem(), Material.GRAY_STAINED_GLASS_PANE));
            task.stop();
        }
        this.lockinCompass.updateTasksInventory(this);
    }

    //---------------------------------------------------------------------------------------------
	// Listener methods
    //---------------------------------------------------------------------------------------------
	public void registerListeners() {
        if (debug) LoggerUtils.info("Registering tasks");
        for (CustomTask task : this.tasks) {
            if (debug) LoggerUtils.info("Registering task: " + task.getDescription());
            task.registerListeners();
        }
	}

    public void unRegisterListeners() {
        if (debug) LoggerUtils.info("Unregistering tasks");
        for (CustomTask task : this.tasks) {
            if (debug) LoggerUtils.info("Unregistering task: " + task.getDescription());
            task.unRegisterListeners();
        }
    }
}