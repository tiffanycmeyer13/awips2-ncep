/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenWatchStatusLineDrawingTool
 *
 * 3 March 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.LineAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.WatchBoxAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox;

/**
 * Implements a modal map tool to draw watch status line.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 03/10                  B. Yin    Initial Creation.
 * 04/11         #?       B. Yin    Re-factor IAttribute
 * 03/12         697      Q. Zhou   Fixed line arrow head size for watch
 * 12/13         TTR 800  B. Yin    Add a flag when opening the specification
 *                                  dialog.
 * May 16, 2016  5640     bsteffen  Access triggering component using PgenUtil.
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenWatchStatusLineDrawingTool extends AbstractPgenDrawingTool {

    // the watch element working on
    private WatchBox wb;

    @Override
    protected void activateTool() {
        super.activateTool();

        LineAttrDlg lineAttrDlg = (LineAttrDlg) attrDlg;
        lineAttrDlg.setSmoothLvl(0);
        lineAttrDlg.setColor(new Color[] { Color.RED, Color.RED });

        AbstractDrawableComponent triggerComponent = PgenUtil
                .getTriggerComponent(event);

        if (triggerComponent instanceof WatchBox) {
            wb = (WatchBox) triggerComponent;
        }

        return;
    }

    @Override
    /**
     * Return the current mouse handler
     */
    public IInputHandler getMouseHandler() {

        if (this.mouseHandler == null) {
            this.mouseHandler = new PgenWatchStatusLineDrawingHandler();
        }

        return this.mouseHandler;
    }

    @Override
    public void deactivateTool() {

        super.deactivateTool();

        PgenWatchStatusLineDrawingHandler wsh = (PgenWatchStatusLineDrawingHandler) mouseHandler;
        if (wsh != null) {
            wsh.clearPoints();
        }

    }

    /**
     * Implements input handler for mouse events.
     *
     * @author bingfan
     *
     */
    private class PgenWatchStatusLineDrawingHandler
            extends InputHandlerDefaultImpl {

        /**
         * Points of the new element.
         */
        protected ArrayList<Coordinate> points = new ArrayList<>();

        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(anX, aY);
            if (loc == null || shiftDown) {
                return false;
            }

            if (button == 1) {
                // add a new point
                points.add(loc);

                return true;
            } else if (button == 3) {
                return true;
            } else if (button == 2) {
                return true;
            } else {
                return false;
            }

        }

        /*
         * overrides the function in selecting tool
         */
        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!drawingLayers.isEditable() || shiftDown) {
                return false;
            }

            if (button == 3) {

                if (points.size() == 0) {

                    // close the line attr dialog
                    if (attrDlg != null) {
                        attrDlg.close();
                    }
                    attrDlg = null;

                    // return to watch modifying tool
                    PgenUtil.loadWatchBoxModifyTool(wb);

                    // set the watch element as selected
                    drawingLayers.setSelected(wb);

                    // open and initialize watch box attr dialog
                    WatchBoxAttrDlg wbdlg = WatchBoxAttrDlg.getInstance(null);
                    wbdlg.openSpecDlg(false);
                    wbdlg.setDrawableElement(wb);
                    wbdlg.setMouseHandlerName("Pgen Select");
                    wbdlg.setAttrForDlg(wb);
                    wbdlg.enableButtons();
                    wbdlg.setPgenCategory(wb.getPgenCategory());
                    wbdlg.setPgenType(wb.getPgenType());
                    wbdlg.setDrawingLayers(drawingLayers);
                    wbdlg.setMapEditor(mapEditor);

                } else if (points.size() < 2) {

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                } else {

                    // create a status line
                    Line statusLine = new Line(null, attrDlg.getColors(),
                            attrDlg.getLineWidth(), 1.0, false, false, points,
                            ((ILine) attrDlg).getSmoothFactor(),
                            FillPattern.SOLID, "Lines", "POINTED_ARROW");

                    // add the line to watch DECollection
                    ((DECollection) wb.getParent()).add(statusLine);

                    drawingLayers.removeGhostLine();
                    points.clear();
                    mapEditor.refresh();

                }
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
            Line ghostLine = new Line(null, attrDlg.getColors(),
                    attrDlg.getLineWidth(), 1.0, false, false, points,
                    ((ILine) attrDlg).getSmoothFactor(), FillPattern.SOLID,
                    "Lines", "POINTED_ARROW");

            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);

                ghostLine.setLinePoints(new ArrayList<>(ghostPts));

                drawingLayers.setGhostLine(ghostLine);
                mapEditor.refresh();

            }

            return false;

        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (!drawingLayers.isEditable() || shiftDown) {
                return false;
            } else {
                return true;
            }
        }

        public void clearPoints() {
            points.clear();
        }

    }

}
