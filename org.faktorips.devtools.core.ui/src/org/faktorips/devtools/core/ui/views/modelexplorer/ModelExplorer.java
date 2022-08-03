/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.contexts.IContextService;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.TreeViewerRefreshAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.AbstractShowInSupportingViewPart;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.util.ArgumentCheck;

/**
 * The <code>ModelExplorer</code> is a <code>ViewPart</code> for displaying <code>IIpsObject</code>s
 * along with their attributes.
 * <p>
 * The view uses a <code>TreeViewer</code> to represent the hierarchical data structure. It can be
 * configured to show the tree of package fragments in a hierarchical (default) or a flat layout
 * style.
 * 
 * @author Stefan Widmaier
 */

public class ModelExplorer extends AbstractShowInSupportingViewPart {

    /** Extension id of this viewer extension. */
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modelExplorer"; //$NON-NLS-1$

    /** The filter group in the context menu of the model explorer. */
    protected static final String MENU_FILTER_GROUP = "group.filter"; //$NON-NLS-1$

    /** Used for saving the current filter into an eclipse <code>Memento</code>. */
    protected static final String FILTER_KEY = "filter"; //$NON-NLS-1$

    /** Used for saving the state of group by into an eclipse <code>Memento</code>. */
    protected static final String GROUP_BY_KEY = "groupby"; //$NON-NLS-1$

    protected static final String LINK_TO_EDITOR_KEY = "linktoeditor"; //$NON-NLS-1$

    private static final String MEMENTO = "modelExplorer.memento"; //$NON-NLS-1$

    private static final String LAYOUT_STYLE_KEY = "style"; //$NON-NLS-1$

    /** The tree viewer displaying the object model. */
    private TreeViewer treeViewer;

    /** Label provider for the tree viewer. */
    private ModelLabelProvider labelProvider;

    /** The model explorer configuration containing the allowed types. */
    private final ModelExplorerConfiguration config;

    /**
     * LayoutStyle flat or hierarchical.
     */
    private LayoutStyle layoutStyle = LayoutStyle.HIERACHICAL;

    /** Flag that indicates whether linking is enabled. */
    private boolean linkingEnabled;

    /** Listener for activation of editors. */
    private ActivationListener editorActivationListener;

    /** Content provider for the tree viewer. */
    private ModelContentProvider contentProvider;

    private IpsResourceChangeListener resourceListener;

    /** Flag that indicates if non ips projects will be excluded or not. */
    private boolean excludeNoIpsProjects = true;

    /**
     * Decorator for problems in ips objects. This decorator is adjusted according to the current
     * layout style.
     */
    // TODO where to get the registered one?
    // private IpsProblemsLabelDecorator ipsDecorator;

    private ToggleLinkingAction toggleLinking;

    private boolean supportCategories = false;

    private ModelExplorerSorter sorter;

    /** Creates a new <code>ModelExplorer</code>. */
    public ModelExplorer() {
        super();
        config = createConfig();
        contentProvider = createContentProvider();
    }

    /**
     * Creates and returns a <code>ModelExplorerConfiguration</code> with the all ips object types
     * defined in the IpsModel.
     * 
     * @see IIpsModel#getIpsObjectTypes()
     * @see ModelExplorerConfiguration
     */
    protected ModelExplorerConfiguration createConfig() {
        IIpsModel ipsModel = IIpsModel.get();
        IpsObjectType[] objectTypes = ipsModel.getIpsObjectTypes();
        return new ModelExplorerConfiguration(objectTypes);
    }

    /**
     * Creates the <code>ModelContentProvider</code> that is used by the model explorer to show
     * contents.
     */
    protected ModelContentProvider createContentProvider() {
        return new ModelContentProvider(getConfig(), layoutStyle);
    }

    @Override
    public void createPartControl(Composite parent) {
        // Init saved state
        contentProvider.setExcludeNoIpsProjects(excludeNoIpsProjects);

        labelProvider = new ModelLabelProvider();
        treeViewer = new TreeViewer(parent);
        getTreeViewer().setContentProvider(contentProvider);
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingLabelProvider decoProvider = new DecoratingLabelProvider(getLabelProvider(),
                decoManager.getLabelDecorator());
        getTreeViewer().setLabelProvider(decoProvider);
        sorter = new ModelExplorerSorter(isSupportCategories());
        getTreeViewer().setComparator(sorter);
        getTreeViewer().setInput(IIpsModel.get());

        getTreeViewer().addDoubleClickListener(new ModelExplorerDoubleclickListener(getTreeViewer()));
        getTreeViewer().addDragSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() },
                new IpsElementDragListener(getTreeViewer()));
        getTreeViewer().addDropSupport(DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() },
                new ModelExplorerDropListener());
        createFilters(getTreeViewer());

        getSite().setSelectionProvider(getTreeViewer());

        resourceListener = new IpsResourceChangeListener(getTreeViewer());
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_BUILD);

        /*
         * Use the current value of layoutStyle, which is set by loading the memento/viewState
         * before this method is called
         */
        setLayoutStyle(layoutStyle);

        // Create 'link with editor' action
        toggleLinking = new ToggleLinkingAction(this);

        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        createMenu(menuManager);
        createAdditionalMenuEntries(menuManager);
        createContextMenu();
        createToolBar();

        if (isLinkingEnabled()) {
            IEditorPart editorPart = getSite().getPage().getActiveEditor();
            if (editorPart != null) {
                editorActivated(editorPart);
            }
        }

        editorActivationListener = new ActivationListener(getSite().getPage());
        activateContext();
    }

    /**
     * Show an ips file or a normal file in the navigator view corresponding to the active editor if
     * "link with editor" is enabled.
     * 
     * @param editorPart The editor that has been activated.
     */
    private void editorActivated(IEditorPart editorPart) {
        if (!isLinkingEnabled() || editorPart == null) {
            return;
        }

        if (editorPart instanceof IpsObjectEditor) {
            IpsObjectEditor ipsEditor = (IpsObjectEditor)editorPart;
            setSelectionInTree(ipsEditor.getIpsSrcFile());
        } else if (editorPart.getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)editorPart.getEditorInput()).getFile();
            IIpsElement ipsElement = IIpsModel.get().getIpsElement(Wrappers.wrap(file).as(AFile.class));
            if (ipsElement == null || !ipsElement.exists()) {
                setSelectionInTree(file);
            } else {
                setSelectionInTree(ipsElement);
            }
        }
    }

    private void setSelectionInTree(Object objectToSelect) {
        IStructuredSelection newSelection = new StructuredSelection(objectToSelect);
        if (getTreeViewer().getSelection().equals(newSelection)) {
            getTreeViewer().getTree().showSelection();
        } else {
            getTreeViewer().setSelection(newSelection, true);
        }
    }

    /**
     * This operation is empty by default. Subclasses may overwrite to create filters for filtering
     * out specific content from the model explorer.
     * 
     * @param tree The tree viewer of the model explorer.
     */
    protected void createFilters(TreeViewer tree) {
        // Empty default implementation
    }

    /** Create menu for layout styles */
    private void createMenu(IMenuManager menuManager) {
        IAction flatLayoutAction = new LayoutAction(this, true);
        flatLayoutAction.setText(Messages.ModelExplorer_actionFlatLayout);
        flatLayoutAction.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "ModelExplorerFlatLayout.gif")); //$NON-NLS-1$
        IAction hierarchicalLayoutAction = new LayoutAction(this, false);
        hierarchicalLayoutAction.setText(Messages.ModelExplorer_actionHierarchicalLayout);
        hierarchicalLayoutAction.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "ModelExplorerHierarchicalLayout.gif")); //$NON-NLS-1$

        // Actions are unchecked as per default, check action for current layout
        if (isFlatLayout()) {
            flatLayoutAction.setChecked(true);
        } else {
            hierarchicalLayoutAction.setChecked(true);
        }

        IMenuManager layoutMenu = new MenuManager(Messages.ModelExplorer_submenuLayout);
        layoutMenu.add(flatLayoutAction);
        layoutMenu.add(hierarchicalLayoutAction);
        menuManager.add(layoutMenu);
    }

    /**
     * Create additional menu entries e.g. filters.
     */
    protected void createAdditionalMenuEntries(IMenuManager menuManager) {
        menuManager.add(new Separator(MENU_FILTER_GROUP));

        Action groupForCategories = createGroupCategoriesAction();
        groupForCategories.setChecked(isSupportCategories());
        menuManager.add(groupForCategories);

        addProjectFilterAction(menuManager);

        menuManager.add(toggleLinking);

    }

    protected void addProjectFilterAction(IMenuManager menuManager) {
        Action showNoIpsProjectsAction = createShowNoIpsProjectsAction();
        showNoIpsProjectsAction.setChecked(excludeNoIpsProjects);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showNoIpsProjectsAction);
    }

    /** Creates the context menu for the model explorer. */
    protected void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new ModelExplorerContextMenuBuilder(this, getConfig(), getViewSite(), getSite(),
                getTreeViewer()));
        Menu contextMenu = manager.createContextMenu(getTreeViewer().getControl());
        getTreeViewer().getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, getTreeViewer());
    }

    private void createToolBar() {
        Action refreshAction = new TreeViewerRefreshAction(getSite());
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(retargetAction);
        getViewSite().getActionBars().getToolBarManager().add(new CollapseAllAction(getTreeViewer()));
        getViewSite().getActionBars().getToolBarManager().add(toggleLinking);

    }

    @Override
    public void setFocus() {
        if (getTreeViewer() == null || getTreeViewer().getControl() == null
                || getTreeViewer().getControl().isDisposed()) {
            return;
        }
        getTreeViewer().getControl().setFocus();
    }

    /** Answers whether this part shows the packagFragments flat or hierarchical */
    private boolean isFlatLayout() {
        return layoutStyle == LayoutStyle.FLAT;
    }

    protected LayoutStyle getLayoutStyle() {
        return layoutStyle;
    }

    /**
     * Sets the new layout style. Informs label and contentProvider, activates emptyPackageFilter
     * for flat layout to hide empty PackageFragments.
     */
    private void setLayoutStyle(LayoutStyle newStyle) {
        ArgumentCheck.notNull(newStyle);
        layoutStyle = newStyle;
        // TODO see declaration
        // ipsDecorator.setFlatLayout(isFlatLayout());
        contentProvider.setLayoutStyle(newStyle);
        getLabelProvider().setIsFlatLayout(isFlatLayout());

        getTreeViewer().getControl().setRedraw(false);
        getTreeViewer().refresh();
        getTreeViewer().getControl().setRedraw(true);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Loads the layout style from the given Memento Object.
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento layout = memento.getChild(MEMENTO);
            if (layout != null) {
                Integer layoutValue = layout.getInteger(LAYOUT_STYLE_KEY);
                Integer filterValue = layout.getInteger(FILTER_KEY);
                Integer groupByValue = layout.getInteger(GROUP_BY_KEY);
                Integer linkingValue = layout.getInteger(LINK_TO_EDITOR_KEY);
                layoutStyle = layoutValue == null ? LayoutStyle.HIERACHICAL : LayoutStyle.getById(layoutValue);
                excludeNoIpsProjects = filterValue == null ? false : filterValue.intValue() == 1;
                setSupportCategories(groupByValue == null ? isSupportCategories() : groupByValue.intValue() == 1);
                linkingEnabled = linkingValue == null ? false : linkingValue.intValue() == 1;
            }
        }
    }

    /** Returns whether the editor is linked to the navigator. */
    public boolean isLinkingEnabled() {
        return linkingEnabled;
    }

    /**
     * Save the state and link editor with navigator if linkingEnabled is <code>true</code>.
     * 
     * @param linkingEnabled Flag indicating whether linking should be enabled.
     */
    public void setLinkingEnabled(boolean linkingEnabled) {
        this.linkingEnabled = linkingEnabled;

        if (linkingEnabled) {
            IEditorPart editorPart = getSite().getPage().getActiveEditor();
            if (editorPart != null) {
                editorActivated(editorPart);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Saves the current layout style into the given memento object.
     */
    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(MEMENTO);
        layout.putInteger(LAYOUT_STYLE_KEY, layoutStyle.getId());
        layout.putInteger(FILTER_KEY, excludeNoIpsProjects ? 1 : 0);
        layout.putInteger(GROUP_BY_KEY, isSupportCategories() ? 1 : 0);
        layout.putInteger(LINK_TO_EDITOR_KEY, linkingEnabled ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Unregisters this part as resource-listener in the workspace and disposes of it.
     */
    @Override
    public void dispose() {
        editorActivationListener.dispose();

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);

        super.dispose();
    }

    //
    // @Override
    // public boolean show(ShowInContext context) {
    // ISelection selection = context.getSelection();
    // if (selection instanceof IStructuredSelection) {
    // IStructuredSelection structuredSelection = ((IStructuredSelection)selection);
    // if (structuredSelection.size() >= 1) {
    // return reveal(structuredSelection.getFirstElement());
    // }
    // }
    //
    // Object input = context.getInput();
    // if (input instanceof IProductCmpt) {
    // return reveal(context.getInput());
    // } else if (input instanceof IFileEditorInput) {
    // IFile file = ((IFileEditorInput)input).getFile();
    // return reveal(file);
    // }
    //
    // return false;
    // }

    @Override
    protected boolean show(IAdaptable adaptable) {
        IIpsElement ipsElement = adaptable.getAdapter(IIpsElement.class);
        if (ipsElement != null) {
            if (ipsElement instanceof IIpsObject) {
                // If the object is an IpsElement we have to get the ipsSrcFile because only the
                // SrcFile is in the content, not the ipsObject
                ipsElement = ((IIpsObject)ipsElement).getIpsSrcFile();
            }
            selectAndReveal(ipsElement);
            return true;
        }
        IIpsSrcFile ipsSrcFile = adaptable.getAdapter(IIpsSrcFile.class);
        if (ipsSrcFile != null) {
            selectAndReveal(ipsSrcFile);
            return true;
        }
        IResource resource = adaptable.getAdapter(IResource.class);
        if (resource != null) {
            ipsElement = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
            if (ipsElement != null) {
                selectAndReveal(ipsElement);
                return true;
            }
            selectAndReveal(resource);
        }
        return false;
    }

    private void selectAndReveal(Object aObject) {
        // The reveal does not always works with the setSelection(..., true) (Eclipse 3.4)
        getTreeViewer().reveal(aObject);
        StructuredSelection selection = new StructuredSelection(aObject);
        getTreeViewer().setSelection(selection, true);
        if (!getTreeViewer().getSelection().equals(selection)) {
            // If the IpsObject is not expanded yet, the parts are not loaded in the model explorer.
            // We have to expand the IpsObject (indeed it is the IpsSrcFile) and then select the
            // part again.
            if (aObject instanceof IIpsObjectPart) {
                IIpsObjectPart ipsObjectPart = (IIpsObjectPart)aObject;
                getTreeViewer().expandToLevel(ipsObjectPart.getIpsSrcFile(), 1);
                getTreeViewer().setSelection(selection, true);
            }
        }
    }

    @Override
    protected ISelection getSelection() {
        return getTreeViewer().getSelection();
    }

    /** Returns the content provider. */
    protected ModelContentProvider getContentProvider() {
        return contentProvider;
    }

    /**
     * Returns whether this class is a kind of model explorer.
     * <p>
     * A model explorer is an explorer with enhanced functionality. Other derived explorer classes
     * should return <code>false</code> if restriced menu operations should be provided.
     */
    protected boolean isModelExplorer() {
        return true;
    }

    private Action createShowNoIpsProjectsAction() {
        return new Action(Messages.ModelExplorer_menuShowIpsProjectsOnly_Title, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                excludeNoIpsProjects = !excludeNoIpsProjects;
                contentProvider.setExcludeNoIpsProjects(excludeNoIpsProjects);
                getTreeViewer().refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ModelExplorer_menuShowIpsProjectsOnly_Tooltip;
            }
        };
    }

    protected Action createGroupCategoriesAction() {
        return new Action(Messages.ModelExplorer_menuGroupCategories_Title, IAction.AS_CHECK_BOX) {
            @Override
            public String getToolTipText() {
                return Messages.ModelExplorer_menuGroupCategories_Tooltip;
            }

            @Override
            public void run() {
                setSupportCategories(!isSupportCategories());
                sorter.setSupportCategories(isSupportCategories());
                getTreeViewer().refresh();
            }
        };
    }

    /**
     * 
     * Activate a context that this view uses. It will be tied to this * view activation events and
     * will be removed when the view is
     * 
     * disposed.
     */

    private void activateContext() {
        IContextService service = getSite().getService(IContextService.class);
        service.activateContext("org.faktorips.devtools.core.ui.views.modelExplorer.context"); //$NON-NLS-1$
    }

    protected TreeViewer getTreeViewer() {
        return treeViewer;
    }

    protected ModelLabelProvider getLabelProvider() {
        return labelProvider;
    }

    protected ModelExplorerConfiguration getConfig() {
        return config;
    }

    protected boolean isSupportCategories() {
        return supportCategories;
    }

    protected void setSupportCategories(boolean supportCategories) {
        this.supportCategories = supportCategories;
    }

    /** Internal part and shell activation listener. */
    private class ActivationListener implements IPartListener, IWindowListener {

        private IPartService partService;

        /**
         * Creates a new activation listener.
         */
        public ActivationListener(IPartService partService) {
            this.partService = partService;
            partService.addPartListener(this);
            PlatformUI.getWorkbench().addWindowListener(this);
        }

        /**
         * Disposes this activation listener.
         */
        public void dispose() {
            partService.removePartListener(this);
            PlatformUI.getWorkbench().removeWindowListener(this);
            partService = null;
        }

        @Override
        public void partActivated(IWorkbenchPart part) {
            if (wasActivatedByModelExplorer()) {
                return;
            }
            if (part instanceof IEditorPart) {
                editorActivated((IEditorPart)part);
            }
        }

        @Override
        public void windowActivated(IWorkbenchWindow window) {
            if (wasActivatedByModelExplorer()) {
                return;
            }
            editorActivated(window.getActivePage().getActiveEditor());
        }

        /**
         * Returns <code>true</code> if the activation was triggered by the model explorer.
         * Additionally resets the activation flag.
         */
        private boolean wasActivatedByModelExplorer() {
            return false;
            // if (activatedByModelExplorer) {
            // activatedByModelExplorer = false;
            // return true;
            // }
            // return false;
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partOpened(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowOpened(IWorkbenchWindow window) {
            // Nothing to do
        }

    }

    /**
     * DoubleClickListener which informs the model explorer about double clicking inside the tree.
     * <p>
     * Used to avoid the handling of editor activation (linking) if the editor was activated by the
     * model explorer.
     * <p>
     * Avoid the following scenario: If a child is double clicked then the editor will be opened
     * (activated) afterwards the activation will be catched by this model explorer and the model
     * explorer selects the corresponding parent object (linking), the selection of the child will
     * be lost!
     */
    private class ModelExplorerDoubleclickListener extends TreeViewerDoubleclickListener {

        /**
         * Creates a new model explorer double click listener.
         */
        public ModelExplorerDoubleclickListener(TreeViewer tree) {
            super(tree);
        }

        @Override
        public void doubleClick(DoubleClickEvent event) {
            openEditorsForSelection();
            collapsOrExpandTree(event);
        }

    }

    private static class LayoutAction extends Action {

        private boolean isFlatLayout;

        private ModelExplorer modelExplorer;

        public LayoutAction(ModelExplorer modelExplorer, boolean flat) {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$

            isFlatLayout = flat;
            this.modelExplorer = modelExplorer;
        }

        @Override
        public void run() {
            if (modelExplorer.isFlatLayout() != isFlatLayout) {
                modelExplorer.setLayoutStyle(isFlatLayout ? LayoutStyle.FLAT : LayoutStyle.HIERACHICAL);
            }
        }

    }

}
