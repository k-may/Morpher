package morpher;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PVector;
import morpher.utils.BoundingBox;
import morpher.utils.PathParser;

public class MPathData {

	public ArrayList<PVector> points;
	public ArrayList<MSegment> segments;
	public String d;
	private BoundingBox bb;
	
	public MPathData(){
		points = new ArrayList<>();
		segments = new ArrayList<>();
		bb = new BoundingBox();
	}

	public MPathData(String d) {
		this.d = d;
		points = new ArrayList<>();
		segments = new ArrayList<>();
		bb = new BoundingBox();

		init();
	}

	private void init() {
		PathParser pp = new PathParser(d);

		while (!pp.isEnd()) {
			pp.nextCommand();
			switch (pp.command) {
			case "M":
			case "m":
				seeStartTo(pp);
				break;
			case "L":
			case "l":
				seeLineTo(pp);
				break;
			case "H":
			case "h":
				seeHorizontalLineTo(pp);
				break;
			case "V":
			case "v":
				seeVerticalLineTo(pp);
				break;
			case "C":
			case "c":
				seeCurveTo(pp);
				break;
			case "S":
			case "s":
				seeSmoothCurveTo(pp);
				break;
			case "Q":
			case "q":
				seeQuadraticBezierTo(pp);
				break;
			case "T":
			case "t":
				seeSmoothQuadraticBezierTo(pp);
				break;
			case "A":
			case "a":
				seeEllipticalArcTo(pp);
				break;
			case "Z":
			case "z":
				seeCloseTo(pp);
				break;
			}
		}
	}

	private void seeCloseTo(PathParser pp) {
		pp.current = pp.start.get();
		bb.addPoint(pp.start.x, pp.start.y);
		addPoint(pp.current);
	}

	private void seeEllipticalArcTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector curr = pp.current;
			float rx = pp.getScalar();
			float ry = pp.getScalar();
			float xAxisRotation = pp.getScalar() * (float) (Math.PI / 180.0);
			float largeArcFlag = pp.getScalar();
			float sweepFlag = pp.getScalar();
			PVector cp = pp.getAsCurrentPoint();

			// Conversion from endpoint to center parameterization
			// http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes
			// x1", y1"
			PVector currp = new PVector((float) (Math.cos(xAxisRotation)
					* (curr.x - cp.x) / 2.0 + Math.sin(xAxisRotation)
					* (curr.y - cp.y) / 2.0), (float) (-Math.sin(xAxisRotation)
					* (curr.x - cp.x) / 2.0 + Math.cos(xAxisRotation)
					* (curr.y - cp.y) / 2.0));
			// adjust radii
			double l = Math.pow(currp.x, 2) / Math.pow(rx, 2)
					+ Math.pow(currp.y, 2) / Math.pow(ry, 2);
			if (l > 1) {
				rx *= Math.sqrt(l);
				ry *= Math.sqrt(l);
			}
			// cx', cy'
			double s = (largeArcFlag == sweepFlag ? -1 : 1)
					* Math.sqrt(((Math.pow(rx, 2) * Math.pow(ry, 2))
							- (Math.pow(rx, 2) * Math.pow(currp.y, 2)) - (Math
							.pow(ry, 2) * Math.pow(currp.x, 2)))
							/ (Math.pow(rx, 2) * Math.pow(currp.y, 2) + Math
									.pow(ry, 2) * Math.pow(currp.x, 2)));
			if (s == Double.NaN)
				s = 0;

			PVector cpp = new PVector((float) (s * rx * currp.y / ry),
					(float) (s * -ry * currp.x / rx));
			// cx, cy
			PVector centp = new PVector((float) ((curr.x + cp.x) / 2.0
					+ Math.cos(xAxisRotation) * cpp.x - Math.sin(xAxisRotation)
					* cpp.y), (float) ((curr.y + cp.y) / 2.0
					+ Math.sin(xAxisRotation) * cpp.x + Math.cos(xAxisRotation)
					* cpp.y));

			// initial angle
			float a1 = PVector.angleBetween(new PVector(1, 0), new PVector(
					(currp.x - cpp.x) / rx, (currp.y - cpp.y) / ry));
			// angle delta
			PVector u = new PVector((currp.x - cpp.x) / rx, (currp.y - cpp.y)
					/ ry);
			PVector v = new PVector((-currp.x - cpp.x) / rx, (-currp.y - cpp.y)
					/ ry);
			float ad = PVector.angleBetween(u, v);

			if (getCosValue(u, v) <= -1)
				ad = (float) Math.PI;
			if (getCosValue(u, v) >= 1)
				ad = 0;

			if (sweepFlag == 0 && ad > 0)
				ad = ad - (float) (2 * Math.PI);
			if (sweepFlag == 1 && ad < 0)
				ad = ad + (float) (2 * Math.PI);

			// for markers
			PVector halfWay = new PVector(centp.x + rx
					* (float) Math.cos((a1 + (a1 + ad)) / 2), centp.y + ry
					* (float) Math.sin((a1 + (a1 + ad)) / 2));
			pp.addMarkerAngle(halfWay, (a1 + (a1 + ad)) / 2
					+ (sweepFlag == 0 ? -1 : 1) * (float) Math.PI / 2);
			pp.addMarkerAngle(cp, (a1 + ad) + (sweepFlag == 0 ? -1 : 1)
					* (float) Math.PI / 2);

			addPoint(halfWay.x, halfWay.y);
			addPoint(cp.x, cp.y);

			bb.addPoint(cp.x, cp.y);

		}
	}

	float getCosValue(PVector p1, PVector p2) {
		return (p2.x * p1.x + p2.y * p1.y) / (p1.mag() * p2.mag());
	}

	private void seeSmoothQuadraticBezierTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector curr = pp.current;
			PVector cntrl = pp.getReflectedControlPoint();
			pp.control = cntrl;
			PVector cp = pp.getAsCurrentPoint();
			pp.addMarker(cp, cntrl, cntrl);

			addCurvePoint(curr, cntrl, cp);
			bb.addQuadraticCurve(curr.x, curr.y, cntrl.x, cntrl.y, cp.x, cp.y);
		}
	}

	private void seeQuadraticBezierTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector curr = pp.current;
			PVector cntrl = pp.getAsControlPoint();
			PVector cp = pp.getAsCurrentPoint();
			pp.addMarker(cp, cntrl, cntrl);

			addCurvePoint(curr, cntrl, cp);
			bb.addQuadraticCurve(curr.x, curr.y, cntrl.x, cntrl.y, cp.x, cp.y);
		}
	}

	private void seeSmoothCurveTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector curr = pp.current;
			PVector c1 = pp.getReflectedControlPoint();
			PVector c2 = pp.getAsControlPoint();
			PVector p2 = pp.getAsCurrentPoint();

			pp.addMarker(p2, c2, c1);
			addCurvePoint(c1, c2, p2);

			bb.addBezierCurve(curr.x, curr.y, c1.x, c1.y, c2.x, c2.y, p2.x,
					p2.y);
		}

	}

	private void seeCurveTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector current = pp.current.get();
			PVector c1 = pp.getPoint();
			PVector c2 = pp.getAsControlPoint();
			PVector p2 = pp.getAsCurrentPoint();

			pp.addMarker(c1, c2, p2);
			addCurvePoint(c1, c2, p2);
			bb.addBezierCurve(current.x, current.y, c1.x, c1.y, c2.x, c2.y,
					p2.x, p2.y);
		}
	}

	private void seeVerticalLineTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector p1 = new PVector(pp.current.x,
					(pp.isRelativeCommand() ? pp.current.y : 0)
							+ pp.getScalar());
			pp.addMarker(p1, pp.current, null);
			pp.current = p1.get();
			addPoint(p1);
			bb.addPoint(pp.current);
		}
	}

	private void seeHorizontalLineTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector p1 = new PVector(
					(pp.isRelativeCommand() ? pp.current.x : 0)
							+ pp.getScalar(), pp.current.y);
			pp.addMarker(p1, pp.current, null);
			pp.current = p1.get();

			addPoint(p1);
			bb.addPoint(pp.current);
		}
	}

	private void seeLineTo(PathParser pp) {
		while (!pp.isCommandOrEnd()) {
			PVector p2 = pp.current;
			PVector p1 = pp.getAsCurrentPoint();
			pp.addMarker(p1, p2, null);

			addPoint(p1);
			bb.addPoint(p1);
		}
	}

	private void seeStartTo(PathParser pp) {
		PVector p1 = pp.getAsCurrentPoint();
		pp.addMarker(p1);
		addPoint(p1.x, p1.y);
		bb.addPoint(p1.x, p1.y);

		pp.start = pp.current;
		while (!pp.isCommandOrEnd()) {
			p1 = pp.getAsCurrentPoint();
			pp.addMarker(p1, pp.start, null);
			addPoint(p1.x, p1.y);
			bb.addPoint(p1.x, p1.y);
		}
	}

	private void addCurvePoint(PVector c1, PVector c2, PVector p2) {
		addSegment(points.get(points.size() - 1), c1, c2, p2);
		points.add(p2);
	}

	private void addPoint(float x, float y) {
		addPoint(new PVector(x, y));
	}

	private void addPoint(PVector p) {
		if (points.size() > 0)
			addSegment(points.get(points.size() - 1), p);

		points.add(p);
	}

	// straight segment
	private void addSegment(PVector p1, PVector p2) {
		segments.add(new MSegment(p1.get(), p1.get(), p2.get(), p2.get()));
	}

	// curve segment
	private void addSegment(PVector p1, PVector c1, PVector c2, PVector p2) {
		segments.add(new MSegment(p1.get(), c1.get(), c2.get(), p2.get()));
	}

	public static MPathData create(MSegment[] segments2) {
		MPathData data = new MPathData();
		data.segments = new ArrayList<MSegment>(Arrays.asList(segments2));
		return data;
	}
}
