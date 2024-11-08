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
import chalkinshmeal.lockin.artifacts.tasks.general.ActivateBlockTask;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LockinTaskHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final LockinCompass lockinCompass;
    private final LockinScoreboard lockinScoreboard;
    private LockinRewardHandler lockinRewardHandler;
    private final int maxLockinTasks;
    private List<LockinTask> tasks;

    public LockinTaskHandler(JavaPlugin plugin, ConfigHandler configHandler, LockinCompass lockinCompass, LockinScoreboard lockinScoreboard) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockinCompass = lockinCompass;
        this.lockinScoreboard = lockinScoreboard;
        this.lockinRewardHandler = new LockinRewardHandler(this.plugin);
        this.tasks = new ArrayList<>();
        this.maxLockinTasks = this.configHandler.getInt("taskCount", 27);
    }

    // Create the list of tasks for this lockin challenge
    // Return true if successful, false if not
    public boolean CreateTaskList(int tier) {
        this.lockinRewardHandler = new LockinRewardHandler(this.plugin);

        List<LockinTask> allTasks = new ArrayList<>();
        try {
            // General tasks
            allTasks.addAll(ActivateBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, tier));
            //allTasks.addAll(BreakItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(BrewPotionTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(BreedEntitiesTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(BlockArrowWithShieldTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(CatchFishTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(CraftItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(CreateEntityTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(DestroyItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(DieTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(EatTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(EatItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(EnchantItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(EnterBiomeTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(EnterBoatWithPassengerTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(EnterNetherTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(EquipItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(GetExpLevelTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(GetSpecificHealthTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(InteractItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(JumpTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(KillEntitiesTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(KillEntityWithItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(KillEntityWithStatusEffectTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(KillLeftySkeletonTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(LaunchFireworkTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(ObtainItemGroupTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(ObtainItemWithStringTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false, false));
            //allTasks.addAll(PlaceBookOnLecternTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(PlaceFlowerInPotTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(PlaceItemInItemFrameTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(PlaceItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(PunchAnEntityWithItemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(ReceivePotionEffectTypeTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(RepairIronGolemTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(RideEntityTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(ShearColoredSheepTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(ShearSheepTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(ShootBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(ShootProjectileTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(SleepInColoredBedTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(SmeltItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(SneakOnBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(SpecificDeathTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(StandOnBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(StandOnCoordinateTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(StayStillTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(TouchBlockTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false));
            //allTasks.addAll(UseEyeOfEnderTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(UseNametagTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(UseSpyglassTask.getTasks(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.addAll(WearFullDyedLeatherArmorTask.getTasks(plugin, configHandler, this, lockinRewardHandler));

            //// Specific tasks
            //allTasks.add(new WearFullIronArmorTask(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.add(new TeleportWithAnEnderpearlTask(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.add(new GrowWheatWithBonemealTask(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.add(new LightTNTTask(plugin, configHandler, this, lockinRewardHandler));
            //allTasks.add(new DrinkMilkToCurePoisonTask(plugin, configHandler, this, lockinRewardHandler));
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
            return false;
        }

        // Randomly get items
        this.tasks = Utils.getRandomItems(allTasks, Math.min(this.maxLockinTasks, allTasks.size()));
        Collections.shuffle(this.tasks);

        // Initialize tasks (Generate rewards, set lore, etc.)
        for (LockinTask task : this.tasks) {
            task.init();
        }

        return true;
    }

    //public void CreateSuddenDeathTaskList() {
    //    this.lockinRewardHandler = new LockinRewardHandler(this.plugin);

    //    List<LockinTask> suddenDeathTasks = new ArrayList<>();
    //    try {
    //        suddenDeathTasks.addAll(ObtainItemsTask.getTasks(plugin, configHandler, this, lockinRewardHandler, false, true));
    //    }

    //    catch (Exception e) {
    //        this.plugin.getLogger().warning("Could not create task list: " + e.getMessage());
    //        return;
    //    }

    //    // Randomly get items
    //    this.tasks = suddenDeathTasks;
    //    Collections.shuffle(this.tasks);

    //    // Initialize tasks (Generate rewards, set lore, etc.)
    //    for (LockinTask task : this.tasks.values()) {
    //        task.init();
    //    }
    //}

    //---------------------------------------------------------------------------------------------
	// Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public List<LockinTask> GetTasks() { return new ArrayList<>(this.tasks); }
    public boolean areAllTasksDone() { 
        for (LockinTask task : this.tasks) {
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
        System.out.println("Task completed: " + task.name + ", Value: " + task.value);
        this.lockinScoreboard.addScore(completedPlayer, task.value);
    }

    //---------------------------------------------------------------------------------------------
	// Register all server-wide listeners
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