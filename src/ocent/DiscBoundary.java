package ocent;

import java.awt.*;
import testbed.*;
import base.*;

public class DiscBoundary implements Renderable {
  private static final boolean db = false;

  private static final double PI = Math.PI;

  private static final double RLIMIT = Math.PI * 4;

  public DiscBoundary(EdDisc disc) {
    this.disc = disc;
    ranges.add(new Range(-RLIMIT, RLIMIT));
  }

  private EdDisc disc;

  private DArray ranges = new DArray();

  private void exclude0(double a0, double a1) {

    if (db && T.update())
      T.msg("exclude0 a0=" + Tools.fa(a0) + " a1=" + Tools.fa(a1));
    for (int i = nIntervals() - 1; i >= 0; i--) {
      Range r = range(i);
      if (db && T.update())
        T.msg("removing from range " + r);
      Range r2 = r.remove(a0, a1);
      if (db && T.update())
        T.msg("returned r2=" + r2);
      if (r2 != null)
        ranges.add(i + 1, r2);
    }
    filterRanges();
  }

  public void exclude(EdDisc d) {
    final boolean db = false;

    if (db && T.update())
      T.msg("exclude disc from boundary" + T.show(d) + T.show(this));
    do {
      if (d.contains(disc)) {
        if (db && T.update())
          T.msg("excluding entire boundary");
        exclude(-RLIMIT, RLIMIT);
        break;
      }
      double r1 = disc.getRadius();
      double r2 = d.getRadius();

      final double EPS = 1e-6;
      double sep = disc.getOrigin().distance(d.getOrigin());
      if (Math.abs(sep - (r1 + r2)) < EPS || Math.abs(sep - (r1 - r2)) < EPS) {
        if (db && T.update())
          T.msg("excluding single tangency point from boundary");
        double th = MyMath.polarAngle(disc.getOrigin(), d.getOrigin());
        exclude(th, th);
        break;
      }
      if (sep > r1 + r2)
        break;

      DArray iang = EdDisc.circleIntersections(disc.getOrigin(), r1, d.getOrigin(),
          r2);

      if (db && T.update())
        T.msg("circle intersections:" + iang.toString());

      switch (iang.size()) {
      case 1:
        exclude(iang.getDouble(0), iang.getDouble(0));
        break;
      case 2:
        exclude(iang.getDouble(0), iang.getDouble(1));
        break;
      }
    } while (false);
    if (db && T.update())
      T.msg("after exclude disc from boundary" + T.show(d) + T.show(this)
          + "\n" + this + " valid=" + valid180(db));

  }

  public void exclude(double theta) {
    exclude(theta, theta);
  }

  public void exclude(double thetaStart, double thetaEnd) {
    if (db && T.update())
      T.msg("exclude a0=" + Tools.fa(thetaStart) + " a1=" + Tools.fa(thetaEnd));
    thetaStart = MyMath.normalizeAngle(thetaStart);
    thetaEnd = MyMath.normalizeAngle(thetaEnd);
    if (db && T.update())
      T.msg(" after normalizing, a0=" + Tools.fa(thetaStart) + " a1="
          + Tools.fa(thetaEnd));
    if (thetaEnd < thetaStart) {
      if (db && T.update())
        T.msg(" a1<a0");
      exclude0(thetaStart, RLIMIT);
      exclude0(-RLIMIT, thetaEnd);
    } else {
      if (db && T.update())
        T.msg(" a1 >= a0");
      exclude0(thetaStart, thetaEnd);
    }
  }

  private void filterRanges() {
    DArray nr = new DArray();
    //    Range rPrev = null;
    for (int i = 0; i < nIntervals(); i++) {
      Range r = range(i);
      if (r.isEmpty())
        continue;
      //      if (rPrev != null) {
      //        if (rPrev.r1 == r.r0) {
      //          rPrev.r1 = r.r1;
      //          continue;
      //        }
      //      }
      nr.add(r);
      //      rPrev = r;
    }
    ranges = nr;
  }

  public void render(Color c, int stroke,int markType) {
    final boolean db = false;

    if (c == null)
      c = MyColor.cDARKGREEN;
    V.pushColor(c);
    if (stroke >= 0)
      V.pushStroke(stroke);

    V.draw("" + nIntervals(), disc.getOrigin());
    for (int i = 0; i < nIntervals(); i++) {

      Range r = range(i);
      if (r.r0 > -RLIMIT)
        plotEdge(  r.r0);

      if (r.r1 < RLIMIT)
        plotEdge(  r.r1);
      double a0 = MyMath.clamp(r.r0, -PI, PI);
      double a1 = MyMath.clamp(r.r1, -PI, PI);
      if (db)
        Streams.out.println("plotting " + Tools.fa(a0) + "..." + Tools.fa(a1));

      double rAdj = 0; //vp.getScale() * 1.5;

      V.get2DGraphics().draw(disc.arc(a0, a1 - a0, rAdj));
    }
    if (stroke >= 0)
      V.popStroke();
   V.popColor();
  }

  private void plotEdge(  double a) {
    FPoint2 or = disc.getOrigin();
    final double LEN = 3.0;
    V.drawLine(MyMath.ptOnCircle(or, a, disc.getRadius()), MyMath.ptOnCircle(or,
        a, disc.getRadius() + LEN * V.getScale()));

  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DiscBoundary");
    for (int i = 0; i < nIntervals(); i++)
      sb.append("\n " + range(i));
    return sb.toString();
  }

  private Range range(int n) {
    return (Range) ranges.get(n);
  }

  private int nIntervals() {
    return ranges.size();
  }
  private static class Range {
    double r0;

    double r1;

    public Range(double r0, double r1) {
      this.r0 = r0;
      this.r1 = r1;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Range[");
      sb.append(Tools.fa(r0));
      sb.append("...");
      sb.append(Tools.fa(r1));
      sb.append("]");
      return sb.toString();
    }

    public Range remove(double a0, double a1) {
      if (db && T.update())
        T.msg("remove " + Tools.fa(a0) + "..." + Tools.fa(a1) + " from\n "
            + this);
      Range r = null;
      do {
        if (r0 >= a1)
          break;
        if (r1 <= a0)
          break;
        if (r0 < a0 && r1 > a1) {
          r = new Range(a1, r1);
          r1 = a0;
          break;
        }

        if (r0 >= a0)
          r0 = a1;
        if (r1 <= a1)
          r1 = a0;
      } while (false);

      if (db && T.update())
        T.msg(" range now\n " + this + "\n returning " + r);

      return r;
    }

    public boolean isEmpty() {
      return r0 >= r1;
    }

  }

  /**
   * Determine if disc boundary is valid for a minimum bounding
   * disc.  It must not contain a continuous arc of at least 180 degrees.
   * @param db
   * @return
   */
  public boolean valid180(boolean db) {

    if (db && T.update())
      T.msg("valid180" + T.show(this));
    boolean ret = true;

    for (int i = 0; i < nIntervals(); i++) {
      Range r = range(i);
      if (db && T.update())
        T.msg(" range #" + i + ": " + r);

      double min = r.r0;

      // if range includes crossover point, get true
      // minimum from last range
      if (min < -PI) {
        Range rLast = range(nIntervals() - 1);

        min = rLast.r0;
        if (min < -PI) {
          ret = false;
          break;
        }
        min -= PI*2;
        if (db && T.update())
          T.msg("  rLast=" + rLast);
      }
      if (db && T.update())
        T.msg(" min=" + Tools.fa(min));
      double max = r.r1;
      if (max > PI)
        continue;
      if (db && T.update())
        T.msg(" max=" + Tools.fa(max)+" max-min="+Tools.fa(max-min));
      if (max - min > PI) {
        ret = false;
        if (db && T.update())
          T.msg(" invalid, max-min=" + Tools.fa(max - min));
        break;
      }
    }
    return ret;
  }

}
