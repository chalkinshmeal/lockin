package chalkinshmeal.lockout.utils.cmdframework.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A command that can have other commands as arguments/children.
 */
@SuppressWarnings("deprecation")
public class ParentCommand extends BaseCommand {
	
	private final Set<BaseCommand> children;
	private String childrenName;
	
	public ParentCommand(String name) {
		super(name);
		children = new HashSet<>();
		childrenName = "";
	}
	
	public Set<BaseCommand> getChildren() { return children; }
	public List<BaseCommand> getSortedChildren() {
		List<BaseCommand> commands = new ArrayList<>(this.getChildren());
		Collections.sort(commands, new Comparator<BaseCommand>() {
			public int compare(BaseCommand c1, BaseCommand c2) {
				return c1.getName().compareTo(c2.getName());
			}
		});
		return commands;
	}
	
	/**
	 * Adds a sub command to this command that can be accessed as the first argument behind this command's name
	 */
	public void addChild(BaseCommand child) {
		children.add(child.setParent(this));
	}
	
	public String getParentUsage() {
		if (getParent() != null) {
			return getParent().getParentUsage() + " " + getName();
		}
		return ChatColor.RED + "/" + getName();
	}
	
	public void setChildrenName(String childrenName) {
		this.childrenName = childrenName;
	}
	
	@Override
	public String getUsage() {
		return super.getUsage() + " <" + childrenName + ">";
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sendUsage(sender);
			return;
		}
		for (BaseCommand child : children) {
			if (child.matchesAlias(args[0])) {
				child.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
				return;
			}
		}
		sendUsage(sender);
	}
	
	@Override
	public List<String> getTabList(String[] arguments) {
		if (arguments.length == 1) {
			return children.stream().map(BaseCommand::getName).collect(Collectors.toList());
		}
		List<String> tabList = new LinkedList<>();
		
		for (BaseCommand child : getChildren()) {
			if (child.matchesAlias(arguments[0])) {
				return child.getTabList(Arrays.copyOfRange(arguments, 1, arguments.length));
			}
		}
		return tabList;
	}
}
