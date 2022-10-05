/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenContoursTool
 *
 * October 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenConstant;
import gov.noaa.nws.ncep.ui.pgen.PgenSession;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.ContoursAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.ContoursAttrDlg.ContourDrawingStatus;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourCircle;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourLine;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourMinmax;
import gov.noaa.nws.ncep.ui.pgen.contours.Contours;
import gov.noaa.nws.ncep.ui.pgen.controls.CommandStackListener;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.Arc;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

/**
 * Implements a modal map tool for PGEN Contours drawing.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#      Engineer  Description
 * ------------- ------------ --------- ----------------------------------------
 * 10/09         167          J. Wu     Initial creation
 * 12/09         ?            B. Yin    check if the attrDlg is the contours
 *                                      dialog
 * 12/09         167          J. Wu     Allow editing line and label attributes.
 * 06/10         215          J. Wu     Added support for Contours Min/Max
 * 11/10         345          J. Wu     Added support for Contours Circle
 * 02/11                      J. Wu     Preserve auto/hide flags for text
 * 04/11         #?           B. Yin    Re-factor IAttribute
 * 11/11         #?           J. Wu     Add check for the existing Contours of
 *                                      the same type.
 * 03/13         927          B. Yin    Added right mouse click context menu
 * 08/13         TTR778       J. Wu     Move loading libg2g to
 *                                      GraphToGridParamDialog.
 * 04/14         1117         J. Wu     Set focus to label/use line color for
 *                                      label.
 * 05/14         TTR1008      J. Wu     Remove confirmation dialog when adding
 *                                      to an existing contour.
 * 01/15         R5200/T1059  J. Wu     Use setSettings(de) to save last-used
 *                                      attributes.
 * Jan 14, 2016  13168        J. Wu     Add "One Contours per Layer" rule.
 * Jan 27, 2016  13166        J. Wu     Add symbol only & label only capability.
 * Apr 11, 2016  17056        J. Wu     Match contour line/symbol color with
 *                                      settings.
 * May 16, 2016  5640         bsteffen  Access triggering component using
 *                                      PgenUtil.
 * May 25, 2016  17940        J. Wu     Re-work on mouseDown & mouseUp actions.
 * Jun 01, 2016  18387        B. Yin    Open line dialog in activateTool().
 * Jun 13, 2016  10233        J. Wu     Retain flags from previous
 *                                      PgenSelectHandler.
 * Jul 01, 2016  17377        J. Wu     Return control to panning when "SHIFT"
 *                                      is down.
 * Dec 20, 2019  71072        smanoj    Fix some NullPointerException.
 * Dec 02, 2021  95362        tjensen   Refactor PGEN Resource management to
 *                                      support multi-panel displays
 *
 * </pre>
 *
 * @author J. Wu
 */

public class PgenContoursTool extends AbstractPgenDrawingTool
        implements CommandStackListener {

    /**
     * Points of the new element.
     */
    private final ArrayList<Coordinate> points = new ArrayList<>();

    /**
     * An instance of DrawableElementFactory, which is used to create new
     * elements.
     */
    private final DrawableElementFactory def = new DrawableElementFactory();

    /**
     * Current Contours element.
     */
    private boolean addContourLine = false;

    private Contours elem = null;

    private Contours lastElem = null;

    private ExecutionEvent lastEvent = null;

    private int undo = -1;

    private int redo = -1;

    @Override
    protected void activateTool() {
        super.activateTool();

        /*
         * if the ExecutionEvent's trigger has been set, it should be something
         * from a Contours to start with. Load it's attributes to the Contours
         * attr Dialog. If not. we will start with a new Contours.
         */
        AbstractDrawableComponent de = PgenUtil.getTriggerComponent(event);

        /*
         * The same tool could be activated again (for instance, click on PGEN
         * palette and then click in the editor). However the trigger of the
         * event may not be the current contour if the contour is modified.
         */
        if (event != lastEvent) {
            if (de instanceof Contours) {
                elem = (Contours) de;
                this.setPgenSelectHandler();

                // Retain flags from previous PgenSelectHandler.
                if (event
                        .getApplicationContext() instanceof PgenSelectHandler) {
                    ((PgenSelectHandler) this.getMouseHandler()).setPreempt(
                            ((PgenSelectHandler) event.getApplicationContext())
                                    .isPreempt());
                    ((PgenSelectHandler) this.getMouseHandler()).setDontMove(
                            ((PgenSelectHandler) event.getApplicationContext())
                                    .isDontMove());
                }

                PgenSession.getInstance().getPgenPalette()
                        .setActiveIcon(PgenConstant.ACTION_SELECT);
            } else {
                elem = null;
            }
            lastEvent = event;
        }

        if (attrDlg instanceof ContoursAttrDlg) {
            ContoursAttrDlg contoursAttrDialog = (ContoursAttrDlg) attrDlg;
            contoursAttrDialog.setDrawingTool(this);
            if (de != null) {
                contoursAttrDialog.setSelectMode();
            } else {
                contoursAttrDialog.setDrawingStatus(
                        ContoursAttrDlg.ContourDrawingStatus.DRAW_LINE);
                contoursAttrDialog.setLineTemplate(null);
                contoursAttrDialog.openLineAttrDlg();
            }

            contoursAttrDialog.setLabelFocus();
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

            this.mouseHandler = new PgenContoursHandler();

        }

        return this.mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     */
    public class PgenContoursHandler extends InputHandlerDefaultImpl {

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

            // Set focus on label input box.
            if (attrDlg != null) {
                ((ContoursAttrDlg) attrDlg).setLabelFocus();
                if (((ContoursAttrDlg) attrDlg).isShiftDownInContourDialog()) {
                    return false;
                }
            } else {
                return false;
            }

            /*
             * Check drawing mode and draw either symbol, circle or save point
             * to draw line later.
             */
            if (button == 1) {

                if (((ContoursAttrDlg) attrDlg).drawSymbol()) { // symbol
                    drawContourMinmax(loc);
                    ((ContoursAttrDlg) attrDlg).updateSymbolAttrOnGUI(loc);
                } else if (((ContoursAttrDlg) attrDlg).drawCircle()) { // Circle
                    if (points.size() == 0) {
                        points.add(0, loc);
                    } else {
                        if (points.size() > 1) {
                            points.remove(1);
                        }

                        points.add(1, loc);
                        drawContourCircle();
                    }
                } else { // Line
                    points.add(loc);
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

        @Override
        public boolean handleMouseUp(int anX, int aY, int button) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            }

            if (button == 3) {
                // End drawing symbol or circle
                if (((ContoursAttrDlg) attrDlg).drawSymbol()
                        || ((ContoursAttrDlg) attrDlg).drawCircle()) {
                    points.clear();
                    ((ContoursAttrDlg) attrDlg)
                            .setDrawingStatus(ContourDrawingStatus.SELECT);
                    drawingLayers.removeGhostLine();
                } else { // Handling line drawing
                    if (points.size() == 0) {
                        ((ContoursAttrDlg) attrDlg).setDrawingStatus(
                                ContoursAttrDlg.ContourDrawingStatus.SELECT);
                        ((ContoursAttrDlg) attrDlg).closeAttrEditDialogs();
                    } else {
                        setDrawingMode();
                        drawContours();
                    }
                }

                return true;

            } else {
                return false;
            }

        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            }

            // Check if mouse is in geographic extent
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null) {
                return false;
            }

            if (attrDlg != null) {
                ((ContoursAttrDlg) attrDlg).setLabelFocus();
                if (((ContoursAttrDlg) attrDlg).isShiftDownInContourDialog()) {
                    return false;
                }
            }

            // Draw a ghost contour min/max
            if (attrDlg != null && ((ContoursAttrDlg) attrDlg).drawSymbol()) {

                ContoursAttrDlg dlg = (ContoursAttrDlg) attrDlg;
                String symbType = dlg.getActiveSymbolObjType();
                String[] symbLabel = new String[] { dlg.getLabel() };

                if (dlg.isMinmaxSymbolOnly()) {
                    symbLabel = null;
                }

                if (dlg.isMinmaxLabelOnly()) {
                    symbType = null;
                }

                ContourMinmax ghost = null;
                ghost = new ContourMinmax(loc, dlg.getActiveSymbolClass(),
                        symbType, symbLabel, dlg.hideSymbolLabel());

                IAttribute mmTemp = dlg.getMinmaxTemplate();

                if (ghost.getSymbol() != null) {
                    if (mmTemp != null) {
                        Symbol oneSymb = (Symbol) (ghost.getSymbol());
                        oneSymb.update(mmTemp);
                    }
                }

                if (ghost.getLabel() != null) {
                    IAttribute lblTemp = dlg.getLabelTemplate();
                    if (lblTemp != null) {
                        Text lbl = ghost.getLabel();
                        String[] oldText = lbl.getText();
                        boolean hide = lbl.getHide();
                        boolean auto = lbl.getAuto();
                        lbl.update(lblTemp);
                        lbl.setText(oldText);
                        lbl.setHide(hide);
                        lbl.setAuto(auto);
                    }

                    if (dlg.isUseMainColor() && mmTemp != null) {
                        ghost.getLabel().setColors(mmTemp.getColors());
                    }
                }

                drawingLayers.setGhostLine(ghost);
                mapEditor.refresh();

                return false;

            }

            // Draw a ghost contour circle
            if (attrDlg != null && ((ContoursAttrDlg) attrDlg).drawCircle()) {

                if (points != null && points.size() >= 1) {

                    ContourCircle ghost = new ContourCircle(points.get(0), loc,
                            new String[] {
                                    ((ContoursAttrDlg) attrDlg).getLabel() },
                            ((ContoursAttrDlg) attrDlg).hideCircleLabel());

                    IAttribute circleTemp = ((ContoursAttrDlg) attrDlg)
                            .getCircleTemplate();
                    if (circleTemp != null) {
                        ghost.getCircle().setColors(circleTemp.getColors());
                        ((Arc) ghost.getCircle())
                                .setLineWidth(circleTemp.getLineWidth());
                    }

                    IAttribute lblTemp = ((ContoursAttrDlg) attrDlg)
                            .getLabelTemplate();
                    if (lblTemp != null) {
                        Text lbl = ghost.getLabel();
                        String[] oldText = lbl.getText();
                        boolean hide = lbl.getHide();
                        boolean auto = lbl.getAuto();
                        lbl.update(lblTemp);
                        lbl.setText(oldText);
                        lbl.setHide(hide);
                        lbl.setAuto(auto);
                    }

                    if (((ContoursAttrDlg) attrDlg).isUseMainColor()
                            && circleTemp != null) {
                        ghost.getLabel().setColors(circleTemp.getColors());
                    }

                    drawingLayers.setGhostLine(ghost);
                    mapEditor.refresh();
                }

                return false;

            }

            // Draw a ghost ContourLine
            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);

                ContourLine cline = new ContourLine(ghostPts,
                        ((ILine) attrDlg).isClosedLine(),
                        new String[] { ((ContoursAttrDlg) attrDlg).getLabel() },
                        ((ContoursAttrDlg) attrDlg).getNumOfLabels());

                IAttribute lineTemp = ((ContoursAttrDlg) attrDlg)
                        .getLineTemplate();

                if (lineTemp != null) {
                    Line oneLine = cline.getLine();
                    Boolean isClosed = oneLine.isClosedLine();
                    oneLine.update(lineTemp);
                    oneLine.setClosed(isClosed);
                }

                String lblstr = ((ContoursAttrDlg) attrDlg).getLabel();
                if (lblstr != null
                        && lblstr.contains(PgenConstant.G2G_BOUND_MARK)) {
                    cline.getLine().setSmoothFactor(0);
                }

                IAttribute lblTemp = ((ContoursAttrDlg) attrDlg)
                        .getLabelTemplate();
                for (Text lbl : cline.getLabels()) {
                    if (lblTemp != null) {
                        String[] oldText = lbl.getText();
                        boolean hide = lbl.getHide();
                        boolean auto = lbl.getAuto();
                        lbl.update(lblTemp);
                        lbl.setText(oldText);
                        lbl.setHide(hide);
                        lbl.setAuto(auto);
                    }

                    if (((ContoursAttrDlg) attrDlg).isUseMainColor()
                            && lineTemp != null) {
                        lbl.setColors(lineTemp.getColors());
                    }
                }

                Contours el = (Contours) (def.create(DrawableType.CONTOURS,
                        null, "MET", PgenConstant.CONTOURS, points,
                        drawingLayers.getActiveLayer()));

                cline.setParent(el);
                cline.getLine().setPgenType(
                        ((ContoursAttrDlg) attrDlg).getContourLineType());

                el.update((ContoursAttrDlg) attrDlg);
                el.add(cline);

                drawingLayers.setGhostLine(el);
                mapEditor.refresh();

            }

            return false;

        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            }
            if (attrDlg != null) {
                ((ContoursAttrDlg) attrDlg).setLabelFocus();
                if (((ContoursAttrDlg) attrDlg).isShiftDownInContourDialog()) {
                    return false;
                }
            }

            return true;
        }

        /*
         * create a Contours and add to the Pgen Resource.
         */
        private void drawContours() {

            if (points.size() > 1) {

                ContourLine cline = new ContourLine(points,
                        ((ILine) attrDlg).isClosedLine(),
                        new String[] { ((ContoursAttrDlg) attrDlg).getLabel() },
                        ((ContoursAttrDlg) attrDlg).getNumOfLabels());

                cline.getLine().setPgenType(
                        ((ContoursAttrDlg) attrDlg).getContourLineType());

                IAttribute lineTemp = ((ContoursAttrDlg) attrDlg)
                        .getLineTemplate();
                if (lineTemp != null) {
                    Line oneLine = cline.getLine();
                    Boolean isClosed = oneLine.isClosedLine();
                    oneLine.update(lineTemp);
                    oneLine.setClosed(isClosed);
                    ((ContoursAttrDlg) attrDlg).setSettings(oneLine.copy());
                }

                String lblstr = ((ContoursAttrDlg) attrDlg).getLabel();
                if (lblstr != null
                        && lblstr.contains(PgenConstant.G2G_BOUND_MARK)) {
                    cline.getLine().setSmoothFactor(0);
                }

                IAttribute lblTemp = ((ContoursAttrDlg) attrDlg)
                        .getLabelTemplate();
                for (Text lbl : cline.getLabels()) {
                    if (lblTemp != null) {
                        String[] oldText = lbl.getText();
                        boolean hide = lbl.getHide();
                        boolean auto = lbl.getAuto();
                        lbl.update(lblTemp);
                        lbl.setText(oldText);
                        lbl.setHide(hide);
                        lbl.setAuto(auto);

                        ((ContoursAttrDlg) attrDlg).setSettings(lbl.copy());
                    }

                    if (((ContoursAttrDlg) attrDlg).isUseMainColor()
                            && lineTemp != null) {
                        lbl.setColors(lineTemp.getColors());
                    }
                }

                // Check if we need to add to existing contours or create a new
                // one
                elem = checkExistingContours();

                if (elem == null) {

                    /*
                     * create a new element with attributes from the Attr
                     * dialog, and add it to the PGEN Resource
                     */
                    elem = (Contours) (def.create(DrawableType.CONTOURS, null,
                            "MET", PgenConstant.CONTOURS, points,
                            drawingLayers.getActiveLayer()));

                    cline.setParent(elem);
                    elem.update((ContoursAttrDlg) attrDlg);
                    elem.add(cline);
                    drawingLayers.addElement(elem);

                } else {
                    /*
                     * Make a copy of the existing element; update its
                     * attributes from those in the Attr Dialog; replace the
                     * existing element with the new one in the pgen resource -
                     * (This allows Undo/Redo)
                     */
                    Contours newElem = elem.copy();
                    cline.setParent(newElem);
                    newElem.update((ContoursAttrDlg) attrDlg);
                    newElem.add(cline);

                    drawingLayers.replaceElement(elem, newElem);
                    elem = newElem;

                }

                ((ContoursAttrDlg) attrDlg).setCurrentContours(elem);

            }

            // Always clear the points for the next drawing.
            points.clear();

            // Update the display.
            drawingLayers.removeGhostLine();
            mapEditor.refresh();

        }

        /*
         * create a Contours and add to the Pgen Resource.
         */
        public void drawContourMinmax(Coordinate loc) {

            if (loc != null) {

                ContoursAttrDlg contoursDlg = (ContoursAttrDlg) attrDlg;

                String cls = contoursDlg.getActiveSymbolClass();
                String type = contoursDlg.getActiveSymbolObjType();
                String[] symbLabel = new String[] { contoursDlg.getLabel() };

                if (contoursDlg.isMinmaxSymbolOnly()) {
                    symbLabel = null;
                }

                if (contoursDlg.isMinmaxLabelOnly()) {
                    type = null;
                }

                ContourMinmax cmm = new ContourMinmax(loc, cls, type, symbLabel,
                        contoursDlg.hideSymbolLabel());

                IAttribute mmTemp = (contoursDlg).getMinmaxTemplate();
                if (cmm.getSymbol() != null) {
                    if (mmTemp != null) {
                        Symbol oneSymb = (Symbol) (cmm.getSymbol());
                        oneSymb.update(mmTemp);
                        contoursDlg.setSettings(oneSymb.copy());
                    }
                }

                if (cmm.getLabel() != null) {
                    IAttribute lblTemp = contoursDlg.getLabelTemplate();

                    if (lblTemp != null) {
                        Text lbl = cmm.getLabel();
                        String[] oldText = lbl.getText();
                        boolean hide = lbl.getHide();
                        boolean auto = lbl.getAuto();
                        lbl.update(lblTemp);
                        lbl.setText(oldText);
                        lbl.setHide(hide);
                        lbl.setAuto(auto);

                        contoursDlg.setSettings(lbl.copy());
                    }

                    if (contoursDlg.isUseMainColor() && mmTemp != null) {
                        cmm.getLabel().setColors(mmTemp.getColors());
                    }
                }

                // Check if we need to add to existing contours or create a new
                // one
                elem = checkExistingContours();

                if (elem == null) {
                    /*
                     * create a new element with attributes from the Attr
                     * dialog, and add it to the PGEN Resource
                     */
                    elem = (Contours) (def.create(DrawableType.CONTOURS, null,
                            "MET", PgenConstant.CONTOURS, points,
                            drawingLayers.getActiveLayer()));

                    cmm.setParent(elem);
                    elem.update((ContoursAttrDlg) attrDlg);
                    elem.add(cmm);

                    drawingLayers.addElement(elem);

                } else {

                    /*
                     * Make a copy of the existing element; update its
                     * attributes from those in the Attr Dialog; replace the
                     * existing element with the new one in the pgen resource -
                     * (This allows Undo/Redo)
                     */
                    Contours newElem = elem.copy();
                    cmm.setParent(newElem);
                    newElem.update(contoursDlg);
                    newElem.add(cmm);

                    drawingLayers.replaceElement(elem, newElem);

                    lastElem = elem;
                    elem = newElem;
                }

                contoursDlg.setCurrentContours(elem);
            }

            // Always clear the points for the next drawing.
            points.clear();

            // Update the display.
            drawingLayers.removeGhostLine();
            mapEditor.refresh();

        }

        /*
         * Set drawing mode for adding a contour line or a new Contours.
         */
        private void setDrawingMode() {

            if (points.size() == 0) {
                if (elem == null) {

                    // quit Contours drawing
                    if (attrDlg != null) {
                        attrDlg.close();
                    }

                    attrDlg = null;
                    addContourLine = false;

                    PgenUtil.setSelectingMode();
                } else {

                    // start a new Contours element - new points will be drawn
                    // as new ContourLine in a new Contours element.
                    if (!addContourLine) {
                        elem = null;
                    } else { // back to selecting
                        PgenUtil.setSelectingMode();
                    }
                }
            }
        }

        /*
         * Add a circle to Contours.
         */
        private void drawContourCircle() {

            if (points != null && points.size() > 1) {

                ContourCircle cmm = new ContourCircle(points.get(0),
                        points.get(1),
                        new String[] { ((ContoursAttrDlg) attrDlg).getLabel() },
                        ((ContoursAttrDlg) attrDlg).hideCircleLabel());

                IAttribute circleTemp = (((ContoursAttrDlg) attrDlg)
                        .getCircleTemplate());
                if (circleTemp != null) {
                    cmm.getCircle().setColors(circleTemp.getColors());
                    ((Arc) cmm.getCircle())
                            .setLineWidth(circleTemp.getLineWidth());
                    ((ContoursAttrDlg) attrDlg)
                            .setSettings(cmm.getCircle().copy());
                }

                IAttribute lblTemp = ((ContoursAttrDlg) attrDlg)
                        .getLabelTemplate();
                if (lblTemp != null) {
                    Text lbl = cmm.getLabel();
                    String[] oldText = lbl.getText();
                    boolean hide = lbl.getHide();
                    boolean auto = lbl.getAuto();
                    lbl.update(lblTemp);
                    lbl.setText(oldText);
                    lbl.setHide(hide);
                    lbl.setAuto(auto);
                    ((ContoursAttrDlg) attrDlg).setSettings(lbl.copy());
                }

                if (((ContoursAttrDlg) attrDlg).isUseMainColor()
                        && circleTemp != null) {
                    cmm.getLabel().setColors(circleTemp.getColors());
                }

                // Check if we need to add to existing contours or create a new
                // one
                elem = checkExistingContours();

                if (elem == null) {
                    /*
                     * create a new element with attributes from the Attr
                     * dialog, and add it to the PGEN Resource
                     */
                    elem = (Contours) (def.create(DrawableType.CONTOURS, null,
                            "MET", PgenConstant.CONTOURS, points,
                            drawingLayers.getActiveLayer()));

                    cmm.setParent(elem);
                    elem.update((ContoursAttrDlg) attrDlg);
                    elem.add(cmm);

                    drawingLayers.addElement(elem);

                } else {

                    /*
                     * Make a copy of the existing element; update its
                     * attributes from those in the Attr Dialog; replace the
                     * existing element with the new one in the pgen resource -
                     * (This allows Undo/Redo)
                     */
                    Contours newElem = elem.copy();

                    cmm.setParent(newElem);

                    newElem.update((ContoursAttrDlg) attrDlg);

                    newElem.add(cmm);

                    drawingLayers.replaceElement(elem, newElem);

                    elem = newElem;

                }

                ((ContoursAttrDlg) attrDlg).setCurrentContours(elem);

            }

            // Always clear the points for the next drawing.
            points.clear();

            // Update the display.
            drawingLayers.removeGhostLine();
            mapEditor.refresh();

        }

        /*
         * Loop through current layer and see if there is an same type of
         * Contours. If yes, add to the existing contours. If not, draw a new
         * Contours.
         *
         * If "one contour per layer" rule is forced and cannot find the same
         * type of Contours, the first Contours in the layer is used.
         */
        private Contours checkExistingContours() {

            Contours existingContours = elem;

            if (existingContours == null) {
                existingContours = ((ContoursAttrDlg) attrDlg)
                        .findExistingContours();
            }

            return existingContours;
        }

    }

    public void resetUndoRedoCount() {
        undo = -1;
        redo = -1;
    }

    @Override
    public void stacksUpdated(int undoSize, int redoSize) {

        if (undoSize < undo || redoSize < redo) {
            // there is an undo or a redo
            Contours tmp = elem;
            elem = lastElem;
            lastElem = tmp;

        }

        undo = undoSize;
        redo = redoSize;

    }

    /**
     * Gets the current working contour.
     *
     * @return
     */
    public Contours getCurrentContour() {
        return elem;
    }

    /**
     * Sets the current working contour
     *
     * @param con
     */
    public void setCurrentContour(Contours con) {
        if (con != null) {
            elem = con;
            if (attrDlg != null) {
                attrDlg.setDrawableElement(con);
            }
        }
    }

    /**
     * Sets the selecting handler.
     */
    public void setPgenSelectHandler() {

        setHandler(
                new PgenSelectHandler(this, mapEditor, drawingLayers, attrDlg));
    }

    /**
     * Sets the contour mouse handler.
     */
    public void setPgenContoursHandler() {

        setHandler(new PgenContoursHandler());

    }

    /**
     * Clears selected elements.
     */
    public void clearSelected() {
        drawingLayers.removeSelected();
        points.clear();
        mapEditor.refresh();
    }

    /**
     * Gets the default mouse handler.
     */
    @Override
    protected IInputHandler getDefaultMouseHandler() {
        return new PgenSelectHandler(this, mapEditor, drawingLayers, attrDlg);
    }

    /**
     * Sets current working component
     */
    @Override
    protected void setWorkingComponent(AbstractDrawableComponent adc) {
        if (adc instanceof Contours) {
            setCurrentContour((Contours) adc);
            ((ContoursAttrDlg) attrDlg).setCurrentContours(elem);
        }
    }
}
