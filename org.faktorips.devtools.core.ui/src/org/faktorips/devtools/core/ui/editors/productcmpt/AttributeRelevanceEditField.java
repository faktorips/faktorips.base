/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.util.SelectionListeners;

/**
 * EditField for {@link AttributeRelevance}.
 */
public class AttributeRelevanceEditField extends DefaultEditField<AttributeRelevance> {

    private AttributeRelevanceControl attributeRelevanceControl;

    public AttributeRelevanceEditField(AttributeRelevanceControl attributeRelevanceControl) {
        this.attributeRelevanceControl = attributeRelevanceControl;
    }

    @Override
    public Control getControl() {
        return attributeRelevanceControl;
    }

    @Override
    public void setValue(AttributeRelevance attributeRelevance) {
        attributeRelevanceControl.getRadioButtonGroup().setSelection(attributeRelevance);
    }

    @Override
    public String getText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setText(String newText) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertText(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected AttributeRelevance parseContent() throws Exception {
        return attributeRelevanceControl.getRadioButtonGroup().getSelectedOption();
    }

    @Override
    protected void addListenerToControl() {
        attributeRelevanceControl.getRadioButtonGroup().getRadioButtons()
                .forEach(b -> b.addSelectionListener(SelectionListeners
                        .widgetSelected($ -> notifyChangeListeners(
                                new FieldValueChangedEvent(AttributeRelevanceEditField.this)))));
    }

}
