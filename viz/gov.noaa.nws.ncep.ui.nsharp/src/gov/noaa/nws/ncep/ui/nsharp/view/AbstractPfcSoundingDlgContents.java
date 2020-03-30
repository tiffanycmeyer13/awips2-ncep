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
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingStnInfo;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingStnInfoCollection;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingTimeLines;
import gov.noaa.nws.ncep.ui.nsharp.NsharpStationInfo;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 *
 * Provides a base implementation for NSHARP Point Forecast Sounding Dialog
 * Contents.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- -----------------
 * Mar 20, 2020  73571    smanoj   Initial creation
 *
 * </pre>
 *
 * @author smanoj
 */
public abstract class AbstractPfcSoundingDlgContents {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractPfcSoundingDlgContents.class);

    protected Composite parent;

    protected AbstractNsharpLoadDialog ldDia;

    protected Group topGp, fileTypeGp;

    protected Button namBtn, gfsBtn;

    protected boolean timeLimit = false;

    protected List<String> selectedTimeList = new ArrayList<String>();

    protected NcSoundingProfile.PfcSndType currentSndType = NcSoundingProfile.PfcSndType.NONE;

    protected Font newFont;

    protected org.eclipse.swt.widgets.List sndTimeList;

    protected List<NsharpStationInfo> stnPoints = new ArrayList<NsharpStationInfo>();

    private List<String> timeList = new ArrayList<String>();

    public AbstractPfcSoundingDlgContents(Composite parent) {
        this.parent = parent;
    }

    protected void createTimeList(NcSoundingProfile.PfcSndType currentSndType,
            List<String> availablefileList) {
        if (availablefileList.size() <= 0) {
            return;
        }

        if (timeList != null) {
            timeList.clear();
        }

        String sndStr = currentSndType.toString();
        int endIndex = Math.min(3, sndStr.length());
        String dispSndStr = sndStr.substring(0, endIndex);
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] defaultDays = dfs.getShortWeekdays();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        for (int i = 0; i < availablefileList.size(); i++) {
            String fl = availablefileList.get(i);
            long reftimeMs = NcSoundingQuery.convertRefTimeStr(fl);

            NcSoundingTimeLines timeLines = NcSoundingQuery
                    .soundingRangeTimeLineQuery(sndStr, fl, null);
            if (timeLines != null && timeLines.getTimeLines().length > 0) {
                for (Object obj : timeLines.getTimeLines()) {
                    Date rangestart = (Date) obj;

                    // need to format rangestart to GMT time string.
                    // Date.toString produce a local time Not GMT time
                    cal.setTimeInMillis(rangestart.getTime());
                    long vHour = (cal.getTimeInMillis() - reftimeMs) / 3600000;
                    String dayOfWeek = defaultDays[cal
                            .get(Calendar.DAY_OF_WEEK)];
                    String gmtTimeStr = String.format(
                            "%1$ty%1$tm%1$td/%1$tH(%4$s)V%2$03d %3$s", cal,
                            vHour, dispSndStr, dayOfWeek);
                    if (timeList.indexOf(gmtTimeStr) != -1) {
                        // this indicate that gmtTimeStr is already in the
                        // sndTimeList, then we don't need to add it to list
                        // again.
                        continue;
                    }

                    if (!timeLimit) {
                        timeList.add(gmtTimeStr);
                    } else {
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        if ((hour == 0) || (hour == 12))
                            timeList.add(gmtTimeStr);
                    }
                }
            }
        }
        setSndTimeList(timeList);
    }

    protected void handleSndTimeSelection(
            NcSoundingProfile.PfcSndType currentSndType) {
        String selectedSndTime = null;

        if (selectedTimeList.isEmpty()) {
            statusHandler.handle(Priority.INFO,
                    "Data not available to Mark Points on Map for "
                            + currentSndType.toString());
        } else {
            statusHandler.handle(Priority.INFO, "Marking Points on the Map for "
                    + currentSndType.toString());

            List<String> queriedTimeList = new ArrayList<String>();
            for (int i = 0; i < selectedTimeList.size(); i++) {
                selectedSndTime = selectedTimeList.get(i);
                int endIndex = selectedSndTime.indexOf(" ");
                String queryingSndTime = selectedSndTime.substring(0, endIndex);

                String refTimeStr = NcSoundingQuery
                        .convertSoundTimeDispStringToForecastTime(
                                queryingSndTime);
                String rangeStartStr = NcSoundingQuery
                        .convertSoundTimeDispStringToRangeStartTimeFormat(
                                queryingSndTime);
                if (queriedTimeList.contains(refTimeStr) == true) {
                    addStnPtWithoutQuery(currentSndType, refTimeStr,
                            rangeStartStr, queryingSndTime);
                } else {
                    queriedTimeList.add(refTimeStr);
                    queryAndMarkStn(currentSndType, refTimeStr, rangeStartStr,
                            queryingSndTime);
                }
            }
        }
    }

    protected void queryAndMarkStn(NcSoundingProfile.PfcSndType currentSndType,
            String refTimeStr, String rangeStartStr, String selectedSndTime) {
        String sndTypeStr = currentSndType.toString();
        // use NcSoundingQuery to query stn info
        NcSoundingStnInfoCollection sndStnInfoCol = NcSoundingQuery
                .genericSoundingStnInfoQuery(sndTypeStr, rangeStartStr,
                        refTimeStr);

        if (sndStnInfoCol != null && sndStnInfoCol.getStationInfo() != null) {
            NcSoundingStnInfo[] stnInfoAry = sndStnInfoCol.getStationInfo();

            // Note: A same station may have many reports
            for (int i = 0; i < stnInfoAry.length; i++) {
                NcSoundingStnInfo stnInfo = stnInfoAry[i];
                NsharpStationInfo stn = new NsharpStationInfo();
                NsharpStationInfo.timeLineSpecific timeLinsSpc = stn.new timeLineSpecific();

                int endIndex = Math.min(4, sndTypeStr.length());
                String packedStnIdStr = stnInfo.getStnId().replace(" ", "_");
                String dispInfo = packedStnIdStr + " " + selectedSndTime + " "
                        + sndTypeStr.substring(0, endIndex);
                timeLinsSpc.setDisplayInfo(dispInfo);
                timeLinsSpc.setTimeLine(stnInfo.getRangeStartTime());
                stn.addToTimeLineSpList(timeLinsSpc);
                stn.setLongitude(stnInfo.getStationLongitude());
                stn.setLatitude(stnInfo.getStationLatitude());
                stn.setReftime(stnInfo.getSynopTime());
                stn.setStnId(stnInfo.getStnId());
                stn.setSndType(sndTypeStr);

                stnPoints.add(stn);
            }
        }
    }

    private void addStnPtWithoutQuery(
            NcSoundingProfile.PfcSndType currentSndType, String refTimeStr,
            String rangeStartStr, String selectedSndTime) {
        String sndTypeStr = currentSndType.toString();
        long reftimeMs = NcSoundingQuery.convertRefTimeStr(refTimeStr);
        Date refTime = new Date(reftimeMs);
        for (NsharpStationInfo stn : stnPoints) {
            if (refTime.equals(stn.getReftime()) == true) {
                long rangetimeMs = NcSoundingQuery
                        .convertRefTimeStr(rangeStartStr);
                Date rangeStartTime = new Date(rangetimeMs);
                NsharpStationInfo.timeLineSpecific timeLinsSpc = stn.new timeLineSpecific();
                int endIndex = Math.min(4, sndTypeStr.length());
                String dispInfo = stn.getStnId() + " " + selectedSndTime + " "
                        + sndTypeStr.substring(0, endIndex);
                timeLinsSpc.setDisplayInfo(dispInfo);
                timeLinsSpc.setTimeLine(rangeStartTime);
                stn.addToTimeLineSpList(timeLinsSpc);
            }
        }
    }

    protected abstract void setSndTimeList(List<String> timeList);

    public abstract void cleanup();

    public abstract void createPfcDialogContents();

}
