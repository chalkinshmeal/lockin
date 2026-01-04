package chalkinshmeal.lockin;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.game.GameHandler;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.commands.CompassCommand;
import chalkinshmeal.lockin.commands.HelpCommand;
import chalkinshmeal.lockin.commands.StartCommand;
import chalkinshmeal.lockin.commands.StopCommand;
import chalkinshmeal.lockin.commands.TeamCommand;
import chalkinshmeal.lockin.listeners.server.EntityDamageByEntityListener;
import chalkinshmeal.lockin.listeners.server.InventoryClickListener;
import chalkinshmeal.lockin.listeners.server.InventoryDragListener;
import chalkinshmeal.lockin.listeners.server.PlayerChangedWorldListener;
import chalkinshmeal.lockin.listeners.server.PlayerInteractListener;
import chalkinshmeal.lockin.listeners.server.PlayerJoinListener;
import chalkinshmeal.lockin.utils.WorldUtils;
import chalkinshmeal.mc_plugin_lib.commands.command.ParentCommand;
import chalkinshmeal.mc_plugin_lib.commands.handler.CommandHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigHandler;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Plugin extends JavaPlugin implements Listener {
	private CommandHandler cmdHandler;
    private ConfigHandler configHandler;
    private GameHandler gameHandler;
    private TeamHandler teamHandler;
    private LockinTaskHandler lockinTaskHandler;
    private LockinCompass lockinCompass;

    //-------------------------------------------------------------------------
    // Plugin overrides
    //-------------------------------------------------------------------------
	@Override
	public void onEnable() {
		this.cmdHandler = new CommandHandler(this);
        this.configHandler = new ConfigHandler(this);
        this.teamHandler = new TeamHandler(this, "Lives");
        this.lockinCompass = new LockinCompass(this, this.configHandler, this.teamHandler);
        this.lockinTaskHandler = new LockinTaskHandler(this, this.configHandler, this.lockinCompass, this.teamHandler);
        this.gameHandler = new GameHandler(this, this.configHandler, this.lockinCompass, this.lockinTaskHandler, this.teamHandler);

		// Register commands + listeners
		registerCommands();
		registerListeners();

        // Create spawn cage
        WorldUtils.createSpawnCage();

		// Log some debug information
        Component welcomeMsg = Component.text()
            .append(Component.text("lockin successfully loaded", NamedTextColor.GOLD))
            .build();
		this.getServer().getConsoleSender().sendMessage(welcomeMsg);
    }

	@Override
	public void onDisable() {
        this.unregisterListeners();
        Bukkit.getScheduler().cancelTasks(this);
    }

    //-------------------------------------------------------------------------
    // Command methods
    //-------------------------------------------------------------------------
	private void registerCommands() {
		// Create command
		ParentCommand lockinCmd = new ParentCommand("lockin");

        lockinCmd.addChild(new CompassCommand(this, cmdHandler, lockinCompass));
		lockinCmd.addChild(new HelpCommand(this, cmdHandler));
        lockinCmd.addChild(new StartCommand(this, cmdHandler, gameHandler, teamHandler));
        lockinCmd.addChild(new StopCommand(this, cmdHandler, gameHandler));
        lockinCmd.addChild(new TeamCommand(this, cmdHandler, configHandler, teamHandler, lockinCompass));

		// Register command -> command handler
		this.cmdHandler.registerCommand(lockinCmd);
	}

    //---------------------------------------------------------------------------------------------
	// Listener methods
    //---------------------------------------------------------------------------------------------
	private void registerListeners() {
		PluginManager manager = this.getServer().getPluginManager();
		manager.registerEvents(new EntityDamageByEntityListener(this.gameHandler), this);
		manager.registerEvents(new InventoryClickListener(this.lockinCompass), this);
		manager.registerEvents(new InventoryDragListener(this.lockinCompass), this);
		manager.registerEvents(new PlayerChangedWorldListener(this.gameHandler), this);
		manager.registerEvents(new PlayerJoinListener(this.lockinCompass, this.gameHandler), this);
		manager.registerEvents(new PlayerInteractListener(this.lockinCompass), this);

        this.teamHandler.registerListeners();
	}

    private void unregisterListeners() {
        HandlerList.unregisterAll();
    }
}