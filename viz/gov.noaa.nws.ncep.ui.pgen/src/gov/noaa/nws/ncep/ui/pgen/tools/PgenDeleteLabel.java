package gov.noaa.nws.ncep.ui.pgen.tools;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

/**
 * Implements a modal map tool for PGEN deleting part function for labels of
 * non-met contour symbols only.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer   Description
 * ------------- -------- ---------- -------------------------------------------
 * Dec 06, 0014  8199     S Russell  Initial Creation.
 * Dec 02, 2021  95362    tjensen    Refactor PGEN Resource management to
 *                                   support multi-panel displays
 *
 * </pre>
 *
 * @author Steve Russell
 */

public class PgenDeleteLabel extends AbstractPgenTool {

    protected IInputHandler delLabelHandler = null;

    public PgenDeleteLabel() {
        super();
    }

    /**
     * Returns the current mouse handler.
     *
     * @return IInputHandler the current mouse handler
     */

    @Override
    public IInputHandler getMouseHandler() {

        if (this.delLabelHandler == null
                || this.mapEditor != ((PgenDeleteLabelHandler) delLabelHandler)
                        .getMapEditor()
                || !this.drawingLayers
                        .matches(((PgenDeleteLabelHandler) delLabelHandler)
                                .getPgenrsc())) {
            this.delLabelHandler = new PgenDeleteLabelHandler(this);

        }

        return this.delLabelHandler;

    }
}
