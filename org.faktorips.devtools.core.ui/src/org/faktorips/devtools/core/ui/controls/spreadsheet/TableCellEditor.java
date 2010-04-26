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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.ArgumentCheck;

/**
 * The table cell editControl is a wrapper class that manages the editing of table cells. It
 * allocates a type-safe field editControl instance for the column it edits and handles activation
 * and deactivation.
 * 
 * @author Stephan Jacobi
 */
class TableCellEditor {

    private SpreadsheetControl tableControl;
    private Control editControl;
    private EditField editField;
    private TableItem tableItem = null;
    private int externalColumnIndex = 0;
    private TraverseListener traverseListener = null;

    public TableCellEditor(SpreadsheetControl owner, EditField field) {
        super();
        ArgumentCheck.notNull(owner);
        ArgumentCheck.notNull(field);
        this.tableControl = owner;
        this.editField = field;
        this.editControl = field.getControl();

        // and add a listener to the control that manages focus lost and
        // dispose.
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                    case SWT.FocusIn:
                        setSelection();
                        break;

                    case SWT.FocusOut:
                        commitChanges();
                        break;

                    case SWT.Dispose: {
                        editControl.removeListener(SWT.FocusIn, this);
                        editControl.removeListener(SWT.FocusOut, this);
                        editControl.removeListener(SWT.Dispose, this);
                        editControl.removeTraverseListener(traverseListener);
                        break;
                    }
                }
            }
        };

        editControl.addListener(SWT.FocusIn, listener);
        editControl.addListener(SWT.FocusOut, listener);
        editControl.addListener(SWT.Dispose, listener);

        // add a traverse listener, that handles tab, return and escape events.
        traverseListener = new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                processKeyEvent(e);
            }
        };
        editControl.addTraverseListener(traverseListener);
    }

    /**
     * Activate the editControl. The editControl instance is the sizing parent of the editControl
     * control.
     * 
     * @param item, the table item to edit.
     * @param col, the column to edit.
     */
    void activate(TableItem item, int externalColumnIndex) {
        this.tableItem = item;
        this.externalColumnIndex = externalColumnIndex;
        // hide the table cursor.
        tableControl.getTableCursor().setVisible(false);

        TableEditor tableEditor = tableControl.getTableEditor();
        tableEditor.grabHorizontal = true;
        tableEditor.grabVertical = true;

        // get the value of the cell.
        Object value = tableControl.getCellValue(item, externalColumnIndex);
        // TODO: old code: don't set the value unless it is not null
        editField.setValue(value);

        tableEditor.setEditor(editControl, item, externalColumnIndex + 1);

        // tableEditor_.setEditor calls setVisible on the control.
        // do not call it again, since it will produce lost focus
        // events to be generated.
        editControl.setFocus();
    }

    /**
     * Deactivate the field editControl and restore the table cursor.
     */
    void deactivate() {
        // To avoid recursion due to lost focus events, we save the
        // current value of the editControl first.
        TableItem item = tableItem;
        tableItem = null;

        // to avoid recursion: are we still active?
        if (item == null) {
            return;
        }

        // ugly: if the input is changed during a cell editControl is committing
        // its changes, the deactivation occurs after disposing this
        // object. So, the fieldEditor may be null then.
        //        		
        if (tableControl != null && !tableControl.isDisposed()) {
            if (tableControl.getTableEditor() != null) {
                tableControl.getTableEditor().setEditor(null);
            }
            if (editControl != null && !editControl.isDisposed()) {
                // the table editControl does not call set visible if called with null
                // as argument.
                editControl.setVisible(false);
            }
            TableCursor cursor = tableControl.getTableCursor();
            if (cursor != null && !cursor.isDisposed()) {
                cursor.setVisible(true);
                cursor.setFocus();
            }
        }
    }

    /**
     * Dispose the cell editControl and its allocated resources.
     */
    public void dispose() {
        if (editControl != null) {
            editControl.dispose();
        }
    }

    /**
     * Processes a key event of the edit control. This implements the navigation control.
     * 
     * @param evt, the traverse event to process
     */
    private void processKeyEvent(TraverseEvent evt) {
        switch (evt.detail) {
            case SWT.TRAVERSE_TAB_NEXT:
                commitChanges();
                evt.doit = false;
                sendEvent(SWT.TRAVERSE_TAB_NEXT);
                break;

            case SWT.TRAVERSE_RETURN:
                commitChanges();
                evt.doit = false;
                sendEvent(SWT.TRAVERSE_RETURN);
                break;

            case SWT.TRAVERSE_ESCAPE:
                discardChanges();
                evt.doit = false;
                break;
        }
    }

    /**
     * Commit the changes and deactivate the editControl. This will automatically refresh the
     * current line of the table viewer.
     */
    private void commitChanges() {
        if (tableItem == null) {
            return;
        }

        // Catch IllegalStateExceptions that might be thrown by getValue
        try {
            tableControl.setCellValue(tableItem, externalColumnIndex, editField.getValue());
        } catch (IllegalStateException e) {
            // do nothing, this is a text format error. We simply ignore
            // the new value and discard the changes.
        }
        deactivate();
    }

    /**
     * Discard the changes and deactivate the editControl.
     */
    private void discardChanges() {
        deactivate();
    }

    /**
     * Sends a traversal event to the table cursor.
     * 
     * @param traversal, the <code>SWT.TRAVERSE_</code> value of the traversal event to trigger.
     */
    private void sendEvent(int traversal) {
        Event event = new Event();
        event.detail = traversal;
        tableControl.getTableCursor().notifyListeners(SWT.Traverse, event);
    }

    /**
     * If the editControl gets the focus, the whole text will be selected.
     * 
     * This works currently only for text controls. Other controls must be added to the method.
     */
    private void setSelection() {
        if (editControl == null) {
            return;
        }
        editField.selectAll();
    }

    /**
     * Calculate the control height for the edit control. This method is used by the table control
     * to calculate the required row height.
     * 
     * @return int, the height of the control
     */
    int getControlHeight() {
        if (editControl == null) {
            return 0;
        }
        Point size = editControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return size.y;
    }

    /**
     * Inserts a string at the given position.
     * 
     * @param c, the char to process
     */
    public void processChar(char c) {
        if (editControl == null) {
            return;
        }
        editField.insertText(new String(new char[] { c }));
    }

}
