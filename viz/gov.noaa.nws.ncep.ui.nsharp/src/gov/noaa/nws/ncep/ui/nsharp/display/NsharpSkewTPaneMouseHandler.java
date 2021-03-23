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
package gov.noaa.nws.ncep.ui.nsharp.display;

import gov.noaa.nws.ncep.ui.nsharp.NsharpConstants;
import gov.noaa.nws.ncep.ui.nsharp.display.map.AbstractNsharpMapResource;
import gov.noaa.nws.ncep.ui.nsharp.display.rsc.NsharpSkewTPaneResource;
import gov.noaa.nws.ncep.ui.nsharp.display.rsc.NsharpWitoPaneResource;
import gov.noaa.nws.ncep.ui.nsharp.view.NsharpShowTextDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IView;
import com.raytheon.uf.viz.core.exception.VizException;
import org.locationtech.jts.geom.Coordinate;

/**
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#      Engineer     Description
 * -------      -------      --------     -----------
 * 04/23/2012    229         Chin Chen    Initial coding
 * 03/15/2018    6792        bsteffen     Stop stealing focus from other windows.
 * 04/16/2020    73571       smanoj       NSHARP D2D port refactor
 *
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpSkewTPaneMouseHandler extends NsharpAbstractMouseHandler {
    private Cursor editingCursor;

    private Cursor movingCursor;

    public NsharpSkewTPaneMouseHandler(NsharpEditor editor, IDisplayPane pane) {
        super(editor, pane);
        editingCursor = new Cursor(display, SWT.CURSOR_CROSS);
        movingCursor = new Cursor(display, SWT.CURSOR_SIZEWE);
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        if ((keyCode & SWT.SHIFT) != 0) {
            shiftDown = true;

            return true;
        } else if (shiftDown && keyCode == KEY_Z) {
            zDownWhileShiftDown = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean handleKeyUp(int keyCode) {
        if (getPaneDisplay() == null) {
            return false;
        }
        NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                .getPaneResource();
        if (keyCode == SWT.SHIFT) {
            shiftDown = false;
            return true;
        } else if (zDownWhileShiftDown && keyCode == KEY_Z) {
            zDownWhileShiftDown = false;
            skewRsc.toggleCurseDisplay();
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        theLastMouseX = x;
        theLastMouseY = y;
        if (getPaneDisplay() == null) {
            return false;
        } else if (editor != null && mouseButton == 1) {
            this.mode = Mode.CREATE;
            Coordinate c = editor.translateClick(x, y);
            NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                    .getPaneResource();
            boolean graphEditOn = skewRsc.getRscHandler().isEditGraphOn();
            if (skewRsc.getSkewTBackground().contains(c) == true
                    && graphEditOn) {
                // make sure it is clicked within skewt area
                // save current cursor coordinate difference between display and
                // view point
                Point curPoint = display.getCursorLocation();
                int xdiff = x - curPoint.x;
                int ydiff = y - curPoint.y;
                Coordinate anchoredPtC;
                // get editing cursor point
                anchoredPtC = skewRsc.getPickedTempPoint(c);
                if (anchoredPtC.x == 0 && anchoredPtC.y == 0)
                    // cursor is not within editing range ( i.e within 4 degree
                    // range from temp/dew line)
                    return false;
                skewRsc.getRscHandler()
                        .setInteractiveTempPointCoordinate(anchoredPtC);

                // Translate world screen coordinate to screen (x,y) coordinate
                anchorPointxy = editor.translateInverseClick(anchoredPtC);
                display.setCursorLocation((int) anchorPointxy[0] - xdiff,
                        (int) anchorPointxy[1] - ydiff);
                this.mode = Mode.SKEWT_DOWN;
            }
            editor.refresh();
        }

        return false;
    }

    @Override
    public boolean handleMouseDownMove(int aX, int aY, int button) {
        if (getPaneDisplay() == null || editor == null) {
            return false;
        } else if (button == 1) {

            Coordinate c = editor.translateClick(aX, aY);
            // make sure it is clicked within skewt area
            NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                    .getPaneResource();
            if (skewRsc == null)
                return false;
            boolean graphEditOn = skewRsc.getRscHandler().isEditGraphOn();
            if (this.mode == Mode.SKEWT_DOWN && graphEditOn) {
                if (skewRsc.getSkewTBackground().contains(c) == true) {
                    // NOTE::::keep y axis un-changed when moving mouse cursor
                    c = editor.translateClick(aX, anchorPointxy[1]);
                    skewRsc.getRscHandler()
                            .setInteractiveTempPointCoordinate(c);
                    editor.refresh();
                } else {
                    skewRsc.getRscHandler().setPlotInteractiveTemp(false);
                }
                return false;
            }
            if (prefManager.handleLongClick(ZOOMIN_PREF, button)
                    || prefManager.handleLongClick(ZOOMOUT_PREF, button)) {
                theLastMouseX = aX;
                theLastMouseY = aY;
            }
            if ((!prefManager.handleDrag(PAN_PREF, button))
                    || currentPane == null)
                return false;
            IView tmpView = (IView) currentPane.getRenderableDisplay().getView()
                    .clone();
            tmpView.shiftExtent(new double[] { aX, aY },
                    new double[] { theLastMouseX, theLastMouseY },
                    currentPane.getTarget());
            IExtent tmpExtent = tmpView.getExtent();
            double percentage = getPanningPercentage();
            double xMinThreshold = tmpExtent.getMinX()
                    + (tmpExtent.getMaxX() - tmpExtent.getMinX()) * percentage;
            double xMaxThreshold = tmpExtent.getMinX()
                    + (tmpExtent.getMaxX() - tmpExtent.getMinX())
                            * (1.0 - percentage);
            double yMinThreshold = tmpExtent.getMinY()
                    + (tmpExtent.getMaxY() - tmpExtent.getMinY()) * percentage;
            double yMaxThreshold = tmpExtent.getMinY()
                    + (tmpExtent.getMaxY() - tmpExtent.getMinY())
                            * (1.0 - percentage);

            double height = currentPane.getRenderableDisplay().getWorldHeight();
            double width = currentPane.getRenderableDisplay().getWorldWidth();

            int aX2 = aX, aY2 = aY;

            if ((0 <= xMinThreshold && width >= xMaxThreshold) == false) {
                if (((width < xMaxThreshold && theLastMouseX < aX)
                        || (0 > xMinThreshold
                                && theLastMouseX > aX)) == false) {
                    aX2 = (int) theLastMouseX;
                }
            }

            if ((0 <= yMinThreshold && height >= yMaxThreshold) == false) {
                if (((height < yMaxThreshold && theLastMouseY < aY)
                        || (0 > yMinThreshold
                                && theLastMouseY > aY)) == false) {
                    aY2 = (int) theLastMouseY;
                }
            }

            if (aX2 != theLastMouseX || aY2 != theLastMouseY) {
                currentPane.shiftExtent(new double[] { aX2, aY2 },
                        new double[] { theLastMouseX, theLastMouseY });
            }
            theLastMouseX = aX;
            theLastMouseY = aY;
            NsharpWitoPaneResource witoRsc = skewRsc.getRscHandler()
                    .getWitoPaneRsc();
            if (witoRsc != null)
                witoRsc.createAllWireFrameShapes();
            return true;

        }
        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        if (getPaneDisplay() == null) {
            return false;
        }
        this.mode = Mode.CREATE;
        NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                .getPaneResource();
        if (editor != null && skewRsc != null) {
            Coordinate currentCursorCoord = editor.translateClick(x, y);
            if (skewRsc.getSkewTBackground().contains(currentCursorCoord)) {
                // always update coordinate C to SkewT editor
                boolean graphEditOn = skewRsc.getRscHandler().isEditGraphOn();
                if (graphEditOn == true) {
                    Coordinate anchoredPtC;
                    // get editing cursor point
                    anchoredPtC = skewRsc
                            .getPickedTempPoint(currentCursorCoord);
                    int currentSkewTEditMode = skewRsc
                            .getCurrentSkewTEditMode();
                    if (anchoredPtC.x != 0 || anchoredPtC.y != 0) {
                        // cursor is within editing range ( i.e within 2 degree
                        // range from temp/dew line)
                        if (currentSkewTEditMode == NsharpConstants.SKEWT_EDIT_MODE_EDITPOINT)
                            display.getCursorControl().setCursor(editingCursor);
                        else if (currentSkewTEditMode == NsharpConstants.SKEWT_EDIT_MODE_MOVELINE)
                            display.getCursorControl().setCursor(movingCursor);
                    } else {
                        if (display.getCursorControl() != null)
                            display.getCursorControl().setCursor(null);
                    }
                }
                /*
                 * TBDWB else if(false){ //find wind barb point that closet to
                 * current cursor position
                 * skewRsc.findClosestWindBarbPoint(currentCursorCoord); //do
                 * nothing here now. findClosestWindBarbPoint() already handle
                 * things for wind barb plotting to use }
                 */

                skewRsc.setCursorInSkewT(true);
                try {
                    skewRsc.updateDynamicData(currentCursorCoord);
                    editor.refresh();
                } catch (VizException e) {
                    e.printStackTrace();
                }
            } else {
                skewRsc.setCursorInSkewT(false);
            }
        }
        return false;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {
        if (getPaneDisplay() == null) {
            return false;
        }
        if (editor != null) {
            NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                    .getPaneResource();
            // button 1 is left mouse button
            if (mouseButton == 1) {
                Coordinate c = editor.translateClick(x, y);
                if (skewRsc.getSkewTBackground().contains(c) == true
                        && this.mode == Mode.SKEWT_DOWN) {
                    skewRsc.getRscHandler().setPlotInteractiveTemp(false);
                    int currentSkewTEditMode = skewRsc
                            .getCurrentSkewTEditMode();
                    if (currentSkewTEditMode == NsharpConstants.SKEWT_EDIT_MODE_EDITPOINT)
                        skewRsc.getRscHandler().applyInteractiveTempPoint();
                    else if (currentSkewTEditMode == NsharpConstants.SKEWT_EDIT_MODE_MOVELINE)
                        skewRsc.getRscHandler().applyMovingTempLine();
                    NsharpShowTextDialog osDia = NsharpShowTextDialog
                            .getAccess();
                    if (osDia != null)
                        osDia.refreshTextData();
                }
                this.mode = Mode.CREATE;
            } else if (mouseButton == 3) {
                // right mouse button
                boolean graphEditOn = skewRsc.getRscHandler().isEditGraphOn();
                if (!graphEditOn) {
                    if (AbstractNsharpMapResource
                            .getMapResource(editor) != null) {
                        AbstractNsharpMapResource.getMapResource(editor)
                                .bringMapEditorToTop();
                    }
                } else {
                    Shell shell = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell();
                    Menu menu = new Menu(shell, SWT.POP_UP);
                    menu.setVisible(true);
                    MenuItem item1 = new MenuItem(menu, SWT.PUSH);
                    item1.setText("Edit Point");
                    item1.addListener(SWT.Selection, new Listener() {
                        @Override
                        public void handleEvent(Event event) {
                            MenuItem selItem = (MenuItem) event.widget;
                            String string = selItem.getText();
                            if ("Edit Point".equals(string)) {
                                int currentSkewTEditMode = NsharpConstants.SKEWT_EDIT_MODE_EDITPOINT;
                                NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                                        .getPaneResource();
                                skewRsc.setCurrentSkewTEditMode(
                                        currentSkewTEditMode);
                            }
                        }
                    });
                    MenuItem item2 = new MenuItem(menu, SWT.PUSH);
                    item2.setText("Move Line");
                    item2.addListener(SWT.Selection, new Listener() {
                        @Override
                        public void handleEvent(Event event) {
                            MenuItem selItem = (MenuItem) event.widget;
                            String string = selItem.getText();
                            if ("Move Line".equals(string)) {
                                int currentSkewTEditMode = NsharpConstants.SKEWT_EDIT_MODE_MOVELINE;
                                NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                                        .getPaneResource();
                                skewRsc.setCurrentSkewTEditMode(
                                        currentSkewTEditMode);
                            }
                        }
                    });
                }
            }
            editor.refresh();
        }
        return false;
    }

    @Override
    public boolean handleMouseExit(Event event) {
        cursorInPane = false;
        NsharpSkewTPaneResource skewRsc = (NsharpSkewTPaneResource) getDescriptor()
                .getPaneResource();
        if (skewRsc != null) {
            skewRsc.setCursorInSkewT(false);
        }
        this.mode = Mode.CREATE;
        return false;
    }

    public void disposeCursor() {
        editingCursor.dispose();
    }
}
