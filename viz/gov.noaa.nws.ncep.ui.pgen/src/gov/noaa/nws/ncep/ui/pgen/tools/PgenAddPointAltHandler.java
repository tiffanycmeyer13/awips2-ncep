/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenAddPointAltHandler
 *
 * 1 April 2013
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.annotation.Operation;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.filter.OperationFilter;
import gov.noaa.nws.ncep.ui.pgen.gfa.Gfa;
import gov.noaa.nws.ncep.ui.pgen.gfa.GfaReducePoint;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * Implements input handler for mouse events for the adding point action.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 04/13         927      B. Yin    Moved from the PgenAddPointAlt class
 * Dec 01, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author sgilbert
 */

public class PgenAddPointAltHandler extends InputHandlerDefaultImpl {

    /**
     * Possible states to be in when adding a point to an element
     *
     * @author sgilbert
     */
    private enum ADD_STATUS {
        START, SELECTED, MOVING
    }

    protected AbstractEditor mapEditor;

    protected PgenResourceList pgenrscs;

    protected AttrDlg attrDlg;

    protected AbstractPgenTool tool;

    private boolean preempt;

    private ADD_STATUS status = ADD_STATUS.START;

    private DrawableElement newEl;

    Map<Coordinate, Integer> newPoints = null;

    private Integer index = null;

    private final DECollection ghost = new DECollection();

    Color ghostColor = new Color(255, 255, 255);

    private final Symbol marker = new Symbol(null,
            new Color[] { new Color(255, 0, 0) }, 2.5f, 1.0, false, null,
            "Marker", "BOX");

    OperationFilter addPointFilter = new OperationFilter(Operation.ADD_POINT);

    /**
     * Constructor
     *
     * @param tool
     */
    public PgenAddPointAltHandler(AbstractPgenTool tool) {
        this.mapEditor = tool.mapEditor;
        this.pgenrscs = tool.getDrawingLayers();
        this.tool = tool;
    }

    @Override
    public boolean handleMouseDown(int anX, int aY, int button) {
        if (!tool.isResourceEditable()) {
            return false;
        }

        preempt = false;

        // Check if mouse is in geographic extent
        Coordinate loc = mapEditor.translateClick(anX, aY);
        if (loc == null || shiftDown) {
            return false;
        }

        if (button == 1) {

            switch (status) {

            case START:
                /*
                 * User selects an Element to alter
                 */
                AbstractDrawableComponent elSelected = pgenrscs
                        .getNearestElement(loc, addPointFilter);

                if (elSelected == null) {
                    return false;
                }
                pgenrscs.setSelected(elSelected);

                /*
                 * virtual points are calculated for each segment and displayed
                 * as ghosts.
                 */
                newPoints = calculateNewPoints(pgenrscs.getSelectedDE());
                ghost.clear();
                for (Coordinate coord : newPoints.keySet()) {
                    Symbol sym = new Symbol(null,
                            new Color[] { new Color(255, 0, 0) }, 2.5f, 1.0,
                            false, coord, "Marker", "BOX");
                    ghost.add(sym);
                }
                pgenrscs.setGhostLine(ghost);

                status = ADD_STATUS.SELECTED;
                break;

            case SELECTED:
                /*
                 * Check to see if user has selected a virtual point for
                 * dragging...
                 */
                index = null;
                if (newPoints != null) {
                    for (Coordinate coord : newPoints.keySet()) {
                        if (loc.distance(coord) < 0.2) {
                            index = newPoints.get(coord);
                            status = ADD_STATUS.MOVING;
                            preempt = true;
                            break;
                        }
                    }
                }
                break;
            }

            mapEditor.refresh();
            return preempt;

        } else if (button == 3) {
            return true;
        }

        else {

            return false;

        }

    }

    /*
     * overrides the function in selecting tool
     */
    @Override
    public boolean handleMouseDownMove(int anX, int aY, int button) {

        if (!tool.isResourceEditable() || button != 1 || shiftDown) {
            return false;
        }

        Coordinate loc = mapEditor.translateClick(anX, aY);
        if (loc == null) {
            return false;
        }

        switch (status) {

        case MOVING:
            /*
             * ghost current marker location
             */
            ghost.clear();
            marker.setLocation(loc);
            ghost.add(marker);

            /*
             * ghost line with new point added
             */
            newEl = addPointToElement(loc, index, pgenrscs.getSelectedDE());
            newEl.setColors(new Color[] { ghostColor, ghostColor });
            ghost.add(newEl);
            pgenrscs.setGhostLine(ghost);
            break;
        }

        mapEditor.refresh();
        return preempt;
    }

    /*
     * overrides the function in selecting tool
     */
    @Override
    public boolean handleMouseUp(int anX, int aY, int button) {

        if (!tool.isResourceEditable()) {
            return false;
        }

        if (button == 1) {
            Coordinate loc = mapEditor.translateClick(anX, aY);
            if (loc == null) {
                return false;
            }

            switch (status) {

            case MOVING:
                /*
                 * mouse up. this is location of new point. Create new element
                 * with new point added. and replace the old element in the
                 * resource with the new one.
                 */
                pgenrscs.removeGhostLine();
                AbstractDrawableComponent newComp = addPointToElement(loc,
                        index, pgenrscs.getSelectedComp());
                pgenrscs.replaceElement(pgenrscs.getSelectedComp(), newComp);

                if (newComp instanceof Gfa) {
                    if (((Gfa) newComp).getGfaFcstHr().indexOf("-") > -1) {
                        // snap
                        ((Gfa) newComp).snap();
                        GfaReducePoint.WarningForOverThreeLines((Gfa) newComp);
                    }

                    ((Gfa) newComp)
                            .setGfaVorText(Gfa.buildVorText((Gfa) newComp));
                }

                if (tool instanceof PgenAddPointAlt) {
                    pgenrscs.removeSelected();
                    status = ADD_STATUS.START;
                } else {
                    tool.resetMouseHandler();
                    pgenrscs.removeSelected();
                    pgenrscs.setSelected(pgenrscs.getNearestElement(loc));
                    tool.setWorkingComponent(pgenrscs.getNearestComponent(loc));
                }

                mapEditor.refresh();
                break;
            }

            return false;
        } else if (button == 3) {

            switch (status) {

            case SELECTED:

                if (tool instanceof PgenAddPointAlt) {
                    /*
                     * start over
                     */
                    pgenrscs.removeGhostLine();
                    pgenrscs.removeSelected();
                    status = ADD_STATUS.START;
                    mapEditor.refresh();
                } else {
                    pgenrscs.removeGhostLine();
                    tool.resetMouseHandler();
                }

                break;
            default:
                PgenUtil.setSelectingMode();
            }

            return true;

        } else {
            return false;
        }
    }

    /*
     * Create a new Drawable Element from the given DE with the new point added
     * to the given segment.
     */
    private DrawableElement addPointToElement(Coordinate point, Integer index,
            DrawableElement selectedDE) {

        DrawableElement newEl = (DrawableElement) selectedDE.copy();

        ArrayList<Coordinate> pts = new ArrayList<>(selectedDE.getPoints());
        pts.add(index, point);

        newEl.setPoints(pts);
        return newEl;

    }

    /*
     * Create a new Drawable Element from the given DE with the new point added
     * to the given segment. Note that Jet does not extend from DrawableElement
     * and must be treated differently
     */
    private AbstractDrawableComponent addPointToElement(Coordinate point,
            Integer index, AbstractDrawableComponent elem) {

        if (elem instanceof DrawableElement) {
            DrawableElement de = (DrawableElement) elem;
            return addPointToElement(point, index, de);
        } else if (elem instanceof Jet) {
            Jet newEl = ((Jet) elem).copy();

            ArrayList<Coordinate> pts = new ArrayList<>(
                    elem.getPrimaryDE().getPoints());
            pts.add(index, point);

            newEl.getPrimaryDE().setPoints(pts);
            return newEl;
        }
        return elem;

    }

    /*
     * Returns a map of "virtual" Coordinates along with their segment index
     * representing the midpoints (and maybe endpoints) for each segment of the
     * given DrawableElement.
     */
    private Map<Coordinate, Integer> calculateNewPoints(
            DrawableElement elSelected) {

        Map<Coordinate, Integer> newLocs = new HashMap<>();
        List<Coordinate> points = new ArrayList<>();

        /*
         * Convert Lat/Lon to pixel coordinates
         */
        for (Coordinate c : elSelected.getPoints()) {
            double[] tmp = mapEditor.translateInverseClick(c);
            points.add(new Coordinate(tmp[0], tmp[1]));
        }

        /*
         * calculate midpoints
         */
        for (int i = 0; i < elSelected.getPoints().size() - 1; i++) {
            LineSegment ls = new LineSegment(points.get(i), points.get(i + 1));
            newLocs.put(toLatLon(ls.midPoint()), i + 1);
        }

        if (!((ILine) elSelected).isClosedLine()) {
            Coordinate prev = points.get(0);
            newLocs.put(toLatLon(prev), 0);

            Coordinate next = points.get(points.size() - 1);
            newLocs.put(toLatLon(next), points.size());
        } else {
            LineSegment ls = new LineSegment(points.get(0),
                    points.get(points.size() - 1));
            newLocs.put(toLatLon(ls.midPoint()), points.size());
        }

        return newLocs;
    }

    /*
     * Convert pixel coordinate to Lat/Lon coordinate
     */
    private Coordinate toLatLon(Coordinate pixel) {
        return mapEditor.translateClick(pixel.x, pixel.y);
    }

    @Override
    public void preprocess() {
        newPoints = calculateNewPoints(pgenrscs.getSelectedDE());
        ghost.clear();
        for (Coordinate coord : newPoints.keySet()) {
            Symbol sym = new Symbol(null, new Color[] { new Color(255, 0, 0) },
                    2.5f, 1.0, false, coord, "Marker", "BOX");
            ghost.add(sym);
        }
        pgenrscs.setGhostLine(ghost);

        status = ADD_STATUS.SELECTED;
    }

    public AbstractEditor getMapEditor() {
        return mapEditor;
    }

    public PgenResourceList getPgenrsc() {
        return pgenrscs;
    }

}
