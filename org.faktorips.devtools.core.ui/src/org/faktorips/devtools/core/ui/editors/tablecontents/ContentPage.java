/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContentsGeneration;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.actions.TableImportExportAction;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableUtil;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.util.message.MessageList;

/**
 * The content-page for the <code>TableContentsEditor</code>. Allows the editing of
 * <code>TableContents</code> using a <code>TableViewer</code>.
 * 
 * @author Stefan Widmaier
 */
public class ContentPage extends IpsObjectEditorPage {

    private static final String PAGE_ID = "Contents"; //$NON-NLS-1$
    private static final String TABLE_SETTINGS_PREFIX = "TableColumnWidths_"; //$NON-NLS-1$
    private static final String COLUMN_PREFIX = "col_"; //$NON-NLS-1$
    private static final int DEFAULT_COLUMN_WIDTH = 125;

    private TableViewer tableViewer;

    /**
     * The <tt>ITableContents</tt> the <tt>TableContentsEditor</tt> this page belongs to is
     * currently editing.
     */
    private final ITableContents tableContents;

    private final BindingContext bindingContext = new BindingContext();

    /** The extension property control factory that may extend the controls. */
    private final ExtensionPropertyControlFactory extFactory;

    public ContentPage(TableContentsEditor editor) {
        super(editor, PAGE_ID, Messages.ContentPage_title);
        tableContents = editor.getTableContents();
        extFactory = new ExtensionPropertyControlFactory(tableContents);

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
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        checkDifferences(formBody, toolkit);

        GridLayout layout = new GridLayout(1, false);
        formBody.setLayout(layout);

        if (extFactory.needsToCreateControlsFor(IExtensionPropertyDefinition.POSITION_BOTTOM)) {
            createExtensionProperty(formBody, toolkit);
        }
        Table table = createTable(formBody);
        initTableViewer(table, toolkit);
        NewRowAction newRowAction = new NewRowAction(tableViewer, this);
        DeleteRowAction deleteRowAction = new DeleteRowAction(tableViewer, this);
        initTablePopupMenu(table, deleteRowAction, newRowAction);

        tableViewer.setInput(getTableContents());

        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(newRowAction);
        form.getToolBarManager().add(deleteRowAction);
        form.getToolBarManager().add(new Separator());

        // create own TableImportExportActionInEditor because the editor must be refreshed after
        // importing of the table contents otherwise the old content is visible until the editor is
        // reopened
        // Workaround see
        TableImportExportActionInEditor importAction = new TableImportExportActionInEditor(getSite().getShell(),
                getTableContents(), true);
        importAction.setControlWithDataChangeableSupport(this);
        TableImportExportActionInEditor exportAction = new TableImportExportActionInEditor(getSite().getShell(),
                getTableContents(), false);

        form.getToolBarManager().add(importAction);
        form.getToolBarManager().add(exportAction);
        if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
            form.getToolBarManager().add(new Separator());
            form.getToolBarManager().add(new NavigateToTableStructureAction(getTableContents()));
        }
        form.updateToolBar();

        // FS#822 workaround to activate the correct cell editor (row and column),
        // after scrolling and activating another cell the table on a different page.
        // To fix this problem selection listeners will be added to deactivate the current cell
        // editor first
        // if the user scrolls to another cell in the table
        table.getVerticalBar().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                deactivateCellEditors();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                deactivateCellEditors();
            }
        });
        table.getHorizontalBar().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                deactivateCellEditors();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                deactivateCellEditors();
            }
        });
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
     * Creates a Table with the given formBody as a parent and returns it. Inits the look, layout of
     * the table and adds a KeyListener that enables the editing of the first cell in the currently
     * selected row by pressing "F2".
     * 
     * @return The newly created and initialized Table.
     */
    private Table createTable(Composite formBody) {
        // Table: scroll both vertically and horizontally
        Table table = new Table(formBody, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // occupy all available space
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = formBody.getClientArea().width;
        tableGridData.heightHint = formBody.getClientArea().height;
        table.setLayoutData(tableGridData);
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

        return table;
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
        List<Integer> sizes = new ArrayList<Integer>();
        for (int i = 0; i < getTableContents().getNumOfColumns(); i++) {
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

    /**
     * Inits the <code>TableViewer</code> for this page. Sets content- and labelprovider, column
     * headers and widths, column properties, cell editors, sorter. Inits popupmenu and
     * hoverservice.
     */
    private void initTableViewer(Table table, UIToolkit toolkit) {
        try {
            table.removeAll();
            TableUtil.increaseHeightOfTableRows(table, getTableContents().getNumOfColumns(), 5);

            tableViewer = new TableViewer(table);
            tableViewer.setUseHashlookup(true);
            tableViewer.setContentProvider(new TableContentsContentProvider());
            TableContentsLabelProvider labelProvider = new TableContentsLabelProvider();
            tableViewer.setLabelProvider(labelProvider);

            ITableStructure tableStructure = getTableStructure();
            String[] columnProperties = new String[getTableContents().getNumOfColumns()];
            List<Integer> columnSizes = readColumnWidths();
            int numReadSizes = columnSizes.size();
            for (int i = 0; i < getTableContents().getNumOfColumns(); i++) {
                String columnName;
                ValueDatatype valueDatatype = null;
                if (tableStructure == null) {
                    columnName = Messages.ContentPage_Column + (i + 1);
                } else {
                    columnName = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(tableStructure.getColumn(i));
                    valueDatatype = tableStructure.getColumn(i).findValueDatatype(getTableContents().getIpsProject());
                }
                ValueDatatypeControlFactory valueDatatypeControlFactory = IpsUIPlugin.getDefault()
                        .getValueDatatypeControlFactory(valueDatatype);
                final TableColumn column = new TableColumn(table, valueDatatypeControlFactory.getDefaultAlignment(), i);
                column.setWidth(i < numReadSizes ? columnSizes.get(i) : DEFAULT_COLUMN_WIDTH);
                final int columnIndex = i;
                column.addListener(SWT.Resize, new Listener() {
                    @Override
                    public void handleEvent(Event arg0) {
                        storeColumnWidth(columnIndex, column);
                    }
                });
                column.setText(columnName);
                columnProperties[i] = columnName;
            }
            tableViewer.setCellModifier(new TableContentsCellModifier(tableViewer, this));
            tableViewer.setColumnProperties(columnProperties);

            // column properties must be set before cellEditors are created.
            ValueDatatype[] datatypes = new ValueDatatype[getTableContents().getNumOfColumns()];
            if (tableStructure != null) {
                // use the number of columns in the contents as only those can be edited.
                CellEditor[] editors = new CellEditor[getTableContents().getNumOfColumns()];
                for (int i = 0; i < getTableContents().getNumOfColumns(); i++) {
                    ValueDatatype dataType = tableStructure.getColumn(i).findValueDatatype(
                            getTableContents().getIpsProject());
                    ValueDatatypeControlFactory factory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                            dataType);
                    IpsCellEditor cellEditor = factory.createTableCellEditor(toolkit, dataType, null, tableViewer, i,
                            getTableContents().getIpsProject());
                    TableViewerTraversalStrategy tableTraverseStrat = (TableViewerTraversalStrategy)cellEditor
                            .getTraversalStrategy();
                    tableTraverseStrat.setRowCreating(true);
                    editors[i] = cellEditor;
                    datatypes[i] = dataType;
                }
                tableViewer.setCellEditors(editors);
            }
            labelProvider.setValueDatatypes(datatypes);
            tableViewer.setSorter(new TableSorter());

            new TableMessageHoverService(tableViewer) {
                @Override
                protected MessageList getMessagesFor(Object element) throws CoreException {
                    if (element != null) {
                        return ((IRow)element).validate(((IRow)element).getIpsProject());
                    }
                    return null;
                }
            };
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Adds the given deleteRowAction to the popupmenu of th given table.
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
        try {
            ITableStructure structure = getTableStructure();
            if (structure == null) {
                String msg = NLS.bind(Messages.ContentPage_msgMissingStructure, getTableContents().getTableStructure());
                SetStructureDialog dialog = new SetStructureDialog(getTableContents(), getSite().getShell(), msg);
                int button = dialog.open();
                if (button != Window.OK) {
                    msg = NLS.bind(Messages.ContentPage_msgNoStructureFound, getTableContents().getTableStructure());
                    toolkit.createLabel(formBody, msg);
                    return;
                } else {
                    structure = getTableStructure();
                }
            }
            if (structure == null) {
                return;
            }
            int difference = structure.getColumns().length - getTableContents().getNumOfColumns();

            if (difference != 0) {
                IInputValidator validator = new Validator(difference);

                String msg = null;
                String title = null;
                if (difference > 1) {
                    title = Messages.ContentPage_titleMissingColumns;
                    msg = NLS.bind(Messages.ContentPage_msgAddMany, String.valueOf(difference),
                            String.valueOf(getTableContents().getNumOfColumns()));

                } else if (difference == 1) {
                    title = Messages.ContentPage_titleMissingColumn;
                    msg = NLS
                            .bind(Messages.ContentPage_msgAddOne, String.valueOf(getTableContents().getNumOfColumns()));
                } else if (difference == -1) {
                    title = Messages.ContentPage_titleTooMany;
                    msg = NLS.bind(Messages.ContentPage_msgRemoveOne,
                            String.valueOf(getTableContents().getNumOfColumns()));
                } else if (difference < -1) {
                    title = Messages.ContentPage_titleTooMany;
                    msg = NLS.bind(Messages.ContentPage_msgRemoveMany, String.valueOf(Math.abs(difference)),
                            String.valueOf(getTableContents().getNumOfColumns()));
                }

                InputDialog dialog = new InputDialog(getSite().getShell(), title, msg, "", validator); //$NON-NLS-1$
                int state = dialog.open();
                if (state == Window.OK) {
                    if (difference > 0) {
                        insertColumnsAt(dialog.getValue());
                    } else {
                        removeColumns(dialog.getValue());
                    }
                } else {
                    toolkit.createLabel(formBody, Messages.ContentPage_msgCantShowContent);
                    return;
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void insertColumnsAt(String insertIndices) {
        int[] indices = getIndices(insertIndices);
        for (int i = 0; i < indices.length; i++) {
            ((TableContents)getTableContents()).newColumnAt(indices[i] + i, null);
        }
    }

    private void removeColumns(String removeIndices) {
        int[] indices = getIndices(removeIndices);
        for (int i = 0; i < indices.length; i++) {
            ((TableContents)getTableContents()).deleteColumn(indices[i] - i);
        }
    }

    private int[] getIndices(String indices) {
        StringTokenizer tokenizer = getTokenizer(indices);
        int[] result = new int[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            result[i] = Integer.valueOf(tokenizer.nextToken()).intValue();
        }

        Arrays.sort(result);
        return result;
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

    private ITableStructure getTableStructure() throws CoreException {
        return getTableContents().findTableStructure(getTableContents().getIpsProject());
    }

    private ITableContentsGeneration getActiveGeneration() {
        return (ITableContentsGeneration)getTableEditor().getTableContents().getFirstGeneration();
    }

    /**
     * Redraws the table.
     */
    void redrawTable() {
        tableViewer.getTable().redraw();
    }

    private boolean wasUniqueKeyErrorStateChanged() {
        return ((TableContentsGeneration)getActiveGeneration()).wasUniqueKeyErrorStateChange();
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
        return ((TableContentsGeneration)getActiveGeneration()).getRow(rowIndex);
    }

    private class TableImportExportActionInEditor extends TableImportExportAction {

        protected TableImportExportActionInEditor(Shell shell, ITableContents tableContents, boolean isImport) {
            super(shell, tableContents);
            if (isImport) {
                initImportAction();
            } else {
                initExportAction();
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

    private class Validator implements IInputValidator {

        private int indexCount = 0;

        public Validator(int requiredIndexCount) {
            indexCount = requiredIndexCount;
        }

        @Override
        public String isValid(String newText) {
            StringTokenizer tokenizer = getTokenizer(newText);
            int tokenizerItemCount = tokenizer.countTokens();

            ArrayList<Integer> values = new ArrayList<Integer>(tokenizerItemCount);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                try {
                    Integer value = Integer.valueOf(token);
                    if (values.contains(value) && indexCount < 0) {
                        return Messages.ContentPage_errorNoDuplicateIndices;
                    }
                    if (indexCount < 0
                            && (value.intValue() >= getTableContents().getNumOfColumns() || value.intValue() < 0)) {
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

            int difference = Math.abs(indexCount) - tokenizerItemCount;
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
