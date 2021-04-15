package gov.noaa.nws.ncep.ui.nsharp.display.map;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpSoundingQueryCommon
 * 
 * This java class performs the NSHARP Modal functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 10/2010      229         Chin Chen   Initial coding
 * 09/28/2015   RM#10295    Chin Chen   Let sounding data query run in its own
 *                                      thread to avoid gui locked out during load
 * 04/06/2020   73571       smanoj      NSHARP D2D port refactor
 * 06/22/2020   79556       smanoj      Fixing some errors and enhancements.
 * 
 * </pre>
 * 
 * @author Chin Chen
 * 
 */
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingLayer;
import gov.noaa.nws.ncep.ui.nsharp.NsharpStationInfo;
import gov.noaa.nws.ncep.ui.nsharp.display.NsharpEditor;
import gov.noaa.nws.ncep.ui.nsharp.display.rsc.NsharpResourceHandler;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

public class NsharpSoundingQueryCommon {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NsharpModelSoundingQuery.class);

    /*
     * Load sounding data to NsharpEditor, run in an UI thread
     */
    public static void handleQueryResponse(
            final NsharpStationInfo currentStnInfo,
            final Map<String, List<NcSoundingLayer>> soundingLysLstMap) {

        Job uijob = new UIJob("handleQueryResponse") {
            public IStatus runInUIThread(IProgressMonitor monitor) {

                if (NsharpEditor.getActiveNsharpEditor() != null) {
                    NsharpEditor.getActiveNsharpEditor().refresh();
                }
                if (soundingLysLstMap.size() <= 0) {
                    statusHandler.handle(Priority.WARN,
                            "No sounding data returned from DB for this station!!");
                } else {
                    NsharpResourceHandler rscHdr = NsharpEditor
                            .createOrOpenEditor().getRscHandler();
                    rscHdr.deleteRscAll();
                    rscHdr.addRsc(soundingLysLstMap, currentStnInfo);

                    NsharpEditor.bringEditorToTop();
                }
                return Status.OK_STATUS;
            }

        };
        uijob.setSystem(true);
        uijob.schedule();
    }

    /*
     * Post input msg to MessageBox
     */
    public static void postToMsgBox(String msg) {
        final String mbMsg = msg;
        Job uijob = new UIJob("postToMsgBox") {
            public IStatus runInUIThread(IProgressMonitor monitor) {
                Shell sh = new Shell();
                MessageBox mb = new MessageBox(sh, SWT.ICON_WARNING | SWT.OK);
                mb.setMessage(mbMsg);
                mb.open();
                return Status.OK_STATUS;
            }
        };
        uijob.setSystem(true);
        uijob.schedule();

    }
}
