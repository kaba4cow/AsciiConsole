package kaba4cow.console;

import java.util.ArrayList;

import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.core.Renderer;
import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.Drawer;
import kaba4cow.ascii.drawing.Glyphs;
import kaba4cow.ascii.toolbox.Colors;

public class ConsoleProgram {

	private ArrayList<String> history;
	private int index;

	private String output;
	private String text;

	private int scroll;
	private int maxScroll;

	protected static int consoleColor = 0x000FFF;

	public ConsoleProgram(Class<?> c, String program) {
		scroll = 0;
		maxScroll = 0;

		text = "";

		history = new ArrayList<>();
		index = 0;

		Console.init(c);
		Console.processCommand(null, "");
		output = program + Console.getOutput();

		updateWindow();
	}

	public static void updateWindow() {
		int width = Console.getWindowWidth();
		int height = Console.getWindowHeight();
		if (Window.getWidth() != width || Window.getHeight() != height) {
			if (width < 0 || height < 0) {
				if (!Window.isFullscreen())
					Window.createFullscreen();
			} else
				Window.createWindowed(width, height);
		}

		consoleColor = Colors.combine(Console.getBackgroundColor(), Console.getForegroundColor());
		Window.setBackground(Glyphs.SPACE, consoleColor);
	}

	public void updateConsole(String fileName) {
		scroll -= 2 * Input.getScroll();
		if (scroll < 0)
			scroll = 0;
		if (scroll > maxScroll)
			scroll = maxScroll;

		if (Input.isKeyDown(Input.KEY_ENTER)) {
			if (Console.processCommand(fileName, text))
				Engine.requestClose();
			String cmd = Console.getOutput();
			output += text + "\n" + cmd;
			if (history.isEmpty() || !history.get(history.size() - 1).equalsIgnoreCase(text))
				history.add(text);
			text = "";
			index = history.size();
			renderConsole();
			scroll = maxScroll;
		} else if (!history.isEmpty() && Input.isKeyDown(Input.KEY_UP)) {
			index--;
			if (index < 0)
				index = history.size() - 1;
			text = history.get(index);
		} else if (!history.isEmpty() && Input.isKeyDown(Input.KEY_DOWN)) {
			index++;
			if (index >= history.size())
				index = 0;
			text = history.get(index);
		} else
			text = Input.typeString(text);
	}

	public void renderConsole() {
		Renderer.setFont(Console.getFontIndex());
		Window.setBackground(' ', consoleColor);

		int x = 0;
		int y = -scroll;
		for (int i = 0; i < output.length(); i++) {
			char c = output.charAt(i);
			if (c == '\n') {
				x = 0;
				y++;
			} else if (c == '\t')
				x += 4;
			else
				Drawer.draw(x++, y, c, consoleColor);

			if (x >= Window.getWidth()) {
				x = 0;
				y++;
			}
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			Drawer.draw(x++, y, c, consoleColor);

			if (x >= Window.getWidth()) {
				x = 0;
				y++;
			}
		}

		y += scroll;
		if (y < Window.getHeight())
			maxScroll = 0;
		else
			maxScroll = y + 5 - Window.getHeight();
	}

}
