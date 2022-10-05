/*
 * gov.noaa.nws.ncep.ui.pgen.tools.FromDlg
 *
 * March 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen.attrdialog;

import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.gfa.Gfa;
import gov.noaa.nws.ncep.ui.pgen.gfa.GfaFormat;

/**
 * Create a dialog for PGEN cycle action.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer     Description
 * ------------- -------- ------------ -----------------------------------------
 * 03/10         223      M.Laryukhin  Initial creation
 * 02/11                  J. Wu        Added confirmation message.
 * 03/11                  J. Wu        Implemented "Format Tag".
 * Dec 01, 2021  95362    tjensen      Refactor PGEN Resource management to
 *                                     support multi-panel displays
 *
 * </pre>
 *
 * @author M.Laryukhin
 */
public class FromAttrDlg extends AttrDlg {

    private static FromAttrDlg instance;

    private Composite top;

    private boolean formatByTag = false;

    /**
     * Private constructor
     * 
     * @param parShell
     * @throws VizException
     */
    private FromAttrDlg(Shell parShell) throws VizException {

        super(parShell);

    }

    /**
     * Creates an extrapolation dialog if the dialog does not exist and returns
     * the instance. If the dialog exists, return the instance.
     * 
     * @param parShell
     * @return
     */
    public static FromAttrDlg getInstance(Shell parShell) {

        if (instance == null) {
            try {
                instance = new FromAttrDlg(parShell);
            } catch (VizException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /*
     * (non-Javadoc) Create all of the widgets on the Dialog
     * 
     * @see
     * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public Control createDialogArea(Composite parent) {

        top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell.
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        top.setLayout(layout);

        // Initialize all of the menus, controls, and layouts
        initializeComponents();

        return top;
    }

    /**
     * Creates buttons, menus, and other controls in the dialog area
     * 
     * @param listener
     */
    private void initializeComponents() {

        this.getShell().setText("Generate FROM");

        createButton("Format All", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                formatAllPressed();
            }
        });

        createButton("Format Layer", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                formatLayerPressed();
            }
        });

        createButton("Format Tag", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                formatTagPressed();
            }
        });

        createButton("Cancel", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                formatByTag = false;
                cancelPressed();
            }
        });
    }

    private Button createButton(String label, SelectionListener listener) {
        Button btn = new Button(top, SWT.PUSH);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        btn.setLayoutData(gridData);
        btn.setText(label);
        btn.addSelectionListener(listener);
        return btn;
    }

    @Override
    public Control createButtonBar(Composite parent) {
        // no button bar
        return null;
    }

    @Override
    public void createButtonsForButtonBar(Composite parent) {
        // no other buttons
    }

    /**
     * Set the location of the dialog
     */
    @Override
    public int open() {

        if (this.getShell() == null) {
            this.create();
        }

        if (shellLocation == null) {
            shellLocation = initialLocation();
        }

        formatByTag = false;

        return super.open();
    }

    public Point initialLocation() {
        Rectangle parentSize = getParentShell().getBounds();
        Rectangle mySize = getShell().getBounds();

        int locationX, locationY;
        locationX = mySize.width * 2 + parentSize.x;
        locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

        return new Point(locationX, locationY);
    }

    @Override
    public void cancelPressed() {
        super.cancelPressed();
        PgenUtil.setSelectingMode();
    }

    /**
     * Gets values of all attributes of the dialog.
     */
    public HashMap<String, Object> getAttrFromDlg() {

        HashMap<String, Object> attr = new HashMap<>();

        return attr;
    }

    /**
     * Sets values of all attributes of the dialog.
     */
    @Override
    public void setAttrForDlg(IAttribute attr) {
    }

    /**
     * Pop ups a dialog to confirm the chosen action.
     */
    private boolean formatConfirmed(String msg) {

        StringBuilder allmsg = new StringBuilder(
                "This will delete and regenerate ALL AIRMETs and OUTLOOKs");
        if (msg != null) {
            allmsg = allmsg.append(msg);
        }
        allmsg.append(".\nOk to continue?");

        MessageDialog confirmDlg = new MessageDialog(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Confirm", null, allmsg.toString(), MessageDialog.QUESTION,
                new String[] { "OK", "Cancel" }, 0);
        confirmDlg.open();

        return (confirmDlg.getReturnCode() == MessageDialog.OK) ? true : false;

    }

    /**
     * Execute when "Format All" button is pressed.
     */
    private void formatAllPressed() {
        formatByTag = false;
        if (formatConfirmed(null)) {

            GfaFormat format = new GfaFormat(drawingLayers);

            format.formatAllPressed();

            refreshSelect();

        }
    }

    /**
     * Execute when "Format Layer" button is pressed.
     */
    private void formatLayerPressed() {
        formatByTag = false;

        String msg = null;
        if (drawingLayers != null
                && drawingLayers.getProductManageDlg() != null) {
            msg = " for layer - " + drawingLayers.getActiveLayer().getName();
        }

        if (formatConfirmed(msg)) {
            GfaFormat format = new GfaFormat(drawingLayers);

            format.formatLayerPressed();

            refreshSelect();
        }
    }

    /**
     * Executes when "Format Tag" button is pressed then a GFA is selected..
     */
    public void formatTagPressed() {

        formatByTag = true;

        String msg = null;
        if (drawingLayers != null) {
            DrawableElement de = drawingLayers.getSelectedDE();
            if (de instanceof Gfa) {
                msg = " \nwith hazard type " + ((Gfa) de).getGfaHazard()
                        + " and tag " + ((Gfa) de).getGfaTag()
                        + ((Gfa) de).getGfaDesk();

                if (mapEditor != null) {
                    mapEditor.refresh();
                }

                if (formatConfirmed(msg)) {

                    GfaFormat format = new GfaFormat(drawingLayers);

                    format.formatTagPressed();
                }

                drawingLayers.removeSelected();

                if (mapEditor != null) {
                    mapEditor.refresh();
                }
            }
        }

    }

    private void refreshSelect() {
        if (mapEditor != null) {
            mapEditor.refresh();
        }
        close();
        PgenUtil.setSelectingMode();
    }

    /**
     * Getter isFormatByTag.
     * 
     * @return
     */
    public boolean isFormatByTag() {
        return formatByTag;
    }

}
