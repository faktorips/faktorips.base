
package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.actions.OpenProjectAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.actions.CreateIpsArchiveAction;
import org.faktorips.devtools.core.ui.actions.ExpandCollapseAllAction;
import org.faktorips.devtools.core.ui.actions.FindPolicyReferencesAction;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.FixDifferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsEditSortOrderAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.IpsPropertiesAction;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.actions.IpsTestCaseCopyAction;
import org.faktorips.devtools.core.ui.actions.MigrateProjectAction;
import org.faktorips.devtools.core.ui.actions.ModelExplorerDeleteAction;
import org.faktorips.devtools.core.ui.actions.MoveAction;
import org.faktorips.devtools.core.ui.actions.NewFileResourceAction;
import org.faktorips.devtools.core.ui.actions.NewFolderAction;
import org.faktorips.devtools.core.ui.actions.NewIpsPacketAction;
import org.faktorips.devtools.core.ui.actions.NewPolicyComponentTypeAction;
import org.faktorips.devtools.core.ui.actions.NewProductComponentAction;
import org.faktorips.devtools.core.ui.actions.NewTableContentAction;
import org.faktorips.devtools.core.ui.actions.NewTableStructureAction;
import org.faktorips.devtools.core.ui.actions.NewTestCaseAction;
import org.faktorips.devtools.core.ui.actions.NewTestCaseTypeAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.RenameAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.actions.TreeViewerRefreshAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * The ModelExplorer is a ViewPart for displaying <code>ProductComponent</code>s,
 * <code>TableContents</code>, <code>TableStructure</code>s and <code>PolicyCmptType</code>s
 * along with their Attributes. The view uses a TreeViewer to represent the hierarchical
 * datastructure. It can be configured to show the tree of PackageFragments in a hierarchical
 * (default) or a flat layout style.
 *
 * @author Stefan Widmaier
 */

public class ModelExplorer extends ViewPart implements IShowInTarget {

    /**
     * Extension id of this views extension.
     */
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modelExplorer"; //$NON-NLS-1$

    private static final int HIERARCHICAL_LAYOUT = 0;
    private static final int FLAT_LAYOUT = 1;

    protected static String MENU_FILTER_GROUP = "goup.filter"; //$NON-NLS-1$

    /**
     * Used for saving the current layout style and filter in a eclipse memento.
     */
    private static final String MEMENTO = "modelExplorer.memento"; //$NON-NLS-1$

    /**
     * Used for saving the current layout and filter style in a eclipse memento.
     */
    private static final String LAYOUT_STYLE_KEY = "style"; //$NON-NLS-1$
    protected static final String FILTER_KEY = "filter"; //$NON-NLS-1$
    protected static final String LINK_TO_EDITOR_KEY = "linktoeditor"; //$NON-NLS-1$

    /*
     * Listener for activation of editors.
     */
    private ActivationListener editorActivationListener;
    
    /**
     * The TreeViewer displaying the object model.
     */
    protected TreeViewer treeViewer;

    /**
     * Decorator for problems in IpsObjects. This decorator is adjusted according to the current
     * layout style.
     */
    private IpsProblemsLabelDecorator ipsDecorator = new IpsProblemsLabelDecorator();

    /**
     * Content provider for the tree viewer.
     */
    private ModelContentProvider contentProvider;

    /**
     * Label provider for the tree viewer.
     */
    protected ModelLabelProvider labelProvider;

    private IpsResourceChangeListener resourceListener;

    protected ModelExplorerConfiguration config;
    /**
     * Flag that indicates whether the current layout style is flat (true) or hierarchical (false).
     */
    protected boolean isFlatLayout = false;

    /** Flag that indicates if non ips projects will be excluded or not */
    private boolean excludeNoIpsProjects = false;

    private ToggleLinkingAction toggleLinking;

    protected boolean linkingEnabled;

    public ModelExplorer() {
        super();
        config = createConfig();
        contentProvider = createContentProvider();
    }

    /*
     * Internal part and shell activation listener.
     */
    private class ActivationListener implements IPartListener, IWindowListener {
        private IPartService partService;
        
        /* indicates if the last activation event was triggered by the model explorer */
        private boolean activatedByModelExplorer;
        
        /**
         * Creates this activation listener.
         */
        public ActivationListener(IPartService partService) {
            this.partService = partService;
            partService.addPartListener(this);
            PlatformUI.getWorkbench().addWindowListener(this);
        }

        /**
         * Sets the "activation by model explorer" flag.
         * Set <code>true</code> if the next activation event was initiated by the model explorer.
         * 
         */
        public void setActivatedByModelExplorer(boolean activatedByModelExplorer) {
            this.activatedByModelExplorer = activatedByModelExplorer;
        }

        /**
         * Disposes this activation listener.
         */
        public void dispose() {
            partService.removePartListener(this);
            PlatformUI.getWorkbench().removeWindowListener(this);
            partService= null;
        }

        public void partActivated(IWorkbenchPart part) {
            if (wasActivatedByModelExplorer()){
                return;
            }
            if (part instanceof IEditorPart){
                editorActivated((IEditorPart) part);
            }
        }

        public void windowActivated(IWorkbenchWindow window) {
            if (wasActivatedByModelExplorer()){
                return;
            }
            editorActivated(window.getActivePage().getActiveEditor());
        }

        /*
         * Returns <code>true</code> if the activation was triggered by the model explorer.
         * Additionally resets the activation flag.
         */
        private boolean wasActivatedByModelExplorer() {
            if (activatedByModelExplorer){
                activatedByModelExplorer = false;
                return true;
            }
            return false;
        }
        
        public void partBroughtToTop(IWorkbenchPart part) {
        }
        public void partClosed(IWorkbenchPart part) {
        }
        public void partDeactivated(IWorkbenchPart part) {
        }
        public void partOpened(IWorkbenchPart part) {
        }
        public void windowDeactivated(IWorkbenchWindow window) {
        }
        public void windowClosed(IWorkbenchWindow window) {
        }
        public void windowOpened(IWorkbenchWindow window) {
        }
    }
    
    /*
     * DoubleClickListener which infoms the model explorer about double clicking inside the tree.
     * Used to avoid the handling of editor activation (linking) if the editor was activated
     * by the model explorer.<p>
     * Avoid the following scenario: if a child is double clicked then the editor will be opened (activated)
     * afterwards the activation will by catched by this model explorer and the model explorer selects the corresponding
     * parent object (linking), the selection of the child will be lost!
     */
    private class ModelExplorerDoubleclickListener extends TreeViewerDoubleclickListener {
        
        public ModelExplorerDoubleclickListener(TreeViewer tree) {
            super(tree);
        }

        public void doubleClick(DoubleClickEvent event) {
            ModelExplorer.this.editorActivationListener.setActivatedByModelExplorer(true);
            IEditorPart editorPart = openEditorsForSelection();
            if (editorPart == null){
                // editor wasn't opened, therfore the activation flag must be reseted
                ModelExplorer.this.editorActivationListener.setActivatedByModelExplorer(false);
            }
            collapsOrExpandTree(event);
        }
    }    
    
    protected ModelExplorerConfiguration createConfig() {
        return new ModelExplorerConfiguration(new Class[] {
                    IPolicyCmptType.class, 
                    IProductCmptType.class,
                    IProductCmpt.class, 
                    IProductCmptGeneration.class,
                    ITableStructure.class,
                    ITableContents.class,
                    IPolicyCmptTypeAttribute.class, 
                    IPolicyCmptTypeAssociation.class,
                    ITestCase.class, 
                    ITestCaseType.class}, 
                new Class[] { IFolder.class,
                              IFile.class, 
                              IProject.class });
    }

    protected ModelContentProvider createContentProvider() {
        return new ModelContentProvider(config, isFlatLayout);
    }

    public void createPartControl(Composite parent) {
        // init saved state
        contentProvider.setExcludeNoIpsProjects(excludeNoIpsProjects);

        labelProvider = new ModelLabelProvider();
        treeViewer = new TreeViewer(parent);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(labelProvider);
        treeViewer.setSorter(new ModelExplorerSorter());
        treeViewer.setInput(IpsPlugin.getDefault().getIpsModel());

        treeViewer.addDoubleClickListener(new ModelExplorerDoubleclickListener(treeViewer));
        treeViewer.addDragSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() },
                new IpsElementDragListener(treeViewer));
        treeViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() },
                new ModelExplorerDropListener());

        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingLabelProvider decoProvider = new DecoratingLabelProvider(labelProvider, decoManager
                .getLabelDecorator());
        treeViewer.setLabelProvider(decoProvider);

        createFilters(treeViewer);

        getSite().setSelectionProvider(treeViewer);
        resourceListener = new IpsResourceChangeListener(treeViewer) {
            // TODO Optimize refresh: refresh folders if files were added or removed, additionally
            // refresh changed files
            protected IResource[] internalResourceChanged(IResourceChangeEvent event) {
                IResourceDelta delta = event.getDelta();
                IResource res = delta.getResource();
                if (res != null) {
                    return new IResource[] { res };
                }
                return new IResource[] {};
            }
        };
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_BUILD);

        /*
         * Use the current value of isFlatLayout, which is set by loading the memento/viewState
         * before this method is called
         */
        setFlatLayout(isFlatLayout);

        // create 'link with editor' ection
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
    }
    
    /**
     * Show an IPS File or normal File in the navigator view corresponding to
     * the active editor if "link with editor" is enabled.
     *
     * @param part editor
     */
    private void editorActivated(IEditorPart editorPart) {
        if (!isLinkingEnabled() || editorPart == null) {
            return;
        }

        if (editorPart instanceof IpsObjectEditor) {
            IpsObjectEditor ipsEditor = (IpsObjectEditor)editorPart;

            IStructuredSelection newSelection = new StructuredSelection(ipsEditor.getIpsSrcFile());
            if (treeViewer.getSelection().equals(newSelection)) {
                treeViewer.getTree().showSelection();
            } else {
                treeViewer.setSelection(newSelection, true);
            }
            return;
        }

        if (editorPart.getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)editorPart.getEditorInput()).getFile();

            IStructuredSelection newSelection = new StructuredSelection(file);

            if (treeViewer.getSelection().equals(newSelection)) {
                treeViewer.getTree().showSelection();
            } else {
                treeViewer.setSelection(newSelection, true);
            }
        }
    }

    protected void createFilters(TreeViewer tree) {
    }

    /*
     * Create menu for layout styles.
     */
    private void createMenu(IMenuManager menuManager) {
        IAction flatLayoutAction = new LayoutAction(this, true);
        flatLayoutAction.setText(Messages.ModelExplorer_actionFlatLayout);
        flatLayoutAction.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("ModelExplorerFlatLayout.gif")); //$NON-NLS-1$
        IAction hierarchicalLayoutAction = new LayoutAction(this, false);
        hierarchicalLayoutAction.setText(Messages.ModelExplorer_actionHierarchicalLayout);
        hierarchicalLayoutAction.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor(
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
     * Create additional menu entries e.g. filters
     */
    protected void createAdditionalMenuEntries(IMenuManager menuManager){
        menuManager.add(new Separator(MENU_FILTER_GROUP));
        Action showNoIpsProjectsAction = createShowNoIpsProjectsAction();
        showNoIpsProjectsAction.setChecked(excludeNoIpsProjects);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showNoIpsProjectsAction);
        menuManager.add(toggleLinking);

    }

    protected void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new MenuBuilder());

        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, treeViewer);
    }


    private void createToolBar() {
        Action refreshAction = new TreeViewerRefreshAction(getSite());
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(retargetAction);
        getViewSite().getActionBars().getToolBarManager().add(new ExpandCollapseAllAction(treeViewer));
        getViewSite().getActionBars().getToolBarManager().add(toggleLinking);

    }

    public void setFocus() {
    }

    /**
     * Answers whether this part shows the packagFragments flat or hierarchical.
     */
    private boolean isFlatLayout() {
        return isFlatLayout;
    }

    /**
     * Sets the layout style to flat respectively hierarchical. Informs label and contentprovider,
     * activates emptyPackageFilter for flat layout to hide empty PackageFragments.
     *
     * @param b
     */
    private void setFlatLayout(boolean b) {
        isFlatLayout = b;

        ipsDecorator.setFlatLayout(isFlatLayout());
        contentProvider.setIsFlatLayout(isFlatLayout());
        labelProvider.setIsFlatLayout(isFlatLayout());

        treeViewer.getControl().setRedraw(false);
        treeViewer.refresh();
        treeViewer.getControl().setRedraw(true);
    }

    /**
     * Loads the layout style from the given Memento Object. {@inheritDoc}
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento layout = memento.getChild(MEMENTO);
            if (layout != null) {
                Integer layoutValue = layout.getInteger(LAYOUT_STYLE_KEY);
                Integer filterValue = layout.getInteger(FILTER_KEY);
                Integer linkingValue = layout.getInteger(LINK_TO_EDITOR_KEY);
                isFlatLayout = layoutValue==null?false:layoutValue.intValue() == FLAT_LAYOUT;
                excludeNoIpsProjects = filterValue==null?false:filterValue.intValue() == 1;
                linkingEnabled = linkingValue==null?false:linkingValue.intValue() == 1;
            }
        }
    }

    /**
     * Is the editor linked to the navigator.
     *
     * @return <code>true</code> if linking is enabled.
     */
    public boolean isLinkingEnabled() {
        return linkingEnabled;
    }

    /**
     * Save the state and link editor with navigator if linkingEnabled is <code>true</code>.
     *
     * @param linkingEnabled Should linking be enabled.
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
     * Saves the current layout style into the given memento object. {@inheritDoc}
     */
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(MEMENTO);
        layout.putInteger(LAYOUT_STYLE_KEY, isFlatLayout() ? FLAT_LAYOUT : HIERARCHICAL_LAYOUT);
        layout.putInteger(FILTER_KEY,  excludeNoIpsProjects ? 1 : 0);
        layout.putInteger(LINK_TO_EDITOR_KEY,  linkingEnabled ? 1 : 0);
    }

    /**
     * Unregisters this part as resource-listener in the workspace, and disposes of it.
     */
    public void dispose() {
        editorActivationListener.dispose();
        
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);

        super.dispose();
    }

    /**
     * {@inheritDoc} Never used...
     */
    public boolean show(ShowInContext context) {
        ISelection selection = context.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = ((IStructuredSelection)selection);
            if (structuredSelection.size() >= 1) {
                return reveal(structuredSelection.getFirstElement());
            }
        }

        Object input = context.getInput();
        if (input instanceof IProductCmpt) {
            return reveal(context.getInput());
        }
        else if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            return reveal(file);
        }
        return false;
    }

    private boolean reveal(Object toReveal) {
        Object node;
        if (toReveal instanceof Object[]) {
            node = ((Object[])toReveal)[0];
        }
        else {
            node = toReveal;
        }

        if (node instanceof IProductCmptGeneration) {
            treeViewer.setSelection(new StructuredSelection(node), true);
            return true;
        }
        else if (node instanceof IProductCmpt) {
            treeViewer.setSelection(new StructuredSelection(node), true);
            return true;
        }
        else if (node instanceof IFile) {
            try {
                IIpsSrcFile file = (IIpsSrcFile)IpsPlugin.getDefault().getIpsModel().getIpsElement((IFile)node);
                IIpsObject obj = file.getIpsObject();
                treeViewer.setSelection(new StructuredSelection(obj), true);
                return true;
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return false;
    }

    protected class MenuBuilder implements IMenuListener {
        // hold references to enabled RetargetActions
        private ActionGroup openActionGroup =  new OpenActionGroup(ModelExplorer.this);
        private ModelExplorerDeleteAction deleteAction = new ModelExplorerDeleteAction(treeViewer, getSite().getShell());
        private IWorkbenchAction copy = ActionFactory.COPY.create(getSite().getWorkbenchWindow());
        private IWorkbenchAction paste = ActionFactory.PASTE.create(getSite().getWorkbenchWindow());
        private IWorkbenchAction delete = ActionFactory.DELETE.create(getSite().getWorkbenchWindow());

        private IWorkbenchAction rename = ActionFactory.RENAME.create(getSite().getWorkbenchWindow());
        private IWorkbenchAction move = ActionFactory.MOVE.create(getSite().getWorkbenchWindow());

        public MenuBuilder() {
            getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(),
                    new IpsCopyAction(treeViewer, getSite().getShell()));
            getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(),
                    new IpsPasteAction(treeViewer, getSite().getShell()));
            getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);

            getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.RENAME.getId(),
                    new RenameAction(getSite().getShell(), treeViewer));
            getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.MOVE.getId(),
                    new MoveAction(getSite().getShell(), treeViewer));
        }

        /**
         * Creates this parts' contextmenu in the given MenuManager dynamically. The context menu
         * and its elements depend on the current selection and the
         * <code>ModelExplorerConfiguration</code>. {@inheritDoc}
         */
        public void menuAboutToShow(IMenuManager manager) {
            if (!(treeViewer.getSelection() instanceof IStructuredSelection)) {
                return;
            }
            Object selected = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
            IStructuredSelection structuredSelection = (IStructuredSelection)treeViewer.getSelection();
            if (selected == null) {
                return;
            }
            selected = mapIpsSrcFile2IpsObject(selected);
            createNewMenu(manager, selected);
            manager.add(new Separator());
            createOpenMenu(manager, selected, (IStructuredSelection)treeViewer.getSelection());
            manager.add(new Separator());
            createReorgActions(manager, selected);
            manager.add(new Separator());
            createObjectInfoActions(manager, selected);
            manager.add(new Separator());
            createProjectActions(manager, selected, (IStructuredSelection)treeViewer.getSelection());
            manager.add(new Separator());
            createTestCaseAction(manager, selected);
            createIpsEditSortOrderAction(manager, selected);
            createFixDifferencesAction(manager, selected, (IStructuredSelection)treeViewer.getSelection());
            createIpsArchiveAction(manager, selected);
            // menus with submenus
            createRefactorMenu(manager, selected);
            createAdditionalActions(manager, structuredSelection);
            manager.add(new Separator());
            createPropertiesActions(manager, selected);
        }

        protected void createNewMenu(IMenuManager manager, Object selected) {
            selected = mapIpsSrcFile2IpsObject(selected);
            MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew);
            if ((selected instanceof IFolder) || (selected instanceof IIpsProject)) {
                newMenu.add(new NewFolderAction(getSite().getShell(), treeViewer));
                newMenu.add(new NewFileResourceAction(getSite().getShell(), treeViewer));
            }
            if ((selected instanceof IIpsElement) && !(selected instanceof IIpsProject)) {
                newMenu.add(new NewIpsPacketAction(getSite().getShell(), treeViewer));
                newMenu.add(new NewFileResourceAction(getSite().getShell(), treeViewer));
                if (config.isAllowedIpsElementType(IProductCmpt.class)) {
                    newMenu.add(new NewProductComponentAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(ITableContents.class)) {
                    newMenu.add(new NewTableContentAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(ITestCase.class)) {
                    newMenu.add(new NewTestCaseAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(IPolicyCmptType.class)) {
                    newMenu.add(new NewPolicyComponentTypeAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(ITableStructure.class)) {
                    newMenu.add(new NewTableStructureAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(ITestCaseType.class)) {
                    newMenu.add(new NewTestCaseTypeAction(getSite().getWorkbenchWindow()));
                }

                // add copy actions depend on selected ips object type
                List ipsCopyActions = new ArrayList(3);
                if (selected instanceof IProductCmpt) {
                    ipsCopyActions.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer,
                            DeepCopyWizard.TYPE_NEW_VERSION));
                    ipsCopyActions.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer,
                            DeepCopyWizard.TYPE_COPY_PRODUCT));
                } else if (selected instanceof ITestCase) {
                    ipsCopyActions.add(new IpsTestCaseCopyAction(getSite().getShell(), treeViewer));
                }
                if (ipsCopyActions.size()>0){
                    newMenu.add(new Separator());
                    for (Iterator iter = ipsCopyActions.iterator(); iter.hasNext();) {
                        newMenu.add((IpsAction)iter.next());
                    }
                    
                }
            }
            manager.add(newMenu);
        }

        private Object mapIpsSrcFile2IpsObject(Object selected) {
            if (selected instanceof IIpsSrcFile){
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)selected;
                selected = ipsSrcFile.getIpsObjectType().newObject(ipsSrcFile);
            }
            return selected;
        }

        protected void createOpenMenu(IMenuManager manager, Object selected, IStructuredSelection structuredSelected) {
            if (selected instanceof IIpsObject || selected instanceof IPolicyCmptTypeAssociation || selected instanceof IPolicyCmptTypeAttribute) {
                manager.add(new OpenEditorAction(treeViewer));
            } else {
                openActionGroup.setContext(new ActionContext(structuredSelected));
                openActionGroup.fillContextMenu(manager);
            }
        }

        protected void createReorgActions(IMenuManager manager, Object selected) {
            manager.add(copy);
            manager.add(paste);
            manager.add(delete);

            copy.setEnabled(true);
            paste.setEnabled(true);
            delete.setEnabled(true);

            if (selected instanceof IIpsObjectPart){
                copy.setEnabled(false);
                paste.setEnabled(false);
                delete.setEnabled(false);
                return;
            }

            if (isRootArchive(selected)) {
                paste.setEnabled(false);
                delete.setEnabled(false);
            }
        }

        private IIpsPackageFragmentRoot getPackageFragmentRoot(Object object) {
            IIpsPackageFragmentRoot root = null;
            if (object instanceof IIpsObject) {
                root = ((IIpsObject)object).getIpsPackageFragment().getRoot();
            }
            else if (object instanceof IIpsPackageFragment) {
                root = ((IIpsPackageFragment)object).getRoot();
            }
            else if (object instanceof IIpsPackageFragmentRoot) {
                root = (IIpsPackageFragmentRoot)object;
            }
            return root;
        }

        private boolean isRootArchive(Object object) {
            IIpsPackageFragmentRoot root = getPackageFragmentRoot(object);
            if (root != null) {
                try {
                    return root.getIpsArchive() != null;
                }
                catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return false;
        }

        protected void createObjectInfoActions(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsElement) {
                if (selected instanceof IProductCmpt) {
                    manager.add(new ShowStructureAction(treeViewer));
                    manager.add(new FindProductReferencesAction(treeViewer));
                }
                if (selected instanceof IPolicyCmptType) {
                    manager.add(new FindPolicyReferencesAction(treeViewer));
                }
                // not to be used in this release
                // if (selected instanceof IPolicyCmptType | selected instanceof IProductCmpt) {
                // manager.add(new ShowAttributesAction(treeViewer));
                // }
            }
        }

        protected void createProjectActions(IMenuManager manager, Object selected, IStructuredSelection selection) {
            if (selected instanceof IIpsElement) {
                if (selected instanceof IIpsProject) {
                    manager.add(openCloseAction((IProject)((IIpsProject)selected).getCorrespondingResource()));
                    try {
                        if (!IpsPlugin.getDefault().getMigrationOperation(((IIpsProject)selected)).isEmpty()) {
                            manager.add(new MigrateProjectAction(getSite().getWorkbenchWindow(), selection));
                        }
                    }
                    catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }
            }
            else {
                if (selected instanceof IProject) {
                    manager.add(openCloseAction((IProject)selected));
                }
            }
        }

        protected IAction openCloseAction(IProject project) {
            if (project.isOpen()) {
                CloseResourceAction close = new CloseResourceAction(getSite().getShell());
                close.selectionChanged((IStructuredSelection)treeViewer.getSelection());
                return close;
            }
            else {
                OpenProjectAction open = new OpenProjectAction(getSite());
                open.selectionChanged((IStructuredSelection)treeViewer.getSelection());
                return open;
            }
        }

        protected void createTestCaseAction(IMenuManager manager, Object selected) {
            if (config.isAllowedIpsElementType(ITestCase.class) || config.isAllowedIpsElementType(IProductCmpt.class)) {
                if (selected instanceof IIpsPackageFragment || selected instanceof IIpsPackageFragmentRoot
                        || selected instanceof IIpsProject || selected instanceof ITestCase
                        || selected instanceof IProductCmpt) {
                    manager.add(new IpsTestAction(treeViewer));
                }
            }
        }

        protected void createFixDifferencesAction(IMenuManager manager, Object selected, IStructuredSelection selection) {
            // show fix differences menu only for the model explorer
            if (! ModelExplorer.this.isModelExplorer()){
                return;
            }
            if (selected instanceof IIpsElement) {
                if (selected instanceof IIpsProject) {
                    IIpsProject project = (IIpsProject)selected;
                    if (project.isProductDefinitionProject()) {
                        manager.add(new FixDifferencesAction(getSite().getWorkbenchWindow(), selection));
                    }
                }
                else if (selected instanceof IIpsPackageFragmentRoot) {
                    manager.add(new FixDifferencesAction(getSite().getWorkbenchWindow(), selection));
                }
                else if (selected instanceof IIpsPackageFragment) {
                    manager.add(new FixDifferencesAction(getSite().getWorkbenchWindow(), selection));
                }
                else if (selected instanceof IFixDifferencesToModelSupport) {
                    manager.add(new FixDifferencesAction(getSite().getWorkbenchWindow(), selection));
                }
            }
        }

        private void createIpsArchiveAction(IMenuManager manager, Object selected) {
            // show ips archive menu only for the model explorer
            if (! ModelExplorer.this.isModelExplorer()){
                return;
            }
            if (config.isAllowedIpsElementType(IIpsProject.class)
                    || config.isAllowedIpsElementType(IIpsPackageFragmentRoot.class)) {
                if (selected instanceof IIpsProject || selected instanceof IIpsPackageFragmentRoot) {
                    if (selected instanceof IIpsPackageFragmentRoot) {
                        try {
                            // don't enable menu for ips archives
                            if (((IIpsPackageFragmentRoot)selected).getIpsArchive() != null) {
                                return;
                            }
                        }
                        catch (CoreException e) {
                            // ignore exception while creating the menu
                        }
                    }
                    manager.add(new CreateIpsArchiveAction(treeViewer));
                }
            }
        }

        protected void createRefactorMenu(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsElement & !(selected instanceof IIpsProject) | selected instanceof IFile
                    | selected instanceof IFolder) {
                if (!isRootArchive(selected)) {
                    MenuManager subMm = new MenuManager(Messages.ModelExplorer_submenuRefactor);
                    subMm.add(rename);
                    subMm.add(move);
                    manager.add(subMm);
                }
            }
        }

        protected void createIpsEditSortOrderAction(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsElement) {
                manager.add(new IpsEditSortOrderAction(treeViewer));
            }
        }
        
        protected void createAdditionalActions(IMenuManager manager, IStructuredSelection structuredSelection) {
            manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));//$NON-NLS-1$
        }

        protected void createPropertiesActions(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsProject) {
                manager.add(new IpsPropertiesAction(getSite(), treeViewer));
            }
            else if (selected instanceof IProject) {
                if (((IProject)selected).isOpen()) {
                    manager.add(new IpsPropertiesAction(getSite(), treeViewer));
                }
            }
        }
    }

    private class LayoutAction extends Action implements IAction {
        private boolean isFlatLayout;
        private ModelExplorer modelExplorer;

        public LayoutAction(ModelExplorer modelExplorer, boolean flat) {
            super("", AS_RADIO_BUTTON); //$NON-NLS-1$

            isFlatLayout = flat;
            this.modelExplorer = modelExplorer;
        }

        /*
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            if (modelExplorer.isFlatLayout() != isFlatLayout) {
                modelExplorer.setFlatLayout(isFlatLayout);
            }
        }
    }

    /**
     * @return Returns the contentProvider.
     */
    protected ModelContentProvider getContentProvider() {
        return contentProvider;
    }

    /**
     * Returns <code>true</code> if this class is a kind of model explorer,
     * the model explorer is an explorer with enhanced functionality.
     * Other derived explorer classes should return <code>false</code> if
     * a restriced menu operations should be provided.
     */
    protected boolean isModelExplorer() {
        return true;
    }

    private Action createShowNoIpsProjectsAction() {
        return new Action(Messages.ModelExplorer_menuShowIpsProjectsOnly_Title, Action.AS_CHECK_BOX) {
            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            public void run() {
                excludeNoIpsProjects = ! excludeNoIpsProjects;
                contentProvider.setExcludeNoIpsProjects(excludeNoIpsProjects);
                treeViewer.refresh();
            }
            public String getToolTipText() {
                return Messages.ModelExplorer_menuShowIpsProjectsOnly_Tooltip;
            }
        };
    }
}