/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
import org.eclipse.swt.widgets.Label;
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class LabelField extends DefaultEditField {

    private Label label;

    public LabelField(Label label) {
        super();
        ArgumentCheck.notNull(label);
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addListenerToControl() {
        // no change listeners for labels neccessary
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object parseContent() {
        return super.prepareObjectForGet(label.getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(Object newValue) {
        label.setText((String)super.prepareObjectForSet(newValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return label.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String newText) {
        label.setText(newText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertText(String text) {
        label.setText(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAll() {
        // nothing to do
    }

}
