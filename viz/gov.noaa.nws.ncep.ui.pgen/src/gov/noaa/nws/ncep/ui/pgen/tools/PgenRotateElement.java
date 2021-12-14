/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenRotateElement
 *
 * 6 January 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

import org.geotools.referencing.GeodeticCalculator;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.annotation.Operation;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.TrackExtrapPointInfoDlg;
import gov.noaa.nws.ncep.ui.pgen.display.ISinglePoint;
import gov.noaa.nws.ncep.ui.pgen.display.IText.TextRotation;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.MultiPointElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.elements.Vector;
import gov.noaa.nws.ncep.ui.pgen.filter.OperationFilter;

import org.locationtech.jts.geom.Coordinate;
/**
 * Implements a modal map tool for PGEN object rotat functions.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- ------------------------------------------
 * 01/2010                Mikhail L.  Initial Creation.
 * 06/2010       280                  Moved two methods to PgenToolUtils.
 * 04/2014       TTR900   pswamy      R-click cannot return to SELECT from
 *                                    Rotate and DEL_OBJ
 * Sep 29, 2015  12832    J. Wu       Fix direction-change when moving hash
 *                                    marks.
 * Mar 04, 2016  15238    J. Wu       Fix direction when rotating burbs.
 * Dec 02, 2021  95362    tjensen     Refactor PGEN Resource management to
 *                                    support multi-panel displays
 *
 * </pre>
 *
 * @author Mikhail Laryukhin
 */

public class PgenRotateElement extends AbstractPgenDrawingTool {

    @Override
    protected void activateTool() {

        attrDlg = null;
        if (buttonName == null) {
            buttonName = new String("Select");
        }

        super.activateTool();

    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = new PgenSelectRotateHandler();
        }
        return mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     *
     * @author bingfan, Mikhail L.
     *
     */
    public class PgenSelectRotateHandler extends InputHandlerDefaultImpl {

        OperationFilter rotateFilter = new OperationFilter(Operation.ROTATE);

        /** Attribute dialog for displaying track points info */
        TrackExtrapPointInfoDlg trackExtrapPointInfoDlg = null;

        /** Flag if any point of the element is selected. */
        protected boolean ptSelected = false;

        /** The original direction is needed for undo. */
        private Double oldDir = null; // using Double instead of double because
                                      // we need to use null value

        @Override
        public boolean handleMouseDown(int x, int y, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null || shiftDown) {
                return false;
            }

            if (button == 1) {

                // Return if an element or a point has been selected
                if (ptSelected || drawingLayers.getSelectedDE() != null) {
                    return true;
                }

                // Get the nearest element and set it as the selected element.
                DrawableElement elSelected = drawingLayers
                        .getNearestElement(loc, rotateFilter);

                if (elSelected != null) {
                    drawingLayers.setSelected(elSelected);
                }

                mapEditor.refresh();
                return false;

            } else if (button == 3) {

                if (trackExtrapPointInfoDlg != null) {
                    trackExtrapPointInfoDlg.close();
                    trackExtrapPointInfoDlg = null;
                }

                return false;
            }

            return false;
        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null || shiftDown) {
                return false;
            }

            DrawableElement el = drawingLayers.getSelectedDE();

            // The requirements are to rotate Vector and Text elements only
            // Do not modify other elements
            if (el != null) {
                Coordinate origin = ((ISinglePoint) el).getLocation();

                if (el instanceof Vector) {
                    if (oldDir == null) {
                        oldDir = ((Vector) el).getDirection();
                    }

                    Double newDir = ((Vector) el).vectorDirection(origin, loc);
                    ((Vector) el).setDirection(newDir);

                } else if (el instanceof Text) {
                    if (oldDir == null) {
                        oldDir = ((Text) el).getRotation();
                    }

                    double[] swtCoordinates = mapEditor
                            .translateInverseClick(origin);
                    Double newRotation = 180 - PgenToolUtils.calculateAngle(
                            oldDir, swtCoordinates[0], swtCoordinates[1], x, y);
                    newRotation = PgenToolUtils
                            .transformToRange0To360(newRotation);
                    if (((Text) el)
                            .getRotationRelativity() == TextRotation.NORTH_RELATIVE) {
                        // offset for the location point
                        newRotation -= southOffsetAngle(origin);
                    }
                    ((Text) el).setRotation(newRotation);
                }

                drawingLayers.resetElement(el); // reset display of this element
                mapEditor.refresh();
            }

            return true;
        }

        /**
         * Calculates the angle difference of "south" relative to the screen's
         * y-axis at a given lat/lon location.
         *
         * @param loc
         *            - The point location in Lat/Lon coordinates
         * @return The angle difference of "north" versus pixel coordinate's
         *         y-axis
         */
        private double southOffsetAngle(Coordinate loc) {

            double delta = 0.05;
            /*
             * copy/paste from DisplayElementFactory
             *
             * Calculate points in pixel coordinates just south and north of
             * original location.
             */
            double[] south = { loc.x, loc.y - delta, 0.0 };
            double[] pt1 = drawingLayers.getDescriptor().worldToPixel(south);

            double[] north = { loc.x, loc.y + delta, 0.0 };
            double[] pt2 = drawingLayers.getDescriptor().worldToPixel(north);

            return -90 - toDegrees(atan2((pt2[1] - pt1[1]), (pt2[0] - pt1[0])));
        }

        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            // Finish the editing
            if (button == 1 && drawingLayers != null) {

                // Create a copy of the currently selected element
                DrawableElement el = drawingLayers.getSelectedDE();

                if (el != null && oldDir != null) {

                    DrawableElement newEl = (DrawableElement) el.copy();

                    drawingLayers.resetElement(el);

                    if (el instanceof Vector) {
                        ((Vector) el).setDirection(oldDir);
                        oldDir = null;
                    } else if (el instanceof Text) {
                        ((Text) el).setRotation(oldDir);
                        oldDir = null;
                    }

                    drawingLayers.replaceElement(el, newEl);
                    drawingLayers.setSelected(newEl);

                    mapEditor.refresh();
                }
                return true;
            } else if (button == 3) {

                if (trackExtrapPointInfoDlg != null) {
                    trackExtrapPointInfoDlg.close();
                    trackExtrapPointInfoDlg = null;
                }

                if (drawingLayers.getSelectedDE() != null) {
                    drawingLayers.removeGhostLine();
                    ptSelected = false;
                    drawingLayers.removeSelected();
                    mapEditor.refresh();

                } else {
                    // set selecting mode
                    PgenUtil.setSelectingMode();
                }

                return true;
            } else {
                return false;
            }
        }

        /**
         * Gets the nearest point of an selected element to the input point
         *
         * @param el
         *            element
         * @param pt
         *            input point
         * @return
         */
        protected int getNearestPtIndex(MultiPointElement el, Coordinate pt) {

            int ptId = 0;
            double minDistance = -1;
            GeodeticCalculator gc;
            gc = new GeodeticCalculator(
                    drawingLayers.getCoordinateReferenceSystem());
            gc.setStartingGeographicPoint(pt.x, pt.y);
            int index = 0;
            for (Coordinate elPoint : el.getPoints()) {
                gc.setDestinationGeographicPoint(elPoint.x, elPoint.y);
                double dist = gc.getOrthodromicDistance();
                if (minDistance < 0 || dist < minDistance) {
                    minDistance = dist;
                    ptId = index;
                }
                index++;
            }
            return ptId;
        }
    }
}
