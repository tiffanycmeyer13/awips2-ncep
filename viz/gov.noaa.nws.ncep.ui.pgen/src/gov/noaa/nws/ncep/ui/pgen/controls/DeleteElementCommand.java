/*
 * DeleteElementCommand
 * 
 * Date created: 14 APRIL 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen.controls;

import java.util.List;

import gov.noaa.nws.ncep.ui.pgen.PGenException;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;

/**
 * This class contains the implementation to delete a drawable element from a
 * product list. The element can be re-added for an undo feature.
 * 
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer       Description
 * ------------- -------- -------------- ---------------------------------------
 * Apr 14, 2009    ##           sgilbert      Initial version.
 * Feb 18, 2022   100402        smanoj        Bug fix to support multi-panel
 *                                            display refactor.
 * 
 * </pre>
 * 
 * @author sgilbert
 */

public class DeleteElementCommand extends PgenCommand {

    // product list from which element should be deleted
    private List<Product> list;

    // layer from which element should be deleted
    private DECollection collection;

    // drawable element to delete
    private AbstractDrawableComponent comp;

    /**
     * Constructor used to specify the element and product list.
     * 
     * @param list
     *            Product list from which element should be deleted.
     * @param element
     *            - drawable element to delete.
     */
    public DeleteElementCommand(List<Product> list,
            AbstractDrawableComponent comp) {
        this.list = list;
        this.comp = comp;
    }

    /**
     * Removes the element from the product list. Saves the layer for possible
     * undo
     */
    @Override
    public void execute() throws PGenException {

        for (Product currProd : list) {
            for (Layer currLayer : currProd.getLayers()) {
                DECollection dec = currLayer.search(comp);
                if (dec != null) {
                    collection = dec;
                    dec.removeElement(comp);
                    return;
                }
            }
        }

    }

    /**
     * Re-adds the drawable element back to the original layer
     */
    @Override
    public void undo() throws PGenException {

        if (collection != null) {
            collection.addElement(comp);
        }

    }

}
