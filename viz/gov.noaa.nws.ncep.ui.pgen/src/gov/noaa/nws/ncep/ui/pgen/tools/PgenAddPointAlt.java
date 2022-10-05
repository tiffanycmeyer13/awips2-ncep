/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenAddPoint
 *
 * 1 June 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

/**
 * Implements a modal map tool for PGEN add point function.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- ------------------------------------------
 * 06/10         282      S. Gilbert  Initial Creation.
 * 04/11         ?        B. Yin      Re-factor IAttribute
 * 05/11         808      J. Wu       Update Gfa vor text
 * 05/12         610      J. Wu       Add warning when GFA FROM lines > 3
 * 03/13         927      B. Yin      Moved out the mouse handler inner class.
 * Dec 01, 2021  95362    tjensen     Refactor PGEN Resource management to
 *                                    support multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenAddPointAlt extends AbstractPgenTool {

    /**
     * Input handler for mouse events.
     */
    protected PgenAddPointAltHandler addPtHandler = null;

    public PgenAddPointAlt() {

        super();

    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.addPtHandler == null
                || this.mapEditor != addPtHandler.getMapEditor()
                || !this.drawingLayers.matches(addPtHandler.getPgenrsc())) {
            this.addPtHandler = new PgenAddPointAltHandler(this);

        }

        return this.addPtHandler;

    }

}
