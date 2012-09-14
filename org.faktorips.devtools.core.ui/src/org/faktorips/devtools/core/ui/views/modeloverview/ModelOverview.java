/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeloverview;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.internal.ICollectorFinishedListener;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerContextMenuBuilder;
import org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.ShowTypeState;
import org.faktorips.devtools.core.ui.views.modeloverview.AbstractModelOverviewContentProvider.ToChildAssociationType;

public class ModelOverview extends ViewPart implements ICollectorFinishedListener {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modeloverview.ModelOverview"; //$NON-NLS-1$
    private static final String MENU_INFO_GROUP = "group.info"; //$NON-NLS-1$

    private static final String SHOW_CARDINALITIES = "show_cardinalities"; //$NON-NLS-1$
    private static final String SHOW_ROLENAMES = "show_rolenames"; //$NON-NLS-1$
    private static final String SHOW_PROJECTS = "show_projects"; //$NON-NLS-1$

    private IPolicyCmptType toggledPolicyCmptInput;
    private IProductCmptType toggledProductCmptInput;

    private Composite panel;
    private TreeViewer treeViewer;
    private final UIToolkit uiToolkit = new UIToolkit(null);
    private Label label;
    private Label emptyMessageLabel;

    private ModelOverviewLabelProvider labelProvider;
    private AbstractModelOverviewContentProvider provider;

    private IMemento memento;
    private Action toggleProductPolicyAction;
    private ExpandAllAction expandAllAction;
    private CollapseAllAction collapseAllAction;

    @Override
    public void createPartControl(Composite parent) {
        panel = uiToolkit.createGridComposite(parent, 1, false, true, new GridData(SWT.FILL, SWT.FILL, true, true));

        label = uiToolkit.createLabel(panel, "", SWT.LEFT, new GridData(SWT.FILL, SWT.FILL, //$NON-NLS-1$
                true, false));

        treeViewer = new TreeViewer(panel);
        // provider = new ModelOverviewContentProvider();
        provider = new ModelOverviewInheritAssociationsContentProvider();
        treeViewer.setContentProvider(provider);

        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        labelProvider = new ModelOverviewLabelProvider();
        DecoratingStyledCellLabelProvider decoratingLabelProvider = new DecoratingStyledCellLabelProvider(
                labelProvider, decoManager.getLabelDecorator(), new DecorationContext());

        treeViewer.setLabelProvider(decoratingLabelProvider);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        getSite().setSelectionProvider(treeViewer); // necessary for the context menu

        // initialize the empty message
        emptyMessageLabel = uiToolkit.createLabel(panel, "", SWT.WRAP, new GridData(SWT.FILL, SWT.FILL, //$NON-NLS-1$
                true, true));
        emptyMessageLabel.setText(Messages.IpsModelOverview_emptyMessage);

        activateContext();
        createContextMenu();
        initMenu();
        initToolBar();

        // set default show state and the according toggle-button image
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        setProductCmptTypeImage();

        treeViewer.addTreeListener(createNewAutoExpandStructureNodesListener());
        treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));
        provider.addCollectorFinishedListener(this);

        showEmptyMessage();
    }

    private void showEmptyMessage() {
        enableButtons(false);
        if (!emptyMessageLabel.isDisposed()) {
            emptyMessageLabel.setVisible(true);
            ((GridData)emptyMessageLabel.getLayoutData()).exclude = false;
        }
        if (!label.isDisposed()) {
            label.setVisible(false);
            ((GridData)label.getLayoutData()).exclude = true;
        }
        if (!treeViewer.getTree().isDisposed()) {
            treeViewer.getTree().setVisible(false);
            ((GridData)treeViewer.getTree().getLayoutData()).exclude = true;
        }
        panel.layout();
    }

    private void showTree() {
        enableButtons(true);
        if (!emptyMessageLabel.isDisposed()) {
            emptyMessageLabel.setVisible(false);
            ((GridData)emptyMessageLabel.getLayoutData()).exclude = true;
        }
        if (!label.isDisposed()) {
            label.setVisible(true);
            ((GridData)label.getLayoutData()).exclude = false;
        }
        if (!treeViewer.getTree().isDisposed()) {
            treeViewer.getTree().setVisible(true);
            ((GridData)treeViewer.getTree().getLayoutData()).exclude = false;
        }
        panel.layout();
    }

    private ITreeViewerListener createNewAutoExpandStructureNodesListener() {
        return new ITreeViewerListener() {

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                // do nothing
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                final Object element = event.getElement();
                final AbstractTreeViewer treeViewer = event.getTreeViewer();
                if (element instanceof ComponentNode) {
                    Control ctrl = treeViewer.getControl();
                    if (ctrl != null && !ctrl.isDisposed()) {
                        ctrl.getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                Control ctrl2 = treeViewer.getControl();
                                if (ctrl2 != null && !ctrl2.isDisposed()) {
                                    treeViewer.expandToLevel(element, 2);
                                }
                            }
                        });
                    }
                }
            }
        };
    }

    private void activateContext() {
        IContextService service = (IContextService)getSite().getService(IContextService.class);
        service.activateContext("org.faktorips.devtools.core.ui.views.modelExplorer.context"); //$NON-NLS-1$
    }

    private void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.add(new Separator("open")); //$NON-NLS-1$
        manager.add(new OpenEditorAction(treeViewer));
        manager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
        manager.add(new GroupMarker(ModelExplorerContextMenuBuilder.GROUP_NAVIGATE));
        final Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, treeViewer);
        MenuCleaner.addAdditionsCleaner(manager);

        manager.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                IIpsSrcFile srcFile = getCurrentlySelectedIpsSrcFile();

                if (srcFile == null) { // show the menu only on non-structure nodes
                    contextMenu.setVisible(false);
                }
            }
        });

    }

    private IIpsSrcFile getCurrentlySelectedIpsSrcFile() {
        TypedSelection<IAdaptable> typedSelection = getSelectionFromSelectionProvider();
        if (typedSelection == null || !typedSelection.isValid()) {
            return null;
        }

        return (IIpsSrcFile)typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
    }

    private TypedSelection<IAdaptable> getSelectionFromSelectionProvider() {
        TypedSelection<IAdaptable> typedSelection;
        ISelectionService selectionService = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, selectionService.getSelection());
        return typedSelection;
    }

    @Override
    public void setFocus() {
        this.treeViewer.getTree().setFocus();
    }

    @Override
    public void dispose() {
        uiToolkit.dispose();
    }

    /**
     * Configures the View to display the scope of a complete IpsProject.
     * 
     * @param input the selected {@link IIpsProject}
     */
    public void showOverview(IIpsProject input) {
        this.showTree();
        this.treeViewer.setInput(input);
        this.updateView();
    }

    /**
     * Configures the View to display the scope of a single IType.
     * 
     * @param input the selected {@link IType}
     */
    public void showOverview(IType input) {
        this.showTree();
        toggleProductPolicyAction.setEnabled(true);
        try {
            if (input instanceof PolicyCmptType) {
                setProductCmptTypeImage();
                IPolicyCmptType policy = (IPolicyCmptType)input;
                toggledProductCmptInput = policy.findProductCmptType(policy.getIpsProject());
                if (toggledProductCmptInput == null) {
                    toggleProductPolicyAction.setEnabled(false);
                }
                toggledPolicyCmptInput = (IPolicyCmptType)input;
            } else if (input instanceof ProductCmptType) {
                setPolicyCmptTypeImage();
                IProductCmptType product = (IProductCmptType)input;
                toggledPolicyCmptInput = product.findPolicyCmptType(product.getIpsProject());
                if (toggledPolicyCmptInput == null) {
                    toggleProductPolicyAction.setEnabled(false);
                }
                toggledProductCmptInput = (ProductCmptType)input;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        this.treeViewer.setInput(input);
        this.updateView();
    }

    /**
     * Returns a {@link TreePath} containing the corresponding {@link IModelOverviewNode
     * IModelOverviewNodes} to the input types. {@link AbstractStructureNode AbstractStructureNodes}
     * will be generated automatically.
     * 
     * @param treePath a list of {@link PathElement PathElements}, ordered from the root-element
     *            downwards
     */
    TreePath computePath(List<PathElement> treePath, ModelOverviewContentProvider contentProvider) {
        // The IpsProject must be from the project which is the lowest in the project hierarchy
        IIpsProject rootProject = treePath.get(treePath.size() - 1).getComponent().getIpsProject();

        // get the root node
        PathElement root = treePath.get(0);
        ComponentNode rootNode = new ComponentNode(root.getComponent(), null, rootProject);
        List<IModelOverviewNode> pathList = new ArrayList<IModelOverviewNode>();
        pathList.add(rootNode);

        for (int i = 1; i < treePath.size(); i++) {
            if (root.getAssociationType() == ToChildAssociationType.SELF) {
                break;
            }
            // add the structure node
            AbstractStructureNode abstractRootChild = null;
            if (root.getAssociationType() == ToChildAssociationType.ASSOCIATION) {
                abstractRootChild = contentProvider.getComponentNodeCompositeChild(rootNode);
            } else { // ToChildAssociationType.SUPERTYPE
                abstractRootChild = contentProvider.getComponentNodeSubtypeChild(rootNode);
            }
            pathList.add(abstractRootChild);

            // add the child node
            for (ComponentNode childNode : abstractRootChild.getChildren()) {
                // note that
                if (childNode.getValue().equals(treePath.get(i).getComponent())) {
                    pathList.add(childNode);
                    rootNode = childNode;
                    break;
                }
            }
            root = treePath.get(i);
        }
        return new TreePath(pathList.toArray());
    }

    private void updateView() {
        Object element = treeViewer.getInput();
        if (element == null) {
            return;
        } else if (element instanceof IType) {
            this.label.setText(((IType)element).getQualifiedName());
        } else if (element instanceof IIpsProject) {
            this.label.setText(((IIpsProject)element).getName());
        } else {
            this.label.setText(element.toString());
        }
    }

    private void initToolBar() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toggleProductPolicyAction = createToggleProductPolicyAction();
        toolBarManager.add(toggleProductPolicyAction);

        expandAllAction = new ExpandAllAction(treeViewer);
        toolBarManager.add(expandAllAction);
        collapseAllAction = new CollapseAllAction(treeViewer);
        toolBarManager.add(collapseAllAction);
    }

    private void initMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(new Separator(MENU_INFO_GROUP));

        Action showCardinalitiesAction = createShowCardinalitiesAction();
        labelProvider.setShowCardinalities(true);

        Action showRoleNameAction = createShowRoleNameAction();
        labelProvider.setShowRolenames(true);

        Action showProjectsAction = createShowProjectsAction();
        labelProvider.setShowProjects(true);

        if (memento != null) {
            Boolean showCard = memento.getBoolean(SHOW_CARDINALITIES);
            Boolean showRoles = memento.getBoolean(SHOW_ROLENAMES);
            Boolean showProjects = memento.getBoolean(SHOW_PROJECTS);
            showCardinalitiesAction.setChecked(showCard == null ? true : showCard);
            showRoleNameAction.setChecked(showRoles == null ? true : showRoles);
            showProjectsAction.setChecked(showProjects == null ? true : showProjects);
        } else {
            showCardinalitiesAction.setChecked(true);
            showRoleNameAction.setChecked(true);
        }

        menuManager.appendToGroup(MENU_INFO_GROUP, showCardinalitiesAction);
        menuManager.appendToGroup(MENU_INFO_GROUP, showRoleNameAction);
        menuManager.appendToGroup(MENU_INFO_GROUP, showProjectsAction);
    }

    private Action createShowCardinalitiesAction() {
        return new Action(Messages.IpsModelOverview_menuShowCardinalities_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("Cardinality.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                labelProvider.toggleShowCardinalities();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsModelOverview_menuShowCardinalities_tooltip;
            }
        };
    }

    private Action createShowRoleNameAction() {
        return new Action(Messages.IpsModelOverview_menuShowRoleName_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                labelProvider.toggleShowRolenames();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsModelOverview_menuShowRoleName_tooltip;
            }
        };
    }

    private Action createShowProjectsAction() {
        return new Action(Messages.IpsModelOverview_menuShowProjects_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                labelProvider.toggleShowProjects();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsModelOverview_menuShowProjects_tooltip;
            }
        };
    }

    private Action createToggleProductPolicyAction() {
        return new Action(Messages.IpsModelOverview_tooltipToggleButton, SWT.TOGGLE) {

            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("PolicyCmptType.gif"); //$NON-NLS-1$
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsModelOverview_tooltipToggleButton;
            }

            @Override
            public void run() {
                toggleShowTypeState();
            }

        };
    }

    private void enableButtons(boolean state) {
        expandAllAction.setEnabled(state);
        collapseAllAction.setEnabled(state);
        toggleProductPolicyAction.setEnabled(state);
    }

    /**
     * Toggles the view between ProductCmpTypes and PolicyCmptTypes. The toggle action takes
     * originally selected element as input for the content provider and shows the corresponding
     * ModelOverview. If the initial content provider input was an IpsProject and the user has not
     * selected any element, the ModelOverview simply switches the view for the complete project.
     */
    private void toggleShowTypeState() {
        Object input = treeViewer.getInput();

        if (input instanceof IIpsProject) { // switch the viewShowState for project selections
            provider.toggleShowTypeState();
            if (provider.getShowTypeState() == ShowTypeState.SHOW_POLICIES) {
                setProductCmptTypeImage();
            } else {
                setPolicyCmptTypeImage();
            }
            treeViewer.getContentProvider().inputChanged(this.treeViewer, input, treeViewer.getInput());
        } else if (input instanceof PolicyCmptType) {
            treeViewer.setInput(toggledProductCmptInput);
            setPolicyCmptTypeImage();
        } else if (input instanceof ProductCmptType) {
            treeViewer.setInput(toggledPolicyCmptInput);
            setProductCmptTypeImage();
        }
        refresh();
    }

    private void setPolicyCmptTypeImage() {
        // FIXME Do not misuse the HoverImageDescriptor for functionality that should be
        // provided by the normal image descriptor
        toggleProductPolicyAction.setHoverImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "PolicyCmptType.gif")); //$NON-NLS-1$
    }

    private void setProductCmptTypeImage() {
        // FIXME Do not misuse the HoverImageDescriptor for functionality that should be
        // provided by the normal image descriptor
        toggleProductPolicyAction.setHoverImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "ProductCmptType.gif")); //$NON-NLS-1$
    }

    private void refresh() {
        final Control ctrl = treeViewer.getControl();

        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }

        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!treeViewer.getControl().isDisposed()) {
                        Object input = treeViewer.getInput();
                        if (input instanceof ComponentNode || input instanceof IIpsProject || input instanceof IType) {
                            treeViewer.refresh();
                        }
                    }
                }
            };

            ctrl.setRedraw(false);
            ctrl.getDisplay().syncExec(runnable);
            this.updateView();
        } finally {
            ctrl.setRedraw(true);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o.equals(this.provider)) {
            if (this.treeViewer.getInput() instanceof IType && provider instanceof ModelOverviewContentProvider) {
                expandPaths((ModelOverviewContentProvider)this.provider);
            }
        }
    }

    private void expandPaths(ModelOverviewContentProvider contentProvider) {
        List<List<PathElement>> paths = contentProvider.getPaths();
        TreePath[] treePaths = new TreePath[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            treePaths[i] = computePath(paths.get(i), contentProvider);
        }
        for (TreePath treePath : treePaths) {
            this.treeViewer.expandToLevel(treePath, 0);
        }
        this.treeViewer.setSelection(new TreeSelection(treePaths));
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        memento.putBoolean(SHOW_CARDINALITIES, this.labelProvider.getShowCardinalities());
        memento.putBoolean(SHOW_ROLENAMES, this.labelProvider.getShowRolenames());
        memento.putBoolean(SHOW_PROJECTS, this.labelProvider.getShowProjects());
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }
}
