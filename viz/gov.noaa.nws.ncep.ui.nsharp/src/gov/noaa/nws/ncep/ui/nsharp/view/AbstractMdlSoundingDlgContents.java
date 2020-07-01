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

import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingLayer;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingProfile.MdlSndType;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingTimeLines;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceCategory;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceDefinition;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceDefnsMngr;
import gov.noaa.nws.ncep.viz.soundingrequest.NcSoundingQuery;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.common.dataplugin.grid.GridConstants;
import com.raytheon.uf.common.dataplugin.grid.GridRecord;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

/**
 *
 * Provides a base implementation for NSHARP Model Sounding Dialog Contents.
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
public abstract class AbstractMdlSoundingDlgContents {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractMdlSoundingDlgContents.class);

    protected Composite parent;

    protected AbstractNsharpLoadDialog ldDia;

    protected List<String> availableFileList = new ArrayList<>();

    protected List<String> sndTimeList = new ArrayList<>();

    // timeLineToFileMap maps time line (rangeStart time in sndTimeList) to
    // available file (reftime in availableFileList)
    protected Map<String, String> timeLineToFileMap = new HashMap<>();

    // soundingLysLstMap maps "lat;lon timeline" string to its queried sounding
    // layer list
    protected final Map<String, List<NcSoundingLayer>> soundingLysLstMap = new HashMap<>();

    protected org.eclipse.swt.widgets.List modelTypeRscDefNameList = null;

    protected Group modelTypeGp, bottomGp, topGp, locationMainGp;

    protected Button latlonBtn, stationBtn, loadBtn;

    protected Text locationText;

    protected Label locationLbl;

    protected float lat, lon;

    protected String stnStr = "";

    protected final String GOOD_LATLON_STR = " A good input looked like this:\n 38.95;-77.45 or 38.95,-77.45";

    protected final String GOOD_STN_STR = " A good input looked like this:\n GAI or gai";

    protected final int MAX_LOCATION_TEXT = 15;

    // used for query to database
    protected String selectedModelType = "";

    // use for display on GUI
    protected String selectedRscDefName = "";

    protected static Map<String, String> gridModelToRscDefNameMap = new HashMap<>();

    protected static Map<String, String> rscDefNameToGridModelMap = new HashMap<>();

    protected Font newFont;

    public enum LocationType {
        LATLON, STATION
    }

    protected LocationType currentLocType = LocationType.LATLON;

    public AbstractMdlSoundingDlgContents(Composite parent) {
        this.parent = parent;
    }

    public LocationType getCurrentLocType() {
        return currentLocType;
    }

    public void setCurrentLocType(LocationType locType) {
        this.currentLocType = locType;
    }

    public Text getLocationText() {
        return locationText;
    }

    protected void createMDLAvailableFileList(String selectedModelType) {
        if (sndTimeList != null) {
            sndTimeList.clear();
        }
        if (availableFileList != null) {
            availableFileList.clear();
        }

        final String timeField = "dataTime.refTime";
        DbQueryRequest dbQuery = new DbQueryRequest();
        dbQuery.setEntityClass(GridRecord.class);
        dbQuery.setDistinct(true);
        dbQuery.addConstraint(GridConstants.DATASET_ID,
                new RequestConstraint(selectedModelType));
        dbQuery.addRequestField(timeField);

        try {
            DbQueryResponse response = (DbQueryResponse) ThriftClient
                    .sendRequest(dbQuery);
            Date[] reftimes = response.getFieldObjects(timeField, Date.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Set<String> formattedTimesSet = new HashSet<>(reftimes.length, 1);
            for (Date reftime : reftimes) {
                formattedTimesSet.add(sdf.format(reftime));
            }
            availableFileList.addAll(formattedTimesSet);

        } catch (VizException e) {
            statusHandler.error("Exception occurred looking up times for "
                    + selectedModelType, e.getMessage());
        }
        setAvailableFileList(availableFileList);
    }

    protected void createMDLSndTimeList(List<String> availableFileList,
            String selectedRscDefName, String selectedModelType) {

        if (availableFileList.size() <= 0) {
            return;
        }
        if (sndTimeList != null) {
            sndTimeList.clear();

        }
        if (timeLineToFileMap != null) {
            timeLineToFileMap.clear();
        }
        // set max resource name length to 10 chars for displaying
        int nameLen = Math.min(10, selectedRscDefName.length());
        String modelName = selectedRscDefName.substring(0, nameLen);
        // query using NcSoundingQuery to query
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] defaultDays = dfs.getShortWeekdays();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        for (int i = 0; i < availableFileList.size(); i++) {
            String fl = availableFileList.get(i);
            int fileYear = Integer.parseInt(fl.substring(0, 4));
            int calYear = Calendar.getInstance().get(Calendar.YEAR);

            //process current data
            if (fileYear > calYear) {
                continue;
            }
            long reftimeMs = NcSoundingQuery.convertRefTimeStr(fl);
            NcSoundingTimeLines timeLines = NcSoundingQuery
                    .soundingRangeTimeLineQuery(MdlSndType.ANY.toString(), fl,
                            selectedModelType);
            if ((timeLines != null) && (timeLines.getTimeLines().length > 0)
                    && sndTimeList.isEmpty()) {
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
                            vHour, modelName, dayOfWeek);
                    if (sndTimeList.indexOf(gmtTimeStr) != -1) {
                        // gmtTimeStr is already in the sndTimeList,
                        // don't need to add it to list again.
                        continue;
                    }
                    if (!sndTimeList.contains(gmtTimeStr)) {
                        sndTimeList.add(gmtTimeStr);
                        timeLineToFileMap.put(gmtTimeStr, fl);
                    }
                }
            }
        }
        setSndTimeList(sndTimeList);
        setTimeLineToFileMap(timeLineToFileMap);
    }

    protected void createModelTypeToRscDefNameMapping() throws VizException {
        ResourceDefnsMngr rscDefnsMngr = ResourceDefnsMngr.getInstance();
        gridModelToRscDefNameMap.clear();
        rscDefNameToGridModelMap.clear();
        if (rscDefnsMngr != null) {
            ResourceCategory cat = ResourceCategory.createCategory("GRID");
            List<ResourceDefinition> rscTypes = rscDefnsMngr
                    .getResourceDefnsForCategory(cat);
            for (ResourceDefinition rd : rscTypes) {
                HashMap<String, String> rpmap = rd.getResourceParameters(false);
                if (rpmap != null) {
                    String mdlType = rpmap.get("GDFILE");
                    String rscDefName = rd.getResourceDefnName();
                    gridModelToRscDefNameMap.put(mdlType, rscDefName);
                    rscDefNameToGridModelMap.put(rscDefName, mdlType);
                } else {
                    continue;
                }
            }
        }
    }

    protected abstract void setAvailableFileList(
            List<String> availableFileList);

    protected abstract void setSndTimeList(List<String> sndTimeList);

    protected abstract void setTimeLineToFileMap(
            Map<String, String> timeLineToFileMap);

    public abstract void cleanup();

    public abstract void createMdlDialogContents();

}
