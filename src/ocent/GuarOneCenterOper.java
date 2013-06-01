package ocent;

import java.awt.*;
import java.util.*;
import ocent.PossOneCentOper.*;
import base.*;
import testbed.*;

public class GuarOneCenterOper implements TestBedOperation, Globals {
  /*! .enum  .private 2650
      resetsamp ntrials plotlast run  
      step  perim _     showarcsep trackindep fpt with2 with3 findarc thread plottri
  */

    private static final int RESETSAMP        = 2650;//!
    private static final int NTRIALS          = 2651;//!
    private static final int PLOTLAST         = 2652;//!
    private static final int RUN              = 2653;//!
    private static final int STEP             = 2654;//!
    private static final int PERIM            = 2655;//!
    private static final int SHOWARCSEP       = 2657;//!
    private static final int TRACKINDEP       = 2658;//!
    private static final int FPT              = 2659;//!
    private static final int WITH2            = 2660;//!
    private static final int WITH3            = 2661;//!
    private static final int FINDARC          = 2662;//!
    private static final int THREAD           = 2663;//!
    private static final int PLOTTRI          = 2664;//!
/*!*/

  private GuarOneCenterOper() {
    //  Tools.unimp("delay starting thread until module is up and running");
    thread = new OurThread();

  }
  public static GuarOneCenterOper singleton = new GuarOneCenterOper();

  public void addControls() {

    C.sOpenTab("G1C");
    {
      C.sStaticText("Guaranteed 1-center of uncertain regions; hover with shift pressed to display samples");
      {
        C.sOpen();
        C.sButton(RUN, "Run", "Generate a number of random hulls");
        C.sNewColumn();
        C.sButton(STEP, "Step", "Process single random hull");
        C.sNewColumn();
        C.sButton(RESETSAMP, "Reset", "Clears samples");
        C.sClose();
      }
      {
        C.sOpen();
        C.sIntSpinner(NTRIALS, "# trials", "Number of trials to run at once",
            1, 1000000, 5000, 1000);

        C.sCheckBox(PERIM, "Restrict",
            "Choose sample points from disc perimeters only", true);
        C.sCheckBox(PLOTLAST, "Plot last", null, false);
        C.sCheckBox(SHOWARCSEP, "plot vertices",
            "show vertices of disc intersection", false);
        C.sClose();
      }
      C.sCheckBox(TRACKINDEP, "user points",
          "allows user to specify point within each uncertain region", false);
      C.sCheckBox(FPT, "FVorn", "Plot furthest point Voronoi diagram", false);
      C.sCheckBox(WITH2, "2-pt", "Include samples determined by two points",
          true);
      C.sCheckBox(WITH3, "3-pt", "Include samples determined by three points",
          true);
      C.sCheckBox(FINDARC, "arc details", "Shows details of arc near mouse",
          false);
      C.sCheckBox(PLOTTRI, "arc tri",
          "Plot right-angle triangle corresponding to arc details", false);
      C.sCheckBox(THREAD, "Thread plot", null, true);
    }

    C.sCloseTab();
  }

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      case THREAD:
        thread.setActive(C.vb(THREAD));
        break;
      case RESETSAMP:
        reset();
        break;
      case STEP:
        run(1, false);
        break;
      case RUN:
        run(C.vi(NTRIALS), true);
        break;
      case FINDARC:
        hoverArc = null;
        //hoverPoint = null;
        break;

      }
    } else if (a.code == TBAction.HOVER && a.shiftPressed()) {
      if (C.vb(FINDARC) && dInt != null) {
       // hoverPoint = a.loc;
        hoverArc = dInt.findArcAt(a.loc);
        if (hoverArc != null && C.vb(PLOTTRI))
          hoverArc.setShowTriangle(a.loc);
      }
    }
  }

  private void reset() {
    lastDisc = null;
    lastPointSet = null;
    dInt = null;
  }

  private void run(int trials, boolean perim0) {

    boolean perim = perim0;
    EdObject[] d2 = OCentMain.getRegions();
    FRect[] bnds = new FRect[d2.length];
    for (int i = 0; i < d2.length; i++)
      bnds[i] = d2[i].getBounds();
    int minDet = C.vb(WITH2) ? 2 : 3;
    int maxDet = C.vb(WITH3) ? 3 : 2;
    EdDisc lastSample = null;

    prepareSamples(d2);
    if (d2.length < 2)
      return;

    for (int i = 0; i < trials; i++) {
      if (perim0) {
        if (false)
          perim = (i & 1) == 0;

      }

      DArray pts = new DArray(d2.length);

      for (int j = 0; j < d2.length; j++) {
        FPoint2 samp = new FPoint2();
        pts.add(samp);

        switch (OCentMain.regionType()) {
        case OCentMain.POLYGONS:
          {

            EdPolygon p = (EdPolygon) d2[j];
            FRect b = bnds[j];
            if (perim) {
              int side = MyMath.rnd(p.nPoints());
              if (MyMath.rnd(3) < 2) {
                samp.setLocation(p.getPoint(side));
              } else {
                FPoint2 ept = FPoint2.interpolate(p.getPoint(side), p
                    .getPointMod(side + 1), MyMath.rnd(1.0));
                samp.setLocation(ept);
              }
            } else {
              while (true) {
                samp.setLocation(MyMath.rnd(b.width) + b.x, MyMath
                    .rnd(b.height)
                    + b.y);
                if (p.contains(samp))
                  break;
              }
            }
          }
          break;
        default:
          {
            FRect b = bnds[j];

            samp.setLocation(MyMath.rnd(b.width) + b.x, MyMath.rnd(b.height)
                + b.y);
            if (perim) {
              if (MyMath.rnd(3) < 2) {
                int corner = MyMath.rnd(4);
                samp.x = (corner < 2) ? b.x : b.x + b.width;
                samp.y = ((corner & 1) == 0) ? b.y : b.y + b.height;
              } else {
                double t = b.width * 2 + b.height * 2;
                double s = MyMath.rnd(t);
                do {
                  if (s < b.width) {
                    samp.y = b.y;
                    break;
                  }
                  s -= b.width;
                  if (s < b.width) {
                    samp.y = b.y + b.height;
                    break;
                  }
                  s -= b.width;
                  if (s < b.height) {
                    samp.x = b.x;
                    break;
                  }
                  samp.x = b.x + b.width;
                } while (false);
              }
            }
          }
          break;

        case OCentMain.DISCS:
          {
            EdDisc disc = (EdDisc) d2[j];
            if (!perim)
              MyMath.rndPtInDisc(disc.getOrigin(), disc.getRadius(), samp);
            else
              MyMath.ptOnCircle(disc.getOrigin(), MyMath.rnd(Math.PI * 2), disc
                  .getRadius(), samp);
          }
          break;
        }
      }

      EdDisc theDisc = OneCenterOfPoints.findCenter(pts, null);
      int nDet = theDisc.flags();

      if (nDet < minDet || nDet > maxDet)
        continue;

      try {
      if (dInt == null)
        dInt = new DiscIntersection(theDisc, pts);
      else
        dInt.include(theDisc, pts);
      } catch (FPError e) {
      Tools.warn("(FPError ignored)");
      }
      
      lastSample = theDisc;

      if (!C.vb(TRACKINDEP)) {
        lastPointSet = pts;
        this.lastDisc = lastSample;
      }
    }
  }

  public void runAlgorithm() {
    vornGraph = null;
    if (C.vb(TRACKINDEP))
      trackIndep();
  }

  private boolean plotSamp() {
    return (C.vb(PLOTLAST) || C.vb(TRACKINDEP)) && !C.vb(FINDARC);

  }

  private void plotSamples1() {
    if (plotSamp()) {
      V.pushColor(MyColor.cDARKGREEN);
      if (lastDisc != null) {
        V.drawCircle(lastDisc.getOrigin(), lastDisc.getRadius());
        V.pushColor(lastDisc.flags() == 2 ? MyColor.cRED : MyColor.cPURPLE);
        V.mark(lastDisc.getOrigin(), MARK_X);
        V.popColor();
        if (lastPointSet != null) {
          CritPt.plotCriticalSet(lastPointSet, lastDisc.getOrigin(), lastDisc
              .getRadius());
          //          plotCriticalSet(lastPointSet, lastDisc.getOrigin(), lastDisc
          //              .getRadius(), true);
        }
      }

      V.popColor();
    }
  }

  //  /**
  //   * @deprecated
  //   * @param points
  //   * @param mbOrigin
  //   * @param mbRadius
  //   * @return
  //   */
  //  public static DArray critSet(DArray points, FPoint2 mbOrigin, double mbRadius) {
  //
  //    DArray ps = new DArray();
  //    for (int i = 0; i < points.size(); i++) {
  //      CritPt c = new CritPt(points.getFPoint2(i), mbOrigin, mbRadius);
  //      if (c.isCritical())
  //        ps.add(c);
  //    }
  //
  //    // sort by angle
  //    ps.sort(CritPt.COMPARATOR);
  //
  //    outer: do {
  //      // find diameter
  //      for (int i = 0; i < ps.size(); i++) {
  //        CritPt pi = (CritPt) ps.get(i);
  //        for (int j = i + 1; j < ps.size(); j++) {
  //          CritPt pj = (CritPt) ps.get(j);
  //          if (Math.abs(pj.angle() - pi.angle() - Math.PI) < .02) {
  //            ps.clear();
  //            ps.add(pi);
  //            ps.add(pj);
  //            break outer;
  //          }
  //        }
  //      }
  //
  //      for (int i = 0; i < ps.size(); i++) {
  //        CritPt prev = (CritPt) ps.getMod(i - 1);
  //        //  CritPt p = (CritPt)ps.get(i);
  //        CritPt next = (CritPt) ps.getMod(i + 1);
  //        double diff = MyMath
  //            .normalizeAnglePositive(next.angle() - prev.angle());
  //        if (diff < Math.PI) {
  //          ps.remove(i);
  //          i--;
  //          continue;
  //        }
  //      }
  //      //    // filter out points not on disc boundary
  //      //    for (int i = 0; i < points.size(); i++) {
  //      //      FPoint2 p = points.getFPoint2(i);
  //      //      double r = p.distance(mbOrigin);
  //      //      if (r + NEARZERO < mbRadius)
  //      //        continue;
  //      //      ps.add(p);
  //      //    }
  //      //
  //      // 
  //    } while (false);
  //
  //    return ps;
  //  }

  //  /**
  //   * Plot critical set for a minimum bounding disc.  If two points form diameter, plot the diameter
  //   * @param points
  //   * @param mbDisc
  //   */
  //  public static void plotCriticalSet(DArray points, FPoint2 mbOrigin,
  //      double mbRadius, boolean filterNonCrit) {
  //
  //    if (filterNonCrit) {
  //      DArray cs = points;
  //      if (cs.size() > 0 && !(cs.get(0) instanceof CritPt)) {
  //        cs = critSet(points, mbOrigin, mbRadius);
  //      }
  //
  //      //   DArray cs = critSet(points, mbOrigin, mbRadius);
  //      V.pushColor(cs.size() == 2 ? MyColor.cPURPLE : MyColor.cLIGHTGRAY);
  //      V.pushStroke(cs.size() == 2 ? STRK_NORMAL : STRK_THIN);
  //      for (int i = 0; i < cs.size(); i++) {
  //        CritPt cp = (CritPt) cs.get(i);
  //        V.pushColor(MyColor.cRED);
  //        V.mark(cp, MARK_DISC, 1.0);
  //        V.pop();
  //
  //        V.drawLine(cp, mbOrigin);
  //      }
  //      V.pop(2);
  //    } else {
  //
  //      for (int i = 0; i < points.size(); i++) {
  //        FPoint2 pt = points.getFPoint2(i);
  //        boolean c = OneCenter.isCritical(mbOrigin, mbRadius, pt);
  //        V.pushColor(c ? MyColor.cRED : MyColor.cDARKGREEN);
  //        V.mark(pt, MARK_DISC, c ? 1.0 : .6);
  //        V.pop();
  //      }
  //
  //      // check if two points form diameter
  //      final double NEARZERO = .2;
  //
  //      for (int i = 0; i < points.size(); i++) {
  //        FPoint2 pi = points.getFPoint2(i);
  //
  //        V.pushColor(MyColor.cLIGHTGRAY);
  //        V.pushStroke(STRK_THIN);
  //        V.drawLine(pi, mbOrigin);
  //        V.pop(2);
  //
  //        for (int j = i + 1; j < points.size(); j++) {
  //          FPoint2 pj = points.getFPoint2(j);
  //          FPoint2 mid = FPoint2.midPoint(pi, pj);
  //          if (mid.distance(mbOrigin) < NEARZERO) {
  //            V.pushColor(MyColor.cPURPLE);
  //            V.drawLine(pi, pj);
  //            V.pop();
  //          }
  //        }
  //      }
  //    }
  //  }

  public void plotSamples() {
    // Ignore if we're in this operation, 
    // since in that case we want to delay this plot operation
    if (TestBed.oper() == this)
      return;
    plotSamples0();
  }

  private void plotSamples0() {
    if (plotSamp()) {
      V.pushColor(MyColor.cDARKGREEN);
      if (lastPointSet != null) {
        for (int i = 0; i < lastPointSet.size(); i++)
          V.mark(lastPointSet.getFPoint2(i));
      }
      if (lastDisc != null) {
        V.drawCircle(lastDisc.getOrigin(), lastDisc.getRadius());
        V.mark(lastDisc.getOrigin(), MARK_X);
      }
      V.popColor();
    }
    T.render(dInt, null, -1, C.vb(SHOWARCSEP) ? 0 : -1);
    T.render(hoverArc);
  }
  public void updateThread() {
    int nSamp = thread.processUpdate();
    if (nSamp > 0) {
      run(nSamp, true);
    }
  }
  public void paintView() {
    if (!threadStarted) {
      thread.setActive(C.vb(THREAD));
      thread.start();
      threadStarted = true;
    }

    updateThread();
    plotSamples0();
    T.render(vornGraph);
    Editor.render();
    plotSamples1();
  }

  /**
   * Make sure there is a point object associated with, and contained within, each
   * uncertain region
   */
  private void trackIndep() {

    final boolean db = false;

    do {
      if (db)
        Streams.out.println("trackIndep");

      // if true, objects have changed and should be replaced
      boolean objChanged = false;

      EdObject[] rgns = OCentMain.getRegions();
      prepareSamples(rgns);

      // new objects, in case they need replacing
      DArray newObj = new DArray(rgns);

      // EdPoint objects
      DArray currPoints = Editor.readObjects(EdPoint.FACTORY, false, false);

      DArray samples = new DArray();

      switch (OCentMain.regionType()) {
      case OCentMain.RECTS:
      case OCentMain.SQUARES:
        {

          if (C.vb(FPT)) {
            Hyperbola[] vDiag = VornUtil.buildRects((EdRect[]) new DArray(rgns)
                .toArray(EdRect.class), 1, StandardDiscBisector.FARTHEST, 0);
            vornGraph = new VornGraph(vDiag);
          }

          for (int i = 0; i < rgns.length; i++) {
            FRect r = rgns[i].getBounds();
            EdPoint pi = null;
            if (i < currPoints.size()) {
              pi = (EdPoint) currPoints.get(i);
            } else {
              pi = new EdPoint(r.midPoint());
              objChanged = true;
            }
            FPoint2 clamped = r.clamp(pi.getPoint(0));

            pi.setPoint(0, clamped);
            newObj.add(pi);
            samples.add(clamped);
          }
        }
        break;

      case OCentMain.DISCS:
        {
          for (int i = 0; i < rgns.length; i++) {
            EdDisc disc = (EdDisc) rgns[i];
            FPoint2 origin = disc.getOrigin();
            double r = disc.getRadius();

            EdPoint pi = null;
            if (i < currPoints.size()) {
              pi = (EdPoint) currPoints.get(i);
            } else {
              pi = new EdPoint(origin);
              if (db)
                Streams.out.println("no point existed for region " + i
                    + ", created one");

              objChanged = true;
            }
            FPoint2 ploc = pi.getOrigin();
            double dist = FPoint2.distance(origin, ploc);
            if (dist > r) {
              pi.setPoint(0, MyMath.ptOnCircle(origin, MyMath.polarAngle(
                  origin, ploc), r));
            }
            newObj.add(pi);
            samples.add(pi.getPoint(0));
          }
        }
        break;

      case OCentMain.POLYGONS:
        {
          for (int i = 0; i < rgns.length; i++) {
            EdPolygon p = (EdPolygon) rgns[i];
            FRect bounds = p.getBounds();

            EdPoint pi = null;
            if (i < currPoints.size()) {
              pi = (EdPoint) currPoints.get(i);
            } else {
              pi = new EdPoint(bounds.midPoint());
              objChanged = true;
            }

            // bring point into range of polygon if necessary
            FPoint2 ploc = pi.getOrigin();
            if (!p.contains(ploc)) {
              ploc = p.closestBoundaryPointTo(ploc);
              pi.setPoint(0, ploc);
            }
            newObj.add(pi);
            samples.add(pi.getPoint(0));
          }
        }
        break;
      }

      if (rgns.length != currPoints.size())
        objChanged = true;

      if (objChanged)
        Editor.replaceAllObjects(newObj);

      lastPointSet = samples;
      EdDisc lastDisc = OneCenterOfPoints.findCenter(samples, null);
      this.lastDisc = lastDisc;
    } while (false);
  }

  public void clearSamples() {
    lastDisc = null;
    lastPointSet = null;
  }

  private void prepareSamples(EdObject[] d) {
    String hash = OCentMain.getHash(d);
    if (!hash.equals(lastHash)) {
      lastHash = hash;
      clearSamples();
      dInt = null;
      thread.itemsChanged();
      hoverArc = null;
    }
  }

  private EdDisc lastDisc;
  private String lastHash;
  private DArray lastPointSet;
  private Renderable vornGraph;

  private DiscIntersection dInt;

  private OurThread thread;

  /**
   * Thread to trigger sampling every second or so
   */
  private static class OurThread extends Thread {
    private static final boolean db = false;

    public OurThread() {

      setDaemon(true);
      if (db)
        Streams.out.println("OurThread constructed");
    }

    /**
     * Notify thread that items have changed (or, an editing operation has begun),
     * so it should reset the clock to avoid long sample times
     */
    public void itemsChanged() {
      itemsChanged = true;
      if (db)
        Streams.out.println("OurThread.itemsChanged");
    }

    // true if thread is active
    private boolean active = true;

    private boolean itemsChanged;
    private boolean update;

    // initial sleep time
    private static final int SLEEP0 = 300;
    // initial number of samples
    private static final int SAMP0 = 20;

    // ticks to sleep 
    private int sleepTime;
    // number of samples
    private int nSamples;

    private void reset() {
      sleepTime = SLEEP0;
      nSamples = SAMP0;
    }

    /**
     * Set thread active state (if not active, doesn't trigger sampling)
     * @param a
     */
    public void setActive(boolean a) {
      this.active = a;
      if (db)
        Streams.out.println("OurThread.active now " + active);
    }

    public void run() {

      if (db)
        Streams.out.println("OurThread.run begins");

      reset();
      while (true) {
        if (db)
          Streams.out.println("OurThread.run: active=" + active);

        // if not yet active, do nothing
        if (active) {
          synchronized (this) {
            if (itemsChanged) {
              itemsChanged = false;
              reset();
            }
            update = true;
            if (db)
              Streams.out.println("OurThread.run: calling V.repaint(), update="
                  + update);

          }
          if (update)
            V.repaint();
        }
        Tools.sleep(sleepTime);
      }
    }

    /**
     * Inform thread main program is ready to update view
     * and generate samples
     * @return number of samples to generate
     */
    public int processUpdate() {
      int ret = 0;

      synchronized (this) {
        if (update) {
          update = false;

          ret = nSamples;
          sleepTime = Math.max(250, (int) (sleepTime * .8));
          nSamples = Math.min((int) (1 + nSamples * 2), 1000);
        } else {

          // reset();
        }
        if (db)
          Streams.out.println("OurThread.processUpdate: returning " + ret
              + " samples");

      }
      return ret;
    }
  }
  private Arc hoverArc;
//  private FPoint2 hoverPoint;
  private boolean threadStarted;
}
