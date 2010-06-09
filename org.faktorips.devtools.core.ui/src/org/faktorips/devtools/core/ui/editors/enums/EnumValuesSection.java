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
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
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
import org.faktorips.devtools.core.model.enums.EnumUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.devtools.core.ui.IpsRefactoringOperation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The UI section for the <tt>EnumTypeEditorPage</tt> and the <tt>EnumContentEditorPage</tt> that
 * contains the <tt>enumValuesTable</tt> to be edited.
 * <p>
 * If the IPS object being edited is an <tt>IEnumType</tt> then in-place fixing of the
 * <tt>enumValuesTable</tt> will be done. That means, if an <tt>IEnumAttribute</tt> is added there
 * will be a new column in the table, if an <tt>IEnumAttribute</tt> is deleted the corresponding
 * table column will be deleted and so on.
 * <p>
 * Fixing the table when editing <tt>IEnumContent</tt> objects is done manually by the user trough a
 * separate dialog.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeEditorPage
 * @see org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentEditorPage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValuesSection extends IpsSection implements ContentsChangeListener {

    /** Key to store the state of the action in the dialog settings. */
    private static final String SETTINGS_KEY_LOCK_AND_SYNC = "lockAndSyncLiteralNames"; //$NON-NLS-1$

    /** The <tt>IEnumValueContainer</tt> holding the <tt>IEnumValue</tt>s to be edited. */
    private IEnumValueContainer enumValueContainer;

    /**
     * The <tt>IEnumType</tt> holding the <tt>IEnumValue</tt>s to be edited or <tt>null</tt> if an
     * <tt>IEnumContent</tt> is being edited.
     */
    private IEnumType enumType;

    /**
     * The <tt>IEnumContent</tt> holding the <tt>IEnumValue</tt>s to be edited or <tt>null</tt> if
     * an <tt>IEnumType</tt> is being edited.
     */
    private IEnumContent enumContent;

    /** The <tt>IIpsProject</tt> the <tt>IEnumValueContainer</tt> to edit is stored in. */
    private IIpsProject ipsProject;

    /** The UI table widget. */
    private Table enumValuesTable;

    /** The JFace table viewer linking the UI table with the model data. */
    private TableViewer enumValuesTableViewer;

    /** The current cell editors that are being used in the table viewer. */
    private CellEditor[] cellEditors;

    /**
     * The names of the columns for the table. This information is stored to make renaming possible.
     */
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

        if (enumValueContainer instanceof IEnumType) {
            enumTypeEditing = true;
            enumType = (IEnumType)enumValueContainer;
        } else {
            enumContent = (IEnumContent)enumValueContainer;
        }

        loadDialogSettings();
        initControls();
        createActions();
        createToolbar();
        createContextMenu();
        setText(Messages.EnumValuesSection_title);

        updateEnabledStates();

        registerAsChangeListenerToEnumValueContainer();
    }

    private void loadDialogSettings() {
        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings();
        if (enumTypeEditing) {
            lockAndSynchronizeLiteralNames = settings.getBoolean(SETTINGS_KEY_LOCK_AND_SYNC);
        } else {
            lockAndSynchronizeLiteralNames = false;
        }
    }

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

    /** Creates the UI table for editing <tt>IEnumValue</tt>s. */
    private void createTable(Composite parent, UIToolkit toolkit) throws CoreException {
        enumValuesTable = toolkit.createTable(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.MULTI
                | SWT.FULL_SELECTION);
        enumValuesTable.setHeaderVisible(true);
        enumValuesTable.setLinesVisible(true);

        // Fill all space.
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = parent.getClientArea().width;
        tableGridData.heightHint = parent.getClientArea().height;
        enumValuesTable.setLayoutData(tableGridData);

        createTableColumns();
        increaseHeightOfTableRows();

        // Key listener for deleting rows with the DEL key.
        enumValuesTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Nothing to do when key pressed.
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    deleteEnumValueAction.run();
                }
            }
        });
    }

    /** Creates the table columns. */
    private void createTableColumns() throws CoreException {
        if (enumTypeEditing) {
            createTableColumnsForEnumType();
        } else {
            createTableColumnsForEnumContent();
        }
    }

    /**
     * Creates the table columns based on the <tt>IEnumAttribute</tt>s of the <tt>IEnumType</tt> to
     * edit.
     */
    private void createTableColumnsForEnumType() throws CoreException {
        for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributesIncludeSupertypeCopies(true)) {
            addTableColumn(currentEnumAttribute.getName(), EnumUtil.findEnumAttributeIsUnique(currentEnumAttribute,
                    ipsProject));
        }
    }

    /**
     * Creates the table columns based upon the <tt>IEnumAttributeReference</tt>s (and upon the
     * <tt>IEnumAttribute</tt>s of the referenced <tt>IEnumType</tt> if possible).
     */
    private void createTableColumnsForEnumContent() throws CoreException {
        IEnumType referencedEnumType = enumContent.findEnumType(ipsProject);
        List<IEnumAttributeReference> enumAttributeReferences = enumContent.getEnumAttributeReferences();
        for (int i = 0; i < enumContent.getEnumAttributeReferencesCount(); i++) {
            if (enumContent.isFixToModelRequired()) {
                addTableColumn(enumAttributeReferences.get(i).getName(), false);
            } else {
                IEnumAttribute currentEnumAttribute = referencedEnumType.getEnumAttributesIncludeSupertypeCopies(false)
                        .get(i);
                addTableColumn(enumAttributeReferences.get(i).getName(), EnumUtil.findEnumAttributeIsUnique(
                        currentEnumAttribute, ipsProject));
            }
        }
    }

    /** Adds a new column with the given name to the end of the table. */
    private void addTableColumn(String columnName, boolean identifierColumnn) {
        TableColumn newColumn = new TableColumn(enumValuesTable, SWT.LEFT);
        newColumn.setText(columnName);
        newColumn.setWidth(200);

        if (identifierColumnn) {
            newColumn.setImage(IpsUIPlugin.getImageHandling().getSharedImage("TableKeyColumn.gif", true)); //$NON-NLS-1$
        }
        columnNames.add(columnName);
    }

    /** Increases the height of the table rows slightly. */
    private void increaseHeightOfTableRows() {
        Listener paintListener = new Listener() {
            @Override
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

    /** Creates the tool bar / context menu actions. */
    private void createActions() {
        newEnumValueAction = new NewEnumValueAction(enumValuesTableViewer);
        deleteEnumValueAction = new DeleteEnumValueAction(enumValuesTableViewer);
        moveEnumValueUpAction = new MoveEnumValueAction(enumValuesTableViewer, true);
        moveEnumValueDownAction = new MoveEnumValueAction(enumValuesTableViewer, false);

        if (enumTypeEditing) {
            lockAndSyncLiteralNameAction = new LockAndSyncLiteralNameAction(this);
            lockAndSyncLiteralNameAction.setChecked(lockAndSynchronizeLiteralNames);
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

    /**
     * Registers this section as <tt>ChangeListener</tt> to the <tt>IEnumValueContainer</tt> that is
     * being edited.
     */
    private void registerAsChangeListenerToEnumValueContainer() {
        enumValueContainer.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                enumValueContainer.getIpsModel().removeChangeListener(EnumValuesSection.this);
            }
        });
    }

    /**
     * Toggles the 'Lock and Synchronize Literal Names' option. Updates the columns that are skipped
     * by cell editors in the process.
     */
    void toggleLockAndSyncLiteralNames() {
        lockAndSynchronizeLiteralNames = !lockAndSynchronizeLiteralNames;
        lockAndSyncLiteralNameAction.setChecked(lockAndSynchronizeLiteralNames);

        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings();
        settings.put(SETTINGS_KEY_LOCK_AND_SYNC, lockAndSynchronizeLiteralNames);

        updateCellEditorSkippedColumns();
    }

    /**
     * Updates the skipped columns lists of the cell editors. If the 'Lock and Synchronize Literal
     * Names' option is active the column corresponding to the first
     * <tt>IEnumLiteralNameAttribute</tt> will be skipped by all cell editors.
     * <p>
     * If the option is not active this operation assures that no columns at all are skipped by the
     * cell editors.
     * <p>
     * <strong>Important:</strong> May only be called if an <tt>IEnumType</tt> is being edited and
     * the cell editors are currently in sync with the <tt>IEnumType</tt>.
     */
    private void updateCellEditorSkippedColumns() {
        if (!lockAndSynchronizeLiteralNames) {
            /*
             * Making sure that there are no columns skipped if the 'Lock and Sync Literal Names'
             * option is not active.
             */
            for (CellEditor cellEditor : cellEditors) {
                clearSkippedColumnIndex((IpsCellEditor)cellEditor);
            }
            return;
        }

        // Do nothing if there isn't a literal name attribute.
        if (!(enumType.hasEnumLiteralNameAttribute())) {
            return;
        }

        // Skip the literal name column in all cell editors.
        int skippedColumnIndex = enumType.getIndexOfEnumLiteralNameAttribute();
        for (CellEditor cellEditor : cellEditors) {
            addSkippedColumnIndex((IpsCellEditor)cellEditor, skippedColumnIndex);
        }

        getCellEditorForLiteralNameColumn().getControl().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (lockAndSynchronizeLiteralNames) {
                    getCellEditorForLiteralNameColumn().deactivate();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Nothing to do on focus lost event.
            }
        });
    }

    private void addSkippedColumnIndex(IpsCellEditor cellEditor, int skippedColumnIndex) {
        TableViewerTraversalStrategy tableTraverseStrat = (TableViewerTraversalStrategy)cellEditor
                .getTraversalStrategy();
        if (tableTraverseStrat != null) {
            tableTraverseStrat.addSkippedColumnIndex(skippedColumnIndex);
        }
    }

    private void clearSkippedColumnIndex(IpsCellEditor cellEditor) {
        TableViewerTraversalStrategy tableTraverseStrat = (TableViewerTraversalStrategy)cellEditor
                .getTraversalStrategy();
        if (tableTraverseStrat != null) {
            tableTraverseStrat.clearSkippedColumns();
        }
    }

    /**
     * Returns the cell editor for the first <tt>IEnumLiteralNameAttribute</tt> column or
     * <tt>null</tt> if there is none.
     * <p>
     * <strong>Important:</strong> May only be called if an <tt>IEnumType</tt> is being edited and
     * the cell editors are currently in sync with the <tt>IEnumType</tt>.
     */
    private CellEditor getCellEditorForLiteralNameColumn() {
        if (enumType.getIndexOfEnumLiteralNameAttribute() == -1) {
            return null;
        }
        return cellEditors[enumType.getIndexOfEnumLiteralNameAttribute()];
    }

    /**
     * Reinitializes the contents of this section:
     * <ul>
     * <li>The <tt>columnNames</tt> will be emptied and created anew.
     * <li>Every table column of the table will be disposed and created anew.
     * <li>The table viewer will be refreshed.
     */
    public void reinit() throws CoreException {
        columnNames.clear();
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            currentColumn.dispose();
        }
        createTableColumns();
        updateTableViewer();
    }

    /** Updates the enabled states of the table and the tool bar actions. */
    private void updateEnabledStates() throws CoreException {
        boolean enabled = enumValueContainer.isCapableOfContainingValues();
        newEnumValueAction.setEnabled(enabled);
        deleteEnumValueAction.setEnabled(enabled);
        if (enumTypeEditing) {
            newEnumValueAction.setEnabled(enabled);
            deleteEnumValueAction.setEnabled(enabled);
            moveEnumValueUpAction.setEnabled(enabled);
            moveEnumValueDownAction.setEnabled(enabled);
            lockAndSyncLiteralNameAction.setEnabled(enabled);
            enumValuesTable.setEnabled(enabled);
            getSectionControl().setEnabled(enabled);
        }
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

    /** Creates the table viewer for the table. */
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

    /**
     * Updates the cell editors for the <tt>enumValuesTableViewer</tt> by creating them anew and
     * overwrites the column properties with actual data. Also refreshes the viewer with actual
     * model data.
     */
    private void updateTableViewer() throws CoreException {
        enumValuesTableViewer.setColumnProperties(columnNames.toArray(new String[columnNames.size()]));
        createCellEditors();
        enumValuesTableViewer.setCellEditors(cellEditors);
        if (enumTypeEditing) {
            updateCellEditorSkippedColumns();
        }
        enumValuesTableViewer.refresh();
    }

    /** Updates the cell editors by recreating them. */
    private void createCellEditors() throws CoreException {
        if (enumTypeEditing) {
            createCellEditorsForEnumType();
        } else {
            createCellEditorsForEnumContent();
        }
    }

    /** Creates the cell editors for editing an <tt>IEnumType</tt>. */
    private void createCellEditorsForEnumType() throws CoreException {
        CellEditor[] cellEditors = new CellEditor[columnNames.size()];

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(true);

        for (int i = 0; i < cellEditors.length; i++) {
            ValueDatatype datatype = enumAttributes.get(i).findDatatype(enumValueContainer.getIpsProject());
            cellEditors[i] = createCellEditor(datatype, i);
            if (enumAttributes.get(i).isLiteralNameDefaultValueProvider()) {
                addFocusListenerToDefaultProviderCellEditor(cellEditors[i]);
            }
        }

        this.cellEditors = cellEditors;
    }

    /** Creates the cell editors for editing an <tt>IEnumContent</tt>. */
    private void createCellEditorsForEnumContent() throws CoreException {
        CellEditor[] cellEditors = new CellEditor[columnNames.size()];

        List<IEnumAttribute> enumAttributes = null;
        if (!(enumContent.isFixToModelRequired())) {
            enumAttributes = enumContent.findEnumType(enumValueContainer.getIpsProject())
                    .getEnumAttributesIncludeSupertypeCopies(false);
        }

        for (int i = 0; i < cellEditors.length; i++) {
            ValueDatatype datatype = (enumAttributes == null) ? Datatype.STRING : enumAttributes.get(i).findDatatype(
                    enumValueContainer.getIpsProject());
            cellEditors[i] = createCellEditor(datatype, i);
        }

        this.cellEditors = cellEditors;
    }

    /**
     * Creates a cell editor fitting for the given <tt>ValueDatatype</tt> that will be used for the
     * given column index.
     */
    private CellEditor createCellEditor(ValueDatatype datatype, int columnIndex) {
        ValueDatatypeControlFactory valueDatatypeControlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(datatype);
        // creates a row-creating CellEditor
        IpsCellEditor cellEditor = valueDatatypeControlFactory.createTableCellEditor(getToolkit(), datatype, null,
                enumValuesTableViewer, columnIndex, enumValueContainer.getIpsProject());
        return cellEditor;
    }

    /**
     * Attaches a <tt>FocusListener</tt> to the control of the given cell editor responsible for
     * filling the field with the default value for the literal name obtained from the default value
     * provider attribute.
     */
    private void addFocusListenerToDefaultProviderCellEditor(final CellEditor defaultProviderCellEditor) {
        defaultProviderCellEditor.addListener(new ICellEditorListener() {

            @Override
            public void applyEditorValue() {
                /*
                 * Return if the default provider control is not a text control and therefore not
                 * valid.
                 */
                if (!(defaultProviderCellEditor.getControl() instanceof Text)) {
                    return;
                }

                /*
                 * Return if the selection is invalid because we can't obtain the EnumValue in that
                 * case.
                 */
                if (enumValueContainer.getEnumValuesCount() - 1 < enumValuesTable.getSelectionIndex()
                        || enumValuesTable.getSelectionIndex() == -1) {
                    return;
                }

                // Return if the EnumType does not have an EnumLiteralNameAttribute.
                if (!(enumType.hasEnumLiteralNameAttribute())) {
                    return;
                }

                IEnumValue enumValue = enumValueContainer.getEnumValues().get(enumValuesTable.getSelectionIndex());
                String defaultValue = ((Text)defaultProviderCellEditor.getControl()).getText();
                if (defaultValue == null ? false : defaultValue.length() > 0) {
                    if (isOverwriteExistingLiteralPossible(enumValue)) {
                        if (defaultValue.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
                            defaultValue = null;
                        }
                        enumValue.getEnumLiteralNameAttributeValue().setValue(defaultValue);
                    }
                }
            }

            /**
             * Returns whether the given, existing literal name may be overwritten.
             * <p>
             * Returns <tt>true</tt> if the existing literal is <tt>null</tt>, empty or equals the
             * configured null-representation. Also returns <tt>true</tt> if the option 'Lock and
             * Synchronize Literal Names' is active.
             */
            private boolean isOverwriteExistingLiteralPossible(IEnumValue enumValue) {
                if (lockAndSynchronizeLiteralNames) {
                    return true;
                }
                String existingLiteral = enumValue.getEnumLiteralNameAttributeValue().getValue();
                return (existingLiteral == null) ? true : existingLiteral.length() == 0
                        || existingLiteral.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
            }

            @Override
            public void cancelEditor() {
                // Nothing to do on this event.
            }

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                // Nothing to do on this event.
            }

        });
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

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    /** Renames the column identified by the given column name to the given new column name. */
    public void renameTableColumn(String columnName, String newColumnName) {
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
     * Updates the <tt>originalOrderedAttributeValuesMap</tt> and refreshes the <tt>
     * enumValuesTableViewer</tt>
     * when <tt>IEnumValue</tt>s have been added, moved or removed.
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        // TODO AW: REFACTOR - this method is pretty awkward.
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
                            reinit();
                            updateEnabledStates();
                        }
                    } else {
                        if (changedIpsObject instanceof IEnumContent) {
                            reinit();
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

                        try {
                            IEnumAttribute modifiedEnumAttribute = (IEnumAttribute)part;
                            String oldName = null;
                            for (String currentColumnName : columnNames) {
                                IEnumType enumTypeModifiedEnumAttribute = modifiedEnumAttribute.getEnumType();
                                if (enumTypeModifiedEnumAttribute
                                        .getEnumAttributeIncludeSupertypeCopies(currentColumnName) == null) {
                                    oldName = currentColumnName;
                                    break;
                                }
                            }

                            // Something else but the name has changed.
                            if (oldName == null) {
                                if (enumTypeEditing) {
                                    reinit();
                                } else {
                                    updateTableViewer();
                                }
                                return;
                            }

                            renameTableColumn(oldName, modifiedEnumAttribute.getName());
                            updateTableViewer();
                        } catch (CoreException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;
        }
    }

    /** The content provider for the table viewer. */
    private class EnumValuesContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            return enumValueContainer.getEnumValues().toArray();
        }

        @Override
        public void dispose() {
            // Nothing to dispose.
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do on input change event.
        }

    }

    /** The label provider for the table viewer. */
    private class EnumValuesLabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (hasErrorsAt((IEnumValue)element, columnIndex)) {
                return IpsUIPlugin.getImageHandling().getImage(OverlayIcons.ERROR_OVR_DESC);
            }
            return null;
        }

        /**
         * Returns <tt>true</tt> if the validation of the given <tt>IEnumValue</tt> detects an error
         * at the given column index, <tt>false</tt> otherwise.
         */
        private boolean hasErrorsAt(IEnumValue enumValue, int columnIndex) {
            // Don't validate if the indicated column does not exist.
            if (enumValue.getEnumAttributeValues().size() <= columnIndex) {
                return false;
            }
            try {
                MessageList messageList = enumValue.validate(enumValue.getIpsProject());
                return !(messageList.getMessagesFor(enumValue.getEnumAttributeValues().get(columnIndex), null)
                        .isEmpty());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            // There needs to be at least one column to be able to obtain label information.
            if (columnNames.size() == 0) {
                return null;
            }

            // For each requested column there must already be an EnumAttributeValue.
            List<IEnumAttributeValue> enumAttributeValues = ((IEnumValue)element).getEnumAttributeValues();
            if (enumAttributeValues.size() - 1 < columnIndex) {
                return null;
            }

            /*
             * Return text formatted by the IPS DatatypeFormatter if the referenced EnumAttribute
             * can be found and so the data type of the value is known. Return the value directly if
             * the EnumAttribute cannot be found.
             */
            String columnValue = enumAttributeValues.get(columnIndex).getValue();
            try {
                IEnumAttribute enumAttribute = enumAttributeValues.get(columnIndex).findEnumAttribute(ipsProject);
                if (enumAttribute == null) {
                    return columnValue;
                }

                String datatype = enumAttributeValues.get(columnIndex).findEnumAttribute(ipsProject).getDatatype();
                ValueDatatype valueDatatype = enumAttributeValues.get(columnIndex).getIpsProject().findValueDatatype(
                        datatype);
                return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(valueDatatype,
                        columnValue);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // Nothing to do.
        }

        @Override
        public void dispose() {
            // Nothing to do.
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // Nothing to do.
        }

    }

    /** The cell modifier for the table viewer. */
    private class EnumCellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        @Override
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

        @Override
        public void modify(Object element, String property, Object value) {
            IEnumValue enumValue;
            if (element instanceof IEnumValue) {
                enumValue = (IEnumValue)element;
            } else if (element instanceof Item) {
                enumValue = (IEnumValue)((Item)element).getData();
            } else {
                return;
            }

            int columnIndex = getColumnIndexByName(property);
            if (columnIndex != -1) {
                IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(columnIndex);

                /*
                 * Do not modify if it is the literal name column and the lock and sync option is
                 * active!
                 */
                if (isLiteralNameColumn(columnIndex)) {
                    if (lockAndSynchronizeLiteralNames) {
                        return;
                    }
                    IEnumLiteralNameAttributeValue literalNameValue = (IEnumLiteralNameAttributeValue)enumAttributeValue;

                    /*
                     * If it is the literal name column we actually have to apply the corresponding
                     * Faktor-IPS refactoring now since generated Java code may depend on the value!
                     */
                    ProcessorBasedRefactoring renameRefactoring = literalNameValue.getRenameRefactoring();
                    IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)renameRefactoring.getProcessor();
                    renameProcessor.setNewName((String)value);

                    IpsRefactoringOperation refactorOp = new IpsRefactoringOperation(renameRefactoring, getShell());
                    refactorOp.execute();

                } else {
                    enumAttributeValue.setValue((String)value);
                }
                enumValuesTableViewer.refresh(true);
            }
        }

        private boolean isLiteralNameColumn(int columnIndex) {
            return enumType.getIndexOfEnumLiteralNameAttribute() == columnIndex;
        }

    }

    /**
     * Listener that reacts to <tt>SelectionChangedEvent</tt>s by deleting all empty rows at the
     * bottom of the table.
     */
    private class RowDeletor implements ISelectionChangedListener {

        @Override
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
            if (enumValuesTable.getItemCount() <= 1) {
                return;
            }
            for (int i = enumValuesTable.getItemCount() - 1; i > enumValuesTable.getSelectionIndex(); i--) {
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
            for (IEnumAttributeValue attrValue : enumValue.getEnumAttributeValues()) {
                if (attrValue.getValue() != null) {
                    /*
                     * TODO pk 10-07-2009: this is not really correct. We actually need an
                     * empty-string-representation-value
                     */
                    if (!(attrValue.getValue().trim().length() == 0)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}
