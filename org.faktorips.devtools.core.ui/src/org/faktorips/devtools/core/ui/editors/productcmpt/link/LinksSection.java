/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsWorkspacePreferences;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.CollapseAllAction;
import org.faktorips.devtools.core.ui.actions.ExpandAllAction;
import org.faktorips.devtools.core.ui.commands.IpsObjectPartTester;
import org.faktorips.devtools.core.ui.editors.ICompositeWithSelectableViewer;
import org.faktorips.devtools.core.ui.editors.IpsObjectPartChangeRefreshHelper;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.SimpleOpenIpsObjectPartAction;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.LinkSectionDropListener.MoveLinkDragListener;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.producttemplate.ShowTemplatePropertyUsageViewAction;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * A section to display a product component's relations in a tree.
 * 
 * @author Thorsten Guenther
 */
public class LinksSection extends IpsSection implements ICompositeWithSelectableViewer {

    private static final String PREFERENCE_ID_SUFFIX_FILTER_EMPTY_ASSOCIATIONS = "_filterEmptyAssociations"; //$NON-NLS-1$

    private static final String ID = "org.faktorips.devtools.core.ui.editors.productcmpt.LinksSection"; //$NON-NLS-1$

    /**
     * the generation the displayed informations are based on.
     */
    private IProductCmptGeneration generation;

    private CardinalityPanel cardinalityPanel;

    /**
     * The tree viewer displaying all the relations.
     */
    private TreeViewer treeViewer;

    /**
     * The popup-Menu for the treeview if enabled.
     */
    private Menu treePopup;

    /**
     * Empty popup-Menu.
     */
    private Menu emptyMenu;

    /**
     * Listener to update the cardinality-pane on selection changes.
     */
    private SelectionChangedListener selectionChangedListener;

    private LinkSectionDropListener dropListener;

    private final ProductCmptEditor editor;

    private FilterEmptyAssociationsAction filterEmptyAssociationAction;

    private final ViewerFilter emptyAssociationFilter = new EmptyAssociationFilter();

    private final IpsWorkspacePreferences preferences = new IpsWorkspacePreferences();

    /**
     * Creates a new RelationsSection which displays relations for the given generation.
     * 
     * @param generation The base to get the relations from.
     * @param parent The composite whicht is the ui-parent for this section.
     * @param toolkit The ui-toolkit to support drawing.
     */
    public LinksSection(ProductCmptEditor editor, IProductCmptGeneration generation, Composite parent,
            UIToolkit toolkit) {
        super(ID, parent, GridData.FILL_BOTH, toolkit);
        this.editor = editor;
        ArgumentCheck.notNull(generation);
        this.generation = generation;
        initControls();
        setText(Messages.PropertiesPage_relations);
        IpsObjectPartChangeRefreshHelper.createAndInit(getActiveGeneration().getIpsObject(), getViewer());
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        Composite relationRootPanel = toolkit.createComposite(client);
        relationRootPanel.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

        boolean filterEmptyAssociations = loadFilterEmptyAssociations();
        filterEmptyAssociationAction = new FilterEmptyAssociationsAction(filterEmptyAssociations);
        LinksContentProvider contentProvider = new LinksContentProvider();

        if (hasNoLinks(contentProvider)) {
            createNoLinksLabel(client, toolkit, relationRootPanel);
        } else {
            createLinkTree(toolkit, relationRootPanel, contentProvider);
            setFilterEmptyAssociations(filterEmptyAssociations);
        }
        toolkit.getFormToolkit().paintBordersFor(relationRootPanel);
    }

    private boolean hasNoLinks(LinksContentProvider contentProvider) {
        return contentProvider.getElements(generation).length == 0;
    }

    private void createNoLinksLabel(Composite client, UIToolkit toolkit, Composite relationRootPanel) {
        GridLayout layout = (GridLayout)client.getLayout();
        layout.marginHeight = 2;
        layout.marginWidth = 1;

        relationRootPanel.setLayout(new GridLayout(1, true));
        relationRootPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        toolkit.createLabel(relationRootPanel, Messages.PropertiesPage_noRelationsDefined)
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void createLinkTree(UIToolkit toolkit, Composite relationRootPanel, LinksContentProvider contentProvider) {
        buildGridLayout(relationRootPanel);
        createTreeViewer(toolkit, relationRootPanel);
        treeViewer.setContentProvider(contentProvider);

        buildCardinalityPanel(toolkit, relationRootPanel);
        buildContextMenu();

        registerSelectionChangedListener();
        registerDoubleClickListener();
        addDragAndDropSupport();
        createTreeMessageHoverService(createLabelProvider());

        treeViewer.setInput(generation);
        treeViewer.expandAll();
    }

    private void buildGridLayout(Composite relationRootPanel) {
        relationRootPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 1;
        layout.marginHeight = 1;
        relationRootPanel.setLayout(layout);
    }

    private void createTreeViewer(UIToolkit toolkit, Composite relationRootPanel) {
        Tree tree = toolkit.getFormToolkit().createTree(relationRootPanel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.heightHint = 100;
        layoutData.widthHint = 50;
        tree.setLayoutData(layoutData);
        treeViewer = new TreeViewer(tree);
        treeViewer.setUseHashlookup(true);
    }

    private void buildCardinalityPanel(UIToolkit toolkit, Composite relationRootPanel) {
        cardinalityPanel = new CardinalityPanel(relationRootPanel, toolkit, generation.isPartOfTemplateHierarchy());
        cardinalityPanel.setDataChangeable(isDataChangeable());
    }

    /**
     * Creates the context menu for the tree viewer.
     */
    private void buildContextMenu() {
        MenuManager menuManager = new MenuManager();

        editor.getSite().registerContextMenu(ID, menuManager, treeViewer);

        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener2() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                addTemplateActions(manager);
            }

            @Override
            public void menuAboutToHide(IMenuManager manager) {
                // nothing to do
            }
        });

        // We use whitelist menu cleaner to avoid any other actions
        MenuCleaner menuCleaner = new MenuCleaner();
        menuCleaner.setWhiteListMode(true);
        menuCleaner.addFilteredPrefix(MenuCleaner.WHITE_LIST_IPS_PREFIX);
        menuCleaner.addFilteredPrefix("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
        menuManager.addMenuListener(menuCleaner);

        treePopup = menuManager.createContextMenu(treeViewer.getControl());

        treeViewer.getControl().setMenu(treePopup);

        // create empty menu for later use
        emptyMenu = new MenuManager().createContextMenu(treeViewer.getControl());
    }

    private void addTemplateActions(IMenuManager manager) {
        TypedSelection<IProductCmptLink> typedSelection = new TypedSelection<>(IProductCmptLink.class,
                treeViewer.getSelection());
        if (typedSelection.isValid()) {
            IProductCmptLink firstLink = typedSelection.getFirstElement();
            final IProductCmptLink templateLink = firstLink.findTemplateProperty(firstLink.getIpsProject());
            if (templateLink != null) {
                String text = getOpenTemplateText(templateLink);
                IAction openTemplateAction = new SimpleOpenIpsObjectPartAction<>(templateLink, text);
                manager.add(openTemplateAction);
                manager.add(new ShowTemplatePropertyUsageViewAction(templateLink,
                        Messages.CardinalityPanel_MenuItem_showUsage));
            } else if (firstLink.isPartOfTemplateHierarchy()) {
                manager.add(new ShowTemplatePropertyUsageViewAction(firstLink,
                        Messages.CardinalityPanel_MenuItem_showUsage));
            }
        }
    }

    private String getOpenTemplateText(final IProductCmptLink templateLink) {
        return NLS.bind(Messages.AttributeValueEditComposite_MenuItem_openTemplate,
                templateLink.getTemplatedValueContainer().getProductCmpt().getName());
    }

    private void registerSelectionChangedListener() {
        selectionChangedListener = new SelectionChangedListener();
        treeViewer.addSelectionChangedListener(selectionChangedListener);
    }

    /**
     * Register a double click listener to open the referenced product component in a new editor
     */
    private void registerDoubleClickListener() {
        treeViewer.addDoubleClickListener(event -> {
            TypedSelection<IAdaptable> typedSelection = new TypedSelection<>(IAdaptable.class,
                    event.getSelection());
            if (typedSelection.isValid()) {
                IProductCmptLink link = IpsObjectPartTester.castOrAdaptToPart(typedSelection.getFirstElement(),
                        IProductCmptLink.class);
                if (link != null) {
                    openLink(link);
                }
            }
        });
    }

    private void addDragAndDropSupport() {
        dropListener = new LinkSectionDropListener(this, generation);
        treeViewer.addDropSupport(DND.DROP_LINK | DND.DROP_MOVE,
                new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() }, dropListener);
        MoveLinkDragListener dragListener = dropListener.new MoveLinkDragListener(treeViewer);
        treeViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() }, dragListener);
    }

    private LinksMessageCueLabelProvider createLabelProvider() {
        final LinksMessageCueLabelProvider labelProvider = new LinksMessageCueLabelProvider(generation.getIpsProject());
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingStyledCellLabelProvider decoratedLabelProvider = new DecoratingStyledCellLabelProvider(labelProvider,
                decoManager.getLabelDecorator(), new DecorationContext());
        treeViewer.setLabelProvider(decoratedLabelProvider);
        return labelProvider;
    }

    private TreeMessageHoverService createTreeMessageHoverService(final LinksMessageCueLabelProvider labelProvider) {
        return new TreeMessageHoverService(treeViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreRuntimeException {
                return labelProvider.getMessages(element);
            }
        };
    }

    protected void setFilterEmptyAssociations(boolean exclude) {
        if (treeViewer == null) {
            return;
        }

        if (exclude) {
            treeViewer.addFilter(emptyAssociationFilter);
        } else {
            treeViewer.removeFilter(emptyAssociationFilter);
        }

        storeFilterEmptyAssociations(exclude);
    }

    private boolean loadFilterEmptyAssociations() {
        return preferences.getBoolean(ID + PREFERENCE_ID_SUFFIX_FILTER_EMPTY_ASSOCIATIONS);
    }

    private void storeFilterEmptyAssociations(boolean exclude) {
        preferences.putBoolean(ID + PREFERENCE_ID_SUFFIX_FILTER_EMPTY_ASSOCIATIONS, exclude);
    }

    private void openLink(IProductCmptLink link) {
        IProductCmpt targetProductCmpt = link.findTarget(link.getIpsProject());
        if (targetProductCmpt != null) {
            IProductCmptGeneration targetGeneration = targetProductCmpt
                    .getBestMatchingGenerationEffectiveOn(getActiveGeneration().getValidFrom());
            IpsUIPlugin.getDefault().openEditor(targetGeneration);
        }
    }

    @Override
    protected void performRefresh() {
        if (treeViewer != null) {
            treeViewer.refresh(true);
        }

        if (cardinalityPanel != null) {
            cardinalityPanel.refresh();
        }
    }

    /**
     * Creates a new link of the given association.
     * 
     * @deprecated As of 3.8
     */
    @Deprecated
    public IProductCmptLink newLink(String associationName) {
        return generation.newLink(associationName);
    }

    /**
     * Returns the currently active generation for this page.
     */
    public IProductCmptGeneration getActiveGeneration() {
        return generation;
    }

    /**
     * To get access to the informations which depend on the selections that can be made in this
     * section, only some parts can be disabled, other parts need special handling.
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (treeViewer == null) {
            // no relations defined, so no tree to disable.
            return;
        }

        if (enabled) {
            treeViewer.getTree().setMenu(treePopup);
        } else {
            treeViewer.getTree().setMenu(emptyMenu);
        }
        cardinalityPanel.update(enabled);
    }

    /**
     * Sets the selection to all links contained in the given list of {@link IProductCmptLink}s.It
     * expands all ancestors so that the given links become visible.
     * 
     * @param links A list of {@link IProductCmptLink}s that will be selected.
     */
    public void setSelection(List<IProductCmptLink> links) {
        List<LinkViewItem> linkViewItems = new ArrayList<>();
        for (IProductCmptLink productCmptLink : links) {
            LinkViewItem viewItem = new LinkViewItem(productCmptLink);
            linkViewItems.add(viewItem);
            getViewer().expandToLevel(viewItem, 0);
        }
        StructuredSelection selection = new StructuredSelection(linkViewItems);
        getViewer().setSelection(selection, true);
    }

    @Override
    public TreeViewer getViewer() {
        return treeViewer;
    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        super.populateToolBar(toolBarManager);

        if (treeViewer != null) {
            toolBarManager.add(new ExpandAllAction(treeViewer));
            toolBarManager.add(new CollapseAllAction(treeViewer));
            toolBarManager.add(filterEmptyAssociationAction);
        }
    }

    /**
     * Listener for updating the cardinality triggerd by the selection of another link.
     */
    private class SelectionChangedListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            TypedSelection<LinkViewItem> typedSelection = TypedSelection.createAnyCount(LinkViewItem.class,
                    event.getSelection());
            if (typedSelection.isValid()) {
                cardinalityPanel.setProductCmptLinkToEdit(typedSelection.getElements());
            } else {
                cardinalityPanel.setProductCmptLinkToEdit(Collections.<LinkViewItem> emptyList());
            }
            if (!isDataChangeable()) {
                cardinalityPanel.setDataChangeable(false);
            }
        }

    }

    private final class FilterEmptyAssociationsAction extends Action {

        FilterEmptyAssociationsAction(boolean exclude) {
            super(Messages.LinksSection_filterEmptyAssociations, AS_CHECK_BOX);
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
            setChecked(exclude);
        }

        @Override
        public void run() {
            setFilterEmptyAssociations(isChecked());
        }
    }

}
