/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.controls.EditTableControl;

/**
 * Replaces the {@link EditTableControl}.
 *
 * @see EditTableControl
 *
 * @since 3.7
 *
 * @author Stefan Widmaier
 */
public class EditTableControlViewer<T> {

    private EditTableControlUIBuilder uiBuilder;
    private TableViewer tableViewer;
    private IEditTableModel<T> tableModel;

    /**
     * Creates a {@link EditTableControlViewer} along with its UI.
     *
     * @param parent the parent composite to which the editable table and its buttons should be
     *            added
     */
    public EditTableControlViewer(Composite parent) {
        uiBuilder = new EditTableControlUIBuilder();
        createUI(parent);
    }

    private void createUI(Composite parent) {
        uiBuilder.createTableEditControl(parent);
        createViewers();
        addListeners();
        updateButtonsEnabledState();
    }

    private void addListeners() {
        ButtonListener buttonListener = new ButtonListener();
        uiBuilder.getAddButton().addSelectionListener(buttonListener);
        uiBuilder.getRemoveButton().addSelectionListener(buttonListener);
        uiBuilder.getUpButton().addSelectionListener(buttonListener);
        uiBuilder.getDownButton().addSelectionListener(buttonListener);
    }

    /**
     * Sets the model this viewer uses. The given table model will be used as input for the table
     * viewer.
     *
     * @param tabelModel the model containing the data to be displayed
     */
    public void setTabelModel(IEditTableModel<T> tabelModel) {
        tableModel = tabelModel;
        getTableViewer().setInput(tabelModel);
        updateButtonsEnabledState();
    }

    public IEditTableModel<T> getTabelModel() {
        return tableModel;
    }

    private void createViewers() {
        tableViewer = new TableViewer(uiBuilder.getTable());
        tableViewer.setUseHashlookup(true);
        tableViewer.addSelectionChangedListener($ -> updateButtonsEnabledState());
    }

    private void updateButtonsEnabledState() {
        uiBuilder.getAddButton().setEnabled(true);
        uiBuilder.getRemoveButton().setEnabled(!tableViewer.getSelection().isEmpty());
        uiBuilder.getUpButton().setEnabled(!tableViewer.getSelection().isEmpty());
        uiBuilder.getDownButton().setEnabled(!tableViewer.getSelection().isEmpty());
    }

    public void setTableDescription(String tableDescription) {
        uiBuilder.setTableDescription(tableDescription);
    }

    /**
     * Returns the {@link TableViewer} for this viewer. Clients may add {@link EditingSupport} and
     * columns to the viewer.
     *
     */
    public TableViewer getTableViewer() {
        return tableViewer;
    }

    public void setLabelProvider(ILabelProvider provider) {
        getTableViewer().setLabelProvider(provider);
    }

    public void setContentProvider(IStructuredContentProvider contentProvider) {
        getTableViewer().setContentProvider(contentProvider);
    }

    private void removeButtonClicked() {
        int[] indices = uiBuilder.getTable().getSelectionIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
            tableModel.removeElement(indices[i]);
        }
        restoreSelection(indices[0]);
    }

    protected void restoreSelection(int index) {
        tableViewer.refresh();
        tableViewer.getControl().setFocus();
        int itemCount = tableViewer.getTable().getItemCount();
        int actualIndex = index;
        if (itemCount > 0) {
            if (actualIndex < 0) {
                actualIndex = 0;
            }
            if (actualIndex >= itemCount) {
                actualIndex = itemCount - 1;
            }
            tableViewer.getTable().setSelection(actualIndex);
        }
        updateButtonsEnabledState();
    }

    private void addButtonClicked() {
        Object addedElement = tableModel.addElement();
        tableViewer.refresh();
        tableViewer.getControl().setFocus();
        int row = tableViewer.getTable().getItemCount() - 1;
        uiBuilder.getTable().setSelection(row);
        updateButtonsEnabledState();
        tableViewer.editElement(addedElement, 1);
    }

    private void upButtonClicked() {
        move(true);
    }

    private void downButtonClicked() {
        move(false);
    }

    private void move(boolean up) {
        if (tableViewer.getTable().getSelectionCount() == 0) {
            return;
        }
        int[] newSelection;
        if (up) {
            newSelection = moveUp(tableViewer.getTable().getSelectionIndices());
        } else {
            newSelection = moveDown(tableViewer.getTable().getSelectionIndices());
        }
        restoreSelection(newSelection[0]);
        tableViewer.refresh();
        tableViewer.getControl().setFocus();
    }

    private int[] moveUp(int[] indices) {
        if (contains(indices, 0)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = 1; i < uiBuilder.getTable().getItemCount(); i++) {
            if (contains(indices, i)) {
                tableModel.swapElements(i - 1, i);
                newSelection[j] = i - 1;
                j++;
            }
        }
        return newSelection;
    }

    private int[] moveDown(int[] indices) {
        if (contains(indices, uiBuilder.getTable().getItemCount() - 1)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = uiBuilder.getTable().getItemCount() - 2; i >= 0; i--) {
            if (contains(indices, i)) {
                tableModel.swapElements(i, i + 1);
                newSelection[j++] = i + 1;
            }
        }
        return newSelection;

    }

    private boolean contains(int[] indices, int index) {
        for (int indice : indices) {
            if (indice == index) {
                return true;
            }
        }
        return false;
    }

    private class ButtonListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == uiBuilder.getAddButton()) {
                addButtonClicked();
            } else if (e.widget == uiBuilder.getRemoveButton()) {
                removeButtonClicked();
            } else if (e.widget == uiBuilder.getUpButton()) {
                upButtonClicked();
            } else if (e.widget == uiBuilder.getDownButton()) {
                downButtonClicked();
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // ignore
        }
    }
}
