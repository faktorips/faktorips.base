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

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enumcontent.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The ui section for the enum type pages and the enum content page that contains the enum values to
 * be edited.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValuesSection extends IpsSection {

    /** The enum value container holding the enum values to be edited. */
    private IEnumValueContainer enumValueContainer;

    /** The enum type that is referenced by the ips object being edited. */
    private IEnumType enumType;

    /** The ui table widget. */
    private Table enumValuesTable;

    /** The jface table viewer linking the ui table with the model data. */
    private TableViewer enumValuesTableViewer;

    /** The names of the columns for the enum values table ui widget. */
    private List<String> enumValuesTableColumnNames;

    /** Action to add new enum values. */
    private IAction newEnumValueAction;

    /** Action to delete enum values. */
    private IAction deleteEnumValueAction;

    /** Action to move enum values up by 1. */
    private IAction moveEnumValueUpAction;

    /** Action to move enum values down by 1. */
    private IAction moveEnumValueDownAction;

    /**
     * Creates a new <code>EnumValuesSection</code> containing the enum values of the given enum
     * value container.
     * 
     * @param enumValueContainer The enum values of this enum value container will be shown.
     * @param parent The parent ui composite.
     * @param toolkit The ui toolkit that shall be used to create ui elements.
     * 
     * @throws CoreException If an error occurs while searching for the enum type referenced by the
     *             ips object being edited.
     * @throws NullPointerException If enumValueContainer is <code>null</code>.
     */
    public EnumValuesSection(IEnumValueContainer enumValueContainer, Composite parent, UIToolkit toolkit)
            throws CoreException {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(enumValueContainer);
        this.enumValueContainer = enumValueContainer;
        this.enumValuesTableColumnNames = new ArrayList<String>(4);
        this.enumType = enumValueContainer.findEnumType();

        initControls();
        createActions();
        createToolbar();
        createContextMenu();

        setText(Messages.EnumValuesSection_title);

        updateEnabledStates();
        createFirstRow();
    }

    /**
     * Make sure that there is at least one row in the enum values table if there are any attributes
     * in the enum type and the enum values table is enabled.
     */
    private void createFirstRow() throws CoreException {
        if (enumValuesTable.isEnabled()) {
            if (enumType.getEnumAttributesCount() > 0 && enumValueContainer.getEnumValuesCount() == 0) {
                enumValueContainer.newEnumValue();
            }
        }
        
        updateTableViewer();
    }

    /** Creates the actions. */
    private void createActions() {
        newEnumValueAction = new NewEnumValueAction(enumValuesTableViewer);
        deleteEnumValueAction = new DeleteEnumValueAction(enumValuesTableViewer);
        moveEnumValueUpAction = new MoveEnumValueAction(enumValuesTableViewer, true);
        moveEnumValueDownAction = new MoveEnumValueAction(enumValuesTableViewer, false);
    }

    /** Creates the section's toolbar. */
    /*
     * TODO align the toolbar at the right of the section title bar
     */
    private void createToolbar() {
        // Create the toolbar
        Section section = getSectionControl();
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(section);
        toolbar.setLocation(220, toolbar.getLocation().y + 1);

        // Add the actions to the toolbar
        toolBarManager.add(newEnumValueAction);
        toolBarManager.add(deleteEnumValueAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(moveEnumValueUpAction);
        toolBarManager.add(moveEnumValueDownAction);

        // Update the toolbar with the new information
        toolBarManager.update(true);
    }

    /**
     * Updates the enabled states of the enum values table and its actions.
     * <p>
     * The enabled states result from the fact whether this enum values section is used for an enum
     * type or for an enum content and whether the (referenced) enum type defines its values in the
     * model or not. The enum values table will also be disabled if the enum type is abstract.
     */
    private void updateEnabledStates() {
        boolean valuesArePartOfModel = enumType.getValuesArePartOfModel();
        boolean isAbstract = enumType.isAbstract();

        if (enumValueContainer instanceof IEnumType) {
            newEnumValueAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            deleteEnumValueAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            moveEnumValueUpAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            moveEnumValueDownAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            enumValuesTable.setEnabled(valuesArePartOfModel && !(isAbstract));
            getSectionControl().setEnabled(valuesArePartOfModel && !(isAbstract));

        } else if (enumValueContainer instanceof IEnumContent) {
            newEnumValueAction.setEnabled(!(valuesArePartOfModel) && !(isAbstract));
            deleteEnumValueAction.setEnabled(!(valuesArePartOfModel) && !(isAbstract));
            moveEnumValueUpAction.setEnabled(!(valuesArePartOfModel) && !(isAbstract));
            moveEnumValueDownAction.setEnabled(!(valuesArePartOfModel) && !(isAbstract));
            enumValuesTable.setEnabled(!(valuesArePartOfModel) && !(isAbstract));
            getSectionControl().setEnabled(!(valuesArePartOfModel) && !(isAbstract));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        try {
            createTable(client, toolkit);
            createTableViewer();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        createTableValidationHoverService();
    }

    /** Creates the ui table for editing enum values. */
    private void createTable(Composite parent, UIToolkit toolkit) throws CoreException {
        // Create the ui widget
        enumValuesTable = toolkit.createTable(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE
                | SWT.FULL_SELECTION);
        enumValuesTable.setHeaderVisible(true);
        enumValuesTable.setLinesVisible(true);

        // Fill all space
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = parent.getClientArea().width;
        tableGridData.heightHint = parent.getClientArea().height;
        enumValuesTable.setLayoutData(tableGridData);

        /*
         * Create columns based upon enum attribute values if there are any enum values yet, if not
         * use the enum attributes to create the columns.
         */
        if (enumValueContainer.getEnumValuesCount() > 0) {
            IEnumValue enumValue = enumValueContainer.getEnumValues().get(0);
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
            for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValues) {
                IEnumAttribute currentEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
                String columnName = (currentEnumAttribute != null) ? currentEnumAttribute.getName() : "";
                addTableColumnToEnumValuesTable(columnName);
            }
        
        } else {
            for (IEnumAttribute currentEnumAttribute : enumValueContainer.findEnumType().getEnumAttributes()) {
                addTableColumnToEnumValuesTable(currentEnumAttribute.getName());
            }
        }

        increaseHeightOfTableRows();
    }

    /** Increases the height of the table rows slightly. */
    private void increaseHeightOfTableRows() {
        Listener paintListener = new Listener() {

            public void handleEvent(Event event) {
                if (event.type == SWT.MeasureItem) {
                    if (enumValuesTable.getColumnCount() == 0) {
                        return;
                    }

                    TableItem item = (TableItem)event.item;
                    String text = item.getText(event.index);
                    Point size = event.gc.textExtent(text);

                    // The height will be increased by 5 pixel
                    event.height = Math.max(event.height, size.y + 5);
                }
            }

        };

        enumValuesTable.addListener(SWT.MeasureItem, paintListener);
    }

    /** Creates the context menu for the enum values table. */
    private void createContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$

        menuMgr.add(newEnumValueAction);
        menuMgr.add(deleteEnumValueAction);
        menuMgr.add(new Separator());
        menuMgr.add(moveEnumValueUpAction);
        menuMgr.add(moveEnumValueDownAction);

        Menu menu = menuMgr.createContextMenu(enumValuesTable);
        enumValuesTable.setMenu(menu);
    }

    /** Creates the table viewer for the enum values table widget. */
    private void createTableViewer() throws CoreException {
        // Create and setup general properties
        enumValuesTableViewer = new TableViewer(enumValuesTable);
        enumValuesTableViewer.setUseHashlookup(true);

        // Create cell editors and set column properties
        updateTableViewer();

        // Assign the content provider and the label provider
        enumValuesTableViewer.setContentProvider(new EnumValuesContentProvider());
        enumValuesTableViewer.setLabelProvider(new EnumValuesLabelProvider());
        enumValuesTableViewer.setInput(enumValueContainer);

        // Set the cell modifier
        enumValuesTableViewer.setCellModifier(new EnumCellModifier());

        // Set the row deletor listener to automatically delete empty rows at the end of the table
        enumValuesTableViewer.addSelectionChangedListener(new RowDeletor());
    }

    /** Creates the hover service for validation messages for the enum values table viewer. */
    private void createTableValidationHoverService() {
        new TableMessageHoverService(enumValuesTableViewer) {

            /**
             * {@inheritDoc}
             */
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return ((IEnumValue)element).validate(enumValueContainer.getIpsProject());
                }

                return null;
            }

        };
    }

    /**
     * Updates the cell editors for the enum values table viewer by creating them anew and
     * overwrites the column properties with actual data. Also refreshes the viewer with actual
     * model data.
     */
    private void updateTableViewer() throws CoreException {
        if (enumValuesTableViewer == null) {
            return;
        }

        String[] columnNames = enumValuesTableColumnNames.toArray(new String[enumValuesTableColumnNames.size()]);

        // Update column properties
        enumValuesTableViewer.setColumnProperties(columnNames);

        // Create cell editors
        CellEditor[] cellEditors = createCellEditors(columnNames);

        // Assign the cell editors to the table viewer
        enumValuesTableViewer.setCellEditors(cellEditors);

        // Refresh the viewer
        enumValuesTableViewer.refresh();
    }

    /** Updates the cell editors for the enum values table by recreating them. */
    private CellEditor[] createCellEditors(String[] columnNames) throws CoreException {
        CellEditor[] cellEditors = new CellEditor[columnNames.length];
        for (int i = 0; i < cellEditors.length; i++) {
            String datatypeQualifiedName = enumType.getEnumAttributes().get(i).getDatatype();
            ValueDatatype datatype = enumValueContainer.getIpsProject().findValueDatatype(datatypeQualifiedName);
            ValueDatatypeControlFactory valueDatatypeControlFactory = IpsUIPlugin.getDefault()
                    .getValueDatatypeControlFactory(datatype);

            TableCellEditor cellEditor = valueDatatypeControlFactory.createCellEditor(getToolkit(), datatype, null,
                    enumValuesTableViewer, i);
            cellEditor.setRowCreating(true);

            cellEditors[i] = cellEditor;
        }

        return cellEditors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    /** Adds a column to the enum values table. */
    private void addTableColumnToEnumValuesTable(String columnName) throws CoreException {
        TableColumn newColumn = new TableColumn(enumValuesTable, SWT.LEFT);
        newColumn.setText(columnName);
        newColumn.setWidth(200);
        enumValuesTableColumnNames.add(columnName);

        updateTableViewer();
    }

    /** Removes the column identified by the given enum attribute from the enum values table. */
    private void removeTableColumnFromEnumValuesTable(IEnumAttribute enumAttribute) throws CoreException {
        String name = enumAttribute.getName();
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            if (currentColumn.getText().equals(name)) {
                currentColumn.dispose();
            }
        }
        enumValuesTableColumnNames.remove(name);

        updateTableViewer();
    }

    /** Updates the column in the enum values table that represents the given enum attribute. */
    private void updateTableColumnInEnumValuesTable(IEnumAttribute enumAttribute) throws CoreException {
        int index = ((IEnumType)enumAttribute.getParent()).getIndexOfEnumAttribute(enumAttribute);
        enumValuesTable.getColumn(index).setText(enumAttribute.getName());

        updateColumnNames();
        updateTableViewer();
    }

    /** Updates the column names list (column order taken into account). */
    private void updateColumnNames() {
        enumValuesTableColumnNames.clear();

        /*
         * Go over all table columns and add them to the column names list in the same order as the
         * column order is.
         */
        TableColumn[] tableColumns = enumValuesTable.getColumns();
        int[] columnOrder = enumValuesTable.getColumnOrder();
        for (int i = 0; i < columnOrder.length; i++) {
            enumValuesTableColumnNames.add(tableColumns[columnOrder[i]].getText());
        }
    }

    /**
     * The content provider for the enum values table viewer.
     */
    private class EnumValuesContentProvider implements IStructuredContentProvider, ContentsChangeListener {

        /** Structure to track the table column order. */
        private class ColumnOrderInfo {
            private int orderNumber;
            private IEnumAttribute enumAttribute;

            private ColumnOrderInfo(int orderNumber, IEnumAttribute enumAttribute) {
                ArgumentCheck.notNull(enumAttribute);
                this.orderNumber = orderNumber;
                this.enumAttribute = enumAttribute;
            }
        }

        /** Column order info list. */
        private List<ColumnOrderInfo> columnOrderInfos;

        /**
         * Creates the content provider for the enum values table viewer and registers itself as
         * content change listener to the ips model.
         */
        public EnumValuesContentProvider() {
            // Register as listener
            enumValueContainer.getIpsModel().addChangeListener(this);

            // Create column order info
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributes();
            columnOrderInfos = new ArrayList<ColumnOrderInfo>(enumValuesTableColumnNames.size());
            for (int i = 0; i < enumAttributes.size(); i++) {
                columnOrderInfos.add(new ColumnOrderInfo(i, enumAttributes.get(i)));
            }
        }

        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            return enumValueContainer.getEnumValues().toArray();
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
            enumValueContainer.getIpsModel().removeChangeListener(this);
        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        /**
         * {@inheritDoc}
         */
        public void contentsChanged(ContentChangeEvent event) {
            // Return if the content changed was not the enum value container to be edited
            if (!(event.getIpsSrcFile().equals(enumValueContainer.getIpsSrcFile()))) {
                return;
            }

            // Switch based upon event type
            IIpsObjectPart part = event.getPart();
            switch (event.getEventType()) {
                case ContentChangeEvent.TYPE_PART_ADDED:
                    if (part != null) {
                        if (part instanceof IEnumAttribute) {
                            try {
                                enumAttributeAdded((IEnumAttribute)part);
                            } catch (CoreException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    break;

                case ContentChangeEvent.TYPE_PROPERTY_CHANGED:
                    if (part != null) {
                        if (part instanceof IEnumAttribute) {
                            IEnumAttribute modifiedEnumAttribute = (IEnumAttribute)part;
                            try {
                                EnumValuesSection.this.updateTableColumnInEnumValuesTable(modifiedEnumAttribute);
                            } catch (CoreException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    break;

                case ContentChangeEvent.TYPE_PART_REMOVED:
                    if (part != null) {
                        if (part instanceof IEnumAttribute) {
                            try {
                                enumAttributeRemoved((IEnumAttribute)part);
                            } catch (CoreException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    break;

                case ContentChangeEvent.TYPE_PARTS_CHANGED_POSITIONS:
                    IIpsObjectPart[] movedParts = event.getMovedParts();
                    IEnumAttribute[] movedEnumAttributes = new IEnumAttribute[movedParts.length];
                    int i;
                    for (i = 0; i < movedParts.length; i++) {
                        if (movedParts[i] instanceof IEnumAttribute) {
                            movedEnumAttributes[i] = (IEnumAttribute)movedParts[i];
                        }
                    }
                    if (i > 0) {
                        if (movedEnumAttributes[0] != null) {
                            try {
                                enumAttributesMoved(movedEnumAttributes);
                            } catch (CoreException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    enumValuesTableViewer.refresh();

                    break;

                case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                    try {
                        IIpsObject changedIpsObject = event.getIpsSrcFile().getIpsObject();
                        if (changedIpsObject instanceof IEnumType) {
                            if ((IEnumType)changedIpsObject == enumValueContainer) {
                                updateEnabledStates();
                            }
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }

                    break;
            }
        }

        /** Builds and sets the current column order from the column order info list. */
        private void buildAndSetColumnOrder() {
            // Build the new column order array for the table
            int[] columnOrder = new int[columnOrderInfos.size()];
            for (int i = 0; i < columnOrderInfos.size(); i++) {
                columnOrder[i] = columnOrderInfos.get(i).orderNumber;
            }

            // Set the column order
            enumValuesTable.setColumnOrder(columnOrder);
        }

        /** Things to do when an enum attribute has been added. */
        private void enumAttributeAdded(IEnumAttribute addedEnumAttribute) throws CoreException {
            // Add table column
            EnumValuesSection.this.addTableColumnToEnumValuesTable(addedEnumAttribute.getName());

            // Add column order info
            columnOrderInfos.add(new ColumnOrderInfo(columnOrderInfos.size(), addedEnumAttribute));

            buildAndSetColumnOrder();
        }

        /** Things to do when enum attributes have been moved. */
        private void enumAttributesMoved(IEnumAttribute[] movedEnumAttributes) throws CoreException {
            // Get the new indizes
            int index1 = -1;
            int index2 = -1;
            for (int i = 0; i < movedEnumAttributes.length; i++) {
                if (columnOrderInfos.get(i).enumAttribute != movedEnumAttributes[i]) {
                    if (index1 == -1) {
                        index1 = i;
                    } else {
                        index2 = i;
                    }
                }
                if (index1 != -1 && index2 != -1) {
                    break;
                }
            }

            // Get current column info for the enum attributes
            ColumnOrderInfo columnOrderInfo1 = columnOrderInfos.get(index1);
            ColumnOrderInfo columnOrderInfo2 = columnOrderInfos.get(index2);

            // Temporary save the contents of the first column order info for swapping
            IEnumAttribute tempInfo1Attribute = columnOrderInfo1.enumAttribute;
            int tempInfo1OrderNumber = columnOrderInfo1.orderNumber;

            // Swap the contents of the column order infos
            columnOrderInfo1.enumAttribute = columnOrderInfo2.enumAttribute;
            columnOrderInfo1.orderNumber = columnOrderInfo2.orderNumber;
            columnOrderInfo2.enumAttribute = tempInfo1Attribute;
            columnOrderInfo2.orderNumber = tempInfo1OrderNumber;

            buildAndSetColumnOrder();
        }

        /** Things to do when an enum attribute has been removed from the referenced enum type. */
        private void enumAttributeRemoved(IEnumAttribute removedEnumAttribute) throws CoreException {
            // Delete referencing enum values if there are no more enum attributes
            IEnumType enumType = (IEnumType)removedEnumAttribute.getParent();
            if (enumType.getEnumAttributesCount() == 0) {
                for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
                    currentEnumValue.delete();
                }
                try {
                    for (IEnumContent currentEnumContent : enumType.findReferencingEnumContents()) {
                        for (IEnumValue currentEnumValue : currentEnumContent.getEnumValues()) {
                            currentEnumValue.delete();
                        }
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

            // Remove the table column of the enum attribute
            EnumValuesSection.this.removeTableColumnFromEnumValuesTable(removedEnumAttribute);

            // Remove column order info entry
            int indexToRemove = -1;
            for (int i = 0; i < columnOrderInfos.size(); i++) {
                if (columnOrderInfos.get(i).enumAttribute == removedEnumAttribute) {
                    indexToRemove = i;
                    break;
                }
            }
            if (indexToRemove == -1) {
                // Should theoretically never happen
                throw new NoSuchElementException();
            }
            ColumnOrderInfo removedColumnOrderInfo = columnOrderInfos.remove(indexToRemove);

            /*
             * Now we removed the column order info entry but we still need to decrement some order
             * numbers by 1 because we now have one column less. If we had 4 column order entries
             * before and now have 3 our order numbers go only to 2 instead to 3. If we removed the
             * entry with order number 1 for example we need to decrement the order numbers of all
             * other entries with a higher order number to make the gap.
             */
            for (ColumnOrderInfo currentColumnOrderInfo : columnOrderInfos) {
                if (currentColumnOrderInfo.orderNumber > removedColumnOrderInfo.orderNumber) {
                    currentColumnOrderInfo.orderNumber--;
                }
            }

            buildAndSetColumnOrder();
        }

    }

    /**
     * The label provider for the enum values table viewer.
     */
    private class EnumValuesLabelProvider implements ITableLabelProvider {

        /** Image for validation errors. */
        private final Image errorImage = IpsPlugin.getDefault().getImage("ovr16/error_co.gif"); //$NON-NLS-1$

        /**
         * {@inheritDoc}
         */
        public Image getColumnImage(Object element, int columnIndex) {
            // Test for errors
            if (hasErrorsAt((IEnumValue)element, columnIndex)) {
                return errorImage;
            }

            return null;
        }

        /**
         * Returns <code>true</code> if the given enum value validation detects an error at the
         * given columnIndex, <code>false</code> otherwise.
         */
        private boolean hasErrorsAt(IEnumValue enumValue, int columnIndex) {
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();

            // Don't validate if the indicated column does not exist
            if (enumAttributeValues.size() <= columnIndex) {
                return false;
            }

            try {
                MessageList messageList = enumValue.validate(enumValue.getIpsProject());
                messageList = messageList.getMessagesFor(enumAttributeValues.get(columnIndex),
                        IEnumAttributeValue.PROPERTY_VALUE);
                return !(messageList.isEmpty());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public String getColumnText(Object element, int columnIndex) {
            // There need to be at least one column to be able to obtain label information
            if (enumValuesTableColumnNames.size() > 0) {
                IEnumValue enumValue = (IEnumValue)element;

                // For each requested column there must already be an enum attribute value
                List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
                if (enumAttributeValues.size() - 1 >= columnIndex) {

                    // Find the correct enum attribute value by the requested column's name
                    String columnName = enumValuesTable.getColumn(columnIndex).getText();
                    for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValues) {
                        try {
                            if (currentEnumAttributeValue.findEnumAttribute().getName().equals(columnName)) {

                                // Obtain the value and its datatype
                                String columnValue = currentEnumAttributeValue.getValue();
                                String datatype = currentEnumAttributeValue.findEnumAttribute().getDatatype();
                                ValueDatatype valueDatatype = currentEnumAttributeValue.getIpsProject()
                                        .findValueDatatype(datatype);

                                // Format value properly
                                return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                                        valueDatatype, columnValue);

                            }
                        } catch (CoreException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void addListener(ILabelProviderListener listener) {

        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {

        }

        /**
         * {@inheritDoc}
         */
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void removeListener(ILabelProviderListener listener) {

        }

    }

    /**
     * The cell modifier for the enum values table viewer.
     */
    private class EnumCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        public boolean canModify(Object element, String property) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Object getValue(Object element, String property) {
            if (element instanceof IEnumValue) {
                IEnumValue enumValue = (IEnumValue)element;

                int columnIndex = getColumnIndexByName(property);
                if (columnIndex != -1) {
                    return enumValue.getEnumAttributeValues().get(columnIndex).getValue();
                }
            }

            return null;
        }

        /** Returns the current index of the column identified by the given name. */
        private int getColumnIndexByName(String columnName) {
            int[] columnOrder = enumValuesTable.getColumnOrder();
            int columnIndex = -1;
            for (int i = 0; i < enumValuesTable.getColumnCount(); i++) {
                if (enumValuesTable.getColumn(columnOrder[i]).getText().equals(columnName)) {
                    columnIndex = i;
                    break;
                }
            }

            return columnIndex;
        }

        /**
         * {@inheritDoc}
         */
        public void modify(Object element, String property, Object value) {
            IEnumValue enumValue = null;
            if (element instanceof IEnumValue) {
                enumValue = (IEnumValue)element;
            }
            if (element instanceof Item) {
                enumValue = (IEnumValue)((Item)element).getData();
            } else {
                return;
            }

            int columnIndex = getColumnIndexByName(property);
            if (columnIndex != -1) {
                enumValue.getEnumAttributeValues().get(columnIndex).setValue((String)value);
                enumValuesTableViewer.refresh(true);
            }
        }

    }

    /**
     * Listener that reacts to <code>SelectionChangedEvent</code>s by deleting all empty rows at the
     * bottom of the table.
     * 
     * @author Stefan Widmaier
     */
    private class RowDeletor implements ISelectionChangedListener {

        /**
         * {@inheritDoc}
         */
        public void selectionChanged(SelectionChangedEvent event) {
            removeRedundantRows();
        }

        /**
         * Checks every row from the last up to the currently selected row for emptyness and deletes
         * every empty row until a non-empty row is found.
         * <p>
         * Only tries to delete rows if the table has more than one row.
         */
        private void removeRedundantRows() {
            int selectionIndex = enumValuesTable.getSelectionIndex();
            if (enumValuesTable.getItemCount() <= 1) {
                return;
            }

            for (int i = enumValuesTable.getItemCount() - 1; i > selectionIndex; i--) {
                IEnumValue currentEnumValue = (IEnumValue)enumValuesTableViewer.getElementAt(i);
                if (isRowEmpty(currentEnumValue)) {
                    enumValuesTableViewer.remove(currentEnumValue);
                    currentEnumValue.delete();
                } else {
                    break;
                }
            }
        }

        /**
         * Checks whether a row (enum value) is empty or not. Returns <code>true</code> if all the
         * given row's values (columns) contain a whitespace string.
         * <p>
         * <code>null</code> is treated as content. Thus a row that contains <code>null</code>
         * values is not empty.
         */
        private boolean isRowEmpty(IEnumValue enumValue) {
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();

            int columnNumber = enumValuesTable.getColumnCount();
            for (int i = 0; i < columnNumber; i++) {
                String value = enumAttributeValues.get(i).getValue();
                if (value == null || !(value.trim().equals(""))) { //$NON-NLS-1$
                    return false;
                }
            }

            return true;
        }
    }

}
