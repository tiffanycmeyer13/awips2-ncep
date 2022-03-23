package gov.noaa.nws.ncep.viz.ui.display;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IRenderableDisplayChangedListener;
import com.raytheon.uf.viz.core.IRenderableDisplayChangedListener.DisplayChangeType;
import com.raytheon.uf.viz.core.datastructure.LoopProperties;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.color.BackgroundColor;
import com.raytheon.viz.ui.color.IBackgroundColorChangedListener.BGColorMode;
import com.raytheon.viz.ui.editor.ISelectedPanesChangedListener;
import com.raytheon.viz.ui.input.InputAdapter;
import com.raytheon.viz.ui.input.InputManager;
import com.raytheon.viz.ui.panes.PaneManager;
import com.raytheon.viz.ui.panes.VizDisplayPane;

import gov.noaa.nws.ncep.viz.common.display.INatlCntrsDescriptor;
import gov.noaa.nws.ncep.viz.common.display.INatlCntrsPaneManager;
import gov.noaa.nws.ncep.viz.common.display.INatlCntrsRenderableDisplay;
import gov.noaa.nws.ncep.viz.common.display.INcPaneID;
import gov.noaa.nws.ncep.viz.common.display.INcPaneLayout;
import gov.noaa.nws.ncep.viz.common.display.IPaneLayoutable;
import gov.noaa.nws.ncep.viz.common.display.NcDisplayName;
import gov.noaa.nws.ncep.viz.common.display.NcDisplayType;

/**
 * Natl Cntrs extention of PaneManager.
 *
 * Note that this uses a slightly different method of selecting panes.
 * IPaneManager allows for different kind of pane selections (ie actions of
 * LOAD, IMAGE, ....) but one one may be selected for each action.)
 * NCPaneManager ignores the action but will allow for more than one pane to be
 * selected at one time. selectPane() and deselectPane() should be called
 * instead of setSelectedPane().
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#    Engineer     Description
 * ------------ ----------  -----------  --------------------------
 * 03/07/11      R1G2-9     Greg Hull    Created
 * 07/18/12      #649       Shova Gurung Fixed echo/virtual cursor display issue.
 * 09/13/12      ?          B. Yin       Refresh only for multiple panes
 * 01/28/12      #972       Greg Hull    created from NCPaneManager minus remove PaneLayout code.
 * 12/16/13      #958       sgurung      Do not set virtual cursor for NcNonMapRenderableDisplay
 * 05/16/2014    #1136      qzhou        Add NCTimeseries for Graph.
 * 09/09/2014    R4078      sgurung      Added "contextualMenusEnabled" to specify whether to
 *                                       enable/disable contextual menus in panes.
 * 03/25/2022    8790       mapeters     Handle pane manager refactor
 *
 * </pre>
 *
 * @author ghull
 */
public abstract class AbstractNcPaneManager extends PaneManager
        implements IInputHandler, INatlCntrsPaneManager {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractNcPaneManager.class);

    public static final String NC_PANE_SELECT_ACTION = "NC_SELECT_PANE";

    protected INcPaneLayout paneLayout;

    protected NcDisplayType displayType;

    // protected int displayId;
    protected NcDisplayName displayName;

    // a flag to indicate that resources other than those in the default RBD
    // have been loaded.
    // The primary and currently only use for this is that if false, this editor
    // is considered
    // empty or available for loading new data.

    // Currently it is only the resources that are checked and not the sync
    // flags or the area.
    //
    protected boolean availableToLoad = false;

    protected boolean geoSyncPanesEnabled;

    protected boolean timeSyncPanesEnabled = true;

    protected boolean contextualMenusEnabled = true;

    // means loop rsc on, and Hide btn displayed
    protected boolean isHide = false;

    protected String defaultTool = "gov.noaa.nws.ncep.viz.tools.pan";

    // The pane that is currently selected. While D2D allows for mulitple
    // types of selections (actions).
    // NC only supports selected/not selected (ie NC_PANE_SELECT_ACTION).
    // We are using the selectedPanes map from PaneManager but the keys are
    // bogus
    // and only the entry set is used.
    // protected ArrayList<IDisplayPane> selectedPanesList = null;

    // we could use the member in PaneManager if it were not private
    protected Set<ISelectedPanesChangedListener> listeners;

    // Implement the VirtualCursor and Pane Selection
    protected class NcPaneMouseHandler extends InputAdapter {

        private IDisplayPane[] lastHandledPanes = null;

        protected NcPaneMouseHandler(AbstractNcEditor ncEd) {
        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            Coordinate c = translateClick(x, y);

            if (c == null) {
                return false;
            }

            lastHandledPanes = getDisplayPanes();

            boolean geoSync = true;

            if (lastHandledPanes.length > 1
                    && paneContainer instanceof AbstractNcEditor) {

                geoSync = NcEditorUtil
                        .arePanesGeoSynced((AbstractNcEditor) paneContainer);
            }

            for (IDisplayPane pane : lastHandledPanes) {

                if (geoSync && currentMouseHoverPane != pane
                        && !(currentMouseHoverPane
                                .getRenderableDisplay() instanceof NCNonMapRenderableDisplay)) {
                    ((VizDisplayPane) pane).setVirtualCursor(c);
                } else {
                    ((VizDisplayPane) pane).setVirtualCursor(null);
                }
            }

            // Refresh only for multiple panes.
            if (getNumberofPanes() > 1) {
                refresh();
            }

            return false;
        }

        @Override
        public boolean handleMouseExit(Event event) {
            if (lastHandledPanes != null) {
                for (IDisplayPane pane : lastHandledPanes) {
                    ((VizDisplayPane) pane).setVirtualCursor(null);
                }
            }
            return false;
        }

        @Override
        public boolean handleMouseDown(int x, int y, int mouseButton) {

            return false;
        }
    }

    public AbstractNcPaneManager(INcPaneLayout playout,
            NcDisplayType dispType) {
        paneLayout = playout;
        setDisplayType(dispType);

        // this is defined in PaneManager. We are using this but we
        // ignore the keys and just fill them in with bogus unique numbers.
        selectedPanes = new HashMap<>();
        listeners = new HashSet<>();
    }

    @Override
    public INcPaneLayout getPaneLayout() {
        return paneLayout;
    }

    // from INcPaneContainer
    @Override
    public IPaneLayoutable getPane(INcPaneID pid) {
        if (paneLayout.containsPaneId(pid)
                && paneLayout.getPaneIndex(pid) < displayPanes.size()) {
            VizDisplayPane vdp = displayPanes.get(paneLayout.getPaneIndex(pid));
            if (vdp != null
                    && vdp.getRenderableDisplay() instanceof IPaneLayoutable) {
                return (IPaneLayoutable) vdp.getRenderableDisplay();
            }
        }
        return null;
    }

    // called from AbstractEditor.createPartControl
    @Override
    public abstract void initializeComponents(IDisplayPaneContainer container,
            Composite parent);

    // The following methods are based on the NcDisplayType but these can be
    // overridden
    public String getEditorId() {
        if (displayType == NcDisplayType.NMAP_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NcMapEditor";
        } else if (displayType == NcDisplayType.NTRANS_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NTransDisplay";
        } else if (displayType == NcDisplayType.SOLAR_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.SolarDisplay";
        } else if (displayType == NcDisplayType.GRAPH_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.GraphDisplay";

        }
        return "Unsupported displayType: " + displayType.toString();
    }

    // for NSHARP this may be based off of the paneId
    public String getRenderableDisplayClass(INcPaneID pid) {
        if (displayType == NcDisplayType.NMAP_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCMapRenderableDisplay";
        } else if (displayType == NcDisplayType.NTRANS_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCNonMapRenderableDisplay";
        } else if (displayType == NcDisplayType.SOLAR_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCNonMapRenderableDisplay";
        } else if (displayType == NcDisplayType.GRAPH_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCTimeSeriesRenderableDisplay";

        }
        return "Unsupported displayTyep: " + displayType.toString();
    }

    public String getDescriptorClass(String rendDispName, INcPaneID pid) {
        if (displayType == NcDisplayType.NMAP_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCMapDescriptor";
        } else if (displayType == NcDisplayType.NTRANS_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCNonMapDescriptor";
        } else if (displayType == NcDisplayType.SOLAR_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCNonMapDescriptor";
        } else if (displayType == NcDisplayType.GRAPH_DISPLAY) {
            return "gov.noaa.nws.ncep.viz.ui.display.NCTimeSeriesDescriptor";
        }
        return "Unsupported displayTyep: " + displayType.toString();
    }

    public String getDefaultTool() {
        return defaultTool;
    }

    public INatlCntrsRenderableDisplay createNcRenderableDisplay(
            INcPaneID pid) {
        try {
            String rendDispClassName = getRenderableDisplayClass(pid);
            Class<?> rendDispClass = Class.forName(rendDispClassName);

            Object rendDispObj = rendDispClass.getDeclaredConstructor()
                    .newInstance();

            if (rendDispObj instanceof INatlCntrsRenderableDisplay) {

                INatlCntrsRenderableDisplay iRendDisp = (INatlCntrsRenderableDisplay) rendDispObj;
                iRendDisp.setPaneId(pid);

                Class<?> descrClass = Class
                        .forName(getDescriptorClass(rendDispClassName, pid));
                Object descrObj = descrClass.getDeclaredConstructor()
                        .newInstance();

                if (descrObj instanceof INatlCntrsDescriptor) {
                    INatlCntrsDescriptor iDescr = (INatlCntrsDescriptor) descrObj;

                    iRendDisp.setDescriptor(iDescr);

                    return iRendDisp;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error creating renderable display for pane: " + pid, e);
        }

        return null;
    }

    @Override
    protected void registerHandlers(IDisplayPane pane) {
        // I think that this listener needs to be added before the
        // input Manager otherwise an inputHandler method may reference
        // the wrong selected pane.
        //
        final IDisplayPane thisPane = pane;

        pane.addListener(SWT.MouseDown, e -> {
            if (e.button == 1 || e.button == 3) {

                boolean radioBehaviour = ((e.stateMask & SWT.CONTROL) == 0);

                if (paneContainer instanceof AbstractNcEditor) {
                    NcEditorUtil.selectPane((AbstractNcEditor) paneContainer,
                            thisPane, radioBehaviour);
                }
            }
        });

        pane.addListener(SWT.MouseUp, inputManager);
        pane.addListener(SWT.MouseDown, inputManager);
        pane.addListener(SWT.MouseMove, inputManager);
        pane.addListener(SWT.MouseWheel, inputManager);
        pane.addListener(SWT.MouseHover, inputManager);
        pane.addListener(SWT.MouseDoubleClick, inputManager);
        pane.addListener(SWT.KeyDown, inputManager);
        pane.addListener(SWT.KeyUp, inputManager);
        pane.addListener(SWT.MenuDetect, inputManager);
        pane.addListener(SWT.MouseExit, inputManager);
    }

    @Override
    public int getNumberofPanes() {
        return displayPanes.size();
    }

    //
    public int getNumberofSelectedPanes() {
        return selectedPanes.size();
    }

    @Override
    public NcDisplayType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(NcDisplayType displayType) {
        this.displayType = displayType;
    }

    // This interface method shouldn't be called from NC perspective.
    @Override
    public void setSelectedPane(String action, IDisplayPane pane) {
        if (action == null || !NC_PANE_SELECT_ACTION.equals(action)) {
            return;
        }

        // if not already selected then put it in the map.
        // NOTE: We are using the map from PaneManager but this was meant
        // to store different types (actions) of selections. We ignore this
        // since we only have 1 kind of selection (NC_PANE_SELECT_ACTION) but
        // multiple panes can be selected at once.
        if (!selectedPanes.containsValue(pane)) {
            String keyStr = Integer.toString(displayPanes.indexOf(pane));
            if (selectedPanes.containsKey(keyStr)) {
                statusHandler.warn(
                        "Sanity check in NcPaneManager.setSelectedPane. A non-unique key was created? "
                                + keyStr);
                return;
            }
            selectedPanes.put(keyStr, pane);
        }
    }

    public void selectPane(IDisplayPane pane) {
        setSelectedPane(NC_PANE_SELECT_ACTION, pane);
    }

    public void selectPanes(List<IDisplayPane> seldPanes) {

        if (seldPanes.isEmpty()) {
            return;
        }

        selectedPanes.clear();

        for (IDisplayPane p : seldPanes) {
            setSelectedPane(NC_PANE_SELECT_ACTION, p);
        }

        for (ISelectedPanesChangedListener lstnr : listeners) {
            lstnr.selectedPanesChanged(NC_PANE_SELECT_ACTION,
                    getSelectedPanes(NC_PANE_SELECT_ACTION));
        }
    }

    // This method is part of the IPaneManager interface but
    // for the NC Perspective multiple panes may be selected.
    // Here we'll return the first selected pane in the list
    // but if the caller needs to handle cases with multiple
    // selected panes, it should call getSelectedPanes instead.
    //
    @Override
    public IDisplayPane getSelectedPane(String action) {
        if (action == null || !NC_PANE_SELECT_ACTION.equals(action)) {
            return null;
        }

        if (getNumberofSelectedPanes() > 0) {

            return selectedPanes.values().iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public IDisplayPane[] getSelectedPanes(String action) {
        if (action == null || !NC_PANE_SELECT_ACTION.equals(action)) {
            return null;
        }
        IDisplayPane seldPanes[] = selectedPanes.values()
                .toArray(new IDisplayPane[0]);

        return seldPanes;
    }

    @Override
    public boolean isSelectedPane(String action, IDisplayPane pane) {
        if (!NC_PANE_SELECT_ACTION.equals(action)) {
            return false;
        }
        return selectedPanes.containsValue(pane);
    }

    @Override
    public void addSelectedPaneChangedListener(
            ISelectedPanesChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSelectedPaneChangedListener(
            ISelectedPanesChangedListener listener) {
        listeners.remove(listener);
    }

    // this should get called with the displays in the order in displaysToLoad
    // which should be in the order of the pane index.
    //
    protected IDisplayPane addRenderableDisplayToPane(
            IRenderableDisplay renderableDisplay, Composite canvasComp,
            INcPaneID pid) {

        // sanity check
        // the pane id index should match the index into the displayPanes array.
        if (pid != null
                && paneLayout.getPaneIndex(pid) != displayPanes.size()) {
            statusHandler.warn("Pane index (" + paneLayout.getPaneIndex(pid)
                    + ") doesn't match the index in displayPanes array ("
                    + displayPanes.size() + ")");
        }

        VizDisplayPane pane = null;
        try {
            pane = new VizDisplayPane(paneContainer, canvasComp,
                    renderableDisplay, contextualMenusEnabled);
            // register the inputManager and the mouse listener for pane
            // selection
            registerHandlers(pane);

            final VizDisplayPane thisPane = pane;

            pane.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    activatedPane = thisPane;
                }

                @Override
                public void focusLost(FocusEvent e) {
                }
            });

            pane.addMouseTrackListener(new MouseTrackListener() {
                @Override
                public void mouseEnter(MouseEvent e) {
                    activatedPane = thisPane;
                    currentMouseHoverPane = thisPane;
                }

                @Override
                public void mouseExit(MouseEvent e) {
                }

                @Override
                public void mouseHover(MouseEvent e) {
                }
            });
        } catch (VizException e) {
            statusHandler.handle(Priority.PROBLEM, "Error adding pane", e);
            if (pane != null) {
                pane.dispose();
            }
        }

        if (pane != null) {
            try {
                if (activatedPane == null) {
                    activatedPane = pane;
                }
                // ++displayedPaneCount; // not hiding panes...
                if (displayPanes.size() > 0) {
                    pane.getRenderableDisplay().setBackgroundColor(
                            displayPanes.get(0).getRenderableDisplay()
                                    .getBackgroundColor());
                } else {
                    BackgroundColor.getActivePerspectiveInstance().setColor(
                            BGColorMode.EDITOR,
                            pane.getRenderableDisplay().getBackgroundColor());
                }

                if (!displayPanes.isEmpty()) {
                    pane.getDescriptor().synchronizeTimeMatching(
                            displayPanes.get(0).getDescriptor());
                }

                displayPanes.add(pane);

            } catch (Throwable t) {
                statusHandler.handle(Priority.PROBLEM, "Error adding pane", t);
            }
        }

        if (getNumberofSelectedPanes() == 0) {
            selectPane(pane);
            currentMouseHoverPane = pane;
            activatedPane = pane;
        }

        return pane;
    }

    @Override
    public IDisplayPane addPane(IRenderableDisplay renderableDisplay) {
        INcPaneID paneId = null;

        if (renderableDisplay instanceof INatlCntrsRenderableDisplay) {
            paneId = ((INatlCntrsRenderableDisplay) renderableDisplay)
                    .getPaneId();
        } else {
            statusHandler.error(
                    "Unable to add unexpected renderable display type to pane: "
                            + renderableDisplay);
        }

        Composite canvasComp = new Composite(composite, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        canvasComp.setLayout(gl);
        canvasComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return addRenderableDisplayToPane(renderableDisplay, canvasComp,
                paneId);
    }

    @Override
    public void removePane(IDisplayPane pane) {
        // Not implemented for NCP
    }

    @Override
    public void hidePane(IDisplayPane pane) {
        // Not implemented for NCP
    }

    @Override
    public void showPane(IDisplayPane pane) {
        // Not implemented for NCP
    }

    // TODO : if we implement hide/show then this will need to change, but for
    // now all panes are shown.
    @Override
    public int displayedPaneCount() {
        return displayPanes.size();
    }

    @Override
    public void clear() {
        // Not implemented for NCP
    }

    @Override
    public IDisplayPane[] getDisplayPanes() {
        return displayPanes.toArray(new VizDisplayPane[displayPanes.size()]);
    }

    @Override
    public LoopProperties getLoopProperties() {
        return paneContainer.getLoopProperties();
    }

    @Override
    public void setLoopProperties(LoopProperties loopProperties) {
        paneContainer.setLoopProperties(loopProperties);
    }

    @Override
    public IDisplayPane getActiveDisplayPane() {
        if (activatedPane == null) {
            activatedPane = displayPanes.size() > 0 ? displayPanes.get(0)
                    : null;
        }
        return activatedPane;
    }

    @Override
    public void refresh() {
        for (IDisplayPane pane : displayPanes) {
            pane.refresh();
        }
    }

    @Override
    public void addRenderableDisplayChangedListener(
            IRenderableDisplayChangedListener displayChangedListener) {
    }

    @Override
    public void removeRenderableDisplayChangedListener(
            IRenderableDisplayChangedListener displayChangedListener) {
    }

    @Override
    public void notifyRenderableDisplayChangedListeners(IDisplayPane pane,
            IRenderableDisplay display, DisplayChangeType type) {
    }

    @Override
    public void registerMouseHandler(IInputHandler handler,
            InputPriority priority) {
        inputManager.registerMouseHandler(handler, priority);
    }

    @Override
    public void registerMouseHandler(IInputHandler handler) {
        inputManager.registerMouseHandler(handler);
    }

    @Override
    public void unregisterMouseHandler(IInputHandler handler) {
        inputManager.unregisterMouseHandler(handler);
    }

    @Override
    public void dispose() {
        activatedPane = null;
        currentMouseHoverPane = null;
        selectedPanes = null;
        composite = null;
    }

    @Override
    public InputManager getMouseManager() {
        return inputManager;
    }

    @Override
    public void setFocus() {
        IDisplayPane pane = getActiveDisplayPane();
        if (pane != null) {
            pane.setFocus();
        }
    }

    public IDescriptor getDescriptor() {
        IDescriptor descriptor = null;
        IRenderableDisplay display = getActiveDisplayPane()
                .getRenderableDisplay();
        if (display != null) {
            descriptor = display.getDescriptor();
        }
        return descriptor;
    }

    // from PaneManager if we want to use it.....
    @Override
    public BufferedImage screenshot() {
        return getActiveDisplayPane().getTarget().screenshot();
    }

    @Override
    public NcDisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(NcDisplayName dname) {
        displayName = dname;
    }

    public boolean isDisplayAvailableToLoad() {
        return availableToLoad;
    }

    public void setDisplayAvailable(boolean av) {
        this.availableToLoad = av;
    }

    public boolean getHideShow() {
        return isHide;
    }

    public boolean setHideShow(boolean isHide) {
        return this.isHide = isHide;
    }

    public boolean getAutoUpdate() {
        INatlCntrsDescriptor desc = (INatlCntrsDescriptor) getDescriptor();
        return desc.isAutoUpdate();
    }

    public void setAutoUpdate(boolean autoUpdate) {
        IDisplayPane[] dispPanes = getDisplayPanes();
        for (IDisplayPane pane : dispPanes) {
            ((INatlCntrsDescriptor) pane.getDescriptor())
                    .setAutoUpdate(autoUpdate);
        }
    }

    public void setGeoSyncPanesEnabled(boolean s) {
        geoSyncPanesEnabled = s;
    }

    public boolean arePanesGeoSynced() {
        return geoSyncPanesEnabled;
    }

    public boolean arePanesTimeSynced() {
        return timeSyncPanesEnabled;
    }

    public boolean areContextualMenusEnabled() {
        return contextualMenusEnabled;
    }

    public void setContextualMenusEnabled(boolean val) {
        this.contextualMenusEnabled = val;
    }
}
