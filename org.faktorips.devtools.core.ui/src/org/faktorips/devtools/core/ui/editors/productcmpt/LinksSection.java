/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.MenuCleaner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.ISelectionProviderActivation;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.pctype.ContentsChangeListenerForWidget;
import org.faktorips.devtools.core.ui.editors.productcmpt.LinkSectionDropListener.MoveLinkDragListener;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * A section to display a product component's relations in a tree.
 * 
 * @author Thorsten Guenther
 */
public class LinksSection extends IpsSection implements ISelectionProviderActivation {

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

        LinksContentProvider contentProvider = new LinksContentProvider();
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
            treeViewer.setInput(generation);
            treeViewer.addSelectionChangedListener(selectionChangedListener);
            dropListener = new LinkSectionDropListener(editor, treeViewer, generation);
            treeViewer.addDropSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] { FileTransfer.getInstance(),
                    TextTransfer.getInstance() }, dropListener);
            MoveLinkDragListener dragListener = dropListener.new MoveLinkDragListener(treeViewer);
            treeViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() }, dragListener);

            treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
            treeViewer.expandAll();

            final LinksMessageCueLabelProvider labelProvider = new LinksMessageCueLabelProvider(generation);
            treeViewer.setLabelProvider(labelProvider);
            new TreeMessageHoverService(treeViewer) {
                @Override
                protected MessageList getMessagesFor(Object element) throws CoreException {
                    return labelProvider.getMessages(element);
                }
            };

            buildContextMenu();

            cardinalityPanel = new CardinalityPanel(relationRootPane, toolkit);
            cardinalityPanel.setDataChangeable(isDataChangeable());
            cardinalityPanel.deactivate();

            addFocusControl(treeViewer.getTree());
            registerDoubleClickListener();
            ModelViewerSynchronizer synchronizer = new ModelViewerSynchronizer(treeViewer);
            synchronizer.setWidget(client);
            IpsPlugin.getDefault().getIpsModel().addChangeListener(synchronizer);
        }
        toolkit.getFormToolkit().paintBordersFor(relationRootPane);
    }

    /**
     * Register a double click listener to open the referenced product component in a new editor
     */
    private void registerDoubleClickListener() {
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                TypedSelection<IProductCmptLink> typedSelection = new TypedSelection<IProductCmptLink>(
                        IProductCmptLink.class, event.getSelection());
                if (typedSelection.isValid()) {
                    openLink(typedSelection.getFirstElement());
                }
            }
        });
    }

    private void openLink(IProductCmptLink link) {
        IFile file = (IFile)link.getAdapter(IFile.class);
        IpsUIPlugin.getDefault().openEditor(file);
    }

    /**
     * Creates the context menu for the treeview.
     */
    private void buildContextMenu() {
        MenuManager menuManager = new MenuManager();

        editor.getSite().registerContextMenu(ID, menuManager, treeViewer);

        // We do not want to have additions in this menu!
        MenuCleaner menuCleaner = new MenuCleaner();
        menuCleaner.addFilteredMenuGroup(IWorkbenchActionConstants.MB_ADDITIONS);
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
     */
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
     * Returns the name of the product component type relation identified by the target. The target
     * is either the name itself (the top level nodes of the relation section tree) or instances of
     * IProductCmptRelation. (the nodes below the relation type nodes).
     */
    String getAssociationName(Object target) {
        if (target instanceof String) {
            return (String)target;
        }
        if (target instanceof IProductCmptLink) {
            return ((IProductCmptLink)target).getAssociation();
        }
        return null;
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
            if (selected instanceof IProductCmptLink) {
                cardinalityPanel.setProductCmptLinkToEdit((IProductCmptLink)selected);
            } else {
                cardinalityPanel.setProductCmptLinkToEdit(null);
            }
        }

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

    /**
     * Synchronizes the model object thus the generation object with the tree viewer. It implements
     * an algorithm that calculates the next selection of an item after the currently selected item
     * has been selected.
     * 
     * @author Peter Erzberger
     */
    private class ModelViewerSynchronizer extends ContentsChangeListenerForWidget implements SelectionListener {

        private List<Integer> lastSelectionPath = null;

        private ModelViewerSynchronizer(TreeViewer treeViewer) {
            treeViewer.getTree().addSelectionListener(this);
        }

        /**
         * Keeps track of structural changes of relations of the current product component
         * generation. {@inheritDoc}
         */
        @Override
        public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
            // the generationDirty flag is only necessary because of a buggy behaviour when the
            // the generation of the product component editor has changed. The pages a created newly
            // in this case and I don't exactly what then happens... (pk). It desperately asks for
            // refactoring
            if (!event.getIpsSrcFile().equals(generation.getIpsObject().getIpsSrcFile())) {
                return;
            }
            try {
                IIpsObject obj = event.getIpsSrcFile().getIpsObject();
                if (obj == null || obj.getIpsObjectType() != IpsObjectType.PRODUCT_CMPT) {
                    return;
                }

            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            processRelationChanges(event);
        }

        private void processRelationChanges(ContentChangeEvent event) {
            if (event.getEventType() == ContentChangeEvent.TYPE_PART_REMOVED
                    && event.containsAffectedObjects(IProductCmptLink.class)) {
                IProductCmptLink relation = (IProductCmptLink)event.getPart();
                treeViewer.refresh(relation.getAssociation());
                TreeItem possibleSelection = getNextPossibleItem(treeViewer.getTree(), lastSelectionPath);
                if (possibleSelection == null) {
                    return;
                }
                treeViewer.getTree().setSelection(new TreeItem[] { possibleSelection });
                /*
                 * this additional sending of an event is necessary due to a bug in swt. The problem
                 * is that dispite of the new selection no event is triggered
                 */
                treeViewer.getTree().notifyListeners(SWT.Selection, null);
                return;
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty implementation
        }

        /**
         * Calculates the path within the tree that points to the provided TreeItem. The path is a
         * list of indices. The first index in the list is meant to be the index of the TreeItem
         * starting from the root of the tree, the second is the index of the TreeItem starting from
         * the TreeItem calculated before and so on.
         */
        private void createSelectionPath(List<Integer> path, TreeItem item) {
            TreeItem parent = item.getParentItem();
            if (parent == null) {
                path.add(new Integer(item.getParent().indexOf(item)));
                return;
            }
            createSelectionPath(path, parent);
            path.add(new Integer(parent.indexOf(item)));
        }

        /**
         * Calculates the next possible selection for the provided path. The assumption is that the
         * item the path is targeting at might not exist anymore therefor an item has to be
         * determined that is preferably near to it.
         */
        private TreeItem getNextPossibleItem(Tree tree, List<Integer> pathList) {
            if (pathList == null || tree.getItemCount() == 0 || pathList.size() == 0) {
                return null;
            }
            TreeItem parent = null;
            Integer index = pathList.get(0);
            if (tree.getItemCount() > index.intValue()) {
                parent = tree.getItem((pathList.get(0)).intValue());
            } else {
                parent = tree.getItem(tree.getItemCount() - 1);
            }
            for (int i = 1; i < pathList.size(); i++) {
                if (parent.getItemCount() == 0) {
                    return parent;
                }
                index = pathList.get(i);
                if (parent.getItemCount() > index.intValue()) {
                    TreeItem item = parent.getItem(index.intValue());
                    if (item.getItemCount() == 0) {
                        return item;
                    }
                    parent = item;
                    continue;
                }
                return parent.getItem(parent.getItemCount() - 1);
            }
            return parent;
        }

        /**
         * Calculates the path starting from the tree root to the currently selected item. The path
         * is kept in the lastSelectionPath member variable.
         */
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.item != null) {
                TreeItem item = (TreeItem)e.item;
                lastSelectionPath = new ArrayList<Integer>();
                createSelectionPath(lastSelectionPath, item);
            }
        }
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return treeViewer;
    }

    @Override
    public boolean isActivated() {
        if (treeViewer == null) {
            return false;
        }
        return treeViewer.getTree().isFocusControl();
    }

}
