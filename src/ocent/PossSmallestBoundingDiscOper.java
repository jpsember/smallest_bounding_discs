package ocent;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import ocent.PossOneCentOper.*;
import base.*;
import testbed.*;

public class PossSmallestBoundingDiscOper implements TestBedOperation, Globals {
  /*! .enum  .private 2900
      _ ntrials plotlast run  
      step  _ resolution     _ trackindep _ with2 with3 _ thread _ _ monochrome minbound discbnd optimize
      optrange opta optb optc
  */

    private static final int NTRIALS          = 2901;//!
    private static final int PLOTLAST         = 2902;//!
    private static final int RUN              = 2903;//!
    private static final int STEP             = 2904;//!
    private static final int RESOLUTION       = 2906;//!
    private static final int TRACKINDEP       = 2908;//!
    private static final int WITH2            = 2910;//!
    private static final int WITH3            = 2911;//!
    private static final int THREAD           = 2913;//!
    private static final int MONOCHROME       = 2916;//!
    private static final int MINBOUND         = 2917;//!
    private static final int DISCBND          = 2918;//!
    private static final int OPTIMIZE         = 2919;//!
    private static final int OPTRANGE         = 2920;//!
    private static final int OPTA             = 2921;//!
    private static final int OPTB             = 2922;//!
    private static final int OPTC             = 2923;//!
/*!*/
private static final boolean FULL = OCentMain.FULL;

  private PossSmallestBoundingDiscOper() {
    thread = new OurThread();

  }
  public static PossSmallestBoundingDiscOper singleton = new PossSmallestBoundingDiscOper();

  public void addControls() {

    C.sOpenTab("PSBD");
    {
      C.sStaticText("Possible smallest bounding disc of uncertain regions; hover with shift pressed to display samples");
      {
        C.sOpen();
        C.sButton(RUN, "Run", "Generate a number of random sets");
        C.sIntSpinner(NTRIALS, "# trials", "Number of trials to run at once",
            1, 1000000, 1000, 1000);
        C.sNewColumn();
        C.sButton(STEP, "Step", "Process single random set");
        //     C.sNewColumn();
        //   C.sButton(RESETSAMP, "Reset", "Clears samples");
        C.sIntSpinner(RESOLUTION, "Res", "Sampling resolution", 1, 10, 3, 1);
        C.sClose();
      }
      {
        C.sOpen();
        C.sCheckBox(PLOTLAST, "Plot last", null, false);
        C.sCheckBox(TRACKINDEP, "User points",
            "allows user to specify point within each uncertain region", false);
        C.sCheckBox(DISCBND, "Boundaries", "Plot disc boundaries only", false);
        C.sCheckBox(THREAD, "Thread plot", null, true);
        C.sNewColumn();

        C.sCheckBox(WITH2, "2-pt", "Include samples determined by two points",
            true);
        C.sCheckBox(WITH3, "3-pt",
            "Include samples determined by three points", true);
        if (FULL) {
        C.sCheckBox(MONOCHROME, "Monochrome",
            "Plot samples in single color, regardless of # critical points",
            false);
        C.sCheckBox(MINBOUND, "SBD",
            "Plot smallest bounding disc of uncertain discs", false);
        }
        C.sClose();
      }
     if (FULL) {
        C.sOpen();
        C.sCheckBox(OPTIMIZE, "Type III search", "Target random points for type III curve", false);
        C.sIntSpinner(OPTRANGE,"Range",null,1,100,10,1);
        C.sIntSpinner(OPTA,"Disc A",null,0,360,90,1);
        C.sIntSpinner(OPTB,"Disc B",null,0,360,70,1);
        C.sIntSpinner(OPTC,"Disc C",null,0,360,110,1);
        
        C.sClose();
      }

    }

    C.sCloseTab();
  }

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      case THREAD:
        thread.setActive(C.vb(THREAD));
        break;
      //      case RESETSAMP:
      //        reset();
      //        break;
      case STEP:
        run(1);
        break;
      case RUN:
        run(C.vi(NTRIALS));
        break;
      }
    } else if (a.code == TBAction.HOVER && a.shiftPressed()) {
      if (ptSamples != null) {
        hoverArc = (OneCenterSample) ptSamples.findObjectAt(a.loc);
      }
    }
  }

  //  private void reset() {
  //    lastSample = null;
  //    ptSamples = null;
  //    hoverArc = null;
  //  }


  private void run(int trials) {

    //  boolean perim = perim0;
    EdObject[] d2 = OCentMain.getRegions();
    FRect[] bnds = new FRect[d2.length];
    for (int i = 0; i < d2.length; i++)
      bnds[i] = d2[i].getBounds();
    int minDet = C.vb(WITH2) ? 2 : 3;
    int maxDet = C.vb(WITH3) ? 3 : 2;

    prepareSamples(d2);
    if (d2.length < 2)
      return;

    for (int i = 0; i < trials; i++) {
      //      if (perim0) {
      //        if (false)
      //          perim = (i & 1) == 0;
      //
      //      }

      DArray pts = new DArray(d2.length);

      for (int j = 0; j < d2.length; j++) {
        FPoint2 samp = new FPoint2();
        pts.add(samp);

        switch (OCentMain.regionType()) {
        case OCentMain.POLYGONS:
          {

            EdPolygon p = (EdPolygon) d2[j];
            //if (perim) 
            {
              int side = MyMath.rnd(p.nPoints());
              if (MyMath.rnd(3) < 2) {
                samp.setLocation(p.getPoint(side));
              } else {
                FPoint2 ept = FPoint2.interpolate(p.getPoint(side), p
                    .getPointMod(side + 1), MyMath.rnd(1.0));
                samp.setLocation(ept);
              }
            }
            //            else {
            //              FRect b = bnds[j];
            //              while (true) {
            //                samp.setLocation(MyMath.rnd(b.width) + b.x, MyMath
            //                    .rnd(b.height)
            //                    + b.y);
            //                if (p.contains(samp))
            //                  break;
            //              }
            //            }
          }
          break;
        default:
          {
            FRect b = bnds[j];

            samp.setLocation(MyMath.rnd(b.width) + b.x, MyMath.rnd(b.height)
                + b.y);
            // if (perim)
            {
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
            
            double range = Math.PI*2;
            double offset = 0;
            
            if (j < 3 && FULL && C.vb(OPTIMIZE)) {
              range = C.vi(OPTRANGE) * (Math.PI/180);
              offset = C.vi(OPTA + j) * (Math.PI/180) - range*.5;
            }
            MyMath.ptOnCircle(disc.getOrigin(), offset+MyMath.rnd(range), disc
                .getRadius(), samp);
          }
          break;
        }
      }

      OneCenterSample s = new OneCenterSample(pts);

      EdDisc theDisc = OneCenterOfPoints.findCenter(pts, null);
      int nDet = s.nCritPoints(); //theDisc.flags();

      if (nDet < minDet || nDet > maxDet)
        continue;

      int ci = nDet - 2;

      if (!FULL || C.vb(MONOCHROME))
        ci = 0;

      Color col = OneCenterSample. sampleColors[ci];
      ptSamples.setColor(col);

      if (C.vb(DISCBND))
        ptSamples.plotCircle(theDisc.getOrigin(), theDisc.getRadius(), s);
      else
        ptSamples.plotDisc(theDisc.getOrigin(), theDisc.getRadius(), s);

      if (!C.vb(TRACKINDEP)) {
        this.lastSample = s;
      }
    }
  }

  public void runAlgorithm() {
    if (C.vb(TRACKINDEP))
      trackIndep();
  }

  private void plotSamplesBehind() {
    T.render(ptSamples);
    //    if (ptSamples != null) {
    //      ptSamples.render();
    //    }

  }
  public void updateThread() {
    int nSamp = thread.processUpdate();
    if (nSamp > 0) {
      run(nSamp);
    }
  }
  public void paintView() {
    if (!threadStarted) {
      thread.setActive(C.vb(THREAD));
      thread.start();
      threadStarted = true;
    }

    updateThread();
    plotSamplesBehind();
    // PossOneCentOper.singleton.plotSamples();
    Editor.render();

    {
      if (FULL && C.vb(MINBOUND)) {
        EdDisc d = OCentMain.getMinBound();
        d.render(MyColor.cRED, STRK_THICK, MARK_X);
      }
    }

    if (C.vb(PLOTLAST))
      T.render(lastSample, MyColor.cDARKGREEN);
    T.render(hoverArc);
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

      this.lastSample = new OneCenterSample(samples);
    } while (false);
  }

  public void clearSamples() {
    lastSample = null;
  }

  private void prepareSamples(EdObject[] d) {
    int res = C.vi(RESOLUTION);
    double gs = res * res * .1;
    if (res > 0) {
      if (ptSamples == null) {
        ptSamples = new BitMap();
      }
      String hash = OCentMain.getHash(d);
      hash = hash + C.vb(WITH2) + C.vb(WITH3);
      ptSamples.prepare(hash, gs);
      ptSamples.addObjectMatrix();
    }
  }

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

//    /**
//     * Notify thread that items have changed (or, an editing operation has begun),
//     * so it should reset the clock to avoid long sample times
//     */
//    public void itemsChanged() {
//      itemsChanged = true;
//      if (db)
//        Streams.out.println("OurThread.itemsChanged");
//    }

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
          nSamples = Math.min((int) (1 + nSamples * 2), 50);
        }
        if (db)
          Streams.out.println("OurThread.processUpdate: returning " + ret
              + " samples");

      }
      return ret;
    }
  }


  private OneCenterSample lastSample;

  private OurThread thread;

  private OneCenterSample hoverArc;
  private boolean threadStarted;
  private BitMap ptSamples;
}
