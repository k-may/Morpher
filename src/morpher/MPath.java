package morpher;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class MPath {

	MPathData data;

	public MPath(MPathData data) {
		this.data = data;
	}

	public void draw(PApplet applet) {
		applet.beginShape();
		for (int i = 0; i < data.segments.size(); i++) {
			data.segments.get(i).draw(applet);
		}
		applet.endShape();
	}

	public ArrayList<MSegment> getSegments() {
		return data.segments;
	}

	public MSegment getSegment(int i) {
		return data.segments.get(i);
	}

	public MSegment getFirst() {
		return data.segments.get(0);
	}

	public MSegment getLast() {
		return data.segments.get(data.segments.size() - 1);
	}

	public int numSegments() {
		return data.segments.size();
	}


}
