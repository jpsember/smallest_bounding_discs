package ocent;

import java.awt.*;
import java.util.*;
import ocent.GuarOneCenterOper.*;
import testbed.*;
import base.*;

public class CritPt extends FPoint2 {
  public static final Comparator COMPARATOR = new Comparator() {

    public int compare(Object arg0, Object arg1) {
      CritPt c1 = (CritPt) arg0;
      CritPt c2 = (CritPt) arg1;
      return MyMath.sign(c1.angle - c2.angle);
    }
  };
  final double NEARZERO = 1e-4;
  public CritPt(FPoint2 loc, FPoint2 mbOrigin, double mbRadius) {
    super(loc);
    double r = loc.distance(mbOrigin);
    if (r + NEARZERO >= mbRadius) {
      isCritical = true;
      angle = MyMath.polarAngle(mbOrigin, loc);
      //        this.loc = loc;
    }
  }
  //    private FPoint2 loc;
  private boolean isCritical;
  private double angle;
  public boolean isCritical() {
    return isCritical;
  }
  public double angle() {
    if (!isCritical)
      throw new IllegalStateException();
    return angle;
  }
  //    public FPoint2 loc() {
  //      if (!isCritical)
  //        throw new IllegalStateException();
  //      return loc;
  //    }

  /**
   * Construct a set of CritPts from a set of FPoint2s
   * @param points array of FPoint2s
   * @param mbOrigin origin of min bounding disc
   * @param mbRadius radius of min bounding disc
   * @param filterNonCrit if true, filters non critical set
   */
  public static DArray constructCriticalSet(DArray points, FPoint2 mbOrigin,
      double mbRadius, boolean filterNonCrit) {

    DArray ps = new DArray();
    for (int i = 0; i < points.size(); i++) {
      CritPt c = new CritPt(points.getFPoint2(i), mbOrigin, mbRadius);
      if (!filterNonCrit || c.isCritical())
        ps.add(c);
    }

    if (filterNonCrit) {
      // sort by angle
      ps.sort(CritPt.COMPARATOR);

      outer: do {
        // find diameter
        for (int i = 0; i < ps.size(); i++) {
          CritPt pi = (CritPt) ps.get(i);
          for (int j = i + 1; j < ps.size(); j++) {
            CritPt pj = (CritPt) ps.get(j);
            if (Math.abs(pj.angle() - pi.angle() - Math.PI) < .02) {
              ps.clear();
              ps.add(pi);
              ps.add(pj);
              break outer;
            }
          }
        }

        for (int i = 0; i < ps.size(); i++) {
          CritPt prev = (CritPt) ps.getMod(i - 1);
          //  CritPt p = (CritPt)ps.get(i);
          CritPt next = (CritPt) ps.getMod(i + 1);
          double diff = MyMath.normalizeAnglePositive(next.angle()
              - prev.angle());
          if (diff < Math.PI) {
            ps.remove(i);
            i--;
            continue;
          }
        }
        //    // filter out points not on disc boundary
        //    for (int i = 0; i < points.size(); i++) {
        //      FPoint2 p = points.getFPoint2(i);
        //      double r = p.distance(mbOrigin);
        //      if (r + NEARZERO < mbRadius)
        //        continue;
        //      ps.add(p);
        //    }
        //
        // 
      } while (false);
    }
    return ps;

  }

  private static Color colors[] = {
    MyColor.cPURPLE,
    new Color(.1f,.1f,.8f),
  };
  
  public static void plotCriticalSet(DArray points, FPoint2 mbOrigin,
      double mbRadius) {

    // if array is not an array of CritPts, construct it
    if (points.size() > 0 && !(points.get(0) instanceof CritPt))
      points = constructCriticalSet(points, mbOrigin, mbRadius, true);

    V.pushColor(points.size() == 2 ? colors[0] : colors[1]);
    V.pushStroke(points.size() == 2 ? V.STRK_NORMAL : V.STRK_RUBBERBAND);
    for (int i = 0; i < points.size(); i++) {
      CritPt cp = (CritPt) points.get(i);
      V.pushColor(MyColor.cRED);
      V.mark(cp, V.MARK_DISC, 1.0);
      V.pop();

      V.drawLine(cp, mbOrigin);
    }
    V.pop(2);

  }

}
