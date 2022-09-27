/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 *
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 *
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.nws.ncep.ui.nsharp.display;

import org.eclipse.swt.widgets.Event;
import com.raytheon.uf.viz.core.IDisplayPane;

/**
 * 
 * The class for NSHARP Inset Pane MouseHandler
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------      --------   -----------
 * 04/01/2022   89212       smanoj      Initial version
 *
 * </pre>
 * 
 * @author smanoj
 */
public class NsharpInsetPaneMouseHandler extends NsharpAbstractMouseHandler {

    public NsharpInsetPaneMouseHandler(NsharpEditor editor, IDisplayPane pane) {
        super(editor, pane);
    }

    @Override
    public boolean handleMouseWheel(Event event, int x, int y) {

        if (editor == null || cursorInPane == false) {
            return false;
        }

        com.raytheon.viz.ui.input.preferences.MouseEvent SCROLL_FORWARD = com.raytheon.viz.ui.input.preferences.MouseEvent.SCROLL_FORWARD;
        com.raytheon.viz.ui.input.preferences.MouseEvent SCROLL_BACK = com.raytheon.viz.ui.input.preferences.MouseEvent.SCROLL_BACK;
        if ((event.count < 0
                && prefManager.handleEvent(ZOOMIN_PREF, SCROLL_FORWARD))
                || (event.count > 0 && prefManager.handleEvent(ZOOMOUT_PREF,
                        SCROLL_BACK))) {
            currentPane.zoom(event.count, event.x, event.y);

            editor.refresh();
        }
        return true;
    }

}
