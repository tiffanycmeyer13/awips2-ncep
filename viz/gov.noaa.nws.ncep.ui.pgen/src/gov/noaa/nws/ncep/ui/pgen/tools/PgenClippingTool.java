/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenClippingTool
 *
 * 28 January 2013
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import org.locationtech.jts.geom.Polygon;

import gov.noaa.nws.ncep.ui.pgen.PgenStaticDataProvider;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.clipper.ClipProduct;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.productmanage.ProductConfigureDialog;
import gov.noaa.nws.ncep.ui.pgen.producttypes.ProductType;

/**
 * Implements PGEN "Clipping" function.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 01/13         966      B. Yin    Initial Creation.
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */

public class PgenClippingTool extends AbstractPgenTool {

    private static HashMap<String, Polygon> bounds;

    public PgenClippingTool() {

        super();

    }

    @Override
    protected void activateTool() {

        super.activateTool();
        if (!isResourceEditable()) {
            return;
        }

        drawingLayers.removeGhostLine();
        drawingLayers.removeSelected();

        String pdName = drawingLayers.getActiveProduct().getType();
        ProductType pt = ProductConfigureDialog.getProductTypes().get(pdName);

        Polygon boundsPoly = null;

        if (pt != null && pt.getClipFlag() != null && pt.getClipFlag()) {
            boundsPoly = getBoundsPoly(pt.getClipBoundsTable(),
                    pt.getClipBoundsName());
            if (boundsPoly != null) {
                processClip(boundsPoly);
            }
        }

        PgenUtil.setSelectingMode();

    }

    @Override
    public IInputHandler getMouseHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Clip all drawables in the active layer.
     * 
     * @param ol
     * @param layer
     * @param boundsPoly
     */
    private void processClip(Polygon boundsPoly) {
        // clip
        List<AbstractDrawableComponent> before = new ArrayList<>();
        for (AbstractDrawableComponent adc : drawingLayers.getActiveLayer()
                .getDrawables()) {
            before.add(adc.copy());
        }

        List<AbstractDrawableComponent> clipped = new ClipProduct(boundsPoly,
                true).clipDrawableComponents(before);
        List<AbstractDrawableComponent> old = new ArrayList<>(
                drawingLayers.getActiveLayer().getDrawables());

        drawingLayers.replaceElements(old, clipped);

    }

    /**
     * Gets the bounds polygon.
     * 
     * @param boundsTbl
     * @param boundsName
     * @return
     */
    private Polygon getBoundsPoly(String boundsTbl, String boundsName) {
        if (bounds == null) {
            bounds = new HashMap<>();
        }

        // check if the polygon is still in memory.
        Polygon boundsPoly = bounds.get(boundsTbl + boundsName);

        // load the bounds polygon.
        if (boundsPoly == null) {
            boundsPoly = PgenStaticDataProvider.getProvider()
                    .loadBounds(boundsTbl, boundsName);
            if (boundsPoly != null) {
                // only keep one polygon in memory
                bounds.clear();
                bounds.put(boundsTbl + boundsName, boundsPoly);
            }
        }

        return boundsPoly;
    }
}
