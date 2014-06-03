package morpher;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import morpher.utils.PathParser;
import processing.core.PApplet;
import processing.data.XML;

public class MorpherLoader {

	private PApplet applet;
	private MPath[] paths;

	public MorpherLoader(String filePath, PApplet applet) {
		this.applet = applet;

		// load svg
		MSVGData data;

		XML svg = applet.loadXML(filePath);
		if (svg == null)
			throw new MorpherLoaderException("couldn't load file : " + filePath);

		XML[] rawPaths = svg.getChildren("path");

		paths = new MPath[rawPaths.length];

		for (int i = 0; i < rawPaths.length; i++) {
			XML p = rawPaths[i];
			paths[i] = digestPath(p.getString("d"));
		}
	}

	private MPath digestPath(String d) {
		// get rid of all commas
		d = d.replaceAll("[, ]", " ");

		// separate commands from commands
		d = d.replaceAll("([MmZzLlHhVvCcSsQqTtAa])([MmZzLlHhVvCcSsQqTtAa])",
				"$1 $2");

		// separate commands from commands
		d = d.replaceAll("([MmZzLlHhVvCcSsQqTtAa])([^\\s])", "$1 $2");

		// separate commands from points
		d = d.replaceAll("([^\\s])([MmZzLlHhVvCcSsQqTtAa])", "$1 $2");

		// separate commands from points
		d = d.replaceAll("([0-9])([+\\-])", "$1 $2");

		// separate digits when no comma
		d = d.replaceAll("(\\.[0-9]*)(\\.)", "$1 $2");

		// separate digits when no comma
		d = d.replaceAll("([Aa](\\s+[0-9]+){3})\\s+([01])\\s*([01])",
				"$1 $3 $4 ");

		// shorthand elliptical arc path syntax
		d = d.replaceAll("[\\s\\r\\t\\n]+", " ");

		// compress multiple spaces
		d = d.replaceAll("^\\s+|\\s+$", "");

		MPathData pathData = new MPathData(d);

		return new MPath(pathData);
	}

	public MPath[] getPaths() {
		return paths;
	}

	public void draw() {

		for (int i = 0; i < paths.length; i++) {
			paths[i].draw(applet);
		}
	}
}
