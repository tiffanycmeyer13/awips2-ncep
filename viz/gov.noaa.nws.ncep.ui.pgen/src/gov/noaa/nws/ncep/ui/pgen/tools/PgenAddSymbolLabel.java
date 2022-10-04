package gov.noaa.nws.ncep.ui.pgen.tools;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

/**
 * Action handler routing for adding labels to non-met, non-contour symbols
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * Jul 08, 2015  8198     srussell  Initial creation
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author Steve Russell
 */

public class PgenAddSymbolLabel extends AbstractPgenTool {

    protected IInputHandler addLabelHandler = null;

    /**
     * Constructor
     */

    public PgenAddSymbolLabel() {
        super();
    }

    /**
     * Returns the current mouse handler.
     *
     * @return IInputHandler the current mouse handler
     */

    @Override
    public IInputHandler getMouseHandler() {

        if (this.addLabelHandler == null
                || this.mapEditor != ((PgenAddSymbolLabelHandler) addLabelHandler)
                        .getMapEditor()
                || !this.drawingLayers
                        .matches(((PgenAddSymbolLabelHandler) addLabelHandler)
                                .getPgenrsc())) {
            this.addLabelHandler = new PgenAddSymbolLabelHandler(this);

        }

        return this.addLabelHandler;

    }

}
