
/*
 * gov.noaa.nws.ncep.ui.pgen.rsc.PgenOutlookDrawingTool
 *
 * 31 March 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;
import java.util.Iterator;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.OutlookAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Outlook;

/**
 * Implements a modal map tool for PGEN outlook drawing.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 03/10         ?        B. Yin    Initial Creation.
 * 04/11         ?        B. Yin    Re-factor IAttribute
 * 12/11         582      Q.Zhou    changed hard coded line type in mouse
 *                                  down/move for TROPICAL
 * 03/2012       599      Q.Zhou    Added FLOOD
 * 05/12         710      B. Yin    Set the outlook type to layer name
 * 08/13         TTR784   B. Yin    Remove code that sets the line attributes to
 *                                  the last used in order to set the attributes
 *                                  to the default.
 * 11/13         1049     B. Yin    Handle outlook type defined in layer.
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenOutlookDrawingTool extends AbstractPgenDrawingTool {

    private Outlook otlk;

    private String lbl;

    public PgenOutlookDrawingTool() {

        super();

    }

    @Override
    protected void activateTool() {

        super.activateTool();

        if (attrDlg != null && !isDelObj()) {
            ((OutlookAttrDlg) attrDlg).enableAddDel(false);

            String layer = drawingLayers.getActiveLayer().getName();
            boolean setName = false;
            String otlkType = drawingLayers.getActiveLayer()
                    .getMetaInfoFromKey(OutlookAttrDlg.OTLK_TYPE_IN_LAYER_META);

            if (otlkType != null && !otlkType.isEmpty()) {
                ((OutlookAttrDlg) attrDlg).setOtlkType(otlkType);
                setName = true;
            }
            // set the outlook type to the layer name if there is one
            else if (layer != null && !layer.isEmpty()
                    && !layer.equalsIgnoreCase("Default")) {
                ((OutlookAttrDlg) attrDlg).setOtlkType(layer);

                setName = true;

            }
            if (otlk != null) {

                if (!setName) {
                    ((OutlookAttrDlg) attrDlg)
                            .setOtlkType(otlk.getOutlookType());
                }
            }
        }

        return;

    }

    @Override
    public void deactivateTool() {

        super.deactivateTool();

        PgenOutlookDrawingHandler mph = (PgenOutlookDrawingHandler) mouseHandler;
        if (mph != null) {
            mph.clearPoints();
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

            this.mouseHandler = new PgenOutlookDrawingHandler();

        }

        return this.mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     * 
     * @author bingfan
     *
     */

    public class PgenOutlookDrawingHandler extends InputHandlerDefaultImpl {

        /**
         * Points of the new element.
         */
        protected ArrayList<Coordinate> points = new ArrayList<>();

        /**
         * Current element.
         */
        protected DrawableElement elem;

        /**
         * An instance of DrawableElementFactory, which is used to create new
         * elements.
         */
        protected DrawableElementFactory def = new DrawableElementFactory();

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
                points.add(loc);

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
                DECollection dec = null;

                if (points.size() == 0) {

                    attrDlg.close();
                    attrDlg = null;
                    PgenUtil.setSelectingMode();

                } else if (points.size() < 2) {

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                } else {
                    String lineType = ((OutlookAttrDlg) attrDlg).getLineType();
                    // create a new DrawableElement.
                    elem = (DrawableElement) def.create(DrawableType.LINE,
                            attrDlg, "Lines", lineType, points,
                            drawingLayers.getActiveLayer());

                    dec = new DECollection(Outlook.OUTLOOK_LABELED_LINE);
                    dec.setPgenCategory("MET");

                    otlk = getCurrentOtlk(
                            ((OutlookAttrDlg) attrDlg).getOutlookType());

                    Outlook newOtlk = def.createOutlook(
                            ((OutlookAttrDlg) attrDlg).getOutlookType(), elem,
                            dec, otlk);

                    newOtlk.update((OutlookAttrDlg) attrDlg);

                    // create a new outlook
                    if (otlk == null || !otlk.getPgenType().equalsIgnoreCase(
                            ((OutlookAttrDlg) attrDlg).getOutlookType())) {
                        drawingLayers.addElement(newOtlk);
                    } else {
                        drawingLayers.replaceElement(otlk, newOtlk);
                    }

                    otlk = newOtlk;

                    attrDlg.setDrawableElement(elem);

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                    // set TextDrawingtool or SymbolDrawingTool
                    // bring up the text or the symbol dialog
                    if (((OutlookAttrDlg) attrDlg).addLabel()) {
                        if (((OutlookAttrDlg) attrDlg).addText()) {
                            lbl = ((OutlookAttrDlg) attrDlg).getLblTxt();
                            PgenUtil.setDrawingTextMode(true,
                                    ((OutlookAttrDlg) attrDlg).useLineColor(),
                                    lbl, dec);
                        } else if (((OutlookAttrDlg) attrDlg).addSymbol()) {

                            PgenUtil.setDrawingSymbolMode(
                                    ((OutlookAttrDlg) attrDlg).getSymbolCat(),
                                    ((OutlookAttrDlg) attrDlg).getSymbolType(),
                                    ((OutlookAttrDlg) attrDlg).useLineColor(),
                                    dec);
                        }
                    }
                }

                return true;
            } else {
                return false;
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

            // create the ghost line and put it in the drawing layer
            AbstractDrawableComponent ghost = null;
            String lineType = ((OutlookAttrDlg) attrDlg).getLineType();

            ghost = def.create(DrawableType.LINE, attrDlg, "Lines", lineType,
                    points, drawingLayers.getActiveLayer());

            if (((ILine) attrDlg).isFilled()) {
                ((Line) ghost)
                        .setFillPattern(((ILine) attrDlg).getFillPattern());
            }

            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);
                Line ln = (Line) ghost;
                ln.setLinePoints(new ArrayList<>(ghostPts));

                drawingLayers.setGhostLine(ghost);
                mapEditor.refresh();

            }

            return false;

        }

        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            } else {
                return true;
            }
        }

        public void clearPoints() {
            points.clear();
        }

    }

    /**
     * Get the outlook of the input type in current layer.
     * 
     * @return
     */
    private Outlook getCurrentOtlk(String type) {
        Outlook ol = null;

        Iterator<AbstractDrawableComponent> it = drawingLayers.getActiveLayer()
                .getComponentIterator();
        while (it.hasNext()) {
            AbstractDrawableComponent adc = it.next();
            if (adc instanceof Outlook
                    && adc.getPgenType().equalsIgnoreCase(type)) {
                ol = (Outlook) adc;
                break;
            }
        }

        return ol;
    }

}
