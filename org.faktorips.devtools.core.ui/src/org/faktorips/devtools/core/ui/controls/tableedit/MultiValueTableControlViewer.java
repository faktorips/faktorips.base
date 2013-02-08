/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;

/**
 * Replaces the {@link EditTableControl}.
 * 
 * @see EditTableControl
 * 
 * @since 3.7
 * 
 * @author Stefan Widmaier
 */
public class MultiValueTableControlViewer {

    private MultiValueTableControlUIBuilder uiBuilder;
    private TableViewer tableViewer;
    private MultiValueTableModel tableModel;

    /**
     * Creates a {@link MultiValueTableControlViewer} along with its UI.
     * 
     * @param parent the parent composite to which the editable table and its buttons should be
     *            added
     */
    public MultiValueTableControlViewer(Composite parent) {
        uiBuilder = new MultiValueTableControlUIBuilder();
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
    public void setTabelModel(MultiValueTableModel tabelModel) {
        this.tableModel = tabelModel;
        getTableViewer().setInput(tabelModel);
        updateButtonsEnabledState();
    }

    public MultiValueTableModel getTabelModel() {
        return tableModel;
    }

    private void createViewers() {
        tableViewer = new TableViewer(uiBuilder.getTable());
        tableViewer.setUseHashlookup(true);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonsEnabledState();
            }
        });
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
            tableModel.removeValue(indices[i]);
        }
        restoreSelection(indices[0]);
    }

    protected void restoreSelection(int index) {
        tableViewer.refresh();
        tableViewer.getControl().setFocus();
        int itemCount = tableViewer.getTable().getItemCount();
        if (itemCount > 0) {
            if (index < 0) {
                index = 0;
            }
            if (index >= itemCount) {
                index = itemCount - 1;
            }
            tableViewer.getTable().setSelection(index);
        }
        updateButtonsEnabledState();
    }

    private void addButtonClicked() {
        Object addedElement = tableModel.addValue();
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
                tableModel.swapValues(i - 1, i);
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
                tableModel.swapValues(i, i + 1);
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
