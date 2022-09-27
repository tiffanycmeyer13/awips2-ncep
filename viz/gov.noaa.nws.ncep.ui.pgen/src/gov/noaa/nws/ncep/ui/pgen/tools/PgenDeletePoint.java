/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenDeletePoint
 *
 * 1 May 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

/**
 * Implements a modal map tool for PGEN deleting point function.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 05/09         79       B. Yin    Initial Creation.
 * 08/09         79       B. Yin    Handle jet.
 * 11/10         332      B. Yin    Added cleanup() for handler
 * 04/11         #?       B. Yin    Re-factor IAttribute
 * 02/12         665      J. Wu     Back to "Select" if no DE selected
 * 05/12         610      J. Wu     Add warning when GFA FROM lines > 3
 * 03/13         927      B. Yin    Moved the handler class out.
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenDeletePoint extends PgenSelectingTool {

    /**
     * Input handler for mouse events.
     */
    protected PgenDeletePointHandler delPtHandler = null;

    public PgenDeletePoint() {

        super();

    }

    @Override
    public void deactivateTool() {

        if (delPtHandler != null) {
            delPtHandler.cleanup();
        }

        super.deactivateTool();

    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.delPtHandler == null
                || this.mapEditor != delPtHandler.getMapEditor()
                || !this.drawingLayers.matches(delPtHandler.getPgenrsc())) {
            this.delPtHandler = new PgenDeletePointHandler(this);

        }

        return this.delPtHandler;

    }

}
