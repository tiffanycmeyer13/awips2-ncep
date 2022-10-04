/*
 * ReplaceElementCommand
 * 
 * Date created: 14 APRIL 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen.controls;

import java.util.List;

import gov.noaa.nws.ncep.ui.pgen.PGenException;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;

/**
 * This class contains the implementation to replace an existing element in a
 * product list with a new element. The original element can be restored as an
 * undo feature.
 * 
 * <pre>
 *
 *    SOFTWARE HISTORY
 *
 * Date           Ticket#       Engineer      Description
 * -------------  -----------   ------------  --------------------------------
 * Apr 14, 2009    ##           sgilbert      Initial version.
 * Feb 18, 2022   100402        smanoj        Bug fix to support multi-panel
 *                                            display refactor.
 * 
 * </pre>
 * 
 * @author sgilbert
 */

public class ReplaceElementCommand extends PgenCommand {

    // product list
    private List<Product> list;

    // layer that contains the element to be replaced
    private Layer layer;

    // the drawable to be replaced
    private AbstractDrawableComponent oldElement;

    // The new drawable element
    private AbstractDrawableComponent newElement;

    /**
     * Constructor used to specify the product list as well as the old and new
     * drawable element.
     * 
     * @param list
     *            The product list
     * @param oldElement
     *            Drawable element to replace
     * @param newElement
     *            New drawable element
     */
    public ReplaceElementCommand(List<Product> list,
            AbstractDrawableComponent oldElement,
            AbstractDrawableComponent newElement) {
        this.list = list;
        this.oldElement = oldElement;
        this.newElement = newElement;
    }

    /**
     * Replaces one drawable element with another in a product list
     */
    @Override
    public void execute() throws PGenException {

        for (Product currProd : list) {
            for (Layer currLayer : currProd.getLayers()) {
                if (currLayer.replace(oldElement, newElement)) {
                    layer = currLayer;
                    return;
                }
            }
        }

    }

    /**
     * restores the original element back in the product list
     */
    @Override
    public void undo() throws PGenException {

        if (layer != null) {
            if (layer.replace(newElement, oldElement)) {
                return;
            }
        }

    }

}
