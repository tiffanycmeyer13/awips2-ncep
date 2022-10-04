package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

import gov.noaa.nws.ncep.ui.pgen.PgenStaticDataProvider;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

/**
 *
 * TODO Add Description
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * ???                                 Initial creation
 * Dec 02, 2021 95362      tjensen     Refactor PGEN Resource management to support multi-panel
 *
 * </pre>
 *
 * @author tjensen
 */
public class PgenSpenesDrawingTool extends AbstractPgenDrawingTool {

    public PgenSpenesDrawingTool() {

        super();

    }

    /*
     * Invoked by the CommandService when starting this tool
     *
     * @see com.raytheon.viz.ui.tools.AbstractTool#runTool()
     */
    @Override
    protected void activateTool() {

        super.activateTool();
        new Thread() {
            @Override
            public void run() {
                PgenStaticDataProvider.getProvider().loadCwaTable();
                PgenStaticDataProvider.getProvider().loadStateTable();
                PgenStaticDataProvider.getProvider().loadRfcTable();
            }
        }.start();

    }

    @Override
    public void deactivateTool() {

        super.deactivateTool();

        if (mouseHandler instanceof PgenSpenesDrawingHandler) {
            PgenSpenesDrawingHandler mph = (PgenSpenesDrawingHandler) mouseHandler;
            if (mph != null) {
                mph.clearPoints();
            }
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

            this.mouseHandler = new PgenSpenesDrawingHandler();

        }

        return this.mouseHandler;
    }

    private class PgenSpenesDrawingHandler extends InputHandlerDefaultImpl {

        /**
         * Points of the new element.
         */
        private final ArrayList<Coordinate> points = new ArrayList<>();

        /**
         * Current element.
         */
        private AbstractDrawableComponent elem;

        /**
         * An instance of DrawableElementFactory, which is used to create new
         * elements.
         */
        private final DrawableElementFactory def = new DrawableElementFactory();

        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(anX, aY);
            if (loc == null || shiftDown) {
                return false;
            }

            if (button == 1) {
                points.add(loc);
                return true;
            } else if (button == 3) {
                return true;
            } else if (button == 2) {
                return false;
            } else {
                return false;
            }

        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null) {
                return false;
            }

            // create the ghost element and put it in the drawing layer
            AbstractDrawableComponent ghostline = def.create(
                    DrawableType.SPENES, attrDlg, pgenCategory, pgenType,
                    points, drawingLayers.getActiveLayer());

            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);
                Line ln = (Line) ghostline;
                ln.setLinePoints(new ArrayList<>(ghostPts));

                /*
                 * Ghost distance and direction to starting point, if requested
                 */
                drawingLayers.setGhostLine(ghostline);
                mapEditor.refresh();

            }

            return false;

        }

        @Override
        public boolean handleMouseDownMove(int aX, int aY, int button) {
            if (shiftDown) {
                return false;
            }
            return true;
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

                    if (attrDlg != null) {
                        attrDlg.close();
                    }
                    attrDlg = null;
                    PgenUtil.setSelectingMode();

                } else if (points.size() < 2) {

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                } else {
                    // create a line
                    elem = def.create(DrawableType.SPENES, attrDlg,
                            pgenCategory, pgenType, points,
                            drawingLayers.getActiveLayer());

                    attrDlg.setDrawableElement(elem);

                    // add the product to PGEN resource
                    drawingLayers.addElement(elem);
                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                }

                return true;
            }
            return false;

        }

        private void clearPoints() {
            points.clear();
        }
    }

}
