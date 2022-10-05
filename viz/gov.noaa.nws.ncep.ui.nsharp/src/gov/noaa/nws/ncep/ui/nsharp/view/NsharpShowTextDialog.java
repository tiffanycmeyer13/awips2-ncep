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

import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingLayer;
import gov.noaa.nws.ncep.ui.nsharp.NsharpStationInfo;
import gov.noaa.nws.ncep.ui.nsharp.display.NsharpEditor;
import gov.noaa.nws.ncep.ui.nsharp.display.rsc.NsharpResourceHandler;

import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.palette.NsharpShowTextDialog
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/23/2010   229         Chin Chen   Initial coding
 * 01/22/2014               Chin Chen   DR17003: fixed show text cause error
 *                                      exception when in D2D swapping from
 *                                      main pane to side pane
 * 01/22/2014               Chin Chen   fixed show text cause nsharp hang
 *                                      issue, when text shown and then
 *                                      start looping action
 * 03/21/2022   89212       smanoj      Configuration Dialog display issues.
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpShowTextDialog extends Dialog {
    private static NsharpShowTextDialog INSTANCE = null;

    protected Composite top;

    private Shell shell;

    private Text text = null;

    private Group textGp;

    private Font newFont;

    public Text getText() {
        return text;
    }

    protected NsharpShowTextDialog(Shell parentShell) throws VizException {
        super(parentShell);

        this.setShellStyle(SWT.TITLE | SWT.MODELESS | SWT.CLOSE);
        shell = parentShell;

    }

    private void createShowtextDialogContents(Composite parent) {
        textGp = new Group(parent, SWT.SHADOW_OUT);
        textGp.setLayout(new GridLayout());
        textGp.setText("Sounding Text");
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        textGp.setLayoutData(data);
        text = new Text(textGp, SWT.V_SCROLL | SWT.H_SCROLL);

        GridData data1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        text.setLayoutData(data1);
        text.setEditable(false);
        Font font = text.getFont();
        FontData[] fontData = font.getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setHeight(12);
            fontData[i].setName("courier");
        }
        newFont = new Font(font.getDevice(), fontData);
        text.setFont(newFont);

    }

    @Override
    public void createButtonsForButtonBar(Composite parent) {
        // create buttons with "CLOSE" label but with cancel function
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CLOSE_LABEL, false);

        // Push buttons for SAVE
        Button saveBtn = createButton(parent, IDialogConstants.CLIENT_ID,
                "SAVE", false);
        saveBtn.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                // Action to save text report
                NsharpSaveHandle.saveFile(shell);
            }
        });
    }

    /**
     * Creates the dialog area
     */
    @Override
    public Control createDialogArea(Composite parent) {

        top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 13;
        mainLayout.marginWidth = 13;
        top.setLayout(mainLayout);

        // Initialize all of the menus, controls, and layouts
        createShowtextDialogContents(top);

        // FixMark:looping
        doRefreshTextData();
        return top;
    }

    // @Override
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

        // FixMark:SwapPaneShowText
        if (text != null) {
            if (text.getFont() != null) {
                text.getFont().dispose();
                newFont = null;
            }
            text.dispose();
            text = null;
        }
        if (textGp != null) {
            textGp.dispose();
            textGp = null;
        }
        INSTANCE = null;
        // end FixMark:SwapPaneShowText
        if (newFont != null) {
            newFont.dispose();
            newFont = null;
        }
        return (super.close());
    }

    public static NsharpShowTextDialog getInstance(Shell parShell) {

        if (INSTANCE == null) {
            try {
                INSTANCE = new NsharpShowTextDialog(parShell);

            } catch (VizException e) {
                e.printStackTrace();
            }

        }

        return INSTANCE;

    }

    public static NsharpShowTextDialog getAccess() {

        return INSTANCE;
    }

    private void doRefreshTextData() {

        if (NsharpEditor.getActiveNsharpEditor() == null) {
            return;
        }
        NsharpResourceHandler rsc = NsharpEditor.getActiveNsharpEditor()
                .getRscHandler();
        if (rsc != null && rsc.getSoundingLys() != null && !text.isDisposed()
                && text != null) {
            String hdr;
            List<NcSoundingLayer> soundLyList = rsc.getSoundingLys();
            hdr = "PRESSURE  HGHT\t   TEMP\t  DWPT    WDIR     WSPD    OMEG\n";
            String latlonstr;
            NsharpStationInfo stnInfo = rsc.getPickedStnInfo();
            if (stnInfo != null) {
                latlonstr = "  LAT=" + stnInfo.getLatitude() + " LON="
                        + stnInfo.getLongitude();
            } else {
                latlonstr = "  LAT=  LON=  ";
            }
            String textToShow = rsc.getPickedStnInfo().getSndType() + "  "
                    + rsc.getPickedStnInfoStr() + latlonstr + "\n" + hdr;

            String tempText = "";
            for (NcSoundingLayer layer : soundLyList) {
                tempText = String.format(
                        "%7.2f\t%8.2f %7.2f %7.2f   %6.2f  %6.2f  %9.6f\n",
                        layer.getPressure(), layer.getGeoHeight(),
                        layer.getTemperature(), layer.getDewpoint(),
                        layer.getWindDirection(), layer.getWindSpeed(),
                        layer.getOmega());

                textToShow = textToShow + tempText;

            }

            text.setText(textToShow);
            textGp.layout();
        }
    }

    /**
     * Need use asyncExec to handle update text request from other thread
     * (worker thread)
     */
    public void refreshTextData() {
        try {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    doRefreshTextData();
                }
            });
        } catch (SWTException e) {
            System.out.println("refreshTextData: can not run asyncExec()");
        }
    }

}
