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

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.runtime.internal.IpsStringUtils;
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
        String content = getText();
        if ((content != null && content.length() == 0) || (content == null)) {
            return null;
        }
        return Integer.valueOf(content);
    }

    @Override
    public void setValue(Integer newValue) {
        if (newValue == null) {
            text.setText(IpsStringUtils.EMPTY);
        } else {
            ArgumentCheck.isInstanceOf(newValue, Integer.class);
            text.setText(newValue.toString());
        }
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
        text.addModifyListener($ -> notifyChangeListeners(new FieldValueChangedEvent(IntegerField.this)));
    }

}
