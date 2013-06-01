package ocent;

import java.awt.*;
import java.util.*;
import base.*;
import testbed.*;
/**
 * @deprecated
 */
public class Z_FurthVornOper implements TestBedOperation, Globals {
  /*! .enum  .private 3200
  */

/*!*/
  private Z_FurthVornOper() {
  }
  public static Z_FurthVornOper singleton = new Z_FurthVornOper();

  public void addControls() {
    C.sOpenTab("FVorn");
    {
      C
          .sStaticText("Furthest point Voronoi diagram of axis-aligned rectangles");
    }

    C.sCloseTab();
  }

  private void v2(DArray rects) {

    Hyperbola[] vDiag = VornUtil.buildRects((EdRect[]) rects
        .toArray(EdRect.class), 1, StandardDiscBisector.FARTHEST, 0);

    //    DArray sites = new DArray();
    //    for (int i = 0; i < rects.size(); i++) {
    //      EdRect r = (EdRect) rects.get(i);
    //      FRect f = r.getBounds();
    //      for (int j = 0; j < 4; j++) {
    //        sites.add(new RPoint(r, f.corner(j)));
    //      }
    //    }
    //    EdPoint[] s = (EdPoint[]) sites.toArray(EdPoint.class);
    //    Hyperbola[] vDiag = VornUtil.build(s, 1, StandardDiscBisector.FARTHEST, 0 //
    //        //          | (C.vb(DBVORN) ? VornUtil.FLG_TRACE : 0) //
    //        //          | (C.vb(NOCLIP) ? VornUtil.FLG_NOCLIP : 0) //
    //        //
    //        );
    //
    //    // filter out hyperbolas that bisect corners of same rectangle
    //    {
    //      DArray f = new DArray();
    //      for (int i = 0; i < vDiag.length; i++) {
    //        Hyperbola h = vDiag[i];
    //
    ////        System.out.println("hyp=" + h + "\n data=" + Tools.tv(h.getData(0))
    ////            + "\n dat2=" + Tools.tv(h.getData(1)));
    //        RPoint r0 = (RPoint) h.getData(0);
    //        RPoint r1 = (RPoint) h.getData(1);
    //        if (r0.rect == r1.rect)
    //          continue;
    //        f.add(h);
    //      }
    //      vDiag = (Hyperbola[]) f.toArray(Hyperbola.class);
    //
    //    }

    vornGraph = new VornGraph(vDiag);

  }
  public void runAlgorithm() {
    vornGraph = null;
    //    convexHull = null;
    //    hullPts = null;
    //    vornDiag = null;
    do {
      DArray obj = Editor.readObjects(EdRect.FACTORY, false, true);
      if (obj.size() < 3)
        break;

      v2(obj);

      //      
      //      
      //      calcHull(obj);
      //      buildVorn();

    } while (false);

  }

  //  private void buildVorn() {
  //    final boolean db = true;
  //
  //    vornDiag = new DArray();
  //    DArray hp = new DArray();
  //    for (int i = 0; i < hullPts.size(); i++) {
  //      RPoint pi = (RPoint) hullPts.get(i);
  //      if (hp.isEmpty() || hp.last() != pi.rect)
  //        hp.add(pi.rect);
  //    }
  //
  //    outer: while (hp.size() >= 2) {
  //
  //      if (hp.size() > 1) {
  //        for (int i = 0; i < hp.size(); i++) {
  //          if (hp.get(i) == hp.getMod(i + 1)) {
  //            hp.remove(i);
  //            continue outer;
  //          }
  //        }
  //      }
  //
  //      if (db && T.update())
  //        T.msg("processing hull" + T.showAll(hp, MyColor.cRED));
  //      //      if (db && T.update())
  //      //        T.msg("reduced hull" + T.show(new EdPolygon(hp)));
  //      EdDisc maxdisc = null;
  //      FPoint2 m0, m1, m2;
  //      m0 = null;
  //      m1 = null;
  //      m2 = null;
  //
  //      EdRect pj = null, pk = null;
  //      int k = -1;
  //      for (int j = 0; j < hp.size(); j++) {
  //        pj = (EdRect) hp.get(j);
  //        int l;
  //        for (k = j + 1;; k++) {
  //          if (k == j + hp.size())
  //            break outer;
  //          pk = (EdRect) hp.getMod(k);
  //          if (pk != pj)
  //            break;
  //        }
  //        k = k % hp.size();
  //
  //        EdRect pl;
  //        for (l = k + 1;; l++) {
  //          if (l == j + hp.size())
  //            break outer;
  //          pl = (EdRect) hp.getMod(l);
  //          if (pl != pj && pl != pk)
  //            break;
  //        }
  //        l = l % hp.size();
  //
  //        if (db && T.update())
  //          T.msg("the three rects" + T.show(pj, MyColor.cRED)
  //              + T.show(pk, MyColor.cRED) + T.show(pl, MyColor.cRED));
  //
  //        for (int i0 = 0; i0 < 4; i0++) {
  //          FPoint2 p0 = pj.getBounds().corner(i0);
  //          for (int i1 = 0; i1 < 4; i1++) {
  //            FPoint2 p1 = pk.getBounds().corner(i1);
  //            for (int i2 = 0; i2 < 4; i2++) {
  //              FPoint2 p2 = pl.getBounds().corner(i2);
  //
  //              DArray cc = MyMath.calcCircumCenter(p0, p1, p2);
  //              double rad = cc.getDouble(0);
  //              FPoint2 cent = cc.getFPoint2(1);
  //              EdDisc curr = new EdDisc(cent, rad);
  //              if (false)
  //                if (db && T.update())
  //                  T.msg("disc " + i0 + "" + i1 + "" + i2 + T.show(curr)
  //                      + T.show(maxdisc, MyColor.cPURPLE));
  //              if (maxdisc != null && maxdisc.getRadius() >= curr.getRadius())
  //                continue;
  //              maxdisc = curr;
  //              m0 = p0;
  //              m1 = p1;
  //              m2 = p2;
  //            }
  //          }
  //        }
  //      }
  //
  //      if (k < 0)
  //        break;
  //
  //      if (db && T.update())
  //        T.msg("max disc" + T.show(maxdisc));
  //
  //      if (maxdisc != null) {
  //        vornDiag.add(new EdPoint(maxdisc.getOrigin()));
  //
  //        addSeg(m0, m1, maxdisc);
  //        addSeg(m1, m2, maxdisc);
  //        if (hp.size() == 3)
  //          addSeg(m0, m2, maxdisc);
  //      }
  //
  //      if (db && T.update())
  //        T.msg("removing hull point" + T.show(pk, MyColor.cRED));
  //      hp.remove(k);
  //
  //    }
  //  }
  //  private static class RPoint extends EdPoint {
  //    public RPoint(EdRect rect, IVector s) {
  //      this.addPoint(new FPoint2(s));
  //      this.rect = rect;
  //    }
  //    public EdRect rect;
  //  }
  //  private void calcHull(DArray r) {
  //    DArray pts = new DArray();
  //    for (int i = 0; i < r.size(); i++) {
  //      EdRect rect = (EdRect) r.get(i);
  //      FRect fr = rect.getBounds();
  //      for (int j = 0; j < 4; j++) {
  //        FPoint2 pt = fr.corner(j);
  //        pts.add(new RPoint(rect, pt));
  //      }
  //    }
  //
  //    DArray hi = MyMath.convexHull(pts);
  //    EdPolygon poly = new EdPolygon();
  //    hullPts = new DArray();
  //    for (int i = 0; i < hi.size(); i++) {
  //      int j = hi.getInt(i);
  //      RPoint rp = (RPoint) pts.get(j);
  //      hullPts.add(rp);
  //      poly.addPoint(rp.getPoint(0));
  //    }
  //    convexHull = poly;
  //  }

  public void paintView() {
    Editor.render();
    //    T.show(convexHull, MyColor.cDARKGREEN, STRK_THIN, -1);

    //    T.showAll(vornDiag, MyColor.cPURPLE);
    T.show(vornGraph);
  }
  //  private void addSeg(FPoint2 p0, FPoint2 p1, EdDisc d) {
  //    FPoint2 mid = FPoint2.midPoint(p0, p1);
  //    double th = MyMath.polarAngle(p0, p1) + Math.PI / 2;
  //
  //    FPoint2 s0 = MyMath.ptOnCircle(mid, th, 20);
  //    FPoint2 s1 = MyMath.ptOnCircle(mid, th + Math.PI, 20);
  //    vornDiag.add(new EdSegment(s0, s1));
  //
  //  }
  //
  public void processAction(TBAction a) {
  }
  //  private EdPolygon convexHull;
  //  private DArray hullPts;
  //  private DArray vornDiag;
  private VornGraph vornGraph;

}
