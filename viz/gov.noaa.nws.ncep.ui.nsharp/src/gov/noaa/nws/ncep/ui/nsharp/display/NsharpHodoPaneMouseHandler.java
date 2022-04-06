/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 *
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 *
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.nws.ncep.ui.nsharp.display;

import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;
import gov.noaa.nws.ncep.ui.nsharp.display.rsc.NsharpHodoPaneResource;
import gov.noaa.nws.ncep.ui.nsharp.view.NsharpShowTextDialog;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IView;
import com.raytheon.uf.viz.core.exception.VizException;
import org.locationtech.jts.geom.Coordinate;

/**
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------      --------   -----------
 * 04/23/2012   229         Chin Chen   Initial coding
 * 03/15/2018   6792        bsteffen    Stop stealing focus from other windows.
 * 04/16/2020   73571       smanoj      NSHARP D2D port refactor
 * 04/01/2022   89212       smanoj      Fix some Nsharp display issues.
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpHodoPaneMouseHandler extends NsharpAbstractMouseHandler {
    private boolean cursorInHodo = false;

    public NsharpHodoPaneMouseHandler(NsharpEditor editor, IDisplayPane pane) {
        super(editor, pane);
    }

    @Override
    public boolean handleMouseWheel(Event event, int x, int y) {

        if (editor == null || cursorInPane == false) {
            return false;
        }

        com.raytheon.viz.ui.input.preferences.MouseEvent SCROLL_FORWARD = com.raytheon.viz.ui.input.preferences.MouseEvent.SCROLL_FORWARD;
        com.raytheon.viz.ui.input.preferences.MouseEvent SCROLL_BACK = com.raytheon.viz.ui.input.preferences.MouseEvent.SCROLL_BACK;
        if ((event.count < 0
                && prefManager.handleEvent(ZOOMIN_PREF, SCROLL_FORWARD))
                || (event.count > 0 && prefManager.handleEvent(ZOOMOUT_PREF,
                        SCROLL_BACK))) {
            currentPane.zoom(event.count, event.x, event.y);

            editor.refresh();
        }
        return true;
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        theLastMouseX = x;
        theLastMouseY = y;
        if (getPaneDisplay() == null) {
            return false;
        } else if (mouseButton == 1) {
            this.mode = Mode.CREATE;
            Coordinate c = editor.translateClick(x, y);
            NsharpHodoPaneResource hodoRsc = (NsharpHodoPaneResource) getDescriptor()
                    .getPaneResource();
            boolean graphEditOn = hodoRsc.getRscHandler().isEditGraphOn();
            if (hodoRsc.getHodoBackground().contains(c) == true) {

                if (graphEditOn) {
                    Point curPoint = display.getCursorLocation();
                    int xdiff = x - curPoint.x;
                    int ydiff = y - curPoint.y;
                    Coordinate anchoredPtC = hodoRsc.getRscHandler()
                            .getClosestHodoPoint(c);
                    if (anchoredPtC.x != 0 && anchoredPtC.y != 0) {
                        anchorPointxy = editor
                                .translateInverseClick(anchoredPtC);
                        display.setCursorLocation(
                                (int) anchorPointxy[0] - xdiff,
                                (int) anchorPointxy[1] - ydiff);
                        this.mode = Mode.HODO_DOWN_MOVE;
                    }
                } else {
                    this.mode = Mode.HODO_DOWN;
                }
            }
            editor.refresh();
        }

        return false;
    }

    @Override
    public boolean handleMouseDownMove(int aX, int aY, int button) {

        if (getPaneDisplay() == null) {
            return false;
        } else if (button == 1) {

            Coordinate c = editor.translateClick(aX, aY);
            // make sure it is clicked within skewt area
            NsharpHodoPaneResource hodoRsc = (NsharpHodoPaneResource) getDescriptor()
                    .getPaneResource();
            boolean graphEditOn = hodoRsc.getRscHandler().isEditGraphOn();
            if (this.mode == Mode.HODO_DOWN_MOVE && graphEditOn) {
                if (hodoRsc.getHodoBackground().contains(c) == true) {
                    c = editor.translateClick(aX, aY);
                    hodoRsc.getRscHandler()
                            .setInteractiveHodoPointCoordinate(c);
                    editor.refresh();

                }
                return false;
            }
            if (prefManager.handleLongClick(ZOOMIN_PREF, button)
                    || prefManager.handleLongClick(ZOOMOUT_PREF, button)) {
                theLastMouseX = aX;
                theLastMouseY = aY;
            }
            if ((!prefManager.handleDrag(PAN_PREF, button))
                    || currentPane == null)
                return false;
            IView tmpView = (IView) currentPane.getRenderableDisplay().getView()
                    .clone();
            tmpView.shiftExtent(new double[] { aX, aY },
                    new double[] { theLastMouseX, theLastMouseY },
                    currentPane.getTarget());
            IExtent tmpExtent = tmpView.getExtent();
            double percentage = getPanningPercentage();
            double xMinThreshold = tmpExtent.getMinX()
                    + (tmpExtent.getMaxX() - tmpExtent.getMinX()) * percentage;
            double xMaxThreshold = tmpExtent.getMinX()
                    + (tmpExtent.getMaxX() - tmpExtent.getMinX())
                            * (1.0 - percentage);
            double yMinThreshold = tmpExtent.getMinY()
                    + (tmpExtent.getMaxY() - tmpExtent.getMinY()) * percentage;
            double yMaxThreshold = tmpExtent.getMinY()
                    + (tmpExtent.getMaxY() - tmpExtent.getMinY())
                            * (1.0 - percentage);

            double height = currentPane.getRenderableDisplay().getWorldHeight();
            double width = currentPane.getRenderableDisplay().getWorldWidth();

            int aX2 = aX, aY2 = aY;

            if ((0 <= xMinThreshold && width >= xMaxThreshold) == false) {
                if (((width < xMaxThreshold && theLastMouseX < aX)
                        || (0 > xMinThreshold
                                && theLastMouseX > aX)) == false) {
                    aX2 = (int) theLastMouseX;
                }
            }

            if ((0 <= yMinThreshold && height >= yMaxThreshold) == false) {
                if (((height < yMaxThreshold && theLastMouseY < aY)
                        || (0 > yMinThreshold
                                && theLastMouseY > aY)) == false) {
                    aY2 = (int) theLastMouseY;
                }
            }

            if (aX2 != theLastMouseX || aY2 != theLastMouseY) {
                currentPane.shiftExtent(new double[] { aX2, aY2 },
                        new double[] { theLastMouseX, theLastMouseY });
            }
            theLastMouseX = aX;
            theLastMouseY = aY;
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        if (getPaneDisplay() == null) {
            return false;
        }
        this.mode = Mode.CREATE;
        if (editor != null) {
            Coordinate c = editor.translateClick(x, y);
            NsharpHodoPaneResource hodoRsc = (NsharpHodoPaneResource) getDescriptor()
                    .getPaneResource();
            if (hodoRsc.getHodoBackground().contains(c)) {
                cursorInHodo = true;
                hodoRsc.setCursorInHodo(true);
                try {
                    hodoRsc.updateDynamicData(c);
                    editor.refresh();
                } catch (VizException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                if (cursorInHodo == true) {
                    cursorInHodo = false;
                    hodoRsc.setCursorInHodo(false);
                }
            }
        }
        return false;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {
        if (getPaneDisplay() == null) {
            return false;
        }
        if (editor != null) {
            // button 1 is left mouse button
            if (mouseButton == 1) {
                NsharpHodoPaneResource hodoRsc = (NsharpHodoPaneResource) getDescriptor()
                        .getPaneResource();
                Coordinate c = editor.translateClick(x, y);
                if (hodoRsc.getHodoBackground().contains(c) == true) {
                    // make sure it is clicked within hodo area
                    boolean graphEditOn = hodoRsc.getRscHandler()
                            .isEditGraphOn();
                    if (graphEditOn && this.mode == Mode.HODO_DOWN_MOVE) {
                        NsharpShowTextDialog osDia = NsharpShowTextDialog
                                .getAccess();
                        if (osDia != null)
                            osDia.refreshTextData();
                    } else if (this.mode == Mode.HODO_DOWN) {
                        hodoRsc.getRscHandler().setHodoStmCenter(c);
                    }
                }
                this.mode = Mode.CREATE;
            } else if (mouseButton == 3) {
                // right mouse button
                if (AbstractNsharpMapResource.getMapResource(editor) != null) {
                    AbstractNsharpMapResource.getMapResource(editor)
                            .bringMapEditorToTop();
                }
            }
            editor.refresh();
        }
        return false;
    }
}
