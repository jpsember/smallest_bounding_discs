package ocent;

import java.awt.*;
import java.awt.geom.*;
import testbed.*;
import base.*;

/**
 * Calculates intersection of discs
 */
public class DiscIntersection implements Renderable {

  /**
   * Constructor for an initial disc
   * @param origin origin of disc
   * @param radius radius of disc
   */
  public DiscIntersection(FPoint2 origin, double radius, DArray critPtSet) {
    arcs.add(new Arc(origin, radius, -Math.PI, Math.PI, critPtSet));
  }
  /**
   * Constructor for an initial disc
   * @param initialDisc initial disc
   */
  public DiscIntersection(EdDisc initialDisc, DArray critPtSet) {
    this(initialDisc.getOrigin(), initialDisc.getRadius(), critPtSet);
  }

  public void render(Color c, int stroke, int markType) {
    final boolean SHOWSIZE = false;
    final boolean db = false;

    FRect r = null;
    if (!isEmpty()) {
      T.renderAll(arcs, c, stroke, markType);
      if (markType >= 0) {
        V.pushColor(c, MyColor.cRED);

        for (int i = 0; i < size(); i++) {

          Arc a = arc(i);
          Arc a2 = arc(i + 1);

          if (a.getOrigin().equals(a2.getOrigin()))
            continue;

          double t1 = a.t1();
          double t2 = a2.t0();
          if (t2 < t1) {
            t2 -= Math.PI * 2;
          }
          double tm = .5 * (t1 + t2);
          if (db)
            Streams.out.println("t1=" + Tools.fa(t1) + " t2=" + Tools.fa(t2)
                + " tm=" + Tools.fa(tm));

          FPoint2 p = a.pt(t1);
          r = FRect.add(r, p);
          V.drawLine(p, MyMath.ptOnCircle(p, tm, V.getScale() * .6));
        }
        if (SHOWSIZE) {
          if (r != null)
            V.draw("size=" + size(), r.x + r.width + 5, r.y);
        }
        V.pop();
      }
    }

  }
  /**
   * Get # arcs that form the intersection
   * @return # arcs, or zero if empty
   */
  public int size() {
    if (empty)
      return 0;
    return arcs.size();
  }

  /**
   * Determine if intersection of all the discs is empty
   * @return
   */
  public boolean isEmpty() {
    return empty;
  }

  /**
   * Get index of arc that contains a point (or is very close to it)
   * @param pt point
   * @return index of arc, or -1 if none found
   */
  public Arc findArcAt(FPoint2 pt) {

    Arc ret = null;
    final double NEARZERO = .3;

    double minDist = NEARZERO * 2;
    for (int i = 0; i < size(); i++) {
      Arc a = arc(i);
      double dist = a.distanceFrom(pt);
      if (dist < minDist) {
        minDist = dist;
        ret = a;
      }
    }

    if (ret != null) {
      ret = ret.getUserVersion();
    }

    return ret;
  }

  /**
   * Include a disc
   * @param origin origin of disc
   * @param radius radius of disc
   * @return true if intersection is now empty
   */
  public boolean include(FPoint2 origin, double radius, DArray critPtSet) {
    final boolean db = false;

    if (db && T.update())
      T.msg("include " + T.show(new EdDisc(origin, radius)));

    if (!empty) {
      DArray newArcs = new DArray();

      // first, clip old arcs to new disc

      for (int i = 0; i < arcs.size(); i++) {
        Arc a = arc(i);

        if (db && T.update())
          T.msg("clipping old arc " + T.show(a, MyColor.cRED) + a);

        // Calculate angle values where new disc (D) intersects this arc (A).
        // These are the possibilities:
        // 1. D contains A (add all of A)
        // 2. D, A disjoint (intersection is empty)
        // 3. A leaves D
        // 4. A enters D
        // 5. A leaves and re-enters D
        // 6. A enters and leaves D
        if (EdDisc.contains(origin, radius, a.getOrigin(), a.getRadius())) {
          if (db && T.update())
            T.msg("case 1: disc contains arc");
          // case 1
          newArcs.add(a);
          continue;
        }
        if (EdDisc.contains(a.getOrigin(), a.getRadius(), origin, radius)) {
          if (db && T.update())
            T.msg("case 1b: arc disc contains new disc");
          // case 1b
          continue;
        }

        DArray ia = EdDisc.circleIntersections(a.getOrigin(), a.getRadius(),
            origin, radius);
        if (ia.isEmpty()) {
          if (db && T.update())
            T.msg("case 2: disc and arc are disjoint");
          // case 2
          empty = true;
          break;
        }
        if (ia.size() != 2)
          throw new FPError("expected two intersections between\n" + origin
              + " r=" + Tools.f(radius) + ", " + a.getOrigin() + " r="
              + Tools.f(a.getRadius()));

        double i0 = MyMath.normalizeAngle(ia.getDouble(0));
        double i1 = MyMath.normalizeAngle(ia.getDouble(1));

        if (i0 > i1) {
          double tmp = i0;
          i0 = i1;
          i1 = tmp;
        }

        if (db && T.update())
          T.msg("intersect points" + T.show(a.pt(i0)) + T.show(a.pt(i1)));
        FPoint2 im = MyMath.ptOnCircle(a.getOrigin(), .5 * (i0 + i1), a
            .getRadius());
        if (db && T.update())
          T.msg("i0=" + Tools.fa(i0) + " i1=" + Tools.fa(i1) + " midpoint"
              + T.show(im));

        if (!EdDisc.contains(origin, radius, im)) {
          if (db && T.update())
            T.msg("case 5");
          double t = Math.min(a.t1(), i0);
          if (t > a.t0()) {
            Arc a1 = a.subArc(a.t0(), t);
            if (db && T.update())
              T.msg("adding a1 " + T.show(a1) + a1);
            newArcs.add(a1);
          }
          t = Math.max(i1, a.t0());
          if (t < a.t1()) {
            Arc a2 = a.subArc(Math.max(i1, a.t0()), a.t1());
            if (db && T.update())
              T.msg("case 5, and a2 " + T.show(a2) + a2);
            newArcs.add(a2);
          }
          // case 5
          continue;
        }

        if (i1 < a.t0() || i0 > a.t1()) {
          if (db && T.update())
            T.msg("case 2");
        } else if (i0 > a.t0()) {
          if (db && T.update())
            T.msg("case 4 or 6");
          Arc a2 = a.subArc(i0, Math.min(i1, a.t1()));
          if (db && T.update())
            T.msg("adding " + a2 + T.show(a2));
          newArcs.add(a2);
        } else {
          if (db && T.update())
            T.msg("case 3");

          Arc a2 = a.subArc(a.t0(), Math.min(i1, a.t1())); //i0, a.t1);
          if (db && T.update())
            T.msg("adding " + a2 + T.show(a2));
          newArcs.add(a2);
        }
      }

      // second, add arcs of new disc to fill in gaps in old ones

      // create a buffer to hold arcs inserted at front whose
      // angles are actually greater than the front ones, and should
      // thus be placed at the end instead
      DArray rearList = new DArray();

      if (!empty) {
        arcs.clear();
        if (db && T.update())
          T.msg("adding arcs of new disc to fill in gaps"
              + T.showAll(newArcs, Color.red));

        if (newArcs.isEmpty()) {

          Arc a2 = new Arc(origin, radius, -Math.PI, Math.PI, critPtSet);
          if (db && T.update())
            T.msg("adding complete new disc" + T.show(a2));
          arcs.add(a2);

        } else {

          Arc newLast = null;
          Arc last = (Arc) newArcs.last();
          FPoint2 prevPt = last.pt(last.t1());

          if (db && T.update())
            T.msg("initial prev pt" + T.show(prevPt) + "\narcs=\n"
                + newArcs.toString(true));

          for (int i = 0; i < newArcs.size(); i++) {
            Arc a = (Arc) newArcs.get(i);

            if (db && T.update())
              T.msg("pass 2, arc " + a + T.show(a));

            FPoint2 newPt = a.pt(a.t0());

            if (db && T.update())
              T.msg("comparing points" + T.show(newPt) + T.show(prevPt));

            final double EPS1 = 1e-6;
            final double EPS2 = 1e-2;

            double dist = FPoint2.distance(newPt, prevPt);
            if (dist > EPS1) {

              // if distance is still pretty small, rejoin them without
              // adding a small arc of the new disc

              if (dist < EPS2) {

                Arc a0 = (Arc) newArcs.getMod(i - 1);

                FPoint2 midpoint = FPoint2.midPoint(prevPt, newPt);
                a0.setT1(MyMath.normalizeAngle(MyMath.polarAngle(
                    a0.getOrigin(), midpoint)));
                a.setT0(MyMath.normalizeAngle(MyMath.polarAngle(a.getOrigin(),
                    midpoint)));

              } else {

                double t0 = MyMath.polarAngle(origin, prevPt);
                double t1 = MyMath.polarAngle(origin, newPt);
                if (t0 > t1) {
                  if (db && T.update())
                    T.msg("initializing newLast, t0=" + Tools.fa(t0) + " t1="
                        + Tools.fa(t1));
                  newLast = new Arc(origin, radius, t0, Math.PI, critPtSet);
                  t0 = -Math.PI;
                  if (db && T.update())
                    T.msg("initializing newLast" + T.show(newLast));
                }
                Arc a2 = new Arc(origin, radius, t0, t1, critPtSet);
                if (db && T.update())
                  T.msg("adding arc " + a2 + T.show(a2));
                if (a2.t1() > a.t0())
                  rearList.add(a2);
                else
                  arcs.add(a2);
              }
            }

            if (db && T.update())
              T.msg("adding arc " + a + T.show(a));

            arcs.add(a);
            prevPt = a.pt(a.t1());
          }
          if (newLast != null) {
            if (db && T.update())
              T.msg("adding newLast " + newLast + T.show(newLast));
            arcs.add(newLast);
          }
          arcs.addAll(rearList);

          if (db && T.update())
            T.msg("after pass 2:\n" + arcs.toString(true));
        }
      }
      if (!empty) {
        final int MAX = 100;
        if (arcs.size() > MAX) {
          Tools.warn("arcs.size exceeded; precision problem?");
          arcs.removeRange(MAX, arcs.size());
          if (false) {
            StringBuilder sb = new StringBuilder();
            sb.append("arcs[");
            for (int i = 0; i < arcs.size(); i++) {
              Arc a = (Arc) arcs.get(i);
              sb.append(Tools.fa(a.t0()) + " ... " + Tools.fa(a.t1()));
              sb.append("\n");
            }
            sb.append("]");
            System.out.println(sb);
          }
        }

      }
    }
    return empty;
  }

  private Arc arc(int n) {
    return (Arc) arcs.getMod(n);
  }

  /**
   * Include a disc
   * @param d disc to include
   * @return true if intersection is now empty
   */
  public boolean include(EdDisc d, DArray critPtSet) {
    return include(d.getOrigin(), d.getRadius(), critPtSet);
  }

  private DArray arcs = new DArray();
  private boolean empty;
}
