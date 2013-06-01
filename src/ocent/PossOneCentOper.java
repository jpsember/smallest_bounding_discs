package ocent;

import java.awt.*;
import java.util.*;
import base.*;
import testbed.*;

public class PossOneCentOper implements TestBedOperation, Globals {
  /*! .enum  .private 2700
     resetsamp ntrials plotlast run  
      step _ maxpoints _ resolution
      with2
      with3 _ _ _ _ _ _ _ 
     _ _ _ plotsamples thread fpt usersamples bndonly
  */

  private static final int RESETSAMP = 2700;//!
  private static final int NTRIALS = 2701;//!
  private static final int PLOTLAST = 2702;//!
  private static final int RUN = 2703;//!
  private static final int STEP = 2704;//!
  private static final int MAXPOINTS = 2706;//!
  private static final int RESOLUTION = 2708;//!
  private static final int WITH2 = 2709;//!
  private static final int WITH3 = 2710;//!
  private static final int PLOTSAMPLES = 2721;//!
  private static final int THREAD = 2722;//!
  private static final int FPT = 2723;//!
  private static final int USERSAMPLES = 2724;//!
  private static final int BNDONLY = 2725;//!
  /*!*/

  public void addControls() {
    C.sOpenTab("P1C");
    C.sStaticText("Investigates possible 1-centers of uncertain regions");
    {
      {
        C.sOpen();
        C.sButton(STEP, "Step", "Process single random sample");
        C.sButton(RUN, "Run", "Generate a number of random samples");
        C.sNewColumn();
        C.sButton(RESETSAMP, "Reset", "Clears samples");
        C.sClose();
      }

      C.sCheckBox(USERSAMPLES, "user points",
          "allows user to specify point within each uncertain region", false);

      {
        C.sOpen();
        C.sIntSpinner(NTRIALS, "# trials", "Number of trials to run at once",
            1, 1000000, 5000, 1000);

        C.sIntSpinner(RESOLUTION, "Res", "Sampling resolution", 1, 10, 3, 1);
        C.sNewColumn();
        //        C.sCheckBox(PERIM, "Restrict",
        //            "Choose sample points from disc perimeters only", true);
        C.sCheckBox(PLOTLAST, "Plot last", null, false);
        C.sCheckBox(PLOTSAMPLES, "Plot samples", null, true);
        C.sCheckBox(THREAD, "Thread plot", null, true);
        C.sCheckBox(FPT, "FVorn", "Plot furthest point Voronoi diagram", false);

        C.sClose();
      }

      C.sHide();
      C.sIntSpinner(MAXPOINTS, "max samples",
          "if not zero, limit of # samples to keep", 0, 100000, 0, 5000);
      {
        C.sOpen();

        C.sCheckBox(WITH2, "2-pt", "Include samples determined by two points",
            true);
        C.sCheckBox(WITH3, "3-pt",
            "Include samples determined by three points", true);
        C
            .sCheckBox(BNDONLY, "Bnd only",
                "Restrict points to region boundaries only (not sufficient)",
                false);
        //        C.sNewColumn();
        //
        //        C.sIntSpinner(FIXRAD, "radius",
        //            "display only samples with this radius", 0, 120, 40, 1);
        //        C.sIntSpinner(RADSHARP, "range", "cutoff factor for radius", 0, 50, 0,
        //            1);
        C.sClose();
      }
    }

    C.sCloseTab();
  }

  public static PossOneCentOper singleton = new PossOneCentOper();

  private SampleThread thread;

  private static class SampleThread extends Thread {
    public SampleThread() {

      setDaemon(true);
    }
    //    public void updateActive(boolean a) {
    //      this.active = a;
    //    }

    private boolean update;

    private static final int SLEEP0 = 500;
    private static final int SAMP0 = 0;

    private int sleepTime;
    private int nSamples;

    private boolean active = true;

    private void reset() {
      sleepTime = SLEEP0;
      nSamples = SAMP0;
    }
    public void run() {
      reset();
      while (true) {
        if (active) {
          synchronized (this) {
            update = true;
          }
          V.repaint();
        }
        Tools.sleep(sleepTime);
      }
    }

    public int processUpdate() {
      int ret = 0;
      synchronized (this) {
        if (update) {
          update = false;

          ret = nSamples;
          sleepTime = Math.max(200, (int) (sleepTime * .8));
          nSamples = Math.min((int) (1 + nSamples * 2), 10000);
        }
        //        System.out.println("sleep=" + sleepTime + " n=" + nSamples);
      }
      return ret;
    }

    public void setActive(boolean a) {
      this.active = a;
    }
  }

  private PossOneCentOper() {
    thread = new SampleThread();
    //   thread.setActive(C.vb(PLOTSAMPLES) && C.vb(THREAD));
    // thread.start();
  }

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      case RESETSAMP:
        reset();
        break;
      case STEP:
        run(1, C.vb(BNDONLY), false);
        break;
      case RUN:
        resample();
        break;
      case PLOTSAMPLES:
      case THREAD:
        thread.setActive(C.vb(PLOTSAMPLES) && C.vb(THREAD));
        break;

      }
    } else if (a.code == TBAction.HOVER && a.shiftPressed()) {
      if (ptSamples != null) {
        hoverSample = (OneCenterSample) ptSamples.findObjectAt(a.loc);
      }
    }
  }

  public void resample() {
    run(C.vi(NTRIALS), false, C.vi(NTRIALS) < 1000);
  }

  public void clearSamples() {
    lastSample = null;
    userSample = null;
    ptSamples = null;
  }

  private void reset() {
    clearSamples();

  }

  private void prepareSamples(EdObject[] d) {
    int res = C.vi(RESOLUTION);
    double gs = res * .1;
    {
      if (ptSamples == null) {
        ptSamples = new BitMap();
      }
      String hash = OCentMain.getHash(d);
      hash = hash + C.vb(WITH2) + C.vb(WITH3);
      if (ptSamples.prepare(hash, gs)) {
        lastSample = null;
        vornGraph = null;
      }

      // don't store samples if resolution is very low
      if (res > 2)
        ptSamples.addObjectMatrix();
    }
  }

  private int minDet, maxDet;

  private void run(int trials, boolean perim, boolean newOnly) {

    minDet = 2;
    maxDet = 3;
    if (!C.vb(WITH2))
      minDet++;
    if (!C.vb(WITH3))
      maxDet--;
    //  updateLastSample = !C.vb(TRACKINDEP);  

    EdObject[] d2 = OCentMain.getRegions();
    if (d2.length < 2) {
      clearSamples();
      return;
    }
    prepareSamples(d2);
    OneCenterSample sample = null;

    switch (OCentMain.regionType()) {
    case OCentMain.POLYGONS:
      {
        FRect[] bnds = new FRect[d2.length];

        for (int i = 0; i < d2.length; i++) {
          EdObject ds = d2[i];
          bnds[i] = ds.getBounds();
        }
        for (int i = 0; i < trials; i++) {
          DArray pts = new DArray(bnds.length);

          for (int j = 0; j < bnds.length; j++) {
            EdPolygon p = (EdPolygon) d2[j];
            FPoint2 samp = new FPoint2();
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
            pts.add(samp);
          }

          sample = new OneCenterSample(pts);
          addSample(sample, newOnly);

        }
      }
      break;
    default:
      {
        FRect[] bnds = new FRect[d2.length];

        for (int i = 0; i < d2.length; i++) {
          EdObject ds = d2[i];
          bnds[i] = ds.getBounds();
        }
        for (int i = 0; i < trials; i++) {
          DArray pts = new DArray(d2.length);

          for (int j = 0; j < bnds.length; j++) {
            FRect b = bnds[j];

            FPoint2 pt = new FPoint2();
            pts.add(pt);
            pt.setLocation(MyMath.rnd(b.width) + b.x, MyMath.rnd(b.height)
                + b.y);
            if (perim) {
              if (MyMath.rnd(3) < 2) {
                int corner = MyMath.rnd(4);
                pt.x = (corner < 2) ? b.x : b.x + b.width;
                pt.y = ((corner & 1) == 0) ? b.y : b.y + b.height;
              } else {
                double t = b.width * 2 + b.height * 2;
                double s = MyMath.rnd(t);
                do {
                  if (s < b.width) {
                    pt.y = b.y;
                    break;
                  }
                  s -= b.width;
                  if (s < b.width) {
                    pt.y = b.y + b.height;
                    break;
                  }
                  s -= b.width;
                  if (s < b.height) {
                    pt.x = b.x;
                    break;
                  }
                  pt.x = b.x + b.width;
                } while (false);
              }
            }
          }

          sample = new OneCenterSample(pts);
          addSample(sample, newOnly);
        }

      }
      break;
    case OCentMain.DISCS:
      {

        FPoint2[] dor = new FPoint2[d2.length];
        double[] rad = new double[d2.length];
        for (int i = 0; i < d2.length; i++) {
          EdDisc ds = (EdDisc) d2[i];

          dor[i] = ds.getOrigin();
          rad[i] = ds.getRadius();
        }
        for (int i = 0; i < trials; i++) {

          DArray pts = new DArray(dor.length);

          for (int j = 0; j < dor.length; j++) {
            FPoint2 pt = new FPoint2(); //samp[j];
            pts.add(pt);
            if (!perim)
              MyMath.rndPtInDisc(dor[j], rad[j], pt);
            else
              MyMath.ptOnCircle(dor[j], MyMath.rnd(Math.PI * 2), rad[j], pt);
          }
          sample = new OneCenterSample(pts);
          addSample(sample, newOnly);

        }
      }
      break;
    }

  }
  // private boolean updateLastSample;
  private void addSample(OneCenterSample sample, boolean newOnly) {
    do {
      int nDet = sample.nCritPoints();

      if (nDet < minDet || nDet > maxDet)
        break;

      if (newOnly && ptSamples.findObjectAt(sample.getOrigin()) != null)
        break;

      ptSamples.setColor(OneCenterSample.sampleColors[nDet - 2]);

      //   Color col = OneCenterSample. sampleColors[ci];

      ptSamples.plot(sample.getOrigin(), sample);
      lastSample = sample;
    } while (false);
  }

  public void runAlgorithm() {
    // vornGraph = null;
    boundDisc = null;
    bisectorEdges = null;
    if (C.vb(USERSAMPLES))
      trackIndep();

  }
  private static Color trackColors[] = { MyColor.get(MyColor.GREEN, .6), //
      MyColor.get(MyColor.ORANGE, .3), MyColor.get(MyColor.BROWN, 1.0), };

  /**
   * Make sure there is a point object associated with, and contained within, each
   * uncertain region
   */
  private void trackIndep() {
    do {

      // if true, objects have changed and should be replaced
      boolean objChanged = false;
      EdObject[] rgns = OCentMain.getRegions();
      // new objects, in case they need replacing
      DArray newObj = new DArray(rgns);
      // EdPoint objects
      DArray currPoints = Editor.readObjects(EdPoint.FACTORY, false, false);
      DArray samples = new DArray();
      prepareSamples(rgns);

      switch (OCentMain.regionType()) {
      case OCentMain.RECTS:
      case OCentMain.SQUARES:
        {

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

      userSample = new OneCenterSample(samples);
      ptSamples.setColor(trackColors[2]);
      ptSamples.plot(userSample.getOrigin(), userSample);

    } while (false);
  }

  public void plotSamples() {
    // Ignore if we're in the one center operation, 
    // since in that case we want to delay this plot operation
    if (TestBed.oper() == this)
      return;
    plotSamples0();
  }

  private void plotSamples0() {
    if (ptSamples != null) {
      if (C.vb(PLOTSAMPLES))
        ptSamples.render();
    }
    T.render(hoverSample);
  }
  private void plotSamples1() {
    if (C.vb(USERSAMPLES))
      T.render(userSample, MyColor.cRED);
    if (C.vb(PLOTLAST))
      T.render(lastSample, MyColor.cDARKGREEN);

    T.render(boundDisc, MyColor.cLIGHTGRAY, -1, MARK_X);

    T.renderAll(bisectorEdges, Color.black);

  }

  private boolean perim;
  private boolean threadStarted;

  public void updateThread() {

    if (!threadStarted) {
      thread.setActive(C.vb(PLOTSAMPLES) && C.vb(THREAD));
      thread.start();
      threadStarted = true;
    }

    int nSamp = thread.processUpdate();
    if (nSamp > 0) {
      run(nSamp, C.vb(BNDONLY) || perim, true);
      perim ^= true;
    }
  }

  public void paintView() {

    updateThread();
    plotSamples0();

    if (C.vb(FPT)) {
      if (vornGraph == null) {
        EdObject[] rgns = OCentMain.getRegions();

        switch (OCentMain.regionType()) {
        case OCentMain.RECTS:
        case OCentMain.SQUARES:
          {
            Hyperbola[] vDiag = VornUtil.buildRects((EdRect[]) new DArray(rgns)
                .toArray(EdRect.class), 1, StandardDiscBisector.FARTHEST, 0);
            vornGraph = new VornGraph(vDiag);
          }
          break;

        case OCentMain.DISCS:
          {
            Hyperbola[] vDiag = VornUtil.build((EdDisc[]) new DArray(rgns)
                .toArray(EdDisc.class), 1, StandardDiscBisector.FARTHEST, 0);
            vornGraph = new VornGraph(vDiag);
          }
          break;
        }
      }
      T.render(vornGraph);
    }

    Editor.render();
    plotSamples1();
  }
  private Renderable vornGraph;
  private OneCenterSample lastSample;
  private OneCenterSample userSample;
  private EdDisc boundDisc;
  private DArray bisectorEdges;
  private BitMap ptSamples;
  private OneCenterSample hoverSample;
}
