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

import gov.noaa.nws.ncep.ui.nsharp.NsharpConfigManager;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConfigStore;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConstants;
import gov.noaa.nws.ncep.ui.nsharp.NsharpGraphProperty;
import gov.noaa.nws.ncep.ui.nsharp.SurfaceStationPointData;
import gov.noaa.nws.ncep.ui.nsharp.display.NsharpEditor;
import gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpModelSoundingQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.dataplugin.grid.GridInfoConstants;
import com.raytheon.uf.common.dataplugin.grid.GridInfoRecord;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.view.ModelSoundingDialogContents
 * 
 * This java class performs the NSHARP NsharpLoadDialog functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ----------   ---------   ----------  -----------
 * 01/2011       229         Chin Chen   Initial coding
 * 03/09/2015    RM#6674     Chin Chen   Support model sounding query data 
 *                                       interpolation and nearest point option
 * Aug 05,2015   4486        rjpeter     Changed Timestamp to Date.
 * 07202015      RM#9173     Chin Chen   use NcSoundingQuery.genericSoundingDataQuery()
 *                                       to query grid model sounding data
 * 08/24/2015    RM#10188    Chin Chen   Model selection upgrades - use grid resource 
 *                                       definition name for model type display
 * 09/28/2015    RM#10295    Chin Chen   Let sounding data query run in its own thread
 *                                       to avoid gui locked out during load
 * 04/05/2016    RM#10435    rjpeter     Removed Inventory usage.
 * 04/02/2020    73571       smanoj      NSHARP D2D port refactor
 * 07/16/2020    80425       smanoj      Added queryLimit for NSHARP time queries.
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpModelSoundingDialogContents
        extends AbstractMdlSoundingDlgContents {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NsharpModelSoundingDialogContents.class);

    private Group availableFileGp, sndTimeListGp;

    private Button timeBtn;

    private boolean timeLimit = false;

    private org.eclipse.swt.widgets.List modelTypeRscDefNameList = null,
            availableFileList = null, sndTimeList = null;

    private static final String SND_TIMELINE_NOT_AVAIL_STRING = "No Sounding Time for Nsharp";

    private List<String> selectedFileList = new ArrayList<>();

    private List<String> selectedTimeList = new ArrayList<>();

    // For NSHARP in D2D queryLimit is set to the number of Frames in the
    // D2D Display, but for NCP, pass in 0 and then ignore the checks in
    // NsharpModelSoundingQuery.
    private int queryLimit = 0;

    public NsharpModelSoundingDialogContents(Composite parent) {
        super(parent);
        ldDia = NsharpLoadDialog.getAccess();
        newFont = ldDia.getNewFont();
        try {
            createModelTypeToRscDefNameMapping();
        } catch (VizException e) {
            statusHandler.handle(Priority.ERROR,
                    "NsharpModelSoundingDialogContents: exception while createModelTypeToRscDefNameMapping.",
                    e);
        }
    }

    private void createModelTypeList() {
        if (modelTypeRscDefNameList != null) {
            modelTypeRscDefNameList.removeAll();
        }
        if (sndTimeList != null) {
            sndTimeList.removeAll();
        }
        if (availableFileList != null) {
            availableFileList.removeAll();
        }
        ldDia.startWaitCursor();
        List<String> cfgList = null;
        NsharpConfigManager configMgr = NsharpConfigManager.getInstance();
        NsharpConfigStore configStore = configMgr
                .retrieveNsharpConfigStoreFromFs();
        NsharpGraphProperty graphConfigProperty = configStore
                .getGraphProperty();
        cfgList = graphConfigProperty.getGribModelTypeList();
        DbQueryRequest dbQuery = new DbQueryRequest();
        dbQuery.setEntityClass(GridInfoRecord.class);
        dbQuery.setDistinct(true);
        dbQuery.addRequestField(GridInfoConstants.DATASET_ID);

        try {
            DbQueryResponse response = (DbQueryResponse) ThriftClient
                    .sendRequest(dbQuery);
            String[] models = response.getFieldObjects(
                    GridInfoConstants.DATASET_ID, String.class);
            Arrays.sort(models, String.CASE_INSENSITIVE_ORDER);

            /*
             * the returned string has format like this, "gfsP5". Therefore, we
             * do not have to process on it.
             */
            for (String modelName : models) {
                String rscDefName = gridModelToRscDefNameMap.get(modelName);
                if ((cfgList != null) && (cfgList.size() > 0)) {
                    if (cfgList.contains(rscDefName)) {
                        if (rscDefName != null) {
                            modelTypeRscDefNameList.add(rscDefName);
                        }
                    }
                } else if (rscDefName != null) {
                    modelTypeRscDefNameList.add(rscDefName);
                }
            }
        } catch (VizException e) {
            statusHandler.handle(Priority.ERROR,
                    "Exception occured loading available models", e);
        }

        ldDia.stopWaitCursor();
    }

    private void handleAvailFileListSelection() {
        String selectedFile = null;
        if (availableFileList.getSelectionCount() > 0) {
            selectedFileList.clear();
            for (int i = 0; i < availableFileList.getSelectionCount(); i++) {
                selectedFile = availableFileList.getSelection()[i];
                selectedFileList.add(selectedFile);
            }
            createMDLSndTimeList(selectedFileList, selectedRscDefName,
                    selectedModelType);
        }
    }

    private void handleSndTimeSelection() {
        String selectedSndTime = null;
        if ((sndTimeList.getSelectionCount() > 0)
                && (sndTimeList.getSelection()[0]
                        .equals(SND_TIMELINE_NOT_AVAIL_STRING) == false)) {
            selectedTimeList.clear();
            for (int i = 0; i < sndTimeList.getSelectionCount(); i++) {
                selectedSndTime = sndTimeList.getSelection()[i];
                selectedTimeList.add(selectedSndTime);
            }
            ldDia.getMapRsc().bringMapEditorToTop();
        }
    }

    public void createMdlDialogContents() {
        topGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        topGp.setLayout(new GridLayout(2, false));
        selectedModelType = ldDia.getActiveMdlSndMdlType();
        selectedRscDefName = rscDefNameToGridModelMap.get(selectedModelType);
        ldDia.createSndTypeList(topGp);

        modelTypeGp = new Group(topGp, SWT.SHADOW_ETCHED_IN);
        modelTypeGp.setText("Model Type");
        modelTypeGp.setFont(newFont);
        modelTypeRscDefNameList = new org.eclipse.swt.widgets.List(modelTypeGp,
                SWT.BORDER | SWT.V_SCROLL);
        modelTypeRscDefNameList.setBounds(modelTypeGp.getBounds().x,
                modelTypeGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.filelistWidth, NsharpConstants.listHeight);
        // query to get and add available sounding models from DB
        modelTypeRscDefNameList.setFont(newFont);
        createModelTypeList();

        // create a selection listener to handle user's selection on list
        modelTypeRscDefNameList.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (modelTypeRscDefNameList.getSelectionCount() > 0) {
                    selectedRscDefName = modelTypeRscDefNameList
                            .getSelection()[0];
                    // convert selectedModel, in resource definition name, to
                    // grid model type name
                    selectedModelType = rscDefNameToGridModelMap
                            .get(selectedRscDefName);
                    ldDia.setActiveMdlSndMdlType(selectedModelType);
                    createMDLAvailableFileList(selectedModelType);
                }
            }
        });

        availableFileGp = new Group(topGp, SWT.SHADOW_ETCHED_IN);
        availableFileGp.setText("Available Grid files:");
        availableFileGp.setFont(newFont);
        availableFileList = new org.eclipse.swt.widgets.List(availableFileGp,
                SWT.BORDER | SWT.V_SCROLL);
        availableFileList.setBounds(availableFileGp.getBounds().x,
                availableFileGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.filelistWidth, NsharpConstants.listHeight);
        availableFileList.setFont(newFont);
        // create a selection listener to handle user's selection on list
        availableFileList.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                handleAvailFileListSelection();
            }
        });

        // create Sounding Times widget list
        sndTimeListGp = new Group(topGp, SWT.SHADOW_ETCHED_IN);
        sndTimeListGp.setText("Sounding Times:");
        sndTimeListGp.setFont(newFont);
        sndTimeList = new org.eclipse.swt.widgets.List(sndTimeListGp,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        sndTimeList.removeAll();
        sndTimeList.setFont(newFont);
        sndTimeList.setBounds(sndTimeListGp.getBounds().x,
                sndTimeListGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.listWidth, NsharpConstants.listHeight);
        sndTimeList.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                handleSndTimeSelection();
            }
        });
        timeBtn = new Button(topGp, SWT.CHECK | SWT.BORDER);
        timeBtn.setText("00Z and 12Z only");
        timeBtn.setEnabled(true);
        timeBtn.setFont(newFont);
        timeBtn.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (timeLimit) {
                    timeLimit = false;
                } else {
                    timeLimit = true;
                }

                // refresh sounding list if file type is selected already
                if ((selectedModelType != null)
                        && (selectedFileList.size() > 0)) {
                    createMDLSndTimeList(selectedFileList, selectedRscDefName,
                            selectedModelType);
                }
            }
        });
        locationMainGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        locationMainGp.setLayout(new GridLayout(5, false));
        locationMainGp.setText("Location");
        locationMainGp.setFont(newFont);
        latlonBtn = new Button(locationMainGp, SWT.RADIO | SWT.BORDER);
        latlonBtn.setText("Lat/Lon");
        latlonBtn.setFont(newFont);
        latlonBtn.setEnabled(true);
        latlonBtn.setSelection(true);
        latlonBtn.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setCurrentLocType(LocationType.LATLON);
                locationText.setText("");
            }
        });
        stationBtn = new Button(locationMainGp, SWT.RADIO | SWT.BORDER);
        stationBtn.setText("Station");
        stationBtn.setEnabled(true);
        stationBtn.setFont(newFont);
        stationBtn.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setCurrentLocType(LocationType.STATION);
                locationText.setText("");
            }
        });
        locationLbl = new Label(locationMainGp, SWT.NONE | SWT.BORDER);
        locationLbl.setText("Location:");
        locationLbl.setFont(newFont);
        locationText = new Text(locationMainGp, SWT.BORDER | SWT.SINGLE);
        GridData data1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        locationText.setLayoutData(data1);
        locationText.setTextLimit(MAX_LOCATION_TEXT);
        locationText.setFont(newFont);
        locationText.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String userInputStr = e.text;
                if (userInputStr.length() > 0) {

                    if (currentLocType == LocationType.LATLON) {
                        // to make sure user enter digits and separated by ";"
                        // or ","only, if lat/lon is used
                        if (userInputStr.length() == 1) {
                            char inputChar = userInputStr.charAt(0);
                            if (!(('0' <= inputChar) && (inputChar <= '9'))
                                    && (inputChar != ';') && (inputChar != ',')
                                    && (inputChar != '-')
                                    && (inputChar != '.')) {
                                e.doit = false;
                                return;
                            }
                        }
                    } else {
                        // do nothing when station type

                    }
                }
            }
        });

        loadBtn = new Button(locationMainGp, SWT.PUSH);
        loadBtn.setText("Load ");
        loadBtn.setFont(newFont);
        loadBtn.setEnabled(true);
        loadBtn.setBounds(
                locationMainGp.getBounds().x + NsharpConstants.btnGapX,
                locationLbl.getBounds().y + locationLbl.getBounds().height
                        + NsharpConstants.btnGapY,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        loadBtn.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                NsharpLoadDialog ldDia = NsharpLoadDialog.getAccess();
                if ((selectedTimeList != null)
                        && (selectedTimeList.size() == 0)) {
                    ldDia.setAndOpenMb(
                            "Time line(s) is not selected!\n Can not load data!");
                    return;
                }
                String textStr = locationText.getText();
                if ((textStr != null) && !(textStr.isEmpty())) {
                    if (currentLocType == LocationType.LATLON) {
                        // to make sure user enter digits and separated by ";"
                        // or ","only, if lat/lon is used
                        int dividerIndex = textStr.indexOf(';');
                        boolean indexFound = false;
                        if (dividerIndex != -1) {
                            indexFound = true;
                        }
                        if (indexFound == false) {
                            dividerIndex = textStr.indexOf(',');
                            if (dividerIndex != -1) {
                                indexFound = true;
                            }
                        }
                        if (indexFound == true) {
                            try {
                                lat = Float.parseFloat(
                                        textStr.substring(0, dividerIndex));
                                lon = Float.parseFloat(
                                        textStr.substring(dividerIndex + 1));
                                if ((lat > 90) || (lat < -90) || (lon > 180)
                                        || (lon < -180)) {
                                    ldDia.setAndOpenMb("lat/lon out of range ("
                                            + textStr + ") entered!\n"
                                            + GOOD_LATLON_STR);
                                    locationText.setText("");
                                    return;
                                }
                                NsharpModelSoundingQuery qryAndLd = new NsharpModelSoundingQuery(
                                        "Querying Sounding Data...", queryLimit);
                                NsharpEditor skewtEdt = NsharpEditor
                                        .createOrOpenEditor();
                                qryAndLd.queryAndLoadData(false, skewtEdt,
                                        soundingLysLstMap, selectedTimeList,
                                        timeLineToFileMap, lat, lon, stnStr,
                                        selectedModelType, selectedRscDefName);

                            } catch (Exception e) {
                                statusHandler.handle(Priority.ERROR,
                                        "NsharpModelSoundingDialogContents: exception while parsing string to float.",
                                        e);
                                return;
                            }

                        } else {
                            ldDia.setAndOpenMb("Bad lat/lon (" + textStr
                                    + ") entered!\n" + GOOD_LATLON_STR);
                            locationText.setText("");
                            return;
                        }
                    } else if (currentLocType == LocationType.STATION) {
                        // query station lat /lon
                        try {
                            // user may start with a space before enter station
                            // id
                            textStr = textStr.trim();
                            stnStr = textStr.toUpperCase(Locale.getDefault());
                            Coordinate co = SurfaceStationPointData
                                    .getStnCoordinate(stnStr);
                            lat = (float) co.y;
                            lon = (float) co.x;
                            if (lat == SurfaceStationPointData.DEFAULT_LATLON) {
                                ldDia.setAndOpenMb("Bad station id (" + textStr
                                        + ") entered!\n" + GOOD_STN_STR);
                                locationText.setText("");
                                return;
                            }
                            NsharpModelSoundingQuery qryAndLd = new NsharpModelSoundingQuery(
                                    "Querying Sounding Data...", queryLimit);
                            NsharpEditor skewtEdt = NsharpEditor
                                    .createOrOpenEditor();
                            qryAndLd.queryAndLoadData(true, skewtEdt,
                                    soundingLysLstMap, selectedTimeList,
                                    timeLineToFileMap, lat, lon, stnStr,
                                    selectedModelType, selectedRscDefName);
                        } catch (Exception e) {
                            statusHandler.handle(Priority.ERROR,
                                    "NsharpModelSoundingDialogContents: exception while parsing string to float.",
                                    e);

                            return;
                        }
                    }
                }
            }
        });

        if ((selectedModelType != null)
                && (selectedModelType.equals("") == false)) {
            String[] selectedModelArray = { selectedModelType };
            selectedRscDefName = gridModelToRscDefNameMap
                    .get(selectedModelType);
            modelTypeRscDefNameList.setSelection(selectedModelArray);
            createMDLAvailableFileList(selectedModelType);

            Object[] selFileObjectArray = selectedFileList.toArray();
            String[] selFileStringArray = Arrays.copyOf(selFileObjectArray,
                    selFileObjectArray.length, String[].class);
            availableFileList.setSelection(selFileStringArray);
            handleAvailFileListSelection();

            Object[] selTimeObjectArray = selectedTimeList.toArray();
            String[] selTimeStringArray = Arrays.copyOf(selTimeObjectArray,
                    selTimeObjectArray.length, String[].class);
            sndTimeList.setSelection(selTimeStringArray);
            handleSndTimeSelection();
        }
    }

    public void cleanup() {
        if (modelTypeRscDefNameList != null) {
            if (modelTypeRscDefNameList
                    .getListeners(SWT.Selection).length > 0) {
                modelTypeRscDefNameList.removeListener(SWT.Selection,
                        modelTypeRscDefNameList.getListeners(SWT.Selection)[0]);
            }
            modelTypeRscDefNameList.dispose();
            modelTypeRscDefNameList = null;
        }
        if (modelTypeGp != null) {
            modelTypeGp.dispose();
            modelTypeGp = null;
        }
        if (timeBtn != null) {
            timeBtn.removeListener(SWT.MouseUp,
                    timeBtn.getListeners(SWT.MouseUp)[0]);
            timeBtn.dispose();
            timeBtn = null;
        }

        NsharpLoadDialog ldDia = NsharpLoadDialog.getAccess();
        ldDia.cleanSndTypeList();

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
        if (topGp != null) {
            topGp.dispose();
            topGp = null;
        }

        if (loadBtn != null) {
            loadBtn.removeListener(SWT.MouseUp,
                    loadBtn.getListeners(SWT.MouseUp)[0]);
            loadBtn.dispose();
            loadBtn = null;
        }
        if (stationBtn != null) {
            stationBtn.removeListener(SWT.MouseUp,
                    stationBtn.getListeners(SWT.MouseUp)[0]);
            stationBtn.dispose();
            stationBtn = null;
        }
        if (latlonBtn != null) {
            latlonBtn.removeListener(SWT.MouseUp,
                    latlonBtn.getListeners(SWT.MouseUp)[0]);
            latlonBtn.dispose();
            latlonBtn = null;
        }
        if (locationText != null) {
            locationText.removeListener(SWT.Verify,
                    locationText.getListeners(SWT.Verify)[0]);
            locationText.dispose();
            locationText = null;
        }

        if (locationLbl != null) {
            locationLbl.dispose();
            locationLbl = null;
        }
        if (locationMainGp != null) {
            locationMainGp.dispose();
            locationMainGp = null;
        }
    }

    @Override
    protected void setAvailableFileList(List<String> fileList) {
        for (int i = 0; i < fileList.size(); i++) {
            this.availableFileList.add(fileList.get(i));
        }
    }

    @Override
    protected void setSndTimeList(List<String> timeList) {
        this.sndTimeList.removeAll();
        for (int i = 0; i < timeList.size(); i++) {
            this.sndTimeList.add(timeList.get(i));
        }
    }

    @Override
    protected void setTimeLineToFileMap(Map<String, String> timeLineToFileMap) {
        this.timeLineToFileMap = timeLineToFileMap;
    }

}
