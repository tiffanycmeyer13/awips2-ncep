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
package gov.noaa.nws.ncep.ui.nsharp.display.map;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpMapResourceData
 * 
 * This java class performs the NSHARP ResourceData functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/23/2010   229         Chin Chen   Initial coding
 * 03/31/2020   73571       smanoj      NSHARP D2D port refactor
 *
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpMapResourceData extends AbstractNsharpMapResourceData {

    public NsharpMapResourceData() {
        super();
    }

    @Override
    public AbstractNsharpMapResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        return new NsharpMapResource(this, loadProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NsharpMapResourceData)) {
            return false;
        }
        NsharpMapResourceData rdata = (NsharpMapResourceData) obj;
        if (this.markerState.equals(rdata.getMarkerState())
                && this.markerType.equals(rdata.getMarkerType())
                && this.markerSize.equals(rdata.getMarkerSize())
                && this.markerWidth.equals(rdata.getMarkerWidth())
                && this.markerTextSize.equals(rdata.getMarkerTextSize())
                && this.stnMarkerType.equals(rdata.getStnMarkerType())) {
            return true;
        } else {
            return false;
        }
    }
}
