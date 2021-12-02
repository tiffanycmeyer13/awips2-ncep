package gov.noaa.nws.ncep.ui.pgen.productmanage;

import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;

/**
 * *
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * ????                             Initial creation
 * Dec 01, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author tjensen
 */
public class ProductDialogStarter implements Runnable {

    PgenResourceList pgen = null;

    /**
     * @param pgen
     */
    public ProductDialogStarter(PgenResourceList pgen) {
        this.pgen = pgen;
    }

    @Override
    public void run() {
        pgen.startProductManage();
    }

}
