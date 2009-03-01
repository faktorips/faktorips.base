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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.osgi.util.NLS;
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
 * The ui section for the enum type pages and the enum content page that contains the enum values
 * table to be edited.
 * <p>
 * If the ips object being edited is an enum type then in-place fixing of the enum values table will
 * be done. That means, if an enum attribute is added there will be a new column in the table, if an
 * enum attribute is deleted the table column will be deleted and so on.
 * <p>
 * For fixing the table when editing enum content objects public methods are provided. Fixing the
 * table when editing enum content objects is done manually by the user trough separate dialogs.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeStructurePage
 * @see org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentPage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValuesSection extends IpsSection {

    /** The enum value container holding the enum values to be edited. */
    private IEnumValueContainer enumValueContainer;

    /** The ui table widget. */
    private Table enumValuesTable;

    /** The jface table viewer linking the ui table with the model data. */
    private TableViewer enumValuesTableViewer;

    /** Column order info list. */
    private List<ColumnOrderInfo> columnOrderInfos;

    /**
     * The ordering of the enum attribute values for each enum value as it was when the enum values
     * table has been created.
     */
    private Map<IEnumValue, List<IEnumAttributeValue>> originalOrderedAttributeValuesMap;

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
     * @throws NullPointerException If <code>enumValueContainer</code> is <code>null</code>.
     */
    public EnumValuesSection(IEnumValueContainer enumValueContainer, Composite parent, UIToolkit toolkit)
            throws CoreException {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(enumValueContainer);
        this.enumValueContainer = enumValueContainer;
        this.columnOrderInfos = new ArrayList<ColumnOrderInfo>(4);
        this.enumValuesTableColumnNames = new ArrayList<String>(4);

        this.originalOrderedAttributeValuesMap = new HashMap<IEnumValue, List<IEnumAttributeValue>>();
        for (IEnumValue currentEnumValue : enumValueContainer.getEnumValues()) {
            originalOrderedAttributeValuesMap.put(currentEnumValue, new ArrayList<IEnumAttributeValue>(0));
        }

        IEnumType enumType = enumValueContainer.findEnumType();

        initControls();
        createActions();
        createToolbar();
        createContextMenu();

        setText(Messages.EnumValuesSection_title);

        updateEnabledStates(enumType);

        if (enumType != null) {
            createFirstRow(enumType);
        }
    }

    /**
     * Makes sure that there is at least one row in the enum values table if there are any
     * attributes in the enum type and the enum values table is enabled.
     */
    private void createFirstRow(IEnumType enumType) throws CoreException {
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
     * TODO aw: align the toolbar at the right of the section title bar
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
    private void updateEnabledStates(IEnumType enumType) {
        boolean valuesArePartOfModel = (enumType != null) ? enumType.getValuesArePartOfModel() : false;
        boolean isAbstract = (enumType != null) ? enumType.isAbstract() : false;

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
            IEnumType enumType = enumValueContainer.findEnumType();
            createTable(enumType, client, toolkit);
            createTableViewer();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        createTableValidationHoverService();
    }

    /** Creates the ui table for editing enum values. */
    private void createTable(IEnumType enumType, Composite parent, UIToolkit toolkit) throws CoreException {
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
            /*
             * TODO aw: also consider columns of the other enum values, what to if the number of
             * enum attribute values differs?
             */
            IEnumValue enumValue = enumValueContainer.getEnumValues().get(0);
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
            for (int i = 0; i < enumAttributeValues.size(); i++) {
                IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
                IEnumAttribute currentEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
                String columnName = (currentEnumAttribute != null) ? currentEnumAttribute.getName() : NLS.bind(
                        Messages.EnumValuesSection_defaultColumnName, i + 1);
                enumAttributeAdded(columnName);
            }

        } else {
            if (enumType != null) {
                for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributes()) {
                    enumAttributeAdded(currentEnumAttribute);
                }
            }
        }

        increaseHeightOfTableRows();
    }

    /** Increases the height of the table rows slightly. */
    private void increaseHeightOfTableRows() {
        Listener paintListener = new Listener() {
            /**
             * {@inheritDoc}
             */
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
        IEnumType enumType = enumValueContainer.findEnumType();
        CellEditor[] cellEditors = createCellEditors(enumType, columnNames);

        // Assign the cell editors to the table viewer
        enumValuesTableViewer.setCellEditors(cellEditors);

        // Refresh the viewer
        enumValuesTableViewer.refresh();
    }

    /** Updates the cell editors for the enum values table by recreating them. */
    private CellEditor[] createCellEditors(IEnumType enumType, String[] columnNames) throws CoreException {
        CellEditor[] cellEditors = new CellEditor[columnNames.length];
        for (int i = 0; i < cellEditors.length; i++) {
            String datatypeQualifiedName = (enumType != null) ? enumType.getEnumAttributes().get(i).getDatatype()
                    : "String";
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

    /**
     * Adds a new table column with the name to the given enum attribute to the end of the enum
     * values table.
     * 
     * @param addedEnumAttribute The enum attribute that has been added to the referenced enum type.
     * 
     * @throws CoreException If an error occurs while searching for the enum type of the enum value
     *             container to be edited or while updating the table viewer of the enum values
     *             table.
     * @throws NullPointerException If <code>addedEnumAttribute</code> is <code>null</code>.
     * @throws IllegalStateException If this method is called while the referenced enum type is
     *             invalid.
     * @throws IllegalArgumentException If enum type referenced by the given enum attribute is not
     *             the enum type referenced by the enum value container to edit.
     */
    public void enumAttributeAdded(IEnumAttribute addedEnumAttribute) throws CoreException {
        ArgumentCheck.notNull(addedEnumAttribute);
        IEnumType enumType = enumValueContainer.findEnumType();
        if (enumType == null) {
            throw new IllegalStateException();
        }
        ArgumentCheck.isTrue((IEnumType)addedEnumAttribute.getParent() == enumType);

        String columnName = addedEnumAttribute.getName();
        enumAttributeAdded(columnName);
    }

    /** Adds a new column to the end of the enum values table with the given name. */
    private void enumAttributeAdded(String columnName) throws CoreException {
        // Add column to the table
        TableColumn newColumn = new TableColumn(enumValuesTable, SWT.LEFT);
        newColumn.setText(columnName);
        newColumn.setWidth(200);

        // Add column order info entry
        columnOrderInfos.add(new ColumnOrderInfo(columnOrderInfos.size(), newColumn));

        // Add the name to the column names list
        enumValuesTableColumnNames.add(columnName);

        // Add the new enum attribute values to the original mappings
        for (IEnumValue currentEnumValue : enumValueContainer.getEnumValues()) {
            IEnumAttributeValue enumAttributeValueToAdd = currentEnumValue.getEnumAttributeValues().get(
                    columnOrderInfos.size() - 1);
            originalOrderedAttributeValuesMap.get(currentEnumValue).add(enumAttributeValueToAdd);
        }

        updateTableViewer();
        buildAndSetColumnOrder();
    }

    /**
     * Removes the column with the name of the given enum attribute from the enum values table.
     * 
     * @param removedEnumAttribute The enum attribute that has been removed from the referenced enum
     *            type.
     * 
     * @throws CoreException If an error occurs while searching for the enum type of the enum value
     *             container to be edited or while updating the table viewer of the enum values
     *             table.
     * @throws NullPointerException If <code>removedEnumAttribute</code> is <code>null</code>.
     * @throws IllegalStateException If this method is called while the referenced enum type is
     *             invalid.
     * @throws IllegalArgumentException If the referenced enum type of the given enum attribute is
     *             not the enum type referenced by the enum value container to be edited.
     * @throws NoSuchElementException If no column with the name of the given enum attribute exists.
     */
    public void enumAttributeRemoved(IEnumAttribute removedEnumAttribute) throws CoreException {
        ArgumentCheck.notNull(removedEnumAttribute);
        IEnumType enumType = enumValueContainer.findEnumType();
        if (enumType == null) {
            throw new IllegalStateException();
        }
        ArgumentCheck.isTrue((IEnumType)removedEnumAttribute.getParent() == enumType);

        String columnName = removedEnumAttribute.getName();

        // Remove column order info entry
        ColumnOrderInfo removedColumnOrderInfo = null;
        for (ColumnOrderInfo currentColumnOrderInfo : columnOrderInfos) {
            if (currentColumnOrderInfo.tableColumn.getText().equals(columnName)) {
                removedColumnOrderInfo = currentColumnOrderInfo;
                columnOrderInfos.remove(currentColumnOrderInfo);
                break;
            }
        }

        if (removedColumnOrderInfo == null) {
            throw new NoSuchElementException();
        }

        // Dispose column from table
        int columnIndexToRemove = -1;
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            columnIndexToRemove++;
            if (currentColumn.getText().equals(columnName)) {
                currentColumn.dispose();
                break;
            }
        }

        /*
         * Now we removed the column order info entry but we still need to decrement some order
         * numbers by 1 because we now have one column less. If we had 4 column order entries before
         * and now have 3 our order numbers go only to 2 instead to 3. If we removed the entry with
         * order number 1 for example we need to decrement the order numbers of all other entries
         * with a higher order number to make the gap.
         */
        for (ColumnOrderInfo currentColumnOrderInfo : columnOrderInfos) {
            if (currentColumnOrderInfo.orderNumber > removedColumnOrderInfo.orderNumber) {
                currentColumnOrderInfo.orderNumber--;
            }
        }

        // Remove the name from the column names list
        enumValuesTableColumnNames.remove(columnName);

        // Remove enum attribute values from the original mappings
        int[] columnOrder = enumValuesTable.getColumnOrder();
        for (IEnumValue currentEnumValue : enumValueContainer.getEnumValues()) {
            originalOrderedAttributeValuesMap.get(currentEnumValue).remove(columnOrder[columnIndexToRemove]);
        }

        updateTableViewer();
        buildAndSetColumnOrder();
    }

    /**
     * Renames the column identified by the given column name to the name of the given enum
     * attribute.
     * 
     * @param renamedEnumAttribute The enum attribute that has been renamed.
     * @param columnName The name of the column to be renamed.
     * 
     * @throws CoreException If an error occurs while searching for the enum type of the enum value
     *             container to be edited or while updating the table viewer of the enum values
     *             table.
     * @throws NullPointerException If <code>renamedEnumAttribute</code> or <code>columnName</code>
     *             is <code>null</code>.
     * @throws IllegalStateException If this method is called while the referenced enum type is
     *             invalid.
     * @throws IllegalArgumentException If the referenced enum type of the given enum attribute is
     *             not the enum type referenced by the enum value container to be edited.
     */
    public void enumAttributeRenamed(IEnumAttribute renamedEnumAttribute, String columnName) throws CoreException {
        ArgumentCheck.notNull(new Object[] { renamedEnumAttribute, columnName });
        IEnumType enumType = enumValueContainer.findEnumType();
        if (enumType == null) {
            throw new IllegalStateException();
        }
        ArgumentCheck.isTrue((IEnumType)renamedEnumAttribute.getParent() == enumType);

        String newColumnName = renamedEnumAttribute.getName();

        if (!(enumValuesTableColumnNames.contains(columnName))) {
            throw new NoSuchElementException();
        }

        // Change name in table column
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            if (currentColumn.getText().equals(columnName)) {
                currentColumn.setText(newColumnName);
                break;
            }
        }

        // Update column names
        for (int i = 0; i < enumValuesTableColumnNames.size(); i++) {
            String currentColumnName = enumValuesTableColumnNames.get(i);
            if (currentColumnName.equals(columnName)) {
                enumValuesTableColumnNames.set(i, newColumnName);
                break;
            }
        }

        updateTableViewer();
    }

    /**
     * Interchanges the positions of the columns identified by their names by the given enum
     * attributes.
     * 
     * @param enumAttribute1 The first enum attribute to be moved.
     * @param enumAttribute2 The second enum attribute to be moved.
     * 
     * @throws CoreException If an error occurs while searching for the enum type of the enum value
     *             container to be edited.
     * @throws NullPointerException If <code>tableColumnName1</code> or
     *             <code>tableColumnName2</code> is <code>null</code>.
     * @throws IllegalStateException If this method is called while the referenced enum type is
     *             invalid.
     * @throws IllegalArgumentException If the referenced enum type of a given enum attribute is not
     *             the enum type referenced by the enum value container to be edited.
     * @throws NoSuchElementException If a table column can't be found because it does not exist.
     */
    public void enumAttributesMoved(IEnumAttribute enumAttribute1, IEnumAttribute enumAttribute2) throws CoreException {
        ArgumentCheck.notNull(new Object[] { enumAttribute1, enumAttribute2 });
        IEnumType enumType = enumValueContainer.findEnumType();
        if (enumType == null) {
            throw new IllegalStateException();
        }
        ArgumentCheck.isTrue((IEnumType)enumAttribute1.getParent() == enumType);
        ArgumentCheck.isTrue((IEnumType)enumAttribute2.getParent() == enumType);

        String tableColumnName1 = enumAttribute1.getName();
        String tableColumnName2 = enumAttribute2.getName();

        // Get the column order infos for the columns to move
        ColumnOrderInfo columnOrderInfo1 = null;
        ColumnOrderInfo columnOrderInfo2 = null;
        for (ColumnOrderInfo currentColumnOrderInfo : columnOrderInfos) {
            String currentText = currentColumnOrderInfo.tableColumn.getText();
            if (currentText.equals(tableColumnName1)) {
                columnOrderInfo1 = currentColumnOrderInfo;
            } else if (currentText.equals(tableColumnName2)) {
                columnOrderInfo2 = currentColumnOrderInfo;
            }

            if (columnOrderInfo1 != null && columnOrderInfo2 != null) {
                break;
            }
        }

        if (columnOrderInfo1 == null || columnOrderInfo2 == null) {
            throw new NoSuchElementException();
        }

        // Temporary save the contents of the first column order info for swapping
        int tempInfo1OrderNumber = columnOrderInfo1.orderNumber;
        TableColumn tempInfo1TableColumn = columnOrderInfo1.tableColumn;

        // Swap the contents of the column order infos
        columnOrderInfo1.tableColumn = columnOrderInfo2.tableColumn;
        columnOrderInfo1.orderNumber = columnOrderInfo2.orderNumber;
        columnOrderInfo2.tableColumn = tempInfo1TableColumn;
        columnOrderInfo2.orderNumber = tempInfo1OrderNumber;

        buildAndSetColumnOrder();
        enumValuesTableViewer.refresh();
    }

    /** Builds and sets the current column order from the column order info list. */
    private void buildAndSetColumnOrder() {
        // Build the new column order array for the enum values table
        int[] columnOrder = new int[columnOrderInfos.size()];
        for (int i = 0; i < columnOrderInfos.size(); i++) {
            columnOrder[i] = columnOrderInfos.get(i).orderNumber;
        }

        // Set the column order
        enumValuesTable.setColumnOrder(columnOrder);
    }

    /**
     * Returns the current index of the column identified by the given name. Returns
     * <code>null</code> if no column with the given name exists.
     */
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

    /** Structure to track the table column order. */
    private class ColumnOrderInfo {

        /** The current order number of the column. */
        private int orderNumber;

        /** The table column at this order number. */
        private TableColumn tableColumn;

        /** Creates a new <code>ColumnOrderInfo</code>. */
        private ColumnOrderInfo(int orderNumber, TableColumn tableColumn) {
            this.orderNumber = orderNumber;
            this.tableColumn = tableColumn;
        }

    }

    /**
     * The content provider for the enum values table viewer. Provides in-place fixing of the enum
     * values table for enum types being edited.
     */
    private class EnumValuesContentProvider implements IStructuredContentProvider, ContentsChangeListener {

        /**
         * Creates the <code>EnumValuesContentProvider</code> and registers itself as
         * <code>ContentsChangeListener</code> to the ips model.
         */
        public EnumValuesContentProvider() {
            enumValueContainer.getIpsModel().addChangeListener(this);
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
            /*
             * Return if the enum value container to be edited is not an enum type. In-place fixing
             * is only possible for enum types.
             */
            if (!(enumValueContainer instanceof IEnumType)) {
                return;
            }

            /*
             * Return if the content changed was not the enum value container to be edited.
             */
            if (!(event.getIpsSrcFile().equals(enumValueContainer.getIpsSrcFile()))) {
                return;
            }

            /*
             * Switch based upon event type.
             */
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
                                String oldName = null;
                                for (String currentColumnName : enumValuesTableColumnNames) {
                                    if (enumValueContainer.findEnumType().getEnumAttribute(currentColumnName) == null) {
                                        oldName = currentColumnName;
                                        break;
                                    }
                                }

                                // Something else but the name has changed
                                if (oldName == null) {
                                    return;
                                }

                                enumAttributeRenamed(modifiedEnumAttribute, oldName);
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
                    for (int i = 0; i < movedParts.length; i++) {
                        IIpsObjectPart currentIpsObjectPart = movedParts[i];
                        if (currentIpsObjectPart instanceof IEnumAttribute) {
                            movedEnumAttributes[i] = (IEnumAttribute)currentIpsObjectPart;
                        } else {
                            return;
                        }
                    }

                    // Get the indizes of the moved attributes
                    int index1 = -1;
                    int index2 = -1;
                    for (int i = 0; i < movedEnumAttributes.length; i++) {
                        if (!(columnOrderInfos.get(i).tableColumn.getText().equals(movedEnumAttributes[i].getName()))) {
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

                    // Should theoretically never happen
                    if (index1 == -1 || index2 == -1) {
                        throw new NoSuchElementException();
                    }

                    try {
                        enumAttributesMoved((IEnumAttribute)movedParts[index1], (IEnumAttribute)movedParts[index2]);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }

                    break;

                case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                    try {
                        IIpsObject changedIpsObject = event.getIpsSrcFile().getIpsObject();
                        if (changedIpsObject instanceof IEnumType) {
                            updateEnabledStates((IEnumType)changedIpsObject);
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }

                    break;
            }
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
                messageList = messageList.getMessagesFor(originalOrderedAttributeValuesMap.get(enumValue).get(
                        columnIndex), IEnumAttributeValue.PROPERTY_VALUE);
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

                    IEnumAttributeValue enumAttributeValue = originalOrderedAttributeValuesMap.get(enumValue).get(
                            columnIndex);

                    /*
                     * Return text formatted by the ips datatype formatter if the referenced enum
                     * attribute can be found and so the datatype of the value is known. Return the
                     * value directly if the enum attribute cannot be found.
                     */
                    String columnValue = enumAttributeValue.getValue();
                    IEnumAttribute enumAttribute = null;
                    try {
                        enumAttribute = enumAttributeValue.findEnumAttribute();
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                    if (enumAttribute == null) {
                        return columnValue;
                    }

                    try {
                        // Format value properly
                        String datatype = enumAttributeValue.findEnumAttribute().getDatatype();
                        ValueDatatype valueDatatype = enumAttributeValue.getIpsProject().findValueDatatype(datatype);
                        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                                valueDatatype, columnValue);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
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
