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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.core.util.ArrayElementMover;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A dialog to edit a unique or foreign key.
 */
public class KeyEditDialog extends IpsPartEditDialog {

    // the key being edited
    private IKey key;

    // table viewer to show the item candidates that can be added to the key.
    private TableViewer candidatesViewer;

    // table viewer to show the key's items.
    private TableViewer itemsViewer;

    // fields
    private TextButtonField tableStructureRefField;
    private TextField uniqueKeyRefField;

    // completion processor for a table structure's unique keys.
    UniqueKeyCompletionProcessor completionProcessor;

    // buttons
    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    public KeyEditDialog(IKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialog_title, true);
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.KeyEditDialog_generalTitle);
        page.setControl(createGeneralPage(folder));

        createDescriptionTabItem(folder);
        refreshUi();
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite pageComposite;
        Composite itemEditComposite;
        if (key instanceof IForeignKey) {
            pageComposite = createTabItemComposite(folder, 1, false);
            Composite refTableComposite = uiToolkit.createLabelEditColumnComposite(pageComposite);
            GridLayout layout = (GridLayout)refTableComposite.getLayout();
            layout.marginHeight = 12;
            uiToolkit.createFormLabel(refTableComposite, Messages.KeyEditDialog_labelReferenceStructure);
            TableStructureRefControl refControl = uiToolkit.createTableStructureRefControl(key.getIpsProject(),
                    refTableComposite);
            refControl.setFocus();
            tableStructureRefField = new TextButtonField(refControl);
            tableStructureRefField.addChangeListener(new ValueChangeListener() {

                @Override
                public void valueChanged(FieldValueChangedEvent e) {
                    // following line is a hack to make sure the model is
                    // uptodate. The uicontroller gets informated after this
                    // listener because it is registered later, so the refreshUi methode
                    // would work on the old model data.
                    // correct implementation would be to listen for model changes,
                    // and then update the ui.
                    ((IForeignKey)key).setReferencedTableStructure(tableStructureRefField.getText());
                    try {
                        ITableStructure structure = ((IForeignKey)key)
                                .findReferencedTableStructure(key.getIpsProject());
                        completionProcessor.setTableStructure(structure);
                        if (structure != null) {
                            IUniqueKey[] keys = structure.getUniqueKeys();
                            if (keys.length > 0) {
                                uniqueKeyRefField.setText(keys[0].getName());
                            }
                        }
                    } catch (CoreException e1) {
                        IpsPlugin.log(e1);
                    }
                    refreshUi();
                }

            });
            uiToolkit.createFormLabel(refTableComposite, Messages.KeyEditDialog_labelReferenceUniqueKey);
            Text ukRefControl = uiToolkit.createText(refTableComposite);
            completionProcessor = new UniqueKeyCompletionProcessor();
            ContentAssistHandler.createHandlerForText(ukRefControl, CompletionUtil
                    .createContentAssistant(completionProcessor));

            uniqueKeyRefField = new TextField(ukRefControl);
            uniqueKeyRefField.addChangeListener(new ValueChangeListener() {

                @Override
                public void valueChanged(FieldValueChangedEvent e) {
                    // see comment above
                    ((IForeignKey)key).setReferencedUniqueKey(uniqueKeyRefField.getText());
                    refreshUi();
                }

            });

            uiToolkit.createHorizonzalLine(pageComposite);
            itemEditComposite = uiToolkit.createGridComposite(pageComposite, 3, false, false);
        } else {
            itemEditComposite = createTabItemComposite(folder, 3, false);
            pageComposite = itemEditComposite;
        }
        Composite left = uiToolkit.createGridComposite(itemEditComposite, 1, false, true);
        Composite leftGroup = uiToolkit.createGroup(left, SWT.NONE, Messages.KeyEditDialog_labelKeyItems);
        createKeyItemsComposite(leftGroup);

        Composite middle = uiToolkit.createGridComposite(itemEditComposite, 1, true, true);
        middle.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_CENTER));
        createButtons(middle);

        Composite right = uiToolkit.createGridComposite(itemEditComposite, 1, false, true);
        Composite rightGroup = uiToolkit.createGroup(right, SWT.NONE, Messages.KeyEditDialog_groupTitle);
        createItemCandidatesComposite(rightGroup);

        return pageComposite;
    }

    /**
     * Creates the composite showing the key's items.
     */
    private void createKeyItemsComposite(Composite parent) {
        Composite c = uiToolkit.createGridComposite(parent, 1, false, false);
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
            protected MessageList getMessagesFor(Object element) throws CoreException {
                MessageList list = key.validate(key.getIpsProject());
                return list.getMessagesFor(key, IKey.PROPERTY_KEY_ITEMS, key.getIndexForKeyItemName((String)element));
            }

        };
        itemsViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return key.getKeyItemNames();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        });
        itemsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonEnabledState();
            }

        });
        itemsViewer.setInput(this);
    }

    private void createButtons(Composite middle) {
        uiToolkit.createVerticalSpacer(middle, 10);

        addButton = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        addButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        addButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft.gif", true)); //$NON-NLS-1$
        addButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addSelectedItems();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        removeButton = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        removeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        removeButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight.gif", true)); //$NON-NLS-1$
        removeButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedItems();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        uiToolkit.createVerticalSpacer(middle, 10);

        upButton = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        upButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        upButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowUp.gif", true)); //$NON-NLS-1$
        upButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedItems(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        downButton = uiToolkit.createButton(middle, ""); //$NON-NLS-1$
        downButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        downButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown.gif", true)); //$NON-NLS-1$
        downButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedItems(false);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

    }

    private void createItemCandidatesComposite(Composite parent) {
        Composite c = uiToolkit.createGridComposite(parent, 1, false, false);
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
        candidatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonEnabledState();
            }

        });
        candidatesViewer.setLabelProvider(new DefaultLabelProvider());
        candidatesViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return key.getItemCandidates();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        });
        candidatesViewer.setInput(this);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    @Override
    protected void connectToModel() {
        super.connectToModel();
        if (tableStructureRefField != null) {
            uiController.add(tableStructureRefField, IForeignKey.PROPERTY_REF_TABLE_STRUCTURE);
            uiController.add(uniqueKeyRefField, IForeignKey.PROPERTY_REF_UNIQUE_KEY);
        }
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
        itemsViewer.refresh(); // must refresh before changing selection!
        table.setSelection(newSelection);
        refreshUi();
    }

    private void refreshUi() {
        uiController.updateUI();
        itemsViewer.refresh();
        candidatesViewer.refresh();
        setTitle(buildTitle());
        updateButtonEnabledState();
        validate();
    }

    private void validate() {
        try {
            MessageList msgList = key.validate(key.getIpsProject());
            if (msgList.isEmpty()) {
                setMessage(null);
                return;
            }
            Message msg = msgList.getMessage(0);
            setMessage(msg.getText(), uiToolkit.convertToJFaceSeverity(msg.getSeverity()));
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void updateButtonEnabledState() {
        addButton.setEnabled(isDataChangeable() && !candidatesViewer.getSelection().isEmpty());
        removeButton.setEnabled(isDataChangeable() && !itemsViewer.getSelection().isEmpty());
        upButton.setEnabled(isDataChangeable() && !itemsViewer.getSelection().isEmpty());
        downButton.setEnabled(isDataChangeable() && !itemsViewer.getSelection().isEmpty());
    }

    private class KeyItemLabelProvider extends DefaultLabelProvider {

        private ResourceManager resourceManager;

        ImageDescriptor tableColumnDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("TableColumn.gif");

        public KeyItemLabelProvider() {
            super();
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }

        @Override
        public Image getImage(Object element) {
            String item = (String)element;
            Image image;
            IColumnRange range = key.getTableStructure().getRange(item);
            if (range != null) {
                image = super.getImage(range);
            } else {
                image = (Image)resourceManager.get(tableColumnDescriptor);
            }
            MessageList list;
            try {
                list = key.validate(key.getIpsProject());
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return image;
            }
            if (list.getSeverity() == Message.NONE) {
                return image;
            }

            MessageList itemMsgList = list.getMessagesFor(key, IKey.PROPERTY_KEY_ITEMS, key
                    .getIndexForKeyItemName(item));
            if (itemMsgList.getSeverity() == Message.NONE) {
                return image;
            }

            ImageDescriptor descriptor = IpsProblemOverlayIcon.createOverlayIcon(image, itemMsgList.getSeverity());
            return (Image)resourceManager.get(descriptor);
        }

        @Override
        public void dispose() {
            resourceManager.dispose();
            super.dispose();
        }

    }
}
