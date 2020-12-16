/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptVRuleReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.LinkEditDialog;
import org.faktorips.devtools.core.ui.internal.ICollectorFinishedListener;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDateContentProvider;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDateViewer;
import org.faktorips.devtools.core.ui.views.AbstractShowInSupportingViewPart;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 * 
 */
public class ProductStructureExplorer extends AbstractShowInSupportingViewPart
        implements ContentsChangeListener, IIpsSrcFilesChangeListener {

    /**
     * The ID of this view extension
     */
    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    private static final String MENU_INFO_GROUP = "group.info"; //$NON-NLS-1$
    private static final String MENU_FILTER_GROUP = "group.filter"; //$NON-NLS-1$
    private static final String MENU_RULE_GROUP = "group.rule"; //$NON-NLS-1$

    // Used for saving the current layout style in a eclipse memento.
    private static final String LAYOUT_AND_FILTER_MEMENTO = "layoutandfilter"; //$NON-NLS-1$

    private static final String CHECK_MENU_STATE = "checkedmenus"; //$NON-NLS-1$

    private static final int OPTION_REFERENCE_TABLE = 1 << 0;

    private static final int OPTION_TABLE_STRUCTURE_ROLE_NAME = 1 << 1;

    private static final int OPTION_ASSOCIATION_NODE = 1 << 2;

    private static final int OPTION_ASSOCIATED_CMPTS = 1 << 3;

    private TreeViewer treeViewer;
    private IIpsSrcFile file;
    private IProductCmpt productComponent;

    private ProductStructureLabelProvider labelProvider;
    private Label errormsg;

    private boolean showAssociationNode = false;
    private boolean showTableStructureRoleName = false;
    private boolean showReferencedTable = true;
    private boolean showRules = true;
    private boolean showAssociatedCmpts = true;

    private Composite viewerPanel;

    private GenerationDateViewer generationDateViewer;

    private ProductStructureContentProvider contentProvider;

    private Action refreshAction;

    private Action clearAction;

    private ExpandAllAction expandAllAction;

    private CollapseAllAction collapseAllAction;

    private IWorkbenchAction deleteAction;

    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
        IpsPlugin.getDefault().getIpsModel().addIpsSrcFilesChangedListener(this);
        // add as resource listener because refactoring-actions like move or rename
        // would not cause a model-changed-event otherwise.
    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
    }

    private void initMenu(IMenuManager menuManager) {
        // show association node
        Action showAssociationNodeAction = createShowAssociationNodeAction();
        showAssociationNodeAction.setChecked(showAssociationNode);

        // show associated components
        Action showAssociatedCmptsAction = createShowAssociatedCmptsAction();
        showAssociatedCmptsAction.setChecked(showAssociatedCmpts);

        // show cardinalities
        Action showCardinalitiesAction = createShowCardinalitiesAction();
        showCardinalitiesAction.setChecked(true);

        // show table role name
        Action showRoleNameAction = createShowTableRoleNameAction();
        showRoleNameAction.setChecked(showTableStructureRoleName);

        // show referenced tables
        Action showReferencedTableAction = createShowReferencedTables(showRoleNameAction);
        showReferencedTableAction.setChecked(showReferencedTable);

        // show rules
        Action showRulesAction = createShowRulesAction();
        showRulesAction.setChecked(showRules);

        // info group
        menuManager.add(new Separator(MENU_INFO_GROUP));
        menuManager.appendToGroup(MENU_INFO_GROUP, showAssociationNodeAction);
        menuManager.appendToGroup(MENU_INFO_GROUP, showAssociatedCmptsAction);
        menuManager.appendToGroup(MENU_INFO_GROUP, showCardinalitiesAction);

        // filter group
        menuManager.add(new Separator(MENU_FILTER_GROUP));
        menuManager.appendToGroup(MENU_FILTER_GROUP, showReferencedTableAction);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showRoleNameAction);

        // rule group
        menuManager.add(new Separator(MENU_RULE_GROUP));
        menuManager.appendToGroup(MENU_RULE_GROUP, showRulesAction);
    }

    private Action createShowReferencedTables(final Action showRoleNameAction) {
        return new Action(Messages.ProductStructureExplorer_menuShowReferencedTables_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                contentProvider.setShowTableContents(!contentProvider.isShowTableContents());
                showReferencedTable = contentProvider.isShowTableContents();

                showRoleNameAction.setEnabled(showReferencedTable);
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_menuShowReferencedTables_tooltip;
            }
        };
    }

    private Action createShowTableRoleNameAction() {
        return new Action(Messages.ProductStructureExplorer_menuShowTableRoleName_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                labelProvider.setShowTableStructureUsageName(!labelProvider.isShowTableStructureUsageName());
                showTableStructureRoleName = labelProvider.isShowTableStructureUsageName();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_menuShowTableRoleName_tooltip;
            }
        };
    }

    private Action createShowRulesAction() {
        return new Action(Messages.ProductStructureExplorer_ShowRulesActionLabel, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                contentProvider.setShowValidationRules(!contentProvider.isShowValidationRules());
                showRules = contentProvider.isShowValidationRules();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_ShowRulesActionTooltip;
            }
        };
    }

    private Action createShowAssociationNodeAction() {
        return new Action(Messages.ProductStructureExplorer_menuShowAssociationNodes_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("ShowAssociationTypeNodes.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                setShowAssociationNode(!isShowAssociationNode());
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipToggleRelationTypeNodes;
            }
        };
    }

    private Action createShowCardinalitiesAction() {
        return new Action(Messages.ProductStructureExplorer_menuShowCardinalities_name, IAction.AS_CHECK_BOX) {
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
                return Messages.ProductStructureExplorer_tooltipToggleCardinalities;
            }
        };
    }

    private Action createShowAssociatedCmptsAction() {
        return new Action(Messages.ProductStructureExplorer_menuShowAssociatedCmpts_name, IAction.AS_CHECK_BOX) {

            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("AssociationType-Association.gif"); //$NON-NLS-1$
            }

            @Override
            public void run() {
                setShowAssociatedCmpts(!isShowAssociatedCmpts());
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipToggleAssociatedCmptsNodes;
            }
        };
    }

    private void initToolBar(IToolBarManager toolBarManager) {
        final ImageDescriptor refreshDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif"); //$NON-NLS-1$
        refreshAction = new Action() {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return refreshDescriptor;
            }

            @Override
            public void run() {
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipRefreshContents;
            }
        };
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(refreshAction);
        expandAllAction = new ExpandAllAction(treeViewer);
        toolBarManager.add(expandAllAction);
        collapseAllAction = new CollapseAllAction(treeViewer);
        toolBarManager.add(collapseAllAction);

        // clear action
        clearAction = new Action("", IpsUIPlugin.getImageHandling().createImageDescriptor("Clear.gif")) { //$NON-NLS-1$//$NON-NLS-2$
            @Override
            public void run() {
                productComponent = null;
                showTreeInput(null);
                // treeViewer.refresh();
                // showEmptyMessage();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipClear;
            }
        };
        toolBarManager.add(clearAction);
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, true));
        errormsg = new Label(parent, SWT.WRAP);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.exclude = true;
        errormsg.setLayoutData(layoutData);
        errormsg.setVisible(false);
        // dnd for label
        DropTarget dropTarget = new DropTarget(parent, DND.DROP_LINK);
        dropTarget.addDropListener(new ProductCmptDropListener(this));
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        viewerPanel = new Composite(parent, SWT.NONE);
        viewerPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewerPanel.setLayout(new GridLayout(1, true));

        Composite adjustmentDatePanel = new Composite(viewerPanel, SWT.NONE);
        adjustmentDatePanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        adjustmentDatePanel.setLayout(new GridLayout(3, false));

        generationDateViewer = new GenerationDateViewer(adjustmentDatePanel);

        GenerationDateContentProvider adjustmentContentProvider = new GenerationDateContentProvider();
        generationDateViewer.setContentProvider(adjustmentContentProvider);
        adjustmentContentProvider.addCollectorFinishedListener(new ICollectorFinishedListener() {

            @Override
            public void update(Observable o, Object arg) {
                generationDateViewer.setSelection(0);
            }

        });

        generationDateViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                GenerationDate adjDate = generationDateViewer.getSelectedDate();
                if (adjDate != null) {
                    setAdjustmentDate(adjDate);
                }
            }
        });

        treeViewer = new TreeViewer(viewerPanel);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IpsUIPlugin.getDefault().addDropSupport(treeViewer);

        // XXX Dragging is disabled because moving components within this view may be confusing
        // treeViewer.addDragSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance() },
        // new IpsElementDragListener(treeViewer));

        contentProvider = new ProductStructureContentProvider(false);
        contentProvider.setShowAssociationNodes(showAssociationNode);
        contentProvider.setShowAssociatedCmpts(showAssociatedCmpts);
        contentProvider.setShowTableContents(showReferencedTable);

        treeViewer.setContentProvider(contentProvider);

        labelProvider = new ProductStructureLabelProvider();
        labelProvider.setShowAssociationNodes(showAssociationNode);

        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        ValidationToolTipLabelProvider decoratedLabelProvider = new ValidationToolTipLabelProvider(labelProvider,
                decoManager.getLabelDecorator(), new DecorationContext());
        treeViewer.setLabelProvider(decoratedLabelProvider);
        ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);
        labelProvider.setShowTableStructureUsageName(showTableStructureRoleName);

        treeViewer.addDoubleClickListener(new ProdStructExplTreeDoubleClickListener(treeViewer));

        hookGlobalActions();
        initContextMenue();

        getSite().setSelectionProvider(treeViewer);

        IActionBars actionBars = getViewSite().getActionBars();
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());
        showEmptyMessage();
    }

    protected void initContextMenue() {
        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(true);
        menumanager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                createContextMenu(manager, (IStructuredSelection)treeViewer.getSelection());
            }

        });

        Menu menu = menumanager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menumanager, treeViewer);
    }

    private void createContextMenu(IMenuManager menumanager, IStructuredSelection selection) {
        IProductCmptStructureReference selectedRef;
        if (getSelectedObjectFromSelection(selection) instanceof IProductCmptStructureReference) {
            selectedRef = (IProductCmptStructureReference)getSelectedObjectFromSelection(selection);
        } else {
            return;
        }

        boolean openEnabled = isOpenActionSupportedForSelection(selectedRef);

        if (openEnabled) {
            menumanager.add(new Separator("open")); //$NON-NLS-1$
            final IAction openAction = new OpenEditorAction(treeViewer);
            menumanager.add(openAction);
        }

        createCardinalityContextMenu(menumanager, selectedRef);

        menumanager.add(new Separator("edit")); //$NON-NLS-1$

        IProductCmptGeneration prodCmptGenToChange = (IProductCmptGeneration)selectedRef
                .getAdapter(IProductCmptGeneration.class);
        boolean editable = IpsUIPlugin.getDefault().isGenerationEditable(prodCmptGenToChange);

        if (selectedRef instanceof IProductCmptReference) {
            menumanager.add(deleteAction);
        }

        if (selectedRef instanceof IProductCmptVRuleReference) {
            IProductCmptVRuleReference ruleRef = (IProductCmptVRuleReference)selectedRef;
            final IAction toggleRuleAction = new ToggleRuleAction(ruleRef.getValidationRuleConfig());
            toggleRuleAction.setEnabled(editable);
            menumanager.add(toggleRuleAction);
        }

        if (!(selectedRef instanceof IProductCmptVRuleReference
                || selectedRef instanceof IProductCmptTypeAssociationReference)) {
            IpsMenuId.GROUP_COPY_PRODUCTC.addSeparator(menumanager);
            IpsMenuId.GROUP_REFACTORING.addSeparator(menumanager);
            IpsMenuId.GROUP_NAVIGATE.addSeparator(menumanager);
            IpsMenuId.GROUP_GOTO.addGroupMarker(menumanager);
            IpsMenuId.GROUP_ADDITIONS.addSeparator(menumanager);
            IpsMenuId.GROUP_PROPERTIES.addSeparator(menumanager);
        }
    }

    private void createCardinalityContextMenu(IMenuManager menumanager, IProductCmptStructureReference selectedRef) {
        // change cardinalities
        if (selectedRef instanceof IProductCmptReference) {
            IProductCmptReference productCmptReference = (IProductCmptReference)selectedRef;
            if (canChangeCardinality(productCmptReference)) {
                MenuManager cardinalitiesSub = new MenuManager(Messages.ProductStructureExplorer_setCardinalities,
                        IpsUIPlugin.getImageHandling().createImageDescriptor("Cardinality.gif"), null); //$NON-NLS-1$

                // Optional Excluded
                final IAction cardinalitiesOptionalExcludedAction = cardinalitiesOptionalExcludedAction(
                        productCmptReference);

                // Optional Included
                final IAction cardinalitiesOptionalIncludedAction = cardinalitiesOptionalIncludedAction(
                        productCmptReference);

                // Mandatory
                final IAction cardinalitiesMandatoryAction = cardinalitiesMandatoryAction(productCmptReference);

                // Other
                final IAction cardinalitiesOtherAction = cardinalitiesOtherAction(productCmptReference);

                // Create menu
                cardinalitiesSub.add(cardinalitiesOptionalExcludedAction);
                cardinalitiesSub.add(cardinalitiesOptionalIncludedAction);
                cardinalitiesSub.add(cardinalitiesMandatoryAction);
                cardinalitiesSub.add(cardinalitiesOtherAction);

                selectActionMatchingCurrentLinksCardinality(cardinalitiesOptionalExcludedAction,
                        cardinalitiesOptionalIncludedAction, cardinalitiesMandatoryAction, cardinalitiesOtherAction,
                        productCmptReference.getLink());

                menumanager.add(cardinalitiesSub);
            } else {
                Action dummyCardinalityAction = createDummyCardinalityAction();
                dummyCardinalityAction.setEnabled(false);
                menumanager.add(dummyCardinalityAction);
            }
        }
    }

    protected boolean canChangeCardinality(IProductCmptReference productCmptReference) {
        boolean canChangeCardinality = false;
        IProductCmptTypeAssociationReference parent = productCmptReference.getParent();
        if (parent != null) {
            IProductCmptTypeAssociation association = parent.getAssociation();
            if (association != null) {
                canChangeCardinality = association.constrainsPolicyCmptTypeAssociation(association.getIpsProject());
            }
        }
        return canChangeCardinality;
    }

    protected void selectActionMatchingCurrentLinksCardinality(final IAction cardinalitiesOptionalExcludedAction,
            final IAction cardinalitiesOptionalIncludedAction,
            final IAction cardinalitiesMandatoryAction,
            final IAction cardinalitiesOtherAction,
            IProductCmptLink currentLink) {
        int minCardinality = currentLink.getMinCardinality();
        int maxCardinality = currentLink.getMaxCardinality();
        int defaultCardinality = currentLink.getDefaultCardinality();

        if (maxCardinality == 1) {
            if (minCardinality == 1) {
                cardinalitiesMandatoryAction.setChecked(true);
            } else if (minCardinality == 0) {
                if (defaultCardinality == 0) {
                    cardinalitiesOptionalExcludedAction.setChecked(true);
                } else if (defaultCardinality == 1) {
                    cardinalitiesOptionalIncludedAction.setChecked(true);
                }
            }
        } else {
            cardinalitiesOtherAction.setChecked(true);
        }
    }

    private IAction cardinalitiesOtherAction(IProductCmptReference productCmptReference) {
        final IAction cardinalitiesOtherAction = new CardinalityDialogAction(productCmptReference, this);
        cardinalitiesOtherAction.setText(Messages.ProductStructureExplorer_setCardinalitiesOther);
        cardinalitiesOtherAction.setToolTipText(Messages.ProductStructureExplorer_setCardinalitiesOther_ToolTip);
        return cardinalitiesOtherAction;
    }

    private IAction cardinalitiesOptionalExcludedAction(IProductCmptReference productCmptReference) {
        return cardinalitiesAction(productCmptReference, 0, 1, 0,
                Messages.ProductStructureExplorer_setCardinalitiesOptionalExcluded,
                Messages.ProductStructureExplorer_setCardinalitiesOptionalExcluded_ToolTip);
    }

    private IAction cardinalitiesOptionalIncludedAction(IProductCmptReference productCmptReference) {
        return cardinalitiesAction(productCmptReference, 0, 1, 1,
                Messages.ProductStructureExplorer_setCardinalitiesOptionalIncluded,
                Messages.ProductStructureExplorer_setCardinalitiesOptionalIncluded_ToolTip);
    }

    private IAction cardinalitiesMandatoryAction(IProductCmptReference productCmptReference) {
        return cardinalitiesAction(productCmptReference, 1, 1, 1,
                Messages.ProductStructureExplorer_setCardinalitiesMandatory,
                Messages.ProductStructureExplorer_setCardinalitiesMandatory_ToolTip);
    }

    private IAction cardinalitiesAction(IProductCmptReference productCmptReference,
            int minCardinality,
            int maxCardinality,
            int defaultCardinality,
            String textKey,
            String toolTipKey) {
        final IAction cardinalitiesAction = new CardinalityDirectAction(productCmptReference, this, minCardinality,
                maxCardinality, defaultCardinality);
        cardinalitiesAction.setText(textKey);
        cardinalitiesAction.setToolTipText(toolTipKey);
        return cardinalitiesAction;
    }

    private void hookGlobalActions() {
        deleteAction = ActionFactory.DELETE.create(getViewSite().getWorkbenchWindow());

        IActionBars bars = getViewSite().getActionBars();
        bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), new ReferenceDeleteAction(treeViewer));
    }

    private boolean isOpenActionSupportedForSelection(Object selectedRef) {
        if (selectedRef == null) {
            return false;
        }
        return !(selectedRef instanceof IProductCmptTypeAssociationReference);
    }

    private Object getSelectedObjectFromSelection(ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        Object selectedRef = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
        return selectedRef;
    }

    @Override
    public void setFocus() {
        Control control = treeViewer.getControl();
        if (control != null && !control.isDisposed()) {
            control.setFocus();
        }
    }

    /**
     * Displays the structure of the product component defined by the given file.
     * 
     * @param file The selection to display
     */
    public void showStructure(IIpsSrcFile file) {
        if (isSupported(file)) {
            showStructure((IProductCmpt)file.getIpsObject());
        }
    }

    boolean isSupported(IIpsSrcFile file) {
        return file != null && file.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT;
    }

    /**
     * Displays the structure of the given product component.
     * 
     * @param product The product to show the structure from
     */
    public void showStructure(final IProductCmpt product) {
        BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {

            @Override
            public void run() {
                if (product == null) {
                    return;
                }

                if (errormsg == null) {
                    // return if called before the explorer is shown
                    return;
                }

                productComponent = product;
                file = product.getIpsSrcFile();
                generationDateViewer.setInput(product);
                generationDateViewer.setSelection(0);
                // setting the adjustment date to null updates the treeViewer content with latest
                // adjustment
                // until the valid adjustment dates are collected
                setAdjustmentDate(null);
                generationDateViewer.updateButtons();
            }
        });
    }

    public void setAdjustmentDate(GenerationDate generationDate) {
        try {
            GregorianCalendar validFrom = null;
            if (generationDate != null) {
                validFrom = generationDate.getValidFrom();
            }
            IProductCmptTreeStructure structure = productComponent.getStructure(validFrom,
                    productComponent.getIpsProject());
            labelProvider.setAdjustmentDate(generationDate);
            showTreeInput(structure);
            treeViewer.expandToLevel(2);
        } catch (CycleInProductStructureException e) {
            handleCircle(e);
        }
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
                        if (input instanceof IProductCmptTreeStructure) {
                            IProductCmptTreeStructure structure = (IProductCmptTreeStructure)input;
                            try {
                                structure.refresh();
                            } catch (CycleInProductStructureException e) {
                                handleCircle(e);
                                return;
                            }
                            treeViewer.refresh();
                        } else {
                            showEmptyMessage();
                        }
                    }
                }
            };

            ctrl.setRedraw(false);
            ctrl.getDisplay().syncExec(runnable);
        } finally {
            ctrl.setRedraw(true);
        }
    }

    @Override
    protected ISelection getSelection() {
        return treeViewer.getSelection();
    }

    @Override
    protected boolean show(IAdaptable adaptable) {
        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)adaptable.getAdapter(IIpsSrcFile.class);
        if (ipsSrcFile == null) {
            return false;
        }
        if (isSupported(ipsSrcFile)) {
            showStructure(ipsSrcFile);
            return true;
        }
        return false;
    }

    private void handleCircle(CycleInProductStructureException e) {
        IpsPlugin.log(e);
        ((GridData)treeViewer.getTree().getLayoutData()).exclude = true;
        String msg = Messages.ProductStructureExplorer_labelCircleRelation;
        IIpsElement[] cyclePath = e.getCyclePath();
        StringBuilder path = new StringBuilder();

        // don't show first element if the first element is no product relevant node (e.g. effective
        // date info node)
        IIpsElement[] cyclePathCpy;
        if (cyclePath[0] == null) {
            cyclePathCpy = new IIpsElement[cyclePath.length - 1];
            System.arraycopy(cyclePath, 1, cyclePathCpy, 0, cyclePath.length - 1);
        } else {
            cyclePathCpy = new IIpsElement[cyclePath.length];
            System.arraycopy(cyclePath, 0, cyclePathCpy, 0, cyclePath.length);
        }

        for (int i = cyclePathCpy.length - 1; i >= 0; i--) {
            path.append(cyclePathCpy[i] == null ? "" : cyclePathCpy[i].getName()); //$NON-NLS-1$
            if (i % 2 != 0) {
                path.append(" -> "); //$NON-NLS-1$
            } else if (i % 2 == 0 && i > 0) {
                path.append(":"); //$NON-NLS-1$
            }
        }

        String message = msg + " " + path; //$NON-NLS-1$
        showErrorMsg(message);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (file == null || !event.getIpsSrcFile().equals(file)) {
            /*
             * Either no contents are set or the event concerns different source file - nothing to
             * refresh.
             */
            IIpsSrcFile eventFile = event.getIpsSrcFile();
            if (eventFile != null && event.getPart() instanceof PolicyCmptTypeAssociation) {
                // However, if an association in a policy cmpt type is modified (e.g. different
                // cardinalities are set), the tree should refresh its labels.
                postRefresh();
            }
            return;
        }
        int type = event.getEventType();
        IIpsObjectPartContainer part = event.getPart();

        // refresh only for relevant changes
        if (part instanceof ITableContentUsage || part instanceof IProductCmptLink
                || part instanceof IValidationRuleConfig || type == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
            postRefresh();
        }
    }

    @Override
    public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
        final Set<IIpsSrcFile> ipsSrcFiles = event.getChangedIpsSrcFiles();
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (file != null) {
                    for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                        if (file.getName().equals(ipsSrcFile.getName()) && !ipsSrcFile.exists()) {
                            treeViewer.setInput(null);
                            return;
                        }
                    }
                }
                refreshIfSourceFilesArePartOfCurrentStructure(ipsSrcFiles);
            }

            protected void refreshIfSourceFilesArePartOfCurrentStructure(final Set<IIpsSrcFile> ipsSrcFiles) {
                /*
                 * Source-file change may be processed by not yet initialized/visible product
                 * structure explorer. Prevent NPE. See FIPS-1040.
                 */
                if (contentProvider != null) {
                    /*
                     * Refresh only if an IPS source file in the product component structure was
                     * changed to avoid unnecessary rebuilding of the structure.
                     */
                    for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                        if (contentProvider.isIpsSrcFilePartOfStructure(ipsSrcFile)) {
                            postRefresh();
                            return;
                        }
                    }
                }
            }
        });
    }

    private void postRefresh() {
        getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    @Override
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        IpsPlugin.getDefault().getIpsModel().removeIpsSrcFilesChangedListener(this);
        super.dispose();
    }

    private void showErrorMsg(String message) {
        enableButtons(false);
        viewerPanel.setVisible(false);
        errormsg.setText(message);
        errormsg.setVisible(true);
        ((GridData)errormsg.getLayoutData()).exclude = false;
        errormsg.getParent().layout();
    }

    private void showTreeInput(IProductCmptTreeStructure input) {
        treeViewer.setInput(input);
        updateView();
    }

    public void updateView() {
        if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
            Object element = treeViewer.getInput();
            if (element == null) {
                showEmptyMessage();
            } else if (element instanceof IProductCmptTreeStructure) {
                showMessgeOrTableView(MessageTableSwitch.TABLE);
                enableButtons(true);
                treeViewer.refresh();
            }
        }
    }

    private void showEmptyMessage() {
        showErrorMsg(Messages.ProductStructureExplorer_infoMessageEmptyView_1);
    }

    private void showMessgeOrTableView(MessageTableSwitch mtSwitch) {
        boolean messageWasVisible = false;
        if (errormsg != null && !errormsg.isDisposed()) {
            messageWasVisible = errormsg.isVisible();
            errormsg.setVisible(mtSwitch.isMessage());
            ((GridData)errormsg.getLayoutData()).exclude = !mtSwitch.isMessage();
        }
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            viewerPanel.setVisible(!mtSwitch.isMessage());
            ((GridData)viewerPanel.getLayoutData()).exclude = mtSwitch.isMessage();
            viewerPanel.getParent().layout();
        }
        if (viewerPanel != null && !viewerPanel.isDisposed()) {
            viewerPanel.layout();
        }
        if (messageWasVisible) {
            setFocus();
        }
    }

    private void enableButtons(boolean status) {
        clearAction.setEnabled(status);
        refreshAction.setEnabled(status);
        expandAllAction.setEnabled(status);
        collapseAllAction.setEnabled(status);
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento layout = memento.getChild(LAYOUT_AND_FILTER_MEMENTO);
            if (layout != null) {
                Integer checkedMenuState = layout.getInteger(CHECK_MENU_STATE);
                if (checkedMenuState != null) {
                    intitMenuStateFields(checkedMenuState.intValue());
                }
            }
        }
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        int checkedMenuState = evalMenuStates();
        IMemento layout = memento.createChild(LAYOUT_AND_FILTER_MEMENTO);
        layout.putInteger(CHECK_MENU_STATE, checkedMenuState);
    }

    private void intitMenuStateFields(int checkedMenuState) {
        showReferencedTable = (checkedMenuState & OPTION_REFERENCE_TABLE) == OPTION_REFERENCE_TABLE;
        showTableStructureRoleName = (checkedMenuState
                & OPTION_TABLE_STRUCTURE_ROLE_NAME) == OPTION_TABLE_STRUCTURE_ROLE_NAME;
        showAssociationNode = (checkedMenuState & OPTION_ASSOCIATION_NODE) == OPTION_ASSOCIATION_NODE;
        showAssociatedCmpts = (checkedMenuState & OPTION_ASSOCIATED_CMPTS) == OPTION_ASSOCIATED_CMPTS;
    }

    private int evalMenuStates() {
        return ((showReferencedTable ? OPTION_REFERENCE_TABLE : 0)
                | (showTableStructureRoleName ? OPTION_TABLE_STRUCTURE_ROLE_NAME : 0)
                | (showAssociationNode ? OPTION_ASSOCIATION_NODE : 0)
                | (showAssociatedCmpts ? OPTION_ASSOCIATED_CMPTS : 0));
    }

    public void setShowAssociationNode(boolean showAssociationNode) {
        this.showAssociationNode = showAssociationNode;
        contentProvider.setShowAssociationNodes(showAssociationNode);
        labelProvider.setShowAssociationNodes(showAssociationNode);
    }

    public boolean isShowAssociationNode() {
        return showAssociationNode;
    }

    public void setShowAssociatedCmpts(boolean showAssociatedCmpts) {
        this.showAssociatedCmpts = showAssociatedCmpts;
        contentProvider.setShowAssociatedCmpts(showAssociatedCmpts);
    }

    public boolean isShowAssociatedCmpts() {
        return showAssociatedCmpts;
    }

    public void expandToLevel(int level) {
        treeViewer.expandToLevel(level);
    }

    /**
     * Searches the shown structure for the given elements. Every element of the structure
     * representing one of the given elements is selected.
     */
    public void setSelection(final List<IProductCmpt> toBeSelected) {
        if (toBeSelected.isEmpty()) {
            return;
        }

        Object input = treeViewer.getInput();
        if (!(input instanceof IProductCmptTreeStructure)) {
            return;
        }

        IProductCmptTreeStructure struct = (IProductCmptTreeStructure)input;
        List<IProductCmptReference> refs = struct.findReferencesFor(toBeSelected);
        IStructuredSelection selection = new StructuredSelection(refs);

        treeViewer.setSelection(selection, true);
    }

    private Action createDummyCardinalityAction() {
        return new Action(Messages.ProductStructureExplorer_setCardinalities,
                IpsUIPlugin.getImageHandling().createImageDescriptor("Cardinality.gif")) { //$NON-NLS-1$

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_setCardinalities;
            }
        };
    }

    private abstract static class AbstractCardinalityAction extends Action {

        private final ProductStructureExplorer productStructureExplorer;

        private final IProductCmptReference productCmptReference;

        protected AbstractCardinalityAction(IProductCmptReference productCmptReference,
                ProductStructureExplorer productStructureExplorer) {
            this.productCmptReference = productCmptReference;
            this.productStructureExplorer = productStructureExplorer;
        }

        public ProductStructureExplorer getProductStructureExplorer() {
            return productStructureExplorer;
        }

        public IProductCmptReference getProductCmptReference() {
            return productCmptReference;
        }

        @Override
        public void run() {
            IIpsSrcFile ipsSrcFile = productCmptReference.getLink().getIpsSrcFile();
            boolean dirty = ipsSrcFile.isDirty();

            changeCardinality();

            if (!dirty && ipsSrcFile.isDirty()) {
                try {
                    productCmptReference.getLink().getIpsSrcFile().save(false, new NullProgressMonitor());
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
            productStructureExplorer.refresh();
        }

        protected abstract void changeCardinality();

    }

    private static class CardinalityDialogAction extends AbstractCardinalityAction {

        protected CardinalityDialogAction(IProductCmptReference productCmptReference,
                ProductStructureExplorer productStructureExplorer) {
            super(productCmptReference, productStructureExplorer);
        }

        @Override
        protected void changeCardinality() {
            Shell shell = getProductStructureExplorer().getViewSite().getShell();
            LinkEditDialog dialog = new LinkEditDialog(getProductCmptReference().getLink(), shell);
            dialog.setTargetSelectionEnabled(false);
            dialog.open();
        }
    }

    private static class CardinalityDirectAction extends AbstractCardinalityAction {

        private final int minCardinality;
        private final int maxCardinality;
        private final int defaultCardinality;

        protected CardinalityDirectAction(IProductCmptReference productCmptReference, ProductStructureExplorer pse,
                int minCardinality, int maxCardinality, int defaultCardinality) {
            super(productCmptReference, pse);
            this.minCardinality = minCardinality;
            this.maxCardinality = maxCardinality;
            this.defaultCardinality = defaultCardinality;
        }

        @Override
        protected void changeCardinality() {
            IProductCmptReference productCmptReference = getProductCmptReference();
            productCmptReference.getLink().setMinCardinality(minCardinality);
            productCmptReference.getLink().setMaxCardinality(maxCardinality);
            productCmptReference.getLink().setDefaultCardinality(defaultCardinality);
        }
    }

    /**
     * Class to handle double clicks. Doubleclicks of ProductCmptTypeAssociationReference will be
     * ignored.
     */
    private class ProdStructExplTreeDoubleClickListener extends TreeViewerDoubleclickListener {

        public ProdStructExplTreeDoubleClickListener(TreeViewer tree) {
            super(tree);
        }

        @Override
        public void doubleClick(DoubleClickEvent event) {
            if (getSelectedObjectFromSelection(event.getSelection()) instanceof IProductCmptReference) {
                IProductCmptReference selectedProductCmptReference = (IProductCmptReference)getSelectedObjectFromSelection(
                        event.getSelection());
                GenerationDate selectedGenerationDate = generationDateViewer.getSelectedDate();
                IProductCmptGeneration generationToBeOpened = selectedProductCmptReference.getProductCmpt()
                        .getBestMatchingGenerationEffectiveOn(selectedGenerationDate.getValidFrom());

                IpsUIPlugin.getDefault().openEditor(generationToBeOpened);
            }
            if (getSelectedObjectFromSelection(
                    event.getSelection()) instanceof IProductCmptStructureTblUsageReference) {
                IProductCmptStructureTblUsageReference selectedTableReference = (IProductCmptStructureTblUsageReference)getSelectedObjectFromSelection(
                        event.getSelection());
                try {
                    ITableContents tableUsage = selectedTableReference.getTableContentUsage()
                            .findTableContents(selectedTableReference.getTableContentUsage().getIpsProject());
                    if (tableUsage != null) {
                        IpsUIPlugin.getDefault().openEditor(tableUsage);
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }

    }

    private static enum MessageTableSwitch {
        MESSAGE,
        TABLE;

        public boolean isMessage() {
            return equals(MESSAGE);
        }
    }

}
