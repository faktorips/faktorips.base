/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.spreadsheet;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class SpreadsheetControl extends Composite {

    /** the underlying table control */
    private Table table;
    private TableViewer viewer;

    private TableContentProvider contentProvider;
    private ColumnInfo[] columnInfos;

    private TableCursor cursor;

    private TableEditor tableEditor;

    /**
     * if a row is being added by tabbing over the last cell, the new row is stored here. If the
     * user leaves the row without entering any values. The row is automatically removed again.
     */
    private Object pendingRow = null;

    /**
     * array of cell editors, one for each column. The cell editors are generated during
     * initColumns.
     */
    private TableCellEditor[] editors_ = null;

    /** Popup menu of the table cursor. */
    private Menu popupMenu_ = null;

    /** the required row height. */
    private int rowHeight = 12;

    /**
     * the editor generates an image of the required column height that will be inserted into the
     * last column to size the column.
     */
    private Image rowHeightImage = null;

    public SpreadsheetControl(Composite parent, int style, TableContentProvider contentProvider) {
        super(parent, SWT.NONE);
        this.contentProvider = contentProvider;
        columnInfos = contentProvider.getColumnInfos();

        initComposite();
        initTableControl(style);
        initColumns();
        initCursor();
        tableEditor = new org.eclipse.swt.custom.TableEditor(table);
        initTableViewer();
        initPopupMenu();
    }

    private void initComposite() {
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        // add a resize listener to the composite that resizes the
        // table control.
        Listener resizeListener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                switch (e.type) {
                    case SWT.Resize:
                        Rectangle clientRect = SpreadsheetControl.this.getClientArea();
                        table.setSize(clientRect.width, clientRect.height);
                        break;
                }
            }
        };
        addListener(SWT.Resize, resizeListener);
    }

    private void initTableControl(int style) {
        table = new Table(this, style);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableKeyListener keyListener = new TableKeyListener();
        table.addKeyListener(keyListener);
    }

    private void initCursor() {
        cursor = new TableCursor(this);
        // add a cursor listener, that processes the tab key to proceed to
        // the next column in case of tabs.
        cursor.addTraverseListener(new TableTraverseListener());
        cursor.addSelectionListener(new SelectionAdapter() {
            // when the TableContentsEditor is over a cell, select the corresponding row in
            // the table
            @Override
            public void widgetSelected(SelectionEvent e) {
                table.setSelection(new TableItem[] { cursor.getTableItem() });
                // ensure the item is visible.
                ensureItemIsVisible();

                // check for an removal of a added row.
                checkForAddedRowRemoval();
            }

            // when the user hits "ENTER" in the TableCursor, pop up a text editor so that
            // they can change the text of the cell
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        TableKeyListener keyListener = new TableKeyListener();
        cursor.addKeyListener(keyListener);

        // Support for double clicking on a cell.
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                beginCellEdit(null);
            }
        };
        cursor.addMouseListener(mouseAdapter);
    }

    private void initPopupMenu() {
        // build the context menu for the table cursor.
        popupMenu_ = new Menu(cursor);

        MenuItem item = new MenuItem(popupMenu_, SWT.NONE);
        item.setAccelerator(SWT.ALT | SWT.DEL);
        item.setText(Messages.SpreadsheetControl_menuDelete);
        item.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Delete.gif", true)); //$NON-NLS-1$
        item.setData("deleteRow"); //$NON-NLS-1$

        item.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SpreadsheetControl.this.deleteRow();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        item = new MenuItem(popupMenu_, SWT.NONE);
        item.setAccelerator(SWT.DEL);
        item.setText(Messages.SpreadsheetControl_menuSetNull);
        item.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Clear.gif", true)); //$NON-NLS-1$
        item.setData("clearCell"); //$NON-NLS-1$

        item.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SpreadsheetControl.this.clearCell();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        cursor.setMenu(popupMenu_);
        table.setMenu(popupMenu_);

        /*
         * add a mouse listener that is attached to the table control and resends event triggered by
         * the right mouse button to the table cursor. Otherwise the popupmenu won't work on
         * non-selected cells.
         */
        PopupMenuListener popupMenuListener = new PopupMenuListener();

        popupMenu_.addListener(SWT.Show, popupMenuListener);
        popupMenu_.addListener(SWT.Hide, popupMenuListener);
        popupMenu_.addListener(SWT.Move, popupMenuListener);
        table.addMouseListener(popupMenuListener);
        table.addMouseListener(popupMenuListener);
    }

    /**
     * Creates the TableColumns for the table control. Also used to reflect structural changes on
     * the table on the GUI by adding or removing table columns in the table control.
     */
    private void initColumns() {
        // make the table cursor invisible, since column resizing will lead
        // to an exception, if the table has no rows.
        if (cursor != null) {
            cursor.setVisible(false);
            cursor.setEnabled(false);
            cursor.removeColumnResizeListeners();
        }

        // the table columns are initialized every time, the table content
        // page is activated. So, we save the column width first and restore
        // it afterwards.
        int columnWidths[] = new int[table.getColumnCount()];
        for (int i = 0, s = table.getColumnCount(); i < s; i++) {
            columnWidths[i] = table.getColumn(i).getWidth();
        }

        table.removeAll();
        // add row sizing column
        TableColumn sizingColumn = new TableColumn(table, SWT.NONE);
        sizingColumn.setWidth(0);

        // create columns
        for (int i = 0; i < columnInfos.length; i++) {
            TableColumn column = new TableColumn(table, columnInfos[i].style);
            column.setText(columnInfos[i].columnName);
            // restore the original column width, or default to 100 pixel.
            if (i < columnWidths.length) {
                column.setWidth(columnWidths[i]);
            } else { // set default widht
                column.setWidth(columnInfos[i].initialWidth);
            }
            column.addSelectionListener(new SortListener());
        }

        // create the cell editors for each column and store them
        // in the editors array.
        createCellEditors();

        if (cursor != null) {
            cursor.addColumnResizeListeners();
        }
    }

    public int getColumnCount() {
        return columnInfos.length;
    }

    /**
     * Create the cell editors of the table.
     */
    private void createCellEditors() {
        // if there are already cell editors dispose them and recreate them
        disposeCellEditors();

        editors_ = new TableCellEditor[columnInfos.length];
        int height = 0;
        for (int i = 0; i < columnInfos.length; i++) {
            editors_[i] = new TableCellEditor(this, columnInfos[i].createEditField(table));
            height = Math.max(editors_[i].getControlHeight(), height);
        }

        if (rowHeight != height) {
            rowHeight = height;
            if (rowHeightImage != null) {
                rowHeightImage.dispose();
                rowHeightImage = null; // force image recreation
            }
        }
    }

    /**
     * Dispose the allocated cell editors.
     */
    private void disposeCellEditors() {
        if (editors_ == null) {
            return;
        }
        for (int i = 0; i < editors_.length; i++) {
            if (editors_[i] != null) {
                editors_[i].dispose();
                editors_[i] = null;
            }
        }
        editors_ = null;
    }

    public void setLinesVisible(boolean show) {
        table.setLinesVisible(show);
    }

    public void setHeaderVisible(boolean visible) {
        table.setHeaderVisible(visible);
    }

    public void setCusor(int row, int column) {
        cursor.setSelection(row, column);
    }

    TableCursor getTableCursor() {
        return cursor;
    }

    public TableViewer getTableViewer() {
        return viewer;
    }

    Object getPendingRow() {
        return pendingRow;
    }

    /**
     * Returns the underlying swt table control.
     */
    public Table getTable() {
        return table;
    }

    ColumnInfo[] getColumnInfos() {
        return columnInfos;
    }

    TableEditor getTableEditor() {
        return tableEditor;
    }

    /**
     * Dispose all allocated resources.
     */
    @Override
    public void dispose() {
        // dispose cursor
        if (cursor != null && !cursor.isDisposed()) {
            cursor.dispose();
            cursor = null;
        }

        disposeCellEditors();

        // dispose the sizing image.
        if (rowHeightImage != null && !rowHeightImage.isDisposed()) {
            rowHeightImage.dispose();
            rowHeightImage = null;
        }

        viewer = null;

        if (popupMenu_ != null && !popupMenu_.isDisposed()) {
            popupMenu_.dispose();
        }
        popupMenu_ = null;
    }

    /**
     * Create the viewer object. The viewer object is created once at the first call to
     * createTableViewer. Each additional call will reinitalize the viewer to reflect the changes of
     * the table.
     */
    private void initTableViewer() {
        viewer = new TableViewer(table);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput("DummyInput"); //$NON-NLS-1$
    }

    /**
     * Get the value for the given cell.
     * 
     * @param item the item to retrieve the value from
     * @return Object, the value, may be null.
     */
    Object getCellValue(TableItem item, int externalColumnIndex) {
        return columnInfos[externalColumnIndex].getValue(item.getData());
    }

    /**
     * Get the value for the given cell.
     * 
     * @param externalColumnIndex the column to get the value from
     * @return Object, the value, may be null.
     */
    Object getCellValue(Object row, int externalColumnIndex) {
        return columnInfos[externalColumnIndex].getValue(row);
    }

    void setCellValue(TableItem item, int externalColumnIndex, Object newValue) {
        columnInfos[externalColumnIndex].setValue(item.getData(), newValue);
        viewer.refresh(item.getData(), true);
        cursor.redraw();
        notifyListeners(SWT.Modify, new Event());
    }

    /**
     * Sorts the given column in the table.
     */
    public void sortColumn(int externalColumnIndex) {
        Sorter sorter = (Sorter)viewer.getSorter();
        if (sorter == null) {
            sorter = new Sorter(this, externalColumnIndex, columnInfos[externalColumnIndex].comparator);
            viewer.setSorter(sorter);
        } else {
            if (sorter.getExternalColumnIndex() == externalColumnIndex) {
                if (sorter.nextOrder() == Sorter.NATURAL) {
                    viewer.setSorter(null);
                }
                refresh();
            } else {
                sorter.setExternalColumn(externalColumnIndex);
            }
        }

        // set the cursor.
        int selection = table.getSelectionIndex();
        if (selection > 0) {
            cursor.setSelection(selection, cursor.getInternalColumn());
        }
    }

    /**
     * Refresh the contents of the table.
     */
    public void refresh() {
        viewer.refresh();
    }

    /**
     * Delete the currently selected row. If no row is selected this method does simply nothing. The
     * method is used by the menu handler.
     */
    protected void deleteRow() {
        TableItem item = cursor.getTableItem();
        if (item == null) {
            return;
        }
        int row = table.indexOf(item);
        if (!contentProvider.deleteRow(item.getData())) {
            return;
        }
        // check for the added row...
        if (pendingRow == item.getData()) {
            pendingRow = null;
        }
        // avoid that the table gets empty.
        if (table.getItemCount() <= 0) {
            addRow();
        }

        // refresh, otherwise the table is outdated.
        viewer.refresh();
        notifyListeners(SWT.Modify, new Event());

        // select another row.
        if (row >= table.getItemCount()) {
            row = table.getItemCount() - 1;
        }
        table.select(row);
        cursor.setSelection(row, cursor.getInternalColumn());
    }

    /**
     * Sets the currently selected cell to null. This method is used by the menu handler.
     */
    protected void clearCell() {
        TableItem item = cursor.getTableItem();
        int col = cursor.getExternalColumn();

        if (item != null) {
            setCellValue(item, col, null);
            table.select(table.indexOf(item));
        }
    }

    /**
     * This method is called whenever a selection is made. If a new row has been added to the end of
     * the table. The row is checked for empty values. If a row consists only of empty values, the
     * row is removed again.
     */
    protected void checkForAddedRowRemoval() {
        if (pendingRow == null) {
            return;
        }
        int row = table.getSelectionIndex();
        if (table.getItem(row).getData() != pendingRow) {
            if (contentProvider.deletePendingRow(pendingRow)) {
                contentProvider.deleteRow(pendingRow);
                // refresh works async, setting the selection on the table cursor
                // may
                viewer.refresh(false);
                cursor.setSelection(table.getSelectionIndex(), cursor.getInternalColumn());

                notifyListeners(SWT.Modify, new Event());
            }
            pendingRow = null;
        }
    }

    /**
     * Method is used to ensure that the selected item is visible. This may include horizontal
     * scrolling to the selected item.
     */
    void ensureItemIsVisible() {
        Rectangle cursorBounds = cursor.getBounds();
        Rectangle clientArea = table.getClientArea();

        // no scrolling possible.
        if (clientArea.width <= cursorBounds.width) {
            return;
        }

        // check scroll right
        if (cursorBounds.x < clientArea.x) {
            cursor.setSelection(table.getSelectionIndex(), cursor.getInternalColumn());
        } else if (cursorBounds.x + cursorBounds.width > clientArea.x + clientArea.width) {
            cursor.setSelection(table.getSelectionIndex(), cursor.getInternalColumn());
        }
    }

    /**
     * This method is called whenever the user presses the tab key.
     * 
     * @param forward if true, the cursor is moved forward, otherwise it is set to the previous
     *            cell.
     */
    protected void processTabEvent(boolean forward) {
        TableItem item = cursor.getTableItem();
        if (item == null) {
            return;
        }
        if (forward) {
            cursor.moveToNextCell();
        } else {
            cursor.moveToPreviousCell();
        }
    }

    /**
     * Adds a new row to the underlyling table model.
     */
    protected void addRow() {
        if (getColumnCount() == 0) {
            return;
        }
        pendingRow = contentProvider.newRow();
        viewer.refresh();
        notifyListeners(SWT.Modify, new Event());
    }

    /**
     * Begin editing the currently selected cell.
     * 
     * @param event may be null.
     */
    protected void beginCellEdit(KeyEvent event) {
        if (!cursor.isEnabled() || !cursor.isVisible()) {
            return;
        }
        TableItem item = cursor.getTableItem();
        int externalColumnIndex = cursor.getExternalColumn();
        if (!columnInfos[externalColumnIndex].modifiable) {
            return;
        }
        // activate the editor.
        editors_[externalColumnIndex].activate(item, externalColumnIndex);
        if (event != null) {
            if (event.character != SWT.BS && event.character != SWT.DEL) {
                editors_[externalColumnIndex].processChar(event.character);
            }
        }
    }

    public Image getRowHeightImage() {
        if (rowHeightImage == null) {
            int h = Math.max(table.getItemHeight(), rowHeight);
            rowHeightImage = new Image(table.getDisplay(), 16, h);
            GC gc = new GC(rowHeightImage);
            gc.setBackground(table.getBackground());
            gc.fillRectangle(rowHeightImage.getBounds());
            gc.dispose();
        }
        return rowHeightImage;
    }

    private boolean isSizingColumn(int columnIndex) {
        return columnIndex == 0;
    }

    private class LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (isSizingColumn(columnIndex)) {
                return getRowHeightImage();
            } else {
                return columnInfos[columnIndex - 1].getImage(element);
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (isSizingColumn(columnIndex)) {
                return ""; //$NON-NLS-1$
            }
            String text = columnInfos[columnIndex - 1].getText(element);

            if (text == null) {
                return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
            return text;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // Nothing to do
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return true;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // Nothing to do
        }

    }

    /**
     * The traverse listener processes the key events for cell editors and table cursors. Its main
     * purpose is to provide handling of the tab key.
     */
    private class TableTraverseListener implements TraverseListener {

        @Override
        public void keyTraversed(TraverseEvent e) {
            switch (e.detail) {
                case SWT.TRAVERSE_TAB_NEXT:
                    processTabEvent(true);
                    e.doit = false;
                    break;

                case SWT.TRAVERSE_TAB_PREVIOUS:
                    processTabEvent(false);
                    e.doit = false;
                    break;

                case SWT.TRAVERSE_RETURN:
                    // create a new event.
                    Event evt = new Event();
                    evt.keyCode = SWT.ARROW_DOWN;
                    evt.display = e.display;
                    evt.widget = e.widget;
                    cursor.notifyListeners(SWT.KeyDown, evt);
                    e.doit = false;
                    break;
            }
        }
    }

    /**
     * Key listener to process begin edits.
     */
    private class TableKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.F2 && (e.stateMask & SWT.MODIFIER_MASK) == 0) {
                beginCellEdit(null);
            }

            else if (e.character > 0 && e.character != SWT.CR && e.character != '\t') {
                beginCellEdit(e);
            }
        }
    }

    /**
     * Selection listener to process sort requests.
     */
    private class SortListener implements SelectionListener {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget instanceof TableColumn) {
                TableColumn col = (TableColumn)e.widget;
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (table.getColumn(i) == col) {
                        sortColumn(i - 1);
                        break;
                    }
                }
            }
        }
    }

    private class PopupMenuListener extends MouseAdapter implements Listener {

        private boolean tableEvent_ = false;
        private TableItem item_ = null;

        @Override
        public void handleEvent(Event event) {
            switch (event.type) {
                case SWT.Show:
                case SWT.Move:
                    boolean disable = (tableEvent_ && item_ == null);
                    MenuItem mitems[] = popupMenu_.getItems();
                    for (MenuItem mitem : mitems) {
                        mitem.setEnabled(!disable);
                    }
                    break;

                case SWT.Hide:
                    tableEvent_ = false;
                    item_ = null;
                    break;
            }
        }

        @Override
        public void mouseDown(MouseEvent e) {
            if (e.widget == table && e.button == 3) {
                item_ = table.getItem(new Point(e.x, e.y));
                tableEvent_ = true;
            } else {
                item_ = null;
                tableEvent_ = false;
            }
        }
    }

}
