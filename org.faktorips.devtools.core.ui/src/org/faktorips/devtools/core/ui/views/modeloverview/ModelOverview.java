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
import java.util.Observer;

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
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerContextMenuBuilder;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ShowTypeState;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ToChildAssociationType;

public class ModelOverview extends ViewPart implements Observer {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modeloverview.ModelOverview"; //$NON-NLS-1$
    private static final String MENU_INFO_GROUP = "group.info"; //$NON-NLS-1$
    private static final String SHOW_CARDINALITIES = "show_cardinalities"; //$NON-NLS-1$
    private static final String SHOW_ROLENAMES = "show_rolenames"; //$NON-NLS-1$

    private TreeViewer treeViewer;
    private final UIToolkit uiToolkit = new UIToolkit(null);
    private Label label;

    private ModelOverviewLabelProvider labelProvider;

    private ModelOverviewContentProvider provider;

    private IMemento memento;

    @Override
    public void createPartControl(Composite parent) {
        Composite panel = uiToolkit.createGridComposite(parent, 1, false, true, new GridData(SWT.FILL, SWT.FILL, true,
                true));

        label = uiToolkit.createLabel(panel, "", SWT.LEFT, new GridData(SWT.FILL, SWT.FILL, //$NON-NLS-1$
                true, false));

        this.treeViewer = new TreeViewer(panel);
        provider = new ModelOverviewContentProvider();
        // default showState for selection of IIpsProjects
        provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        this.treeViewer.setContentProvider(provider);

        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        labelProvider = new ModelOverviewLabelProvider();
        DecoratingStyledCellLabelProvider decoratingLabelProvider = new DecoratingStyledCellLabelProvider(
                labelProvider, decoManager.getLabelDecorator(), new DecorationContext());

        this.treeViewer.setLabelProvider(decoratingLabelProvider);
        this.treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.getSite().setSelectionProvider(treeViewer); // important for the context menu
        treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));

        this.activateContext();
        this.createContextMenu();
        this.initMenu();
        this.initToolBar();

        this.treeViewer.addTreeListener(createNewAutoExpandStructureNodesListener());

        this.provider.addObserver(this);
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

                if (srcFile == null) {
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
        this.treeViewer.setInput(input);
        this.updateView();
    }

    /**
     * Configures the View to display the scope of a single IType.
     * 
     * @param input the selected {@link IType}
     */
    public void showOverview(IType input) {
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
    protected static TreePath computePath(List<PathElement> treePath) {
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
                abstractRootChild = rootNode.getCompositeChild();
            } else { // ToChildAssociationType.SUPERTYPE
                abstractRootChild = rootNode.getSubtypeChild();
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
        toolBarManager.add(createToggleProductPolicyAction());

        toolBarManager.add(new ExpandAllAction(treeViewer));
        toolBarManager.add(new CollapseAllAction(treeViewer));
    }

    private void initMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(new Separator(MENU_INFO_GROUP));

        Action showCardinalitiesAction = createShowCardinalitiesAction();
        labelProvider.setShowCardinalities(true);

        Action showRoleNameAction = createShowRoleNameAction();
        labelProvider.setShowRolenames(true);

        if (memento != null) {
            Boolean showCard = memento.getBoolean(SHOW_CARDINALITIES);
            Boolean showRoles = memento.getBoolean(SHOW_ROLENAMES);
            showCardinalitiesAction.setChecked(showCard == null ? true : showCard);
            showRoleNameAction.setChecked(showRoles == null ? true : showRoles);
        } else {
            showCardinalitiesAction.setChecked(true);
            showRoleNameAction.setChecked(true);
        }

        menuManager.appendToGroup(MENU_INFO_GROUP, showCardinalitiesAction);
        menuManager.appendToGroup(MENU_INFO_GROUP, showRoleNameAction);
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
                return Messages.IpsModelOverview_tooltipToggleCardinalities;
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

    private Action createToggleProductPolicyAction() {
        return new Action(Messages.IpsModelOverview_tooltipShowOnlyProducts, SWT.TOGGLE) {

            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("PolicyCmptType.gif"); //$NON-NLS-1$
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsModelOverview_tooltipShowOnlyPolicies;
            }

            @Override
            public void run() {
                toggleShowTypeState();
            }

        };
    }

    /**
     * Toggles the view between ProductCmpTypes and PolicyCmptTypes. The toggle action takes the
     * first selected element as input for the content provider and shows the corresponding
     * ModelOverview. If the initial content provider input was an IpsProject and the user has not
     * selected any element, the ModelOverview simply switches the view for the complete project.
     */
    private void toggleShowTypeState() {
        Object input = treeViewer.getInput();

        if (input instanceof IIpsProject) { // switch the viewShowState for project selections
            provider.toggleShowTypeState();
            treeViewer.getContentProvider().inputChanged(this.treeViewer, input, treeViewer.getInput());
        } else if (input instanceof PolicyCmptType) {
            PolicyCmptType policy = (PolicyCmptType)input;
            try {
                IProductCmptType correspondingProductCmptType = policy.findProductCmptType(policy.getIpsProject());
                if (correspondingProductCmptType != null) {
                    treeViewer.setInput(correspondingProductCmptType);
                } else {
                    return;
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        } else if (input instanceof ProductCmptType) {
            ProductCmptType product = (ProductCmptType)input;
            try {
                IPolicyCmptType correspondingPolicyCmptType = product.findPolicyCmptType(product.getIpsProject());
                if (correspondingPolicyCmptType != null) {
                    treeViewer.setInput(correspondingPolicyCmptType);
                } else {
                    return;
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        refresh();
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
            if (this.treeViewer.getInput() instanceof IType) {
                expandPaths();
            }
        }
    }

    private void expandPaths() {
        List<List<PathElement>> paths = ((ModelOverviewContentProvider)this.treeViewer.getContentProvider()).getPaths();
        TreePath[] treePaths = new TreePath[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            treePaths[i] = ModelOverview.computePath(paths.get(i));
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
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }
}
