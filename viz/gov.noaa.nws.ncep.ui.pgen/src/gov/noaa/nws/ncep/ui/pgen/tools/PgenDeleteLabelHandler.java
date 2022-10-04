package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.Iterator;

import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlg;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourMinmax;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;

/**
 * Implements a modal map tool for PGEN deleting part function for labels of
 * non-met contour symbols only. This is the action hanlder for doing that as
 * registered in the plugin.xml
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer   Description
 * ------------- -------- ---------- -------------------------------------------
 * Dec 06, 0014  8199     S Russell  Initial Creation.
 * Dec 02, 2021  95362    tjensen    Refactor PGEN Resource management to
 *                                   support multi-panel displays
 *
 * </pre>
 *
 * @author Steve Russell
 */

public class PgenDeleteLabelHandler extends InputHandlerDefaultImpl {

    protected AbstractEditor mapEditor;

    protected PgenResourceList pgenrscs;

    protected AbstractPgenTool tool;

    protected AttrDlg attrDlg;

    /**
     * Constructor
     *
     * @param AbstractPgenTool
     *            tool
     */
    public PgenDeleteLabelHandler(AbstractPgenTool tool) {
        this.tool = tool;
        pgenrscs = tool.getDrawingLayers();
        mapEditor = tool.mapEditor;

        if (tool instanceof AbstractPgenDrawingTool) {
            attrDlg = ((AbstractPgenDrawingTool) tool).getAttrDlg();
        }
    }

    /**
     * Handle a mouse down event
     *
     * @param x
     *            the x screen coordinate
     * @param y
     *            the y screen coordinate
     * @param mouseButton
     *            the button held down
     * @return true if other handlers should be pre-empted
     */
    @Override
    public boolean handleMouseDown(int anX, int aY, int button) {
        return false;
    }

    /**
     * Handle a mouse down move event
     *
     * @param x
     *            the x screen coordinate
     * @param y
     *            the y screen coordinate
     * @param mouseButton
     *            the button held down
     * @return true if other handlers should be pre-empted
     */
    @Override
    public boolean handleMouseDownMove(int x, int y, int mouseButton) {
        return false;
    }

    /**
     * Close any attribute dialogs, then delete the label
     */
    @Override
    public void preprocess() {

        if (pgenrscs.getSelectedComp() != null) {

            if (attrDlg != null) {
                AbstractDrawableComponent adc = pgenrscs.getSelectedComp()
                        .getParent();

                if ((adc instanceof Layer) || adc.isLabeledSymbolType()) {
                    attrDlg.close();
                }

            }

            doDelete();
            tool.resetMouseHandler();
        }
    }

    /**
     * Deletes the label from a non-met,non-contour symbol
     */
    private void doDelete() {
        AbstractDrawableComponent adc = pgenrscs.getSelectedComp();

        if (adc.getParent() instanceof ContourMinmax
                || adc.isLabeledSymbolType()) {
            deleteFirstLabelFound((DECollection) adc.getParent());
        } else {
            pgenrscs.removeElement(adc);
        }

        // Set the selected element ( now removed ) as null
        pgenrscs.removeSelected();
        mapEditor.refresh();
    }

    /**
     * Remove the first Text object found in a DECollection object
     *
     * @param labeledSymbol
     *            a collection holding at least 2 drawable elements
     */
    private void deleteFirstLabelFound(DECollection labeledSymbol) {

        Iterator<AbstractDrawableComponent> it = labeledSymbol
                .getComponentIterator();

        while (it.hasNext()) {
            AbstractDrawableComponent item = it.next();
            if (item instanceof Text) {
                // Remove the label from the screen
                pgenrscs.removeElement(item);
                return;
            }
        }

    }

    /**
     * Get the mapEditor object
     *
     * @return AbstractEditor mapEditor
     */
    public AbstractEditor getMapEditor() {
        return mapEditor;
    }

    /**
     * Get the PgenResource object
     *
     * @return PgenResource pgenrsc
     */
    public PgenResourceList getPgenrsc() {
        return pgenrscs;
    }

}
