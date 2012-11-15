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

package org.faktorips.devtools.core.ui.views.modelstructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.ISelection;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
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
import org.faktorips.devtools.core.ui.views.AbstractShowInSupportingViewPart;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerContextMenuBuilder;
import org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ShowTypeState;

/**
 * This is the main class for the model structure view.
 * 
 * @author noschinski2
 */
public final class ModelStructure extends AbstractShowInSupportingViewPart implements ICollectorFinishedListener {

    private static final String CONTEXT_MENU_GROUP_OPEN = "open"; //$NON-NLS-1$

    private static final String OPEN_PARENT_ASSOCIATION_TYPE_EDITOR_ACTION_ID = "OpenParentAssociationTypeEditorAction"; //$NON-NLS-1$

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modelstructure.ModelStructure"; //$NON-NLS-1$

    private static final String MENU_GROUP_INFO = "group.info"; //$NON-NLS-1$
    private static final String MENU_GROUP_CONTENT_PROVIDER = "group.contentprovider"; //$NON-NLS-1$

    private static final String SHOW_CARDINALITIES = "show_cardinalities"; //$NON-NLS-1$
    private static final String SHOW_ROLENAMES = "show_rolenames"; //$NON-NLS-1$
    private static final String SHOW_PROJECTS = "show_projects"; //$NON-NLS-1$

    private static final String PRODUCT_CMPT_TYPE_IMAGE = "ProductCmptType.gif"; //$NON-NLS-1$
    private static final String POLICY_CMPT_TYPE_IMAGE = "PolicyCmptType.gif"; //$NON-NLS-1$
    private static final String CARDINALITY_IMAGE = "Cardinality.gif"; //$NON-NLS-1$
    private static final String PROVIDER_SHOW_STATE = "show_state"; //$NON-NLS-1$
    private static final String INITIAL_CONTENT_PROVIDER = "initial_content_provider"; //$NON-NLS-1$

    private static final String MODEL_STRUCTURE_CONTENT_PROVIDER_EXTENSION_POINT_ID = "org.faktorips.devtools.core.ui.modelStructureContentProvider"; //$NON-NLS-1$

    private static final boolean DEFAULT_SHOW_CARDINALITIES = true;
    private static final boolean DEFAULT_SHOW_ROLENAMES = true;
    private static final boolean DEFAULT_SHOW_PROJECTNAMES = false;

    private IPolicyCmptType toggledPolicyCmptInput;
    private IProductCmptType toggledProductCmptInput;

    private Composite panel;
    private TreeViewer treeViewer;
    private final UIToolkit uiToolkit = new UIToolkit(null);
    private Label label;
    private Label infoMessageLabel;

    private ModelStructureLabelProvider labelProvider;
    private AbstractModelStructureContentProvider provider;

    private Action toggleProductPolicyAction;
    private ExpandAllAction expandAllAction;
    private CollapseAllAction collapseAllAction;

    private boolean showCardinalities;
    private boolean showRolenames;
    private boolean showProjectnames;
    private ShowTypeState providerShowState;

    private Action showCardinalitiesAction;
    private Action showRoleNameAction;
    private Action showProjectsAction;
    private Action refreshAction;

    private List<IAction> contentProviderActions;

    private String initialContentProvider;

    @Override
    public void createPartControl(Composite parent) {
        panel = uiToolkit.createGridComposite(parent, 1, false, true, new GridData(SWT.FILL, SWT.FILL, true, true));
        label = uiToolkit.createLabel(panel, "", SWT.LEFT, new GridData(SWT.FILL, SWT.FILL, //$NON-NLS-1$
                true, false));

        treeViewer = new TreeViewer(panel);

        // initializes with a default content provider
        provider = new ModelStructureInheritAssociationsContentProvider();

        initContentProviders();
        // set default show state and the according toggle-button image
        provider.setShowTypeState(providerShowState);
        treeViewer.setContentProvider(provider);

        ColumnViewerToolTipSupport.enableFor(treeViewer);
        labelProvider = new ModelStructureLabelProvider();
        IDecoratorManager decoratorManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        // decoratorManager.setEnabled(decoratorId, enabled)

        DecoratingStyledCellLabelProvider decoratingLabelProvider = new ModelStructureDecoratingStyledCellLabelProvider(
                labelProvider, decoratorManager.getLabelDecorator(), DecorationContext.DEFAULT_CONTEXT);

        treeViewer.setLabelProvider(decoratingLabelProvider);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        getSite().setSelectionProvider(treeViewer); // necessary for the context menu

        // initialize the empty message
        infoMessageLabel = uiToolkit.createLabel(panel, "", SWT.WRAP, new GridData(SWT.FILL, SWT.FILL, //$NON-NLS-1$
                true, true));

        activateContext();
        createContextMenu();
        initMenu();
        initToolBar();

        treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));
        provider.addCollectorFinishedListener(this);

        showInfoMessage(Messages.ModelStructure_emptyMessage);
    }

    private void showInfoMessage(String message) {
        infoMessageLabel.setText(message);
        enableButtons(false);
        if (!infoMessageLabel.isDisposed()) {
            infoMessageLabel.setVisible(true);
            ((GridData)infoMessageLabel.getLayoutData()).exclude = false;
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
        if (!infoMessageLabel.isDisposed()) {
            infoMessageLabel.setVisible(false);
            ((GridData)infoMessageLabel.getLayoutData()).exclude = true;
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
        this.setFocus();
    }

    private void activateContext() {
        IContextService service = (IContextService)getSite().getService(IContextService.class);
        service.activateContext("org.faktorips.devtools.core.ui.views.modelExplorer.context"); //$NON-NLS-1$
    }

    private void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.add(new Separator(CONTEXT_MENU_GROUP_OPEN));
        manager.add(new OpenEditorAction(treeViewer));
        manager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
        manager.add(new GroupMarker(ModelExplorerContextMenuBuilder.GROUP_NAVIGATE));

        final Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, treeViewer);
        MenuCleaner.addDefaultCleaner(manager);

        manager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                IIpsSrcFile srcFile = getCurrentlySelectedIpsSrcFile();

                if (srcFile == null) { // show the menu only on non-structure nodes
                    contextMenu.setVisible(false);
                } else {
                    addOpenSourceAssociationTargetingTypeEditorAction(manager);
                }
            }

        });

    }

    private void addOpenSourceAssociationTargetingTypeEditorAction(IMenuManager manager) {
        final ComponentNode node = getCurrentlySelectedComponentNode();
        manager.remove(OPEN_PARENT_ASSOCIATION_TYPE_EDITOR_ACTION_ID);
        if (node instanceof AssociationComponentNode) {
            Action openParentAssociationTypeEditorAction = new Action() {

                @Override
                public String getId() {
                    return OPEN_PARENT_ASSOCIATION_TYPE_EDITOR_ACTION_ID;
                }

                @Override
                public void run() {
                    IpsUIPlugin.getDefault().openEditor(
                            ((AssociationComponentNode)node).getTargetingType().getIpsSrcFile());
                }

            };
            openParentAssociationTypeEditorAction
                    .setText(Messages.ModelStructure_contextMenuOpenAssociationTargetingTypeEditor
                            + ((AssociationComponentNode)node).getTargetingType().getName());
            manager.appendToGroup(CONTEXT_MENU_GROUP_OPEN, openParentAssociationTypeEditorAction);
        }
    }

    private ComponentNode getCurrentlySelectedComponentNode() {
        TypedSelection<IAdaptable> typedSelection = getSelectionFromSelectionProvider();
        if (typedSelection == null || !typedSelection.isValid()) {
            return null;
        }

        return (ComponentNode)typedSelection.getFirstElement();
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
    public void showStructure(IIpsProject input) {
        List<IType> result = AbstractModelStructureContentProvider.getProjectITypes(input, new IpsObjectType[] {
                IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE });
        List<IType> projectSpecificITypes = AbstractModelStructureContentProvider.getProjectSpecificITypes(result,
                input);
        if (projectSpecificITypes.isEmpty()) {
            showInfoMessage(Messages.ModelStructure_NothingToShow_message);
        } else {
            this.treeViewer.setInput(input);
            this.showTree();
            this.updateView();
        }
    }

    /**
     * Configures the View to display the scope of a single IType.
     * 
     * @param input the selected {@link IType}
     */
    public void showStructure(IType input) {
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
                this.provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
            } else if (input instanceof ProductCmptType) {
                setPolicyCmptTypeImage();
                IProductCmptType product = (IProductCmptType)input;
                toggledPolicyCmptInput = product.findPolicyCmptType(product.getIpsProject());
                if (toggledPolicyCmptInput == null) {
                    toggleProductPolicyAction.setEnabled(false);
                }
                toggledProductCmptInput = (ProductCmptType)input;
                this.provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        this.treeViewer.setInput(input);
        this.showTree();
        this.updateView();
    }

    private TreePath[] computePathsForIType(IType typeToExpand) {
        List<ComponentNode> rootElements = this.provider.getStoredRootElements();
        List<List<ComponentNode>> paths = new ArrayList<List<ComponentNode>>();

        for (ComponentNode rootElement : rootElements) {
            computePathForIType(typeToExpand, rootElement, new ArrayList<ComponentNode>(), paths);
        }

        TreePath[] treePaths = new TreePath[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            treePaths[i] = new TreePath(paths.get(i).toArray());
        }

        return treePaths;
    }

    private void computePathForIType(IType typeToExpand,
            ComponentNode position,
            List<ComponentNode> pathHead,
            List<List<ComponentNode>> foundPaths) {
        pathHead.add(position);
        if (position.getValue().equals(typeToExpand)) {
            foundPaths.add(pathHead);
        }

        Object[] children = provider.getChildren(position);
        for (Object child : children) {
            computePathForIType(typeToExpand, (ComponentNode)child, new ArrayList<ComponentNode>(pathHead), foundPaths);
        }
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

        refreshAction = createRefreshAction();
        toolBarManager.add(refreshAction);

        if (providerShowState == ShowTypeState.SHOW_POLICIES) {
            setProductCmptTypeImage();
        } else {
            setPolicyCmptTypeImage();
        }
    }

    private Action createRefreshAction() {
        // refresh action
        Action newRefreshAction = new Action(Messages.ModelStructure_tooltipRefreshContents, IpsUIPlugin
                .getImageHandling().createImageDescriptor("Refresh.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ModelStructure_tooltipRefreshContents;
            }
        };
        return newRefreshAction;
    }

    private void initMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(new Separator(MENU_GROUP_INFO));

        showCardinalitiesAction = createShowCardinalitiesAction();
        labelProvider.setShowCardinalities(showCardinalities);
        showCardinalitiesAction.setChecked(showCardinalities);

        showRoleNameAction = createShowRoleNameAction();
        labelProvider.setShowRolenames(showRolenames);
        showRoleNameAction.setChecked(showRolenames);

        showProjectsAction = createShowProjectsAction();
        labelProvider.setShowProjects(showProjectnames);
        showProjectsAction.setChecked(showProjectnames);

        menuManager.appendToGroup(MENU_GROUP_INFO, showCardinalitiesAction);
        menuManager.appendToGroup(MENU_GROUP_INFO, showRoleNameAction);
        menuManager.appendToGroup(MENU_GROUP_INFO, showProjectsAction);

        menuManager.add(new Separator(MENU_GROUP_CONTENT_PROVIDER));

        for (IAction contentProviderAction : getContentProviderActions()) {
            menuManager.appendToGroup(MENU_GROUP_CONTENT_PROVIDER, contentProviderAction);
        }
    }

    private List<IAction> getContentProviderActions() {
        initContentProviders();
        return contentProviderActions;
    }

    /**
     * Initializes the set of content providers, if it has not been initialized yet.
     */
    private void initContentProviders() {
        if (contentProviderActions == null || contentProviderActions.isEmpty()) {
            contentProviderActions = new ArrayList<IAction>();

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry
                    .getExtensionPoint(MODEL_STRUCTURE_CONTENT_PROVIDER_EXTENSION_POINT_ID);
            IExtension[] extensions = extensionPoint.getExtensions();

            for (int i = 0; i < extensions.length; i++) {
                IConfigurationElement[] elements = extensions[i].getConfigurationElements();
                for (int j = 0; j < elements.length; j++) {
                    try {
                        Object contentProvider = elements[j].createExecutableExtension("class"); //$NON-NLS-1$
                        if (contentProvider instanceof AbstractModelStructureContentProvider) {
                            String label = elements[j].getAttribute("label"); //$NON-NLS-1$

                            Action contentProviderAction = createContentProviderAction(label,
                                    (AbstractModelStructureContentProvider)contentProvider);
                            contentProviderActions.add(contentProviderAction);
                            if (initialContentProvider == null) {
                                initialContentProvider = contentProvider.getClass().getCanonicalName();
                            }
                            if (initialContentProvider.equals(contentProvider.getClass().getCanonicalName())) {
                                provider = (AbstractModelStructureContentProvider)contentProvider;
                                contentProviderAction.setChecked(true);
                            }
                        }
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
        }

    }

    private Action createShowCardinalitiesAction() {
        return new Action(Messages.ModelStructure_menuShowCardinalities_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor(CARDINALITY_IMAGE);
            }

            @Override
            public void run() {
                labelProvider.toggleShowCardinalities();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ModelStructure_menuShowCardinalities_tooltip;
            }
        };
    }

    private Action createShowRoleNameAction() {
        return new Action(Messages.ModelStructure_menuShowRoleName_name, IAction.AS_CHECK_BOX) {
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
                return Messages.ModelStructure_menuShowRoleName_tooltip;
            }
        };
    }

    private Action createShowProjectsAction() {
        return new Action(Messages.ModelStructure_menuShowProjects_name, IAction.AS_CHECK_BOX) {
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
                return Messages.ModelStructure_menuShowProjects_tooltip;
            }
        };
    }

    private Action createToggleProductPolicyAction() {
        return new Action(Messages.ModelStructure_tooltipToggleButton, SWT.DEFAULT) {

            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor(POLICY_CMPT_TYPE_IMAGE);
            }

            @Override
            public String getToolTipText() {
                return Messages.ModelStructure_tooltipToggleButton;
            }

            @Override
            public void run() {
                toggleShowTypeState();
            }

        };
    }

    private Action createContentProviderAction(final String label,
            final AbstractModelStructureContentProvider contentProvider) {
        return new Action(label, IAction.AS_RADIO_BUTTON) {
            AbstractModelStructureContentProvider newProvider = contentProvider;

            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public String getToolTipText() {
                return label;
            }

            @Override
            public void run() {
                switchContentProvider(newProvider);
            }
        };
    }

    private void enableButtons(boolean state) {
        expandAllAction.setEnabled(state);
        collapseAllAction.setEnabled(state);
        // toggleProductPolicyAction.setEnabled(state);

        showCardinalitiesAction.setEnabled(state);
        showProjectsAction.setEnabled(state);
        showRoleNameAction.setEnabled(state);

        refreshAction.setEnabled(state);
    }

    private void switchContentProvider(AbstractModelStructureContentProvider newProvider) {
        Object input = treeViewer.getInput();
        ShowTypeState currentShowTypeState;
        currentShowTypeState = provider.getShowTypeState();

        newProvider.removeCollectorFinishedListener(this);

        this.provider = newProvider;

        treeViewer.setContentProvider(newProvider);
        newProvider.setShowTypeState(currentShowTypeState);
        newProvider.addCollectorFinishedListener(this);
        treeViewer.setInput(input);
        refresh();
    }

    /**
     * Toggles the view between ProductCmpTypes and PolicyCmptTypes. The toggle action takes
     * originally selected element as input for the content provider and shows the corresponding
     * ModelStructure. If the initial content provider input was an IpsProject and the user has not
     * selected any element, the ModelStructure simply switches the view for the complete project.
     */
    private void toggleShowTypeState() {
        Object input = treeViewer.getInput();

        if (input instanceof IIpsProject) { // switch the viewShowState for project selections
            provider.toggleShowTypeState();
            treeViewer.getContentProvider().inputChanged(this.treeViewer, input, treeViewer.getInput());
        } else if (input instanceof PolicyCmptType) {
            provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);
            treeViewer.setInput(toggledProductCmptInput);
        } else if (input instanceof ProductCmptType) {
            provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
            treeViewer.setInput(toggledPolicyCmptInput);
        }
        if (provider.getShowTypeState() == ShowTypeState.SHOW_POLICIES) {
            setProductCmptTypeImage();
        } else {
            setPolicyCmptTypeImage();
        }
        refresh();
    }

    private void setPolicyCmptTypeImage() {
        /*
         * FIXME Do not misuse the HoverImageDescriptor for functionality that should be provided by
         * the normal image descriptor
         */
        toggleProductPolicyAction.setHoverImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                POLICY_CMPT_TYPE_IMAGE));
    }

    private void setProductCmptTypeImage() {
        /*
         * FIXME Do not misuse the HoverImageDescriptor for functionality that should be provided by
         * the normal image descriptor
         */
        toggleProductPolicyAction.setHoverImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                PRODUCT_CMPT_TYPE_IMAGE));
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
            if (this.treeViewer.getInput() instanceof IType && provider.getStoredRootElements() != null) {
                expandPaths((IType)this.treeViewer.getInput());
            }
        }
    }

    private void expandPaths(IType typeToExpand) {
        TreePath[] treePaths = computePathsForIType(typeToExpand);

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

        memento.putInteger(PROVIDER_SHOW_STATE, this.provider.getShowTypeState().getState());

        memento.putString(INITIAL_CONTENT_PROVIDER, this.provider.getClass().getCanonicalName());
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        // initialize the parameter stored in the memento with meaningful values
        showCardinalities = true;
        showRolenames = true;
        showProjectnames = false;
        providerShowState = ShowTypeState.SHOW_POLICIES;

        if (memento != null) {
            // initialize the label settings
            Boolean showcard = memento.getBoolean(SHOW_CARDINALITIES);
            showCardinalities = showcard == null ? DEFAULT_SHOW_CARDINALITIES : showcard;

            Boolean showRoles = memento.getBoolean(SHOW_ROLENAMES);
            showRolenames = showRoles == null ? DEFAULT_SHOW_ROLENAMES : showRoles;

            Boolean showProjects = memento.getBoolean(SHOW_PROJECTS);
            showProjectnames = showProjects == null ? DEFAULT_SHOW_PROJECTNAMES : showProjects;

            initialContentProvider = memento.getString(INITIAL_CONTENT_PROVIDER);

            // initialize the provider show state
            Integer state = memento.getInteger(PROVIDER_SHOW_STATE);
            if (state != null && state == ShowTypeState.SHOW_PRODUCTS.getState()) {
                providerShowState = ShowTypeState.SHOW_PRODUCTS;
            }
        }
    }

    @Override
    protected ISelection getSelection() {
        return treeViewer.getSelection();
    }

    @Override
    protected boolean show(IAdaptable adaptable) {
        IIpsObject ipsObject = (IIpsObject)adaptable.getAdapter(IIpsObject.class);
        if (ipsObject instanceof IType) {
            IType type = (IType)ipsObject;
            showStructure(type);
            return true;
        } else if (ipsObject != null) {
            // it is an ipsObject but no type
            return false;
        }
        IIpsElement ipsElement = (IIpsElement)adaptable.getAdapter(IIpsElement.class);
        if (ipsElement != null) {
            showStructure(ipsElement.getIpsProject());
            return true;
        }
        IResource resource = (IResource)adaptable.getAdapter(IResource.class);
        if (resource != null) {
            show(resource);
        }
        return false;
    }
}
