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

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * Field to handle a {@link TextButtonControl} for preview-purposes only (usually the button opens
 * the editor for the contents to be previewed by the {@link Text} control).
 * 
 * @author Thorsten Guenther
 * @author Cornelius Dirmeier
 * @author Alexander Weickmann
 */
public class PreviewTextButtonField extends DefaultEditField<IValueSet> {

    private final TextButtonControl control;

    private IValueSet currentValue;

    public PreviewTextButtonField(TextButtonControl control) {
        this.control = control;
    }

    @Override
    public Control getControl() {
        return control;
    }

    @Override
    protected void addListenerToControl() {
        // nothing to do - preview only!
    }

    @Override
    public IValueSet parseContent() {
        if (currentValue != null
                && getText().equals(IpsUIPlugin.getDefault().getDatatypeFormatter().formatValueSet(currentValue))) {
            /*
             * The text in the control equals the text representation of the current value. To avoid
             * changes, return the current value.
             */
            return currentValue;
        } else {
            /*
             * The value has changed or the current value is null. Return null to indicate that the
             * value needs to be updated.
             */
            return null;
        }
    }

    @Override
    public void setValue(IValueSet newValue) {
        currentValue = newValue;
        control.setText((IpsUIPlugin.getDefault().getDatatypeFormatter().formatValueSet(newValue)));
    }

    @Override
    public String getText() {
        return control.getText();
    }

    @Override
    public void setText(String newText) {
        // nothing to do - preview only!
    }

    @Override
    public void insertText(String text) {
        // nothing to do - preview only!
    }

    @Override
    public void selectAll() {
        // nothing to do - preview only!
    }

}
