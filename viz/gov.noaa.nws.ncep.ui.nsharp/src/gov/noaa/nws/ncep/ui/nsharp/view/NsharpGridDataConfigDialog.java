/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
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

import gov.noaa.nws.ncep.ui.nsharp.NsharpConfigManager;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConfigStore;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConstants;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.view.NsharpGridDataConfigDialog
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/09/2015   RM#6674     Chin Chen   Initial coding. support model sounding query
 *                                      data interpolation and nearest point option
 * 04/22/2020   76580       smanoj      Allow user to interact with NsharpEditor while
 *                                      the dialog is open.
 * 03/21/2022   89212       smanoj      Configuration Dialog display issues.
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpGridDataConfigDialog extends Dialog {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NsharpGridDataConfigDialog.class);

    private static NsharpGridDataConfigDialog thisDialog = null;

    private NsharpConfigStore configStore = null;

    private NsharpConfigManager mgr;

    private boolean gridInterpolation;

    public static NsharpGridDataConfigDialog getInstance(Shell parShell) {

        if (thisDialog == null) {
            try {
                thisDialog = new NsharpGridDataConfigDialog(parShell);
            } catch (VizException e) {
                statusHandler.handle(Priority.ERROR,
                        "Error creating Grid Data Config Dialog:",
                        e.getMessage());
            }
        }

        return thisDialog;
    }

    public NsharpGridDataConfigDialog(Shell parentShell) throws VizException {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS);
        thisDialog = this;
        mgr = NsharpConfigManager.getInstance();
        configStore = mgr.retrieveNsharpConfigStoreFromFs();
        if (configStore != null) {
            gridInterpolation = configStore.getGraphProperty()
                    .isGridInterpolation();
        } else
            gridInterpolation = true; // by default
    }

    private void updateCfgStore() throws VizException {
        if (configStore != null) {
            configStore.getGraphProperty()
                    .setGridInterpolation(gridInterpolation);
        }
    }

    private void saveCfgStore() throws VizException {
        if (configStore != null) {
            configStore.getGraphProperty()
                    .setGridInterpolation(gridInterpolation);
            mgr.saveConfigStoreToFs(configStore);
        }
    }

    @Override
    public void createButtonsForButtonBar(Composite parent) {
        Button saveBtn = createButton(parent, IDialogConstants.INTERNAL_ID,
                "Save", false);
        saveBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                try {
                    saveCfgStore();
                } catch (VizException e) {
                    statusHandler.handle(Priority.ERROR,
                            "Error saving config store:", e.getMessage());
                }
            }
        });

        Button canBtn = createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, false);
        canBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                close();
            }
        });
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Nsharp Grid Data Interpolation");

    }

    private void createDialogContents(Composite parent) {

        Group btnGp = new Group(parent, SWT.SHADOW_ETCHED_IN);
        Button intpBtn = new Button(btnGp, SWT.RADIO | SWT.BORDER);
        intpBtn.setText("interpolation");
        intpBtn.setEnabled(true);
        intpBtn.setBounds(btnGp.getBounds().x + NsharpConstants.btnGapX,
                btnGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        intpBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                gridInterpolation = true;
                try {
                    updateCfgStore();
                } catch (VizException e) {
                    statusHandler.handle(Priority.ERROR,
                            "Error updating config store:", e.getMessage());
                }
            }
        });

        Button nearptBtn = new Button(btnGp, SWT.RADIO | SWT.BORDER);
        nearptBtn.setText("nearest point");
        nearptBtn.setEnabled(true);
        nearptBtn.setBounds(btnGp.getBounds().x + NsharpConstants.btnGapX,
                intpBtn.getBounds().y + intpBtn.getBounds().height
                        + NsharpConstants.btnGapY,
                NsharpConstants.btnWidth, NsharpConstants.btnHeight);
        nearptBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                gridInterpolation = false;
                try {
                    updateCfgStore();
                } catch (VizException e) {
                    statusHandler.handle(Priority.ERROR,
                            "Error updating config store:", e.getMessage());
                }
            }
        });

        if (gridInterpolation) {
            intpBtn.setSelection(true);
        } else {
            nearptBtn.setSelection(true);
        }
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite top;
        top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        top.setLayout(mainLayout);

        // Initialize all of the menus, controls, and layouts
        createDialogContents(top);

        return top;
    }

    @Override
    public int open() {

        if (this.getShell() == null) {
            this.create();
        } else {
            try {
                this.getShell().setLocation(
                        this.getShell().getParent().getLocation().x + 1100,
                        this.getShell().getParent().getLocation().y + 200);
            } catch (Exception e) {
                // if widget disposed
                this.create();
            }
        }
        return super.open();

    }

    @Override
    public boolean close() {
        return (super.close());
    }

}
