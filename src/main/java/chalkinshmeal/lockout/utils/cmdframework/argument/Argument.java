package chalkinshmeal.lockout.utils.cmdframework.argument;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class to convert string inputs in commands into ArgValues according to given ArgType and to throw exceptions
 * if wrong inputs were given
 * Has the methods:
 * createValue() - creates a ArgValue object from the input
 * setters - setDefault()
 * getters - getDefault(), getName(), getType(), getTabList()
 */
@SuppressWarnings("deprecation")
public class Argument {
	
	private final String name;
	private final ArgType type;
	private List<String> tabList;
	private ArgValue defaultValue;
	private boolean required;

	// Constructors
	public Argument(String name, ArgType type) {
		this(name, type, new String[]{});
	}
	
	public Argument(String name, ArgType type, String... tabList) {
		this.name = name;
		this.type = type;
		this.tabList = new ArrayList<>();
		this.tabList.addAll(Arrays.asList(tabList));
		this.required = true;
	}
	public Argument(String name, ArgType type, List<String> tabList) {
		this.name = name;
		this.type = type;
		this.tabList = tabList;
		this.required = true;
	}

	public Argument(String name, ArgType type, List<String> tabList, boolean required) {
		this.name = name;
		this.type = type;
		this.tabList = tabList;
		this.required = required;
	}

	public ArgValue createValue(String input) {
		if (input != null) {
			return new ArgValue(input, type);
		} else if (defaultValue != null) {
			return defaultValue;
		} else if (!this.required) {
			return new ArgValue("", type);
		} else {
			throw new IllegalArgumentException(ChatColor.RED + "Missing argument '" + getName() + "'.");
		}
	}
	
	public ArgValue getDefault() {
		return defaultValue;
	}
	
	public String getName() {
		return name;
	}
	
	public ArgType getType() {
		return type;
	}
	
	public List<String> getTabList() {
		return tabList;
	}
	
	public Argument setDefault(String value) {
		defaultValue = new ArgValue(value, type);
		return this;
	}

	public void setTabList(List<String> tabList) {
		this.tabList = tabList;
	}
}