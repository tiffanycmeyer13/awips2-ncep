/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenWatchBoxDrawingTool
 *
 * 5 December 2008
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.edex.common.stationTables.Station;
import gov.noaa.nws.ncep.edex.common.stationTables.StationTable;
import gov.noaa.nws.ncep.ui.pgen.PgenStaticDataProvider;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.WatchBoxAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox.WatchShape;

/**
 * Implements a modal map tool to draw PGEN watch boxes.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 09/09                  B. Yin    Initial Creation.
 * 03/10                  B. Yin    Put watch element in a DECollection
 * 04/11         ?        B. Yin    load modify-tool once the 2nd point dropped
 * 07/11         ?        B. Yin    Pre-load county table
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenWatchBoxDrawingTool extends AbstractPgenDrawingTool {

    /**
     * public constructor
     */
    public PgenWatchBoxDrawingTool() {

        super();

    }

    @Override
    protected void activateTool() {
        super.activateTool();
        if (super.isDelObj()) {
            return;
        }

        if (attrDlg != null) {
            ((WatchBoxAttrDlg) attrDlg).enableDspBtn(false);
        }

        new Thread() {
            @Override
            public void run() {
                PgenStaticDataProvider.getProvider().getSPCCounties();
            }
        }.start();

        return;

    }

    @Override
    public void deactivateTool() {

        super.deactivateTool();

        PgenWatchBoxDrawingHandler wbh = (PgenWatchBoxDrawingHandler) mouseHandler;
        if (wbh != null) {
            wbh.clearPoints();
        }

    }

    /**
     * Returns the current mouse handler.
     * 
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.mouseHandler == null) {
            this.mouseHandler = new PgenWatchBoxDrawingHandler();
        }

        return this.mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     * 
     * @author bingfan
     *
     */

    private class PgenWatchBoxDrawingHandler extends InputHandlerDefaultImpl {

        /**
         * Points of the new watch box.
         */
        private final ArrayList<Coordinate> points = new ArrayList<>();

        /**
         * An instance of DrawableElementFactory, which is used to create a new
         * watch box.
         */
        private final DrawableElementFactory def = new DrawableElementFactory();

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

                /*
                 * Select watch box shape first.
                 */
                if (((WatchBoxAttrDlg) attrDlg).getWatchBoxShape() == null) {
                    MessageDialog infoDlg = new MessageDialog(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                    .getShell(),
                            "Information", null,
                            "Please select a watch shape before drawing.",
                            MessageDialog.INFORMATION, new String[] { "OK" },
                            0);

                    infoDlg.open();
                } else {
                    points.add(loc);
                }

                return true;

            } else if (button == 3) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            if (button == 1) {
                if (points != null && points.size() == 2) {

                    WatchShape ws = ((WatchBoxAttrDlg) attrDlg)
                            .getWatchBoxShape();

                    /*
                     * Get all anchor points in the polygon.
                     */
                    ArrayList<Station> anchorsInPoly = getAnchorsInPoly(
                            mapEditor,
                            WatchBox.generateWatchBoxPts(ws,
                                    WatchBox.HALF_WIDTH * PgenUtil.SM2M,
                                    points.get(0), points.get(1)));

                    /*
                     * If there is no anchor points in the polygon, pop up a
                     * warning message. Otherwise, get the two closest anchor
                     * points and generate the eight points of the watch box.
                     */
                    if (anchorsInPoly == null || anchorsInPoly.isEmpty()) {
                        MessageDialog infoDlg = new MessageDialog(
                                PlatformUI.getWorkbench()
                                        .getActiveWorkbenchWindow().getShell(),
                                "Information", null,
                                "No anchor point in the area!",
                                MessageDialog.INFORMATION,
                                new String[] { "OK" }, 0);

                        infoDlg.open();
                    } else {

                        DECollection dec = def.createWatchBox(pgenCategory,
                                pgenType, ws, points.get(0), points.get(1),
                                anchorsInPoly, attrDlg);
                        if (dec != null) {
                            drawingLayers.addElement(dec);

                            // load modify tool
                            ((WatchBoxAttrDlg) attrDlg).enableDspBtn(true);
                            ((WatchBoxAttrDlg) attrDlg)
                                    .setWatchBox((WatchBox) dec.getPrimaryDE());
                            points.clear();
                            PgenUtil.loadWatchBoxModifyTool(dec.getPrimaryDE());
                        }

                    }

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();
                }
                return true;
            } else if (button == 3) {

                if (points.size() == 0) {

                    attrDlg.close();
                    attrDlg = null;
                    PgenUtil.setSelectingMode();

                } else if (points.size() < 2) {

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

            // create the ghost line and put it in the drawing layer
            AbstractDrawableComponent ghost = def.create(DrawableType.LINE,
                    attrDlg, "Line", "LINE_SOLID", points,
                    drawingLayers.getActiveLayer());

            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(
                        points);
                ghostPts.add(loc);
                ((Line) ghost)
                        .setLinePoints(new ArrayList<>(ghostPts));

                drawingLayers.setGhostLine(ghost);
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

        /**
         * Remove all points from the mouse handler.
         */
        public void clearPoints() {
            points.clear();
        }

    }

    /**
     * Get a list of anchor points in the polygon.
     * 
     * @param editor
     *            - map editor
     * @param polyPoints
     *            - list of coordinates of the polygon
     * @return - list of anchor points in the polygon
     */
    public static ArrayList<Station> getAnchorsInPoly(AbstractEditor editor,
            List<Coordinate> polyPoints) {

        ArrayList<Station> stnList = new ArrayList<>();

        // construct the polygon
        int[] xpoints = new int[polyPoints.size()];
        int[] ypoints = new int[polyPoints.size()];
        for (int ii = 0; ii < polyPoints.size(); ii++) {
            double pt[] = editor.translateInverseClick(polyPoints.get(ii));
            xpoints[ii] = (int) pt[0];
            ypoints[ii] = (int) pt[1];
        }

        Polygon poly = new Polygon(xpoints, ypoints, polyPoints.size());

        // get a list of all anchor points
        StationTable anchorTbl = PgenStaticDataProvider.getProvider()
                .getAnchorTbl();
        List<Station> anchorList = anchorTbl.getStationList();

        // if an anchor point is in the polygon, add it to the return list.
        for (Station stn : anchorList) {
            double loc[] = editor.translateInverseClick(
                    new Coordinate(stn.getLongitude(), stn.getLatitude()));
            if (poly.contains(loc[0], loc[1])) {
                stnList.add(stn);
            }
        }

        return stnList;
    }

}
