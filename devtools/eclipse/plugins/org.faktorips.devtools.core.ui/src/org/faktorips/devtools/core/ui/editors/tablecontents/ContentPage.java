/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.actions.TableImportExportAction;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.SearchBar;
import org.faktorips.devtools.core.ui.editors.SelectionStatusBarPublisher;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableUtil;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablecontents.TableRows;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * The content-page for the <code>TableContentsEditor</code>. Allows the editing of
 * <code>TableContents</code> using a <code>TableViewer</code>.
 * 
 * @author Stefan Widmaier
 */
public class ContentPage extends IpsObjectEditorPage implements ContentsChangeListener {

    private static final String PAGE_ID = "Contents"; //$NON-NLS-1$
    private static final String TABLE_SETTINGS_PREFIX = "TableColumnWidths_"; //$NON-NLS-1$
    private static final String COLUMN_PREFIX = "col_"; //$NON-NLS-1$
    private static final int DEFAULT_COLUMN_WIDTH = 125;

    private Table table;
    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private SearchBar searchBar;

    private SelectionStatusBarPublisher selectionStatusBarPublisher;
    private IAction openFixTableContentDialogAction;
    private NewRowAction newRowAction;
    private DeleteRowAction deleteRowAction;
    private TableImportExportActionInEditor importAction;
    private TableImportExportActionInEditor exportAction;

    /**
     * The <code>ITableContents</code> the <code>TableContentsEditor</code> this page belongs to is
     * currently editing.
     */
    private final ITableContents tableContents;

    private final BindingContext bindingContext = new BindingContext();

    /** The extension property control factory that may extend the controls. */
    private final ExtensionPropertyControlFactory extFactory;

    public ContentPage(TableContentsEditor editor) {
        super(editor, PAGE_ID, Messages.ContentPage_title);
        tableContents = editor.getTableContents();
        tableContents.getIpsModel().addChangeListener(this);
        extFactory = new ExtensionPropertyControlFactory(tableContents);
        selectionStatusBarPublisher = new SelectionStatusBarPublisher(getEditor().getEditorSite());

    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
    }

    @Override
    public void refresh() {
        super.refresh();
        bindingContext.updateUI();
        updateToolbarActionsEnabledStates();
    }

    /**
     * Updates or creates a new table by disposing the old table columns
     */
    private void updateTable() {
        if (!tableViewer.getTable().isDisposed()) {
            if (tableViewer.getTable().getColumnCount() > 0) {
                TableColumn[] columns = tableViewer.getTable().getColumns();

                for (TableColumn column : columns) {
                    column.dispose();
                }
            }
        }

        createNewTableViewer();
        updateToolbarActionsEnabledStates();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        this.toolkit = toolkit;
        checkDifferences(formBody, toolkit);

        GridLayout layout = new GridLayout(1, false);
        formBody.setLayout(layout);

        if (extFactory.needsToCreateControlsFor(IExtensionPropertyDefinition.POSITION_BOTTOM)) {
            createExtensionProperty(formBody, toolkit);
        }

        searchBar = new SearchBar(formBody, toolkit);

        createTable(formBody, toolkit);

        initTableViewer(toolkit);

        TableRows tableRows = (TableRows)getTableContents().getTableRows();
        tableViewer.setItemCount(tableRows.getNumOfRows());

        createToolbarActions();

        createToolbar(tableRows);

        initTablePopupMenu(table, deleteRowAction, newRowAction);

        searchBar.setFilterTo(tableViewer);

    }

    /**
     * Creates a Table with the given formBody as a parent. Inits the look, layout of the table and
     * adds a KeyListener that enables the editing of the first cell in the currently selected row
     * by pressing "F2".
     */
    private void createTable(Composite formBody, UIToolkit toolkit) {
        // Table: scroll both vertically and horizontally
        table = toolkit.createAndConfigureTable(formBody,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);

        // FS#822 workaround to activate the correct cell editor (row and column),
        // after scrolling and activating another cell the table on a different page.
        // To fix this problem selection listeners will be added to deactivate the current cell
        // editor first if the user scrolls to another cell in the table
        SelectionListener cellEditorDeactivator = new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                deactivateCellEditors();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                deactivateCellEditors();
            }
        };

        table.getVerticalBar().addSelectionListener(cellEditorDeactivator);
        table.getHorizontalBar().addSelectionListener(cellEditorDeactivator);

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.F2) {
                    IRow selectedRow = (IRow)((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
                    if (selectedRow != null) {
                        tableViewer.editElement(selectedRow, 0);
                    }
                }
            }
        });

        table.removeAll();
        TableUtil.increaseHeightOfTableRows(table, getTableContents().getColumnReferencesCount(), 5);
    }

    /**
     * Inits the <code>TableViewer</code> for this page. Sets content- and labelprovider, column
     * headers and widths, column properties, cell editors, sorter. Inits popupmenu and
     * hoverservice.
     */
    private void initTableViewer(UIToolkit toolkit) {
        tableViewer = toolkit.createAndConfigureTableViewer(table, getTableContents(),
                new TableContentsContentProvider());
        /*
         * SetUseHashlookup in combination with SWT.VIRTUAL does't work.
         * 
         * 
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=269721
         * 
         * tableViewer.setUseHashlookup(true);
         */
        TableContentsLabelProvider labelProvider = new TableContentsLabelProvider();
        tableViewer.setLabelProvider(labelProvider);

        createNewTableViewer();

        tableViewer.addSelectionChangedListener(
                event -> selectionStatusBarPublisher.updateMarkedRows(rowsFromSelection(event.getSelection())));
    }

    /**
     * creates a new <code>TableViewer</code> based on the corresponding <code>ITableContents</code>
     */
    private void createNewTableViewer() {
        ITableStructure tableStructure = getTableStructure();
        int columnReferencesCount = getTableContents().getColumnReferencesCount();
        String[] columnProperties = new String[columnReferencesCount];
        List<Integer> columnSizes = readColumnWidths();
        int numReadSizes = columnSizes.size();
        // use the number of references in the contents as only those can be edited.
        CellEditor[] editors = new CellEditor[columnReferencesCount];
        ValueDatatype[] datatypes = new ValueDatatype[columnReferencesCount];
        for (int i = 0; i < columnReferencesCount; i++) {
            String columnName;
            ValueDatatype dataType = null;
            if (tableStructure == null) {
                columnName = Messages.ContentPage_Column + (i + 1);
            } else {
                String referenceName = getTableContents().getColumnReferences().get(i).getName();
                IColumn column = tableStructure.getColumn(referenceName);
                columnName = findColumnName(column, referenceName);
                dataType = findValueDatatype(column);
            }
            ValueDatatypeControlFactory factory = getValueDatatypeControlFactory(dataType);
            createTableColumn(columnSizes, numReadSizes, i, columnName, factory);
            columnProperties[i] = columnName;
            editors[i] = createCellEditor(i, dataType, factory);
            datatypes[i] = dataType;
        }
        tableViewer.setCellModifier(new TableContentsCellModifier(tableViewer, this));
        tableViewer.setColumnProperties(columnProperties);
        if (tableStructure != null) {
            tableViewer.setCellEditors(editors);
        }
        ((TableContentsLabelProvider)tableViewer.getLabelProvider()).setValueDatatypes(datatypes);
        tableViewer.setComparator(new TableSorter());
        tableViewer.refresh();
    }

    /**
     * Initializes the toolbar actions.
     */
    private void createToolbarActions() {
        openFixTableContentDialogAction = new OpenFixTableContentWizardAction(this, tableContents,
                getSite().getShell());

        newRowAction = new NewRowAction(tableViewer, this);
        deleteRowAction = new DeleteRowAction(tableViewer, this);

        importAction = new TableImportExportActionInEditor(getSite().getShell(),
                getTableContents(), true);
        importAction.setControlWithDataChangeableSupport(this);
        exportAction = new TableImportExportActionInEditor(getSite().getShell(),
                getTableContents(), false);
    }

    /**
     * Initializes the toolbar.
     * 
     * @param tableRows {@link TableRows} required for providing the unique key validation button
     */
    private void createToolbar(TableRows tableRows) {
        IToolBarManager formToolbarManager = getManagedForm().getForm().getToolBarManager();

        addUniqueKeyValidationButton(tableRows, formToolbarManager);
        formToolbarManager.add(openFixTableContentDialogAction);
        formToolbarManager.add(new Separator());

        formToolbarManager.add(newRowAction);
        formToolbarManager.add(deleteRowAction);
        formToolbarManager.add(new Separator());

        formToolbarManager.add(importAction);
        formToolbarManager.add(exportAction);
        if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
            formToolbarManager.add(new Separator());
            formToolbarManager.add(new NavigateToTableStructureAction(getTableContents()));
        }

        formToolbarManager.update(true);
        updateToolbarActionsEnabledStates();
    }

    /**
     * Adds a unique key validation button to the toolbar, if required.
     * 
     * @param tableRows The {@link TableRows} used for checking if the button is required
     * @param formToolbarManager The currently used {@link IToolBarManager}
     */
    private void addUniqueKeyValidationButton(TableRows tableRows, IToolBarManager formToolbarManager) {
        if (tableRows.isUniqueKeyValidationEnabled() && !tableRows.isUniqueKeyValidatedAutomatically()) {
            UniqueKeyValidatonAction uniqueKeyValidationAction = new UniqueKeyValidatonAction(tableViewer);
            ActionContributionItem uniqueKeyValidationActionContributionItem = new ActionContributionItem(
                    uniqueKeyValidationAction);
            formToolbarManager.add(uniqueKeyValidationActionContributionItem);
            formToolbarManager.add(new Separator());
        }
    }

    /**
     * Updates the visibility of the toolbar items based on the current changeability state.
     * <p>
     * The export functionality should be always available.
     * 
     * @param changeable Whether the page content is changeable
     */
    private void updateToolbarVisibilityState(boolean changeable) {
        IToolBarManager toolBarManager = getManagedForm().getForm().getToolBarManager();
        Arrays.stream(toolBarManager.getItems())
                .filter(item -> item.getId() == null || !item.getId().equals(exportAction.getId()))
                .forEach(item -> item.setVisible(changeable));
        toolBarManager.update(true);
    }

    private List<Integer> rowsFromSelection(ISelection selection) {
        List<Integer> rowNumbers = new ArrayList<>();
        if (!selection.isEmpty()) {
            Collection<IRow> rows = TypedSelection.createAnyCount(IRow.class, selection).getElements();

            for (IRow row : rows) {
                rowNumbers.add(row.getRowNumber());
            }
        }
        return rowNumbers;
    }

    private void createExtensionProperty(Composite formBody, UIToolkit toolkit) {
        Composite composite = toolkit.createLabelEditColumnComposite(formBody);
        extFactory.createControls(composite, toolkit, tableContents);
        extFactory.bind(bindingContext);
    }

    /**
     * Deactivates all active cell editor (i.e. the current active cell editor)
     */
    private void deactivateCellEditors() {
        CellEditor[] cellEditors = tableViewer.getCellEditors();
        for (CellEditor cellEditor : cellEditors) {
            cellEditor.deactivate();
        }
    }

    /**
     * Reads the widths of the table columns stored for this table content. If no widths have been
     * stored, the default width for each column is returned.
     * 
     * @return the stored widths for each table column.
     */
    private List<Integer> readColumnWidths() {
        String tableSettingsKey = TABLE_SETTINGS_PREFIX + getTableContents().getQualifiedName();
        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection(tableSettingsKey);
        List<Integer> sizes = new ArrayList<>();
        for (int i = 0; i < getTableContents().getColumnReferencesCount(); i++) {
            String val = settings == null ? null : settings.get(COLUMN_PREFIX + i);
            try {
                sizes.add(val == null ? DEFAULT_COLUMN_WIDTH : Integer.parseInt(val));
            } catch (NumberFormatException e) {
                sizes.add(DEFAULT_COLUMN_WIDTH);
            }
        }
        return sizes;
    }

    /**
     * Stores the width of a column in the dialog settings of the user's workspace. Thus, whenever
     * the user reopens the editor, the width of the column can be set to the same width as before.
     * 
     * @param index the index of the column.
     * @param column the column of the table.
     */
    private void storeColumnWidth(final int index, TableColumn column) {
        String tableSettingsKey = TABLE_SETTINGS_PREFIX + getTableContents().getQualifiedName();
        IDialogSettings settings = IpsPlugin.getDefault().getDialogSettings().getSection(tableSettingsKey);
        if (settings == null) {
            settings = IpsPlugin.getDefault().getDialogSettings().addNewSection(tableSettingsKey);
        }
        settings.put(COLUMN_PREFIX + index, column.getWidth());
    }

    private IpsCellEditor createCellEditor(int i, ValueDatatype dataType, ValueDatatypeControlFactory factory) {
        IpsCellEditor cellEditor = factory.createTableCellEditor(toolkit, dataType, null, tableViewer, i,
                getTableContents().getIpsProject());
        TableViewerTraversalStrategy tableTraverseStrat = (TableViewerTraversalStrategy)cellEditor
                .getTraversalStrategy();
        tableTraverseStrat.setRowCreating(true);
        return cellEditor;
    }

    private void createTableColumn(List<Integer> columnSizes,
            int numReadSizes,
            int i,
            String columnName,
            ValueDatatypeControlFactory factory) {
        final TableColumn column = new TableColumn(table, factory.getDefaultAlignment(), i);
        column.setWidth(i < numReadSizes ? columnSizes.get(i) : DEFAULT_COLUMN_WIDTH);
        final int columnIndex = i;
        column.addListener(SWT.Resize, $ -> storeColumnWidth(columnIndex, column));
        column.setText(columnName);
    }

    private ValueDatatypeControlFactory getValueDatatypeControlFactory(ValueDatatype dataType) {
        return IpsUIPlugin.getDefault().getValueDatatypeControlFactory(dataType);
    }

    private ValueDatatype findValueDatatype(IColumn column) {
        // referenced column does not exist under known reference name anymore
        // the ValueDatatype will be String as a workaround to still show the content of the cells
        if (column == null) {
            return ValueDatatype.STRING;
        } else {
            return column.findValueDatatype(getTableContents().getIpsProject());
        }
    }

    private String findColumnName(IColumn column, String referenceName) {
        // referenced column does not exist under known reference name anymore
        // the referenced column name will be displayed in place of the column's name
        if (column == null) {
            return referenceName;
        } else {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(column);
        }
    }

    /**
     * Adds the given deleteRowAction to the popup menu of the given table.
     */
    private void initTablePopupMenu(Table table, DeleteRowAction deleteRowAction, NewRowAction newRowAction) {
        // popupmenu
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(false);
        menuMgr.add(newRowAction);
        menuMgr.add(deleteRowAction);
        Menu menu = menuMgr.createContextMenu(table);
        table.setMenu(menu);
    }

    private void checkDifferences(Composite formBody, UIToolkit toolkit) {
        if (!IpsUIPlugin.isEditable(getIpsObject().getIpsSrcFile())) {
            // no set table structure dialog and fix differences supported
            // because table contents is read only
            return;
        }
        ITableStructure structure = getTableStructure();
        if (structure == null) {
            String msg = NLS.bind(Messages.ContentPage_msgMissingStructure, getTableContents().getTableStructure());
            SetStructureDialog dialog = new SetStructureDialog(getTableContents(), getSite().getShell(), msg);
            int button = dialog.open();
            if (button != Window.OK) {
                msg = NLS.bind(Messages.ContentPage_msgNoStructureFound, getTableContents().getTableStructure());
                toolkit.createLabel(formBody, msg);
            }
        }
    }

    private StringTokenizer getTokenizer(String tokens) {
        return new StringTokenizer(tokens, ",", false); //$NON-NLS-1$
    }

    private TableContentsEditor getTableEditor() {
        return (TableContentsEditor)getEditor();
    }

    private ITableContents getTableContents() {
        return getTableEditor().getTableContents();
    }

    private ITableStructure getTableStructure() {
        return getTableContents().findTableStructure(getTableContents().getIpsProject());
    }

    private ITableRows getActiveGeneration() {
        return getTableEditor().getTableContents().getTableRows();
    }

    /**
     * Redraws the table.
     */
    void redrawTable() {
        tableViewer.getTable().redraw();
    }

    private boolean wasUniqueKeyErrorStateChanged() {
        return ((TableRows)getActiveGeneration()).wasUniqueKeyErrorStateChange();
    }

    public void refreshTable(final IRow row) {
        tableViewer.refresh(row);
        if (wasUniqueKeyErrorStateChanged()) {
            // either the index error is solved or there is a new index error
            // refresh the rest of the table because an index error concerns more than one
            // row
            refreshTable();
        }
    }

    public void refreshTable() {
        tableViewer.refresh();
    }

    IRow getRow(int rowIndex) {
        return ((TableRows)getActiveGeneration()).getRow(rowIndex);
    }

    @Override
    protected void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        searchBar.setEnabled(true);
        Composite header = getManagedForm().getForm().getForm().getHead();
        toolkit.setDataChangeable(header, true);
        updateToolbarVisibilityState(changeable);
    }

    /**
     * Updates the enabled states of the tool bar.
     * <p>
     * The <code>OpenFixEnumContentWizardAction</code> will be enabled if the
     * <code>ITableStructure</code> the <code>ITableContents</code> to edit is built upon is not
     * correct
     */
    void updateToolbarActionsEnabledStates() {
        boolean isFixToModelRequired = tableContents.isFixToModelRequired();
        openFixTableContentDialogAction.setEnabled(isFixToModelRequired);
        newRowAction.setEnabled(!isFixToModelRequired);
        deleteRowAction.setEnabled(!isFixToModelRequired);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        for (PropertyChangeEvent propertyChangeEvent : event.getPropertyChangeEvents()) {
            if (TableContents.COLUMNREFERENCENAME.equals(propertyChangeEvent.getPropertyName())) {
                updateTable();
            }
        }
    }

    private class TableImportExportActionInEditor extends TableImportExportAction {

        /**
         * ID used for identifying the import action within the toolbar.
         */
        private static final String IMPORT_ACTION_ID = "table_content_import_action"; //$NON-NLS-1$
        /**
         * ID used for identifying the export action within the toolbar.
         */
        private static final String EXPORT_ACTION_ID = "table_content_export_action"; //$NON-NLS-1$

        protected TableImportExportActionInEditor(Shell shell, ITableContents tableContents, boolean isImport) {
            super(shell, tableContents);
            if (isImport) {
                initImportAction();
                setId(IMPORT_ACTION_ID);
            } else {
                initExportAction();
                setId(EXPORT_ACTION_ID);
            }
        }

        @Override
        public void run(IStructuredSelection selection) {
            if (super.runInternal(selection)) {
                tableViewer.setInput(getTableContents());
                tableViewer.refresh(true);
                tableViewer.getTable().redraw();
            }
        }
    }

    @SuppressWarnings("unused")
    private class Validator implements IInputValidator {

        private int indexCount = 0;

        public Validator(int requiredIndexCount) {
            indexCount = requiredIndexCount;
        }

        @Override
        public String isValid(String newText) {
            StringTokenizer tokenizer = getTokenizer(newText);
            int tokenizerItemCount = tokenizer.countTokens();

            ArrayList<Integer> values = new ArrayList<>(tokenizerItemCount);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                try {
                    Integer value = Integer.valueOf(token);
                    if (values.contains(value) && indexCount < 0) {
                        return Messages.ContentPage_errorNoDuplicateIndices;
                    }
                    if (indexCount < 0 && (value.intValue() >= getTableContents().getColumnReferencesCount()
                            || value.intValue() < 0)) {
                        return NLS.bind(Messages.ContentPage_errorIndexOutOfRange, value);
                    }
                    values.add(value);
                } catch (NumberFormatException e) {
                    if (indexCount == 1) {
                        return NLS.bind(Messages.ContentPage_errorInvalidValueOne, token);
                    } else {
                        return NLS.bind(Messages.ContentPage_errorInvalidValueMany, token);
                    }
                }
            }

            return differenceIndexItem(Math.abs(indexCount) - tokenizerItemCount);
        }

        private String differenceIndexItem(int difference) {
            if (difference < 0) {
                if (indexCount == 1 || indexCount == -1) {
                    return Messages.ContentPage_errorTooManyOne;
                } else {
                    return NLS.bind(Messages.ContentPage_errorTooManyMany, String.valueOf(Math.abs(indexCount)));
                }
            } else if (difference == 1) {
                return Messages.ContentPage_errorOneMore;
            } else if (difference > 1) {
                return NLS.bind(Messages.ContentPage_errorManyMore, String.valueOf(difference));
            }
            return null;
        }
    }

}
