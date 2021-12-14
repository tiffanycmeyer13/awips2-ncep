/*
 * gov.noaa.nws.ncep.ui.pgen.layering.PegnHotKayHandler.java
 *
 * 26 March 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen.layering;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;

/**
 * Hot key handler for switching between activity layers.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 05/09         R?       archana   Created
 * Aug 15, 2016  21066    J. Wu     Re-implemented.
 * Dec 01, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author archana
 *
 */
public class PgenLayeringHotKeyHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String layerIndexStr = event.getParameter("layerIndex");
        if (layerIndexStr == null || layerIndexStr.isEmpty()) {
            return null;
        }

        int layerIndex = Integer.parseInt(layerIndexStr);

        PgenResourceList pgenResources = PgenUtil
                .findPgenResources(PgenUtil.getActiveEditor());
        for (PgenResource pgenResource : pgenResources.getResourceList()) {
            if (pgenResource != null && layerIndex > 0) {
                Product activeProduct = pgenResource.getActiveProduct();
                int layerListSize = activeProduct.getLayers().size();

                if (layerListSize >= layerIndex) {
                    Layer layerToActivate = activeProduct
                            .getLayer(layerIndex - 1);
                    // Switch to the new layer and update on GUI.
                    pgenResource.getResourceData()
                            .switchLayer(layerToActivate.getName());
                }
            }
        }

        return null;
    }

}
