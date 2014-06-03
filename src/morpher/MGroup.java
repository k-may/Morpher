package morpher;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PVector;

public class MGroup {
	MPath p1, p2;
	MSegment[] interSegments;

	MSegmentsGroup[] groups;

	MPath current;
	int maxLength;

	ArrayList<MPath[]> morphMap;
	boolean isHetero = false;

	public MGroup(MPath p1, MPath p2) {
		this.p1 = p1;
		this.p2 = p2;
		
		isHetero = p1.numSegments() != p2.numSegments(); 

		if (isHetero)
			setHetero();
		else{
			//only one group
			groups = new MSegmentsGroup[]{new MSegmentsGroup(p1.getSegments().toArray(new MSegment[p1.numSegments()]),
					p2.getSegments().toArray(new MSegment[p2.numSegments()]))};
		}
	}

	private void setHetero() {
		isHetero = true;

		int i;
		float j = 0, k = 0;

		float diff = p1.numSegments() / p2.numSegments();
		// divide initial path into two
		int minCount = Math.min(p1.numSegments(), p2.numSegments());

		groups = new MSegmentsGroup[minCount];

		for (i = 0; i < minCount; i++) {
			ArrayList<MSegment> g1 = new ArrayList<>();
			ArrayList<MSegment> g2 = new ArrayList<>();
			if (diff < 1) {
				j = i;
				g1.add(p1.getSegment((int) j));

				while (k * diff < (j + 1)) {
					g2.add(p2.getSegment((int) k));
				}
			} else {
				k = i;
				g2.add(p2.getSegment((int) k));
				while (j / diff < k + 1) {
					g1.add(p1.getSegment((int) j));
				}
			}

			groups[i] = new MSegmentsGroup(g1.toArray(new MSegment[g1.size()]),
					g2.toArray(new MSegment[g2.size()]));
		}
	}

	public MPath interpolate(float value) {

		ArrayList<MSegment> segs = new ArrayList<>();
		for (int i = 0; i < groups.length; i++) {
			segs.addAll(new ArrayList<MSegment>(Arrays.asList(groups[i]
					.interpolate(value))));
		}

		return new MPath(
				MPathData.create(segs.toArray(new MSegment[segs.size()])));
	}

	class MSegmentsGroup {
		
		MSegment[] start;
		MSegment[] interStart;
		MSegment[] interDest;
		MSegment[] dest;
		MSegment interSeg;

		public MSegmentsGroup(MSegment[] start, MSegment[] dest) {
			this.start = start;
			this.dest = dest;

			if (start.length != dest.length) {
				interSeg = getInterSegment();
				interStart = getInterPath(start);
				interDest = getInterPath(dest);
			}
		}

		public MSegment[] interpolate(float value) {
			boolean isHetero = interSegments != null;

			MSegment[] begin = isHetero ? value <= 0.5f ? start : interStart
					: start;
			MSegment[] end = isHetero ? value <= 0.5f ? interDest : dest : dest;

			MSegment[] segs = new MSegment[start.length];

			for (int i = 0; i < start.length; i++) {
				segs[i] = MSegment.interpolate(start[i], dest[i], value);
			}

			return segs;
		}

		private MSegment getInterSegment() {

			PVector pt1 = PVector.lerp(start[0].pt1, dest[0].pt1, 0.5f);
			PVector pt2 = PVector.lerp(start[start.length - 1].pt1,
					dest[dest.length - 1].pt2, 0.5f);

			// create intermediary line between irregular paths
			return new MSegment(pt1, pt2);
		}

		private MSegment[] getInterPath(MSegment[] ref) {

			PVector pt1 = interSeg.pt1;
			PVector pt2;
			// create 1:1 mapping of segments
			float percentage;
			int i = 0;
			MSegment[] segs = new MSegment[start.length];

			pt2 = pt1.get();
			while (i < ref.length) {
				percentage = (i + 1) / ref.length;
				pt1 = pt2.get();
				pt2 = PVector.lerp(interSeg.pt1, interSeg.pt2, percentage);
				segs[i] = new MSegment(pt1, pt2);
				i++;
			}

			return segs;
		}

	}
}
