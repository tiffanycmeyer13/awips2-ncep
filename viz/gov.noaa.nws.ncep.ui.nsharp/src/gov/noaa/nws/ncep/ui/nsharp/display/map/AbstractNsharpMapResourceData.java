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

import gov.noaa.nws.ncep.viz.common.ui.Markers.MarkerState;
import gov.noaa.nws.ncep.viz.common.ui.Markers.MarkerTextSize;
import gov.noaa.nws.ncep.viz.common.ui.Markers.MarkerType;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;


/**
*
* Provides a base implementation for NSHARP Sounding Load Dialog.
*
* <pre>
*
* SOFTWARE HISTORY
*
* Date          Ticket#  Engineer  Description
* ------------- -------- --------- -----------------
* Mar 23, 2020  73571    smanoj   Initial creation
*
* </pre>
*
* @author smanoj
*/
public abstract class AbstractNsharpMapResourceData extends AbstractResourceData {

    protected MarkerState markerState = MarkerState.MARKER_ONLY;

    protected MarkerType markerType = MarkerType.DIAMOND;

    protected Float markerSize = 1f;

    protected Integer markerWidth = 2;

    protected MarkerTextSize markerTextSize = MarkerTextSize.MEDIUM;

    protected String mapName = "NSHARP";

    protected MarkerType stnMarkerType = MarkerType.LARGE_X;

    public AbstractNsharpMapResourceData() {
        super();
    }

    @Override
    public void update(Object updateData) {

    }

    public MarkerType getStnMarkerType() {
        return stnMarkerType;
    }

    public MarkerState getMarkerState() {
        return markerState;
    }

    public void setMarkerState(MarkerState markerState) {
        this.markerState = markerState;
    }

    public MarkerType getMarkerType() {
        return markerType;
    }

    public void setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
    }

    public Float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(Float markerSize) {
        this.markerSize = markerSize;
    }

    public Integer getMarkerWidth() {
        return markerWidth;
    }

    public void setMarkerWidth(Integer markerWidth) {
        this.markerWidth = markerWidth;
    }

    public MarkerTextSize getMarkerTextSize() {
        return markerTextSize;
    }

    public void setMarkerTextSize(MarkerTextSize markerTextSize) {
        this.markerTextSize = markerTextSize;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public abstract boolean equals(Object obj);
}
