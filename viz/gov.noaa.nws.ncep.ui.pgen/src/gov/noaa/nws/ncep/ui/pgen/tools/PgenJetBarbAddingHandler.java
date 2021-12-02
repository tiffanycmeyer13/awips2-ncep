/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenJetBarbAddingHandler
 *
 * 8 July 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;

import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.attrdialog.JetAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.display.IText.DisplayType;
import gov.noaa.nws.ncep.ui.pgen.display.IText.FontStyle;
import gov.noaa.nws.ncep.ui.pgen.display.IText.TextJustification;
import gov.noaa.nws.ncep.ui.pgen.display.IText.TextRotation;
import gov.noaa.nws.ncep.ui.pgen.display.IVector.VectorType;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet.JetBarb;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;

/**
 * Mouse handler to add barb when drawing jet.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer   Description
 * ------------- -------- ---------- -------------------------------------------
 * 07/09         135      B. Yin     Initial Creation.
 * Sep 30, 2009  169      Greg Hull  NCMapEditor
 * 12/11         523      B. Yin     Clear top/bottom after adding barb
 * Dec 02, 2021  95362    tjensen    Refactor PGEN Resource management to
 *                                   support multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */
public class PgenJetBarbAddingHandler extends InputHandlerDefaultImpl {

    // private NCMapEditor mapEditor;
    private final AbstractEditor mapEditor;

    private final PgenResourceList drawingLayers;

    private final IJetBarb prevTool;

    private final JetAttrDlg jetDlg;

    /**
     * public constructor
     *
     * @param mapEditor
     * @param drawingLayers
     * @param prevTool
     * @param jet
     * @param jetDlg
     */
    public PgenJetBarbAddingHandler(AbstractEditor mapEditor,
            PgenResourceList drawingLayers, IJetBarb prevTool,
            JetAttrDlg jetDlg) {
        this.mapEditor = mapEditor;
        this.drawingLayers = drawingLayers;
        this.prevTool = prevTool;
        this.jetDlg = jetDlg;
    }

    @Override
    public boolean handleMouseDown(int anX, int aY, int button) {
        if (!drawingLayers.isEditable()) {
            return false;
        }
        // Check if mouse is in geographic extent
        Coordinate loc = mapEditor.translateClick(anX, aY);
        if (loc == null || shiftDown) {
            return false;
        }

        Jet jet = prevTool.getJet();
        if (jet == null) {
            return false;
        }

        if (button == 1) {

            Jet newJet = jet.copy();
            newJet.addBarb(createWindInfo(loc, newJet, true));

            drawingLayers.replaceElement(jet, newJet);
            jet = newJet;

            prevTool.setJet(jet);

            mapEditor.refresh();

            // top/bottom are needed only once.
            if (jetDlg.getFLDepth() != null && !jetDlg.getFLDepth().isEmpty()) {
                jetDlg.clearFLDepth();
            }

            jetDlg.updateSegmentPane();

            return true;

        } else if (button == 3) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        if (!drawingLayers.isEditable()) {
            return false;
        }

        // Check if mouse is in geographic extent
        Coordinate loc = mapEditor.translateClick(x, y);
        if (loc == null) {
            return false;
        }

        Jet jet = prevTool.getJet();
        if (jet == null) {
            return false;
        }

        drawingLayers.setGhostLine(createWindInfo(loc, jet, false));
        mapEditor.refresh();

        return true;

    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int mouseButton) {
        if (!drawingLayers.isEditable() || shiftDown) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * overrides the function in selecting tool
     */
    @Override
    public boolean handleMouseUp(int x, int y, int button) {
        if (!drawingLayers.isEditable() || shiftDown) {
            return false;
        }

        if (button == 3) {
            drawingLayers.removeGhostLine();
            mapEditor.refresh();

            if (jetDlg != null) {
                jetDlg.closeBarbDlg();
            }

            prevTool.resetMouseHandler();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create the wind info(barb and FL text) at location loc.
     *
     * @param loc
     * @return
     */
    private DECollection createWindInfo(Coordinate loc, Jet aJet,
            boolean updateTemplate) {

        DECollection wind = new DECollection("WindInfo");
        wind.setParent(aJet);

        // create barb
        JetBarb barb = aJet.new JetBarb(null,
                new Color[] { new Color(0, 255, 0), new Color(255, 0, 0) },
                2.0f, 2.0, true, loc, VectorType.WIND_BARB, 100, 270, 1.0,
                false, "Vector", "Barb");

        IAttribute barbAttr = jetDlg.getBarbAttr();

        if (barbAttr != null) {
            barb.update(barbAttr);
        }

        if (updateTemplate) {
            jetDlg.updateBarbTemplate(barb);
        }

        barb.setSpeed(jetDlg.getBarbSpeed());
        wind.add(barb);

        // create FL text
        String flInfo[];
        if (jetDlg.getFLDepth() == null) {
            flInfo = new String[1];
        } else {
            flInfo = new String[2];
            flInfo[1] = jetDlg.getFLDepth();
        }

        flInfo[0] = "FL" + Integer.toString(jetDlg.getFlightLevel());

        Text txt = aJet.new JetText(null, "Courier", 18.0f,
                TextJustification.CENTER, new Coordinate(0, 0), 0,
                TextRotation.SCREEN_RELATIVE, flInfo, FontStyle.REGULAR,
                new Color(0, 255, 0), 0, 0, true, DisplayType.NORMAL, "Text",
                "General Text");

        wind.add(txt);

        IAttribute flAttr = jetDlg.getFLAttr();

        if (flAttr != null) {
            txt.update(flAttr);
        }

        if (updateTemplate) {
            jetDlg.updateFlTemplate(txt);
        }

        txt.setText(flInfo);

        return wind;

    }

}
