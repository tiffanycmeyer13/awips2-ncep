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

import gov.noaa.nws.ncep.viz.rsc.ncgrid.FloatGridData;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.dgdriv.Dgdriv;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.dgdriv.DgdrivException;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.gempak.comms.IGempakCommunicator;
import gov.noaa.nws.ncep.viz.rsc.ncgrid.gempak.exception.GempakProcessingException;

/**
 * Handler for taking a {@link GempakDataRecordRequest}, processing it, and
 * returning a {@link GempakDataRecord}. This is done in a GEMPAK subprocess.
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
public class GempakDataRecordRequestHandler
        implements IGempakRequestHandler<GempakDataRecordRequest> {

    private final IGempakCommunicator communicator;

    /**
     * Constructor
     *
     * @param communicator
     *            used for communicating with CAVE (to request intermediate data
     *            and to send back processed data record)
     */
    public GempakDataRecordRequestHandler(IGempakCommunicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public GempakDataRecord handleRequest(GempakDataRecordRequest request)
            throws GempakProcessingException {
        Dgdriv dgdriv = new Dgdriv(request.getDataInput(),
                new GempakSubprocessDataURIRetriever(communicator),
                new GempakSubprocessDbDataRetriever(communicator));
        try {
            FloatGridData floatData = dgdriv.execute();
            if (floatData != null) {
                return new GempakDataRecord(floatData,
                        dgdriv.getSubgSpatialObj());
            }
        } catch (DgdrivException e) {
            throw new GempakProcessingException(
                    "Error performing GEMPAK data processing", e);
        }
        return null;
    }
}
