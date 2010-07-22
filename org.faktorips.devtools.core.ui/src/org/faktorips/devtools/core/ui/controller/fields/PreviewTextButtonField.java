/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

/**
 * Field to handle text-button-controlls which are only for preview-purposes (usually the button
 * opens the editor for the contents only previewed by the textfield).
 * 
 * @author Thorsten Guenther
 */
public class PreviewTextButtonField extends DefaultEditField {

    private TextButtonControl control;

    public PreviewTextButtonField(TextButtonControl control) {
        this.control = control;
    }

    @Override
    public Control getControl() {
        return control;
    }

    @Override
    protected void addListenerToControl() {
        // no changes - preview only
    }

    @Override
    public Object parseContent() {
        return control.getText();
    }

    @Override
    public void setValue(Object newValue) {
        // nothing to do - preview only!
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
