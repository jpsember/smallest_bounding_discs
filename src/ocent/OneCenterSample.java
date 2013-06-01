package ocent;

import java.awt.*;
import testbed.*;
import base.*;

/**
 * Objects to be stored with bitmap pixels
 */
public  class OneCenterSample implements Renderable {

  public OneCenterSample(DArray pts) {
    if (pts.size() < 2)
      T.err("insufficient number of points");
    disc = OneCenterOfPoints.findCenter(pts, null);
    critPts = CritPt.constructCriticalSet(pts, disc.getOrigin(), disc
        .getRadius(), true);
  }
  public int nCritPoints() {
    return critPts.size();
  }
  public void render(Color c, int stroke, int markType) {
    V.pushColor(c, MyColor.cRED);
    V.pushStroke(V.STRK_RUBBERBAND);
    V.drawCircle(disc.getOrigin(), disc.getRadius());
    CritPt.plotCriticalSet(critPts, disc.getOrigin(), disc.getRadius());
    V.pop(2);
  }
  private DArray critPts;
  private EdDisc disc;
  public double getRadius() {
    return disc.getRadius();
  }
  public FPoint2 getOrigin() {
    return disc.getOrigin();
  }
  public static Color sampleColors[] = {//
    new Color(.7f, .7f, 1.0f),
    new Color(.6f,.8f,.6f),
//    new Color(1.0f, 1.0f, .8f),
//    new Color(1.0f, .8f, 1.0f), Color.cyan,
//    Color.green,
//    Color.magenta, 
//    Color.orange.darker(), 
    };
}
