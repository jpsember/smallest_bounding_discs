package ocent;

import java.awt.*;
import java.util.*;
import base.*;
import testbed.*;

public class OneCenterOper implements TestBedOperation, Globals {
  /*! .enum  .private 3200
    
  */
/*!*/
  private OneCenterOper() {
  }
  public static OneCenterOper singleton = new OneCenterOper();

  public void addControls() {
    C.sOpenTab("1C");
    {
      C.sStaticText("1-center of points");
    }

    C.sCloseTab();
  }

  public void runAlgorithm() {
    disc = null;
   DArray pts = getSegs();
    if (pts.size() > 1) {
   disc =    OneCenterOfPoints.findCenter(pts, null);
    }
  }

  public void paintView() {
    T.render(disc, MyColor.cRED, STRK_THIN, MARK_X);
    Editor.render();
  }
  public void processAction(TBAction a) {
  }


  private DArray  getSegs() {
    DArray ptObj = Editor.readObjects(EdPoint.FACTORY, false, true);
    DArray pts = new DArray();
    for (int i = 0; i < ptObj.size(); i++) {
      EdPoint s = (EdPoint) ptObj.get(i);
      pts.add(s.getPoint(0));
    }
    return pts;
  }
  private EdDisc disc;
}
