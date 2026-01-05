package chalkinshmeal.lockin.commands;

import static chalkinshmeal.mc_plugin_lib.strings.StringUtils.stringToComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.mc_plugin_lib.commands.argument.ArgType;
import chalkinshmeal.mc_plugin_lib.commands.argument.ArgValue;
import chalkinshmeal.mc_plugin_lib.commands.argument.Argument;
import chalkinshmeal.mc_plugin_lib.commands.command.ArgCommand;
import chalkinshmeal.mc_plugin_lib.commands.handler.CommandHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigFile;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TeamCommand extends ArgCommand {
    private final ConfigFile config;
    private final TeamHandler teamHandler;
    private final LockinCompass lockinCompass;

    // Constructor
    public TeamCommand(JavaPlugin plugin, CommandHandler cmdHandler, ConfigFile config, TeamHandler teamHandler, LockinCompass lockinCompass) {
        super("team", false);
        this.setPlayerRequired(true);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Creates a team for lockin", NamedTextColor.WHITE))
            .build());
        
        this.config = config;
        this.teamHandler = teamHandler;
        this.lockinCompass = lockinCompass;
        this.addArg(new Argument("team", ArgType.STRING, new ArrayList<>(this.teamHandler.getTeamNames())));
        this.addArg(new Argument("material", ArgType.STRING, Stream.of(Material.values()).map(Material::name).collect(Collectors.toList())));
    }

    @Override
    protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
        Player player = (Player) sender;
        String teamName = argValues.get(0).get();
        Material material;
        try {
            material = Material.valueOf(argValues.get(1).get());
            @SuppressWarnings("unused")
            ItemStack item = new ItemStack(material);
        }
        catch (Exception e) {
            player.sendMessage(Component.text("Invalid material provided: '" + argValues.get(1).get() + "'", NamedTextColor.RED));
            return;
        }
        if (material.equals(Material.AIR)) {
            player.sendMessage(Component.text("Invalid material provided: '" + argValues.get(1).get() + "'", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(
            Component.text("Added team ", NamedTextColor.GRAY)
                .append(Component.text(teamName, NamedTextColor.GOLD))
                .append(Component.text(" to the game", NamedTextColor.GRAY)));

        int maxLives = this.config.getInt("maxLives", 5);
        this.teamHandler.addTeam(teamName, stringToComponent(teamName), material, maxLives);
        this.lockinCompass.addTeam(this.teamHandler.getTeam(teamName));
    }
}