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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * {@link EditField} that can be used to bind the title of a {@link Section} to a model object.
 * 
 * @author Alexander Weickmann
 * 
 * @see BindingContext
 */
public class SectionEditField extends StringValueEditField {

    private final Section section;

    public SectionEditField(Section section) {
        this.section = section;
    }

    @Override
    protected int getMessageDecorationPosition() {
        return SWT.LEFT | SWT.TOP;
    }

    @Override
    public Control getControl() {
        return section;
    }

    @Override
    protected String parseContent() throws Exception {
        return super.prepareObjectForGet(section.getText());
    }

    @Override
    public void setValue(String newValue) {
        section.setText(super.prepareObjectForSet(newValue));
    }

    @Override
    public void setText(String newText) {
        section.setText(newText);
    }

    @Override
    public String getText() {
        return section.getText();
    }

    @Override
    public void insertText(String text) {
        section.setText(text);
    }

    @Override
    public void selectAll() {
        // The section title text cannot be selected
    }

    @Override
    protected void addListenerToControl() {
        // No listeners are required
    }

}
