package chalkinshmeal.lockin;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.game.GameHandler;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.commands.CompassCommand;
import chalkinshmeal.lockin.commands.HelpCommand;
import chalkinshmeal.lockin.commands.StartCommand;
import chalkinshmeal.lockin.commands.StopCommand;
import chalkinshmeal.lockin.commands.TeamCommand;
//import chalkinshmeal.mc_plugin_lib.config.ConfigHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigHandler;
import chalkinshmeal.lockin.listeners.server.EntityDamageByEntityListener;
import chalkinshmeal.lockin.listeners.server.InventoryClickListener;
import chalkinshmeal.lockin.listeners.server.InventoryDragListener;
import chalkinshmeal.lockin.listeners.server.PlayerChangedWorldListener;
import chalkinshmeal.lockin.listeners.server.PlayerInteractListener;
import chalkinshmeal.lockin.listeners.server.PlayerJoinListener;
import chalkinshmeal.lockin.utils.WorldUtils;
import chalkinshmeal.lockin.utils.cmdframework.command.ParentCommand;
import chalkinshmeal.lockin.utils.cmdframework.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Plugin extends JavaPlugin implements Listener {
	private CommandHandler cmdHandler;
    private ConfigHandler configHandler;
    private GameHandler gameHandler;
    private LockinTaskHandler lockinTaskHandler;
    private LockinTeamHandler lockinTeamHandler;
    private LockinCompass lockinCompass;
    private LockinScoreboard lockinScoreboard;


	@Override
	public void onEnable() {
		super.onEnable();
		this.cmdHandler = new CommandHandler(this);
        this.configHandler = new ConfigHandler(this);
        this.lockinScoreboard = new LockinScoreboard(this, this.configHandler);
        this.lockinTeamHandler = new LockinTeamHandler(this, this.lockinScoreboard);
        this.lockinCompass = new LockinCompass(this, this.configHandler, this.lockinTeamHandler, this.lockinScoreboard);
        this.lockinTaskHandler = new LockinTaskHandler(
            this, this.configHandler, this.lockinCompass, this.lockinScoreboard, this.lockinTeamHandler);
        this.gameHandler = new GameHandler(
            this, this.configHandler, this.lockinCompass, this.lockinTaskHandler, this.lockinScoreboard, this.lockinTeamHandler);

		// Register commands + listeners
		registerCommands();
		registerListeners();

		// Log some debug information
        Component welcomeMsg = Component.text()
            .append(Component.text("lockin successfully loaded", NamedTextColor.GOLD))
            .build();
		this.getServer().getConsoleSender().sendMessage(welcomeMsg);

        // Create spawn cage
        WorldUtils.createSpawnCage();
	}

	/** Register all commands within the /command directory */
	private void registerCommands() {
		// Create command
		ParentCommand lockinCmd = new ParentCommand("lockin");

        lockinCmd.addChild(new CompassCommand(this, cmdHandler, lockinCompass));
        lockinCmd.addChild(new StartCommand(this, cmdHandler, gameHandler));
        lockinCmd.addChild(new StopCommand(this, cmdHandler, gameHandler));
        lockinCmd.addChild(new TeamCommand(this, cmdHandler, lockinTeamHandler, lockinCompass));
		lockinCmd.addChild(new HelpCommand(this, cmdHandler));

		// Register command -> command handler
		this.cmdHandler.registerCommand(lockinCmd);
	}

    //---------------------------------------------------------------------------------------------
	// Register all server-wide listeners
    //---------------------------------------------------------------------------------------------
	private void registerListeners() {
		PluginManager manager = this.getServer().getPluginManager();
		manager.registerEvents(new EntityDamageByEntityListener(this.gameHandler), this);
		manager.registerEvents(new InventoryClickListener(this.lockinCompass), this);
		manager.registerEvents(new InventoryDragListener(this.lockinCompass), this);
		manager.registerEvents(new PlayerChangedWorldListener(this.gameHandler), this);
		manager.registerEvents(new PlayerJoinListener(this.lockinCompass, this.lockinScoreboard, this.gameHandler), this);
		manager.registerEvents(new PlayerInteractListener(this.lockinCompass), this);
	}
}
