/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenJetHashDeletingHandler
 *
 * 6 December 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;

import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.attrdialog.JetAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.ISinglePoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.IJetTools;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet;
import gov.noaa.nws.ncep.ui.pgen.elements.Jet.JetHash;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;
//import gov.noaa.nws.ncep.viz.ui.display.NCMapEditor;

/**
 * Mouse handler to delete hash when drawing jet.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 12/10         366      B. Yin    Initial Creation.
 * 04/11         #?       B. Yin    Re-factor IAttribute
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */
public class PgenJetHashDeletingHandler extends InputHandlerDefaultImpl {

    private final AbstractEditor mapEditor;

    private final PgenResourceList drawingLayers;

    private final IJetBarb prevTool;

    private AbstractDrawableComponent hashSelected;

    private final JetAttrDlg jetDlg;

    /**
     * Public constructor
     *
     * @param mapEditor
     * @param drawingLayers
     * @param prevTool
     * @param jet
     */
    public PgenJetHashDeletingHandler(AbstractEditor mapEditor,
            PgenResourceList drawingLayers, IJetBarb prevTool,
            JetAttrDlg jetDlg) {
        this.mapEditor = mapEditor;
        this.drawingLayers = drawingLayers;
        this.prevTool = prevTool;
        this.jetDlg = jetDlg;

        drawingLayers.removeGhostLine();
        mapEditor.refresh();
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
            if (hashSelected != null) {
                Jet newJet = jet.copy();
                // Remove the selected hash from jet
                newJet.remove(newJet.getNearestComponent(
                        ((ISinglePoint) (hashSelected.getPrimaryDE()))
                                .getLocation()));
                IJetTools snapTool = newJet.getSnapTool();
                if (snapTool != null) {
                    snapTool.snapJet(newJet);
                }

                drawingLayers.replaceElement(jet, newJet);
                jet = newJet;
                prevTool.setJet(jet);

                // de-select the barb
                drawingLayers.removeSelected(hashSelected);
                drawingLayers.setGhostLine(null);

                hashSelected = null;
                jetDlg.updateSegmentPane();

            } else {
                // Get the nearest hash and set it as the selected.
                hashSelected = jet.getNearestDE(loc);
                if (!(hashSelected instanceof JetHash)) {
                    hashSelected = null;
                } else {
                    drawingLayers.addSelected(hashSelected);
                    Symbol selectSymbol = new Symbol(null,
                            new Color[] { Color.red }, 2.5f, 7.5, false,
                            ((JetHash) hashSelected).getLocation(), "Marker",
                            "DOT");
                    drawingLayers.setGhostLine(selectSymbol);

                }
            }

            mapEditor.refresh();
            return true;

        } else if (button == 3) {
            return true;
        } else {
            return false;
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

            prevTool.resetMouseHandler();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int mouseButton) {
        if (!drawingLayers.isEditable() || shiftDown) {
            return false;
        } else {
            return true;
        }
    }

}
