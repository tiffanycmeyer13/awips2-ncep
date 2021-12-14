/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenDeletePointHandler
 *
 * 1 April 2013
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.annotation.Operation;
import gov.noaa.nws.ncep.ui.pgen.display.IMultiPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet;
import gov.noaa.nws.ncep.ui.pgen.elements.MultiPointElement;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox;
import gov.noaa.nws.ncep.ui.pgen.filter.OperationFilter;
import gov.noaa.nws.ncep.ui.pgen.gfa.Gfa;
import gov.noaa.nws.ncep.ui.pgen.gfa.GfaReducePoint;

import org.locationtech.jts.geom.Coordinate;
/**
 * Implements input handler for mouse events for the deleting point action.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 04/13         927      B. Yin    Moved from the PgenDeletePointclass
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author bingfan
 */

public class PgenDeletePointHandler extends PgenSelectHandler {

    private final OperationFilter delPointFilter = new OperationFilter(
            Operation.DELETE_POINT);

    /**
     * Constructor
     * 
     * @param tool
     */
    public PgenDeletePointHandler(AbstractPgenTool tool) {
        super(tool, tool.mapEditor, tool.getDrawingLayers(), null);
    }

    @Override
    public boolean handleMouseDown(int anX, int aY, int button) {
        if (!tool.isResourceEditable()) {
            return false;
        }

        // Check if mouse is in geographic extent
        Coordinate loc = mapEditor.translateClick(anX, aY);
        if (loc == null || shiftDown) {
            return false;
        }

        if (button == 1) {

            if (pgenrscs.getSelectedDE() == null) {

                // Get the nearest element and set it as the selected element.
                DrawableElement elSelected = pgenrscs.getNearestElement(loc,
                        delPointFilter);
                if (elSelected instanceof MultiPointElement
                        && !(elSelected instanceof WatchBox)) {
                    pgenrscs.setSelected(elSelected);
                } else {
                    return false;
                }
            } else if (!ptSelected) {

                // select the nearest point
                ptIndex = getNearestPtIndex(
                        (MultiPointElement) pgenrscs.getSelectedDE(), loc);
                pgenrscs.addPtSelected(ptIndex);
                ptSelected = true;

            } else {

                // remove the selected point
                if (pgenrscs.getSelectedDE() instanceof MultiPointElement) {
                    DrawableElement newEl = (DrawableElement) pgenrscs
                            .getSelectedDE().copy();
                    if (((IMultiPoint) newEl).getLinePoints().length <= 2) {
                        return true;
                    }
                    newEl.getPoints().remove(ptIndex);

                    if (newEl instanceof Gfa) {
                        ((Gfa) newEl)
                                .setGfaVorText(Gfa.buildVorText((Gfa) newEl));
                        GfaReducePoint.WarningForOverThreeLines((Gfa) newEl);
                    }

                    if (newEl instanceof Jet.JetLine) {

                        Jet jet = (Jet) pgenrscs.getActiveLayer()
                                .search(pgenrscs.getSelectedDE());
                        Jet newJet = jet.copy();
                        pgenrscs.replaceElement(jet, newJet);
                        newJet.getPrimaryDE().setPoints(
                                ((MultiPointElement) newEl).getPoints());
                        pgenrscs.setSelected(newJet.getPrimaryDE());

                    } else {
                        pgenrscs.replaceElement(pgenrscs.getSelectedDE(),
                                newEl);
                        pgenrscs.setSelected(newEl);
                    }

                    pgenrscs.removePtsSelected();
                    ptSelected = false;

                    if (!(tool instanceof PgenDeletePoint)) {
                        tool.resetMouseHandler();
                    }
                }

            }

            mapEditor.refresh();
            return true;

        } else if (button == 3) {
            return true;
        } else {

            return false;

        }

    }

    /*
     * overrides the function in selecting tool
     */
    @Override
    public boolean handleMouseDownMove(int anX, int aY, int button) {
        if (!tool.isResourceEditable() || shiftDown) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * overrides the function in selecting tool
     */
    @Override
    public boolean handleMouseUp(int x, int y, int button) {
        if (button == 3) {

            if (pgenrscs.getSelectedDE() != null
                    && tool instanceof PgenDeletePoint) {
                ptSelected = false;
                pgenrscs.removeSelected();
                mapEditor.refresh();
            } else {

                if (tool instanceof PgenDeletePoint) {
                    PgenUtil.setSelectingMode();
                } else {
                    pgenrscs.removePtsSelected();
                    mapEditor.refresh();
                    tool.resetMouseHandler();
                }
            }

            return true;

        } else {
            return false;
        }
    }

    /**
     * Removes the selected element.
     */
    public void cleanup() {
        ptSelected = false;
        pgenrscs.removeSelected();
    }

    /**
     * Sets the nearest point as the point that is going to be deleted.
     */
    @Override
    public void preprocess() {

        Coordinate lastClick = mapEditor.translateClick(
                mapEditor.getActiveDisplayPane().getLastMouseX(),
                mapEditor.getActiveDisplayPane().getLastMouseY());

        ptIndex = getNearestPtIndex(
                (MultiPointElement) pgenrscs.getSelectedDE(), lastClick);
        pgenrscs.addPtSelected(ptIndex);
        ptSelected = true;

        mapEditor.refresh();
    }

}
