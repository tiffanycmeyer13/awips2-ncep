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
package gov.noaa.nws.ncep.ui.nsharp.view;

import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.palette.NsharpPaletteAction
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ----------   -----       --------    -----------
 * 03/23/2010   229         Chin Chen   Initial coding
 * 04/15/2020   73571       smanoj      NSHARP D2D port refactor.
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpPaletteAction extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        //The viewID string is in the XML file for NSHARP extension point. 
        IWorkbenchPage wpage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        IViewPart vpart = wpage.findView( "gov.noaa.nws.ncep.ui.nsharp" );

        try {
            if ( vpart == null ) {
                vpart = wpage.showView( "gov.noaa.nws.ncep.ui.nsharp" );

                //Chin MERGE moved this here from the NsharpPaletteWindow so we can open the view without an editor.
                if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
                    if (EditorUtil.getActiveEditor() != null) {
                        AbstractEditor mapEditor = ((AbstractEditor) EditorUtil.getActiveEditor());
                        if (AbstractNsharpMapResource.getMapResource(mapEditor) !=null) {
                            AbstractNsharpMapResource.getMapResource(mapEditor).setPoints(null);
                        }
                    }
                }
            }
            else {
                if ( ! wpage.isPartVisible(vpart) ) vpart = wpage.showView( "gov.noaa.nws.ncep.ui.nsharp" );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
