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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
*
* gov.noaa.nws.ncep.ui.nsharp.palette.NsharpConfigDialog
*
*
* This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
*
* <pre>
* SOFTWARE HISTORY
*
* Date         Ticket#     Engineer    Description
* -------      -------     --------    -----------
* 03/21/2012   229         Chin Chen   Initial coding
* 03/09/2015   RM#6674     Chin Chen   support model sounding query data interpolation
*                                      and nearest point option
* 09/16/2015   RM#10188    Chin Chen   Model selection upgrades - use grid resource 
*                                      definition name for model type display
* 08/21/2018   #7081       dgilling    Support refactored dialogs.
* 11/13/2018   7576        bsteffen    Unify activation dialogs.
* 04/22/2020   76580       smanoj      Allow user to interact with NsharpEditor while
*                                      the dialog is open.
* 03/21/2022   89212       smanoj      Configuration Dialog display issues.
* 
* </pre>
*
* @author Chin Chen
*/
public class NsharpConfigDialog extends Dialog {

    private static NsharpConfigDialog thisDialog = null;

    private static NsharpParametersSelectionConfigDialog parameterSelDialog = null;

    private NsharpDataDisplayConfigDialog dataDisplayDialog = null;

    private NsharpDataPageConfigDialog dataPageDialog = null;

    private NsharpPaneConfigDialog paneCfgDialog = null;

    private static NsharpGridDataConfigDialog mdlDataDialog = null;

    private NsharpWindBarbConfigDialog windBarbDialog = null;

    public NsharpConfigDialog(Shell parentShell) {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS);
       
    }

    public NsharpConfigDialog(IShellProvider parentShell) {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS);
    }

    public static NsharpConfigDialog getInstance(Shell parShell) {

        if (thisDialog == null) {
            thisDialog = new NsharpConfigDialog(parShell);
        }

        return thisDialog;

    }

    public void createDialogContents(Composite parent) {
        Button parameterBtn = new Button(parent, SWT.PUSH);
        parameterBtn.setText("Parameters Selection");
        parameterBtn.setEnabled(true);
        parameterBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                parameterSelDialog = NsharpParametersSelectionConfigDialog
                        .getInstance(shell);
                if (parameterSelDialog != null) {
                    parameterSelDialog.open();
                }
            }
        });
        parameterBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button dataDisplayBtn = new Button(parent, SWT.PUSH);
        dataDisplayBtn.setText("Data Display Configuration");
        dataDisplayBtn.setEnabled(true);
        dataDisplayBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                if (dataDisplayDialog == null) {
                    dataDisplayDialog = new NsharpDataDisplayConfigDialog(
                            shell);
                    if (dataDisplayDialog != null) {
                        dataDisplayDialog.addCloseCallback((rVal) -> {
                            dataDisplayDialog = null;
                        });
                    }
                }

                dataDisplayDialog.open();
            }
        });
        dataDisplayBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button dataPageBtn = new Button(parent, SWT.PUSH);
        dataPageBtn.setText("Data Page Configuration");
        dataPageBtn.setEnabled(true);
        dataPageBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                if (dataPageDialog == null) {
                    dataPageDialog = new NsharpDataPageConfigDialog(shell);
                    if (dataPageDialog != null) {
                        dataPageDialog.addCloseCallback((rVal) -> {
                            dataPageDialog = null;
                        });
                    }
                }

                dataPageDialog.open();
            }
        });
        dataPageBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button timeLineBtn = new Button(parent, SWT.PUSH);
        timeLineBtn.setText("Time Line Activation");
        timeLineBtn.setEnabled(true);
        timeLineBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                ActivationDialog.createTimeLineActivationDialog(shell).open();
            }
        });
        timeLineBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button stnBtn = new Button(parent, SWT.PUSH);
        stnBtn.setText("Station Activation");
        stnBtn.setEnabled(true);
        stnBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                ActivationDialog.createStationActivationDialog(shell).open();
            }
        });
        stnBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button sndBtn = new Button(parent, SWT.PUSH);
        sndBtn.setText("Sounding Source Activation");
        sndBtn.setEnabled(true);
        sndBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                ActivationDialog.createSoundingTypeActivationDialog(shell).open();
            }
        });
        sndBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button paneCfgBtn = new Button(parent, SWT.PUSH);
        paneCfgBtn.setText("Display Pane Configuration");
        paneCfgBtn.setEnabled(true);
        paneCfgBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                if (paneCfgDialog == null) {
                    paneCfgDialog = new NsharpPaneConfigDialog(shell);
                    if (paneCfgDialog != null) {
                        paneCfgDialog.addCloseCallback((rVal) -> {
                            paneCfgDialog = null;
                        });
                    }
                }

                paneCfgDialog.open();
            }
        });
        paneCfgBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button mdlDataBtn = new Button(parent, SWT.PUSH);
        mdlDataBtn.setText("Grid Data Interpolation");
        mdlDataBtn.setEnabled(true);
        mdlDataBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                mdlDataDialog = NsharpGridDataConfigDialog.getInstance(shell);
                if (mdlDataDialog != null) {
                    mdlDataDialog.open();
                }
            }
        });
        mdlDataBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button windbarbCfgBtn = new Button(parent, SWT.PUSH);
        windbarbCfgBtn.setText("Wind Barb Configuration");
        windbarbCfgBtn.setEnabled(true);
        windbarbCfgBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = e.display.getActiveShell();
                if (windBarbDialog == null) {
                    windBarbDialog = new NsharpWindBarbConfigDialog(shell);
                    if (windBarbDialog != null) {
                        windBarbDialog.addCloseCallback((rVal) -> {
                            windBarbDialog = null;
                        });
                    }
                }

                windBarbDialog.open();
            }
        });
        windbarbCfgBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite top;
        top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        top.setLayout(mainLayout);

        // Initialize all of the menus, controls, and layouts
        createDialogContents(top);

        return top;
    }

    @Override
    public void createButtonsForButtonBar(Composite parent) {
        Button closeBtn = createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, true);
        closeBtn.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                close();
            }
        });

    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Nsharp Configuration");
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        Rectangle parentBounds = getParentShell().getBounds();
        int x = parentBounds.x + parentBounds.width - initialSize.x;
        int y = parentBounds.y + (parentBounds.height / 2) - initialSize.y;
        return new Point(x, y);
    }

    @Override
    public boolean close() {
        // close all the child dialog windows
        if (parameterSelDialog != null) {
            parameterSelDialog.close();
        }

        if (dataDisplayDialog != null) {
            dataDisplayDialog.close();
        }

        if (dataPageDialog != null) {
            dataPageDialog.close();
        }

        if (paneCfgDialog != null) {
            paneCfgDialog.close();
        }

        if (mdlDataDialog != null) {
            mdlDataDialog.close();
        }

        if (windBarbDialog != null) {
            windBarbDialog.close();
        }

        return (super.close());
    }
}
