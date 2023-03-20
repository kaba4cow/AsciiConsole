package kaba4cow.console;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.prefs.Preferences;

public abstract class Console {

	private static LinkedHashMap<String, Command> commands = new LinkedHashMap<>();

	private static String[] parameterArray = new String[32];
	private static boolean exit = false;

	private static Preferences preferences;
	private static int backgroundColor;
	private static int foregroundColor;
	private static int windowWidth;
	private static int windowHeight;
	private static File directory;

	private static StringBuilder output = new StringBuilder();

	public static void init(Class<?> c) {
		preferences = Preferences.userNodeForPackage(c);
		backgroundColor = preferences.getInt("color-b", 0x000);
		foregroundColor = preferences.getInt("color-f", 0xFFF);
		windowWidth = preferences.getInt("width", -1);
		windowHeight = preferences.getInt("height", -1);
		directory = new File(preferences.get("home", System.getProperty("user.dir")));
		if (!directory.exists() || directory.isFile())
			directory = new File(System.getProperty("user.dir"));

		new Command("help", "", "Prints all available commands") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				for (String name : commands.keySet()) {
					Command command = commands.get(name);
					output.append("-> " + name.toUpperCase() + " " + command.parameters + "\n");
					output.append(command.description + "\n\n");
				}
			}
		};

		new Command("exit", "", "Closes the program") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				savePreferences();
				exit = true;
			}
		};

		new Command("dir", "", "Prints all files in current directory") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				File[] files = directory.listFiles();
				for (File file : files)
					output.append("-> " + file.getName() + "\n");
			}
		};

		new Command("cd", "[path]", "Changes current directory") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;
				String path = parameters[0];
				File file;
				if (path.equals("..")) {
					if (directory.getParentFile() != null)
						directory = directory.getParentFile();
				} else {
					file = new File(path);
					if (file.isDirectory())
						directory = file;
					else {
						path = directory.getAbsolutePath() + "\\" + path;
						file = new File(path);
						if (file.isDirectory())
							directory = file;
						else
							output.append(path + " is not a directory\n");
					}
				}
			}
		};

		new Command("md", "[name]", "Creates new directory") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;
				String name = parameters[0];
				File file = new File(directory.getAbsolutePath() + "/" + name);
				if (!file.mkdirs())
					output.append("Could not create directory\n");
			}
		};

		new Command("echo", "[message]", "Prints a message") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				for (int i = 0; i < numParameters; i++)
					output.append(parameters[i] + " ");
				output.append('\n');
			}
		};

		new Command("resize", "[width] [height]", "Resizes window (-1 for fullscreen)") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 2, output))
					return;
				int w, h;
				try {
					w = Integer.parseInt(parameters[0]);
					h = Integer.parseInt(parameters[1]);
				} catch (NumberFormatException e) {
					invalidParameters(output);
					return;
				}
				windowWidth = w;
				windowHeight = h;
			}
		};

		new Command("color-b", "[color]", "Sets background color (000-FFF)") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;
				try {
					backgroundColor = Integer.parseInt(parameters[0], 16);
				} catch (NumberFormatException e) {
					invalidParameters(output);
				}
			}
		};

		new Command("color-f", "[color]", "Sets foreground color (000-FFF)") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;
				try {
					foregroundColor = Integer.parseInt(parameters[0], 16);
				} catch (NumberFormatException e) {
					invalidParameters(output);
				}
			}
		};
	}

	public static void addCommand(Command command) {
		commands.put(command.name, command);
	}

	public static boolean processCommand(String fileName, String line) {
		output.append('\n');
		String name = getCommandName(line);
		int numParameters = getCommandParameters(name, line);

		Command command = commands.get(name);

		if (line.isEmpty())
			output.append('\n');
		else if (command == null)
			output.append("Unknown command: " + line + "\n");
		else
			command.execute(parameterArray, numParameters, output);
		output.append('\n');

		if (exit)
			return true;

		if (fileName == null)
			output.append(directory.getAbsolutePath() + ": ");
		else
			output.append(directory.getAbsolutePath() + " -> " + fileName + ": ");

		return false;
	}

	private static String getCommandName(String string) {
		String name = "";
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c == ' ')
				break;
			else
				name += c;
		}
		return name;
	}

	private static int getCommandParameters(String name, String string) {
		if (name.length() == string.length())
			return 0;

		string = string.substring(name.length()) + " ";
		final int length = string.length();

		int index = 0;
		boolean backslash = false;
		boolean space = false;
		String token = "";

		for (int i = 1; i < length; i++) {
			char c = string.charAt(i);
			if (!space && !backslash && c == ' ') {
				parameterArray[index++] = token;
				token = "";
				space = true;
			} else if (c == '\\') {
				backslash = true;
				space = false;
			} else {
				token += c;
				space = false;
				backslash = false;
			}

			if (index >= parameterArray.length)
				break;
		}

		for (int i = index; i < parameterArray.length; i++)
			parameterArray[i] = null;

		return index;
	}

	public static String getOutput() {
		String string = output.toString();
		output = new StringBuilder();
		return string;
	}

	public static File getDirectory() {
		return directory;
	}

	public static int getBackgroundColor() {
		return backgroundColor;
	}

	public static int getForegroundColor() {
		return foregroundColor;
	}

	public static int getWindowWidth() {
		return windowWidth;
	}

	public static int getWindowHeight() {
		return windowHeight;
	}

	private static void savePreferences() {
		preferences.put("color-b", Integer.toString(backgroundColor));
		preferences.put("color-f", Integer.toString(foregroundColor));
		preferences.put("width", Integer.toString(windowWidth));
		preferences.put("height", Integer.toString(windowHeight));
		preferences.put("home", directory.getAbsolutePath());
	}

}
