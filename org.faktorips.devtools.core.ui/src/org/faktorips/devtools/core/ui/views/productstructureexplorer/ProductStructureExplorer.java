/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
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
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ReferenceDropListener;
import org.faktorips.devtools.core.ui.ReferenceDropListener.IDropDoneListener;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowInstanceAction;
import org.faktorips.devtools.core.ui.internal.DeferredStructuredContentProvider;
import org.faktorips.devtools.core.ui.internal.ICollectorFinishedListener;
import org.faktorips.devtools.core.ui.internal.adjustmentdate.AdjustmentDate;
import org.faktorips.devtools.core.ui.internal.adjustmentdate.AdjustmentDateViewer;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

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

    private TreeViewer tree;
    private IIpsSrcFile file;
    private IProductCmpt productComponent;
    private ProductStructureLabelProvider labelProvider;
    private Label errormsg;

    private boolean showAssociationNode = false;
    private boolean showTableStructureRoleName = false;
    private boolean showReferencedTable = true;

    private Composite viewerPanel;

    private AdjustmentDateViewer adjustmentDateViewer;

    private Button prevButton;

    private Button nextButton;

    private ProductStructureContentProvider contentProvider;

    /*
     * Class to handle double clicks. Doubleclicks of ProductCmptTypeRelationReference will be
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
            if (getSelectedObjectFromSelection(event.getSelection()) instanceof ProductCmptTypeRelationReference) {
                return;
            }
            super.doubleClick(event);
        }
    }

    /**
     * This drop listener is only responsible to view ne objects not to drag into the structure. For
     * making new associations {@link ReferenceDropListener} is used
     * 
     * @author dirmeier
     */
    private class ProductCmptDropListener extends IpsElementDropListener {

        public void dragEnter(DropTargetEvent event) {
            dropAccept(event);
        }

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
                contentProvider.setAssociationTypeShowing(!contentProvider.isAssociationTypeShowing());
                showAssociationNode = contentProvider.isAssociationTypeShowing();
                refresh();
            }

            @Override
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipToggleRelationTypeNodes;
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
                // XXX tree.expandAll();

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

        // collapse all action
        toolBarManager.add(new Action("", IpsUIPlugin.getImageHandling().createImageDescriptor("CollapseAll.gif")) {//$NON-NLS-1$
                    @Override
                    public void run() {
                        tree.collapseAll();
                    }

                    @Override
                    public String getToolTipText() {
                        return Messages.ProductStructureExplorer_menuCollapseAll_toolkit;
                    }
                });

        // clear action
        toolBarManager.add(new Action("", IpsUIPlugin.getImageHandling().createImageDescriptor("Clear.gif")) {//$NON-NLS-1$
                    @Override
                    public void run() {
                        tree.setInput(null);
                        tree.refresh();
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

        adjustmentDateViewer = new AdjustmentDateViewer(adjustmentDatePanel);
        adjustmentDateViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        adjustmentDateViewer.getCombo().setToolTipText(
                NLS.bind(Messages.ProductStructureExplorer_selectAdjustmentToolTip, generationConceptName));
        AdjustmentDateContentProvider adjustmentContentProvider = new AdjustmentDateContentProvider();
        adjustmentContentProvider.addCollectorFinishedListener(new ICollectorFinishedListener() {

            public void update(Observable o, Object arg) {
                adjustmentDateViewer.setSelection(0);
            }

        });
        adjustmentDateViewer.setContentProvider(adjustmentContentProvider);
        adjustmentDateViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof AdjustmentDate) {
                    return ((AdjustmentDate)element).getText();
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

            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = adjustmentDateViewer.getCombo().getSelectionIndex();
                adjustmentDateViewer.setSelection(selectedIndex + 1);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        nextButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = adjustmentDateViewer.getCombo().getSelectionIndex();
                adjustmentDateViewer.setSelection(selectedIndex - 1);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        adjustmentDateViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                AdjustmentDate adjDate = adjustmentDateViewer.getSelectedDate();
                if (adjDate != null) {
                    setAdjustmentDate(adjDate);
                }
            }
        });

        tree = new TreeViewer(viewerPanel);
        tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final ReferenceDropListener dropListener = new ReferenceDropListener();
        tree.addDropSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance(),
                TextTransfer.getInstance() }, dropListener);
        dropListener.addDropDoneListener(new IDropDoneListener() {

            public void dropDone(DropTargetEvent event, List<IProductCmptLink> result, boolean srcFileWasDirty) {
                Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();
                for (IProductCmptLink link : result) {
                    IIpsSrcFile srcFile = link.getIpsSrcFile();
                    srcFiles.add(srcFile);
                }
                for (IIpsSrcFile srcFile : srcFiles) {
                    if (srcFile != null && !srcFileWasDirty && srcFile.isMutable()) {
                        try {
                            srcFile.save(false, null);
                        } catch (CoreException e) {
                            IpsPlugin.logAndShowErrorDialog(e);
                        }
                    }
                }
                refresh();

            }
        });

        contentProvider = new ProductStructureContentProvider(false);
        contentProvider.setAssociationTypeShowing(showAssociationNode);
        contentProvider.setShowTableContents(showReferencedTable);

        tree.setContentProvider(contentProvider);

        labelProvider = new ProductStructureLabelProvider();

        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingLabelProvider decoratedLabelProvider = new DecoratingLabelProvider(labelProvider, decoManager
                .getLabelDecorator());
        tree.setLabelProvider(decoratedLabelProvider);
        labelProvider.setShowTableStructureUsageName(showTableStructureRoleName);

        tree.addDoubleClickListener(new ProdStructExplTreeDoubleClickListener(tree));
        tree.expandAll();

        tree.addDragSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance() }, new IpsElementDragListener(
                tree));

        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(true);
        menumanager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                if (isReferenceAndOpenActionSupportedForSelection()) {
                    manager.add(new OpenEditorAction(tree));
                    manager.add(new FindProductReferencesAction(tree));
                    manager.add(new ShowInstanceAction(tree));
                }
            }
        });

        Menu menu = menumanager.createContextMenu(tree.getControl());
        tree.getControl().setMenu(menu);
        getSite().setSelectionProvider(tree);

        showEmptyMessage();

        IActionBars actionBars = getViewSite().getActionBars();
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());
    }

    private boolean isReferenceAndOpenActionSupportedForSelection() {
        Object selectedRef = getSelectedObjectFromSelection(tree.getSelection());
        if (selectedRef == null) {
            return false;
        }
        return (selectedRef instanceof IProductCmptReference || selectedRef instanceof ProductCmptStructureTblUsageReference);
    }

    private Object getSelectedObjectFromSelection(ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        Object selectedRef = ((IStructuredSelection)tree.getSelection()).getFirstElement();
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
        adjustmentDateViewer.setInput(product);
        adjustmentDateViewer.setSelection(0);
        // setting the adjustment date to null updates the tree content with latest adjustment
        // until the valid adjustment dates are collected
        setAdjustmentDate(null);
    }

    public void setAdjustmentDate(AdjustmentDate adjustmentDate) {
        try {
            GregorianCalendar validFrom = null;
            if (adjustmentDate != null) {
                validFrom = adjustmentDate.getValidFrom();
            }
            IProductCmptTreeStructure structure = productComponent.getStructure(validFrom, productComponent
                    .getIpsProject());
            updateButtons();
            labelProvider.setAdjustmentDate(adjustmentDate);
            showTreeInput(structure);
        } catch (CycleInProductStructureException e) {
            handleCircle(e);
        }
    }

    private void updateButtons() {
        int index = adjustmentDateViewer.getCombo().getSelectionIndex();
        nextButton.setEnabled(index > 0);
        prevButton.setEnabled(index < adjustmentDateViewer.getCombo().getItems().length - 1);
    }

    private void refresh() {
        Control ctrl = tree.getControl();

        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }

        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (!tree.getControl().isDisposed()) {
                        Object input = tree.getInput();
                        if (input instanceof IProductCmptTreeStructure) {
                            IProductCmptTreeStructure structure = (IProductCmptTreeStructure)input;
                            try {
                                structure.refresh();
                            } catch (CycleInProductStructureException e) {
                                handleCircle(e);
                                return;
                            }
                            showTreeInput(structure);
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
    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(null, tree.getSelection());
        return context;
    }

    private void handleCircle(CycleInProductStructureException e) {
        IpsPlugin.log(e);
        ((GridData)tree.getTree().getLayoutData()).exclude = true;
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
            public void run() {
                refresh();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
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

        tree.setInput(input);
        // XXX
        tree.expandAll();
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
        showReferencedTable = (checkedMenuState & 1) > 0;
        showTableStructureRoleName = (checkedMenuState & 2) > 0;
        showAssociationNode = (checkedMenuState & 4) > 0;
    }

    private int evalMenuStates() {
        return ((showReferencedTable ? 1 : 0) | (showTableStructureRoleName ? 2 : 0) | (showAssociationNode ? 4 : 0));
    }

    private static class AdjustmentDateContentProvider extends DeferredStructuredContentProvider {

        @Override
        protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
            if (inputElement instanceof IProductCmpt) {
                IProductCmpt productCmpt = (IProductCmpt)inputElement;
                try {
                    TreeSet<GregorianCalendar> validFromDates = collectValidFromDates(productCmpt, null, productCmpt
                            .getIpsProject(), monitor);
                    List<AdjustmentDate> result = new ArrayList<AdjustmentDate>();
                    GregorianCalendar lastDate = null;
                    AdjustmentDate lastAdjDate = null;
                    for (Iterator<GregorianCalendar> validFromIterator = validFromDates.iterator(); validFromIterator
                            .hasNext();) {
                        GregorianCalendar nextDate = validFromIterator.next();
                        lastAdjDate = new AdjustmentDate(nextDate, lastDate);
                        lastDate = (GregorianCalendar)nextDate.clone();
                        lastDate.add(Calendar.DATE, -1);
                        result.add(lastAdjDate);
                    }
                    return result.toArray();
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return new Object[0];
        }

        @Override
        protected String getWaitingLabel() {
            return NLS.bind(Messages.ProductStructureExplorer_collectingAdjustmentDates, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural());
        }

        private TreeSet<GregorianCalendar> collectValidFromDates(IProductCmpt productCmpt,
                Set<IProductCmpt> alreadyPassed,
                IIpsProject ipsProject,
                IProgressMonitor monitor) throws CoreException {
            if (alreadyPassed == null) {
                alreadyPassed = new HashSet<IProductCmpt>();
            }
            TreeSet<GregorianCalendar> result = new TreeSet<GregorianCalendar>(new Comparator<GregorianCalendar>() {

                public int compare(GregorianCalendar o1, GregorianCalendar o2) {
                    // descending order
                    return o2.getTime().compareTo(o1.getTime());
                }

            });
            if (productCmpt == null) {
                return result;
            }

            List<IIpsObjectGeneration> generations = productCmpt.getGenerations();
            try {
                monitor.beginTask(productCmpt.getName(), generations.size());
                for (IIpsObjectGeneration generation : generations) {
                    if (monitor.isCanceled()) {
                        return result;
                    }
                    result.add(generation.getValidFrom());
                    if (generation instanceof ProductCmptGeneration) {
                        ProductCmptGeneration prodCmptGeneration = (ProductCmptGeneration)generation;
                        IProductCmptLink[] links = prodCmptGeneration.getLinks();
                        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
                        try {
                            subMonitor.beginTask(null, links.length);
                            for (IProductCmptLink link : links) {
                                if (monitor.isCanceled()) {
                                    return result;
                                }
                                IProductCmptTypeAssociation linkAssociation = link.findAssociation(ipsProject);
                                if (linkAssociation != null && !linkAssociation.isAssoziation()) {
                                    IProductCmpt target = link.findTarget(ipsProject);
                                    if (alreadyPassed.add(target)) {
                                        IProgressMonitor recMonitor = new SubProgressMonitor(subMonitor, 1);
                                        result.addAll(collectValidFromDates(target, alreadyPassed, ipsProject,
                                                recMonitor));
                                    }
                                }
                            }
                        } finally {
                            subMonitor.done();
                        }
                    }
                }
            } finally {
                monitor.done();
            }
            return result;
        }

        @Override
        public void dispose() {

        }
    }

}
