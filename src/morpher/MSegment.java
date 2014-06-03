package morpher;
import processing.core.PApplet;
import processing.core.PVector;

public class MSegment {

	public PVector pt1;
	public PVector pt2;
	public PVector ctrl1;
	public PVector ctrl2;

	public MSegment(PVector pt1, PVector ctrl1, PVector ctrl2, PVector pt2) {
		this.pt1 = pt1;
		this.pt2 = pt2;
		this.ctrl1 = ctrl1;
		this.ctrl2 = ctrl2;
	}

	public MSegment(PVector pt12, PVector pt22) {
		this(pt12, pt12.get(), pt22.get(), pt22);
	}

	public void draw(PApplet applet) {
		//applet.bezier(pt1.x, pt1.y, ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, pt2.x,pt2.y);
		applet.vertex(pt1.x, pt1.y);
		applet.bezierVertex(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, pt2.x,pt2.y);
		/*
		 * change to fillable shape
		 * applet.vertex(pt1.x, pt2); applet.bezierVertex(pt1.x, pt1.y, ctrl1.x,
		 * ctrl1.y, y4);
		 */
	}
	
	public void translate(int x, int y){
		PVector v = new PVector(x, y);
		pt1.add(v);
		pt2.add(v);
		ctrl1.add(v);
		ctrl2.add(v);
	}
	
	public void scale(float n){
		ctrl1 = PVector.sub(ctrl2, pt1);
		ctrl2 = PVector.sub(ctrl1, pt2);
		
		pt1.mult(n);
		pt2.mult(n);
		
		ctrl1.mult(n);
		ctrl1 = PVector.add(pt1, ctrl1);
		
		ctrl2.mult(n);
		ctrl2 = PVector.add(pt2, ctrl2);
		
	}
	
	public MSegment clone(){
		//TODO test this shiit
		return new MSegment(pt1.get(), ctrl1.get(), ctrl2.get(), pt2.get());
	}
	
	public static MSegment interpolate(MSegment s1, MSegment s2, float value){
		PVector p1 = PVector.lerp(s1.pt1, s2.pt1, value);
		PVector p2 = PVector.lerp(s1.pt2, s2.pt2, value);
		
		PVector cV = PVector.sub(s2.ctrl1, s1.ctrl1);
		cV.mult(value);
		PVector c1 = PVector.add(cV, s1.ctrl1);
		
		cV = PVector.sub(s2.ctrl2, s1.ctrl2);
		cV.mult(value);
		PVector c2 = PVector.add(cV, s2.ctrl2);
		
		return new MSegment(p1, c1, c2, p2);
	}
}
