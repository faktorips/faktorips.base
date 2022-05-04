/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.util.Arrays;

import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A collection of utility methods for Tables.
 */
public class TableUtil {

    private TableUtil() {
        // Prevent instantiation by making the constructor private.
    }

    public static void createEditor(TableViewer tableViewer) {
        TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer,
                new FocusCellOwnerDrawHighlighter(tableViewer));

        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
                        || event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }
        };
        TableViewerEditor.create(tableViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.KEYBOARD_ACTIVATION);
    }

    public static void updateColumnWidths(TableViewer tableViewer, TableViewerColumn... columns) {
        for (int i = 0; i < columns.length - 1; i++) {
            columns[i].getColumn().pack();
        }

        // avoid the horizontal scrollbar being shown by decreasing the width of the
        // third tableViewerColumn by 5 pixels (taking a minimal width of 150 into account)
        int thirdColWidth = Math.max(150, tableViewer.getControl().getSize().x - 5 - Arrays.stream(columns)
                .limit(columns.length - 1).map(TableViewerColumn::getColumn).mapToInt(TableColumn::getWidth).sum());

        columns[columns.length - 1].getColumn().setWidth(thirdColWidth);
    }

    /**
     * Increases the height of a table row. This might be necessary because otherwise control
     * elements won't be displayed correctly.
     * 
     * @param table the table, which rows needs to be resized
     * @param numOfColumns number of columns of the table
     * @param amount number of pixel to increase the height by
     */
    public static void increaseHeightOfTableRows(Table table, final int numOfColumns, final int amount) {
        // add paint lister to increase the height of the table row.
        Listener paintListener = new PaintListener(amount, numOfColumns);
        table.addListener(SWT.MeasureItem, paintListener);
    }

    private static final class PaintListener implements Listener {
        private final int amount;
        private final int numOfColumns;

        private PaintListener(int amount, int numOfColumns) {
            this.amount = amount;
            this.numOfColumns = numOfColumns;
        }

        @Override
        public void handleEvent(Event event) {
            if (event.type == SWT.MeasureItem) {
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
                // the height will be increased by amount pixels.
                event.height = Math.max(event.height, size.y + amount);
            }
        }

        String getText(TableItem item, int column) {
            String text = item.getText(column);
            return text;
        }
    }
}
