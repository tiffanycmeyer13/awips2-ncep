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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.geotools.referencing.GeodeticCalculator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ncep.ui.nsharp.SurfaceStationPointData;
import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingLayer;
import gov.noaa.nws.ncep.ui.nsharp.NsharpStationInfo;
import gov.noaa.nws.ncep.ui.nsharp.view.AbstractMdlSoundingDlgContents;
import gov.noaa.nws.ncep.ui.nsharp.view.AbstractNsharpLoadDialog;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;

/**
 *
 * Provides a base implementation for NSHARP Map Mouse Handler.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- -----------------
 * Mar 25, 2020  73571    smanoj   Initial creation
 * Jul 14, 2020  80425    smanoj   Fixing a Null Pointer Exception.
 * Jul 16, 2020  80425    smanoj   Added method to get queryLimit, for D2D use
 *                                 the number of frames D2D is set to display.
 * </pre>
 *
 * @author smanoj
 */
public abstract class AbstractNsharpMapMouseHandler extends InputHandlerDefaultImpl {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NsharpMapMouseHandler.class);

    private static final double NctextuiPointMinDistance = 45000;

    private AbstractNsharpMapResource mapRsc;

    public AbstractNsharpMapMouseHandler() {
        super();
     }

    @Override
    public boolean handleMouseDown(int x, int y, int button) {
        return false;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        return false;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int button) {
        AbstractNsharpLoadDialog loadDia = getLoadDialog();
        if (loadDia == null) {
            return false;
        }

        mapRsc = loadDia.getMapRsc();
        if (!mapRsc.isEditable()) {
            return false;
        }

        // button 1 is left mouse button
        if (button == 1) {
            AbstractEditor mapEditor = mapRsc.getMapEditor();
            if (mapEditor != null) {
                // Check if mouse is in geographic extent
                Coordinate loc = mapEditor.translateClick(x, y);
                if (loc == null)
                    return false;
                if (loadDia != null) {
                    if (loadDia
                            .getActiveLoadSoundingType() == AbstractNsharpLoadDialog.MODEL_SND
                            && loadDia.getMdlDialog() != null
                            && loadDia.getMdlDialog().getLocationText() != null
                            && (!(loadDia.getMdlDialog().getLocationText())
                                    .isDisposed())) {
                        if (loadDia.getMdlDialog()
                                .getCurrentLocType() == AbstractMdlSoundingDlgContents.LocationType.STATION) {
                            String stnName = SurfaceStationPointData
                                    .calculateNearestPoint(loc);
                            if (stnName == null)
                                stnName = "";
                            loadDia.getMdlDialog().getLocationText()
                                    .setText(stnName);
                        } else {
                            String latLonStr = String.format("%6.2f;%7.2f",
                                    loc.y, loc.x);
                            loadDia.getMdlDialog().getLocationText()
                                    .setText(latLonStr);
                        }

                    } else {
                        // get the stn (point) list
                        int activeLoadType = loadDia
                                .getActiveLoadSoundingType();
                        List<NsharpStationInfo> points = mapRsc
                                .getOrCreateNsharpMapResource().getPoints();
                        if (points != null && !points.isEmpty()) {
                            // get the stn close to loc "enough" and retrieve
                            // report for it
                            // Note::One stn may have more than one dataLine, if
                            // user picked multiple data time lines
                            List<NsharpStationInfo> stnPtDataLineLst = getPtWithinMinDist(
                                    points, loc);
                            if (stnPtDataLineLst != null
                                    && stnPtDataLineLst.size() > 0) {
                                // hash map, use stn display info as key
                                Map<String, List<NcSoundingLayer>> soundingLysLstMap = 
                                        new HashMap<String, List<NcSoundingLayer>>();
                                if (activeLoadType == AbstractNsharpLoadDialog.OBSER_SND) {
                                    NsharpObservedSoundingQuery obsQry = new NsharpObservedSoundingQuery(
                                            "Querying Sounding Data...",
                                            getQueryLimit());
                                    obsQry.getObservedSndData(stnPtDataLineLst,
                                            loadDia.getObsDialog().isRawData(),
                                            soundingLysLstMap);
                                } else if (activeLoadType == AbstractNsharpLoadDialog.PFC_SND) {
                                    NsharpPfcSoundingQuery pfcQry = new NsharpPfcSoundingQuery(
                                            "Querying Sounding Data...",
                                            getQueryLimit());
                                    pfcQry.getPfcSndDataBySndTmRange(
                                            stnPtDataLineLst,
                                            soundingLysLstMap);
                                }
                                /*
                                 * Add GPD code here when we start to support
                                 * it.
                                 */
                                else
                                    return false;
                            }
                        }
                    }
                }
            }

        } else if (button == 3) {
            // button 3 is right button.
            bringSkewTEdToTop();
        }

        return false;
    }

    /*
     * Chin Note: If calling NsharpEditor.bringSkewTEditorToTop() directly in
     * mouse handler API, e.g. handleMouseUp(), then handleMouseUp() will be
     * called one more time by System. Do not know the root cause of it. To
     * avoid handling such event twice (e.g. query sounding data twice), we will
     * call NsharpEditor.bringSkewTEditorToTop() from an UiJob.
     */
    private void bringSkewTEdToTop() {
        Job uijob = new UIJob("bring skewT to top") {
            public IStatus runInUIThread(IProgressMonitor monitor) {
                mapRsc.bringMapEditorToTop();
                return Status.OK_STATUS;
            }
        };
        uijob.setSystem(true);
        uijob.schedule();
    }

    /**
     * Gets the nearest point of an selected element to the input point
     * 
     * @param el
     *            element
     * @param pt
     *            input point
     * @return
     */
    private List<NsharpStationInfo> getPtWithinMinDist(
            List<NsharpStationInfo> points, Coordinate pt) {

        NsharpStationInfo thePoint = null;
        double minDistance = NctextuiPointMinDistance;
        GeodeticCalculator gc;
        List<NsharpStationInfo> thePoints = new ArrayList<NsharpStationInfo>();
        // can't assume this is a map Editor/MapDescriptor
        AbstractEditor mapEditor = mapRsc.getMapEditor();
        if (mapEditor != null) {
            IMapDescriptor desc = (IMapDescriptor) mapEditor
                    .getActiveDisplayPane().getRenderableDisplay()
                    .getDescriptor();
            gc = new GeodeticCalculator(desc.getCRS());
            gc.setStartingGeographicPoint(pt.x, pt.y);
            for (NsharpStationInfo point : points) {

                gc.setDestinationGeographicPoint(point.getLongitude(),
                        point.getLatitude());
                double dist;
                try {
                    dist = gc.getOrthodromicDistance();
                    if (dist < minDistance) {

                        minDistance = dist;
                        thePoint = point;
                    }
                } catch (Exception e) {
                    statusHandler.handle(Priority.ERROR,
                            "NsharpMapMouseHandler: getOrthodromicDistance exception happened!",
                            e);
                }
            }
            // Chin, there may be more than one point for a selected stn. As
            // user may selected more than one data time,
            // For same stn, each data time will have one point to represent it.
            // So, a stn may have more than one points
            if (thePoint != null) {
                for (NsharpStationInfo point : points) {
                    if ((thePoint.getLatitude() == point.getLatitude())
                            && (thePoint.getLongitude() == point
                                    .getLongitude())) {
                        thePoints.add(point);
                    }
                }

                // marked X on selected point
                mapRsc.getOrCreateNsharpMapResource().setPickedPoint(thePoint);
            }
        }
        return thePoints;
    }

    public abstract AbstractNsharpLoadDialog getLoadDialog();

    public abstract int getQueryLimit();
}
