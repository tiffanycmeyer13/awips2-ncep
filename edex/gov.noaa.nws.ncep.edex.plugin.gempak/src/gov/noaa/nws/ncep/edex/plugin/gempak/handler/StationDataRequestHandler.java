package gov.noaa.nws.ncep.edex.plugin.gempak.handler;

import gov.noaa.nws.ncep.common.dataplugin.gempak.request.StationDataRequest;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.pointdata.PointDataContainer;
import com.raytheon.uf.common.pointdata.PointDataDescription.Type;
import com.raytheon.uf.common.pointdata.PointDataView;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.pointdata.PointDataQuery;

/**
 * Executes point data queries for station data from GEMPAK. Both surface and
 * sounding requests are performed via this handler. Queries are performed for a
 * single station for one timestamp.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * ???          ???         ???        Initial creation
 * Aug 07, 2014 3478       bclement    removed PointDataDescription.Type.Double
 * Sep 23, 2016 17968      pmoyer      Fixed database query assembly for UAIR
 * </pre>
 * 
 * @version 1.0
 */
public class StationDataRequestHandler implements
        IRequestHandler<StationDataRequest> {

    private static final String STATION_ID = "location.stationId";

    private static final String REF_TIME = "dataTime.refTime";

    private static final String REF_HOUR = "refHour";

    private static final String REP_TYPE = "reportType";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Map<String, Object> handleRequest(StationDataRequest request)
            throws Exception {

        Map<String, Object> params = new HashMap<>();

        PointDataQuery query = new PointDataQuery(request.getPluginName());

        // changed query parameters to properly retrieve upper-air bufrua data
        // records from the database.
        query.setParameters(request.getParmList());
        query.addParameter(STATION_ID, request.getStationId(), "=");
        if (!request.getPluginName().equalsIgnoreCase("bufrua")) {
            query.addParameter(REF_HOUR, request.getRefTime().toString(), "=");
            query.addParameter(REF_TIME, request.getRefTime().toString(), "<=");
        } else {
            query.addParameter(REF_TIME, request.getRefTime().toString(), "=");
        }
        if (!request.getPartNumber().equals("0")) {
            query.addParameter(REP_TYPE, request.getPartNumber(), "=");
        }

        query.requestAllLevels();

        PointDataContainer container = null;
        container = query.execute();
        if (container == null) {
            return params;
        }

        for (int n = 0; n < container.getAllocatedSz(); n++) {
            PointDataView pdv = container.readRandom(n);

            for (String param : pdv.getContainer().getParameters()) {
                int dimensions = pdv.getDimensions(param);
                Type t = pdv.getType(param);
                switch (t) {
                case FLOAT:
                case INT:
                case LONG:
                    if (dimensions == 2) {
                        params.put(param, pdv.getNumberAllLevels(param));
                    } else {
                        params.put(param, pdv.getNumber(param));
                    }
                    break;
                case STRING:
                    if (dimensions == 2) {
                        params.put(param, pdv.getStringAllLevels(param));
                    } else {
                        params.put(param, pdv.getString(param));
                    }
                    break;
                }
            }
        }

        return params;
    }
}
