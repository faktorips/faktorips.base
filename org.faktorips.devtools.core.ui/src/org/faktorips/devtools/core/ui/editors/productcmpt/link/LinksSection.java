/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.commands.IpsObjectPartTester;
import org.faktorips.devtools.core.ui.editors.ICompositeWithSelectableViewer;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.LinkSectionDropListener.MoveLinkDragListener;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

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

    /**
     * Creates a new RelationsSection which displays relations for the given generation.
     * 
     * @param generation The base to get the relations from.
     * @param parent The composite whicht is the ui-parent for this section.
     * @param toolkit The ui-toolkit to support drawing.
     */
    public LinksSection(ProductCmptEditor editor, IProductCmptGeneration generation, Composite parent, UIToolkit toolkit) {
        super(ID, parent, GridData.FILL_BOTH, toolkit);
        this.editor = editor;
        ArgumentCheck.notNull(generation);
        this.generation = generation;
        initControls();
        setText(Messages.PropertiesPage_relations);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        Composite relationRootPane = toolkit.createComposite(client);
        relationRootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

        boolean filterEmptyAssociations = loadFilterEmptyAssociations();
        LinksContentProvider contentProvider = new LinksContentProvider();
        filterEmptyAssociationAction = new FilterEmptyAssociationsAction(filterEmptyAssociations);

        if (contentProvider.getElements(generation).length == 0) {
            GridLayout layout = (GridLayout)client.getLayout();
            layout.marginHeight = 2;
            layout.marginWidth = 1;

            relationRootPane.setLayout(new GridLayout(1, true));
            relationRootPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            toolkit.createLabel(relationRootPane, Messages.PropertiesPage_noRelationsDefined).setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));
        } else {
            relationRootPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 1;
            layout.marginHeight = 1;
            relationRootPane.setLayout(layout);

            Tree tree = toolkit.getFormToolkit().createTree(relationRootPane, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
            GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
            layoutData.heightHint = 100;
            layoutData.widthHint = 50;
            tree.setLayoutData(layoutData);

            selectionChangedListener = new SelectionChangedListener();

            treeViewer = new TreeViewer(tree);
            treeViewer.setContentProvider(contentProvider);
            setFilterEmptyAssociations(filterEmptyAssociations);

            treeViewer.addSelectionChangedListener(selectionChangedListener);
            dropListener = new LinkSectionDropListener(editor, treeViewer, generation);
            treeViewer.addDropSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance(),
                    TextTransfer.getInstance() }, dropListener);
            MoveLinkDragListener dragListener = dropListener.new MoveLinkDragListener(treeViewer);
            treeViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() }, dragListener);

            final LinksMessageCueLabelProvider labelProvider = new LinksMessageCueLabelProvider(
                    generation.getIpsProject());
            IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
            DecoratingStyledCellLabelProvider decoratedLabelProvider = new DecoratingStyledCellLabelProvider(
                    labelProvider, decoManager.getLabelDecorator(), new DecorationContext());
            treeViewer.setLabelProvider(decoratedLabelProvider);
            treeViewer.setInput(generation);

            new TreeMessageHoverService(treeViewer) {
                @Override
                protected MessageList getMessagesFor(Object element) throws CoreException {
                    return labelProvider.getMessages(element);
                }
            };

            treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
            treeViewer.expandAll();

            buildContextMenu();

            cardinalityPanel = new CardinalityPanel(relationRootPane, toolkit);
            cardinalityPanel.setDataChangeable(isDataChangeable());
            cardinalityPanel.deactivate();

            addFocusControl(treeViewer.getTree());
            registerDoubleClickListener();
            treeViewer.refresh(true);
        }
        toolkit.getFormToolkit().paintBordersFor(relationRootPane);
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
        IPreferencesService preferencesService = Platform.getPreferencesService();
        String pluginId = IpsUIPlugin.getDefault().getBundle().getSymbolicName();
        String preferenceId = ID + PREFERENCE_ID_SUFFIX_FILTER_EMPTY_ASSOCIATIONS;

        return preferencesService.getBoolean(pluginId, preferenceId, false, null);
    }

    private void storeFilterEmptyAssociations(boolean exclude) {
        String pluginId = IpsUIPlugin.getDefault().getBundle().getSymbolicName();
        IEclipsePreferences node = new InstanceScope().getNode(pluginId);
        String preferenceId = ID + PREFERENCE_ID_SUFFIX_FILTER_EMPTY_ASSOCIATIONS;
        node.putBoolean(preferenceId, exclude);
    }

    /**
     * Register a double click listener to open the referenced product component in a new editor
     */
    private void registerDoubleClickListener() {
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                TypedSelection<IAdaptable> typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, event
                        .getSelection());
                if (typedSelection.isValid()) {
                    IProductCmptLink link = IpsObjectPartTester.castOrAdaptToPart(typedSelection.getFirstElement(),
                            IProductCmptLink.class);
                    if (link != null) {
                        openLink(link);
                    }
                }
            }
        });
    }

    private void openLink(IProductCmptLink link) {
        try {
            IProductCmpt targetProductCmpt = link.findTarget(link.getIpsProject());
            if (targetProductCmpt != null) {
                IProductCmptGeneration targetGeneration = targetProductCmpt
                        .getBestMatchingGenerationEffectiveOn(getActiveGeneration().getValidFrom());
                IpsUIPlugin.getDefault().openEditor(targetGeneration);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Creates the context menu for the tree viewer.
     */
    private void buildContextMenu() {
        MenuManager menuManager = new MenuManager();

        editor.getSite().registerContextMenu(ID, menuManager, treeViewer);

        // We use whitelist menu cleaner to avoid any other actions
        MenuCleaner menuCleaner = new MenuCleaner();
        menuCleaner.setWhiteListMode(true);
        menuCleaner.addFilteredPrefix("org.faktorips"); //$NON-NLS-1$
        menuCleaner.addFilteredPrefix("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
        menuManager.addMenuListener(menuCleaner);

        treePopup = menuManager.createContextMenu(treeViewer.getControl());

        treeViewer.getControl().setMenu(treePopup);

        // create empty menu for later use
        emptyMenu = new MenuManager().createContextMenu(treeViewer.getControl());
    }

    @Override
    protected void performRefresh() {
        if (treeViewer != null) {
            treeViewer.refresh(true);
            treeViewer.expandAll();
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
        cardinalityPanel.setEnabled(enabled);
    }

    @Override
    public Viewer getViewer() {
        return treeViewer;
    }

    /**
     * Listener for updating the cardinality triggerd by the selection of another link.
     */
    private class SelectionChangedListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();

            if (!isDataChangeable()) {
                cardinalityPanel.setDataChangeable(false);
            }
            if (selected instanceof LinkViewItem) {
                cardinalityPanel.setProductCmptLinkToEdit(((LinkViewItem)selected).getLink());
            } else {
                cardinalityPanel.setProductCmptLinkToEdit(null);
            }
        }

    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        super.populateToolBar(toolBarManager);

        if (treeViewer != null) {
            toolBarManager.add(filterEmptyAssociationAction);
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
