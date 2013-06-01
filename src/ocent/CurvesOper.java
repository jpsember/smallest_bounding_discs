package ocent;

import java.awt.*;
import java.util.*;
import base.*;
import testbed.*;

public class CurvesOper implements TestBedOperation, Globals {
  /*! .enum  .private 3750
     theta maxiter sample showall showsubset multpass guess set3
     curvetype  set1 t1 set2 theta2 fpt interp dbvorn noclip clrsamp
     set3b lambda noclip3b  trails
  */

    private static final int THETA            = 3750;//!
    private static final int MAXITER          = 3751;//!
    private static final int SAMPLE           = 3752;//!
    private static final int SHOWALL          = 3753;//!
    private static final int SHOWSUBSET       = 3754;//!
    private static final int MULTPASS         = 3755;//!
    private static final int GUESS            = 3756;//!
    private static final int SET3             = 3757;//!
    private static final int CURVETYPE        = 3758;//!
    private static final int SET1             = 3759;//!
    private static final int T1               = 3760;//!
    private static final int SET2             = 3761;//!
    private static final int THETA2           = 3762;//!
    private static final int FPT              = 3763;//!
    private static final int INTERP           = 3764;//!
    private static final int DBVORN           = 3765;//!
    private static final int NOCLIP           = 3766;//!
    private static final int CLRSAMP          = 3767;//!
    private static final int SET3B            = 3768;//!
    private static final int LAMBDA           = 3769;//!
    private static final int NOCLIP3B         = 3770;//!
    private static final int TRAILS           = 3771;//!
/*!*/

  public void addControls() {
    C.sOpenTab("Curves");
    C
        .sStaticText("Generates Type I,II,III curves for possible 1-center problem, based on parameters");
    {
      C.sOpen();
      C.sButton(SAMPLE, "Resample", null);
      C.sNewColumn();
      C.sButton(CLRSAMP, "Reset", null);
      C.sClose();
    }
    C.sCheckBox(SHOWSUBSET, "subset",
        "Show subset diagram, to see type II curves", false);
    C.sCheckBox(FPT, "fpt vorn", "plot farthest-point Voronoi diagram", false);
    C.sCheckBox(DBVORN, "db vorn", null, false);
    C.sCheckBox(NOCLIP, "no clip", null, false);
    C.sCheckBox(TRAILS, "trails", null, false);

    C.sOpenTabSet(CURVETYPE);
    C.sOpenTab("I");
    {
      C.sTextField(SET1, "set:", "Set of discs (e.g. ab)", 2, true, "ab");
      C.sIntSlider(T1, "t:", null, -100, 100, 0, 1);
    }
    C.sCloseTab();
    C.sOpenTab("II");
    {
      C.sTextField(SET2, "set:", "Set of discs (e.g. ab)", 2, true, "ab");
      C.sIntSlider(THETA2, "Theta:", null, 0, 360, 0, 1);
      C.sCheckBox(INTERP, "interp", "plot interpolation disc", false);
    }
    C.sCloseTab();
    C.sOpenTab("III");
    {
      C
          .sStaticText("Calculates, using iterative method, the angle of b's point as a function of that of the angle of a's point");
      C.sTextField(SET3, "set:", "Set of discs (e.g. abc)", 3, true, "abc");
      C.sIntSlider(THETA, "Theta:", null, 0, 360, 0, 1);
      C.sIntSpinner(MAXITER, "Max iter:",
          "Maximum # iterations of Newton's method before aborting", 4, 50, 12,
          1);
      C.sCheckBox(SHOWALL, "samples",
          "Show all samples from Newton iterations", false);
      C.sCheckBox(MULTPASS, "multpass", null, false);
      C.sIntSpinner(GUESS, "Guess:",
          "Initial angle offset for first Newton iteration", 0, 360, 180, 30);
    }
    C.sCloseTab();
    C.sOpenTab("IIIb");
    {
      C
          .sStaticText("Calculates, using iterative method, the radius h of C as a function of its angle lambda around disc F");
      C.sTextField(SET3B, "set:", "Set of discs (e.g. abc)", 3, true, "abc");
      C.sIntSlider(LAMBDA, "Lambda:", null, 0, 360, 0, 1);
      C.sCheckBox(NOCLIP3B, "no clip", null, false);
    }
    C.sCloseTab();
    C.sCloseTabSet();

    C.sCloseTab();
  }

  public static CurvesOper singleton = new CurvesOper();

  private CurvesOper() {
  }

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      case SAMPLE:
        PossOneCentOper.singleton.resample();
        break;
      case CLRSAMP:
        PossOneCentOper.singleton.clearSamples();
        break;
      }
    }
  }

  public void runAlgorithm() {

    samples = null;
    show = new DArray();
    g = null;
    if (!C.vb(TRAILS))
      trails = null;
    else if (trails == null) {
      trails = new DArray();
      tmap = new HashMap();
    }

    if (C.vb(FPT))
      constructFV();

    try {
      switch (C.vi(CURVETYPE)) {
      case 0:
        doType1();
        break;
      case 1:
        doType2();
        break;
      case 2:
        doType3();
        break;
      case 3:
        doType3b();
        break;
      }
    } catch (FPError e) {
      //   Tools.warn("caught: " + e);
    }

  }

  private EdDisc[] getSet(String s, int minLen) {
    EdDisc[] ret = null;

    outer: do {
      if (s.length() < minLen)
        break;
      EdDisc[] r = new EdDisc[minLen];
      EdDisc[] ds = OCentMain.getDiscs();
      for (int i = 0; i < minLen; i++) {
        int c = Character.toUpperCase(s.charAt(i)) - 'A';
        if (c < 0 || c >= ds.length)
          break outer;
        for (int j = 0; j < i; j++)
          if (r[j] == ds[c])
            break outer;
        r[i] = ds[c];
      }
      ret = r;
    } while (false);
    return ret;
  }

  private void doType1() {
    EdDisc[] ds = getSet(C.vs(SET1), 2);
    if (ds == null)
      return;

    EdDisc da = ds[0], db = ds[1];

    double sep = FPoint2.distance(da.getOrigin(), db.getOrigin());
    Hyperbola h = new Hyperbola(da.getOrigin(), db.getOrigin(), .5 * (sep
        + da.getRadius() + db.getRadius()));

    double t = C.vi(T1) * .55;
    FPoint2 q = h.calcPoint(t);

    double rad = FPoint2.distance(q, da.getOrigin()) - da.getRadius();
    EdDisc qDisc = new EdDisc(q, rad);

    show.add(qDisc);
    if (trails != null) {
      if (tmap.put(qDisc.getOrigin(), Boolean.TRUE) == null)
        trails.add(qDisc);
    }

    show.add(new Mark(q, null, MARK_X, -1));

    show.add(new TangentPoint(qDisc, Math.PI
        + MyMath.polarAngle(da.getOrigin(), q), da.getLabel()));
    show.add(new TangentPoint(qDisc, Math.PI
        + MyMath.polarAngle(db.getOrigin(), q), db.getLabel()));

    show.add(new TangentPoint(qDisc, MyMath.polarAngle(da.getOrigin(), q), da
        .getLabel()
        + "~"));
    show.add(new TangentPoint(qDisc, MyMath.polarAngle(db.getOrigin(), q), db
        .getLabel()
        + "~"));
  }
  private void doType2() {
    EdDisc[] ds = getSet(C.vs(SET2), 2);
    if (ds == null)
      return;
    EdDisc da = ds[0], db = ds[1];

    double theta = MyMath.radians(C.vi(THETA2));

    FPoint2 pa = da.polarPoint(theta);
    FPoint2 pb = db.polarPoint(theta);
    FPoint2 q = FPoint2.midPoint(pa, pb);
    double rad = FPoint2.distance(q, pa);

    show.add(pa);
    show.add(pb);
    show.add(new EdDisc(q, rad));
    show.add(q);

    if (C.vb(INTERP)) {
      FPoint2 c = FPoint2.midPoint(da.getOrigin(), db.getOrigin());
      double r = (da.getRadius() + db.getRadius()) * .5;
      show.add(new EdDisc(c, r));
      double radius = 30.0;
      double th2 = theta + Math.PI / 2;
      show.add(new EdSegment(MyMath.ptOnCircle(q, th2, radius), MyMath
          .ptOnCircle(q, th2 + Math.PI, radius)));

      radius = radius * .3;

      show.add(new EdSegment(MyMath.ptOnCircle(pa, th2, radius), MyMath
          .ptOnCircle(pa, th2 + Math.PI, radius)));
      show.add(new EdSegment(MyMath.ptOnCircle(pb, th2, radius), MyMath
          .ptOnCircle(pb, th2 + Math.PI, radius)));

    }
  }

  private void doType3() {
    final boolean db = true;

    EdDisc[] ds = getSet(C.vs(SET3), 3);
    if (ds == null)
      return;

    EdDisc discF = ds[0];
    EdDisc discA = ds[1];
    EdDisc discB = ds[2];

    double a = discA.getOrigin().x;
    double b = discA.getOrigin().y;
    double c = discB.getOrigin().x;
    double d = discB.getOrigin().y;
    {
      double e = discF.getOrigin().x;
      double f = discF.getOrigin().y;

      a -= e;
      b -= f;
      c -= e;
      d -= f;
    }

    double r = discA.getRadius();
    double s = discB.getRadius();
    double t = discF.getRadius();

    double theta = MyMath.radians(C.vi(THETA));

    double aPrime = a + Math.cos(theta) * r;
    double bPrime = b + Math.sin(theta) * r;

    double phi = 0;

    for (int pass = 0; pass < 2; pass++) {
      DArray samp = new DArray();

      double off = MyMath.radians(C.vi(GUESS));
      if (!C.vb(MULTPASS)) {

        if (pass != 0)
          break;
        phi = theta + off;
      } else {
        phi = theta + off + (Math.PI * pass);
      }

      phi = MyMath.normalizeAngle(phi);

      if (db && T.update())
        T.msg("initial phi=" + Tools.fa2(phi));

      int iter = 0;
      int maxIter = C.vi(MAXITER);

      while (true) {
        if (iter++ == maxIter) {
          samp = null;
          break;
        }
        double fnPhi, fnPhiPrime;

        double cPrime = c + Math.cos(phi) * s;
        double dPrime = d + Math.sin(phi) * s;

        double x = .5 * (aPrime + cPrime);
        double y = .5 * (bPrime + dPrime);

        double u = MyMath.sq(x - aPrime) + MyMath.sq(y - bPrime);
        double v = MyMath.sq(x) + MyMath.sq(y);

        double sinP = Math.sin(phi);
        double cosP = Math.cos(phi);

        double dx = -.5 * s * sinP;
        double dy = .5 * s * cosP;
        double du = 2 * (x - aPrime) * dx + 2 * (y - bPrime) * dy;
        double dv = 2 * x * dx + 2 * y * dy;

        double uRoot = Math.sqrt(u);
        double vRoot = Math.sqrt(v);

        fnPhi = t + uRoot - vRoot;
        fnPhiPrime = du / (2 * uRoot) - dv / (2 * vRoot);

        samp.add(new Sample(discF, discA, discB, theta, phi));
        double phiNext = phi - fnPhi / fnPhiPrime;
        phiNext = MyMath.normalizeAngle(phiNext);

        if (db && T.update())
          T.msg("phi=" + Tools.fa(phi) + " f=" + Tools.f(fnPhi) + " f'="
              + Tools.f(fnPhiPrime) + " phi2=" + Tools.fa(phiNext));

        if (Math.abs(phiNext - phi) < 1e-3) {

          if (false) {
            System.out.println("da=" + discA.getOrigin() + " rad="
                + discA.getRadius());
            System.out.println("db=" + discB.getOrigin() + " rad="
                + discB.getRadius());
            System.out.println("df=" + discF.getOrigin() + " rad="
                + discF.getRadius());
            System.out.println("lambda="
                + MyMath.degrees(MyMath.polarAngle(new FPoint2(x, y))));
            System.out.println("p="
                + FPoint2.add(new FPoint2(x, y), discF.getOrigin(), null));
            System.out.println("rad="
                + (Math.sqrt(x * x + y * y) - discF.getRadius()));
          }
          break;
        }
        phi = phiNext;
      }

      if (samp == null)
        continue;

      if (samples == null) {
        samples = samp;
        continue;
      }

      Sample s2 = (Sample) samp.last();
      Sample s0 = (Sample) samples.last();

      if (s2.sameSide() && !s0.sameSide())
        samples = samp;
    }
  }

  private static class Sample implements Renderable {
    protected EdDisc discF, discA, discB;

    private static FPoint2 adj(FPoint2 pt, FPoint2 org) {
      return new FPoint2(pt.x - org.x,pt.y - org.y);
    }
    
    public String toString() {
      
      FPoint2 org = discF.getOrigin();
      
      StringBuilder sb = new StringBuilder();
      sb.append("A=" + adj(discA.getOrigin(),org) + " ra=" + Tools.f(discA.getRadius()));
      sb.append("\n");
      sb.append("B=" + adj(discB.getOrigin(),org) + " rb=" + Tools.f(discB.getRadius()));
      sb.append("\n");
      sb.append("F=" +   " rf=" + Tools.f(discF.getRadius()));
      sb.append("\n");
      sb.append("a=" + adj(diameterPt(true),org));
      sb.append("\n");
      sb.append("b=" + adj(diameterPt(false),org));
      sb.append("\n");
      return sb.toString();
    }

    private Sample(EdDisc discF, EdDisc discA, EdDisc discB) {
      this.discA = discA;
      this.discB = discB;
      this.discF = discF;
    }
    public Sample(EdDisc f, EdDisc a, EdDisc b, double lambda, double h,
        double phi) {
      this(f, a, b);
      setCDisc(MyMath.ptOnCircle(f.getOrigin(), lambda, f.getRadius() + h), h,
          phi);
    }

    public Sample(EdDisc discF, EdDisc discA, EdDisc discB, double thetaA,
        double thetaB) {
      this(discF, discA, discB);

      FPoint2 pt0 = discA.polarPoint(thetaA);
      FPoint2 pt1 = discB.polarPoint(thetaB);

      setCDisc(FPoint2.midPoint(pt0, pt1), FPoint2.distance(pt0, pt1) * .5,
          MyMath.polarAngle(pt0, pt1));
    }

    private void setCDisc(FPoint2 origin, double radius, double phi) {
      this.p = origin;
      this.radius = radius;
      this.phi = phi;
      this.lambda = MyMath.polarAngle(discF.getOrigin(), p);

    }
    private double lambda;
    public double lambda() {
      return lambda;
    }
    public boolean sameSide() {
      FPoint2 p0 = origin();
      FPoint2 p1 = diameterPt(false);
      boolean same = true;
      double side = -1;
      for (int k = 0; k < 3; k++) {
        EdDisc ds = k == 0 ? discF : (k == 1 ? discA : discB);

        double sl = MyMath.sign(MyMath.sideOfLine(p0, p1, ds.getOrigin()));
        if (k == 0) {
          side = sl;
        } else if (sl != side) {
          same = false;
          break;
        }
      }
      return same;
    }

    public FPoint2 diameterPt(boolean discA) {
      return MyMath.ptOnCircle(p, phi + (discA ? -Math.PI : 0), radius);
    }
    public FPoint2 origin() {
      return p;
    }
    public double phi() {
      return phi;
    }
    public double radius() {
      return radius;
    }
    public void render(Color c, int stroke, int markType) {
      V.pushColor(c);

      V.drawCircle(origin(), radius());
      if (markType >= 0) {
        V.mark(origin(), MARK_DISC, .5);
        V.drawLine(diameterPt(false), diameterPt(true));

        //        double cphi = MyMath.polarAngle(ptThetaA(), ptThetaB());

        for (int i = 0; i < 2; i++) {
          boolean da = (i == 0);
          FPoint2 loc = MyMath.ptOnCircle(origin(), da ? phi() - Math.PI
              : phi(), radius() + 3);
          V.draw("Q" + (1 + quadrant(da)), loc);
        }
        V.pushColor(MyColor.cLIGHTGRAY);
        V.drawLine(discF.getOrigin(), MyMath.ptOnCircle(discF.getOrigin(),
            lambda(), (radius() + discF.getRadius()) * 1.5));
        V.pop();
      }
      V.pop();
    }
    private int quadrant(boolean daa) {
      EdDisc da = (daa) ? discA : discB;
      double ang = MyMath.polarAngle(da.getOrigin(), MyMath.ptOnCircle(
          origin(), daa ? phi() - Math.PI : phi(), radius()));
      ang = MyMath.normalizeAnglePositive(ang + Math.PI / 2);
      return (int) Math.floor(ang / (Math.PI / 2));
    }

    private FPoint2 p;
    private double phi;
    private double radius;
  }

  public static EdDisc[] filterContaining(EdDisc[] ds) {
    final boolean db = false;

    DArray a = new DArray();

    outer: for (int i = 0; i < ds.length; i++) {
      for (int j = 0; j < ds.length; j++) {
        if (j == i)
          continue;
        if (ds[i].contains(ds[j])) {
          if (db)
            Streams.out.println(" disc " + ds[i].getLabel() + " contains "
                + ds[j].getLabel());
          continue outer;
        }
      }
      if (db)
        Streams.out.println(" adding " + ds[i].getLabel());

      a.add(ds[i]);
    }

    return (EdDisc[]) a.toArray(EdDisc.class);
  }

  private void constructFV() {
    EdDisc[] ds = filterContaining(OCentMain.getDiscs());

    Hyperbola[] vDiag = VornUtil.build(ds, 1, StandardDiscBisector.FARTHEST, 0 //
        | (C.vb(DBVORN) ? VornUtil.FLG_TRACE : 0) //
        | (C.vb(NOCLIP) ? VornUtil.FLG_NOCLIP : 0) //

    );
    g = new VornGraph(vDiag);
  }

  private void plotFarthestPointVDiag() {
    if (g != null) {
      g.setAppearance(null, -1, -1, Editor.withLabels(true));
      g.render(null, -1, -1);
    }
  }

  public void paintView() {
    PossOneCentOper.singleton.updateThread();
    PossOneCentOper.singleton.plotSamples();
    Editor.render();

    if (C.vb(SHOWSUBSET)) {
      Hyperbola[] hypList = VornUtil.buildSubset(OCentMain.getDiscs(), true,
          SubsetDiscBisector.S);

      VornGraph vg = new VornGraph(hypList);

      vg.plot(MyColor.get(MyColor.BROWN, .9), -1, false, false);

    }

    plotFarthestPointVDiag();

    if (samples != null) {
      Sample samp = null;
      for (int i = 0; i < samples.size(); i++) {
        samp = (Sample) samples.get(i);
        boolean last = (i + 1) == samples.size();

        if (!last && !C.vb(SHOWALL))
          continue;
        samp.render(last ? MyColor.cRED : MyColor.cDARKGRAY, -1, last ? MARK_X
            : -1);
      }
   //   Tools.warn("displaying sample"); System.out.println(samp);
      V.drawLine(samp.discA.getOrigin(), samp.diameterPt(true));
      V.drawLine(samp.discB.getOrigin(), samp.diameterPt(false));
    }
    T.showAll(trails, Color.gray, STRK_THIN, -1);
    T.showAll(show, MyColor.cRED, -1, -1);
  }

  private static class TangentPoint implements Renderable {
    public TangentPoint(EdDisc src, double theta, String label) {
      this.tangPt = src.polarPoint(theta);
      this.theta = theta;
      this.label = label;
    }
    private FPoint2 tangPt;
    private double theta;
    private String label;
    public void render(Color c, int stroke, int markType) {
      V.pushColor(c, MyColor.cRED);
      V.mark(tangPt);
      V.draw(label, MyMath.ptOnCircle(tangPt, theta, 2.0));
      V.popColor();
    }
  }
  private static class Mark implements Renderable {
    public Mark(FPoint2 loc, Color c, int markType, double scale) {
      this.loc = loc;
      this.color = c;
      this.markType = markType;
      this.scale = scale;
    }
    private FPoint2 loc;
    private Color color;
    private int markType;
    private double scale;

    public void render(Color c, int stroke, int markType) {
      if (color == null)
        color = MyColor.cRED;
      V.pushColor(c, color);
      if (this.markType < 0)
        this.markType = MARK_DISC;
      if (markType < 0)
        markType = this.markType;
      if (scale < 0)
        scale = 1.0;
      V.mark(loc, markType, scale);
      V.popColor();
    }
  }

  private void doType3b() {
    final boolean db = true;

    EdDisc[] ds = getSet(C.vs(SET3B), 3);
    if (ds == null)
      return;

    EdDisc discF = ds[0];
    EdDisc discA = ds[1];
    EdDisc discB = ds[2];

    if (MyMath.sideOfLine(discA.getOrigin(), discB.getOrigin(), discF
        .getOrigin()) < 0) {
      discA = discB;
      discB = ds[1];
    }

    double r = discA.getRadius();
    double s = discB.getRadius();
    double t = discF.getRadius();

    FPoint2 orA = discA.getOrigin();
    FPoint2 orB = discB.getOrigin();
    FPoint2 orF = discF.getOrigin();

    double a = orA.x;
    double b = orA.y;
    double c = orB.x;
    double d = orB.y;
    {
      double e = orF.x;
      double f = orF.y;
      a -= e;
      b -= f;
      c -= e;
      d -= f;
    }

    //  Tools.unimp("lambda need be only 180");
    double lambda = MyMath.radians(-1.0 * C.vi(LAMBDA));

    double cosLambda = Math.cos(lambda);
    double sinLambda = Math.sin(lambda);

    double h0 = 0;
    double phi0 = 0;

    // derive initial values from type II curve
    {
      FPoint2 l0 = orF;
      FPoint2 l1 = discF.polarPoint(lambda);
      FPoint2 origin = FPoint2.midPoint(orA, orB);
      double rad = (discA.getRadius() + discB.getRadius()) * .5;
      DArray ipts = lineCircleIntersection(l0, l1, origin, rad);
      if (db && T.update())
        T.msg("line circle intersection" + T.show(new EdDisc(origin, rad))
            + T.show(new EdSegment(l0, l1)) + T.show(ipts));

      FPoint2 best = null;

      for (int i = 0; i < ipts.size(); i++) {
        FPoint2 q = ipts.getFPoint2(i);
        if (best == null || q.distance(l0) > best.distance(l0))
          best = q;
      }
      if (best == null)
        return;

      h0 = best.distance(l0) - t;
      double theta = MyMath.polarAngle(origin, best);
      FPoint2 bPt = discB.polarPoint(theta);
      phi0 = MyMath.polarAngle(best, bPt);

      if (db && T.update())
        T.msg("initial h, phi chosen from type II disc" + T.show(best)
            + T.show(bPt) + " phi=" + Tools.fa(phi0));
    }

    for (int attempt = 0; attempt < 20; attempt++) {

      int limitH = 5;
      int limitPhi = 0;
      double h = h0;

      boolean type = (attempt % 2) == 1;
      double sign = (attempt / 2) % 2 == 0 ? 1 : -1;
      int count = (attempt / 4);

      if (!type) {
        h += count * 5 * sign;
      }

      double phi = phi0;
      if (type) {
        limitH = 3;
        phi += MyMath.radians(count * 3 * sign);
        limitPhi = 4;
      }

      int iter = 0;

      int maxIter = C.vi(MAXITER);

      DArray samp = new DArray();

      while (true) {
        if (iter++ == maxIter) {
          samp = null;
          break;
        }

        Sample sm = new Sample(discF, discA, discB, lambda, h, phi + Math.PI);
        //        sm.setPass(attempt);

        if (db && T.update())
          T.msg("latest sample" + T.show(sm, null, -1, MARK_X) + sm);
        samp.add(sm);

        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);

        double x = (h + t) * cosLambda;
        double y = (h + t) * sinLambda;

        double g00 = x + h * cosPhi - a;
        double g01 = y + h * sinPhi - b;
        double g10 = x - h * cosPhi - c;
        double g11 = y - h * sinPhi - d;

        // calculate jacobian

        double j00 = 2 * g00 * (cosLambda + cosPhi) + 2 * g01
            * (sinLambda + sinPhi);
        double j01 = 2 * g10 * (cosLambda - cosPhi) + 2 * g11
            * (sinLambda - sinPhi);

        double j10 = 2 * g00 * (-h * sinPhi) + 2 * g01 * (h * cosPhi);
        double j11 = 2 * g10 * (h * sinPhi) + 2 * g11 * (-h * cosPhi);

        Matrix j = new Matrix(2);
        j.set(0, 0, j00);
        j.set(0, 1, j01);
        j.set(1, 0, j10);
        j.set(1, 1, j11);

        Matrix ji = null;
        try {
          ji = j.invert(null);
        } catch (FPError e) {
          if (db && T.update())
            T.msg("singular matrix");
          samp = null;
          break;
        }
        //        if (db && T.update())
        //          T.msg("inverse of Jacobian:\n" + ji);

        double f0 = MyMath.sq(x + h * cosPhi - a)
            + MyMath.sq(y + h * sinPhi - b) - MyMath.sq(r);
        double f1 = MyMath.sq(x - h * cosPhi - c)
            + MyMath.sq(y - h * sinPhi - d) - MyMath.sq(s);

        // observe that we are using the transpose of the jacobian here!
        double h2 = h - ji.get(0, 0) * f0 - ji.get(1, 0) * f1;
        double phi2 = phi - ji.get(0, 1) * f0 - ji.get(1, 1) * f1;

        double h2Was = h2, phi2Was = phi2;

        // don't allow h to change by much
        if (limitH > 0) {
          h2 = MyMath.clamp(h2, Math.max(.5, h - limitH), h + limitH);
        }

        double phd = phi2 - phi;
        if (limitPhi > 0) {
          final double DELTAPHI = MyMath.radians(limitPhi);
          phd = MyMath.clamp(phd, -DELTAPHI, DELTAPHI);
        }
        phi2 = MyMath.normalizeAngle(phi + phd);

        //        
        //        phi2 = MyMath.normalizeAngle(phi2);
        //
        //        double phd = MyMath.normalizeAngle(phi2 - phi);
        //        if (limitPhi > 0) {
        //          final double DELTAPHI = MyMath.radians(limitPhi);
        //          phd = MyMath.clamp(phd, -DELTAPHI, DELTAPHI);
        //        }
        //        phi2 = MyMath.normalizeAngle(phi + phd);

        if (db && T.update())
          T.msg("clamped h from " + h2Was + " to " + h2 + ", phi="
              + Tools.fa(phi2Was) + " to " + Tools.fa(phi2));

        final double EPS = 1e-3;

        if (Math.abs(phi - phi2) < EPS && Math.abs(h - h2) < EPS)
          break;

        phi = phi2;
        h = h2;
      }

      // determine if we have found the correct point
      if (samp != null) {

        boolean found = false;

        do {
          if (C.vb(NOCLIP3B)) {
            found = true;
            break;
          }

          Sample sm = (Sample) samp.last();
          FPoint2 s0 = sm.diameterPt(true), s1 = sm.diameterPt(false);

          if (MyMath.sideOfLine(s0, s1, orF) < 0
              || MyMath.sideOfLine(s0, s1, orA) < 0
              || MyMath.sideOfLine(s0, s1, orB) < 0)
            break;

          found = true;
        } while (false);

        if (!found)
          samp = null;
      }
      if (samp != null) {
        samples = samp;
        break;
      }
    }
  }
  /**
   * Calculate the intersection points between a line and a circle
   * @param l0
   * @param l1 endpoints of line
   * @param origin 
   * @param rad origin, radius of circle
   * @return array of intersection points
   */
  private static DArray lineCircleIntersection(FPoint2 l0, FPoint2 l1,
      FPoint2 origin, double rad) {

    final boolean db = false;

    DArray ret = new DArray();

    if (db && T.update())
      T.msg("line/circle intersection" + T.show(new EdSegment(l0, l1))
          + T.show(new EdDisc(origin, rad)));

    double x1 = l0.x - origin.x;
    double y1 = l0.y - origin.y;
    double x2 = l1.x - origin.x;
    double y2 = l1.y - origin.y;

    double dx = x2 - x1;
    double dy = y2 - y1;

    double segLength = Math.sqrt(dx * dx + dy * dy);
    if (Math.abs(segLength) < 1e-3)
      throw new FPError("degenerate line");
    dx = dx / segLength;
    dy = dy / segLength;
    x2 = x1 + dx;
    y2 = y1 + dy;

    double D = x1 * y2 - x2 * y1;

    double discrim = rad * rad - D * D;

    if (db && T.update())
      T.msg("x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2=" + y2 + " rad="
          + rad);
    if (db && T.update())
      T.msg("len was " + segLength + ", discrim=" + discrim);

    if (Math.abs(discrim) < 1e-5)
      discrim = 0;

    do {
      if (discrim < 0)
        break;

      double q = Math.sqrt(discrim);
      double sgndy = Math.signum(dy);
      for (int pass = 0; pass < 2; pass++) {
        double sign = pass == 0 ? 1 : -1;

        double x = (D * dy + sign * sgndy * dx * q);
        double y = (-D * dx + sign * Math.abs(dy) * q);

        FPoint2 pt = new FPoint2(x + origin.x, y + origin.y);

        if (db && T.update())
          T.msg("adding point: " + T.show(pt));
        ret.add(pt);
        if (discrim == 0)
          break;
      }
    } while (false);

    return ret;
  }
  private DArray samples;
  private DArray show;
  private VornGraph g;
  private DArray trails;
  private Map tmap;

  public static double s(double a) {
    return a * a;
  }

  public static void main(String[] args) {
    double Ax = 98.572;
    double Ay = 42.956;
    double Ra = 24.573;
    double Bx = 17.860;
    double By = 53.252;
    double Rb = 19.344;
    double Fx = 52.618;
    double Fy = 14.601;
    double Rf = 4.774;
    double ax = 116.544;
    double ay = 59.715;
    double bx = 19.405;
    double by = 72.534;

    Ax -= Fx;
    Ay -= Fy;
    Bx -= Fx;
    By -= Fy;
    ax -= Fx;
    ay -= Fy;
    bx -= Fx;
    by -= Fy;
    Fx = 0;
    Fy = 0;

    //    double px = .5 * (ax + bx);
    //    double py = .5 * (ay + by);

    System.out.println("L:" + (s(ax - Ax) + s(ay - Ay)) + " R:" + s(Ra));
    System.out.println("L:" + (s(bx - Bx) + s(by - By)) + " R:" + s(Rb));
    System.out.println("L:" + //
        (s(.5 * (ax + bx) - Fx) + s(.5 * (ay + by) - Fy))//
        + " R:" + s(.5 * Math.sqrt(s(ax - bx) + s(ay - by)) + Rf));

    double left, right;
    {
      double u = s(Fx) + s(Fy) - s(Rf);

      left = .25 * s(ax + bx) - (ax + bx) * Fx + .25 * s(ay + by) - (ay + by)
          * Fy - .25 * s(ax - bx) - .25 * s(ay - by) + u;

      right = Rf * Math.sqrt(s(ax - bx) + s(ay - by));
      System.out.println("left=" + left + "\nrigt=" + right);
      System.out.println("ay=" + (Math.sqrt(s(Ra) - s(ax - Ax)) + Ay));

      left = -(ax + bx) * Fx - (ay + by) * Fy + u + ax * bx + ay * by;

      right = Rf * Math.sqrt(s(ax - bx) + s(ay - by));
      System.out.println("left=" + left + "\nrigt=" + right);
    }

    left = ax * bx + ay * by - s(Rf);
    right = Rf * Math.sqrt(s(ax - bx) + s(ay - by));
    System.out.println("left=" + left + "\nrigt=" + right);

    double u = ax, v = ay, s = bx, t = by, r = Rf;

    left = u * u * s * s //
        + v * v * t * t //
        + r * r * r * r //
        - 2 * u * v * s * t //
        + 3 * r * r * u * s//
        + 3 * r * r * v * t //
        - r * r * s * s //
        - r * r * t * t;
    System.out.println("left=" + left);

    left = u * s + v * t - r * r;
    right = r * Math.sqrt(s(u - s) + s(v - t));

    System.out.println("left=" + left + " right=" + right);

    left = u * u * s * s + v * v * t * t + r * r * r * r + 2 * u * v * s * t
        - 2 * r * r * u * s - 2 * r * r * v * t;
    right = r * r * (s(u - s) + s(v - t));

    System.out.println("left=" + left + " right=" + right);

    left = s(u) * s(s) + s(v) * s(t) + s(r * r) + 2 * u * v * s * t - s(r)
        * (s(u) + s(s) + s(v) + s(t));
    System.out.println("left=" + left);
  }
}
