package morpher.utils;

import processing.core.PVector;

public class BoundingBox {
	float x1, x2, y1, y2;
	PVector size;

	public BoundingBox(){
		x1 = Float.NaN;
		x2 = Float.NaN;
		y1 = Float.NaN;
		y2 = Float.NaN;
	}
	
	public void addPoint(PVector p){
		addPoint(p.x, p.y);
	}
	
	public void addPoint(float x, float y) {
		if (x != Float.NaN) {
			if (x1 == Float.NaN|| x2 == Float.NaN) {
				this.x1 = x;
				this.x2 = x;
			}
			if (x < this.x1)
				this.x1 = x;
			if (x > this.x2)
				this.x2 = x;
		}

		if (y != Float.NaN) {
			if (y1 == Float.NaN|| y2 == Float.NaN) {
				this.y1 = y;
				this.y2 = y;
			}
			if (y < this.y1)
				this.y1 = y;
			if (y > this.y2)
				this.y2 = y;
		}

	}
	
	void addX(float x) {
		this.addPoint(x, Float.NaN);
	}
	
	void addY(float y) {
		this.addPoint(Float.NaN, y);
	}

	public float width(){
		return x2 - x1;
	}
	
	public float height(){
		return y2 - y1;
	}
	
	public void scale(float value){
		x2 = x1 + width()*value;
		y2 = y1 + height()*value;
	}
	
	public void addBoundingBox(BoundingBox bb) {
		this.addPoint(bb.x1, bb.y1);//(bb.x1, bb.y1);
		this.addPoint(bb.x2, bb.y2);//bb.x2, bb.y2);
	}
	

	public void addQuadraticCurve(float p0x,float p0y,float p1x,float p1y,float p2x,float p2y) {
		float cp1x = p0x + 2 / 3 * (p1x - p0x);
		// CP1 = QP0 + 2/3 *(QP1-QP0)
		float cp1y = p0y + 2 / 3 * (p1y - p0y);
		// CP1 = QP0 + 2/3 *(QP1-QP0)
		float cp2x = cp1x + 1 / 3 * (p2x - p0x);
		// CP2 = CP1 + 1/3 *(QP2-QP0)
		float cp2y = cp1y + 1 / 3 * (p2y - p0y);
		// CP2 = CP1 + 1/3 *(QP2-QP0)
		this.addBezierCurve(p0x, p0y, cp1x, cp2x, cp1y, cp2y, p2x, p2y);
	}

	public void addBezierCurve(float p0x, float p0y, float p1x, float p1y, float p2x, float p2y,float p3x, float p3y) {
		// from http://blog.hackers-cafe.net/2009/06/how-to-calculate-bezier-curves-bounding.html
		PVector p0 = new PVector(p0x, p0y);
		PVector p1 = new PVector(p1x, p1y);
		PVector p2 = new PVector(p2x, p2y);
		PVector p3 = new PVector(p3x, p3y);
		
		this.addPoint(p0);
		this.addPoint(p3);

		for (int i = 0; i <= 1; i++) {
			float pp0 = i == 0 ? p0.x : p0.y;
			float pp1 = i == 0 ? p1.x : p1.y;
			float pp2 = i == 0 ? p2.x : p2.y;
			float pp3 = i == 0 ? p3.x : p3.y;

			/*var f = function(t) {
				return Math.pow(1 - t, 3) * p0[i] + 3 * Math.pow(1 - t, 2) * t * p1[i] + 3 * (1 - t) * Math.pow(t, 2) * p2[i] + Math.pow(t, 3) * p3[i];
			}*/
			float b = 6 * pp0 - 12 * pp1 + 6 * pp2;
			float a = -3 * pp0 + 9 * pp1 - 9 * pp2 + 3 * pp3;
			float c = 3 * pp1 - 3 * pp0;

			if (a == 0) {
				if (b == 0)
					continue;
				float t = -c / b;
				if (0 < t && t < 1) {
					if (i == 0)
						this.addX((float)f(t, pp0, pp1, pp2, pp3));
					if (i == 1)
						this.addY((float)f(t, pp0, pp1, pp2, pp3));
				}
				continue;
			}

			float b2ac = (float)(Math.pow(b, 2) - 4 * c * a);
			if (b2ac < 0)
				continue;
			float t1 = (float)(-b + Math.sqrt(b2ac)) / (2 * a);
			if (0 < t1 && t1 < 1) {
				if (i == 0)
					this.addX((float)f(t1, pp0, pp1, pp2, pp3));
				if (i == 1)
					this.addY((float)f(t1, pp0, pp1, pp2, pp3));
			}
			
			float t2 = (float)(-b - Math.sqrt(b2ac)) / (2 * a);
			if (0 < t2 && t2 < 1) {
				if (i == 0)
					this.addX((float)f(t2, pp0, pp1, pp2, pp3));
				if (i == 1)
					this.addY((float)f(t2, pp0, pp1, pp2, pp3));
			}
		}
	}
	
	private double f(float t, float p0, float p1, float p2,float p3) {
		return Math.pow(1 - t, 3) * p0 + 3 * Math.pow(1 - t, 2) * t * p1 + 3 * (1 - t) * Math.pow(t, 2) * p2 + Math.pow(t, 3) * p3;
	}
	
}
