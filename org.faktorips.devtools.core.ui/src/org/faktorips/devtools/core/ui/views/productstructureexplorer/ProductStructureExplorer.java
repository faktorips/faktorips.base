/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.GregorianCalendar;
import java.util.Observable;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.ui.IpsFileTransferViewerDropAdapter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.LinkDropListener;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowInstanceAction;
import org.faktorips.devtools.core.ui.internal.ICollectorFinishedListener;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDateContentProvider;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDateViewer;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 * 
 */
public class ProductStructureExplorer extends ViewPart implements ContentsChangeListener, IShowInSource,
        IResourceChangeListener {// , IPropertyChangeListener {
    /**
     * The ID of this view extension
     */
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    private static String MENU_INFO_GROUP = "goup.info"; //$NON-NLS-1$
    private static String MENU_FILTER_GROUP = "goup.filter"; //$NON-NLS-1$

    // Used for saving the current layout style in a eclipse memento.
    private static final String LAYOUT_AND_FILTER_MEMENTO = "layoutandfilter"; //$NON-NLS-1$

    private static final String CHECK_MENU_STATE = "checkedmenus"; //$NON-NLS-1$

    private static final int OPTION_REFERENCE_TABLE = 1 << 0;

    private static final int OPTION_TABLE_STRUCTURE_ROLE_NAME = 1 << 1;

    private static final int OPTION_ASSOCIATION_NODE = 1 << 2;

    private static final int OPTION_ASSOCIATIONED_CMPTS = 1 << 3;

    private TreeViewer treeViewer;
    private IIpsSrcFile file;
    private IProductCmpt productComponent;

    private ProductStructureLabelProvider labelProvider;
    private Label errormsg;

    private boolean showAssociationNode = false;
    private boolean showTableStructureRoleName = false;
    private boolean showReferencedTable = true;
    private boolean showAssociatedCmpts = true;

    private Composite viewerPanel;

    private GenerationDateViewer generationDateViewer;

    private Button prevButton;

    private Button nextButton;

    private ProductStructureContentProvider contentProvider;

    /*
     * Class to handle double clicks. Doubleclicks of ProductCmptTypeAssociationReference will be
     * ignored.
     */
    private class ProdStructExplTreeDoubleClickListener extends TreeViewerDoubleclickListener {
        public ProdStructExplTreeDoubleClickListener(TreeViewer tree) {
            super(tree);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doubleClick(DoubleClickEvent event) {
            if (getSelectedObjectFromSelection(event.getSelection()) instanceof ProductCmptTypeAssociationReference) {
                return;
            }
            super.doubleClick(event);
        }
    }

    /**
     * This drop listener is only responsible to view new objects not to drag into the structure.
     * For creating new links {@link LinkDropListener} is used.
     * 
     * @author dirmeier
     */
    private class ProductCmptDropListener extends IpsElementDropListener {

        @Override
        public void dragEnter(DropTargetEvent event) {
            dropAccept(event);
        }

        @Override
        public void drop(DropTargetEvent event) {
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
                try {
                    showStructure((IIpsSrcFile)transferred[0]);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }

        @Override
        public void dropAccept(DropTargetEvent event) {
            Object[] transferred = super.getTransferedElements(event.currentDataType);
            // in linux transferred is always null while drag action
            if (transferred == null || transferred.length > 0 && transferred[0] instanceof IIpsSrcFile
                    && isSupported((IIpsSrcFile)transferred[0])) {
                event.detail = DND.DROP_LINK;
            } else {
                event.detail = DND.DROP_NONE;
            }
        }
    }

    /**
     * Default Constructor
     */
    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);

        // add as resource listener because refactoring-actions like move or rename
        // does not cause a model-changed-event.
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
    }

    private void initMenu(IMenuManager menuManager) {
        menuManager.add(new Separator(MENU_INFO_GROUP));
        Action showAssociationNodeAction = createShowAssociationNodeAction();
        showAssociationNodeAction.setChecked(showAssociationNode);
        menuManager.appendToGroup(MENU_INFO_GROUP, showAssociationNodeAction);
        Action showAssociatedCmptsAction = createShowAssociatedCmptsAction();
        showAssociatedCmptsAction.setChecked(showAssociatedCmpts);
        menuManager.appendToGroup(MENU_INFO_GROUP, showAssociatedCmptsAction);
        Action showRoleNameAction = createShowTableRoleNameAction();
        showRoleNameAction.setChecked(showTableStructureRoleName);
        menuManager.appendToGroup(MENU_INFO_GROUP, showRoleNameAction);

        menuManager.add(new Separator(MENU_FILTER_GROUP));
        Action showReferencedTableAction = createShowReferencedTables();
        showReferencedTableAction.setChecked(showReferencedTable);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showReferencedTableAction);
    }

    private Action createShowReferencedTables() {
        return new Action(Messages.ProductStructureExplorer_menuShowReferencedTables_name, IAction.AS_CHECK_BOX) {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }

            @Override
            public void run() {
                contentProvider.setShowTableContents(!contentProvider.isShowTableContents());
                showReferencedTable = contentProvider.isShowTableContents();
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
        final ImageDescriptor refreshDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif");
        Action refreshAction = new Action() {
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
        getViewSite().getActionBars().getToolBarManager().add(retargetAction);

        toolBarManager.add(new ExpandAllAction(treeViewer));
        toolBarManager.add(new CollapseAllAction(treeViewer));

        // clear action
        toolBarManager.add(new Action("", IpsUIPlugin.getImageHandling().createImageDescriptor("Clear.gif")) {//$NON-NLS-1$
                    @Override
                    public void run() {
                        productComponent = null;
                        treeViewer.setInput(null);
                        treeViewer.refresh();
                        showEmptyMessage();
                    }

                    @Override
                    public String getToolTipText() {
                        return Messages.ProductStructureExplorer_tooltipClear;
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
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
        dropTarget.addDropListener(new ProductCmptDropListener());
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        viewerPanel = new Composite(parent, SWT.NONE);
        viewerPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewerPanel.setLayout(new GridLayout(1, true));

        String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();

        Composite adjustmentDatePanel = new Composite(viewerPanel, SWT.NONE);
        adjustmentDatePanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        adjustmentDatePanel.setLayout(new GridLayout(3, false));

        generationDateViewer = new GenerationDateViewer(adjustmentDatePanel);
        generationDateViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        generationDateViewer.getCombo().setToolTipText(
                NLS.bind(Messages.ProductStructureExplorer_selectAdjustmentToolTip, generationConceptName));
        GenerationDateContentProvider adjustmentContentProvider = new GenerationDateContentProvider();
        adjustmentContentProvider.addCollectorFinishedListener(new ICollectorFinishedListener() {

            @Override
            public void update(Observable o, Object arg) {
                generationDateViewer.setSelection(0);
            }

        });
        generationDateViewer.setContentProvider(adjustmentContentProvider);
        generationDateViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof GenerationDate) {
                    return ((GenerationDate)element).getText();
                }
                return super.getText(element);
            }
        });

        prevButton = new Button(adjustmentDatePanel, SWT.NONE);
        prevButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft_small.gif", true)); //$NON-NLS-1$
        prevButton.setToolTipText(NLS.bind(Messages.ProductStructureExplorer_prevAdjustmentToolTip,
                generationConceptName));
        prevButton.setEnabled(false);

        nextButton = new Button(adjustmentDatePanel, SWT.NONE);
        nextButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight_small.gif", true)); //$NON-NLS-1$
        nextButton.setToolTipText(NLS.bind(Messages.ProductStructureExplorer_nextAdjustmentToolTip,
                generationConceptName));
        nextButton.setEnabled(false);

        prevButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = generationDateViewer.getCombo().getSelectionIndex();
                generationDateViewer.setSelection(selectedIndex + 1);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        nextButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = generationDateViewer.getCombo().getSelectionIndex();
                generationDateViewer.setSelection(selectedIndex - 1);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
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

        final IpsFileTransferViewerDropAdapter dropListener = new LinkDropListener(treeViewer);
        treeViewer.addDropSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance(),
                TextTransfer.getInstance() }, dropListener);
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
        DecoratingStyledCellLabelProvider decoratedLabelProvider = new DecoratingStyledCellLabelProvider(labelProvider,
                decoManager.getLabelDecorator(), new DecorationContext());
        treeViewer.setLabelProvider(decoratedLabelProvider);
        labelProvider.setShowTableStructureUsageName(showTableStructureRoleName);

        treeViewer.addDoubleClickListener(new ProdStructExplTreeDoubleClickListener(treeViewer));

        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);
        menumanager.add(new Separator("open"));
        final IAction openAction = new OpenEditorAction(treeViewer);
        menumanager.add(openAction);

        menumanager.add(new Separator("edit"));
        final IAction addAction = new AddLinkAction(treeViewer);
        menumanager.add(addAction);
        menumanager.add(ActionFactory.DELETE.create(getSite().getWorkbenchWindow()));

        menumanager.add(new Separator("copy"));
        final IpsDeepCopyAction copyNewVersionAction = new IpsDeepCopyAction(getSite().getShell(), treeViewer,
                DeepCopyWizard.TYPE_NEW_VERSION);
        menumanager.add(copyNewVersionAction);
        final IpsDeepCopyAction copyProductAction = new IpsDeepCopyAction(getSite().getShell(), treeViewer,
                DeepCopyWizard.TYPE_COPY_PRODUCT);
        menumanager.add(copyProductAction);

        menumanager.add(new Separator("otherviews"));
        final IAction findReferenceAction = new FindProductReferencesAction(treeViewer);
        menumanager.add(findReferenceAction);
        final IAction showInstancesAction = new ShowInstanceAction(treeViewer);
        menumanager.add(showInstancesAction);

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                Object selectedRef = getSelectedObjectFromSelection(treeViewer.getSelection());
                boolean copyEnabled = false;
                boolean addActionEnabled = false;
                boolean enabled = isReferenceAndOpenActionSupportedForSelection(selectedRef);
                if (selectedRef instanceof IProductCmptReference) {
                    IProductCmptReference reference = (IProductCmptReference)selectedRef;
                    IIpsSrcFile srcFileToChange = reference.getWrappedIpsSrcFile();
                    addActionEnabled = IpsUIPlugin.isEditable(srcFileToChange);
                    copyEnabled = IpsPlugin.getDefault().getIpsPreferences().isWorkingModeEdit();
                }
                if (selectedRef instanceof IProductCmptTypeAssociationReference) {
                    IProductCmptTypeAssociationReference reference = (IProductCmptTypeAssociationReference)selectedRef;
                    IIpsSrcFile srcFileToChange = reference.getParent().getWrappedIpsSrcFile();
                    addActionEnabled = IpsUIPlugin.isEditable(srcFileToChange);
                }
                addAction.setEnabled(addActionEnabled);
                copyNewVersionAction.setEnabled(copyEnabled);
                copyProductAction.setEnabled(copyEnabled);
                openAction.setEnabled(enabled);
                findReferenceAction.setEnabled(enabled);
                showInstancesAction.setEnabled(enabled);
            }
        });

        Menu menu = menumanager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(menu);
        getSite().setSelectionProvider(treeViewer);

        showEmptyMessage();

        IActionBars actionBars = getViewSite().getActionBars();
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());
        hookGlobalActions();
    }

    private void hookGlobalActions() {
        IActionBars bars = getViewSite().getActionBars();
        bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), new ReferenceDeleteAction(treeViewer));
    }

    private boolean isReferenceAndOpenActionSupportedForSelection(Object selectedRef) {
        if (selectedRef == null) {
            return false;
        }
        return (selectedRef instanceof IProductCmptReference || selectedRef instanceof ProductCmptStructureTblUsageReference);
    }

    private Object getSelectedObjectFromSelection(ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        Object selectedRef = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
        return selectedRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
        // nothing to do.
    }

    /**
     * Displays the structure of the product component defined by the given file.
     * 
     * @param file The selection to display
     * @throws CoreException
     */
    public void showStructure(IIpsSrcFile file) throws CoreException {
        if (isSupported(file)) {
            showStructure((IProductCmpt)file.getIpsObject());
        }
    }

    private boolean isSupported(IIpsSrcFile file) {
        return file != null && file.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT;
    }

    /**
     * Displays the structure of the given product component.
     * 
     * @param product The product to show the structure from
     */
    public void showStructure(IProductCmpt product) {
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
        // setting the adjustment date to null updates the treeViewer content with latest adjustment
        // until the valid adjustment dates are collected
        setAdjustmentDate(null);
    }

    public void setAdjustmentDate(GenerationDate generationDate) {
        try {
            GregorianCalendar validFrom = null;
            if (generationDate != null) {
                validFrom = generationDate.getValidFrom();
            }
            IProductCmptTreeStructure structure = productComponent.getStructure(validFrom, productComponent
                    .getIpsProject());
            updateButtons();
            labelProvider.setAdjustmentDate(generationDate);
            showTreeInput(structure);
        } catch (CycleInProductStructureException e) {
            handleCircle(e);
        }
    }

    private void updateButtons() {
        int index = generationDateViewer.getCombo().getSelectionIndex();
        nextButton.setEnabled(index > 0);
        prevButton.setEnabled(index < generationDateViewer.getCombo().getItems().length - 1);
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
                            // showTreeInput(structure);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(null, treeViewer.getSelection());
        return context;
    }

    private void handleCircle(CycleInProductStructureException e) {
        IpsPlugin.log(e);
        ((GridData)treeViewer.getTree().getLayoutData()).exclude = true;
        String msg = Messages.ProductStructureExplorer_labelCircleRelation;
        IIpsElement[] cyclePath = e.getCyclePath();
        StringBuffer path = new StringBuffer();

        // don't show first element if the first elemet is no product relevant node (e.g. effective
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (file == null || !event.getIpsSrcFile().equals(file)) {
            // no contents set or event concerncs another source file - nothing to refresh.
            return;
        }
        int type = event.getEventType();
        IIpsObjectPart part = event.getPart();

        // refresh only for relevant changes
        if (part instanceof ITableContentUsage || part instanceof IProductCmptLink
                || type == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
            postRefresh();
        }
    }

    private void postRefresh() {
        getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // TODO update AdjustmentDateContent, wenn neue Anpassungsstufe hinzugekommen ist
        if (file == null) {
            return;
        }
        postRefresh();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        // IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        super.dispose();
    }

    private void showErrorMsg(String message) {
        viewerPanel.setVisible(false);
        errormsg.setText(message);
        errormsg.setVisible(true);
        ((GridData)errormsg.getLayoutData()).exclude = false;
        errormsg.getParent().layout();
    }

    private void showTreeInput(IProductCmptTreeStructure input) {
        errormsg.setVisible(false);
        ((GridData)errormsg.getLayoutData()).exclude = true;

        viewerPanel.setVisible(true);
        ((GridData)viewerPanel.getLayoutData()).exclude = false;
        viewerPanel.getParent().layout();

        treeViewer.setInput(input);
        treeViewer.expandToLevel(2);
    }

    private void showEmptyMessage() {
        showErrorMsg(Messages.ProductStructureExplorer_infoMessageEmptyView_1
                + Messages.ProductStructureExplorer_infoMessageEmptyView_2);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        int checkedMenuState = evalMenuStates();
        IMemento layout = memento.createChild(LAYOUT_AND_FILTER_MEMENTO);
        layout.putInteger(CHECK_MENU_STATE, checkedMenuState);
    }

    private void intitMenuStateFields(int checkedMenuState) {
        showReferencedTable = (checkedMenuState & OPTION_REFERENCE_TABLE) == OPTION_REFERENCE_TABLE;
        showTableStructureRoleName = (checkedMenuState & OPTION_TABLE_STRUCTURE_ROLE_NAME) == OPTION_TABLE_STRUCTURE_ROLE_NAME;
        showAssociationNode = (checkedMenuState & OPTION_ASSOCIATION_NODE) == OPTION_ASSOCIATION_NODE;
        showAssociatedCmpts = (checkedMenuState & OPTION_ASSOCIATIONED_CMPTS) == OPTION_ASSOCIATIONED_CMPTS;
    }

    private int evalMenuStates() {
        return ((showReferencedTable ? OPTION_REFERENCE_TABLE : 0) | //
                (showTableStructureRoleName ? OPTION_TABLE_STRUCTURE_ROLE_NAME : 0) | //
                (showAssociationNode ? OPTION_ASSOCIATION_NODE : 0) | (showAssociatedCmpts ? OPTION_ASSOCIATIONED_CMPTS
                : 0)); //
    }

    /**
     * @param showAssociationNode The showAssociationNode to set.
     */
    public void setShowAssociationNode(boolean showAssociationNode) {
        this.showAssociationNode = showAssociationNode;
        contentProvider.setShowAssociationNodes(showAssociationNode);
        labelProvider.setShowAssociationNodes(showAssociationNode);
    }

    /**
     * @return Returns the showAssociationNode.
     */
    public boolean isShowAssociationNode() {
        return showAssociationNode;
    }

    /**
     * @param showAssociations The showAssociations to set.
     */
    public void setShowAssociatedCmpts(boolean showAssociatedCmpts) {
        this.showAssociatedCmpts = showAssociatedCmpts;
        contentProvider.setShowAssociatedCmpts(showAssociatedCmpts);
    }

    /**
     * @return Returns the showAssociations.
     */
    public boolean isShowAssociatedCmpts() {
        return showAssociatedCmpts;
    }

}
