/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenModifyTool
 *
 * May 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.rsc.IInputHandler;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.annotation.Operation;
import gov.noaa.nws.ncep.ui.pgen.elements.Arc;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.MultiPointElement;
import gov.noaa.nws.ncep.ui.pgen.filter.OperationFilter;
import gov.noaa.nws.ncep.ui.pgen.gfa.Gfa;
import gov.noaa.nws.ncep.ui.pgen.gfa.GfaReducePoint;

/**
 * Implements a modal map tool for PGEN Line Modification function.
 *
 * Only Line/Front can be selected to be modified now (not Arc).
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer   Description
 * ------------- -------- ---------- -------------------------------------------
 * 05/09         120      J. Wu      Initial Creation.
 * 04/10         165      G. Zhang   Added isModifiableSigmet()
 * 02/12         597      S. Gurung  Removed snapping while modification for all
 *                                   sigmets. Moved snap functionalities to
 *                                   SnapUtil from SigmetInfo.
 * 05/12         808      J. Wu      Update GFA vor text
 * 05/12         610      J. Wu      Add warning when GFA FROM lines > 3
 * 05/12         TTR 998  J. Wu      Use mapDescriptor's pixelToWorld instead of
 *                                   PaneManager's translateClick() to convert
 *                                   point - translateClick() may fail to
 *                                   convert if a point is outside of Grid
 *                                   coverage.
 * 12/14         5413     B. Yin     PGEN in D2D changes.
 * 04/15         6879     J. Wu      Make a local translateClick() without grid
 *                                   range check to fix TTR 998 and make
 *                                   modification work on screen pixels instead
 *                                   of canvas pixels.
 * Dec 02, 2021  95362    tjensen    Refactor PGEN Resource management to
 *                                   support multi-panel displays
 *
 * </pre>
 *
 * @author J. Wu
 */

public class PgenModifyTool extends AbstractPgenTool {

    /**
     * Input handler for mouse events.
     */
    protected IInputHandler modifyHandler = null;

    public PgenModifyTool() {

        super();

    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.modifyHandler == null) {

            this.modifyHandler = new PgenModifyHandler();

        }

        return this.modifyHandler;

    }

    /**
     * Implements input handler for mouse events.
     *
     * @author bingfan
     *
     */
    public class PgenModifyHandler extends InputHandlerDefaultImpl {

        private boolean preempt;

        OperationFilter modifyFilter = new OperationFilter(Operation.MODIFY);

        /**
         * Array list to hold clicked points.
         */
        ArrayList<Coordinate> clickPts = null;

        /**
         * Ghost element that shows the modified element.
         */
        MultiPointElement ghostEl = null;

        /**
         * Instance for performing modification.
         */
        PgenModifyLine pml = null;

        /**
         * Color of the ghost element.
         */
        Color ghostColor = new java.awt.Color(255, 255, 255);

        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            preempt = false;
            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(anX, aY);
            if (loc == null || shiftDown) {
                return false;
            }

            if (button == 1) {

                if (drawingLayers.getSelectedDE() == null) {

                    // Get the nearest element and set it as the selected
                    // element.
                    DrawableElement elSelected = drawingLayers
                            .getNearestElement(loc, modifyFilter);
                    if (((elSelected instanceof Line)
                            && !(elSelected instanceof Arc))
                            || isModifiableSigmet(elSelected)) {
                        drawingLayers.setSelected(elSelected);
                        mapEditor.refresh();
                        preempt = true;
                    } else {
                        return false;
                    }
                } else {
                    preempt = true;

                    if (clickPts == null) {
                        clickPts = new ArrayList<>();
                    }

                    clickPts.add(loc);

                    if (pml == null) {
                        pml = new PgenModifyLine();
                    }

                    pml.setClickPts(latlonToPixel(
                            clickPts.toArray(new Coordinate[clickPts.size()])));

                    ModifyLine();

                    ghostEl.setColors(new Color[] { ghostColor,
                            new java.awt.Color(255, 255, 255) });

                    drawingLayers.setGhostLine(ghostEl);
                    mapEditor.refresh();

                }

                return preempt;

            } else if (button == 3) {
                return true;
            } else {
                return false;
            }

        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null) {
                return false;
            }

            // create the ghost element and put it in the drawing layer
            if (clickPts != null && clickPts.size() >= 1) {

                ArrayList<Coordinate> newPts = new ArrayList<>(clickPts);
                newPts.add(loc);

                pml.setClickPts(latlonToPixel(
                        newPts.toArray(new Coordinate[newPts.size()])));

                ModifyLine();

                ghostEl.setColors(new Color[] { ghostColor,
                        new java.awt.Color(255, 255, 255) });
                drawingLayers.setGhostLine(ghostEl);
                mapEditor.refresh();

            }

            return true;
        }

        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            }

            if (button == 3) {

                if (drawingLayers.getSelectedDE() != null) {

                    if (clickPts != null && !clickPts.isEmpty()) {

                        pml.setClickPts(latlonToPixel(clickPts
                                .toArray(new Coordinate[clickPts.size()])));

                        ModifyLine();

                        if (!(((Line) drawingLayers.getSelectedDE())
                                .isClosedLine()
                                && ghostEl.getLinePoints().length < 3)) {

                            MultiPointElement selected = (MultiPointElement) drawingLayers
                                    .getSelectedDE();

                            if (selected instanceof Jet.JetLine) {

                                Jet jet = (Jet) drawingLayers.getActiveLayer()
                                        .search(selected);
                                Jet newJet = jet.copy();
                                drawingLayers.replaceElement(jet, newJet);
                                newJet.getPrimaryDE()
                                        .setPoints(ghostEl.getPoints());
                                drawingLayers
                                        .setSelected(newJet.getPrimaryDE());
                            } else {
                                MultiPointElement mpe = (MultiPointElement) drawingLayers
                                        .getSelectedDE().copy();

                                drawingLayers.replaceElement(
                                        drawingLayers.getSelectedDE(), mpe);

                                mpe.setPoints(ghostEl.getPoints());
                                if (mpe instanceof Gfa) {
                                    if (((Gfa) mpe).getGfaFcstHr()
                                            .indexOf("-") > -1) {
                                        // snap
                                        ((Gfa) mpe).snap();

                                        GfaReducePoint.WarningForOverThreeLines(
                                                (Gfa) mpe);
                                    }

                                    ((Gfa) mpe).setGfaVorText(
                                            Gfa.buildVorText((Gfa) mpe));
                                }

                                drawingLayers.setSelected(mpe);
                            }
                        }

                        drawingLayers.removeGhostLine();
                        clickPts.clear();

                        mapEditor.refresh();

                    } else {

                        ghostEl = null;

                        drawingLayers.removeGhostLine();
                        drawingLayers.removeSelected();
                        mapEditor.refresh();

                    }

                } else {

                    drawingLayers.removeSelected();
                    mapEditor.refresh();

                    // set selecting mode
                    PgenUtil.setSelectingMode();

                }

                return true;
            }

            else {
                return false;
            }

        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            } else {
                return preempt;
            }
        }

        /**
         * Set up a "modify" instance, perform modification and build a new
         * modified element
         */
        private void ModifyLine() {

            pml.setOriginalPts(latlonToPixel(
                    ((Line) drawingLayers.getSelectedDE()).getLinePoints()));

            pml.setSmoothLevel(
                    ((Line) drawingLayers.getSelectedDE()).getSmoothFactor());

            pml.setClosed(
                    ((Line) drawingLayers.getSelectedDE()).isClosedLine());

            pml.PerformModify();

            buildNewElement();

        }

        /**
         * Converts an array of lat/lons to pixel coordinates
         *
         * @param pts
         *            An array of points in lat/lon coordinates
         * @return The array of points in pixel coordinates
         */
        private double[][] latlonToPixel(Coordinate[] pts) {

            double[] point = new double[2];
            double[][] pixels = new double[pts.length][2];

            int ii = 0;
            for (Coordinate crd : pts) {

                point = mapEditor.translateInverseClick(crd);
                pixels[ii][0] = point[0];
                pixels[ii][1] = point[1];

                ii++;
            }

            return pixels;
        }

        /**
         * Converts an array of pixel coordinates to lat/lons
         *
         * @param pts
         *            An array of points in pixel coordinates
         * @return The array of points in Lat/Lons
         */
        private ArrayList<Coordinate> pixelToLatlon(double[][] pixels) {

            ArrayList<Coordinate> crd = new ArrayList<>();
            for (double[] pixel : pixels) {
                Coordinate pp = translateClick(pixel[0], pixel[1]);
                crd.add(pp);
            }

            return crd;

        }

        /**
         * Translate a current (x,y) screen coordinate to world coordinates.
         *
         * Note: R6879 - this method is the same as PaneManager.translateClick()
         * but without the check for grid range. The check for grid range turns
         * points outside of grid range to null and causes exceptions (TTR998).
         *
         * @param x
         *            a visible x screen coordinate
         * @param y
         *            a visible y screen coordinate
         * @return the lat lon value of the cooordinate
         */
        private Coordinate translateClick(double x, double y) {
            IDisplayPane pane = mapEditor.getActiveDisplayPane();

            // Convert the screen coordinates to grid space
            double[] world = pane.screenToGrid(x, y, 0);

            if (world == null) {
                return null;
            }

            // use descriptor to convert pixel world to CRS world space
            world = pane.getDescriptor().pixelToWorld(world);

            // Check for null
            if (world == null) {
                return null;
            }

            return new Coordinate(world[0], world[1], world[2]);
        }

        /**
         * Build a new modified DrawableElement from a set of lat/lons
         *
         * @param pts
         *            An array of points in pixel coordinates
         * @return The array of points in Lat/Lons
         */
        private void buildNewElement() {

            ghostEl = (MultiPointElement) (drawingLayers.getSelectedDE()
                    .copy());

            if (ghostEl != null && pml.getModifiedPts() != null
                    && pml.getModifiedPts().length > 1) {

                ghostEl.setLinePoints(pixelToLatlon(pml.getModifiedPts()));

                if (((Line) drawingLayers.getSelectedDE()).isClosedLine()) {
                    if (pml.getModifiedPts().length < 3) {
                        ghostEl.setClosed(false);
                    }
                }
            }
        }

    }

    /**
     * check if the DE is modifiable Sigmet.
     *
     * @param DrawableElement
     *            : DE to be checked.
     * @return boolean: true: the Sigmet is Modifiable.
     */
    private boolean isModifiableSigmet(DrawableElement el) {

        if (el instanceof gov.noaa.nws.ncep.ui.pgen.sigmet.Sigmet) {
            gov.noaa.nws.ncep.ui.pgen.sigmet.Sigmet sig = (gov.noaa.nws.ncep.ui.pgen.sigmet.Sigmet) el;

            if (!sig.getType().contains("Text")
                    && !sig.getType().contains("Isolated")) {
                return true;
            }
        }

        return false;
    }

}