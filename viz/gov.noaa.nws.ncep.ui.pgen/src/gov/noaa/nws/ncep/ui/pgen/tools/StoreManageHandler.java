/*
 * gov.noaa.nws.ncep.ui.pgen.controls.StoreManageHandler
 *
 * 27 March 2013
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen.tools;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.ui.tools.AbstractTool;

import gov.noaa.nws.ncep.ui.pgen.PgenSession;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.controls.StoreActivityDialog;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;

/**
 * Define a handler for PGEN product/activity store to EDEX.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- ------------------------------------------
 * 03/13         977      S. Gilbert  modified from PgenFileManageHandler.
 * 11/13         1077     J. Wu       Pop up dialog for "Save All".
 * 06/15         8354     J. Wu       Add CTRL+S hotkey for "Save".
 * Dec 02, 2021  95362    tjensen     Refactor PGEN Resource management to
 *                                    support multi-panel displays
 *
 * </pre>
 *
 * @author S. Gilbert
 */
public class StoreManageHandler extends AbstractTool {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        PgenResource currentResource = PgenSession.getInstance()
                .getCurrentResource();
        if (currentResource == null || !currentResource.isEditable()) {
            return null;
        }

        String actionName = null;
        String btnName = null;
        String btnClicked = null;

        if (event.getParameter("name") != null) {
            btnName = event.getParameter("name");
            btnClicked = btnName;
        } else if (event.getParameter("action") != null) {
            actionName = event.getParameter("action");
            btnClicked = actionName;
        } else {
            return null;
        }

        // Set "active" icon for the palette button corresponding to this tool
        if (btnName != null) {
            PgenSession.getInstance().getPgenPalette().setActiveIcon(btnName);
        }

        PgenResource rsc = PgenSession.getInstance().getCurrentResource();
        String curFile = rsc.getActiveProduct().getOutputFile();

        if (curFile != null && btnClicked.equalsIgnoreCase("Save")) {
            rsc.storeCurrentProduct(curFile);
        } else if (btnClicked.equalsIgnoreCase("Save All")) {

            // Save the current one first
            Product curPrd = rsc.getActiveProduct();
            if (curFile != null) {
                rsc.storeCurrentProduct(curFile);
            } else {
                storeActivity(btnClicked);
            }

            // Save the rest
            for (Product pp : rsc.getProducts()) {
                if (pp == curPrd) {
                    continue;
                }

                rsc.setActiveProduct(pp);
                if (pp.getOutputFile() != null) {
                    rsc.storeCurrentProduct(pp.getOutputFile());
                } else {
                    storeActivity(btnClicked);
                }

                rsc.setActiveProduct(curPrd);
            }
        } else { // "Save As"
            storeActivity(btnClicked);
        }

        // Reset the original icon for the palette button corresponding to this
        // tool
        if (PgenSession.getInstance().getPgenPalette() != null
                && btnName != null) {
            PgenSession.getInstance().getPgenPalette().resetIcon(btnName);
        }

        // Set to "selecting" mode.
        PgenUtil.setSelectingMode();

        return null;
    }

    /*
     * Pops up the Save/Store dialog to save an activity.
     *
     * @param btnClicked
     */
    private void storeActivity(String btnClicked) {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        StoreActivityDialog storeDialog = null;

        if (storeDialog == null) {
            try {
                storeDialog = new StoreActivityDialog(shell, btnClicked);
                storeDialog.setBlockOnOpen(true);
            } catch (VizException e) {
                e.printStackTrace();
            }
        }

        if (storeDialog != null) {
            storeDialog.open();
        }
    }

}