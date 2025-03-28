/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.AbstractShowInSupportingViewPart;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceExplorer;
import org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ShowTypeState;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IType;

/**
 * This is the main class for the model structure view.
 *
 * @author noschinski2
 */
public final class ModelStructure extends AbstractShowInSupportingViewPart implements PropertyChangeListener {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modelstructure.ModelStructure"; //$NON-NLS-1$

    private static final String CONTEXT_MENU_GROUP_OPEN = "open"; //$NON-NLS-1$

    private static final String OPEN_PARENT_ASSOCIATION_TYPE_EDITOR_ACTION_ID = "OpenParentAssociationTypeEditorAction"; //$NON-NLS-1$

    private static final String MENU_GROUP_INFO = "group.info"; //$NON-NLS-1$
    private static final String MENU_GROUP_CONTENT_PROVIDER = "group.contentprovider"; //$NON-NLS-1$

    private static final String SHOW_CARDINALITIES = "show_cardinalities"; //$NON-NLS-1$
    private static final String SHOW_ROLENAMES = "show_rolenames"; //$NON-NLS-1$
    private static final String SHOW_PROJECTS = "show_projects"; //$NON-NLS-1$

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

        DropTarget dropTarget = new DropTarget(parent, DND.DROP_LINK);
        dropTarget.addDropListener(new ModelStructureDropListener(this));
        dropTarget.setTransfer(FileTransfer.getInstance());

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
        // necessary for the context menu
        getSite().setSelectionProvider(treeViewer);

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
        setFocus();
    }

    private void activateContext() {
        IContextService service = getSite().getService(IContextService.class);
        service.activateContext("org.faktorips.devtools.core.ui.views.modelExplorer.context"); //$NON-NLS-1$
    }

    private void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.add(new Separator(CONTEXT_MENU_GROUP_OPEN));
        manager.add(new OpenEditorAction(treeViewer));
        manager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));
        IpsMenuId.addDefaultGroups(manager);

        final Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(manager, treeViewer);
        MenuCleaner.addDefaultCleaner(manager);

        manager.addMenuListener(manager1 -> {
            IIpsSrcFile srcFile = getCurrentlySelectedIpsSrcFile();

            if (srcFile == null) {
                // show the menu only on non-structure nodes
                contextMenu.setVisible(false);
            } else {
                addOpenSourceAssociationTargetingTypeEditorAction(manager1);
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
                    IpsUIPlugin.getDefault()
                            .openEditor(((AssociationComponentNode)node).getTargetingType().getIpsSrcFile());
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

        return typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
    }

    private TypedSelection<IAdaptable> getSelectionFromSelectionProvider() {
        ISelectionService selectionService = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        return new TypedSelection<>(IAdaptable.class, selectionService.getSelection());
    }

    @Override
    public void setFocus() {
        treeViewer.getTree().setFocus();
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
        List<IType> result = AbstractModelStructureContentProvider.getProjectITypes(input,
                IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE);
        List<IType> projectSpecificITypes = AbstractModelStructureContentProvider.getProjectSpecificITypes(result,
                input);
        if (projectSpecificITypes.isEmpty()) {
            showInfoMessage(Messages.ModelStructure_NothingToShow_message);
        } else {
            treeViewer.setInput(input);
            showTree();
            updateView();
        }
    }

    /**
     * Configures the View to display the scope of a single IType.
     *
     * @param input the selected {@link IType}
     */
    public void showStructure(IType input) {
        toggleProductPolicyAction.setEnabled(true);
        if (input instanceof IPolicyCmptType policy) {
            setProductCmptTypeImage();
            toggledProductCmptInput = policy.findProductCmptType(policy.getIpsProject());
            if (toggledProductCmptInput == null) {
                toggleProductPolicyAction.setEnabled(false);
            }
            toggledPolicyCmptInput = (IPolicyCmptType)input;
            provider.setShowTypeState(ShowTypeState.SHOW_POLICIES);
        } else if (input instanceof IProductCmptType product) {
            setPolicyCmptTypeImage();
            toggledPolicyCmptInput = product.findPolicyCmptType(product.getIpsProject());
            if (toggledPolicyCmptInput == null) {
                toggleProductPolicyAction.setEnabled(false);
            }
            toggledProductCmptInput = (IProductCmptType)input;
            provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);
        }
        treeViewer.setInput(input);
        showTree();
        updateView();
    }

    private TreePath[] computePathsForIType(IType typeToExpand) {
        List<ComponentNode> rootElements = provider.getStoredRootElements();
        List<List<ComponentNode>> paths = new ArrayList<>();

        for (ComponentNode rootElement : rootElements) {
            computePathForIType(typeToExpand, rootElement, new ArrayList<>(), paths);
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
            computePathForIType(typeToExpand, (ComponentNode)child, new ArrayList<>(pathHead), foundPaths);
        }
    }

    private void updateView() {
        Object element = treeViewer.getInput();
        switch (element) {
            case null -> {
                // ignore
            }
            case IType type -> label.setText(type.getQualifiedName());
            case IIpsProject ipsProject -> label.setText(ipsProject.getName());
            default -> label.setText(element.toString());
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
        return new Action(Messages.ModelStructure_tooltipRefreshContents,
                IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ModelStructure_tooltipRefreshContents;
            }
        };
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

        List<IAction> actions = getContentProviderActions();
        if (actions.size() > 1) {
            for (IAction contentProviderAction : actions) {
                menuManager.appendToGroup(MENU_GROUP_CONTENT_PROVIDER, contentProviderAction);
            }
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
            contentProviderActions = new ArrayList<>();

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry
                    .getExtensionPoint(MODEL_STRUCTURE_CONTENT_PROVIDER_EXTENSION_POINT_ID);
            IExtension[] extensions = extensionPoint.getExtensions();

            for (IExtension extension : extensions) {
                IConfigurationElement[] elements = extension.getConfigurationElements();
                for (IConfigurationElement element : elements) {
                    try {
                        Object contentProvider = element.createExecutableExtension("class"); //$NON-NLS-1$
                        if (contentProvider instanceof AbstractModelStructureContentProvider) {
                            String label = element.getAttribute("label"); //$NON-NLS-1$

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
                        throw new IpsException(e);
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
                return IIpsDecorators.getDefaultImageDescriptor(PolicyCmptType.class);
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
            private final AbstractModelStructureContentProvider newProvider = contentProvider;

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
        ShowTypeState currentShowTypeState = provider.getShowTypeState();

        newProvider.removeCollectorFinishedListener(this);

        provider = newProvider;

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

        if (input instanceof IIpsProject) {
            // switch the viewShowState for project selections
            provider.toggleShowTypeState();
            treeViewer.getContentProvider().inputChanged(treeViewer, input, treeViewer.getInput());
        } else if (input instanceof IPolicyCmptType) {
            provider.setShowTypeState(ShowTypeState.SHOW_PRODUCTS);
            treeViewer.setInput(toggledProductCmptInput);
        } else if (input instanceof IProductCmptType) {
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
        toggleProductPolicyAction
                .setHoverImageDescriptor(
                        IIpsDecorators.getDefaultImageDescriptor(PolicyCmptType.class));
    }

    private void setProductCmptTypeImage() {
        /*
         * FIXME Do not misuse the HoverImageDescriptor for functionality that should be provided by
         * the normal image descriptor
         */
        toggleProductPolicyAction
                .setHoverImageDescriptor(
                        IIpsDecorators.getDefaultImageDescriptor(ProductCmptType.class));
    }

    private void refresh() {
        final Control ctrl = treeViewer.getControl();

        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }

        try {
            Runnable runnable = () -> {
                if (!treeViewer.getControl().isDisposed()) {
                    Object input = treeViewer.getInput();
                    if (input instanceof ComponentNode || input instanceof IIpsProject || input instanceof IType) {
                        treeViewer.refresh();
                    }
                }
            };

            ctrl.setRedraw(false);
            ctrl.getDisplay().syncExec(runnable);
            updateView();
        } finally {
            ctrl.setRedraw(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue().equals(provider)) {
            if (treeViewer.getInput() instanceof IType && provider.getStoredRootElements() != null) {
                expandPaths((IType)treeViewer.getInput());
            }
        }
    }

    private void expandPaths(IType typeToExpand) {
        TreePath[] treePaths = computePathsForIType(typeToExpand);

        for (TreePath treePath : treePaths) {
            treeViewer.expandToLevel(treePath, 0);
        }
        treeViewer.setSelection(new TreeSelection(treePaths));
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        memento.putBoolean(SHOW_CARDINALITIES, labelProvider.getShowCardinalities());
        memento.putBoolean(SHOW_ROLENAMES, labelProvider.getShowRolenames());
        memento.putBoolean(SHOW_PROJECTS, labelProvider.getShowProjects());

        memento.putInteger(PROVIDER_SHOW_STATE, provider.getShowTypeState().getState());

        memento.putString(INITIAL_CONTENT_PROVIDER, provider.getClass().getCanonicalName());
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
        IIpsObject ipsObject = adaptable.getAdapter(IIpsObject.class);
        if (ipsObject instanceof IType type) {
            showStructure(type);
            return true;
        } else if (ipsObject != null) {
            // it is an ipsObject but no type
            return false;
        }
        IIpsElement ipsElement = adaptable.getAdapter(IIpsElement.class);
        if (ipsElement != null) {
            showStructure(ipsElement.getIpsProject());
            return true;
        }
        IResource resource = adaptable.getAdapter(IResource.class);
        if (resource != null) {
            show(resource);
        }
        return false;
    }

    private static class ModelStructureDropListener extends IpsElementDropListener {

        private final ModelStructure modelStructure;

        public ModelStructureDropListener(ModelStructure modelStructure) {
            this.modelStructure = modelStructure;
        }

        @Override
        public void dragEnter(DropTargetEvent event) {
            dropAccept(event);
        }

        @Override
        public void drop(DropTargetEvent event) {
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
                modelStructure.show((IIpsSrcFile)transferred[0]);
            }
        }

        @Override
        public void dropAccept(DropTargetEvent event) {
            event.detail = DND.DROP_NONE;
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred == null) {
                if (super.getTransfer().isSupportedType(event.currentDataType)) {
                    event.detail = DND.DROP_LINK;
                }
                return;
            }
            if (transferred.length == 1 && transferred[0] instanceof IIpsSrcFile ipsSrcFile) {
                IIpsObject selected = ipsSrcFile.getIpsObject();
                if (InstanceExplorer.supports(selected)) {
                    event.detail = DND.DROP_LINK;
                }
            }
        }

        @Override
        public int getSupportedOperations() {
            return DND.DROP_LINK;
        }
    }

}
