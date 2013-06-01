package ocent;

import java.awt.*;
import java.awt.geom.*;
import ocent.DiscIntersection.*;
import testbed.*;
import base.*;

public class Arc implements Renderable {

  public Arc getUserVersion() {
    Arc a = this;
    if (critSet == null) {
      a = new Arc(getOrigin(), getRadius(), t0(), t1(), critPts);
      a.critSet = CritPt.constructCriticalSet(critPts, getOrigin(),
          getRadius(), true);
    }
    return a;
  }

  public Arc(FPoint2 origin2, double radius2, double t0, double t1,
      DArray critPtSet) {
    this.setOrigin(new FPoint2(origin2));
    this.setRadius(radius2);
    this.setT0(MyMath.normalizeAngle(t0));
    this.setT1(MyMath.normalizeAngle(t1));
    if (t1 == Math.PI)
      this.setT1(Math.PI);
    this.critPts = critPtSet;
  }

  /**
   * Determine distance of point from arc
   * @param pt
   * @return
   */
  public double distanceFrom(FPoint2 pt) {

    double angle = MyMath.polarAngle(getOrigin(), pt);
    if (angle < t0() || angle > t1()) {
      angle = t0();
      if (pt(t0()).distance(pt) > pt(t1()).distance(pt))
        angle = t1();
    }
    return pt(angle).distance(pt);
  }

  public FPoint2 pt(double t) {
    return MyMath.ptOnCircle(getOrigin(), t, getRadius());
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Arc ");
    sb.append(getOrigin());
    sb.append(" r=" + Tools.f(getRadius()));
    sb.append(" t0=" + Tools.fa(t0()));
    sb.append(" t1=" + Tools.fa(t1()));
    return sb.toString();
  }
  public Arc subArc(double s0, double s1) {
    Arc a = new Arc(getOrigin(), getRadius(), s0, s1, critPts);
    return a;
  }
  public void render(Color c, int stroke, int markType) {

    Graphics2D g = V.get2DGraphics();
    V.pushColor(c, MyColor.cRED);
    V.pushStroke(stroke);

    if (critSet != null) {
      V.pushStroke(V.STRK_RUBBERBAND);
      V.drawCircle(getOrigin(), getRadius());
      CritPt.plotCriticalSet(critSet, getOrigin(), getRadius());
      V.pop();

      if (arcPt != null) {
        V.pushColor(MyColor.cLIGHTGRAY);
        for (int i = 0; i < 2; i++) {
          FPoint2 cp = critSet.getFPoint2(i);
          double th = MyMath.polarAngle(arcPt, cp);

          V.drawLine(arcPt, MyMath.ptOnCircle(cp, th, 20));
        }
        V.pop();
      }

    } else {
      g.draw(new Arc2D.Double(getOrigin().x - getRadius(), //
          getOrigin().y - getRadius(), //
          getRadius() * 2, getRadius() * 2, //
          -MyMath.degrees(t0()), //
          -MyMath.degrees(t1() - t0()), Arc2D.OPEN));
    }
    V.pop(2);
  }

  public void setShowTriangle(FPoint2 arcPt) {
    this.arcPt = null;
    if (arcPt != null && critSet.size() == 2) {
      arcPt = MyMath.ptOnCircle(origin, MyMath.polarAngle(origin, arcPt),
          radius);
      this.arcPt = arcPt;
    }
  }
  private FPoint2 arcPt;

  private void setOrigin(FPoint2 origin) {
    this.origin = origin;
  }

  public FPoint2 getOrigin() {
    return origin;
  }

  private void setRadius(double radius) {
    this.radius = radius;
  }

  public double getRadius() {
    return radius;
  }

  public void setT0(double t0) {
    this.t0 = t0;
  }

  public double t0() {
    return t0;
  }

  public void setT1(double t1) {
    this.t1 = t1;
  }

  public double t1() {
    return t1;
  }

  private DArray critSet;

  private FPoint2 origin;
  private double radius;
  private double t0;
  private double t1;
  private DArray critPts;

}
