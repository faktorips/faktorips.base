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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;

/**
 * An edit field for Integer.
 * 
 * @author Jan Ortmann
 */
public class IntegerField extends DefaultEditField {

    private Text text;

    public IntegerField(Text text) {
        super();
        ArgumentCheck.notNull(text);
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getControl() {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object parseContent() {
        String text = getText();
        if (text != null && text.length() == 0) {
            return null;
        }
        text = (String)super.prepareObjectForGet(text);
        if (text == null) {
            return null;
        }
        return Integer.valueOf(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(Object newValue) {
        ArgumentCheck.isInstanceOf(newValue, Integer.class);
        newValue = super.prepareObjectForSet(newValue);
        text.setText(newValue.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return text.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String newText) {
        text.setText(newText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertText(String s) {
        text.insert(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAll() {
        text.selectAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(IntegerField.this));
            }

        });

    }
}
