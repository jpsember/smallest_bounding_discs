package ocent;

import java.util.*;
import base.*;
import testbed.*;

public class GeneratorOper implements TestBedOperation, Globals {
  /*! .enum  .private 1600
  seed count minrad maxrad _ _ position _  
   type rndsqr circle spiral  rnddisc shadows square segs
  */

    private static final int SEED             = 1600;//!
    private static final int COUNT            = 1601;//!
    private static final int MINRAD           = 1602;//!
    private static final int MAXRAD           = 1603;//!
    private static final int POSITION         = 1606;//!
    private static final int TYPE             = 1608;//!
    private static final int RNDSQR           = 1609;//!
    private static final int CIRCLE           = 1610;//!
    private static final int SPIRAL           = 1611;//!
    private static final int RNDDISC          = 1612;//!
    private static final int SHADOWS          = 1613;//!
    private static final int SQUARE           = 1614;//!
    private static final int SEGS             = 1615;//!
/*!*/

  public void addControls() {
    C.sOpenTab("Gen");
    C.sStaticText("Generates disc sites");
    C.sCheckBox(SQUARE, "Squares", "True to set width & height equal", false);
    {
      C.sOpenComboBox(TYPE, "Pattern", "Select pattern of random discs", false);
      C.sChoice(RNDDISC, "random (disc)");
      C.sChoice(RNDSQR, "random (square)");
      C.sChoice(CIRCLE, "circle");
      C.sChoice(SPIRAL, "spiral");
      C.sChoice(SHADOWS, "bitangents");
      C.sChoice(SEGS, "segments");
      C.sCloseComboBox();
    }
    {
      C.sOpen();
      {
        C.sOpen();
        C.sIntSlider(SEED, "Seed",
            "Random number generator seed (zero for unseeded)", 0, 100, 0, 1);
        C.sNewColumn();
        C.sIntSpinner(COUNT, "Count", "Number to generate", 0, 100, 12, 1);
        C.sClose();
      }
      {
        C.sOpen();
        C.sIntSlider(MINRAD, "Min Radius", "Minimum radius", 0, 30, 0, 1);
        C.sNewColumn();
        C.sIntSlider(MAXRAD, "Max Radius", "Maximum radius", 0, 250, 50, 1);
        C.sClose();
      }
      {
        C.sOpen();
        C.sIntSlider(POSITION, "Position", "Distance from center (circular)",
            0, 200, 30, 1);
        C.sNewColumn();
        C.sClose();
      }

      C.sClose();
    }
    C.sCloseTab();
  }

  public static void generateRandom() {
    genRand(C.vi(TYPE));
  }
  private static void genRand(int type) {
    {
      DArray items = new DArray();

      int seed = C.vi(SEED);
      Random r = seed == 0 ? new Random() : new Random(seed);
      int c = C.vi(COUNT);

      int radMin = Math.min(C.vi(MINRAD), C.vi(MAXRAD));
      int radMax = Math.max(C.vi(MINRAD), C.vi(MAXRAD));
      double range = (radMax - radMin) * .1;
      final int PADDING = 3;
      FPoint2 size = V.logicalSize();
      double sx = size.x - 2 * PADDING;
      double sy = size.y - 2 * PADDING;

      switch (type) {
      default:
        {
          for (int i = 0; i < c; i++) {
            double rv = r.nextDouble();
            double rad = rv * rv * range + radMin;
            EdDisc e = new EdDisc(//
                new FPoint2(//
                    r.nextDouble() * sx + PADDING, //
                    r.nextDouble() * sy + PADDING //
                ), rad);
            items.add(e);
          }
        }
        break;
      case SHADOWS:
        {
          double fx = 50;
          double fy = 35;

          double szMax = C.vi(POSITION);

          double xl = fx - szMax;
          double xr = fx + szMax;
          double basey = fy - szMax;

          // add left, right base discs
          double rad = 5;
          items.add(new EdDisc(MyMath.ptOnCircle(new FPoint2(xl, basey),
              Math.PI * .75, rad), rad));
          items.add(new EdDisc(MyMath.ptOnCircle(new FPoint2(xr, basey),
              Math.PI * .25, rad), rad));

          double xMax = fx + szMax * 1.2;
          double xMin = fx + szMax * .1;

          double p = .95;

          for (int i = 0; i < c; i++) {
            double x = xMin + i * ((xMax - xMin) / c);
            double fnx = Math.pow((x - fx), p) + fy;

            // calculate gradient along x
            double grad = p * Math.pow((x - fx), p - 1);

            double t = (fx - x) / -grad;
            double y = fnx + t;
            FPoint2 onCurve = new FPoint2(x, fnx);

            FPoint2 origin = new FPoint2(fx, y);
            items.add(new EdDisc(origin, onCurve.distance(origin)));
          }
        }
        break;
      case SEGS:
      case RNDDISC:
        {
          double szMax = C.vi(POSITION) * (80.0 / 30.0) / 100.0;

          for (int i = 0; i < c; i++) {
            double rv = r.nextDouble();
            double rad = rv * rv * range + radMin;
            double theta = r.nextDouble() * Math.PI * 2;
            double rd = Math.sqrt(r.nextDouble()) * szMax * .5;

            EdDisc e = new EdDisc(//
                new FPoint2(size.x / 2 + Math.cos(theta) * sx * rd, size.y / 2
                    + Math.sin(theta) * sy * rd), rad);
            items.add(e);
          }
        }
        break;
      case CIRCLE:
        {
          for (int i = 0; i < c; i++) {
            FPoint2 cn = MyMath.ptOnCircle(new FPoint2(50, 50), MyMath
                .radians(i * 360.0 / c), C.vi(POSITION));
            EdDisc e = new EdDisc(cn, C.vi(MINRAD));
            items.add(e);
          }
        }
        break;
      case SPIRAL:
        {
          double s2 = c * .4;

          int r0 = C.vi(MINRAD);
          int r1 = Math.max(r0, C.vi(MAXRAD));
          if (r1 == r0)
            r1++;

          for (int i = 0; i < c; i++) {
            double scl = (i + c * .3) / (double) (c - 1);

            FPoint2 cn = MyMath.ptOnCircle(new FPoint2(50, 50), MyMath
                .radians(i * 360.0 / s2), C.vi(POSITION) * scl);
            EdDisc e = new EdDisc(cn, .05 * r.nextInt(r1 - r0) + r0);
            items.add(e);
          }
        }
        break;
      }
      if (type == SEGS) {
        for (int i = 0; i < items.size(); i++) {
          EdDisc d = (EdDisc) items.get(i);
          double ra = d.getRadius();
          FPoint2 or = d.getOrigin();
          double the = r.nextDouble() * Math.PI;
          EdSegment s = new EdSegment(MyMath.ptOnCircle(or, the, ra), MyMath
              .ptOnCircle(or, the + Math.PI, ra));
          items.set(i, s);
        }
      } else {
        int rt = OCentMain.regionType();
        switch (rt) {
        case OCentMain.RECTS:
        case OCentMain.SQUARES:
          for (int i = 0; i < items.size(); i++) {
            EdDisc d = (EdDisc) items.get(i);
            FRect b = d.getBounds();

            if (OCentMain.regionType() == OCentMain.RECTS) {
              b.width = MyMath.rnd(range) + radMin;
              b.height = MyMath.rnd(range) + radMin;
            }

            EdRect rc = new EdRect(b);

            items.set(i, rc);
          }
          break;
        case OCentMain.POLYGONS:
          {
            int nPts = 6;

            for (int i = 0; i < items.size(); i++) {
              EdDisc d = (EdDisc) items.get(i);

              DArray pts = new DArray();
              for (int j = 0; j < nPts; j++)
                pts.add(MyMath.rndPtInDisc(d.getOrigin(), d.getRadius(), null));

              DArray hp = MyMath.convexHull(pts);
              EdPolygon p = new EdPolygon();
              for (int j = 0; j < hp.size(); j++)
                p.addPoint(pts.getFPoint2(hp.getInt(j)));

              items.set(i, p);
            }
          }
          break;
        }
      }
      Editor.replaceAllObjects(items);
    }

  }
  //  private int genType = RANDOM;
  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      default:
        if (a.ctrlId / 100 == SEED / 100)
          genRand(C.vi(TYPE));
        break;
      }
    }
  }
  public void runAlgorithm() {
  }
  public void paintView() {
    Editor.render();
  }

}
