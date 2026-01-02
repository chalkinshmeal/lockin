package chalkinshmeal.lockin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.game.GameHandler;
import chalkinshmeal.mc_plugin_lib.commands.command.BaseCommand;
import chalkinshmeal.mc_plugin_lib.commands.handler.CommandHandler;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StartCommand extends BaseCommand {
    private final GameHandler gameHandler;
    private final TeamHandler teamHandler;

    // Constructor
    public StartCommand(JavaPlugin plugin, CommandHandler cmdHandler, GameHandler gameHandler, TeamHandler teamHandler) {
        super("start");
        this.setPlayerRequired(false);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Starts a lockin game", NamedTextColor.WHITE))
            .build());
        
        this.gameHandler = gameHandler;
        this.teamHandler = teamHandler;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (this.gameHandler.isActive()) {
            sender.sendMessage(
                Component.text("lockin game is already in progress", NamedTextColor.RED));
            return;
        }
        if (this.gameHandler.getNumTeams() <= 0) {
            sender.sendMessage(
                Component.text("Create at least 1 team to start a lockin game", NamedTextColor.RED));
            return;
        }
        if (this.teamHandler.getAllPlayers().size() <= 0) {
            sender.sendMessage(
                Component.text("Must join a team to start a game. Type /lockin compass and join a team", NamedTextColor.RED));
            return;
        }
        this.gameHandler.queue();
    }
}