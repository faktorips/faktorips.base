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
import org.eclipse.swt.widgets.Label;
import org.faktorips.util.ArgumentCheck;

public class LabelField extends StringValueEditField {

    private Label label;

    public LabelField(Label label) {
        super();
        ArgumentCheck.notNull(label);
        this.label = label;
    }

    @Override
    protected void addListenerToControl() {
        // no change listeners for labels neccessary
    }

    @Override
    public Control getControl() {
        return label;
    }

    /**
     * Returns the label this is a field for.
     */
    public Label getLabel() {
        return label;
    }

    @Override
    public String parseContent() {
        return super.prepareObjectForGet(label.getText());
    }

    @Override
    public void setValue(String newValue) {
        label.setText(super.prepareObjectForSet(newValue));
    }

    @Override
    public String getText() {
        return label.getText();
    }

    @Override
    public void setText(String newText) {
        label.setText(newText);
    }

    @Override
    public void insertText(String text) {
        label.setText(text);
    }

    @Override
    public void selectAll() {
        // nothing to do
    }

}
