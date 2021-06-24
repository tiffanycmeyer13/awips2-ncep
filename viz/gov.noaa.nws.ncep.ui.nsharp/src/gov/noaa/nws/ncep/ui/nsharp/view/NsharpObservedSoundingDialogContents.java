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

import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingProfile;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConstants;
import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;

import java.util.Arrays;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.view.ObservedSoundingDialogContents
 * 
 * This java class performs the NSHARP NsharpLoadDialog functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 01/2011       229         Chin Chen    Initial coding
 * 09/14/2011    457         S. Gurung    Renamed H5UAIR to NCUAIR
 * Aug 05,2015   4486        rjpeter      Changed Timestamp to Date.
 * 07202015      RM#9173     Chin Chen    use NcSoundingQuery.genericSoundingDataQuery()
 *                                        to query grid model sounding data
 * 04/02/2020    73571       smanoj       NSHARP D2D port refactor
 *
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpObservedSoundingDialogContents
        extends AbstractObsSoundingDlgContents {

    private Group sndTimeListGp, midGp;

    private Button timeBtn, rawBtn;

    public NsharpObservedSoundingDialogContents(Composite parent) {
        super(parent);
        ldDia = NsharpLoadDialog.getAccess();
        newFont = ldDia.getNewFont();
    }

    public void createObsDialogContents() {
        currentSndType = ldDia.getActiveObsSndType();
        timeLimit = false;
        rawData = false;
        topGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        topGp.setLayout(new GridLayout(2, false));

        ldDia.createSndTypeList(topGp);

        btnGp = new Group(topGp, SWT.SHADOW_ETCHED_IN);
        btnGp.setText("File Type");
        btnGp.setFont(newFont);
        uairBtn = new Button(btnGp, SWT.RADIO | SWT.BORDER);
        uairBtn.setText(FILE_UAIR);
        uairBtn.setEnabled(true);
        uairBtn.setBounds(btnGp.getBounds().x + NsharpConstants.btnGapX,
                btnGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        uairBtn.setFont(newFont);
        uairBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                sndTimeList.removeAll();
                currentSndType = NcSoundingProfile.ObsSndType.NCUAIR;
                ldDia.setActiveObsSndType(currentSndType);
                createTimeList(currentSndType);
            }
        });

        bufruaBtn = new Button(btnGp, SWT.RADIO | SWT.BORDER);
        bufruaBtn.setText(FILE_BUFRUA);
        bufruaBtn.setEnabled(true);
        bufruaBtn.setBounds(btnGp.getBounds().x + NsharpConstants.btnGapX,
                uairBtn.getBounds().y + uairBtn.getBounds().height
                        + NsharpConstants.btnGapY,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        bufruaBtn.setFont(newFont);
        bufruaBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                sndTimeList.removeAll();
                currentSndType = NcSoundingProfile.ObsSndType.BUFRUA;
                ldDia.setActiveObsSndType(currentSndType);
                createTimeList(currentSndType);
            }
        });

        midGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        midGp.setLayout(new GridLayout(2, false));
        timeBtn = new Button(midGp, SWT.CHECK | SWT.BORDER);
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
                if (currentSndType == NcSoundingProfile.ObsSndType.NCUAIR
                        || currentSndType == NcSoundingProfile.ObsSndType.BUFRUA) {
                    createTimeList(currentSndType);
                }
            }
        });
        rawBtn = new Button(midGp, SWT.CHECK | SWT.BORDER);
        rawBtn.setText("raw data");
        rawBtn.setEnabled(true);
        rawBtn.setBounds(timeBtn.getBounds().x + timeBtn.getBounds().width,
                timeBtn.getBounds().y, timeBtn.getBounds().width,
                timeBtn.getBounds().height);
        rawBtn.setFont(newFont);
        rawBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                if (rawBtn.getSelection())
                    rawData = true;
                else
                    rawData = false;
            }
        });
        // create file widget list
        sndTimeListGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        sndTimeListGp.setText("Sounding Times:");
        sndTimeListGp.setFont(newFont);
        sndTimeList = new org.eclipse.swt.widgets.List(sndTimeListGp,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        sndTimeList.setBounds(btnGp.getBounds().x + NsharpConstants.btnGapX,
                sndTimeListGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.listWidth, NsharpConstants.listHeight * 7);
        sndTimeList.setFont(newFont);

        // create a selection listener to handle user's selection on list
        sndTimeList.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                AbstractNsharpMapResource nsharpMapResource = ldDia.getMapRsc()
                        .getOrCreateNsharpMapResource();
                nsharpMapResource.setPoints(null);
                selectedTimeList.clear();
                if (sndTimeList.getSelectionCount() > 0) {
                    for (int i = 0; i < sndTimeList.getSelectionCount(); i++) {
                        selectedTimeList.add(sndTimeList.getSelection()[i]);
                    }
                }
                handleSndTimeSelection(currentSndType);
                nsharpMapResource.setPoints(stnPoints);
                ldDia.getMapRsc().bringMapEditorToTop();
            }
        });

        if (currentSndType == NcSoundingProfile.ObsSndType.NCUAIR
                || currentSndType == NcSoundingProfile.ObsSndType.BUFRUA) {
            if (currentSndType == NcSoundingProfile.ObsSndType.NCUAIR)
                uairBtn.setSelection(true);
            else
                bufruaBtn.setSelection(true);
            createTimeList(currentSndType);
            Object[] selTimeObjectArray = selectedTimeList.toArray();
            String[] selTimeStringArray = Arrays.copyOf(selTimeObjectArray,
                    selTimeObjectArray.length, String[].class);
            sndTimeList.setSelection(selTimeStringArray);

            AbstractNsharpMapResource nsharpMapResource = ldDia.getMapRsc()
                    .getOrCreateNsharpMapResource();
            nsharpMapResource.setPoints(null);
            handleSndTimeSelection(currentSndType);
            nsharpMapResource.setPoints(stnPoints);
            ldDia.getMapRsc().bringMapEditorToTop();
        }
    }

    public void cleanup() {
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
        if (timeBtn != null) {
            timeBtn.removeListener(SWT.MouseUp,
                    timeBtn.getListeners(SWT.MouseUp)[0]);
            timeBtn.dispose();
            timeBtn = null;
        }
        if (rawBtn != null) {
            rawBtn.removeListener(SWT.MouseUp,
                    rawBtn.getListeners(SWT.MouseUp)[0]);
            rawBtn.dispose();
            rawBtn = null;
        }
        if (midGp != null) {
            midGp.dispose();
            midGp = null;
        }
        if (bufruaBtn != null) {
            bufruaBtn.removeListener(SWT.MouseUp,
                    bufruaBtn.getListeners(SWT.MouseUp)[0]);
            bufruaBtn.dispose();
            bufruaBtn = null;
        }
        if (uairBtn != null) {
            uairBtn.removeListener(SWT.MouseUp,
                    uairBtn.getListeners(SWT.MouseUp)[0]);
            uairBtn.dispose();
            uairBtn = null;
        }
        if (btnGp != null) {
            btnGp.dispose();
            btnGp = null;
        }

        NsharpLoadDialog ldDia = NsharpLoadDialog.getAccess();
        ldDia.cleanSndTypeList();
        if (topGp != null) {
            topGp.dispose();
            topGp = null;
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
