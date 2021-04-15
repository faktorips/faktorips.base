/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class ComboViewerField<T> extends ComboField<T> {

    private final ComboViewer comboViewer;

    private final Class<T> type;

    private boolean allowEmptySelection = false;

    public ComboViewerField(Combo combo, Class<T> type) {
        super(combo);
        comboViewer = new ComboViewer(combo);
        getComboViewer().setContentProvider(new ArrayContentProvider());
        getComboViewer().setLabelProvider(new DefaultLabelProvider());
        this.type = type;
    }

    @Override
    public void setValue(T newValue) {
        if (newValue == null) {
            getComboViewer().setSelection(null);
        } else {
            getComboViewer().setSelection(new StructuredSelection(newValue), true);
        }
    }

    @Override
    protected T parseContent() throws Exception {
        ISelection selection = getComboViewer().getSelection();
        if (isAllowEmptySelection() && selection.isEmpty()) {
            return null;
        }
        TypedSelection<T> typedSelection = new TypedSelection<>(type, selection);
        return typedSelection.getFirstElement();
    }

    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        comboViewer.setLabelProvider(labelProvider);
    }

    public void setInput(T[] array) {
        comboViewer.setInput(array);
    }

    public void setInput(List<T> input) {
        comboViewer.setInput(input);
    }

    public Object getInput() {
        return comboViewer.getInput();
    }

    /**
     * @return Returns the comboViewer.
     */
    ComboViewer getComboViewer() {
        return comboViewer;
    }

    /**
     * @param allowEmptySelection The allowEmptySelection to set.
     */
    public void setAllowEmptySelection(boolean allowEmptySelection) {
        this.allowEmptySelection = allowEmptySelection;
    }

    /**
     * @return Returns the allowEmptySelection.
     */
    public boolean isAllowEmptySelection() {
        return allowEmptySelection;
    }

}
