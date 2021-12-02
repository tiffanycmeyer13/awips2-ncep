/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenTCMDrawingTool
 *
 * 6 September 2011
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.TcmAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.tcm.Tcm;

/**
 * Implements a modal map tool for PGEN TCM drawing.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 09/11         ?        B. Yin    Initial Creation for TCM
 * 12/11         565      B. Yin    change return values for mouse handlers in
 *                                  order for panning to work correctly
 * May 16, 2016  5640     bsteffen  Access triggering component using PgenUtil.
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */
public class PgenTcmDrawingTool extends AbstractPgenDrawingTool {

    public PgenTcmDrawingTool() {

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

        Tcm elem = null;
        AbstractDrawableComponent triggerComponent = PgenUtil
                .getTriggerComponent(event);

        if (triggerComponent instanceof Tcm) {
            elem = (Tcm) triggerComponent;
        }

        if (attrDlg instanceof TcmAttrDlg) {
            if (elem != null) {
                attrDlg.setAttrForDlg(elem);
            }
            ((TcmAttrDlg) attrDlg).setTcm(elem);
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
            this.mouseHandler = new PgenTCMDrawingHandler();
        }

        return this.mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     * 
     * @author jun
     *
     */

    public class PgenTCMDrawingHandler extends InputHandlerDefaultImpl {

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
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            if (button == 1) {
                return false;
            } else if (button == 3) {
                return true;
            } else if (button == 2) {
                return false;
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

                drawingLayers.removeGhostLine();
                mapEditor.refresh();

                if (points.size() == 0) {

                    PgenUtil.setSelectingMode();

                } else {

                    points.clear();

                }

                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean handleMouseMove(int x, int y) {

            return false;

        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            return false;
        }

    }

}
