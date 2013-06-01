package ocent;

import base.*;
import testbed.*;
import java.awt.*;
import java.util.*;

public class OCentMain extends TestBed {
  /*! .enum  .private   4000
     bedges  _ _ _ togglediscs makediam makesupported maketangent
     samprange sampres minbound   random rndtest type disc square rect poly
  */

  private static final int BEDGES = 4000;//!
  private static final int TOGGLEDISCS = 4004;//!
  private static final int MAKEDIAM = 4005;//!
  private static final int MAKESUPPORTED = 4006;//!
  private static final int MAKETANGENT = 4007;//!
  private static final int SAMPRANGE = 4008;//!
  private static final int SAMPRES = 4009;//!
  private static final int MINBOUND = 4010;//!
  private static final int RANDOM = 4011;//!
  private static final int RNDTEST = 4012;//!
  private static final int TYPE = 4013;//!
  private static final int DISC = 4014;//!
  private static final int SQUARE = 4015;//!
  private static final int RECT = 4016;//!
  private static final int POLY = 4017;//!
  /* !*/
   static boolean FULL = false;

  public static final int DISCS = 0, SQUARES = 1, RECTS = 2, POLYGONS = 3;

  //  private static double sq(double t) {
  //    return t * t;
  //  }

  public static void main(String[] args) {

    new OCentMain().doMainGUI(args);
  }

  // -------------------------------------------------------
  // TestBed overrides
  // -------------------------------------------------------

  public void addOperations() {
    addOper(PossOneCentOper.singleton);
    addOper(PossSmallestBoundingDiscOper.singleton);
    addOper(GuarOneCenterOper.singleton);
    addOper(new GeneratorOper());

    if (FULL) {
      addOper(CurvesOper.singleton);
      addOper(DynamicOneCentOper.singleton);
      addOper(OneCenterOper.singleton);
      addOper(DiscVornOper.singleton);
    }
  }
  public static int regionType() {
    return C.vi(TYPE) - DISC;
  }
  public void addControls() {
    C.sOpen();
    // C.sCheckBox(RECTS, "Rectangles", null, false);

    {
      C.sOpen();
      C.sButton(RANDOM, "Random", "Generate random discs");

      if (FULL) {
        C.sCheckBox(MINBOUND, "m.b.disc",
            "plot minimum bounding disc of discs", false);
        C.sCheckBox(BEDGES, "Bisector edges", "Plots edges of 1-center region"
            + " derived from bisector edges", false);
        C.sCheckBox(RNDTEST, "Test", "Repeatedly generate random discs", false);
        C.sNewColumn();
      }

      C.sOpenComboBox(TYPE, "Regions", "Select uncertain regions", false);
      C.sChoice(DISC, "disc");
      C.sChoice(SQUARE, "square");
      C.sChoice(RECT, "rectangle");
      C.sChoice(POLY, "polygon");
      C.sCloseComboBox();
      if (FULL) {
        C.sIntSpinner(SAMPRANGE, "range", null, 5, 300, 100, 5);
        C.sIntSpinner(SAMPRES, "res", null, 1, 100, 10, 1);
      }
      //   C.sIntSpinner(DBSAMP, "db", null, 0, 50, 0, 1);
      C.sClose();
    }

    C.sClose();
  }
  public void initEditor() {
    Editor.addObjectType(EdPolygon.FACTORY);
    Editor.addObjectType(EdDisc.FACTORY);
    Editor.addObjectType(EdSegment.FACTORY);
    Editor.addObjectType(EdDiameter.FACTORY);
    Editor.addObjectType(EdPoint.FACTORY);
    Editor.addObjectType(EdRect.FACTORY);

    Editor.openMenu();
    C.sMenuItem(TOGGLEDISCS, "Toggle discs/points", "!^t");
    C.sMenuItem(MAKETANGENT, "Set disc tangent", "!^3"); //"!^g");
    C.sMenuItem(MAKESUPPORTED, "Set disc supported", "!^4"); //"!^u");
    C.sMenuItem(MAKEDIAM, "Convert seg->diameter", null);
    Editor.closeMenu();
  }

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      case RANDOM:
        GeneratorOper.generateRandom();
        break;
      case RNDTEST:
        if (C.vb(RNDTEST))
          GeneratorOper.generateRandom();
        break;

      case TOGGLEDISCS:
        {
          for (int i = 0; i < discs2.length; i++) {
            EdDisc c = discs2[i];
            if (!c.isSelected())
              continue;
            c.togglePointMode();
          }
        }
        break;
      case MAKESUPPORTED:
        makeSupported();
        break;
      case MAKETANGENT:
        makeTangent();
        break;
      case MAKEDIAM:
        makeDiam();
        break;
      }
    }
  }

  /**
   * Make highlighted inactive discs supported by best-fit pair of candidates
   */
  private void makeSupported() {

    DArray a = Editor.editObjects(EdDisc.FACTORY, true, false);
    for (int k = 0; k < a.size(); k++) {
      EdDisc c = (EdDisc) a.get(k);

      {
        if (c.isActive())
          continue;

        // find best two candidates for supporting this disc
        DArray can = getCandidate(c);
        if (can.size() < 2)
          continue;

        EdDisc ca = (EdDisc) can.get(0);
        EdDisc cb = (EdDisc) can.get(1);
        boolean ia = DiscUtil.itan(c, ca);

        c.setPoint(0,
            DiscUtil.supportingHyperbola(c, ca, cb).snap(c.getOrigin()));

        c.setRadius(FPoint2.distance(c.getOrigin(), ca.getOrigin())
            + (ia ? ca.getRadius() : -ca.getRadius()));
      }
    }
  }

  private static DArray getCandidate(final EdDisc c) {
    // find best two candidates for supporting this disc
    DArray can = new DArray(discs);
    can.sort(new Comparator() {
      public int compare(Object arg0, Object arg1) {
        EdDisc c1 = (EdDisc) arg0, c2 = (EdDisc) arg1;

        double d1 = DiscUtil.itan(c, c1) ? DiscUtil.itanDist(c, c1) : DiscUtil
            .otanDist(c, c1);
        double d2 = DiscUtil.itan(c, c2) ? DiscUtil.itanDist(c, c2) : DiscUtil
            .otanDist(c, c2);

        return (int) Math.signum(d1 - d2);
      }
    });
    return can;
  }

  /**
   * Make highlighted inactive discs tangent to best-fit candidate
   */
  private void makeTangent() {
    DArray a = Editor.editObjects(EdDisc.FACTORY, true, false);
    for (int k = 0; k < a.size(); k++) {
      EdDisc c = (EdDisc) a.get(k);

      // find best two candidates for supporting this disc
      DArray can = getCandidate(c);
      if (can.size() < 1)
        continue;

      EdDisc ca = (EdDisc) can.get(0);
      boolean ia = DiscUtil.itan(c, ca);
      c.setRadius(FPoint2.distance(c.getOrigin(), ca.getOrigin())
          + (ia ? ca.getRadius() : -ca.getRadius()));
    }
  }
  private void makeDiam() {
    DArray a = Editor.editObjects(null, false, false);
    for (int k = 0; k < a.size(); k++) {
      EdObject obj = (EdObject) a.get(k);
      if (!(obj.isSelected()))
        continue;
      if (!(obj instanceof EdSegment))
        continue;

      EdSegment c = (EdSegment) obj;

      FPoint2 mid = FPoint2.midPoint(c.getPoint(0), c.getPoint(1));
      //      double theta = MyMath.polarAngle(c.getPoint(0), c.getPoint(1));
      //      double rad = FPoint2.distance(mid, c.getPoint(1));

      EdDiameter diam = (EdDiameter) EdDiameter.FACTORY.construct();
      diam.setPoint(0, mid);
      diam.setPoint(1, c.getPoint(1));
      a.set(k, diam);
    }
    Editor.replaceAllObjects(a);
  }
  public void setParameters() {
    parms.appTitle = "Possible 1-Centers";
    parms.menuTitle = "Main";
    parms.fileExt = "dat";
  }

  private DArray buildBisectorEdges() {
    DArray bisectorEdges = new DArray();
    final boolean db = false;
    EdDisc[] discs = OCentMain.getDiscs();
    int k = 0;
    for (int di = 0; di < discs.length; di++) {
      EdDisc ddi = discs[di];
      if (!ddi.isActive())
        continue;
      for (int dj = 0; dj < discs.length; dj++) {
        if (dj == di)
          continue;

        EdDisc ddj = discs[dj];
        if (!ddj.isActive())
          continue;
        k++;
        // db = (k == C.vi(DBSAMP));

        Hyperbola bs = GuaranteedDiscBisector.S.getBisector(ddi, ddj);

        if (db && T.update())
          T.msg("bisector for discs" + T.show(ddi) + T.show(ddj) + T.show(bs));
        if (bs == null)
          continue;

        if (!bs.valid())
          continue;

        // add all the time, so we can debug sampling
        bisectorEdges.add(bs);

        sampleBisector(ddi, ddj, bs, db);
      }
    }
    return bisectorEdges;

  }
  private static boolean allDiscsOverlap(EdDisc sd) {
    final boolean db = false;
    boolean valid = true;
    EdDisc[] discs = OCentMain.getDiscs();
    for (int j = 0; j < discs.length; j++) {
      EdDisc dj = discs[j];
      if (!dj.isActive())
        continue;
      if (!EdDisc.overlap(sd, dj)) {
        if (db && T.update())
          T.msg("discs don't overlap" + T.show(sd) + T.show(dj));
        valid = false;
        break;
      }
    }
    return valid;
  }

  private void sampleBisector(EdDisc di, EdDisc dj, Hyperbola b, boolean db) {

    EdDisc[] discs = OCentMain.getDiscs();

    double TMAX = C.vi(SAMPRANGE);
    double TRES = C.vi(SAMPRES) / 6.0;

    double tPrev = -1000;

    for (double t = -TMAX; t < TMAX;) {
      FPoint2 pt = b.calcPoint(t);
      EdDisc sd = new EdDisc(pt, 1e-6 + pt.distance(di.getOrigin())
          + di.getRadius());
      if (db && T.update())
        T.msg("sampling bisector, disc" + T.show(sd) + T.show(sd.getOrigin()));
      boolean valid = false;
      do {
        if (!allDiscsOverlap(sd)) {
          if (db && T.update())
            T.msg("not all discs overlap" + T.show(sd));
          break;
        }

        DiscBoundary dbnd = new DiscBoundary(sd);
        for (int j = 0; j < discs.length; j++) {
          EdDisc ddj = discs[j];
          if (ddj == di)
            continue;
          if (!ddj.isActive())
            continue;
          dbnd.exclude(ddj);
        }
        // exclude the tangency points of the two discs
        // that produced the bisector
        dbnd.exclude(MyMath.polarAngle(pt, dj.getOrigin()));

        // determine if 180 rule is violated before adding di's tangent point
        if (db && T.update())
          T.msg("determining if valid180 before adding inside tangent disc"
              + T.show(di));
        boolean wasValid = dbnd.valid180(db);
        if (wasValid) {
          if (db && T.update())
            T.msg("was valid before inside tangent disc added, skipping");
          break;
        }
        dbnd.exclude(MyMath.polarAngle(pt, di.getOrigin()));

        if (db && T.update())
          T.msg("boundary" + T.show(dbnd));
        if (!dbnd.valid180(db)) {
          if (db && T.update())
            T.msg("boundary is not valid" + T.show(dbnd));
          break;
        }
        valid = true;
      } while (false);

      if (!valid) {
        b.clip(tPrev, t);
      }
      tPrev = t;

      double step = Math.sqrt(Math.abs(t)) / TRES;
      step = Math.max(step, .05);
      t += step;

    }
  }

  public static EdDisc getMinBound() {
    if (minBound == null) {
      minBound = OneCenterOfDiscs.findCenter(new DArray(OCentMain.getDiscs()),
          new Random(1965));
    }
    return minBound;
  }

  public void paintView() {
    discs = null;
    discs2 = null;
    rects = null;
    rects2 = null;
    polygons = null;
    minBound = null;

    if (false)
    PossOneCentOper.singleton.plotSamples();

    if (FULL && C.vb(MINBOUND)) {
      T.render(getMinBound(), MyColor.cLIGHTGRAY, -1, MARK_X);
    }

    if (FULL && C.vb(BEDGES)) {
      DArray bisectorEdges = buildBisectorEdges();
      T.renderAll(bisectorEdges, Color.black);
    }

    super.paintView();

    //    if (TestBed.oper() != EnvOper.singleton)
    //      EnvOper.singleton.plotSamples();

    if (FULL && C.vb(RNDTEST)) {
      if (T.lastEvent() == null) {
        GeneratorOper.generateRandom();
        V.repaint();
      } else
        C.setb(RNDTEST, false);
    }
  }

  public static EdPolygon[] getPolygons() {
    if (polygons == null) {
      DArray a = Editor.readObjects(EdPolygon.FACTORY, false, true);
      polygons = new EdPolygon[a.size()];
      for (int i = 0; i < a.size(); i++) {
        EdPolygon ed = (EdPolygon) a.get(i);
        ed = EdPolygon.normalize(ed);
        polygons[i] = ed;
      }
    }
    return polygons;
  }

  public static void perturbDiscs() {
    Matrix m = new Matrix(3);
    m.setIdentity();

    //      Matrix.identity(3, null);
    m.translate(-50, -50);
    m.rotate(MyMath.radians(1));
    m.translate(50, 50);

    for (Iterator it = Editor.editObjects(EdDisc.FACTORY, false, false)
        .iterator(); it.hasNext();) {
      EdDisc ed = (EdDisc) it.next();
      FPoint2 loc = ed.getOrigin();
      loc = m.apply(loc, null);
      ed.setPoint(0, loc);
    }
    Editor.unselectAll();
  }

  public static EdDisc[] getDiscs2() {
    getDiscs();
    return discs2;
  }

  public static EdDisc[] getDiscs() {
    if (discs == null) {
      DArray a = Editor.readObjects(EdDisc.FACTORY, false, true);
      DArray b = Editor.readObjects(EdDisc.FACTORY, false, false);

      discs = (EdDisc[]) a.toArray(EdDisc.class);
      discs2 = (EdDisc[]) b.toArray(EdDisc.class);

      for (int i = 0; i < discs2.length; i++) {
        discs2[i].clearFlags(DiscUtil.DISC_OVERLAPPING
            | DiscUtil.DISC_CONTAINED);
      }

    }
    return discs;
  }

  public static EdObject[] getRegions() {
    EdObject[] ret;

    switch (regionType()) {
    default:
      throw new UnsupportedOperationException();
    case OCentMain.RECTS:
    case OCentMain.SQUARES:
      ret = getRects();
      break;
    case OCentMain.DISCS:
      ret = getDiscs();
      break;
    case OCentMain.POLYGONS:
      ret = getPolygons();
      break;
    }
    return ret;
  }
  public static EdRect[] getRects() {
    if (rects == null) {
      DArray a = Editor.readObjects(EdRect.FACTORY, false, true);
      DArray b = Editor.readObjects(EdRect.FACTORY, false, false);

      rects = (EdRect[]) a.toArray(EdRect.class);
      rects2 = (EdRect[]) b.toArray(EdRect.class);

      for (int i = 0; i < rects2.length; i++) {
        rects2[i].clearFlags(DiscUtil.DISC_OVERLAPPING
            | DiscUtil.DISC_CONTAINED);
      }

    }
    return rects;
  }

  /**
   * Construct a string that uniquely describes a set of EdObjects
   * @param obj
   * @return
   */
  public static String getHash(EdObject[] obj) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < obj.length; i++) {
      sb.append(obj[i].getHash());
    }
    return sb.toString();
  }

  /**
   * @deprecated
   * @param d
   * @return
   */
  public static DArray getActive(EdObject[] d) {
    DArray f = new DArray();
    for (int j = 0; j < d.length; j++) {
      if (d[j].isActive())
        f.add(d[j]);
      //      else
      //        throw new IllegalStateException("testing");
    }
    return f;
  }

  private static EdDisc[] discs;
  private static EdDisc[] discs2;
  private static EdRect[] rects;
  private static EdRect[] rects2;
  private static EdPolygon[] polygons;
  private static EdDisc minBound;
}
