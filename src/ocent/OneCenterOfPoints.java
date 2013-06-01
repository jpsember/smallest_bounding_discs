package ocent;

import java.util.*;
import base.*;
import testbed.*;

public class OneCenterOfPoints {
  private static final boolean db = false;
  private static int count;

  public static void main(String[] args) {
    DArray pts = new DArray();
    Random r = new Random(1969);
    int n = 5;
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
    double[] critPts = new double[3 * 2];
    double[] cand = new double[C.size() * 2];
    for (int i = 0; i < C.size(); i++) {
      FPoint2 pt = C.getFPoint2(i);
      cand[i * 2] = pt.x;
      cand[i * 2 + 1] = pt.y;
    }
    double[] s = new double[3];
    s[2] = -1;
    int crit = extend(cand, C.size(), critPts, 0, s);
    EdDisc ret = new EdDisc(s[0], s[1], s[2]);
    ret.setFlags(crit);
    return ret;
  }

  /**
   * Extend solution to cover more candidates
   * @param newPoints
   * @param numNewPoints
   * @param criticalPoints
   * @param numCritPoints
   * @param solutionDisc : current disc; if radius < 0, assumes null
   * @return
   */
  private static int extend(double[] newPoints, int numNewPoints,
      double[] criticalPoints, int numCritPoints, double[] solutionDisc) {

    int ret = numCritPoints;

    if (db) {
      Streams.out.println("extend, pts="
          + DArray.toString(newPoints, 0, numNewPoints));
      Streams.out.println("\n G=\n"
          + DArray.toString(criticalPoints, 0, numCritPoints * 2) + "\n s=" + solutionDisc);
    }

    for (int i = 0; i < numNewPoints; i++) {
      if (db)
        count++;
      double ptx = newPoints[i * 2];
      double pty = newPoints[i * 2 + 1];
      if (solutionDisc[2] < 0 || !EdDisc.contains(solutionDisc[0], solutionDisc[1], solutionDisc[2], ptx, pty)) {
        if (db)
          Streams.out.println(" disc doesn't contain " + ptx + "," + pty);
        criticalPoints[numCritPoints * 2] = ptx;
        criticalPoints[numCritPoints * 2 + 1] = pty;
        numCritPoints++;

        ret = numCritPoints;
        solve(criticalPoints, numCritPoints, solutionDisc);
        //        s.setFlags(nCrit);
        ret = extend(newPoints, i, criticalPoints, numCritPoints, solutionDisc);
        if (db)
          Streams.out.println(" extended to " + solutionDisc);
        //        if (db && !s.contains(ptx, pty))
        //          T.msg("extended disc still doesn't contain point:\n" + s + "\n" + ptx
        //              + "," + pty);
        //          Streams.out.println("*** extended disc still doesn't contain point!");
        numCritPoints--;
      }
    }
    if (db) 
      Streams.out.println(" # crit = "+numCritPoints);
          
    return ret;
  }

  private static void solve(double[] C, int size, double[] s) {
    switch (size) {
    case 1:
      s[0] = C[0];
      s[1] = C[1];
      s[2] = 0;
      break;
    case 2:
      {
        double x0 = C[0], y0 = C[1], x1 = C[2], y1 = C[3];
        double mx = (x0 + x1) * .5;
        double my = (y0 + y1) * .5;
        double rad = FPoint2.distance(x0, y0, mx, my);
        s[0] = mx;
        s[1] = my;
        s[2] = rad;
      }
      break;
    case 3:
      EdDisc.calcCircumCenter(C[0], C[1], C[2], C[3], C[4], C[5], s);
      break;
    default:
      throw new IllegalStateException("can't solve for:\n"
          + DArray.toString(C, 0, size * 2));
    }
    if (db)
      Streams.out.println("solve " + DArray.toString(C, 0, size * 2) + " = "
          + s);
  }

}
