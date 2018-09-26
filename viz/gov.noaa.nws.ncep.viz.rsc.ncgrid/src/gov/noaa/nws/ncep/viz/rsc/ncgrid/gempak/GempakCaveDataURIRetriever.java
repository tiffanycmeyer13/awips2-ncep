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
package gov.noaa.nws.ncep.viz.rsc.ncgrid.gempak;

import java.util.Map;

import com.raytheon.uf.common.comm.CommunicationException;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.grid.dataquery.GridQueryAssembler;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ncep.viz.common.util.CommonDateFormatUtil;
import gov.noaa.nws.ncep.viz.gempak.grid.units.GempakGridVcrdInfoLookup;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.NcgribLogger;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.dgdriv.GridDBConstants;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.dgdriv.NcgridDataCache;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.gempak.exception.GempakException;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.gempak.exception.GempakProcessingException;

/**
 * Implementation of {@link IGempakDataURIRetriever} for retrieving GEMPAK data
 * URIs from within CAVE.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 10, 2018 54483      mapeters    Initial creation
 *
 * </pre>
 *
 * @author mapeters
 */
public class GempakCaveDataURIRetriever implements IGempakDataURIRetriever {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(GempakCaveDataURIRetriever.class);

    private final NcgridDataCache dataCache;

    /**
     * Constructor.
     *
     * @param dataCache
     *            cache to use for retrieving/storing data
     */
    public GempakCaveDataURIRetriever(NcgridDataCache dataCache) {
        this.dataCache = dataCache;
    }

    @Override
    public String getDataURI(GempakDataURIRequest gempakRequest)
            throws GempakException {
        long t0 = System.currentTimeMillis();

        String parameters = gempakRequest.getParameters();
        String datauri = dataCache.getDataURI(parameters);
        if (datauri != null) {
            if (NcgribLogger.getInstance().enableDiagnosticLogs()) {
                long t00 = System.currentTimeMillis();
                logger.debug("++++ getDataURIFromAssembler for(" + parameters
                        + ") from cache took: " + (t00 - t0));
            }
            return datauri;
        }
        Map<String, RequestConstraint> rcMap = getRequestConstraint(
                gempakRequest);
        if (NcgribLogger.getInstance().enableDiagnosticLogs()) {
            long t01 = System.currentTimeMillis();
            logger.debug("++++ getRequestConstraint for(" + parameters
                    + ") took: " + (t01 - t0));
        }
        if (rcMap == null) {
            return null;
        }

        t0 = System.currentTimeMillis();
        DbQueryRequest request = new DbQueryRequest();
        request.setConstraints(rcMap);

        DbQueryResponse response;
        try {
            response = (DbQueryResponse) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            throw new GempakProcessingException(
                    "Error requesting GEMPAK data URI from DB for request: "
                            + gempakRequest,
                    e);
        }
        PluginDataObject[] pdos = response
                .getEntityObjects(PluginDataObject.class);
        if (pdos.length > 0) {
            datauri = pdos[0].getDataURI();
        } else {
            datauri = null;
        }

        long t1 = System.currentTimeMillis();
        if (NcgribLogger.getInstance().enableDiagnosticLogs()) {
            String refTime = rcMap.get(GridDBConstants.REF_TIME_QUERY)
                    .getConstraintValue();
            int fcstTime = Integer
                    .parseInt(rcMap.get(GridDBConstants.FORECAST_TIME_QUERY)
                            .getConstraintValue());
            String fcstTimeg = CommonDateFormatUtil
                    .getForecastColonTimeString(fcstTime);
            if (datauri != null) {
                logger.debug("### getDataURIFromAssembler(" + datauri + ") for("
                        + parameters + ") reftime:" + refTime + "(" + fcstTimeg
                        + ") took: " + (t1 - t0));
            } else {
                logger.debug("??? getDataURIFromAssembler(null) for("
                        + parameters + ") reftime:" + refTime + "(" + fcstTimeg
                        + ") took: " + (t1 - t0));
            }
        }

        if (datauri != null) {
            dataCache.addDataURI(parameters, datauri);
        }
        return datauri;
    }

    /*
     * Use unified grid plugin mapping
     */
    private Map<String, RequestConstraint> getRequestConstraint(
            GempakDataURIRequest gempakRequest) {
        String parameters = gempakRequest.getParameters();
        GridQueryAssembler qAssembler = new GridQueryAssembler("GEMPAK");
        String[] parmList = parameters.split("\\|");
        if (NcgribLogger.getInstance().enableDiagnosticLogs()) {
            logger.debug(
                    "enter getRequestConstraint - parameters:" + parameters);
        }
        qAssembler.setDatasetId(parmList[0]);

        if (!parmList[1].isEmpty()) {
            if (gempakRequest.isEnsCategory()) {
                if (!parmList[1].equalsIgnoreCase("null")) {
                    qAssembler.setEnsembleId(parmList[1]);
                }
            } else {
                if (gempakRequest.getEnsembleMember() != null) {
                    qAssembler.setEnsembleId(parmList[1]);
                } else {
                    qAssembler.setSecondaryId(parmList[1]);
                }
            }
        }
        qAssembler.setParameterAbbreviation(parmList[2]);
        qAssembler.setMasterLevelName(parmList[3]);

        int scale_vcord = 1;
        String unitParm = GempakGridVcrdInfoLookup.getInstance()
                .getVcrdUnit(parmList[3]);
        if ((unitParm == null) || unitParm.isEmpty() || unitParm.equals("-")) {
            try {
                scale_vcord = (int) Math.pow(10,
                        Integer.valueOf(GempakGridVcrdInfoLookup.getInstance()
                                .getParmScale((parmList[3]))));
            } catch (NumberFormatException e) {
                //
            }
        }

        String ll1 = null, ll2 = null;
        if (parmList[4].contains(":")) {
            ll1 = parmList[4].split(":")[0];
            ll2 = parmList[4].split(":")[1];
        } else {
            ll1 = parmList[4];
        }
        if (ll1 == null && ll2 == null) {
            return null;
        }
        if (ll1 != null) {
            Double level1;
            try {
                level1 = Double.valueOf(ll1);
            } catch (NumberFormatException e) {
                return null;
            }
            qAssembler.setLevelOneValue(level1 / scale_vcord);
        }
        if (ll2 != null) {
            Double level2;
            try {
                level2 = Double.valueOf(ll2);
            } catch (NumberFormatException e) {
                return null;
            }
            qAssembler.setLevelTwoValue(level2 / scale_vcord);
        } else {
            qAssembler.setLevelTwoValue(-999999.0);
        }

        qAssembler.setLevelUnits(GempakGridVcrdInfoLookup.getInstance()
                .getVcrdUnit(parmList[3]));
        Map<String, RequestConstraint> rcMap;
        try {
            rcMap = qAssembler.getConstraintMap();
        } catch (CommunicationException e) {
            if (NcgribLogger.getInstance().enableDiagnosticLogs()) {
                logger.debug("getConstraintMap - CommunicationException:"
                        + e.toString());
            }
            return null;
        }
        String refTimeg = parmList[5].toUpperCase().split("F")[0];
        String refTime = CommonDateFormatUtil.dattimToDbtime(refTimeg);
        String fcstTimeInSec = Integer.toString(
                CommonDateFormatUtil.getForecastTimeInSec(parmList[5]));

        rcMap.put(GridDBConstants.REF_TIME_QUERY,
                new RequestConstraint(refTime));
        rcMap.put(GridDBConstants.FORECAST_TIME_QUERY,
                new RequestConstraint(fcstTimeInSec));
        if (NcgribLogger.getInstance().enableDiagnosticLogs()) {
            logger.debug(
                    "exit getRequestConstraint - rcMap:" + rcMap.toString());
        }
        return rcMap;
    }
}
