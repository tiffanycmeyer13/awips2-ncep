/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenIncDecTool
 *
 * 17 August 2011
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenSession;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.IncDecDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.filter.AcceptFilter;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;

/**
 * Implements PGEN palette Inc/Dec functions.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * 08/11         ?        B. Yin    Initial Creation.
 * Dec 02, 2021  95362    tjensen   Refactor PGEN Resource management to support
 *                                  multi-panel displays
 *
 * </pre>
 *
 * @author B. Yin
 */
public class PgenIncDecTool extends AbstractPgenDrawingTool {

    @Override
    protected void activateTool() {
        super.activateTool();
        if (attrDlg != null && attrDlg instanceof IncDecDlg) {
            ((IncDecDlg) attrDlg).setTool(this);
        }
    }

    @Override
    public void deactivateTool() {

        super.deactivateTool();

        PgenIncDecHandler pidh = (PgenIncDecHandler) mouseHandler;
        if (pidh != null) {
            pidh.cleanup();
        }

    }

    @Override
    /**
     * Get the MultiSelect mouse handler.
     */
    public IInputHandler getMouseHandler() {
        if (this.mouseHandler == null) {

            this.mouseHandler = new PgenIncDecHandler();

        }

        return this.mouseHandler;
    }

    /**
     * Implements input handler for mouse events.
     *
     * @author bingfan
     *
     */
    public class PgenIncDecHandler extends InputHandlerDefaultImpl {

        // x of the first mouse click
        private int theFirstMouseX;

        // y of the first mouse click
        private int theFirstMouseY;

        // flag to indicate if an area is selecting
        private boolean selectRect;

        private boolean shiftDown;

        List<Coordinate> polyPoints;

        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            theFirstMouseX = anX;
            theFirstMouseY = aY;

            if (button == 1) {

                return true;

            } else if (button == 3) {

                List<AbstractDrawableComponent> txtList = drawingLayers
                        .getAllSelected();
                if ((polyPoints == null || polyPoints.isEmpty())
                        && (txtList == null || txtList.isEmpty())) {
                    // Close the attribute dialog and do the cleanup.
                    if (attrDlg != null) {
                        attrDlg.close();
                    }

                    attrDlg = null;
                    pgenCategory = null;
                    pgenType = null;

                    drawingLayers.removeGhostLine();
                    drawingLayers.removeSelected();
                    mapEditor.refresh();
                    PgenUtil.setSelectingMode();

                } else {
                    if (polyPoints != null && !polyPoints.isEmpty()) {
                        // System.out.println("poly poins");
                    } else {
                        try {

                            List<AbstractDrawableComponent> newTxtList = new ArrayList<>();

                            for (AbstractDrawableComponent adc : drawingLayers
                                    .getAllSelected()) {
                                if (adc instanceof Text) {
                                    Text newTxt = (Text) adc.copy();
                                    newTxt.setText(new String[] {
                                            String.valueOf(Integer.parseInt(
                                                    ((Text) adc).getText()[0])
                                                    + ((IncDecDlg) attrDlg)
                                                            .getInterval()) });
                                    newTxtList.add(newTxt);
                                }
                            }

                            drawingLayers.replaceSelected(newTxtList);

                        } catch (NumberFormatException nfe) {

                        }
                        drawingLayers.removeSelected();
                        mapEditor.refresh();
                    }
                }
                return false;

            } else {
                return false;
            }
        }

        @Override
        public boolean handleMouseDownMove(int anX, int aY, int button) {

            if (!isResourceEditable() || button != 1) {
                return false;
            }

            selectRect = true;

            // draw the selected area
            ArrayList<Coordinate> points = new ArrayList<>();

            points.add(
                    mapEditor.translateClick(theFirstMouseX, theFirstMouseY));
            points.add(mapEditor.translateClick(theFirstMouseX, aY));
            points.add(mapEditor.translateClick(anX, aY));
            points.add(mapEditor.translateClick(anX, theFirstMouseY));

            DrawableElementFactory def = new DrawableElementFactory();
            Line ghost = (Line) def.create(DrawableType.LINE, null, "Lines",
                    "LINE_SOLID", points, drawingLayers.getActiveLayer());

            ghost.setLineWidth(1.0f);
            ghost.setColors(new Color[] { new java.awt.Color(255, 255, 255),
                    new java.awt.Color(255, 255, 255) });
            ghost.setClosed(true);
            ghost.setSmoothFactor(0);

            drawingLayers.setGhostLine(ghost);

            mapEditor.refresh();
            return true;
        }

        @Override
        public boolean handleMouseUp(int anX, int aY, int button) {
            if (!isResourceEditable()) {
                return false;
            }

            if (button == 1) {

                if (shiftDown && !selectRect) {
                    if (polyPoints == null) {
                        polyPoints = new ArrayList<>();
                    }
                    polyPoints.add(new Coordinate(anX, aY));

                } else if (selectRect) {

                    // select elements in the rectangle
                    int[] xpoints = { theFirstMouseX, theFirstMouseX, anX,
                            anX };
                    int[] ypoints = { theFirstMouseY, aY, aY, theFirstMouseY };

                    Polygon rectangle = new Polygon(xpoints, ypoints, 4);

                    drawingLayers.addSelected(inPoly(rectangle));

                    drawingLayers.removeGhostLine();
                    selectRect = false;
                }

                else {

                    // Check if mouse is in geographic extent
                    Coordinate loc = mapEditor.translateClick(anX, aY);
                    if (loc == null) {
                        return false;
                    }

                    // Get the nearest element
                    DrawableElement el = drawingLayers.getNearestElement(loc,
                            new AcceptFilter());

                    if (!drawingLayers.removeSelected(el) && el != null
                            && el instanceof Text) {
                        try {
                            Integer.parseInt(((Text) el).getString()[0]);
                            drawingLayers.addSelected(el);
                        } catch (NumberFormatException nfe) {

                        }
                    }
                }
            }

            else if (button == 3) {

                if (polyPoints != null) {
                    if (polyPoints.size() > 2) {
                        int[] xpoints = new int[polyPoints.size()];
                        int[] ypoints = new int[polyPoints.size()];
                        for (int ii = 0; ii < polyPoints.size(); ii++) {
                            xpoints[ii] = (int) polyPoints.get(ii).x;
                            ypoints[ii] = (int) polyPoints.get(ii).y;
                        }

                        Polygon poly = new Polygon(xpoints, ypoints,
                                polyPoints.size());
                        drawingLayers.addSelected(inPoly(poly));

                        drawingLayers.removeGhostLine();

                    }

                    polyPoints.clear();
                    shiftDown = false;
                }
            }

            mapEditor.setFocus();
            mapEditor.refresh();

            return true;
        }

        /**
         * Return Text elements that contain only numbers in the input area.
         *
         * @param poly
         * @return
         */
        private List<Text> inPoly(Polygon poly) {

            Iterator<AbstractDrawableComponent> it = drawingLayers
                    .getActiveLayer().getComponentIterator();
            List<Text> txtList = new ArrayList<>();

            while (it.hasNext()) {
                AbstractDrawableComponent adc = it.next();

                if (adc instanceof Text) {
                    Coordinate pt = ((Text) adc).getLocation();
                    double pix[] = mapEditor.translateInverseClick(pt);
                    if (poly.contains(pix[0], pix[1])) {
                        try {
                            Integer.parseInt(((Text) adc).getString()[0]);
                            txtList.add((Text) adc);

                        } catch (NumberFormatException nfe) {

                        }
                    }
                }
            }

            return txtList;
        }

        @Override
        public boolean handleMouseMove(int anX, int aY) {
            if (!isResourceEditable()) {
                return false;
            }

            // draw a ghost ploygon
            if (polyPoints != null && !polyPoints.isEmpty()) {

                polyPoints.add(new Coordinate(anX, aY));

                if (polyPoints.size() > 1) {

                    ArrayList<Coordinate> points = new ArrayList<>();

                    for (Coordinate loc : polyPoints) {
                        points.add(mapEditor.translateClick(loc.x, loc.y));
                    }

                    DrawableElementFactory def = new DrawableElementFactory();
                    Line ghost = (Line) def.create(DrawableType.LINE, null,
                            "Lines", "LINE_SOLID", points,
                            drawingLayers.getActiveLayer());

                    ghost.setLineWidth(1.0f);
                    ghost.setColors(
                            new Color[] { new java.awt.Color(255, 255, 255),
                                    new java.awt.Color(255, 255, 255) });
                    ghost.setClosed(true);
                    ghost.setSmoothFactor(0);

                    drawingLayers.setGhostLine(ghost);
                }

                mapEditor.refresh();

                polyPoints.remove(polyPoints.size() - 1);
            }
            return true;
        }

        @Override
        public boolean handleKeyDown(int keyCode) {
            if (!isResourceEditable()) {
                return false;
            }

            if (keyCode == SWT.SHIFT) {
                shiftDown = true;
            } else if (keyCode == SWT.DEL) {
                PgenResource pResource = PgenSession.getInstance()
                        .getCurrentResource();
                pResource.deleteSelectedElements();
            }
            return true;
        }

        @Override
        public boolean handleKeyUp(int keyCode) {
            if (!isResourceEditable()) {
                return false;
            }

            if (keyCode == SWT.SHIFT) {
                shiftDown = false;
            }
            return true;
        }

        public void cleanup() {
            drawingLayers.removeSelected();
            mapEditor.refresh();
            selectRect = false;
            shiftDown = false;
            if (polyPoints != null) {
                polyPoints.clear();
            }
        }

    }
}
