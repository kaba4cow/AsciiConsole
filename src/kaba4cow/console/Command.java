package kaba4cow.console;

public abstract class Command {

	public final String name;
	public final String parameters;
	public final String description;

	public Command(String name, String parameters, String description) {
		this.name = name;
		this.parameters = parameters;
		this.description = description;
		Console.addCommand(this);
	}

	public abstract void execute(String[] parameters, int numParameters, StringBuilder output);

	protected boolean invalidParameters(int numParameters1, int numParameters2, StringBuilder output) {
		if (numParameters1 == numParameters2)
			return false;
		invalidParameters(output);
		return true;
	}

	protected void invalidParameters(StringBuilder output) {
		output.append("Invalid parameters\n");
	}

}
