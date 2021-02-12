/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract EA133W-17-CQ-0082 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.nws.ncep.ui.nsharp.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 04/23/2012   229         Chin Chen   Initial coding
 * 04/22/2020   76580       smanoj      Allow user to interact with NsharpEditor while
 *                                      the dialog is open.
 *
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpBoundaryMotionDialog extends Dialog {
    private static NsharpBoundaryMotionDialog thisDialog = null;

    public static NsharpBoundaryMotionDialog getAccess() {
        return thisDialog;
    }

    protected NsharpBoundaryMotionDialog(Shell parentShell) {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS);
        thisDialog = this;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite top;
        top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        top.setLayout(mainLayout);

        return top;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    @Override
    public int open() {
        return super.open();
    }

}
