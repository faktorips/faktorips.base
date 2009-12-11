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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContentsGeneration;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.actions.TableImportExportAction;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.util.message.MessageList;

/**
 * The content-page for the <code>TableContentsEditor</code>. Allows the editing of
 * <code>TableContents</code> using a <code>TableViewer</code>.
 * 
 * @author Stefan Widmaier
 */
public class ContentPage extends IpsObjectEditorPage {

    final static String PAGE_ID = "Contents"; //$NON-NLS-1$

    private TableViewer tableViewer;

    private Table table;

    private class TableImportExportActionInEditor extends TableImportExportAction {

        protected TableImportExportActionInEditor(Shell shell, ITableContents tableContents, boolean isImport) {
            super(shell, tableContents);
            if (isImport) {
                initImportAction();
            } else {
                initExportAction();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run(IStructuredSelection selection) {
            if (super.runInternal(selection)) {
                tableViewer.setInput(getTableContents());
                tableViewer.refresh(true);
                redrawTable();
            }
        }
    }

    public ContentPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, Messages.ContentPage_title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        checkDifferences(formBody, toolkit);

        GridLayout layout = new GridLayout(1, false);
        formBody.setLayout(layout);

        table = createTable(formBody);
        initTableViewer(table, toolkit, formBody);
        NewRowAction newRowAction = new NewRowAction(tableViewer, this);
        DeleteRowAction deleteRowAction = new DeleteRowAction(tableViewer, this);
        initTablePopupMenu(table, deleteRowAction, newRowAction);

        /*
         * Create a single row if an empty tablecontents is opened. Otherwise no editing is
         * possible.
         */
        if (getActiveGeneration().getNumOfRows() == 0) {
            getActiveGeneration().newRow();
        }

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
            public void widgetDefaultSelected(SelectionEvent e) {
                deactivateCellEditors();
            }

            public void widgetSelected(SelectionEvent e) {
                deactivateCellEditors();
            }
        });
        table.getHorizontalBar().addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                deactivateCellEditors();
            }

            public void widgetSelected(SelectionEvent e) {
                deactivateCellEditors();
            }
        });
    }

    /*
     * Deactivates all active cell editor (i.e. the current active cell editor)
     */
    private void deactivateCellEditors() {
        CellEditor[] cellEditors = tableViewer.getCellEditors();
        for (int i = 0; i < cellEditors.length; i++) {
            cellEditors[i].deactivate();
        }
    }

    /**
     * Creates a Table with the given formBody as a parent and returns it. Inits the look, layout of
     * the table and adds a KeyListener that enables the editing of the first cell in the currently
     * selected row by pressing "F2".
     * 
     * @param formBody
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
     * Inits the <code>TableViewer</code> for this page. Sets content- and labelprovider, column
     * headers and widths, column properties, cell editors, sorter. Inits popupmenu and
     * hoverservice.
     * 
     * @param table
     * @param toolkit
     * @param formBody
     */
    private void initTableViewer(Table table, UIToolkit toolkit, Composite formBody) {
        try {
            table.removeAll();
            increaseHeightOfTableRow(table, getTableContents().getNumOfColumns());

            tableViewer = new TableViewer(table);
            tableViewer.setUseHashlookup(true);
            tableViewer.setContentProvider(new TableContentsContentProvider());
            TableContentsLabelProvider labelProvider = new TableContentsLabelProvider();
            tableViewer.setLabelProvider(labelProvider);

            ITableStructure tableStructure = getTableStructure();
            String[] columnProperties = new String[getTableContents().getNumOfColumns()];
            for (int i = 0; i < getTableContents().getNumOfColumns(); i++) {
                String columnName;
                if (tableStructure == null) {
                    columnName = Messages.ContentPage_Column + (i + 1);
                } else {
                    columnName = tableStructure.getColumn(i).getName();
                }
                TableColumn column = new TableColumn(table, SWT.LEFT, i);
                column.setWidth(125);
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
                    IpsCellEditor cellEditor = factory.createCellEditor(toolkit, dataType, null, tableViewer, i,
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
     * 
     * @param table
     * @param deleteRowAction
     */
    private void initTablePopupMenu(Table table, DeleteRowAction deleteRowAction, NewRowAction newRowAction) {
        // popupmenu
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(false);
        menuMgr.add(newRowAction);
        menuMgr.add(deleteRowAction);
        Menu menu = menuMgr.createContextMenu(table);
        table.setMenu(menu);
        // do not register to avoid mb additions
        // getSite().registerContextMenu(menuMgr, tableViewer);
    }

    private void increaseHeightOfTableRow(Table table, final int numOfColumns) {
        // add paint lister to increase the height of the table row,
        // because @since 3.2 in edit mode the cell becomes a border and the bottom pixel of the
        // text is hidden
        Listener paintListener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                    case SWT.MeasureItem: {
                        if (numOfColumns == 0) {
                            return;
                        }
                        TableItem item = (TableItem)event.item;
                        // column 0 will be used to determine the height,
                        // <code>event.index<code> couldn't be used because it is only available
                        // @since 3.2, that's ok because the height is always the same, even if the
                        // column contains no text, the height only depends on the font
                        String text = getText(item, 0);
                        Point size = event.gc.textExtent(text);
                        // the height will be increased by 5 pixel
                        event.height = Math.max(event.height, size.y + 5);
                        break;
                    }
                }
            }

            String getText(TableItem item, int column) {
                String text = item.getText(column);
                return text;
            }
        };
        table.addListener(SWT.MeasureItem, paintListener);
    }

    private void checkDifferences(Composite formBody, UIToolkit toolkit) {
        if (!getIpsObject().getIpsSrcFile().isMutable()) {
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
                    msg = NLS.bind(Messages.ContentPage_msgAddMany, String.valueOf(difference), String
                            .valueOf(getTableContents().getNumOfColumns()));

                } else if (difference == 1) {
                    title = Messages.ContentPage_titleMissingColumn;
                    msg = NLS
                            .bind(Messages.ContentPage_msgAddOne, String.valueOf(getTableContents().getNumOfColumns()));
                } else if (difference == -1) {
                    title = Messages.ContentPage_titleTooMany;
                    msg = NLS.bind(Messages.ContentPage_msgRemoveOne, String.valueOf(getTableContents()
                            .getNumOfColumns()));
                } else if (difference < -1) {
                    title = Messages.ContentPage_titleTooMany;
                    msg = NLS.bind(Messages.ContentPage_msgRemoveMany, String.valueOf(Math.abs(difference)), String
                            .valueOf(getTableContents().getNumOfColumns()));
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
            throw new RuntimeException(e);
        }
    }

    private void insertColumnsAt(String insertIndices) {
        int[] indices = getIndices(insertIndices);
        for (int i = 0; i < indices.length; i++) {
            ((TableContents)getTableContents()).newColumnAt(indices[i] + i, ""); //$NON-NLS-1$
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

    private class Validator implements IInputValidator {
        private int indexCount = 0;

        public Validator(int requiredIndexCount) {
            indexCount = requiredIndexCount;
        }

        /**
         * {@inheritDoc}
         */
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

    /**
     * Redraws the table.
     */
    void redrawTable() {
        table.redraw();
    }

    private boolean wasUniqueKeyErrorStateChanged() {
        return ((TableContentsGeneration)getActiveGeneration()).wasUniqueKeyErrorStateChange();
    }

    public void refreshTable(final IRow row) {
        tableViewer.refresh(row);
        if (wasUniqueKeyErrorStateChanged()) {
            // either the unique key error is solved or there is a new unique key error
            // refresh the rest of the table because an unique key error concerns to more than one
            // row
            refreshTable();
        }
    }

    public void refreshTable() {
        tableViewer.refresh();
    }

}
