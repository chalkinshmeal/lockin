package chalkinshmeal.lockout.utils.cmdframework.argument;

/*
 * An ENUM. We can refer to ArgType.INTEGER to just mean integer
 */
public enum ArgType {
	
	INTEGER("integer"),
	DECIMAL("number"),
	STRING("string"),
	BOOLEAN("boolean");
	
	private final String simpleName;
	
	ArgType(String simpleName) {
		this.simpleName = simpleName;
	}
	
	public String simpleName() {
		return simpleName;
	}
}