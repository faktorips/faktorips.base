/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
public class IntegerField extends DefaultEditField<Integer> {

    private Text text;

    public IntegerField(Text text) {
        super();
        ArgumentCheck.notNull(text);
        this.text = text;
    }

    @Override
    public Control getControl() {
        return text;
    }

    @Override
    public Integer parseContent() {
        String text = getText();
        if (text != null && text.length() == 0) {
            return null;
        }
        if (text == null) {
            return null;
        }
        return Integer.valueOf(text);
    }

    @Override
    public void setValue(Integer newValue) {
        ArgumentCheck.isInstanceOf(newValue, Integer.class);
        text.setText(newValue.toString());
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    public void setText(String newText) {
        text.setText(newText);
    }

    @Override
    public void insertText(String s) {
        text.insert(s);
    }

    @Override
    public void selectAll() {
        text.selectAll();
    }

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
