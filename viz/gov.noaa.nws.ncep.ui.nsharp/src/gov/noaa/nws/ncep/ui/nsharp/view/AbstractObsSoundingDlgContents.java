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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 *
 * Provides a base implementation for NSHARP Observed Sounding Dialog Contents.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- -----------------
 * Mar 20, 2020  73571    smanoj   Initial creation
 * Jun 22, 2020  79556    smanoj   Fixing some errors and enhancements.
 * 
 * </pre>
 *
 * @author smanoj
 */

public abstract class AbstractObsSoundingDlgContents {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractObsSoundingDlgContents.class);

    protected Composite parent;

    protected AbstractNsharpLoadDialog ldDia;

    protected Group btnGp, topGp;

    protected boolean timeLimit = false;

    protected boolean rawData = false;

    protected Button bufruaBtn, uairBtn;

    protected String FILE_UAIR = "UAIR";

    protected String FILE_BUFRUA = "BUFRUA";

    protected org.eclipse.swt.widgets.List sndTimeList;

    protected NcSoundingProfile.ObsSndType currentSndType = NcSoundingProfile.ObsSndType.NONE;

    protected List<String> selectedTimeList = new ArrayList<String>();

    protected List<NsharpStationInfo> stnPoints = new ArrayList<NsharpStationInfo>();

    protected List<String> timeList = new ArrayList<String>();

    protected Font newFont;

    public AbstractObsSoundingDlgContents(Composite parent) {
        this.parent = parent;
    }

    public boolean isRawData() {
        return rawData;
    }

    public NcSoundingProfile.ObsSndType getCurrentSndType() {
        return currentSndType;
    }

    protected void createTimeList(NcSoundingProfile.ObsSndType currentSndType) {
        if (timeList != null) {
            timeList.clear();
        }
        NcSoundingTimeLines timeLines = NcSoundingQuery
                .soundingTimeLineQuery(currentSndType.toString());
        if (timeLines != null && timeLines.getTimeLines() != null) {
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] defaultDays = dfs.getShortWeekdays();
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

            for (Object timeLine : timeLines.getTimeLines()) {
                Date synoptictime = (Date) timeLine;
                if (synoptictime != null) {
                    // need to format synoptictime to GMT time string.
                    // Date.toString produce a local time Not GMT time
                    cal.setTimeInMillis(synoptictime.getTime());
                    String dayOfWeek = defaultDays[cal
                            .get(Calendar.DAY_OF_WEEK)];
                    String gmtTimeStr = String.format(
                            "%1$ty%1$tm%1$td/%1$tH(%3$s) %2$s", cal,
                            currentSndType.toString(), dayOfWeek);
                    if (!timeLimit) {
                        if (!timeList.contains(gmtTimeStr)) {
                            timeList.add(gmtTimeStr);
                        }
                    } else {
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        // "00z and 12z only hour
                        if ((hour == 0) || (hour == 12)) {
                            if (!timeList.contains(gmtTimeStr)) {
                                timeList.add(gmtTimeStr);
                            }
                        }
                    }
                }
            }

        } else {
            statusHandler.handle(Priority.INFO,
                    "EDEX timeline query return null");
        }
        setSndTimeList(timeList);
    }

    protected void handleSndTimeSelection(
            NcSoundingProfile.ObsSndType currentSndType) {
        String selectedSndTime = null;

        if (selectedTimeList.isEmpty()) {
            statusHandler.handle(Priority.WARN,
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

    protected void queryAndMarkStn(NcSoundingProfile.ObsSndType currentSndType,
            String refTimeStr, String rangeStartStr, String selectedSndTime) {
        double lat, lon;
        String stnInfoStr;

        // use NcSoundingQuery to query stn info
        NcSoundingStnInfoCollection sndStnInfoCol = NcSoundingQuery
                .genericSoundingStnInfoQuery(currentSndType.toString(),
                        refTimeStr, rangeStartStr);

        if (sndStnInfoCol != null && sndStnInfoCol.getStationInfo() != null) {
            NcSoundingStnInfo[] stnInfoAry = sndStnInfoCol.getStationInfo();

            // Note: A same station may have many reports
            for (int i = 0; i < stnInfoAry.length; i++) {
                NcSoundingStnInfo stnInfo = stnInfoAry[i];
                Date synoptictime = null;
                stnInfoStr = stnInfo.getStnId();
                if (stnInfoStr == null || stnInfoStr.length() < 1)
                    stnInfoStr = "*";

                lat = stnInfo.getStationLatitude();
                lon = stnInfo.getStationLongitude();
                synoptictime = (Date) stnInfo.getSynopTime();

                // convert to Nsharp's own station info struct
                NsharpStationInfo stn = new NsharpStationInfo();
                String packedStnInfoStr = stnInfoStr.replace(" ", "_");
                stn.setStnDisplayInfo(packedStnInfoStr + " " + selectedSndTime
                        + " " + currentSndType.toString());
                stn.setLongitude(lon);
                stn.setLatitude(lat);
                stn.setStnId(stnInfoStr);
                stn.setReftime(synoptictime);
                stn.setRangestarttime(synoptictime);
                stn.setSndType(currentSndType.toString());
                stnPoints.add(stn);
            }
        }
    }

    private void addStnPtWithoutQuery(
            NcSoundingProfile.ObsSndType currentSndType, String refTimeStr,
            String rangeStartStr, String selectedSndTime) {
        long reftimeMs = NcSoundingQuery.convertRefTimeStr(refTimeStr);
        Date refTime = new Date(reftimeMs);
        for (NsharpStationInfo stn : stnPoints) {
            if (refTime.equals(stn.getReftime()) == true) {
                long rangetimeMs = NcSoundingQuery
                        .convertRefTimeStr(rangeStartStr);
                Date rangeStartTime = new Date(rangetimeMs);
                NsharpStationInfo.timeLineSpecific timeLinsSpc = stn.new timeLineSpecific();
                String sndTypeStr = currentSndType.toString();
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

    public abstract void createObsDialogContents();

}
