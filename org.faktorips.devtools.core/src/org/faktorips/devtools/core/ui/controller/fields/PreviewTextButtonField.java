/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

/**
 * Field to handle text-button-controlls which are only for preview-purposes 
 * (usually the button opens the editor for the contents only previewed by
 * the textfield). 
 * 
 * @author Thorsten Guenther
 */
public class PreviewTextButtonField extends DefaultEditField {

    private TextButtonControl control;
    
    public PreviewTextButtonField(TextButtonControl control) {
        this.control = control;
    }
    
    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return control;
    }

    /**
     * {@inheritDoc}
     */
    protected void addListenerToControl() {
        // no changes - preview only
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue() {
        return control.getText();
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        // nothing to do - preview only!
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return control.getText();
    }

    /**
     * {@inheritDoc}
     */
    public void setText(String newText) {
        // nothing to do - preview only!
    }

    /**
     * {@inheritDoc}
     */
    public void insertText(String text) {
        // nothing to do - preview only!
    }

    /**
     * {@inheritDoc}
     */
    public void selectAll() {
        // nothing to do - preview only!
    }

}
