package ocent;

import java.util.*;
import base.*;
import testbed.*;

public class OneCenterOfDiscs {
  private static final boolean db = false;
  private static int count;


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
        Streams.out.println(" " + C.get(i));
      Streams.out.println("\n G=\n" + G.toString(true) + "\n s=" + s);
    }

    for (int i = 0; i < cSize; i++) {
      if (db)
        count++;
      
      EdDisc pt = (EdDisc) C.get(i);
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
      r = new EdDisc((EdDisc)C.get (0) );
      break;
    case 2:
      {
        EdDisc  p0 = (EdDisc) C.get (0), p1 = (EdDisc) C.get (1);
        r = DiscUtil.smallestBoundingDisc(p0,p1);
//        
//        FPoint2 midPt = FPoint2.midPoint(p0, p1);
//        double rad = FPoint2.distance(midPt, p0);
//        r = new EdDisc(midPt, rad);
      }
      break;
    case 3:
      {
        EdDisc  p0 = (EdDisc) C.get (0), p1 = (EdDisc) C.get (1), p2 = (EdDisc) C.get (2);
        r =  DiscUtil.smallestBoundingDisc(p0,p1,p2);
//      
      }
      break;
    default:
      throw new IllegalStateException("can't solve for:\n" + C);
    }
    if (db)
      Streams.out.println("solve " + C.toString(true) + " = " + r);
    return r;
  }

}
