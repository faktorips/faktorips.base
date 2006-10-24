package org.faktorips.devtools.core.ui.views.modelexplorer;

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
import org.eclipse.jface.viewers.DecoratingLabelProvider;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.actions.ExpandCollapseAllAction;
import org.faktorips.devtools.core.ui.actions.FindPolicyReferencesAction;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.IpsPropertiesAction;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.actions.ModelExplorerDeleteAction;
import org.faktorips.devtools.core.ui.actions.MoveAction;
import org.faktorips.devtools.core.ui.actions.NewFileResourceAction;
import org.faktorips.devtools.core.ui.actions.NewFolderAction;
import org.faktorips.devtools.core.ui.actions.NewPolicyComponentTypeAction;
import org.faktorips.devtools.core.ui.actions.NewProductComponentAction;
import org.faktorips.devtools.core.ui.actions.NewTableContentAction;
import org.faktorips.devtools.core.ui.actions.NewTestCaseAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.RenameAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.actions.TreeViewerRefreshAction;
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

    /**
     * Used for saving the current layout style in a eclipse memento.
     */
    private static final String LAYOUT_MEMENTO = "layout"; //$NON-NLS-1$

    /**
     * Used for saving the current layout style in a eclipse memento.
     */
    private static final String LAYOUT_STYLE_KEY = "style"; //$NON-NLS-1$

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
    private ModelLabelProvider labelProvider = new ModelLabelProvider();;

    private IpsResourceChangeListener resourceListener;
    
    protected ModelExplorerConfiguration config;
    /**
     * Flag that indicates whether the current layout style is flat (true) or hierarchical (false).
     */
    protected boolean isFlatLayout = false;

    public ModelExplorer() {
        super();
        config = createConfig();
        contentProvider= createContentProvider();
    }

    protected ModelExplorerConfiguration createConfig() {
        return new ModelExplorerConfiguration(new Class[] { IPolicyCmptType.class, ITableStructure.class,
                IProductCmpt.class, ITableContents.class, IAttribute.class, IRelation.class, ITestCase.class,
                ITestCaseType.class }, new Class[] { IFolder.class, IFile.class, IProject.class });
    }
    
    protected ModelContentProvider createContentProvider(){
        return new ModelContentProvider(config, isFlatLayout);
    }

    public void createPartControl(Composite parent) {
        treeViewer = new TreeViewer(parent);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(labelProvider);
        treeViewer.setSorter(new ModelExplorerSorter());
        treeViewer.setInput(IpsPlugin.getDefault().getIpsModel());
        treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));
        treeViewer.addDragSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() },
                new IpsElementDragListener(treeViewer));
        treeViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance() },
                new ModelExplorerDropListener());

        IDecoratorManager decoManager= IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingLabelProvider decoProvider = new DecoratingLabelProvider(labelProvider, decoManager.getLabelDecorator());
        treeViewer.setLabelProvider(decoProvider);

        createFilters(treeViewer);

        getSite().setSelectionProvider(treeViewer);
        resourceListener = new IpsResourceChangeListener(treeViewer){
            // TODO Optimize refresh: refresh folders if files were added or removed, additionally refresh changed files 
            protected IResource[] internalResourceChanged(IResourceChangeEvent event) {
                IResourceDelta delta= event.getDelta();
                IResource res= delta.getResource();
                if(res!=null){
                    return new IResource[]{res};
                }
                return new IResource[]{};
            }
        };
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_BUILD);
        /*
         * Use the current value of isFlatLayout, which is set by loading the memento/viewState
         * before this method is called
         */
        setFlatLayout(isFlatLayout);

        createMenu();
        createContextMenu();
        createToolBar();
    }

    protected void createFilters(TreeViewer tree) {
    }

    /**
     * Create menu for layout styles.
     */
    private void createMenu() {
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
        IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
        IMenuManager layoutMenu = new MenuManager(Messages.ModelExplorer_submenuLayout);
        layoutMenu.add(flatLayoutAction);
        layoutMenu.add(hierarchicalLayoutAction);
        mgr.add(layoutMenu);
    }

    protected void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new MenuBuilder());

        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, treeViewer);
    }
    
    private void createToolBar(){
        Action refreshAction= new TreeViewerRefreshAction(getSite());
        getViewSite().getActionBars()
                .setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(retargetAction);
        getViewSite().getActionBars().getToolBarManager().add(new ExpandCollapseAllAction(treeViewer));
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
            IMemento layout = memento.getChild(LAYOUT_MEMENTO);
            if (layout != null) {
                isFlatLayout = layout.getInteger(LAYOUT_STYLE_KEY).intValue() == FLAT_LAYOUT;
            }
        }
    }

    /**
     * Saves the current layout style into the given memento object. {@inheritDoc}
     */
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(LAYOUT_MEMENTO);
        layout.putInteger(LAYOUT_STYLE_KEY, isFlatLayout() ? FLAT_LAYOUT : HIERARCHICAL_LAYOUT);
    }

    /**
     * Unregisters this part as resource-listener in the workspace, and disposes of it.
     */
    public void dispose() {
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
        } else if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            return reveal(file);
        }
        return false;
    }

    private boolean reveal(Object toReveal) {
        Object node;
        if (toReveal instanceof Object[]) {
            node = ((Object[])toReveal)[0];
        } else {
            node = toReveal;
        }

        if (node instanceof IProductCmptGeneration) {
            treeViewer.setSelection(new StructuredSelection(node), true);
            return true;
        } else if (node instanceof IProductCmpt) {
            treeViewer.setSelection(new StructuredSelection(node), true);
            return true;
        } else if (node instanceof IFile) {
            try {
                IIpsSrcFile file = (IIpsSrcFile)IpsPlugin.getDefault().getIpsModel().getIpsElement(
                        (IFile)node);
                IIpsObject obj = file.getIpsObject();
                treeViewer.setSelection(new StructuredSelection(obj), true);
                return true;
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return false;
    }

    protected class MenuBuilder implements IMenuListener {
        // hold references to enabled RetargetActions
        private IpsDeleteAction ipsDelete = new ModelExplorerDeleteAction(treeViewer, getSite().getShell());
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
            getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), ipsDelete);

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
            if (selected == null) {
                return;
            }

            createEditActions(manager, selected);
            createNewMenu(manager, selected);
            manager.add(new Separator());
            createReorgActions(manager, selected);
            manager.add(new Separator());
            createObjectInfoActions(manager, selected);
            manager.add(new Separator());
            createProjectActions(manager, selected);
            manager.add(new Separator());
            createTestCaseAction(manager, selected);
            createRefactorMenu(manager, selected);
            createAdditionalActions(manager, selected);
            manager.add(new Separator());
            createPropertiesActions(manager, selected);
        }
        
        protected void createEditActions(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsObject || selected instanceof IFile || selected instanceof IRelation
                    || selected instanceof IAttribute) {
                manager.add(new OpenEditorAction(treeViewer));
            }
        }

        protected void createNewMenu(IMenuManager manager, Object selected) {
            MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew);
            if (selected instanceof IFolder) {
                newMenu.add(new NewFolderAction(getSite().getShell(), treeViewer));
                newMenu.add(new NewFileResourceAction(getSite().getShell(), treeViewer));
            }
            if (selected instanceof IIpsElement) {
                newMenu.add(new NewFolderAction(getSite().getShell(), treeViewer));
                newMenu.add(new NewFileResourceAction(getSite().getShell(), treeViewer));
                if (config.isAllowedIpsElementType(IProductCmpt.class)) {
                    newMenu.add(new NewProductComponentAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(ITableContents.class)) {
                    newMenu.add(new NewTableContentAction(getSite().getWorkbenchWindow()));
                }
                if (config.isAllowedIpsElementType(IPolicyCmptType.class)) {
                    newMenu.add(new NewPolicyComponentTypeAction(getSite().getWorkbenchWindow()));
                }
                if (selected instanceof IProductCmpt) {
                    newMenu
                            .add(new IpsDeepCopyAction(getSite().getShell(), treeViewer,
                                    DeepCopyWizard.TYPE_NEW_VERSION));
                    newMenu.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer,
                            DeepCopyWizard.TYPE_COPY_PRODUCT));
                }
                if (config.isAllowedIpsElementType(ITestCase.class)) {
                    newMenu.add(new NewTestCaseAction(getSite().getWorkbenchWindow()));
                }
            }
            manager.add(newMenu);
        }

        protected void createReorgActions(IMenuManager manager, Object selected) {
            manager.add(copy);
            manager.add(paste);
            manager.add(delete);

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

        protected void createProjectActions(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsElement) {
                if (selected instanceof IIpsProject) {
                    manager.add(openCloseAction((IProject)((IIpsProject)selected).getCorrespondingResource()));
                }
            } else {
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
            } else {
                OpenProjectAction open = new OpenProjectAction(getSite());
                open.selectionChanged((IStructuredSelection)treeViewer.getSelection());
                return open;
            }
        }

        protected void createTestCaseAction(IMenuManager manager, Object selected) {
            if (config.isAllowedIpsElementType(ITestCase.class) || config.isAllowedIpsElementType(IProductCmpt.class)) {
                if (selected instanceof IIpsPackageFragment || selected instanceof IIpsPackageFragmentRoot
                        || selected instanceof IIpsProject || selected instanceof ITestCase || selected instanceof IProductCmpt) {
                    manager.add(new IpsTestAction(treeViewer));
                }
            }
        }

        protected void createRefactorMenu(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsElement & !(selected instanceof IIpsProject) | selected instanceof IFile | selected instanceof IFolder ) {
                MenuManager subMm = new MenuManager(Messages.ModelExplorer_submenuRefactor);
                subMm.add(rename);
                subMm.add(move);
                manager.add(subMm);
            }
        }

        protected void createAdditionalActions(IMenuManager manager, Object selected) {
            manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));//$NON-NLS-1$
        }

        protected void createPropertiesActions(IMenuManager manager, Object selected) {
            if (selected instanceof IIpsProject) {
                manager.add(new IpsPropertiesAction(getSite(), treeViewer));
            } else if (selected instanceof IProject) {
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
}
