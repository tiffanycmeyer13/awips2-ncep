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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingProfile;
import gov.noaa.nws.ncep.ui.nsharp.NsharpConstants;
import gov.noaa.nws.ncep.ui.nsharp.display.NsharpEditor;
import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;
import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResourceData;

/**
 *
 * Provides a base implementation for NSHARP Sounding Load Dialog.
 *
 * <pre>
 * 
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- -----------------
 * Mar 24, 2020  73571    smanoj   Initial creation
 *
 * </pre>
 *
 * @author smanoj
 */
public abstract class AbstractNsharpLoadDialog extends Dialog {

    protected Composite top;

    protected Composite dialogParent;

    protected Shell shell;

    protected Text text1;

    protected MessageBox mb;

    protected Font newFont;

    protected Cursor waitCursor = null;

    protected Group soundingTypeGp;

    protected int dialogWidth = 400;

    protected int dialogHeight = 400;

    protected int activeLoadSoundingType = OBSER_SND;

    protected String activeMdlSndMdlType = "";

    protected String activeGpdProdName = "";

    protected AbstractNsharpMapResource mapRsc;

    protected AbstractNsharpMapResourceData mapRscData;

    protected AbstractMdlSoundingDlgContents mdlDialog;

    protected AbstractObsSoundingDlgContents obsDialog;

    protected AbstractPfcSoundingDlgContents pfcDialog;

    protected org.eclipse.swt.widgets.List soundingTypeList;

    protected NcSoundingProfile.PfcSndType activePfcSndType = NcSoundingProfile.PfcSndType.NAMSND;

    protected NcSoundingProfile.ObsSndType activeObsSndType = NcSoundingProfile.ObsSndType.NCUAIR;

    protected List<String> pfcSelectedFileList = new ArrayList<String>();

    protected List<String> pfcSelectedTimeList = new ArrayList<String>();

    // define index to loadStringArray
    public static final int OBSER_SND = 0;

    public static final int MODEL_SND = 1;

    public static final int PFC_SND = 2;

    public static final int ARCHIVE = 3;

    protected static final String[] soundingTypeStringArray = {
            "Observed Soundings", "Model Soundings", "PFC Soundings",
            "Archive Files" };

    public AbstractNsharpLoadDialog(Shell parentShell) throws VizException {
        super(parentShell);
        this.setShellStyle(
                SWT.TITLE | SWT.MODELESS | SWT.CLOSE | SWT.SHELL_TRIM);
    }

    public Font getNewFont() {
        return newFont;
    }

    public List<String> getPfcSelectedFileList() {
        return pfcSelectedFileList;
    }

    public void setPfcSelectedFileList(List<String> pfcSelectedFileList) {
        this.pfcSelectedFileList = pfcSelectedFileList;
    }

    public List<String> getPfcSelectedTimeList() {
        return pfcSelectedTimeList;
    }

    public void setPfcSelectedTimeList(List<String> pfcSelectedTimeList) {
        this.pfcSelectedTimeList = pfcSelectedTimeList;
    }

    public NcSoundingProfile.ObsSndType getActiveObsSndType() {
        return activeObsSndType;
    }

    public void setActiveObsSndType(
            NcSoundingProfile.ObsSndType activeObsSndType) {
        this.activeObsSndType = activeObsSndType;
    }

    public NcSoundingProfile.PfcSndType getActivePfcSndType() {
        return activePfcSndType;
    }

    public void setActivePfcSndType(
            NcSoundingProfile.PfcSndType activePfcSndType) {
        this.activePfcSndType = activePfcSndType;
    }

    public String getActiveMdlSndMdlType() {
        return activeMdlSndMdlType;
    }

    public void setActiveMdlSndMdlType(String activeMdlSndMdlType) {
        this.activeMdlSndMdlType = activeMdlSndMdlType;
    }

    public String getActiveGpdProdName() {
        return activeGpdProdName;
    }

    public void setActiveGpdProdName(String activeGpdProdName) {
        this.activeGpdProdName = activeGpdProdName;
    }

    public int getActiveLoadSoundingType() {
        return activeLoadSoundingType;
    }

    public void setActiveLoadSoundingType(int activeLoadSoundingType) {
        this.activeLoadSoundingType = activeLoadSoundingType;
    }

    public AbstractMdlSoundingDlgContents getMdlDialog() {
        return mdlDialog;
    }

    public AbstractObsSoundingDlgContents getObsDialog() {
        return obsDialog;
    }

    public AbstractPfcSoundingDlgContents getPfcDialog() {
        return pfcDialog;
    }

    public AbstractNsharpMapResource getMapRsc() {
        return mapRsc;
    }

    public void setAndOpenMb(String msg) {
        if (mb != null) {
            mb.setMessage(msg);
            try {
                mb.open();
            } catch (Exception e) {
                mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                mb.setMessage(msg);
                mb.open();
            }
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);

        shell.setSize(dialogWidth, dialogHeight);
        shell.setText("Load");
        mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);

        mb.setMessage("User Input Error!");
        Font font = shell.getFont();
        FontData[] fontData = font.getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setHeight(7);
        }
        newFont = new Font(font.getDevice(), fontData);
        shell.setFont(newFont);
    }

    @Override
    public Control createDialogArea(Composite parent) {
        top = (Composite) super.createDialogArea(parent);

        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;

        createLoadContents(top);

        if (waitCursor == null)
            waitCursor = new Cursor(top.getDisplay(), SWT.CURSOR_WAIT);
        return top;
    }

    @Override
    public int open() {

        if (this.getShell() == null) {
            this.create();
        }
        this.getShell().setLocation(
                this.getShell().getParent().getLocation().x + 1100,
                this.getShell().getParent().getLocation().y + 200);

        return super.open();

    }

    public void createSndTypeList(Group TopLoadGp) {
        soundingTypeGp = new Group(TopLoadGp, SWT.SHADOW_ETCHED_IN);
        soundingTypeGp.setText("Sounding Type");
        soundingTypeGp.setFont(newFont);
        soundingTypeList = new org.eclipse.swt.widgets.List(soundingTypeGp,
                SWT.SINGLE | SWT.V_SCROLL);
        soundingTypeList.setBounds(
                soundingTypeGp.getBounds().x + NsharpConstants.btnGapX,
                soundingTypeGp.getBounds().y + NsharpConstants.labelGap,
                NsharpConstants.filelistWidth, NsharpConstants.filelistHeight);
        soundingTypeList.setFont(newFont);
        for (String loadStr : soundingTypeStringArray) {
            soundingTypeList.add(loadStr);
        }
        // create a selection listener to handle user's selection on list
        soundingTypeList.addListener(SWT.Selection, new Listener() {
            private String selectedProduct = null;

            public void handleEvent(Event e) {
                if (soundingTypeList.getSelectionCount() > 0) {
                    selectedProduct = soundingTypeList.getSelection()[0];
                    getMapRsc().getOrCreateNsharpMapResource();
                    NsharpEditor editor = NsharpEditor.getActiveNsharpEditor();
                    if (editor != null) {
                        editor.refresh();
                    }

                    if (selectedProduct
                            .equals(soundingTypeStringArray[OBSER_SND])) {
                        if (activeLoadSoundingType != OBSER_SND) {
                            cleanupDialog(activeLoadSoundingType);
                            activeLoadSoundingType = OBSER_SND;
                            obsDialog.createObsDialogContents();
                            dialogParent.pack();
                            dialogParent.layout(true);
                            dialogParent.redraw();
                            soundingTypeList.setSelection(OBSER_SND);
                        }
                    } else if (selectedProduct
                            .equals(soundingTypeStringArray[MODEL_SND])) {
                        if (activeLoadSoundingType != MODEL_SND) {
                            cleanupDialog(activeLoadSoundingType);
                            activeLoadSoundingType = MODEL_SND;
                            mdlDialog.createMdlDialogContents();
                            dialogParent.pack();
                            dialogParent.layout(true);
                            dialogParent.redraw();
                            soundingTypeList.setSelection(MODEL_SND);
                        }
                    } else if (selectedProduct
                            .equals(soundingTypeStringArray[PFC_SND])) {
                        if (activeLoadSoundingType != PFC_SND) {
                            cleanupDialog(activeLoadSoundingType);
                            activeLoadSoundingType = PFC_SND;
                            pfcDialog.createPfcDialogContents();
                            dialogParent.pack();
                            dialogParent.layout(true);
                            dialogParent.redraw();
                            soundingTypeList.setSelection(PFC_SND);
                        }
                    } else if (selectedProduct
                            .equals(soundingTypeStringArray[ARCHIVE])) {
                        if (activeLoadSoundingType != ARCHIVE) {
                            cleanupDialog(activeLoadSoundingType);
                            activeLoadSoundingType = ARCHIVE;
                            NsharpHandleArchiveFile.openArchiveFile(shell);
                            close();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean close() {
        cleanSelf();
        if (waitCursor != null)
            waitCursor.dispose();
        waitCursor = null;
        newFont.dispose();
        return (super.close());
    }

    public void cleanSelf() {
        if (text1 != null) {
            text1.dispose();
            text1 = null;
        }
    }

    public void cleanSndTypeList() {
        if (soundingTypeList != null) {
            soundingTypeList.removeListener(SWT.Selection,
                    soundingTypeList.getListeners(SWT.Selection)[0]);
            soundingTypeList.dispose();
            soundingTypeList = null;
        }
        if (soundingTypeGp != null) {
            soundingTypeGp.dispose();
            soundingTypeGp = null;
        }
    }

    public void cleanupDialog(int activeLoadType) {
        switch (activeLoadType) {
        case OBSER_SND:
            obsDialog.cleanup();
            break;
        case MODEL_SND:
            mdlDialog.cleanup();
            break;
        case PFC_SND:
            pfcDialog.cleanup();
            break;
        case ARCHIVE:
            break;
        default:
            break;
        }
    }

    public void startWaitCursor() {
        if (waitCursor != null)
            top.setCursor(waitCursor);
    }

    public void stopWaitCursor() {
        top.setCursor(null);
    }

    @Override
    public void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CLOSE_LABEL, false);
    }

    public abstract void createLoadContents(Composite parent);
}
