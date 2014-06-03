package morpher.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

import processing.core.PApplet;
import processing.core.PVector;

public class PathParser {

	private String[] tokens;
	private int i = -1;
	public String command;
	private String previousCommand;
	public PVector control;
	public PVector current;
	public PVector start;
	private ArrayList<Float> angles;
	private ArrayList<PVector> points;

	public PathParser(String d) {
		PApplet.println(d);
		
		points = new ArrayList<>();
		angles = new ArrayList<>();
		current = new PVector();
		start = new PVector();
		current = new PVector();
		
		this.tokens = d.split(" ");
	}

	public boolean isEnd() {
		return i >= tokens.length - 1;
	}

	public boolean isCommandOrEnd() {
		if (isEnd())
			return true;
		
		Pattern m = Pattern.compile("^[A-Za-z]$");
		return m.matcher(this.tokens[this.i + 1]).find();
	}

	public boolean isRelativeCommand() {
		switch(this.command) {
			case "m":
			case "l":
			case "h":
			case "v":
			case "c":
			case "s":
			case "q":
			case "t":
			case "a":
			case "z":
				return true;
		}
		
		return false;
	}

	public String getToken() {
		this.i++;
		return this.tokens[this.i];
	}

	public float getScalar() {
		return Float.parseFloat(this.getToken());
	}

	public void nextCommand() {
		this.previousCommand = this.command;
		this.command = this.getToken();
	}

	public PVector getPoint() {
		PVector p = new PVector(this.getScalar(), this.getScalar());
		return this.makeAbsolute(p);
	}

	public PVector getAsControlPoint() {
		PVector p = this.getPoint();
		this.control = p;
		return p;
	}

	public PVector getAsCurrentPoint() {
		PVector p = this.getPoint();
		this.current = p;
		return p;
	}

	public PVector getReflectedControlPoint() {
		if (this.previousCommand.toLowerCase() != "c" && this.previousCommand.toLowerCase() != "s") {
			return this.current;
		}

		// reflect point
		PVector p = new PVector(2 * this.current.x - this.control.x, 2 * this.current.y - this.control.y);
		return p;
	}

	public PVector makeAbsolute(PVector p) {
		if (this.isRelativeCommand()) {
			p.x += this.current.x;
			p.y += this.current.y;
		}
		return p;
	}

	public void addMarker(PVector p){
		addMarker(p, null, null);
	}
	
	public void addMarker(PVector p, PVector from, PVector priorTo) {
		// if the last angle isn't filled in because we didn't have this point yet ...
		if (priorTo != null && this.angles.size() > 0 && this.angles.get(this.angles.size() - 1) == null) {
			this.angles.add(PVector.angleBetween(this.points.get(this.points.size() - 1), priorTo));
		}
		float angle = from == null ? Float.NaN : PVector.angleBetween(from, p);
		this.addMarkerAngle(p, angle);
	}

	public void addMarkerAngle(PVector p, float a) {
		this.points.add(p);
		this.angles.add(a);
	}

	public ArrayList<PVector> getMarkerPoint() {
		return this.points;
	}
	
	public ArrayList<Float> getMarkerAngles() {
		for (int i = 0; i < this.angles.size(); i++) {
			if (this.angles.get(i) == null) {
				for (int j = i + 1; j < this.angles.size(); j++) {
					if (this.angles.get(j) != null) {
						this.angles.add(j, this.angles.get(j));
						break;
					}
				}
			}
		}
		return this.angles;
	}
}
