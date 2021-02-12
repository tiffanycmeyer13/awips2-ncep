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

import gov.noaa.nws.ncep.viz.soundingrequest.NcSoundingQuery;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingProfile;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingTimeLines;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConstants;
import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.view.PfcSoundingDialogContents
 * 
 * This java class performs the NSHARP NsharpLoadDialog functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 01/2011      229         Chin Chen   Initial coding
 * Aug 05,2015  4486        rjpeter     Changed Timestamp to Date.
 * 07202015     RM#9173     Chin Chen   use NcSoundingQuery.genericSoundingDataQuery()
 *                                      to query grid model sounding data
 * 04/02/2020   73571       smanoj      NSHARP D2D port refactor
 *
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpPfcSoundingDialogContents
        extends AbstractPfcSoundingDlgContents {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractPfcSoundingDlgContents.class);

    private Group bottomGp, availableFileGp, sndTimeListGp;

    private Button timeBtn;

    private List<String> selectedFileList = new ArrayList<String>();

    private org.eclipse.swt.widgets.List availableFileList;

    public NsharpPfcSoundingDialogContents(Composite parent) {
        super(parent);
        ldDia = NsharpLoadDialog.getAccess();
        newFont = ldDia.getNewFont();
    }

    private void createPFCAvailableFileList() {
        sndTimeList.removeAll();
        availableFileList.removeAll();
        // query using NcSoundingQuery class to query
        NcSoundingTimeLines timeLines = NcSoundingQuery
                .soundingTimeLineQuery(currentSndType.toString());
        if (timeLines != null && timeLines.getTimeLines() != null) {
            ldDia.startWaitCursor();
            for (Object timeLine : timeLines.getTimeLines()) {
                Date reftime = (Date) timeLine;
                if (reftime != null) {
                    // need to format reftime to GMT time string. Date.toString
                    // produce a local time Not GMT time
                    Calendar cal = Calendar
                            .getInstance(TimeZone.getTimeZone("GMT"));
                    cal.setTimeInMillis(reftime.getTime());
                    String gmtTimeStr = String.format("%1$tY-%1$tm-%1$td %1$tH",
                            cal);
                    availableFileList.add(gmtTimeStr);
                }
            }
            ldDia.stopWaitCursor();
        } else
            statusHandler.handle(Priority.WARN, "SQL: query return null");
    }

    private void handleAvailFileListSelection() {
        String selectedFile = null;
        if (availableFileList.getSelectionCount() > 0) {
            selectedFileList.clear();
            for (int i = 0; i < availableFileList.getSelectionCount(); i++) {
                selectedFile = availableFileList.getSelection()[i];
                selectedFileList.add(selectedFile);
            }
            ldDia.setPfcSelectedFileList(selectedFileList);
            createTimeList(currentSndType, selectedFileList);
        }
    }

    public void createPfcDialogContents() {
        topGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        topGp.setLayout(new GridLayout(2, false));
        timeLimit = false;
        currentSndType = ldDia.getActivePfcSndType();
        ldDia.createSndTypeList(topGp);

        fileTypeGp = new Group(topGp, SWT.SHADOW_ETCHED_IN);
        fileTypeGp.setText("File Type");
        fileTypeGp.setFont(newFont);
        namBtn = new Button(fileTypeGp, SWT.RADIO | SWT.BORDER);
        namBtn.setText("NAMSND");
        namBtn.setEnabled(true);
        namBtn.setBounds(fileTypeGp.getBounds().x + NsharpConstants.btnGapX,
                fileTypeGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        namBtn.setFont(newFont);
        namBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                currentSndType = NcSoundingProfile.PfcSndType.NAMSND;
                createPFCAvailableFileList();
                ldDia.setActivePfcSndType(currentSndType);
            }
        });
        gfsBtn = new Button(fileTypeGp, SWT.RADIO | SWT.BORDER);
        gfsBtn.setText("GFSSND");
        gfsBtn.setEnabled(true);
        gfsBtn.setBounds(fileTypeGp.getBounds().x + NsharpConstants.btnGapX,
                namBtn.getBounds().y + namBtn.getBounds().height
                        + NsharpConstants.btnGapY,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        gfsBtn.setFont(newFont);
        gfsBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                currentSndType = NcSoundingProfile.PfcSndType.GFSSND;
                createPFCAvailableFileList();
                ldDia.setActivePfcSndType(currentSndType);
            }
        });

        timeBtn = new Button(parent, SWT.CHECK | SWT.BORDER);
        timeBtn.setText("00Z and 12Z only");
        timeBtn.setEnabled(true);
        timeBtn.setFont(newFont);
        timeBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                if (timeBtn.getSelection())
                    timeLimit = true;
                else
                    timeLimit = false;

                // refresh sounding list if file type is selected already
                if (!currentSndType.equals("NA")
                        && selectedFileList.size() > 0) {
                    createTimeList(currentSndType, selectedFileList);
                }
            }
        });

        bottomGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        bottomGp.setLayout(new GridLayout(2, false));

        availableFileGp = new Group(bottomGp, SWT.SHADOW_ETCHED_IN);
        availableFileGp.setText("Available PFC files:");
        availableFileGp.setFont(newFont);

        availableFileList = new org.eclipse.swt.widgets.List(availableFileGp,
                SWT.BORDER | SWT.V_SCROLL);
        availableFileList.setBounds(availableFileGp.getBounds().x,
                availableFileGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.filelistWidth,
                NsharpConstants.listHeight * 36 / 5);
        // create a selection listener to handle user's selection on list
        availableFileList.setFont(newFont);
        availableFileList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                handleAvailFileListSelection();
            }
        });

        // create Sounding Times widget list
        sndTimeListGp = new Group(bottomGp, SWT.SHADOW_ETCHED_IN);
        sndTimeListGp.setText("Sounding Times:");
        sndTimeListGp.setFont(newFont);
        sndTimeList = new org.eclipse.swt.widgets.List(sndTimeListGp,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        sndTimeList.removeAll();
        sndTimeList.setFont(newFont);
        sndTimeList.setBounds(sndTimeListGp.getBounds().x,
                sndTimeListGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.listWidth, NsharpConstants.listHeight * 36 / 5);
        sndTimeList.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                selectedTimeList.clear();
                for (int i = 0; i < sndTimeList.getSelectionCount(); i++) {
                    selectedTimeList.add(sndTimeList.getSelection()[i]);
                }

                ldDia.startWaitCursor();
                AbstractNsharpMapResource nsharpMapResource = ldDia.getMapRsc()
                        .getOrCreateNsharpMapResource();
                nsharpMapResource.setPoints(null);
                handleSndTimeSelection(currentSndType);

                nsharpMapResource.setPoints(stnPoints);
                ldDia.getMapRsc().bringMapEditorToTop();
                ldDia.stopWaitCursor();
            }
        });

        if (currentSndType == NcSoundingProfile.PfcSndType.GFSSND
                || currentSndType == NcSoundingProfile.PfcSndType.NAMSND) {
            if (currentSndType == NcSoundingProfile.PfcSndType.GFSSND)
                gfsBtn.setSelection(true);
            else
                namBtn.setSelection(true);
            createPFCAvailableFileList();
            selectedFileList = ldDia.getPfcSelectedFileList();
            Object[] selFileObjectArray = selectedFileList.toArray();
            String[] selFileStringArray = Arrays.copyOf(selFileObjectArray,
                    selFileObjectArray.length, String[].class);
            availableFileList.setSelection(selFileStringArray);
            handleAvailFileListSelection();

            selectedTimeList = ldDia.getPfcSelectedTimeList();
            Object[] selTimeObjectArray = selectedTimeList.toArray();
            String[] selTimeStringArray = Arrays.copyOf(selTimeObjectArray,
                    selTimeObjectArray.length, String[].class);
            sndTimeList.setSelection(selTimeStringArray);
            handleSndTimeSelection(currentSndType);

        }
    }

    public void cleanup() {
        if (namBtn != null && namBtn.isDisposed() == false) {
            namBtn.removeListener(SWT.MouseUp,
                    namBtn.getListeners(SWT.MouseUp)[0]);
            namBtn.dispose();
            namBtn = null;
        }
        if (gfsBtn != null) {
            gfsBtn.removeListener(SWT.MouseUp,
                    gfsBtn.getListeners(SWT.MouseUp)[0]);
            gfsBtn.dispose();
            gfsBtn = null;
        }

        NsharpLoadDialog ldDia = NsharpLoadDialog.getAccess();
        ldDia.cleanSndTypeList();

        if (topGp != null) {
            topGp.dispose();
            topGp = null;
        }

        if (timeBtn != null) {
            timeBtn.removeListener(SWT.MouseUp,
                    timeBtn.getListeners(SWT.MouseUp)[0]);
            timeBtn.dispose();
            timeBtn = null;
        }

        if (fileTypeGp != null) {
            fileTypeGp.dispose();
            fileTypeGp = null;
        }

        if (availableFileList != null) {
            availableFileList.removeListener(SWT.Selection,
                    availableFileList.getListeners(SWT.Selection)[0]);
            availableFileList.dispose();
            availableFileList = null;
        }

        if (availableFileGp != null) {
            availableFileGp.dispose();
            availableFileGp = null;
        }
        if (sndTimeList != null) {
            sndTimeList.removeListener(SWT.Selection,
                    sndTimeList.getListeners(SWT.Selection)[0]);
            sndTimeList.dispose();
            sndTimeList = null;
        }
        if (sndTimeListGp != null) {
            sndTimeListGp.dispose();
            sndTimeListGp = null;
        }
        if (bottomGp != null) {
            bottomGp.dispose();
            bottomGp = null;
        }
    }

    @Override
    protected void setSndTimeList(List<String> timeList) {
        this.selectedTimeList.clear();
        this.sndTimeList.removeAll();
        for (int i = 0; i < timeList.size(); i++) {
            this.sndTimeList.add(timeList.get(i));
        }
    }

}
