/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
