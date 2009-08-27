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
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The UI section for the <tt>EnumTypePage</tt> and the <tt>EnumContentPage</tt> that contains the
 * <tt>enumValuesTable</tt> to be edited.
 * <p>
 * If the IPS object being edited is an <tt>IEnumType</tt> then in-place fixing of the
 * <tt>enumValuesTable</tt> will be done. That means, if an <tt>IEnumAttribute</tt> is added there
 * will be a new column in the table, if an <tt>IEnumAttribute</tt> is deleted the corresponding
 * table column will be deleted and so on.
 * <p>
 * Fixing the table when editing <tt>IEnumContent</tt> objects is done manually by the user trough a
 * separate dialog.
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

    /** The <tt>IEnumValueContainer</tt> holding the <tt>IEnumValue</tt>s to be edited. */
    private IEnumValueContainer enumValueContainer;

    /** The <tt>IIpsProject</tt> the <tt>IEnumValueContainer</tt> to edit is stored in. */
    private IIpsProject ipsProject;

    /** The UI table widget. */
    private Table enumValuesTable;

    /** The JFace table viewer linking the UI table with the model data. */
    private TableViewer enumValuesTableViewer;

    /** The names of the columns for the <tt>enumValuesTable</tt> UI widget. */
    private List<String> columnNames;

    /** Action to add new <tt>IEnumValue</tt>s. */
    private IAction newEnumValueAction;

    /** Action to delete <tt>IEnumValue</tt>s. */
    private IAction deleteEnumValueAction;

    /** Action to move <tt>IEnumValue</tt>s up by 1. */
    private IAction moveEnumValueUpAction;

    /** Action to move <tt>IEnumValue</tt>s down by 1. */
    private IAction moveEnumValueDownAction;

    /**
     * Action that locks the literal name column and synchronizes it's values with the values of the
     * default provider column.
     */
    private IAction lockAndSyncLiteralNameAction;

    /** Action to reset all literal names to the values of their respective default providers. */
    private IAction resetLiteralNamesAction;

    /** Flag indicating whether the 'Lock and Synchronize Literal Names' option is currently active. */
    private boolean lockAndSynchronizeLiteralNames;

    /**
     * Flag indicating whether the section is used to edit the <tt>IEnumValue</tt>s of an
     * <tt>IEnumType</tt> (<tt>true</tt>) or an <tt>IEnumContent</tt> (<tt>false</tt>).
     */
    private boolean enumTypeEditing;

    /**
     * Creates a new <tt>EnumValuesSection</tt> containing the <tt>IEnumValue</tt>s of the given
     * <tt>IEnumValueContainer</tt>.
     * 
     * @param enumValueContainer The <tt>IEnumValue</tt>s of this <tt>IEnumValueContainer</tt> will
     *            be shown.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit that shall be used to create UI elements.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IEnumType</tt>
     *             referenced by the IPS object being edited.
     * @throws NullPointerException If <tt>enumValueContainer</tt> is <tt>null</tt>.
     */
    public EnumValuesSection(final IEnumValueContainer enumValueContainer, Composite parent, UIToolkit toolkit)
            throws CoreException {

        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(enumValueContainer);

        this.enumValueContainer = enumValueContainer;
        ipsProject = enumValueContainer.getIpsProject();
        columnNames = new ArrayList<String>(4);
        enumTypeEditing = enumValueContainer instanceof IEnumType;

        initControls();
        createActions();
        createToolbar();
        createContextMenu();
        setText(Messages.EnumValuesSection_title);

        updateEnabledStates();
        toggleLockAndSyncLiteralNames();

        registerAsChangeListenerToEnumValueContainer();
    }

    /**
     * Registers this section as <tt>ChangeListener</tt> to the <tt>IEnumValueContainer</tt> that is
     * being edited.
     */
    private void registerAsChangeListenerToEnumValueContainer() {
        enumValueContainer.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                enumValueContainer.getIpsModel().removeChangeListener(EnumValuesSection.this);
            }
        });
    }

    /** Toggles the 'Lock and Synchronize Literal Names' option. */
    void toggleLockAndSyncLiteralNames() throws CoreException {
        if (!enumTypeEditing || (!enumValueContainer.isCapableOfContainingValues())) {
            return;
        }

        lockAndSynchronizeLiteralNames = !lockAndSynchronizeLiteralNames;
        lockAndSyncLiteralNameAction.setChecked(lockAndSynchronizeLiteralNames);
        if (lockAndSynchronizeLiteralNames) {
            resetLiteralNamesAction.run();
        }
        updateSkippedColumns();
    }

    private void updateSkippedColumns() {
        final CellEditor[] cellEditors = enumValuesTableViewer.getCellEditors();
        if (cellEditors == null) {
            return;
        }

        IEnumType enumType = (IEnumType)enumValueContainer;
        // TODO AW: REFACTOR - provide getIndexOfEnumLiteralNameAttribute() in IEnumType.
        final int literalNameIndex = enumType.getIndexOfEnumAttribute(enumType.getEnumLiteralNameAttribute());
        if (lockAndSynchronizeLiteralNames) {
            for (CellEditor cellEditor : cellEditors) {
                ((TableCellEditor)cellEditor).addSkippedColumnIndex(literalNameIndex);
            }
            if (cellEditors.length - 1 < literalNameIndex) {
                return;
            }
            cellEditors[literalNameIndex].getControl().addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (lockAndSynchronizeLiteralNames) {
                        cellEditors[literalNameIndex].deactivate();
                    }
                }

                public void focusLost(FocusEvent e) {

                }
            });
        } else {
            for (CellEditor cellEditor : cellEditors) {
                ((TableCellEditor)cellEditor).removeSkippedColumnIndex(literalNameIndex);
            }
        }
    }

    /**
     * Reinitializes the contents of this section:
     * <ul>
     * <li>The <tt>columnNames</tt> will be emptied and created anew.
     * <li>Every table column of the <tt>enumValuesTable</tt> will be disposed and the table columns
     * will be created anew.
     * 
     * @param enumType The assigned <tt>IEnumType</tt> or <tt>null</tt> if none exists (only allowed
     *            for editing EnumContent however).
     */
    public void reinit(IEnumType enumType) throws CoreException {
        columnNames.clear();
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            currentColumn.dispose();
        }
        createTableColumns(enumType);
    }

    /** Creates the tool bar / context menu actions. */
    private void createActions() {
        newEnumValueAction = new NewEnumValueAction(enumValuesTableViewer);
        deleteEnumValueAction = new DeleteEnumValueAction(enumValuesTableViewer);
        moveEnumValueUpAction = new MoveEnumValueAction(enumValuesTableViewer, true);
        moveEnumValueDownAction = new MoveEnumValueAction(enumValuesTableViewer, false);

        if (enumTypeEditing) {
            lockAndSyncLiteralNameAction = new LockAndSyncLiteralNameAction(this);
            resetLiteralNamesAction = new ResetLiteralNamesAction(enumValuesTableViewer, (IEnumType)enumValueContainer);
        }
    }

    /** Creates the section's tool bar. */
    private void createToolbar() {
        Section section = getSectionControl();
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(section);

        toolBarManager.add(newEnumValueAction);
        toolBarManager.add(deleteEnumValueAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(moveEnumValueUpAction);
        toolBarManager.add(moveEnumValueDownAction);

        if (enumTypeEditing) {
            toolBarManager.add(new Separator());
            toolBarManager.add(lockAndSyncLiteralNameAction);
        }

        toolBarManager.update(true);
        section.setTextClient(toolbar); // Aligns the tool bar to the right.
    }

    /** Updates the enabled states of the <tt>enumValuesTable</tt> and its actions. */
    private void updateEnabledStates() throws CoreException {
        boolean enabled = enumValueContainer.isCapableOfContainingValues();
        if (enumTypeEditing) {
            newEnumValueAction.setEnabled(enabled);
            deleteEnumValueAction.setEnabled(enabled);
            moveEnumValueUpAction.setEnabled(enabled);
            moveEnumValueDownAction.setEnabled(enabled);
            lockAndSyncLiteralNameAction.setEnabled(enabled);
            enumValuesTable.setEnabled(enabled);
            getSectionControl().setEnabled(enabled);
        } else {
            newEnumValueAction.setEnabled(enabled);
            deleteEnumValueAction.setEnabled(enabled);
        }
    }

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

    /** Creates the UI table for editing <tt>IEnumValue</tt>s. */
    private void createTable(IEnumType enumType, Composite parent, UIToolkit toolkit) throws CoreException {
        // Create the UI widget.
        enumValuesTable = toolkit.createTable(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.MULTI
                | SWT.FULL_SELECTION);
        enumValuesTable.setHeaderVisible(true);
        enumValuesTable.setLinesVisible(true);

        // Fill all space.
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = parent.getClientArea().width;
        tableGridData.heightHint = parent.getClientArea().height;
        enumValuesTable.setLayoutData(tableGridData);

        createTableColumns(enumType);
        increaseHeightOfTableRows();

        // Key listener for deleting rows with the DEL key.
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
     * Creates the columns of the <tt>enumValuesTable</tt> based upon <tt>IEnumAttributeValue</tt>s
     * if there are any <tt>IEnumValue</tt>s yet and the IPS object to edit is an
     * <tt>IEnumContent</tt>.
     * <p>
     * If this is not the case the <tt>IEnumAttribute</tt>s of the <tt>IEnumType</tt> are used to
     * create the columns.
     */
    private void createTableColumns(IEnumType enumType) throws CoreException {
        if (enumTypeEditing) {
            for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributesIncludeSupertypeCopies(true)) {
                Boolean uniqueBoolean = currentEnumAttribute.findIsUnique(ipsProject);
                addTableColumn(currentEnumAttribute.getName(), ((uniqueBoolean == null) ? false : uniqueBoolean
                        .booleanValue()));
            }
        } else {
            IEnumContent enumContent = (IEnumContent)enumValueContainer;
            IEnumType referencedEnumType = enumValueContainer.findEnumType(ipsProject);
            List<IEnumAttributeReference> enumAttributeReferences = enumContent.getEnumAttributeReferences();
            for (int i = 0; i < enumContent.getEnumAttributeReferencesCount(); i++) {
                Boolean identifierBoolean = null;
                if (!(enumContent.isFixToModelRequired())) {
                    IEnumAttribute currentEnumAttribute = referencedEnumType.getEnumAttributesIncludeSupertypeCopies(
                            false).get(i);
                    identifierBoolean = currentEnumAttribute.findIsUnique(ipsProject);
                }
                addTableColumn(enumAttributeReferences.get(i).getName(), (identifierBoolean == null) ? false
                        : identifierBoolean.booleanValue());
            }
        }
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
                    // The height will be increased by 5 pixel.
                    event.height = Math.max(event.height, size.y + 5);
                }
            }
        };
        enumValuesTable.addListener(SWT.MeasureItem, paintListener);
    }

    /** Creates the context menu for the <tt>enumValuesTable</tt>. */
    private void createContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$

        menuMgr.add(newEnumValueAction);
        menuMgr.add(deleteEnumValueAction);
        menuMgr.add(new Separator());
        menuMgr.add(moveEnumValueUpAction);
        menuMgr.add(moveEnumValueDownAction);

        if (enumTypeEditing) {
            menuMgr.add(new Separator());
            menuMgr.add(resetLiteralNamesAction);
        }

        Menu menu = menuMgr.createContextMenu(enumValuesTable);
        enumValuesTable.setMenu(menu);
    }

    /** Creates the table viewer for the <tt>enumValuesTable</tt>. */
    private void createTableViewer() throws CoreException {
        enumValuesTableViewer = new TableViewer(enumValuesTable);
        enumValuesTableViewer.setUseHashlookup(true);

        updateTableViewer();

        enumValuesTableViewer.setContentProvider(new EnumValuesContentProvider());
        enumValuesTableViewer.setLabelProvider(new EnumValuesLabelProvider());
        enumValuesTableViewer.setInput(enumValueContainer);
        enumValuesTableViewer.setCellModifier(new EnumCellModifier());

        // Set the RowDeletor listener to automatically delete empty rows at the end of the table.
        enumValuesTableViewer.addSelectionChangedListener(new RowDeletor());
    }

    /** Creates the hover service for validation messages for the <tt>enumValuesTableViewer</tt>. */
    private void createTableValidationHoverService() {
        new TableMessageHoverService(enumValuesTableViewer) {

            @Override
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element != null) {
                    return ((IEnumValue)element).validate(enumValueContainer.getIpsProject());
                }
                return null;
            }

        };
    }

    /**
     * Updates the cell editors for the <tt>enumValuesTableViewer</tt> by creating them anew and
     * overwrites the column properties with actual data. Also refreshes the viewer with actual
     * model data.
     */
    private void updateTableViewer() throws CoreException {
        if (enumValuesTableViewer == null) {
            return;
        }

        String[] columnNamesArray = columnNames.toArray(new String[columnNames.size()]);
        enumValuesTableViewer.setColumnProperties(columnNamesArray);

        IEnumType enumType = enumValueContainer.findEnumType(ipsProject);
        CellEditor[] cellEditors = createCellEditors(enumType, columnNamesArray);
        enumValuesTableViewer.setCellEditors(cellEditors);
        if (enumTypeEditing) {
            updateSkippedColumns();
        }
        enumValuesTableViewer.refresh();
    }

    /** Updates the cell editors for the <tt>enumValuesTable</tt> by recreating them. */
    private CellEditor[] createCellEditors(IEnumType enumType, String[] columnNames) throws CoreException {
        // Obtain information about enumeration attributes needed later.
        List<IEnumAttribute> enumAttributes = new ArrayList<IEnumAttribute>();
        IEnumAttribute defaultValueProviderAttribute = null;
        IEnumLiteralNameAttribute literalNameAttribute = null;
        if (enumType != null) {
            enumAttributes.addAll(enumType.getEnumAttributesIncludeSupertypeCopies(enumTypeEditing));
            literalNameAttribute = enumType.getEnumLiteralNameAttribute();
            if (literalNameAttribute != null) {
                defaultValueProviderAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(literalNameAttribute
                        .getDefaultValueProviderAttribute());
            }
        }

        CellEditor[] cellEditors = new CellEditor[columnNames.length];
        for (int i = 0; i < cellEditors.length; i++) {
            /*
             * If an EnumContent is being edited while in an invalid state every cell will be
             * represented as String.
             */
            ValueDatatype datatype = Datatype.STRING;
            // TODO AW: REFACTOR - pull up method isFixToModelRequired() to IEnumValueContainer.
            if (enumTypeEditing ? true : !(((IEnumContent)enumValueContainer).isFixToModelRequired())) {
                IEnumAttribute referencedEnumAttribute = enumAttributes.get(i);
                datatype = referencedEnumAttribute.findDatatype(enumValueContainer.getIpsProject());
            }

            ValueDatatypeControlFactory valueDatatypeControlFactory = IpsUIPlugin.getDefault()
                    .getValueDatatypeControlFactory(datatype);
            TableCellEditor cellEditor = valueDatatypeControlFactory.createCellEditor(getToolkit(), datatype, null,
                    enumValuesTableViewer, i, enumValueContainer.getIpsProject());
            cellEditor.setRowCreating(true);
            cellEditors[i] = cellEditor;
        }

        if (literalNameAttribute != null && enumTypeEditing) {
            int literalNameIndex = enumType.getIndexOfEnumAttribute(literalNameAttribute);
            if (defaultValueProviderAttribute != null) {
                int defaultProviderIndex = enumType.getIndexOfEnumAttribute(defaultValueProviderAttribute);
                if (cellEditors.length - 1 >= defaultProviderIndex && cellEditors.length - 1 >= literalNameIndex) {
                    addDefaultProviderListenerToCellEditor(cellEditors[defaultProviderIndex], defaultProviderIndex,
                            literalNameIndex);
                }
            }
        }

        return cellEditors;
    }

    /**
     * Attaches a <tt>FocusListener</tt> to the control of the given cell editor responsible for
     * filling the field with the default value for the literal name obtained from the default value
     * provider attribute.
     */
    private void addDefaultProviderListenerToCellEditor(final CellEditor defaultProviderCellEditor,
            final int defaultValueProviderAttributeIndex,
            final int literalNameAttributeIndex) {

        defaultProviderCellEditor.addListener(new ICellEditorListener() {

            public void applyEditorValue() {
                // Default provider control must be a text control if valid.
                if (!(defaultProviderCellEditor.getControl() instanceof Text)) {
                    return;
                }

                int rowNumber = enumValuesTable.getSelectionIndex();
                if (enumValueContainer.getEnumValuesCount() - 1 < rowNumber || rowNumber == -1) {
                    return;
                }
                IEnumValue enumValue = enumValueContainer.getEnumValues().get(rowNumber);
                List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();

                String value = ((Text)defaultProviderCellEditor.getControl()).getText();
                if (value == null ? false : value.length() > 0) {
                    IEnumAttributeValue literalNameAttributeValue = enumAttributeValues.get(literalNameAttributeIndex);
                    String existingLiteral = literalNameAttributeValue.getValue();
                    if ((existingLiteral == null) ? true : existingLiteral.length() == 0
                            || existingLiteral.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())
                            || lockAndSynchronizeLiteralNames) {
                        if (value.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
                            literalNameAttributeValue.setValue(null);
                        } else {
                            literalNameAttributeValue.setValueAsLiteralName(value);
                        }
                        enumValuesTableViewer.refresh();
                    }
                }
            }

            public void cancelEditor() {

            }

            public void editorValueChanged(boolean oldValidState, boolean newValidState) {

            }

        });
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    /** Adds a new column to the end of the <tt>enumValuesTable</tt> with the given name. */
    private void addTableColumn(String columnName, boolean identifierColumnn) throws CoreException {
        TableColumn newColumn = new TableColumn(enumValuesTable, SWT.LEFT);
        newColumn.setText(columnName);
        newColumn.setWidth(200);

        if (identifierColumnn) {
            newColumn.setImage(UNIQUE_IDENTIFIER_COLUMN_IMAGE);
        }

        columnNames.add(columnName);
        updateTableViewer();
    }

    /** Renames the column identified by the given column name to the given new column name. */
    public void renameTableColumn(String columnName, String newColumnName) throws CoreException {
        if (!(columnNames.contains(columnName))) {
            throw new NoSuchElementException();
        }

        // Change name in table column.
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            if (currentColumn.getText().equals(columnName)) {
                currentColumn.setText(newColumnName);
                break;
            }
        }

        // Update column names.
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
     * Returns the current index of the column identified by the given name. Returns <tt>null</tt>
     * if no column with the given name exists.
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
     * Initiates in-place fixing of the <tt>enumValuesTable</tt> if the <tt>IEnumValueContainer</tt>
     * to be edited is an <tt>IEnumType</tt>.
     * <p>
     * Updates the <tt>originalOrderedAttributeValuesMap</tt> and refreshes the
     * <tt>enumValuesTableViewer</tt> when <tt>IEnumValue</tt>s have been added, moved or removed.
     */
    public void contentsChanged(ContentChangeEvent event) {
        IEnumType enumType;
        try {
            enumType = enumValueContainer.findEnumType(ipsProject);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        /*
         * Return if the content changed was not the EnumValueContainer to be edited or the
         * referenced EnumType.
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
                    if (enumTypeEditing) {
                        if (changedIpsObject instanceof IEnumType) {
                            IEnumType changedEnumType = (IEnumType)changedIpsObject;
                            reinit(changedEnumType);
                            updateEnabledStates();
                        }
                    } else {
                        if (changedIpsObject instanceof IEnumContent) {
                            IEnumContent changedEnumContent = (IEnumContent)changedIpsObject;
                            IEnumType referencedEnumType = changedEnumContent.findEnumType(ipsProject);
                            if (referencedEnumType != null) {
                                reinit(referencedEnumType);
                            }
                            updateEnabledStates();
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

                            // Something else but the name has changed.
                            if (oldName == null) {
                                if (enumTypeEditing) {
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

    /** The content provider for the <tt>enumValuesTableViewer</tt>. */
    private class EnumValuesContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return enumValueContainer.getEnumValues().toArray();
        }

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

    }

    /** The label provider for the <tt>enumValuesTableViewer</tt>. */
    private class EnumValuesLabelProvider implements ITableLabelProvider {

        /** Image for validation errors. */
        private final Image errorImage = IpsPlugin.getDefault().getImage("ovr16/error_co.gif");

        public Image getColumnImage(Object element, int columnIndex) {
            if (hasErrorsAt((IEnumValue)element, columnIndex)) {
                return errorImage;
            }
            return null;
        }

        /**
         * Returns <tt>true</tt> if the validation of the given <tt>IEnumValue</tt> detects an error
         * at the given columnIndex, <tt>false</tt> otherwise.
         */
        private boolean hasErrorsAt(IEnumValue enumValue, int columnIndex) {
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
            // Don't validate if the indicated column does not exist.
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

        public String getColumnText(Object element, int columnIndex) {
            // There need to be at least one column to be able to obtain label information.
            if (columnNames.size() > 0) {
                IEnumValue enumValue = (IEnumValue)element;

                // For each requested column there must already be an EnumAttributeValue.
                List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
                if (enumAttributeValues.size() - 1 >= columnIndex) {

                    IEnumAttributeValue enumAttributeValue = enumAttributeValues.get(columnIndex);
                    /*
                     * Return text formatted by the IPS DatatypeFormatter if the referenced
                     * EnumAttribute can be found and so the data type of the value is known. Return
                     * the value directly if the EnumAttribute cannot be found.
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
                        // Format value properly.
                        String datatype = enumAttributeValue.findEnumAttribute(ipsProject).getDatatype();
                        ValueDatatype valueDatatype = enumAttributeValue.getIpsProject().findValueDatatype(datatype);
                        if (valueDatatype instanceof EnumTypeDatatypeAdapter) {
                            return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                                    valueDatatype, columnValue);
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

        public void addListener(ILabelProviderListener listener) {

        }

        public void dispose() {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {

        }

    }

    /** The cell modifier for the enumeration values table viewer. */
    private class EnumCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         * <p>
         * Returns <tt>true</tt>.
         */
        public boolean canModify(Object element, String property) {
            return true;
        }

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
                // Do not modify if it is the literal name column and lock and sync is active!
                if (lockAndSynchronizeLiteralNames) {
                    IEnumType enumType = (IEnumType)enumValueContainer;
                    IEnumLiteralNameAttribute literalNameAttribute = enumType.getEnumLiteralNameAttribute();
                    int literalNameIndex = (literalNameAttribute == null) ? -1 : enumType
                            .getIndexOfEnumAttribute(literalNameAttribute);
                    if (literalNameIndex == columnIndex) {
                        return;
                    }
                }
                enumValue.getEnumAttributeValues().get(columnIndex).setValue((String)value);
                enumValuesTableViewer.refresh(true);
            }
        }

    }

    /**
     * Listener that reacts to <tt>SelectionChangedEvent</tt>s by deleting all empty rows at the
     * bottom of the table.
     */
    private class RowDeletor implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            removeRedundantRows();
        }

        /**
         * Checks every row from the last up to the currently selected row for emptiness and deletes
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
         * Checks whether a row (<tt>IEnumValue</tt>) is empty or not. Returns <tt>true</tt> if all
         * the given row's values (columns) contain a whitespace string.
         * <p>
         * The value <tt>null</tt> is treated as content. Thus a row that contains <tt>null</tt>
         * values is not empty.
         */
        private boolean isRowEmpty(IEnumValue enumValue) {
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
            boolean allValuesEmpty = true;
            for (IEnumAttributeValue attrValue : enumAttributeValues) {
                String value = attrValue.getValue();
                if (value == null) {
                    continue;
                }
                /*
                 * TODO pk 10-07-2009: this is not really correct. We actually need an
                 * empty-string-representation-value
                 */
                if (!(value.trim().equals(""))) {
                    allValuesEmpty = false;
                    break;
                }
            }
            return allValuesEmpty;
        }
    }

}
