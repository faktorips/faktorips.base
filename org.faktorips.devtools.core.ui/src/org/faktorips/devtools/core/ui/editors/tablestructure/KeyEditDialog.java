/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.tablestructure.Column;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IKey;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.util.ArrayElementMover;
import org.faktorips.devtools.model.util.CollectionUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * A dialog to edit a unique or foreign key.
 */
public abstract class KeyEditDialog extends IpsPartEditDialog2 {

    /** the key being edited */
    private IKey key;

    /** table viewer to show the item candidates that can be added to the key. */
    private TableViewer candidatesViewer;

    /** table viewer to show the key's items. */
    private TableViewer itemsViewer;

    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    private Composite itemEditComposite;

    private Composite middle;

    public KeyEditDialog(IKey key, Shell parentShell, String title) {
        super(key, parentShell, title, true);
        this.key = key;
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;
        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.KeyEditDialog_generalTitle);
        page.setControl(createGeneralPageComposite(folder));
        refreshUi();
        return folder;
    }

    private Composite createGeneralPageComposite(TabFolder folder) {
        Composite pageComposite = createTabItemComposite(folder, 1, false);
        addPageTopControls(pageComposite);
        addHorizontalLine(pageComposite);
        addKeyItemSelectionControls(pageComposite);
        return pageComposite;
    }

    /**
     * Subclasses override this method to add custom controls at the top of the key selection page.
     * A horizontal line is automatically drawn between the page-top controls and the key selection
     * controls (at the bottom).
     * 
     * @param pageComposite the composite to add custom controls to.
     */
    protected abstract void addPageTopControls(Composite pageComposite);

    private void addHorizontalLine(Composite pageComposite) {
        getToolkit().createHorizonzalLine(pageComposite);
    }

    protected void addKeyItemSelectionControls(Composite pageComposite) {
        itemEditComposite = getToolkit().createGridComposite(pageComposite, 3, false, false);
        createItemCandidatesComposite(createGroup(Messages.KeyEditDialog_groupTitle));

        middle = getToolkit().createGridComposite(itemEditComposite, 1, true, true);
        middle.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_CENTER));
        createButtons();

        createKeyItemsComposite(createGroup(Messages.KeyEditDialog_labelKeyItems));
    }

    private Composite createGroup(String message) {
        Composite gridComposite = getToolkit().createGridComposite(itemEditComposite, 1, false, true);
        return getToolkit().createGroup(gridComposite, SWT.NONE, message);
    }

    protected void createItemCandidatesComposite(Composite parent) {
        Composite c = getToolkit().createGridComposite(parent, 1, false, false);
        Table table = new Table(c, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 180;
        data.heightHint = 200;
        table.setLayoutData(data);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                addSelectedItems();
            }
        });
        candidatesViewer = new TableViewer(table);
        candidatesViewer.addSelectionChangedListener($ -> updateButtonEnabledState());
        candidatesViewer.setLabelProvider(new DefaultLabelProvider());
        candidatesViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return key.getItemCandidates();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        });
        candidatesViewer.setInput(this);
    }

    /**
     * Creates the composite showing the key's items.
     */
    protected void createKeyItemsComposite(Composite parent) {
        Composite c = getToolkit().createGridComposite(parent, 1, false, false);
        Table table = new Table(c, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 180;
        data.heightHint = 200;
        table.setLayoutData(data);
        itemsViewer = new TableViewer(table);
        itemsViewer.setLabelProvider(new KeyItemLabelProvider());
        new TableMessageHoverService(itemsViewer) {
            @Override
            protected MessageList getMessagesFor(Object element) throws CoreRuntimeException {
                MessageList list = key.validate(key.getIpsProject());
                return list.getMessagesFor(key, IKey.PROPERTY_KEY_ITEMS, key.getIndexForKeyItemName((String)element));
            }
        };
        setContentProviderForItemsViewer();
        setListenerForItemsViewer();
        itemsViewer.setInput(this);
    }

    private void setListenerForItemsViewer() {
        itemsViewer.addSelectionChangedListener($ -> updateButtonEnabledState());
    }

    private void setContentProviderForItemsViewer() {
        itemsViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return key.getKeyItemNames();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        });
    }

    protected void createButtons() {
        getToolkit().createVerticalSpacer(middle, 10);
        createAddButton();
        createRemoveButton();
        getToolkit().createVerticalSpacer(middle, 10);
        createUpButton();
        createDownButton();

    }

    private void createDownButton() {
        downButton = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        downButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        downButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown.gif", true)); //$NON-NLS-1$
        downButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedItems(false);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void createUpButton() {
        upButton = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        upButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        upButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowUp.gif", true)); //$NON-NLS-1$
        upButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedItems(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void createRemoveButton() {
        removeButton = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        removeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        removeButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft.gif", true)); //$NON-NLS-1$
        removeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedItems();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void createAddButton() {
        addButton = getToolkit().createButton(middle, ""); //$NON-NLS-1$
        addButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        addButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight.gif", true)); //$NON-NLS-1$
        addButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addSelectedItems();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
    }

    private void addSelectedItems() {
        if (!isDataChangeable()) {
            return;
        }
        ISelection selection = candidatesViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        List<String> items = CollectionUtil.toArrayList(key.getKeyItemNames());
        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
            IKeyItem item = (IKeyItem)it.next();
            items.add(item.getName());
        }
        key.setKeyItems(items.toArray(new String[items.size()]));
        refreshUi();
    }

    private void removeSelectedItems() {
        if (!isDataChangeable()) {
            return;
        }
        ISelection selection = itemsViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        List<String> items = CollectionUtil.toArrayList(key.getKeyItemNames());
        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
            String item = (String)it.next();
            items.remove(item);
        }
        key.setKeyItems(items.toArray(new String[items.size()]));
        refreshUi();
    }

    private void moveSelectedItems(boolean up) {
        Table table = itemsViewer.getTable();
        if (table.getSelectionCount() == 0) {
            return;
        }
        String[] items = key.getKeyItemNames();
        ArrayElementMover mover = new ArrayElementMover(items);
        int[] newSelection;
        if (up) {
            newSelection = mover.moveUp(table.getSelectionIndices());
        } else {
            newSelection = mover.moveDown(table.getSelectionIndices());
        }
        key.setKeyItems(items);
        // must refresh before changing selection!
        itemsViewer.refresh();
        table.setSelection(newSelection);
        refreshUi();
    }

    protected void refreshUi() {
        getBindingContext().updateUI();
        itemsViewer.refresh();
        candidatesViewer.refresh();
        setTitle(buildTitle());
        updateButtonEnabledState();
        validate();
    }

    private void updateButtonEnabledState() {
        addButton.setEnabled(isDataChangeable() && !candidatesViewer.getSelection().isEmpty());
        removeButton.setEnabled(isDataChangeable() && !itemsViewer.getSelection().isEmpty());
        upButton.setEnabled(isDataChangeable() && !itemsViewer.getSelection().isEmpty());
        downButton.setEnabled(isDataChangeable() && !itemsViewer.getSelection().isEmpty());
    }

    private void validate() {
        try {
            MessageList msgList = key.validate(key.getIpsProject());
            if (msgList.isEmpty()) {
                setMessage(null);
                return;
            }
            Message msg = msgList.getMessage(0);
            setMessage(msg.getText(), UIToolkit.convertToJFaceSeverity(msg.getSeverity()));
        } catch (CoreRuntimeException e) {
            IpsPlugin.log(e);
        }
    }

    private class KeyItemLabelProvider extends DefaultLabelProvider {

        private ResourceManager resourceManager;

        private ImageDescriptor tableColumnDescriptor = IIpsDecorators.getDefaultImageDescriptor(Column.class);

        public KeyItemLabelProvider() {
            super();
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }

        @Override
        public Image getImage(Object element) {
            String item = (String)element;
            Image image;
            IColumnRange range = key.getTableStructure().getRange(item);
            image = setImageByRange(range);
            MessageList list;
            try {
                list = key.validate(key.getIpsProject());
            } catch (CoreRuntimeException e) {
                IpsPlugin.log(e);
                return image;
            }
            if (list.getSeverity() == Message.NONE) {
                return image;
            }

            MessageList itemMsgList = list.getMessagesFor(key, IKey.PROPERTY_KEY_ITEMS,
                    key.getIndexForKeyItemName(item));
            if (itemMsgList.getSeverity() == Message.NONE) {
                return image;
            }

            ImageDescriptor descriptor = IpsProblemOverlayIcon.createOverlayIcon(image, itemMsgList.getSeverity());
            return (Image)resourceManager.get(descriptor);
        }

        private Image setImageByRange(IColumnRange range) {
            Image image;
            if (range != null) {
                image = super.getImage(range);
            } else {
                image = (Image)resourceManager.get(tableColumnDescriptor);
            }
            return image;
        }

        @Override
        public void dispose() {
            resourceManager.dispose();
            super.dispose();
        }

    }

}
