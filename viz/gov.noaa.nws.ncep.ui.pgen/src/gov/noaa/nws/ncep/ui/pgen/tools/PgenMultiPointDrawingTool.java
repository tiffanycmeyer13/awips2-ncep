
/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenMultiPointDrawingTool
 *
 * 5 December 2008
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.ui.PlatformUI;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlgFactory;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrSettings;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.FrontAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.PgenDistanceDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.PgenDistanceDlg.DistanceDisplayProperties;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.SigmetAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.SigmetCommAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.TrackAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.TrackExtrapPointInfoDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.vaadialog.VaaCloudDlg;
import gov.noaa.nws.ncep.ui.pgen.display.IText.DisplayType;
import gov.noaa.nws.ncep.ui.pgen.display.IText.FontStyle;
import gov.noaa.nws.ncep.ui.pgen.display.IText.TextJustification;
import gov.noaa.nws.ncep.ui.pgen.display.IText.TextRotation;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.MultiPointElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.elements.Track;
import gov.noaa.nws.ncep.ui.pgen.sigmet.CcfpInfo;
import gov.noaa.nws.ncep.ui.pgen.sigmet.ConvSigmet;
import gov.noaa.nws.ncep.ui.pgen.sigmet.Sigmet;
import gov.noaa.nws.ncep.ui.pgen.sigmet.VolcanoAshCloud;
import gov.noaa.nws.ncep.viz.common.LocatorUtil;

/**
 * Implements a modal map tool for PGEN multiple points drawing.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- ------------------------------------------
 * 02/09                  B. Yin      Initial Creation.
 * 04/09         72       S. Gilbert  Modified to use PgenSession and
 *                                    PgenCommands
 * 04/24         99       G. Hull     Use NmapUiUtils
 * 05/09         42       S. Gilbert  Added pgenType and pgenCategory
 * 05/09         79       B. Yin      Extends from AbstractPgenDrawingTool
 * 10/09         160      G. Zhang    Added Sigmet support
 * 01/10         182      G. Zhang    Added ConvSigmet support
 * 04/10         165      G. Zhang    Added VAA support
 * 02/11         ?        B. Yin      Put Front with labels in DECollection
 * 02/11         318      S. Gilbert  Added option of displaying distance from
 *                                    first point while drawing.
 * 01/12         597      S. Gurung   Removed Snapping for ConvSigmet
 * 02/12         TTR456   Q.Zhou      Added parameters to setTrack()
 * 02/12         597      S. Gurung   Removed snapping for NCON_SIGMET.
 * 05/12         708      J. Wu       Use data frame time for Track element
 * 08/13         1025     J. Wu       Populate VOR data for "Isolated"
 *                                    CONV_SIGMET.
 * Dec 12, 2016  17469    W. Kwock    Added CWA Formatter
 * 02/01/2021   87515       wkwock      Remove CWA
 * Dec 02, 2021  95362    tjensen     Refactor PGEN Resource management to
 *                                    support multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenMultiPointDrawingTool extends AbstractPgenDrawingTool {

    private TrackExtrapPointInfoDlg trackExtrapPointInfoDlg;

    public PgenMultiPointDrawingTool() {

        super();

    }

    @Override
    public void deactivateTool() {

        super.deactivateTool();

        if (trackExtrapPointInfoDlg != null) {
            trackExtrapPointInfoDlg.close();
        }

        if (mouseHandler instanceof PgenMultiPointDrawingHandler) {
            PgenMultiPointDrawingHandler mph = (PgenMultiPointDrawingHandler) mouseHandler;
            if (mph != null) {
                mph.clearPoints();
            }
        }
    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    @Override
    public IInputHandler getMouseHandler() {

        if (this.mouseHandler == null) {
            this.mouseHandler = new PgenMultiPointDrawingHandler();
        }

        return this.mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     *
     * @author bingfan
     *
     */

    public class PgenMultiPointDrawingHandler extends InputHandlerDefaultImpl {

        /**
         * Points of the new element.
         */
        protected ArrayList<Coordinate> points = new ArrayList<>();

        /**
         * Current element.
         */
        protected AbstractDrawableComponent elem;

        /**
         * An instance of DrawableElementFactory, which is used to create new
         * elements.
         */
        protected DrawableElementFactory def = new DrawableElementFactory();

        /**
         * flag for CCFP Text Drawing part
         */
        boolean ccfpTxtFlag = false;

        private final GeodeticCalculator gc = new GeodeticCalculator(
                DefaultEllipsoid.WGS84);

        private final DistanceDisplayProperties distProps = PgenDistanceDlg
                .getInstance(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell())
                .getDistanceProperties();

        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(anX, aY);
            if (loc == null || shiftDown) {
                return false;
            }

            if (button == 1) {
                if ("SIGMET".equalsIgnoreCase(pgenCategory)) {
                    return handleSigmetMouseDown(loc);
                }

                points.add(loc);

                if (isTrackElement(getDrawableType(pgenType))) {
                    if (points.size() == 1) {
                        if (attrDlg instanceof TrackAttrDlg
                                && ((TrackAttrDlg) attrDlg).getFrameTimeButton()
                                        .getSelection()) {
                            String ftime = PgenUtil.getCurrentFrameTime();
                            if (ftime != null && ftime.trim().length() > 0) {
                                ((TrackAttrDlg) attrDlg).getFirstTimeText()
                                        .setText(ftime);
                                ((TrackAttrDlg) attrDlg).getSecondTimeText()
                                        .setText("");
                            }
                        }
                    } else if (points.size() == 2) {
                        if (attrDlg instanceof TrackAttrDlg
                                && ((TrackAttrDlg) attrDlg).getFrameTimeButton()
                                        .getSelection()) {
                            Calendar cal = PgenUtil.getCurrentFrameCalendar();
                            String interval = ((TrackAttrDlg) attrDlg)
                                    .getIntervalTimeString();
                            Calendar secondCal = PgenUtil.getNextCalendar(cal,
                                    interval);
                            String stime = PgenUtil.getFrameTime(secondCal);
                            if (stime != null && stime.trim().length() > 0) {
                                ((TrackAttrDlg) attrDlg).getSecondTimeText()
                                        .setText(stime);
                            }
                        }
                    }
                }

                return true;
            } else if (button == 3) {
                return true;
            } else if (button == 2) {
                return true;
            } else {
                return false;
            }
        }

        /*
         * overrides the function in selecting tool
         */
        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!drawingLayers.isEditable() || shiftDown) {
                return false;
            }

            if (button == 3) {
                if (points.size() == 0) {
                    closeAttrDlg(attrDlg, pgenType);
                    attrDlg = null;
                    PgenUtil.setSelectingMode();

                } else if (points.size() < 2) {

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                } else {
                    // Use pgenType value to decide if the DrawableType should
                    // be TRACK or LINE
                    DrawableType drawableType = getDrawableType(pgenType);

                    // create a new DrawableElement.
                    elem = def.create(drawableType, attrDlg, pgenCategory,
                            pgenType, points, drawingLayers.getActiveLayer());

                    attrDlg.setDrawableElement(elem);
                    AttrSettings.getInstance().setSettings(elem);

                    if ("CCFP_SIGMET".equals(pgenType)) {// avoid 2 Sigmet
                                                         // elements issue
                        ccfpTxtFlag = true;
                        return true;// avoid right click cause no showing issue

                    } else if (elem != null
                            && elem.getPgenCategory().equalsIgnoreCase("Front")
                            && ((FrontAttrDlg) attrDlg).labelEnabled()) {

                        DECollection dec = new DECollection("labeledFront");
                        dec.setPgenCategory(pgenCategory);
                        dec.setPgenType(pgenType);
                        dec.addElement(elem);
                        drawingLayers.addElement(dec);

                        PgenUtil.setDrawingTextMode(true,
                                ((FrontAttrDlg) attrDlg).useFrontColor(), "",
                                dec);
                        elem = null;
                    } else {
                        // add the product to PGEN resource
                        drawingLayers.addElement(elem);
                    }

                    if (isTrackElement(drawableType)) {
                        displayTrackExtrapPointInfoDlg((TrackAttrDlg) attrDlg,
                                (Track) elem);
                    }

                    drawingLayers.removeGhostLine();

                    if (!ccfpTxtFlag) {
                        points.clear();
                    }

                    mapEditor.refresh();

                }

                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean handleMouseDownMove(int aX, int aY, int button) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            } else {
                return true;
            }
        }

        private boolean handleSigmetMouseDown(Coordinate loc) {

            if ("CCFP_SIGMET".equals(pgenType) && ccfpTxtFlag) {
                return handleCcfpMouseDown(loc);
            }

            points.add(loc);

            if ((getSigmetLineType(attrDlg).contains("Text")) || "Isolated"
                    .equalsIgnoreCase(getSigmetLineType(attrDlg))) {

                elem = def.create(getDrawableType(pgenType), attrDlg,
                        pgenCategory, pgenType, points,
                        drawingLayers.getActiveLayer());

                attrDlg.setDrawableElement(elem);
                AttrSettings.getInstance().setSettings(elem);

                drawingLayers.addElement(elem);
                drawingLayers.removeGhostLine();
                points.clear();
                mapEditor.refresh();
            }
            return true;
        }

        private DrawableType getDrawableType(String pgenTypeString) {
            if (Track.TRACK_PGEN_TYPE.equalsIgnoreCase(pgenTypeString)) {
                return DrawableType.TRACK;
            } else if (pgenTypeString.equalsIgnoreCase("jet")) {
                return DrawableType.JET;
            } else if (Sigmet.SIGMET_PGEN_TYPE
                    .equalsIgnoreCase(pgenTypeString)) {
                return DrawableType.SIGMET;
            } else if (ConvSigmet.SIGMET_PGEN_TYPE
                    .equalsIgnoreCase(pgenTypeString)
                    || "NCON_SIGMET".equalsIgnoreCase(pgenTypeString)
                    || "AIRM_SIGMET".equalsIgnoreCase(pgenTypeString)
                    || "OUTL_SIGMET".equalsIgnoreCase(pgenTypeString)
                    || "CCFP_SIGMET".equalsIgnoreCase(pgenTypeString)) {
                return DrawableType.CONV_SIGMET;
            } else if (VolcanoAshCloud.SIGMET_PGEN_TYPE
                    .equalsIgnoreCase(pgenTypeString)) {
                return DrawableType.VAA_CLOUD;
            }
            return DrawableType.LINE;
        }

        private String getSigmetLineType(AttrDlg attrDlg) {// 20091222

            if (pgenType.equalsIgnoreCase("INTL_SIGMET")) {
                return ((SigmetAttrDlg) attrDlg).getLineType();
            } else {

                if (pgenType.equalsIgnoreCase("CCFP_SIGMET")) {
                    return ((gov.noaa.nws.ncep.ui.pgen.attrdialog.vaadialog.CcfpAttrDlg) attrDlg)
                            .getLineType();
                }

                if (pgenType.equalsIgnoreCase("VACL_SIGMET")) {
                    return ((VaaCloudDlg) attrDlg).getLineType();
                }
                return ((SigmetCommAttrDlg) attrDlg).getLineType();
            }
        }

        private boolean isTrackElement(DrawableType drawableType) {
            if (drawableType == DrawableType.TRACK) {
                return true;
            }
            return false;
        }

        private void closeAttrDlg(AttrDlg attrDlgObject,
                String pgenTypeString) {
            if (attrDlgObject == null) {
                return;
            }
            if (DrawableType.TRACK == getDrawableType(pgenTypeString)) {
                TrackAttrDlg tempTrackAttrDlg = (TrackAttrDlg) attrDlgObject;
                closeTrackExtrapPointInfoDlg(
                        tempTrackAttrDlg.getTrackExtrapPointInfoDlg());
                tempTrackAttrDlg = null;
            }
            attrDlgObject.close();
        }

        private void closeTrackExtrapPointInfoDlg(
                TrackExtrapPointInfoDlg dlgObject) {
            if (dlgObject != null) {
                dlgObject.close();
            }
        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            if (!isResourceEditable()) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null) {
                return false;
            }

            DECollection ghost = new DECollection();

            if ("SIGMET".equalsIgnoreCase(pgenCategory)) {
                return handleSigmetMouseMove(loc);
            }

            // create the ghost element and put it in the drawing layer
            AbstractDrawableComponent ghostline = def.create(DrawableType.LINE,
                    attrDlg, pgenCategory, pgenType, points,
                    drawingLayers.getActiveLayer());

            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);
                Line ln = (Line) ghostline;
                ln.setLinePoints(new ArrayList<>(ghostPts));
                ghost.add(ghostline);

                /*
                 * Ghost distance and direction to starting point, if requested
                 */
                if (distProps.displayDistance) {
                    gc.setStartingGeographicPoint(loc.x, loc.y);
                    gc.setDestinationGeographicPoint(points.get(0).x,
                            points.get(0).y);

                    double azimuth = gc.getAzimuth();
                    if (azimuth < 0) {
                        azimuth += 360.0;
                    }
                    double distanceInMeter = gc.getOrthodromicDistance();
                    String distdir = createDistanceString(distanceInMeter,
                            azimuth, distProps);
                    ghost.add(new Text(null, "Courier", 18.f,
                            TextJustification.LEFT_JUSTIFY, loc, 0.0,
                            TextRotation.SCREEN_RELATIVE,
                            new String[] { distdir }, FontStyle.BOLD,
                            Color.YELLOW, 4, 6, true, DisplayType.NORMAL,
                            "TEXT", "General Text"));
                }

                drawingLayers.setGhostLine(ghost);
                mapEditor.refresh();

            }

            return false;

        }

        /*
         * Formats the distance and direction to a meaningful string.
         */
        private String createDistanceString(double distanceInMeter, double dir,
                DistanceDisplayProperties distProps) {

            StringBuilder sb = new StringBuilder();
            String distVal = LocatorUtil.distanceDisplay(distanceInMeter, 1,
                    distProps.distanceUnits.toUpperCase());

            sb.append(distVal);
            sb.append(' ');

            if (distProps.directionUnits
                    .equalsIgnoreCase(PgenDistanceDlg.COMPASS_16_PT)) {
                sb.append(LocatorUtil.ConvertTO16PointDir(dir));
            } else {
                sb.append(String.valueOf((int) dir));
                sb.append("deg");
            }

            return sb.toString();
        }

        private boolean handleSigmetMouseMove(Coordinate loc) {

            if (ccfpTxtFlag) {
                return handleCcfpMouseMove(loc);
            }

            AbstractDrawableComponent ghost = def.create(
                    getDrawableType(pgenType), attrDlg, pgenCategory, pgenType,
                    points, drawingLayers.getActiveLayer());

            if ("Isolated".equalsIgnoreCase(getSigmetLineType(attrDlg))
                    || (getSigmetLineType(attrDlg).contains("Text"))
                    || points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);
                MultiPointElement ln = (MultiPointElement) ghost;
                ln.setLinePoints(new ArrayList<>(ghostPts));

                drawingLayers.setGhostLine(ghost);
                mapEditor.refresh();
            }

            return false;
        }

        public void clearPoints() {
            points.clear();
        }

        private boolean handleCcfpMouseMove(Coordinate loc) {

            ConvSigmet ccfp = (ConvSigmet) def.create(getDrawableType(pgenType),
                    attrDlg, pgenCategory, pgenType, points,
                    drawingLayers.getActiveLayer());

            ccfp.setEditableAttrFromLine("CCFP_SIGMET");

            double[] ad = CcfpInfo.getCcfpTxtAziDir(loc, ccfp);
            if (ad == null) {
                return false;
            }

            ccfp.setEditableAttrFreeText("" + ad[0] + ":::" + ad[1]);

            drawingLayers.setGhostLine(ccfp);

            mapEditor.refresh();
            return false;
        }

        private boolean handleCcfpMouseDown(Coordinate loc) {

            ((ConvSigmet) elem).setEditableAttrFromLine("CCFP_SIGMET");

            double[] ad = CcfpInfo.getCcfpTxtAziDir(loc, (Sigmet) elem);
            if (ad == null) {
                return false;
            }

            ((ConvSigmet) elem)
                    .setEditableAttrFreeText("" + ad[0] + ":::" + ad[1]);

            elem = def.create(getDrawableType(pgenType), attrDlg, pgenCategory,
                    pgenType, points, drawingLayers.getActiveLayer());

            double[] ad2 = CcfpInfo.getCcfpTxtAziDir(loc, (Sigmet) elem);
            if (ad2 == null) {
                return true;
            }

            ((ConvSigmet) elem)
                    .setEditableAttrFreeText("" + ad2[0] + ":::" + ad2[1]);
            ((ConvSigmet) elem).setEditableAttrFromLine("CCFP_SIGMET");

            drawingLayers.addElement(elem);
            drawingLayers.removeGhostLine();
            points.clear();
            mapEditor.refresh();

            ccfpTxtFlag = false;
            return true;
        }

    }

    private void displayTrackExtrapPointInfoDlg(TrackAttrDlg trackAttrDlgObject,
            Track trackObject) {

        if (trackAttrDlgObject == null) {
            return;
        }
        TrackExtrapPointInfoDlg extrapPointInfoDlg = trackAttrDlgObject
                .getTrackExtrapPointInfoDlg();
        if (extrapPointInfoDlg != null) {
            extrapPointInfoDlg.close();
        } else {
            extrapPointInfoDlg = (TrackExtrapPointInfoDlg) AttrDlgFactory
                    .createAttrDlg(Track.TRACK_INFO_DLG_CATEGORY_NAME, pgenType,
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                    .getShell());
            trackExtrapPointInfoDlg = extrapPointInfoDlg;
            trackAttrDlgObject.setTrackExtrapPointInfoDlg(extrapPointInfoDlg);
        }

        /*
         * Open the dialog and set the default attributes. Note: must call
         * "attrDlg.setBlockOnOpen(false)" first.
         */
        extrapPointInfoDlg.setBlockOnOpen(false);
        extrapPointInfoDlg.open();

        extrapPointInfoDlg.setTrack(trackObject,
                trackAttrDlgObject.getUnitComboSelectedIndex(),
                trackAttrDlgObject.getRoundComboSelectedIndex(),
                trackAttrDlgObject.getRoundDirComboSelectedIndex());

        extrapPointInfoDlg.setBlockOnOpen(true);

    }

}
