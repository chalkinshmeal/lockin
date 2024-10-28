package chalkinshmeal.lockout.utils.cmdframework.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import chalkinshmeal.lockout.utils.cmdframework.argument.ArgValue;
import chalkinshmeal.lockout.utils.cmdframework.argument.Argument;

import java.util.*;

/**
 * A command with specifiable arguments
 */
@SuppressWarnings("deprecation")
public abstract class ArgCommand extends BaseCommand {
	
	protected final List<Argument> arguments;
	private final List<String> flags;
	private boolean useFlags;
	
	public ArgCommand(String name, boolean useFlags) {
		super(name);
		arguments = new ArrayList<>();
		flags = new LinkedList<>();
		this.useFlags = useFlags;
	}
	
	/**
	 * Adds an optional or mandatory argument that has to be provided for with further string inputs behind this command's name
	 */
	public void addArg(Argument argument) {
		arguments.add(argument);
	}
	
	/**
	 * Adds an optional flag that can be provided anywhere inbetween when executing this command beginning with "-"
	 */
	public void addFlag(String flagName) {
		flags.add("-" + flagName.toLowerCase());
	}
	
	public List<Argument> getArgs() {
		return arguments;
	}
	public Argument getArgByName(String name) {
		for (Argument arg : this.arguments) {
			if (arg.getName() == name) return arg;
		}
		return null;
	}

	public void setTabListOfArgument(String argName, List<String> tabList) {
		for (Argument arg : this.arguments) {
			if (arg.getName() == argName) {
				arg.setTabList(tabList);
			}
		}
	}
	
	/**
	 * Converts the passed sting arguments to the into ArgValues according to the beforehand defined Arguments
	 */
	@Override
	public void onCommand(CommandSender sender, String[] stringArgs) {
		int argCount = getArgs().size();
		int inputCount = stringArgs.length;
		
		List<ArgValue> values = new ArrayList<>();
		Set<String> usedFlags = new HashSet<>();
		
		try {
			for (int i = 0; i < Math.max(inputCount, argCount); i++) {
				String input = i < inputCount ? stringArgs[i] : null;
				
				if ((isFlag(input)) && (this.useFlags)) {
					usedFlags.add(matchFlag(input.toLowerCase()));
					continue;
				}
				values.add(i < argCount ? getArgs().get(i).createValue(input) : new ArgValue(input));
			}
		} catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			sendUsage(sender);
			return;
		}

		if (getPermission() != null && !sender.hasPermission(getPermission())) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
			return;
		}
		executeArgs(sender, values, usedFlags);
	}
	
	protected abstract void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags);
	
	protected boolean isFlag(String input) {
		if (input == null || !input.startsWith("-")) {
			return false;
		}
		try {
			Double.valueOf(input);
		} catch (NumberFormatException e) {
			return true;
		}
		return false;
	}
	
	protected String matchFlag(String input) {
		for (String flag : flags) {
			if (flag.equals(input)) {
				return flag.substring(1);
			}
		}
		throw new IllegalArgumentException("No flag '" + input + "' found.");
	}
	
	@Override
	public String getUsage() {
		StringBuilder usage = new StringBuilder(super.getUsage());
		
		for (Argument arg : getArgs()) {
			usage.append(" <");
			usage.append(arg.getName());
			usage.append(">");
		}
		for (String flag : flags) {
			usage.append(" ");
			usage.append(flag);
		}
		return usage.toString();
	}
	
	@Override
	public List<String> getTabList(String[] stringArgs) {
		String tabbedArg = stringArgs[stringArgs.length - 1];
		
		if (isFlag(tabbedArg)) {
			return flags;
		}
		if (this.arguments.size() >= stringArgs.length) {
			return this.arguments.get(stringArgs.length - 1).getTabList();
		}
		return new LinkedList<>();
	}
}
