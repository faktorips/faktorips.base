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

package org.faktorips.devtools.core.ui.controls.spreadsheet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TypedListener;

public class TableCursor extends Canvas {
    private Table table;
    private SpreadsheetControl tableControl;
    private int row_ = 0;
    private int internalColumnIndex = 0;
    private Listener tableListener;
    private Listener resizeListener;

    /**
     * Constructs a new instance of this class given its parent table.
     */
    TableCursor(SpreadsheetControl control) {
        super(control.getTable(), SWT.NONE);
        tableControl = control;
        table = control.getTable();
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                    case SWT.Dispose:
                        dispose(event);
                        break;
                    case SWT.KeyDown:
                        keyDown(event);
                        break;
                    case SWT.Paint:
                        paint(event);
                        break;
                    case SWT.Traverse:
                        traverseEvent(event);
                        break;
                }
            }
        };
        addListener(SWT.Dispose, listener);
        addListener(SWT.KeyDown, listener);
        addListener(SWT.Paint, listener);
        addListener(SWT.Traverse, listener);

        tableListener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                    case SWT.MouseDown:
                        tableMouseDown(event);
                        break;
                    case SWT.FocusIn:
                        tableFocusIn(event);
                        break;
                }
            }
        };
        table.addListener(SWT.FocusIn, tableListener);
        table.addListener(SWT.MouseDown, tableListener);

        resizeListener = new Listener() {
            public void handleEvent(Event event) {
                resize();
            }
        };
        ScrollBar hBar = table.getHorizontalBar();
        if (hBar != null) {
            hBar.addListener(SWT.Selection, resizeListener);
        }
        ScrollBar vBar = table.getVerticalBar();
        if (vBar != null) {
            vBar.addListener(SWT.Selection, resizeListener);
        }
    }

    int getInternalColumn() {
        return internalColumnIndex;
    }

    int getExternalColumn() {
        return internalColumnIndex - 1;
    }

    /**
     * IMethod to disable the column listeners. This should be called before the columns are
     * allocated or changed.
     */
    public void removeColumnResizeListeners() {
        for (int i = 0, s = table.getColumnCount(); i < s; i++) {
            TableColumn column = table.getColumn(i);
            column.removeListener(SWT.Resize, resizeListener);
        }
    }

    /**
     * IMethod to enable the column listeners. This should be called after the columns are allocated
     * or changed.
     */
    void addColumnResizeListeners() {
        for (int i = 0, s = table.getColumnCount(); i < s; i++) {
            TableColumn column = table.getColumn(i);
            column.addListener(SWT.Resize, resizeListener);
        }
    }

    /**
     * Adds the listener to the collection of listeners who will be notified when the receiver's
     * selection changes, by sending it one of the messages defined in the
     * <code>SelectionListener</code> interface.
     * <p>
     * When <code>widgetSelected</code> is called, the item field of the event object is valid. If
     * the reciever has <code>SWT.CHECK</code> style set and the check selection changes, the event
     * object detail field contains the value <code>SWT.CHECK</code>.
     * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
     * </p>
     * 
     * @param listener the listener which should be notified
     * 
     * @exception IllegalArgumentException <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * @exception SWTException <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
     *                the receiver</li>
     *                </ul>
     * 
     * @see SelectionListener
     * @see SelectionEvent
     */
    public void addSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }

    void dispose(Event event) {
        Display display = getDisplay();
        display.asyncExec(new Runnable() {
            public void run() {
                if (table.isDisposed()) {
                    return;
                }
                table.removeListener(SWT.FocusIn, tableListener);
                table.removeListener(SWT.MouseDown, tableListener);
                int columns = table.getColumnCount();
                for (int i = 0; i < columns; i++) {
                    TableColumn column = table.getColumn(i);
                    column.removeListener(SWT.Resize, resizeListener);
                }
                ScrollBar hBar = table.getHorizontalBar();
                if (hBar != null) {
                    hBar.removeListener(SWT.Selection, resizeListener);
                }
                ScrollBar vBar = table.getVerticalBar();
                if (vBar != null) {
                    vBar.removeListener(SWT.Selection, resizeListener);
                }
            }
        });
    }

    void keyDown(Event event) {
        switch (event.character) {
            case SWT.CR:
                notifyListeners(SWT.DefaultSelection, new Event());
                return;
        }
        switch (event.keyCode) {
            case SWT.ARROW_UP:
                setRowColumn(row_ - 1, internalColumnIndex, true);
                break;
            case SWT.ARROW_DOWN:
                setRowColumn(row_ + 1, internalColumnIndex, true);
                break;
            case SWT.ARROW_LEFT:
            case SWT.ARROW_RIGHT: {
                int leadKey = (getStyle() & SWT.RIGHT_TO_LEFT) != 0 ? SWT.ARROW_RIGHT : SWT.ARROW_LEFT;
                if (event.keyCode == leadKey) {
                    moveToPreviousCell();
                } else {
                    moveToNextCell();
                }
                break;
            }
            case SWT.HOME:
                setRowColumn(0, internalColumnIndex, true);
                break;
            case SWT.END: {
                int row = table.getItemCount() - 1;
                setRowColumn(row, internalColumnIndex, true);
                break;
            }
            case SWT.PAGE_UP: {
                int index = table.getTopIndex();
                if (index == row_) {
                    Rectangle rect = table.getClientArea();
                    TableItem item = table.getItem(index);
                    Rectangle itemRect = item.getBounds(0);
                    rect.height -= itemRect.y;
                    int height = table.getItemHeight();
                    int page = Math.max(1, rect.height / height);
                    index = Math.max(0, index - page + 1);
                }
                setRowColumn(index, internalColumnIndex, true);
                break;
            }
            case SWT.PAGE_DOWN: {
                int index = table.getTopIndex();
                Rectangle rect = table.getClientArea();
                TableItem item = table.getItem(index);
                Rectangle itemRect = item.getBounds(0);
                rect.height -= itemRect.y;
                int height = table.getItemHeight();
                int page = Math.max(1, rect.height / height);
                int end = table.getItemCount() - 1;
                index = Math.min(end, index + page - 1);
                if (index == row_) {
                    index = Math.min(end, index + page - 1);
                }
                setRowColumn(index, internalColumnIndex, true);
                break;
            }
        }
    }

    /**
     * Paint the cell of the table cursor. The <code>paint</code> method is different from the
     * original table cursor.
     * 
     * @param event, the paint event to process.
     */
    void paint(Event event) {
        if (!isVisible()) {
            return;
        }

        validatePosition();

        GC gc = event.gc;
        Display display = getDisplay();
        gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
        gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
        gc.fillRectangle(event.x, event.y, event.width, event.height);

        TableItem item = getTableItem();
        int x = 0, y = 0;
        Point size = getSize();
        Image image = item.getImage(internalColumnIndex);

        if (image != null) {
            Rectangle imageSize = image.getBounds();
            int imageY = y + (int)(((float)size.y - (float)imageSize.height) / 2.0);
            gc.drawImage(image, x, imageY);
            x += imageSize.width;
        }

        x += (internalColumnIndex == 0) ? 2 : 4;

        int textY = y + (int)(((float)size.y - (float)gc.getFontMetrics().getHeight()) / 2.0 + 0.5);

        gc.drawString(item.getText(internalColumnIndex), x, textY);

        if (isFocusControl()) {
            int lw = gc.getLineWidth();
            gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
            gc.setLineWidth(1);
            gc.drawRectangle(event.x, event.y, event.width - 1, event.height - 1);

            gc.setLineWidth(lw);
        }
    }

    /**
     * The validate position method is used to ensure a valid cell is active.
     */
    void validatePosition() {
        int row = Math.min(table.getItemCount() - 1, Math.max(0, row_));
        int col = Math.min(table.getColumnCount() - 1, Math.max(0, internalColumnIndex));

        if (row < 0) {
            row = 0;
        }
        if (col < 1) {
            col = 1;
        }

        if (row_ != row || internalColumnIndex != col) {
            setRowColumn(row, col, true);
        }
    }

    void tableFocusIn(Event event) {
        if (isDisposed()) {
            return;
        }
        if (isVisible()) {
            setFocus();
        }
    }

    void tableMouseDown(Event event) {
        if (isDisposed() || !isVisible()) {
            return;
        }
        Point pt = new Point(event.x, event.y);
        Rectangle clientRect = table.getClientArea();
        int columns = table.getColumnCount();
        int start = table.getTopIndex();
        int end = table.getItemCount();
        for (int row = start; row < end; row++) {
            TableItem item = table.getItem(row);
            for (int column = 0; column < columns; column++) {
                Rectangle rect = item.getBounds(column);
                if (rect.y > clientRect.y + clientRect.height) {
                    return;
                }
                if (rect.contains(pt)) {
                    setRowColumn(row, column, true);
                    setFocus();
                    return;
                }
            }
        }
    }

    private void traverseEvent(Event event) {
        switch (event.detail) {
            case SWT.TRAVERSE_ARROW_NEXT:
            case SWT.TRAVERSE_ARROW_PREVIOUS:
            case SWT.TRAVERSE_RETURN:
                event.doit = false;
                return;
        }
        event.doit = true;
    }

    void setRowColumn(int row, int column, boolean notify) {
        if (column < 0) {
            column = 0;
        }

        if (0 <= row && row < table.getItemCount()) {
            if (0 <= column && column < table.getColumnCount()) {
                this.row_ = row;
                this.internalColumnIndex = column;
                TableItem item = table.getItem(row);
                table.showItem(item);
                setBounds(item.getBounds(column));
                redraw();
                if (notify) {
                    notifyListeners(SWT.Selection, new Event());
                }
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        checkWidget();
        if (visible) {
            resize();
        }
        super.setVisible(visible);
    }

    void resize() {
        TableItem item = table.getItem(row_);
        setBounds(item.getBounds(internalColumnIndex));
    }

    /**
     * Returns the row number, the cursor is positioned in.
     */
    int getRow() {
        return row_;
    }

    /**
     * Returns the row over which the TableCursor is positioned.
     * 
     * @return the item for the current position
     * 
     * @exception SWTException <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
     *                the receiver</li>
     *                </ul>
     */
    public TableItem getTableItem() {
        checkWidget();
        return table.getItem(row_);
    }

    /**
     * Positions the TableCursor over the cell at the given row and column in the parent table.
     * 
     * @param row the index of the row for the cell to select
     * @param column the index of column for the cell to select
     * 
     * @exception SWTException <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
     *                the receiver</li>
     *                </ul>
     * 
     */
    public void setSelection(int row, int column) {
        checkWidget();
        if (row < 0 || row >= table.getItemCount() || column < 0 || column >= table.getColumnCount()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        setRowColumn(row, column, false);
    }

    /**
     * Positions the TableCursor over the cell at the given row and column in the parent table.
     * 
     * @param row the TableItem of the row for the cell to select
     * @param column the index of column for the cell to select
     * 
     * @exception SWTException <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
     *                the receiver</li>
     *                </ul>
     * 
     */
    public void setSelection(TableItem row, int column) {
        checkWidget();
        if (row == null || row.isDisposed() || column < 0 || column >= table.getColumnCount()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        setRowColumn(table.indexOf(row), column, false);
    }

    void moveToNextCell() {
        internalColumnIndex++;
        if (internalColumnIndex >= table.getColumnCount()) {
            internalColumnIndex = 1;
            row_++;
            if (row_ >= table.getItemCount()) {
                tableControl.addRow();
            }
            row_ = Math.min(row_, table.getItemCount() - 1);
        }
        setRowColumn(row_, internalColumnIndex, true);
    }

    void moveToPreviousCell() {
        internalColumnIndex--;
        if (internalColumnIndex <= 0) {
            internalColumnIndex = table.getColumnCount() - 1;
            if (row_ > 0) {
                row_--;
            }
        }
        setRowColumn(row_, internalColumnIndex, true);
    }
}
