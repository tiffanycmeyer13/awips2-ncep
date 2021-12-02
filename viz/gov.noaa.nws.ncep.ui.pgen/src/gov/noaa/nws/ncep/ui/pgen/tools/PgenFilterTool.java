/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenDeleteAll
 *
 * 23 March 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.PgenFilterDlg;

/**
 * Implements PGEN "Delete All" function.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- ------------------------------------------
 * 03/09                  B. Yin      Initial Creation.
 * 04/09         72       S. Gilbert  Modified to use PgenSession and
 *                                    PgenCommands
 * 04/09         103      B. Yin      Extends from AbstractPgenTool
 * 07/09         131      J. Wu       Modify to work on the active layer only
 * Dec 02, 2021  95362    tjensen     Refactor PGEN Resource management to
 *                                    support multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenFilterTool extends AbstractPgenTool {

    public PgenFilterTool() {

        super();

    }

    @Override
    protected void activateTool() {

        super.activateTool();

        PgenFilterDlg filterDlg = PgenFilterDlg.getInstance(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell());

        filterDlg.setResource(drawingLayers, mapEditor);
        filterDlg.setBlockOnOpen(false);
        filterDlg.open();

        PgenUtil.setSelectingMode();

    }

    @Override
    public IInputHandler getMouseHandler() {
        return null;
    }

}