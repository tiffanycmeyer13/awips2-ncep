/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenLabeledLineDelHandler
 *
 * 8 September 2010
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.labeledlines.Label;
import gov.noaa.nws.ncep.ui.pgen.elements.labeledlines.LabeledLine;
//import gov.noaa.nws.ncep.viz.ui.display.NCMapEditor;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;

/**
 * Mouse handler to delete labels of a labeled line or flip a line.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 09/10         304      B. Yin    Initial Creation.
 * 12/11         ?        B. Yin    Added open/close functions. should change
 *                                  the name of this class?
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */
public class PgenLabeledLineDelHandler extends InputHandlerDefaultImpl {

    private final AbstractEditor mapEditor;

    private final PgenResourceList drawingLayers;

    // LabeledLineDrawingTool or LabeledLineModifyTool
    private final ILabeledLine prevTool;

    // LabeledLine attribute dialog
    private final AttrDlg dlg;

    // delLine flag
    private final boolean delLine;

    // flip flag
    private final boolean flip;

    private final boolean openClose;

    // the line working on
    private LabeledLine labeledLine;

    /**
     * public constructor
     *
     * @param mapEditor
     * @param drawingLayers
     * @param prevTool
     * @param dlg
     */
    public PgenLabeledLineDelHandler(AbstractEditor mapEditor,
            PgenResourceList drawingLayers, ILabeledLine prevTool, AttrDlg dlg,
            boolean delLine, boolean flip, boolean openClose) {
        this.mapEditor = mapEditor;
        this.drawingLayers = drawingLayers;
        this.prevTool = prevTool;
        this.dlg = dlg;
        this.labeledLine = prevTool.getLabeledLine();
        this.delLine = delLine;
        this.flip = flip;
        this.openClose = openClose;
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

        if (button == 1) {

            if (labeledLine != null) {
                AbstractDrawableComponent nearestComp = drawingLayers
                        .getNearestComponent(loc);

                if ((nearestComp != null) && nearestComp.getPgenType()
                        .equalsIgnoreCase(labeledLine.getPgenType())) {
                    labeledLine = (LabeledLine) nearestComp;

                    // make a copy in order for undo/redo to work
                    LabeledLine newll = labeledLine.copy();

                    AbstractDrawableComponent adc = this.getNearest(loc, newll);
                    if (adc == null) {
                        return false;
                    }

                    if (flip) {
                        // flip
                        if (adc instanceof Line) {
                            Line newLn = (Line) PgenToolUtils
                                    .createReversedDrawableElement(adc);
                            newll.remove(adc);
                            newll.add(newLn);
                        } else {
                            return false;
                        }
                    } else if (openClose) {
                        if (adc instanceof Line) {
                            Line newLn = (Line) (adc.copy());
                            if (newLn.isClosedLine()) {
                                newLn.setClosed(false);
                            } else {
                                newLn.setClosed(true);
                            }
                            newll.remove(adc);
                            newll.add(newLn);
                        } else {
                            return false;
                        }
                    } else {
                        // remove
                        newll.remove(adc);
                    }

                    drawingLayers.replaceElement(labeledLine, newll);
                    labeledLine = newll;
                    prevTool.setLabeledLine(newll);

                    if (drawingLayers.getSelectedDE() != null) {
                        this.resetSelected(newll);
                    }

                    mapEditor.refresh();
                }
            }
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
            dlg.resetLabeledLineBtns();
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

    /**
     * get nearest line or label(depends on delLine flag) in the input labeled
     * line
     *
     * @param loc
     * @param ll
     * @return
     */
    private AbstractDrawableComponent getNearest(Coordinate loc,
            LabeledLine ll) {

        AbstractDrawableComponent ret = null;
        double[] locScreen = mapEditor.translateInverseClick(loc);

        // find nearest adc
        Iterator<AbstractDrawableComponent> it = ll.getComponentIterator();
        double minDist = Double.MAX_VALUE;

        while (it.hasNext()) {
            AbstractDrawableComponent adc = it.next();
            double dist = Double.MAX_VALUE;

            if ((delLine && adc instanceof Line)) {
                // find line
                Object pts[] = adc.getPoints().toArray();

                for (int ii = 0; ii < pts.length; ii++) {

                    if (ii == pts.length - 1) {
                        if (adc instanceof Line
                                && ((Line) adc).isClosedLine()) {
                            dist = drawingLayers.distanceFromLineSegment(loc,
                                    (Coordinate) pts[ii], (Coordinate) pts[0]);
                        } else {
                            break;
                        }
                    } else {
                        dist = drawingLayers.distanceFromLineSegment(loc,
                                (Coordinate) pts[ii], (Coordinate) pts[ii + 1]);
                    }

                    if (dist < minDist) {
                        minDist = dist;
                        ret = adc;
                    }
                }
            } else if (!delLine && adc instanceof Label) {
                // find label
                double[] pt = mapEditor.translateInverseClick(
                        ((Label) adc).getSpe().getLocation());
                Point ptScreen = new GeometryFactory()
                        .createPoint(new Coordinate(pt[0], pt[1]));

                dist = ptScreen.distance(new GeometryFactory().createPoint(
                        new Coordinate(locScreen[0], locScreen[1])));

                if (dist < minDist) {
                    minDist = dist;
                    ret = adc;
                }
            }
        } // while

        return ret;
    }

    /**
     * reset selected element(handle bars)
     *
     * @param labeledLine
     */
    private void resetSelected(LabeledLine labeledLine) {
        if (labeledLine != null) {
            drawingLayers.removeSelected();
            Iterator<DrawableElement> it = labeledLine.createDEIterator();
            while (it.hasNext()) {
                drawingLayers.addSelected(it.next());
            }
        }
    }

}
