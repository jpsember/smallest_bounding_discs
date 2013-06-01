package ocent;

import java.awt.*;
import java.util.*;
import base.*;
import testbed.*;

public class DynamicOneCentOper implements TestBedOperation, Globals {
  /*! .enum  .private 2800
    time plotdiscs
  */

    private static final int TIME             = 2800;//!
    private static final int PLOTDISCS        = 2801;//!
/*!*/

  private static final int RANGE = 1000;

  private DynamicOneCentOper() {
  }
  public static DynamicOneCentOper singleton = new DynamicOneCentOper();

  public void addControls() {
    C.sOpenTab("D1C");
    {
      C.sStaticText("Dynamic 1-center of points along line segments");
      
      C.sIntSlider(TIME, "Time", null, 0, RANGE, 0, 1);
      C.sCheckBox(PLOTDISCS, "Plot discs", null, true);
    }

    C.sCloseTab();
  }

  public void runAlgorithm() {
    getSegs();
    calcSamples();
  }

  public void paintView() {
    Editor.render();
    if (samples != null) {
      for (int pass = 0; pass < 2; pass++) {
        if (pass == 0 && !C.vb(PLOTDISCS))
          continue;

        V.pushColor(pass == 0 ? MyColor.cLIGHTGRAY : MyColor.cDARKGREEN);
        V.pushStroke(pass == 0 ? STRK_THIN : STRK_NORMAL);
        Iterator it = samples.iterator();
        FPoint2 p0 = null;
        while (it.hasNext()) {
          Sample s = (Sample) it.next();
          EdDisc disc = s.disc;
          FPoint2 p1 = disc.getOrigin();
          if (pass == 0)
            V.drawCircle(p1, disc.getRadius());
          else {
            if (p0 != null)
              V.drawLine(p0, p1);
          }
          p0 = p1;
        }
        V.pop(2);
      }
    }

    if (currentSample != null) {
      Sample sm = currentSample;
      EdDisc disc = sm.disc;
      V.pushScale(.6);
      T.render(pts, MyColor.cDARKGREEN);
      V.pop();
      T.show(disc);
      T.show(disc.getOrigin(), MyColor.cRED);

      for (int i = 0; i < sm.crit.length; i++)
        T.show(pts[sm.crit[i]], MyColor.cRED);
    }

  }
  public void processAction(TBAction a) {
  }

  private void calcSamples() {
    t = C.vi(TIME) / (double) RANGE;

    pts = new FPoint2[segPts.length / 2];
    for (int i = 0; i < pts.length; i++) {
      FPoint2 pt = FPoint2.interpolate(segPts[i * 2 + 0], segPts[i * 2 + 1], t);
      pts[i] = pt;
    }

    DArray p2 = new DArray();
    for (int i = 0; i < pts.length; i++)
      p2.add(pts[i]);
    EdDisc disc = OneCenterOfPoints.findCenter(p2, null);

    DArray cp = new DArray();
//    FPoint2 origin = disc.getOrigin();
//    double rad = disc.getRadius();
    for (int i = 0; i < pts.length; i++) {
      if (OneCenter.isCritical(disc, pts[i]))
        cp.addInt(i);
//      double dist = pts[i].distance(origin);
//      double delta = Math.abs(rad - dist);
//      if (delta <= 1e-3)
//        cp.addInt(i);
    }

    Sample sm = new Sample(t, disc, cp.toIntArray());
    samples.add(sm);

    currentSample = sm;

  }

  private void getSegs() {
    StringBuilder sb = new StringBuilder();
    DArray segs = Editor.readObjects(EdSegment.FACTORY, false, true);
    FPoint2[] pts = new FPoint2[segs.size() * 2];
    int j = 0;
    for (int i = 0; i < segs.size(); i++) {
      EdSegment s = (EdSegment) segs.get(i);
      pts[j] = s.getPoint(0);
      pts[j + 1] = s.getPoint(1);
      sb.append(pts[j]);
      sb.append(pts[j + 1]);
      j += 2;
    }
    String nhash = sb.toString();
    if (!nhash.equals(hash)) {
      segPts = pts;
      currentSample = null;
      hash = nhash;
      samples = new TreeSet(new Comparator() {
        public int compare(Object arg0, Object arg1) {
          return (int) ((((Sample) arg0).t - ((Sample) arg1).t) * RANGE);
        }
      });
    }
  }
  private static class Sample {
    public Sample(double t, EdDisc d, int[] crit) {
      this.t = t;
      this.disc = d;
      this.crit = crit;
    }
    public EdDisc disc;
    public double t;
    public int[] crit;
  }

  private double t;
  private TreeSet samples;
  // private Map centers;
  private Sample currentSample;
  private Object hash;
  private FPoint2[] segPts;
  private FPoint2[] pts;
}
