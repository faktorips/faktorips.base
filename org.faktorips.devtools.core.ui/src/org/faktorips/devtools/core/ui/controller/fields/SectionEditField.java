/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

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
