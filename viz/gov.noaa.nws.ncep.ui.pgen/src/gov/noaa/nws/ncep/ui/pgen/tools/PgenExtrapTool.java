
/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenExtrapTool
 *
 * June 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

//import gov.noaa.nws.ncep.ui.display.InputHandlerDefaultImpl;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.annotation.Operation;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.PgenExtrapDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.WatchBox;
import gov.noaa.nws.ncep.ui.pgen.filter.OperationFilter;

/**
 * Implements a modal map tool for PGEN extrapolation.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 06/09         130      J. Wu     Created
 * 06/09         116      B. Yin    Handle DECollections
 * 08/09         135      B. Yin    Handle Jet
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author J. Wu
 */
public class PgenExtrapTool extends AbstractPgenDrawingTool {

    /**
     * Input handler for mouse events.
     */
    public PgenExtrapTool() {

        super();

    }

    /**
     * Returns the current mouse handler.
     * 
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.mouseHandler == null) {
            this.mouseHandler = new PgenExtrapHandler();
        }

        return this.mouseHandler;

    }

    /**
     * Implements input handler for mouse events.
     * 
     * @author jwu
     *
     */
    public class PgenExtrapHandler extends InputHandlerDefaultImpl {

        private boolean preempt;

        /**
         * Current extrapolation dialog.
         */
        private PgenExtrapDlg extrapDlg = null;

        private final OperationFilter extrapFilter = new OperationFilter(
                Operation.EXTRAPOLATE);

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

                if (drawingLayers.getSelectedComp() == null) {

                    // Get the nearest element and set it as the selected
                    // element.
                    AbstractDrawableComponent elSelected = drawingLayers
                            .getNearestComponent(loc, extrapFilter, true);
                    drawingLayers.setSelected(elSelected);
                    if (elSelected != null) {
                        preempt = true;
                    }

                } else {

                    extrapDlg = (PgenExtrapDlg) attrDlg;
                    AbstractDrawableComponent elem = drawingLayers
                            .getSelectedComp();
                    AbstractDrawableComponent newDE = PgenToolUtils
                            .extrapElement(elem, extrapDlg.getDirection(),
                                    extrapDlg.getDistance());

                    boolean getNewDE = true;
                    if (newDE instanceof WatchBox) {
                        getNewDE = PgenWatchBoxModifyTool.resnapWatchBox(
                                mapEditor, (WatchBox) newDE, (WatchBox) newDE);
                    }

                    if (getNewDE) {
                        if (extrapDlg.isCopy()) {
                            drawingLayers.addElement(newDE);
                        } else {
                            drawingLayers.replaceElement(
                                    drawingLayers.getSelectedComp(), newDE);
                        }
                    }
                    drawingLayers.removeSelected();
                }

                mapEditor.refresh();

                return preempt;
            } else if (button == 3) {
                return true;
            } else {
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

        /*
         * overrides the function in selecting tool
         */
        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            if (button == 3) {

                if (drawingLayers.getSelectedComp() != null) {
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

    }

}
