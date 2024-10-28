package chalkinshmeal.lockout.utils.cmdframework.argument;

/*
 * Flag class
 * A simple class that is a wrapper around a simple name
 * Example: -floor
 */
public class Flag {
	
	private final String name;
	
	public Flag(String name, String shortName) {
		this.name = name.toLowerCase();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean matches(String input) {
		return input.equals(name);
	}
}
