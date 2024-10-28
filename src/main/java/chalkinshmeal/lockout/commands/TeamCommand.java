package chalkinshmeal.lockout.commands;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.team.LockoutTeamHandler;
import chalkinshmeal.lockout.utils.cmdframework.argument.ArgType;
import chalkinshmeal.lockout.utils.cmdframework.argument.ArgValue;
import chalkinshmeal.lockout.utils.cmdframework.argument.Argument;
import chalkinshmeal.lockout.utils.cmdframework.command.ArgCommand;
import chalkinshmeal.lockout.utils.cmdframework.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TeamCommand extends ArgCommand {
    private final LockoutTeamHandler lockoutTeamHandler;

    // Constructor
    public TeamCommand(JavaPlugin plugin, CommandHandler cmdHandler, LockoutTeamHandler lockoutTeamHandler) {
        super("team", false);
        this.setPlayerRequired(true);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Creates a team for Lockout", NamedTextColor.WHITE))
            .build());
        
        this.lockoutTeamHandler = lockoutTeamHandler;
        this.addArg(new Argument("team", ArgType.STRING, this.lockoutTeamHandler.getTeamNames()));
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

        this.lockoutTeamHandler.addTeam(teamName, material);
    }
}
