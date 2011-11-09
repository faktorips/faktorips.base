/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

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
        if (currentValue != null && getText().equals(currentValue.toShortString())) {
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
        control.setText(newValue.toShortString());
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
