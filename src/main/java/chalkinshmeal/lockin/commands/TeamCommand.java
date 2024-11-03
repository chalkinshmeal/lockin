package chalkinshmeal.lockin.commands;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.utils.cmdframework.argument.ArgType;
import chalkinshmeal.lockin.utils.cmdframework.argument.ArgValue;
import chalkinshmeal.lockin.utils.cmdframework.argument.Argument;
import chalkinshmeal.lockin.utils.cmdframework.command.ArgCommand;
import chalkinshmeal.lockin.utils.cmdframework.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TeamCommand extends ArgCommand {
    private final LockinTeamHandler lockinTeamHandler;

    // Constructor
    public TeamCommand(JavaPlugin plugin, CommandHandler cmdHandler, LockinTeamHandler lockinTeamHandler) {
        super("team", false);
        this.setPlayerRequired(true);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Creates a team for lockin", NamedTextColor.WHITE))
            .build());
        
        this.lockinTeamHandler = lockinTeamHandler;
        this.addArg(new Argument("team", ArgType.STRING, this.lockinTeamHandler.getTeamNames()));
        this.addArg(new Argument("material", ArgType.STRING, Stream.of(Material.values()).map(Material::name).collect(Collectors.toList())));
    }

    @Override
    protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
        Player player = (Player) sender;
        String teamName = argValues.get(0).get();
        Material material;
        try {
            material = Material.valueOf(argValues.get(1).get());
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

        this.lockinTeamHandler.addTeam(teamName, material);
    }
}
