package ocent;

import java.util.*;
import base.*;
import testbed.*;

public class OneCenter {
  private static final boolean db = false;
  private static int count;

  public static void main(String[] args) {
    DArray pts = new DArray();
    Random r = new Random(1969);
    int n = 100;
    for (int i = 0; i < n; i++)
      pts.add(new FPoint2(r.nextInt(100), r.nextInt(100)));

    EdDisc d = findCenter(pts, r);
    Streams.out.println("pts=\n" + pts.toString(true));
    Streams.out.println("disc=" + d);
    if (db) {
      Streams.out.println("count=" + count);
      Streams.out.println("count/n=" + (count / (double) n));
    }
  }

  /**
   * Find the 1-center of a set of points
   * @param C : array of FPoint2's
   * @param r : Random number generator, or null to choose a new one
   * @return EdDisc
   */
  public static EdDisc findCenter(DArray C, Random r) {
    C.permute(r);
    if (db) {
      count = 0;
      Streams.out.println("finding center of points:\n" + C.toString(true));
    }
    return extend(C, C.size(), new DArray(), null);
  }

  private static EdDisc extend(DArray C, int cSize, DArray G, EdDisc s) {
    if (db) {
      Streams.out.println("extend, pts=");
      for (int i = 0; i < cSize; i++)
        Streams.out.println(" " + C.getFPoint2(i));
      Streams.out.println("\n G=\n" + G.toString(true) + "\n s=" + s);
    }

    for (int i = 0; i < cSize; i++) {
      if (db)
        count++;
      FPoint2 pt = C.getFPoint2(i);
      if (s == null || !s.contains(pt)) {
        if (db)
          Streams.out.println(" disc doesn't contain " + pt);

        G.add(pt);
        s = solve(G);
        s.setFlags(G.size());
        s = extend(C, i, G, s);
        if (db)
          Streams.out.println(" extended to " + s);
        G.pop();
      }
    }
    if (db)
      Streams.out.println(" extend returning " + s);

    return s;
  }

  private static EdDisc solve(DArray C) {
    EdDisc r = null;
    switch (C.size()) {
    case 1:
      r = new EdDisc(C.getFPoint2(0), 0);
      break;
    case 2:
      {
        FPoint2 p0 = C.getFPoint2(0), p1 = C.getFPoint2(1);
        FPoint2 midPt = FPoint2.midPoint(p0, p1);
        double rad = FPoint2.distance(midPt, p0);
        r = new EdDisc(midPt, rad);
      }
      break;
    case 3:
      {
        FPoint2 p0 = C.getFPoint2(0), p1 = C.getFPoint2(1), p2 = C
            .getFPoint2(2);
        r = EdDisc.calcCircumCenter(p0, p1, p2);
      }
      break;
    default:
      throw new IllegalStateException("can't solve for:\n" + C);
    }
    if (db)
      Streams.out.println("solve " + C.toString(true) + " = " + r);
    return r;
  }
  public static boolean isCritical(FPoint2 origin, double radius, FPoint2 pt) {
    double diff = Math.abs(radius - FPoint2.distance(origin, pt));
    return diff < 1e-4;
  }

  public static boolean isCritical(EdDisc disc, FPoint2 pt) {
    return isCritical(disc.getOrigin(), disc.getRadius(), pt);
  }

}
