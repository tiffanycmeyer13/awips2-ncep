/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenMoveElement
 *
 * 22 April 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.Iterator;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox;
import gov.noaa.nws.ncep.ui.pgen.gfa.Gfa;
import gov.noaa.nws.ncep.ui.pgen.gfa.GfaReducePoint;
import gov.noaa.nws.ncep.ui.pgen.sigmet.SigmetInfo;
import gov.noaa.nws.ncep.viz.common.SnapUtil;

import org.locationtech.jts.geom.Coordinate;
/**
 * Implements a modal map tool for the PGEN copy element function.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer   Description
 * ------------- -------- ---------- -------------------------------------------
 * 04/09         78       B. Yin     Initial Creation.
 * 06/09         116      B. Yin     Use AbstractDrawingComponent
 * 02/12         597      S. Gurung  Moved snap functionalities to SnapUtil from
 *                                   SigmetInfo.
 * 02/12                  S. Gurung  Moved isSnapADC() and getNumOfCompassPts()
 *                                   to SigmetInfo.
 * 05/11         808      J. Wu      Update Gfa vor text
 * 05/12         610      J. Wu      Add warning when GFA FROM lines > 3
 * 12/14         5413     B. Yin     PGEN in D2D Changes.
 * Jun 15, 2016  13559    bkowal     File cleanup. No longer simulate mouse
 *                                   clicks.
 * Dec 02, 2021  95362    tjensen    Refactor PGEN Resource management to
 *                                   support multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenMoveElement extends PgenCopyElement {

    /**
     * Input handler for mouse events.
     */
    protected IInputHandler moveHandler = null;

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.moveHandler == null) {
            this.moveHandler = new PgenMoveHandler();
        }

        return this.moveHandler;

    }

    public class PgenMoveHandler extends PgenCopyElement.PgenCopyHandler {

        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            }

            if (button == 3) {

                if (drawingLayers.getSelectedComp() != null) {
                    // de-select element
                    drawingLayers.removeSelected();
                    drawingLayers.removeGhostLine();
                    ghostEl = null;
                    mapEditor.refresh();
                } else {
                    // set selecting mode
                    PgenUtil.setSelectingMode();
                }

                return true;

            }

            if (ghostEl != null) {

                AbstractDrawableComponent comp = drawingLayers
                        .getSelectedComp();
                // reset color for the el and add it to PGEN resource
                Iterator<DrawableElement> it1 = comp.createDEIterator();
                Iterator<DrawableElement> it2 = ghostEl.createDEIterator();

                while (it1.hasNext() && it2.hasNext()) {
                    it2.next().setColors(it1.next().getColors());
                }

                if (!(ghostEl instanceof WatchBox)
                        || ((ghostEl instanceof WatchBox)
                                && PgenWatchBoxModifyTool.resnapWatchBox(
                                        mapEditor, (WatchBox) ghostEl,
                                        (WatchBox) ghostEl))) {

                    if (SigmetInfo.isSnapADC(ghostEl)) {
                        java.util.ArrayList<Coordinate> list = SnapUtil
                                .getSnapWithStation(ghostEl.getPoints(),
                                        SnapUtil.VOR_STATION_LIST, 10,
                                        SigmetInfo.getNumOfCompassPts(ghostEl));
                        AbstractDrawableComponent ghostElCp = ghostEl.copy();
                        ((DrawableElement) ghostElCp).setPoints(list);

                        drawingLayers.replaceElement(comp, ghostElCp);
                        drawingLayers.setSelected(ghostElCp);
                    } else if (ghostEl instanceof Gfa) {

                        if (((Gfa) ghostEl).getGfaFcstHr().indexOf("-") > -1) {
                            // snap
                            ((Gfa) ghostEl).snap();
                            GfaReducePoint
                                    .WarningForOverThreeLines((Gfa) ghostEl);
                        }

                        ((Gfa) ghostEl)
                                .setGfaVorText(Gfa.buildVorText((Gfa) ghostEl));

                        drawingLayers.replaceElement(comp, ghostEl);
                        drawingLayers.setSelected(ghostEl);

                    }

                    else {
                        drawingLayers.replaceElement(comp, ghostEl);
                        drawingLayers.setSelected(ghostEl);
                    }

                }

                drawingLayers.removeGhostLine();
                ghostEl = null;
                mapEditor.refresh();

            }

            return true;

        }
    }
}
