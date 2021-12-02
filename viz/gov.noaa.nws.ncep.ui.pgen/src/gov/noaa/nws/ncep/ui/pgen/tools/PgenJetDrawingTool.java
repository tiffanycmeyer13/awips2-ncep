/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenJetDrawingTool
 *
 * 8 July 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrSettings;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.JetAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

/**
 * Jet drawing tool.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 07/09         135      B. Yin    Initial Creation.
 * 08/09         135      B. Yin    Implement IJetBarb interface.
 * 12/10         366      B. Yin    Handle hash adding/deleting
 * Jul 26, 2019  66393    mapeters  Handle {@link AttrSettings#getSettings}
 *                                  change
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenJetDrawingTool extends PgenMultiPointDrawingTool
        implements IJetBarb {

    private Jet jet;

    /**
     * Constructor
     */
    public PgenJetDrawingTool() {
        super();
    }

    @Override
    public void deactivateTool() {

        this.resetMouseHandler();

        super.deactivateTool();

        if (mouseHandler instanceof PgenJetDrawingHandler) {
            PgenJetDrawingHandler jdh = (PgenJetDrawingHandler) mouseHandler;
            if (jdh != null) {
                jdh.clearPoints();
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
            this.mouseHandler = new PgenJetDrawingHandler();
        }

        return this.mouseHandler;
    }

    @Override
    public void setAddingBarbHandler() {

        setHandler(new PgenJetBarbAddingHandler(mapEditor, drawingLayers, this,
                ((JetAttrDlg) attrDlg)));
    }

    @Override
    public void setDeletingBarbHandler() {

        setHandler(new PgenJetBarbDeletingHandler(mapEditor, drawingLayers,
                this, ((JetAttrDlg) attrDlg)));
    }

    @Override
    public void setAddingHashHandler() {

        setHandler(new PgenJetHashAddingHandler(mapEditor, drawingLayers, this,
                ((JetAttrDlg) attrDlg)));
    }

    @Override
    public void setDeletingHashHandler() {

        setHandler(new PgenJetHashDeletingHandler(mapEditor, drawingLayers,
                this, ((JetAttrDlg) attrDlg)));
    }

    @Override
    public void resetMouseHandler() {
        setHandler(new PgenJetDrawingHandler());
    }

    /**
     * Set jet as selected when adding barbs
     */
    public void setSelected() {
        drawingLayers.setSelected(jet);
        mapEditor.refresh();
    }

    /**
     * Set the jet instance After add/delete barbs, this method is called to set
     * the new jet.
     */
    @Override
    public void setJet(Jet jet) {
        this.jet = jet;
    }

    /**
     * Get the jet instance.
     */
    @Override
    public Jet getJet() {
        return jet;
    }

    /**
     * De-select everything
     */
    public void deSelect() {
        drawingLayers.removeSelected();
        mapEditor.refresh();
    }

    /**
     * Implements input handler for mouse events.
     * 
     * @author bingfan
     *
     */
    private class PgenJetDrawingHandler
            extends PgenMultiPointDrawingTool.PgenMultiPointDrawingHandler {

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
                if (points.size() == 1) {
                    ((JetAttrDlg) attrDlg).enableBarbBtns(false);
                }

                return true;

            } else if (button == 3) {
                return true;
            } else {
                return true;
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

            // create the ghost element and put it in the drawing layer
            AbstractDrawableComponent ghost = def.create(DrawableType.LINE,
                    attrDlg, "Lines", "FILLED_ARROW", points,
                    drawingLayers.getActiveLayer());

            if (points != null && points.size() >= 1) {

                ArrayList<Coordinate> ghostPts = new ArrayList<>(points);
                ghostPts.add(loc);
                ((Line) ghost).setLinePoints(new ArrayList<>(ghostPts));

                drawingLayers.setGhostLine(ghost);
                mapEditor.refresh();

            }

            return false;

        }

        @Override
        public boolean handleMouseDownMove(int aX, int aY, int button) {
            if (!isResourceEditable() || shiftDown) {
                return false;
            } else {
                return true;
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

                    drawingLayers.removeGhostLine();
                    mapEditor.refresh();
                    attrDlg.close();
                    attrDlg = null;
                    PgenUtil.setSelectingMode();

                } else if (points.size() < 2) {

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                } else {

                    // create a new Jet.
                    elem = def.create(DrawableType.JET, attrDlg, pgenCategory,
                            pgenType, points, drawingLayers.getActiveLayer());

                    jet = (Jet) elem;

                    jet.setSnapTool(
                            new PgenSnapJet(drawingLayers.getDescriptor(),
                                    mapEditor, (JetAttrDlg) attrDlg));

                    // add the jet to PGEN resource
                    drawingLayers.addElement(jet);

                    // reset the jet line attributes
                    AbstractDrawableComponent adc = AttrSettings.getInstance()
                            .getSettings(pgenType);
                    if (adc != null && adc instanceof Jet) {
                        ((Jet) adc).getJetLine().update(attrDlg);
                    }

                    drawingLayers.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                    ((JetAttrDlg) attrDlg)
                            .setJetDrawingTool(PgenJetDrawingTool.this);
                    ((JetAttrDlg) attrDlg).enableBarbBtns(true);

                }

                return true;
            } else {
                return false;
            }
        }

    }
}
