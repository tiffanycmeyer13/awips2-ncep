/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenLayeringHandler
 *
 * July 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen.layering;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gov.noaa.nws.ncep.ui.pgen.PgenSession;

/**
 * Pops up PGEN Layering control dialog in National Centers perspective.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 07/09         131      J. Wu     Initial creation.
 * Dec 01, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author jwu
 *
 */
public class PgenLayeringHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        PgenSession.getInstance().getCurrentResource().activateLayering();

        return null;

    }

}
