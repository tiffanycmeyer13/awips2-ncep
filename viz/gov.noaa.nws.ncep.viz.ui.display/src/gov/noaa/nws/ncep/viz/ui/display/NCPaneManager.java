package gov.noaa.nws.ncep.viz.ui.display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.rsc.IInputHandler;

import gov.noaa.nws.ncep.viz.common.display.INcPaneLayout;
import gov.noaa.nws.ncep.viz.common.display.NcDisplayType;

/**
 * Natl Cntrs extention of PaneManager.
 *
 * Note that this uses a slightly different method of selecting panes.
 * IPaneManager allows for different kind of pane selections (ie actions of
 * LOAD, IMAGE, ....) but one one may be selected for each action.)
 * NCPaneManager ignores the action but will allow for more than one pane to be
 * selected at one time. selectPane() and deselectPane() should be called
 * instead of setSelectedPane().
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#    Engineer     Description
 * ------------ ----------  -----------  --------------------------
 * 03/07/11      R1G2-9        Greg Hull        Created
 * 07/18/12      #649       Shova Gurung Fixed echo/virtual cursor display issue.
 * 09/13/12                    ?               B. Yin           Refresh only for multiple panes
 * 01/28/12      #972       Greg Hull    moved all but paneLayout to AbstractNcPaneManager.
 * Oct 07, 2022  8792       mapeters     Updated for tracking of LegacyPanes
 *
 *
 * </pre>
 *
 * @author ghull
 */
public class NCPaneManager extends AbstractNcPaneManager
        implements IInputHandler {

    public NCPaneManager(INcPaneLayout playout, NcDisplayType dispType) {
        // must be an NcPaneLayout
        super(playout, dispType);
    }

    // called from AbstractEditor.createPartControl
    @Override
    public void initializeComponents(IDisplayPaneContainer container,
            Composite parent) {
        paneContainer = container;
        GridLayout gl = new GridLayout(((NcPaneLayout) paneLayout).getColumns(),
                true);
        gl.horizontalSpacing = 3;
        gl.verticalSpacing = 3;
        gl.marginHeight = 0;
        gl.marginWidth = 0;

        composite = parent;
        composite.setLayout(gl);

        // Enable the inspect adapters
        // handles the VirtualCursor and selecting the panes
        if (paneContainer instanceof AbstractNcEditor) {
            inputManager.registerMouseHandler(
                    new NcPaneMouseHandler((AbstractNcEditor) paneContainer),
                    InputPriority.PERSPECTIVE);
        }

        composite.addListener(SWT.Resize, event -> {
            // PaneManager includes code to adjust the paneLayout here .....
        });

        mainCanvasToPaneMap.clear();
    }
}
