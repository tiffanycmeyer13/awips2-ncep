/*
 * PgenSession
 *
 * Date created: 14 APRIL 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
package gov.noaa.nws.ncep.ui.pgen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IRenderableDisplayChangedListener;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.maps.display.VizMapEditor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.editor.IMultiPaneEditor;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;
import com.raytheon.viz.ui.views.DetachPart;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil.PgenMode;
import gov.noaa.nws.ncep.ui.pgen.controls.PgenCommandManager;
import gov.noaa.nws.ncep.ui.pgen.filter.CategoryFilter;
import gov.noaa.nws.ncep.ui.pgen.palette.PgenPaletteWindow;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceData;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceList;
import gov.noaa.nws.ncep.ui.pgen.tools.AbstractPgenTool;

/**
 * This singleton is intended to couple a PGEN Palette with a PGgenResource, so
 * that a palette can be updated and used to modify a specific PgenResource
 *
 * @author sgilbert
 *
 */

/**
 * Implements a drawing layer for PGEN products.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer       Description
 * ------------- -------- -------------- ---------------------------------------
 * 12/14         5413     B. Yin         Added IPartListener2 and
 *                                       IRenderableDisplayChangedListener to
 *                                       make the swapping in D2D work
 * 12/14         5413     B. Yin         Added exception handling, perspective
 *                                       id, and endSession.
 * 01/15         5413     B. Yin         Added closePalette method.
 * May 16, 2016  18388    J. Wu          Use PgenConstant.
 * Nov 30, 2016  17954    K. Bugenhagen  Added numberOfSessionResources
 *                                       attribute to keep track of total number
 *                                       of pgen resources for entire session.
 * Dec 01, 2021  95362    tjensen        Refactor PGEN Resource management to
 *                                       support multi-panel displays
 * Jan 25, 2022  100402   smanoj         Check against null pointer.
 * 
 * </pre>
 *
 */

@SuppressWarnings("restriction")
public class PgenSession
        implements IPartListener2, IRenderableDisplayChangedListener {

    /*
     * The singleton instance
     */
    private static PgenSession instance = null;

    /*
     * the current PGEN resource
     */
    private PgenResourceList pgenResources;

    /*
     * the current PGEN palette
     */
    private PgenPaletteWindow palette = null;

    private final List<AbstractEditor> editors = new ArrayList<>();

    /*
     * The total number of resources for this session
     */
    private int numberOfSessionResources = 0;

    /*
     * Active PGEN tool
     */
    private AbstractPgenTool pgenTool = null;

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PgenSession.class);

    private String perspectiveId = "";

    /*
     * Hide default constructor
     */
    private PgenSession() {
        AbstractVizPerspectiveManager pMngr = VizPerspectiveListener
                .getCurrentPerspectiveManager();
        if (pMngr != null) {
            setPerspectiveId(pMngr.getPerspectiveId());
        }
        pgenResources = null;
    }

    /**
     * Static method to get THE PgenSession instance
     *
     * @return PgenSession reference
     */
    public static synchronized PgenSession getInstance() {

        if (instance == null) {
            instance = new PgenSession();
        }
        return instance;
    }

    /**
     * Sets a PgenResource for the current session
     *
     * @param rsc
     *            a Pgen Resource
     */
    public void setResource(PgenResourceList rsc) {

        /*
         * Remove the current PGEN Resource from the Session
         */
        removeResource();

        // set new PGEN resource
        pgenResources = rsc;
        // add the palette's stack listener to new resource's command Manager
        if (pgenResources != null && palette != null) {
            pgenResources.getCommandMgr().addStackListener(palette);
        }

    }

    /**
     * Removes the current PGEN resource from the Session
     */
    public void removeResource() {
        if (pgenResources != null) {
            // Remove the Palette's stack listener from the Resource's
            // CommandManager
            pgenResources.getCommandMgr().removeStackListener(palette);
        }
        pgenResources = null;

        /*
         * disable the palette's Undo and redo buttons.
         */
        if (palette != null) {
            palette.disableUndoRedo();
        }
    }

    /**
     * Gets an appropriate PGEN Resource. Returns the current Pgen Resource
     * registered with this PGEN Session if there is one. If not, it will look
     * for an existing resource in the current editor. If one is not found, a
     * new PgenResource will be created.
     *
     * @return the rsc
     */
    public PgenResourceList getPgenResources() {

        if (pgenResources == null || pgenResources.isEmpty()) {
            PgenResourceList rscs = PgenUtil
                    .findPgenResources(PgenUtil.getActiveEditor());
            if (rscs != null) {
                pgenResources = rscs;
            } else {
                pgenResources = new PgenResourceList();
                pgenResources.add(PgenUtil.createNewResource());
            }
        }

        return pgenResources;
    }

    /**
     * Get the PGEN Resource currently registered with the session
     *
     * @return
     */
    public PgenResource getCurrentResource() {
        PgenResource rsc = (PgenResource) PgenUtil
                .findResource(PgenResource.class, PgenUtil.getActiveEditor());
        if (rsc == null) {
            rsc = PgenUtil.createNewResource();
            return rsc;
        }
        return rsc;
    }

    /**
     * Old call to get the only resource. Should now call getCurrentResource()
     * directly if a single resource is needed.
     * 
     * @return
     */
    @Deprecated
    public PgenResource getPgenResource() {
        return getCurrentResource();
    }

    /**
     * Gets the Resource's Command Manager
     *
     * @return the commandMgr
     */
    public PgenCommandManager getCommandManager() {
        return getPgenResources().getCommandMgr();
    }

    /**
     * Register the given palette with the Session
     *
     * @param pal
     */
    public void setPalette(PgenPaletteWindow pal) {
        palette = pal;
        // Register this palette's stack listener with the CommandManager, if
        // able
        PgenCommandManager cmdMgr = getCommandManager();
        if (cmdMgr != null) {
            cmdMgr.addStackListener(palette);
        }
    }

    /**
     * Remove the current palette from this Session
     */
    public void removePalette() {
        // Remove this palette's stack listener from the CommandManager, if able
        PgenCommandManager cmdMgr = getCommandManager();
        if (cmdMgr != null) {
            cmdMgr.removeStackListener(palette);
        }
        palette = null;
    }

    /**
     * Clear and disable undo/redos.
     */
    public void disableUndoRedo() {

        PgenCommandManager cmdMgr = getCommandManager();
        if (cmdMgr != null) {
            cmdMgr.clearStacks();
        }

        if (palette != null) {
            palette.disableUndoRedo();
        }

    }

    public PgenResourceData getPgenResourceData() {
        if (pgenResources != null) {
            return pgenResources.getResourceData();
        } else {
            return null;
        }
    }

    /**
     * Return the palette window
     */
    public PgenPaletteWindow getPgenPalette() {
        return palette;
    }

    public void addEditor(AbstractEditor editor) {
        editors.add(editor);
    }

    public List<AbstractEditor> getEditors() {
        return editors;
    }

    public void bumpNumberOfSessionResources() {
        numberOfSessionResources++;
    }

    /*
     * Remove PGEN handler when swapping to side view. Also open PGEN palette if
     * there is a PGEN resource when swapping to main editor.
     */
    @Override
    public void renderableDisplayChanged(IDisplayPane pane,
            IRenderableDisplay newRenderableDisplay, DisplayChangeType type) {

        if (type == DisplayChangeType.ADD && newRenderableDisplay
                .getContainer() instanceof VizMapEditor) {

            VizMapEditor editorChanged = (VizMapEditor) newRenderableDisplay
                    .getContainer();

            if (PgenUtil.getPgenMode() == PgenMode.SINGLE) {
                // for D2d swapping, single pane mode
                if (pgenResources != null) {
                    pgenResources.removeGhostLine();
                    pgenResources.removeSelected();

                    // Make sure PGEN resource repaint in the new editor.
                    PgenResourceList rsc = PgenUtil
                            .findPgenResources(editorChanged);
                    if (rsc != null) {
                        rsc.resetAllElements();
                    }
                }

                PgenUtil.setSelectingMode();
            }

            else { // for D2d swapping, multi-pane mode

                // clean up current pgen resource
                if (pgenResources != null) {
                    pgenResources.closeDialogs();
                    pgenResources.deactivatePgenTools();
                    pgenResources.getCommandMgr().removeStackListener(palette);
                }

                if (palette != null) {
                    if (PgenUtil.findPgenResources(editorChanged) == null) {
                        // editor does not have PGEN, close the palette

                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage().hideView(palette);
                        palette = null;
                    } else {
                        // editor has PGEN resource, reset to selecting mode
                        pgenResources = PgenUtil
                                .findPgenResources(editorChanged);
                        pgenResources.setCatFilter(new CategoryFilter());
                        palette.setCurrentCategory(PgenConstant.CATEGORY_ANY);
                        PgenUtil.setSelectingMode();
                    }
                } else {
                    // palette is closed
                    if (PgenUtil.findPgenResources(editorChanged) != null) {
                        // editor has PGEN, open the palette
                        IWorkbenchPage wpage = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getActivePage();

                        IViewPart vpart = wpage.findView(PgenUtil.VIEW_ID);

                        try {

                            if (vpart == null) {

                                vpart = wpage.showView(PgenUtil.VIEW_ID);
                                IViewReference pgenViewRef = wpage
                                        .findViewReference(PgenUtil.VIEW_ID);
                                if (pgenViewRef != null
                                        && wpage instanceof WorkbenchPage) {
                                    DetachPart
                                            .detach(pgenViewRef.getPart(true));
                                }
                            } else {

                                if (!wpage.isPartVisible(vpart)) {
                                    vpart = wpage.showView(PgenUtil.VIEW_ID);
                                    IViewReference pgenViewRef = wpage
                                            .findViewReference(
                                                    PgenUtil.VIEW_ID);
                                    if (pgenViewRef != null
                                            && wpage instanceof WorkbenchPage) {
                                        DetachPart.detach(
                                                pgenViewRef.getPart(true));
                                    }
                                }
                            }
                            this.pgenResources = PgenUtil
                                    .findPgenResources(editorChanged);
                            this.pgenResources
                                    .setCatFilter(new CategoryFilter());
                            this.palette.setCurrentCategory(
                                    PgenConstant.CATEGORY_ANY);
                            PgenUtil.setSelectingMode();
                        } catch (Exception e) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Cannot open PGEN palette view", e);
                        }

                    }
                }
            }
        } else if (type == DisplayChangeType.REMOVE && !(newRenderableDisplay
                .getContainer() instanceof AbstractEditor)) {
            // remove to side view
            // unregister pgen handlers

            if (newRenderableDisplay
                    .getContainer() instanceof IMultiPaneEditor) {
                IMultiPaneEditor sideView = (IMultiPaneEditor) newRenderableDisplay
                        .getContainer();
                if (this.getPgenTool() != null) {
                    sideView.unregisterMouseHandler(
                            this.getPgenTool().getMouseHandler());
                }

                // Make sure PGEN resource repaint in the new editor.
                if (PgenUtil.getPgenMode() == PgenMode.SINGLE) {
                    ResourceList rscList = sideView.getActiveDisplayPane()
                            .getDescriptor().getResourceList();

                    for (ResourcePair rp : rscList) {
                        AbstractVizResource<?, ?> rsc = rp.getResource();
                        if (rsc instanceof PgenResource) {
                            ((PgenResource) rsc).resetAllElements();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof VizMapEditor) { // for D2D
            if (PgenUtil.findPgenResources((VizMapEditor) part) != null) {
                ((VizMapEditor) part)
                        .removeRenderableDisplayChangedListener(this);
            }
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // TODO Auto-generated method stub

    }

    public AbstractPgenTool getPgenTool() {
        return pgenTool;
    }

    public void setPgenTool(AbstractPgenTool pgenTool) {
        this.pgenTool = pgenTool;
    }

    public String getPerspectiveId() {
        return perspectiveId;
    }

    public void setPerspectiveId(String perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    public void endSession() {
        instance = null;
    }

    public void closePalette() {
        if (palette != null) {
            if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage() != null) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().hideView(palette);
            }
        }
    }

    public int getNumberOfSessionResources() {
        return numberOfSessionResources;
    }

    public void setNumberOfSessionResources(int numberOfResources) {
        numberOfSessionResources = numberOfResources;
    }

    /**
     * @param pgenResource
     */
    public void addResource(PgenResource pgenResource) {
        pgenResources.add(pgenResource);
    }

}
