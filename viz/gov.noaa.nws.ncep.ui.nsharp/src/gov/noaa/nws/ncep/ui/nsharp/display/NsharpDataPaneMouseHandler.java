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

import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;
import gov.noaa.nws.ncep.ui.nsharp.display.rsc.NsharpDataPaneResource;

import org.eclipse.swt.widgets.Event;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.skewt.rsc.NsharpDataPaneMouseHandler
 * 
 * This java class performs the NSHARP NsharpDataPaneMouseHandler functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/23/2010    229        Chin Chen   Initial coding
 * 03/09/2011               Chin Chen   Updated for R1G2-9
 * 06/14/2011   11-5        Chin Chen   migration
 * 04/16/2020   73571       smanoj      NSHARP D2D port refactor
 * 04/01/2022   89212       smanoj      Fix some Nsharp display issues.
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpDataPaneMouseHandler extends NsharpAbstractMouseHandler {

    public NsharpDataPaneMouseHandler(NsharpEditor editor, IDisplayPane pane) {
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

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        theLastMouseX = x;
        theLastMouseY = y;
        if (getPaneDisplay() == null) {
            return false;
        } else if (mouseButton == 1) {
            this.mode = Mode.CREATE;
            Coordinate c = editor.translateClick(x, y);
            NsharpDataPaneResource rsc = (NsharpDataPaneResource) getDescriptor()
                    .getPaneResource();
            if ((rsc.getDataPanel1Background().contains(c) == true
                    || rsc.getDataPanel2Background().contains(c) == true)
                    && rsc.isSumP1Visible() == true) {
                this.mode = Mode.PARCELLINE_DOWN;
            }
            editor.refresh();
        }

        return false;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {
        if (getPaneDisplay() == null) {
            return false;
        }
        if (editor != null) {
            // button 1 is left mouse button
            if (mouseButton == 1) {
                Coordinate c = editor.translateClick(x, y);
                NsharpDataPaneResource rsc = (NsharpDataPaneResource) getDescriptor()
                        .getPaneResource();
                if ((rsc.getDataPanel1Background().contains(c) == true
                        || rsc.getDataPanel2Background().contains(c) == true)
                        && this.mode == Mode.PARCELLINE_DOWN) {
                    rsc.setUserPickedParcelLine(c);
                }
                this.mode = Mode.CREATE;
            } else if (mouseButton == 3) {
                // right mouse button
                if (AbstractNsharpMapResource.getMapResource(editor) != null) {
                    AbstractNsharpMapResource.getMapResource(editor)
                            .bringMapEditorToTop();
                }
            }
            editor.refresh();
        }
        return false;
    }
}
