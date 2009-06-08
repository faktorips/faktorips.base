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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
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
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.EnumTypeDatatypeField;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The ui section for the enum type pages and the enum content page that contains the enum values
 * table to be edited.
 * <p>
 * If the ips object being edited is an enum type then in-place fixing of the enum values table will
 * be done. That means, if an enum attribute is added there will be a new column in the table, if an
 * enum attribute is deleted the corresponding table column will be deleted and so on.
 * <p>
 * Fixing the table when editing enum content objects is done manually by the user trough a separate
 * dialog.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeStructurePage
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeValuesPage
 * @see org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentPage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValuesSection extends IpsSection implements ContentsChangeListener {

    /** The image to show for columns that contain the identifier value. */
    private final static Image UNIQUE_IDENTIFIER_COLUMN_IMAGE = IpsUIPlugin.getDefault().getImage("TableKeyColumn.gif");

    /** The enum value container holding the enum values to be edited. */
    private IEnumValueContainer enumValueContainer;

    /** The ips project the enum value container to edit is stored in. */
    private IIpsProject ipsProject;

    /** The ui table widget. */
    private Table enumValuesTable;

    /** The jface table viewer linking the ui table with the model data. */
    private TableViewer enumValuesTableViewer;

    /** The names of the columns for the enum values table ui widget. */
    private List<String> columnNames;

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
    public EnumValuesSection(final IEnumValueContainer enumValueContainer, Composite parent, UIToolkit toolkit)
            throws CoreException {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(enumValueContainer);
        this.enumValueContainer = enumValueContainer;
        this.ipsProject = enumValueContainer.getIpsProject();
        this.columnNames = new ArrayList<String>(4);

        IEnumType enumType = enumValueContainer.findEnumType(ipsProject);

        initControls();
        createActions();
        createToolbar();
        createContextMenu();

        setText(Messages.EnumValuesSection_title);

        updateEnabledStates(enumType);
        createFirstRow(enumType);

        enumValueContainer.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            /**
             * {@inheritDoc}
             */
            public void widgetDisposed(DisposeEvent e) {
                enumValueContainer.getIpsModel().removeChangeListener(EnumValuesSection.this);
            }
        });
    }

    /**
     * Reinitializes the contents of this section:
     * <ul>
     * <li>The <code>columnNames</code> will be emptied and created anew.
     * <li>Every table column of the <code>enumValuesTable</code> will be disposed and the table
     * columns will be created anew.
     */
    private void reinit(IEnumType enumType) throws CoreException {
        // Clear column names
        columnNames.clear();

        // Dispose enum values table columns
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            currentColumn.dispose();
        }

        createTableColumns(enumType);
    }

    /**
     * Makes sure that there is at least one row in the enum values table if there are any
     * attributes in the enum type and the enum values table is enabled.
     */
    private void createFirstRow(IEnumType enumType) throws CoreException {
        if (enumType == null) {
            return;
        }

        if (enumType.isContainingValues() && !(enumType.isAbstract())) {
            if (enumType.getEnumAttributesCount(true) > 0 && enumValueContainer.getEnumValuesCount() == 0) {
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
    private void createToolbar() {
        // Create the toolbar
        Section section = getSectionControl();
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(section);

        // Add the actions to the toolbar
        toolBarManager.add(newEnumValueAction);
        toolBarManager.add(deleteEnumValueAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(moveEnumValueUpAction);
        toolBarManager.add(moveEnumValueDownAction);

        // TODO aw: align the toolbar at the right of the section title bar
        toolbar.setLocation(220, toolbar.getLocation().y + 1);

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
        boolean valuesArePartOfModel = (enumType != null) ? enumType.isContainingValues() : false;
        boolean isAbstract = (enumType != null) ? enumType.isAbstract() : false;

        if (enumValueContainer instanceof IEnumType) {
            newEnumValueAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            deleteEnumValueAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            moveEnumValueUpAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            moveEnumValueDownAction.setEnabled(valuesArePartOfModel && !(isAbstract));
            enumValuesTable.setEnabled(valuesArePartOfModel && !(isAbstract));
            getSectionControl().setEnabled(valuesArePartOfModel && !(isAbstract));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        try {
            IEnumType enumType = enumValueContainer.findEnumType(ipsProject);
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
        enumValuesTable = toolkit.createTable(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.MULTI
                | SWT.FULL_SELECTION);
        enumValuesTable.setHeaderVisible(true);
        enumValuesTable.setLinesVisible(true);

        // Fill all space
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = parent.getClientArea().width;
        tableGridData.heightHint = parent.getClientArea().height;
        enumValuesTable.setLayoutData(tableGridData);

        createTableColumns(enumType);
        increaseHeightOfTableRows();

        // Key listener for deleting rows with the DEL key
        enumValuesTable.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    deleteEnumValueAction.run();
                }
            }
        });
    }

    /**
     * Creates the columns of the enum values table based upon enum attribute values if there are
     * any enum values yet and the ips object to edit is an <code>IEnumContent</code>.
     * <p>
     * If this is not the case the enum attributes of the enum type are used to create the columns.
     */
    private void createTableColumns(IEnumType enumType) throws CoreException {
        if (enumValueContainer instanceof IEnumContent) {
            IEnumContent enumContent = (IEnumContent)enumValueContainer;
            IEnumType referencedEnumType = enumValueContainer.findEnumType(ipsProject);
            for (int i = 0; i < enumContent.getReferencedEnumAttributesCount(); i++) {
                boolean obtainNamesFromAttributes = true;
                if (referencedEnumType == null) {
                    obtainNamesFromAttributes = false;
                } else {
                    if (referencedEnumType.getEnumAttributesCount(true) != enumContent
                            .getReferencedEnumAttributesCount()) {
                        obtainNamesFromAttributes = false;
                    }
                }

                String columnName;
                boolean isIdentifier = false;
                if (obtainNamesFromAttributes) {
                    IEnumAttribute currentEnumAttribute = referencedEnumType.getEnumAttributesIncludeSupertypeCopies()
                            .get(i);
                    columnName = currentEnumAttribute.getName();
                    isIdentifier = currentEnumAttribute.isUniqueIdentifier();
                } else {
                    columnName = NLS.bind(Messages.EnumValuesSection_defaultColumnName, i + 1);
                }
                addTableColumn(columnName, isIdentifier);
            }

        } else {
            for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributesIncludeSupertypeCopies()) {
                addTableColumn(currentEnumAttribute.getName(), currentEnumAttribute.isUniqueIdentifier());
            }
        }
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

        String[] columnNamesArray = columnNames.toArray(new String[columnNames.size()]);

        // Update column properties
        enumValuesTableViewer.setColumnProperties(columnNamesArray);

        // Create cell editors
        IEnumType enumType = enumValueContainer.findEnumType(ipsProject);
        CellEditor[] cellEditors = createCellEditors(enumType, columnNamesArray);

        // Assign the cell editors to the table viewer
        enumValuesTableViewer.setCellEditors(cellEditors);

        // Refresh the viewer
        enumValuesTableViewer.refresh();
    }

    /** Updates the cell editors for the enum values table by recreating them. */
    private CellEditor[] createCellEditors(IEnumType enumType, String[] columnNames) throws CoreException {
        CellEditor[] cellEditors = new CellEditor[columnNames.length];
        for (int i = 0; i < cellEditors.length; i++) {
            // If the table is in an invalid state every cell will be represented as string
            String datatypeQualifiedName = "String";
            if (enumType != null) {
                boolean obtainDatatype = true;
                if (enumValueContainer instanceof IEnumContent) {
                    if (enumType.getEnumAttributesCount(true) != ((IEnumContent)enumValueContainer)
                            .getReferencedEnumAttributesCount()) {
                        obtainDatatype = false;
                    }
                }
                if (obtainDatatype) {
                    datatypeQualifiedName = enumType.getEnumAttributesIncludeSupertypeCopies().get(i).getDatatype();
                }
            }

            TableCellEditor cellEditor = null;
            ValueDatatype datatype = enumValueContainer.getIpsProject().findValueDatatype(datatypeQualifiedName);
//            if (datatype instanceof IEnumType) {
//                Combo combo = getToolkit().createCombo(enumValuesTableViewer.getTable());
//                //TODO for enum types without content the content must be found here and provided to the field
//                EnumTypeDatatypeField field = new EnumTypeDatatypeField(combo, (IEnumType)datatype, null);
//                combo.setData(field);
//                cellEditor = new ComboCellEditor(enumValuesTableViewer, i, combo);
//            } else {
                ValueDatatypeControlFactory valueDatatypeControlFactory = IpsUIPlugin.getDefault()
                        .getValueDatatypeControlFactory(datatype);
                
                //the ips project of the enum value container is provided to this method
                cellEditor = valueDatatypeControlFactory.createCellEditor(getToolkit(), datatype, null,
                        enumValuesTableViewer, i, enumValueContainer.getIpsProject());
//            }
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

    /** Adds a new column to the end of the enum values table with the given name. */
    private void addTableColumn(String columnName, boolean identifierColumnn) throws CoreException {
        // Add column to the table
        TableColumn newColumn = new TableColumn(enumValuesTable, SWT.LEFT);
        newColumn.setText(columnName);
        newColumn.setWidth(200);

        // Set identifier image as neccessary
        if (identifierColumnn) {
            newColumn.setImage(UNIQUE_IDENTIFIER_COLUMN_IMAGE);
        }

        // Add the name to the column names list
        columnNames.add(columnName);

        updateTableViewer();
    }

    /**
     * Renames the column identified by the given column name to the given new column name.
     */
    public void renameTableColumn(String columnName, String newColumnName) throws CoreException {
        if (!(columnNames.contains(columnName))) {
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
        for (int i = 0; i < columnNames.size(); i++) {
            String currentColumnName = columnNames.get(i);
            if (currentColumnName.equals(columnName)) {
                columnNames.set(i, newColumnName);
                break;
            }
        }

        updateTableViewer();
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

    /**
     * {@inheritDoc}
     * <p>
     * Initiates in-place fixing of the enum values table if the enum value container to be edited
     * is an enum type.
     * <p>
     * Updates the <code>originalOrderedAttributeValuesMap</code> and refreshes the
     * <code>enumValuesTableViewer</code> when enum values have been added, moved or removed.
     */
    public void contentsChanged(ContentChangeEvent event) {
        IEnumType enumType;
        try {
            enumType = enumValueContainer.findEnumType(ipsProject);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        /*
         * Return if the content changed was not the enum value container to be edited or the
         * referenced enum type.
         */
        if (!(event.getIpsSrcFile().equals(enumValueContainer.getIpsSrcFile()))) {
            if (enumType != null) {
                if (!(event.getIpsSrcFile().equals(enumType.getIpsSrcFile()))) {
                    return;
                }
            } else {
                return;
            }
        }

        switch (event.getEventType()) {

            case ContentChangeEvent.TYPE_PARTS_CHANGED_POSITIONS:
            case ContentChangeEvent.TYPE_PART_ADDED:
            case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                try {
                    IIpsObject changedIpsObject = event.getIpsSrcFile().getIpsObject();
                    if (enumValueContainer instanceof IEnumType) {
                        if (changedIpsObject instanceof IEnumType) {
                            IEnumType changedEnumType = (IEnumType)changedIpsObject;
                            reinit(changedEnumType);
                            updateEnabledStates(changedEnumType);
                        }
                    }
                    if (enumValueContainer instanceof IEnumContent) {
                        if (changedIpsObject instanceof IEnumContent) {
                            IEnumContent changedEnumContent = (IEnumContent)changedIpsObject;
                            IEnumType referencedEnumType = changedEnumContent.findEnumType(ipsProject);
                            if (referencedEnumType != null) {
                                reinit(referencedEnumType);
                            }
                        }
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }

                break;

            case ContentChangeEvent.TYPE_PROPERTY_CHANGED:
                IIpsObjectPart part = event.getPart();
                if (part != null) {
                    if (part instanceof IEnumAttribute) {
                        IEnumAttribute modifiedEnumAttribute = (IEnumAttribute)part;
                        IEnumType enumTypeModifiedEnumAttribute = modifiedEnumAttribute.getEnumType();

                        try {
                            String oldName = null;
                            for (String currentColumnName : columnNames) {
                                if (enumTypeModifiedEnumAttribute
                                        .getEnumAttributeIncludeSupertypeCopies(currentColumnName) == null) {
                                    oldName = currentColumnName;
                                    break;
                                }
                            }

                            // Something else but the name has changed
                            if (oldName == null) {
                                if (enumValueContainer instanceof IEnumType) {
                                    reinit(enumTypeModifiedEnumAttribute);
                                } else {
                                    updateTableViewer();
                                }
                                return;
                            }

                            renameTableColumn(oldName, modifiedEnumAttribute.getName());
                        } catch (CoreException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                break;
        }
    }

    /**
     * The content provider for the enum values table viewer.
     */
    private class EnumValuesContentProvider implements IStructuredContentProvider {

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

        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

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
                messageList = messageList.getMessagesFor(enumAttributeValues.get(columnIndex), null);
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
            if (columnNames.size() > 0) {
                IEnumValue enumValue = (IEnumValue)element;

                // For each requested column there must already be an enum attribute value
                List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
                if (enumAttributeValues.size() - 1 >= columnIndex) {

                    IEnumAttributeValue enumAttributeValue = enumAttributeValues.get(columnIndex);

                    /*
                     * Return text formatted by the ips datatype formatter if the referenced enum
                     * attribute can be found and so the datatype of the value is known. Return the
                     * value directly if the enum attribute cannot be found.
                     */
                    String columnValue = enumAttributeValue.getValue();
                    IEnumAttribute enumAttribute = null;
                    try {
                        enumAttribute = enumAttributeValue.findEnumAttribute(ipsProject);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                    if (enumAttribute == null) {
                        return columnValue;
                    }

                    try {
                        // Format value properly
                        String datatype = enumAttributeValue.findEnumAttribute(ipsProject).getDatatype();
                        ValueDatatype valueDatatype = enumAttributeValue.getIpsProject().findValueDatatype(datatype);
                        //TODO pk: in the case of an enum type without content the content must of it must be find
                        //here and provided to the formatter
                        if(valueDatatype instanceof IEnumType){
                            return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                                    (IEnumType)valueDatatype, null, columnValue);
                        }
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
