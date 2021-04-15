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

import gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpMapResource;
import gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpMapResourceData;

import java.util.ArrayList;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.view.NsharpLoadDialog
 * 
 * This java class performs the NSHARP NsharpLoadDialog functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/23/2010   229         Chin Chen   Initial coding
 * 01/19/2016   5054        randerso    Minor code cleanup, needs more but is out of scope
 * 03/31/2020   73571       smanoj      NSHARP D2D port refactor
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpLoadDialog extends AbstractNsharpLoadDialog {

    private static NsharpLoadDialog INSTANCE = null;

    private ArrayList<String> gpdSelectedTimeList = new ArrayList<String>();

    public ArrayList<String> getGpdSelectedTimeList() {
        return gpdSelectedTimeList;
    }

    public void setGpdSelectedTimeList(ArrayList<String> gpdSelectedTimeList) {
        this.gpdSelectedTimeList = gpdSelectedTimeList;
    }

    public static NsharpLoadDialog getAccess() {
        return INSTANCE;
    }

    public NsharpLoadDialog(Shell parentShell) throws VizException {
        super(parentShell);
        dialogWidth = 450;
        dialogHeight = 920;
    }

    public static NsharpLoadDialog getInstance(Shell parShell) {
        if (INSTANCE == null) {
            try {
                INSTANCE = new NsharpLoadDialog(parShell);
            } catch (VizException e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    @Override
    public void createLoadContents(Composite parent) {
        dialogParent = parent;
        this.shell = parent.getShell();

        mapRscData = new NsharpMapResourceData();
        LoadProperties loadProperties = new LoadProperties();
        mapRsc = new NsharpMapResource(mapRscData, loadProperties);
        mapRsc.setPoints(null);

        obsDialog = new NsharpObservedSoundingDialogContents(dialogParent);
        pfcDialog = new NsharpPfcSoundingDialogContents(dialogParent);
        mdlDialog = new NsharpModelSoundingDialogContents(dialogParent);

        switch (activeLoadSoundingType) {
        case MODEL_SND:
            mdlDialog.createMdlDialogContents();
            break;
        case PFC_SND:
            pfcDialog.createPfcDialogContents();
            break;
        default:
            obsDialog.createObsDialogContents();
            activeLoadSoundingType = OBSER_SND;
            break;
        }

        soundingTypeList.setSelection(activeLoadSoundingType);
    }

}
