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

/**
 * Textfield to represent and edit cardinality values (which means int-values and the asterisk (*)).
 * The askerisk is mapped to Integer.MAX_VALUE on object conversions and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class CardinalityField extends AbstractCardinalityField {

    private Text text;

    public CardinalityField(Text text) {
        super();
        this.text = text;
        setSupportsNullStringRepresentation(false);
    }

    @Override
    protected void addListenerToControl() {
        text.addModifyListener($ -> notifyChangeListeners(new FieldValueChangedEvent(CardinalityField.this)));
    }

    @Override
    public Control getControl() {
        return text;
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    void setTextInternal(String newText) {
        text.setText(newText);
    }

    @Override
    public void insertText(String text) {
        this.text.insert(text);
    }

    @Override
    public void selectAll() {
        text.selectAll();
    }
}
