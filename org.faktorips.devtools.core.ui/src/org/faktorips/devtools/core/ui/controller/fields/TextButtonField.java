/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.TextAndSecondControlComposite;

public class TextButtonField extends StringValueEditField {

    private TextAndSecondControlComposite control;

    protected boolean immediatelyNotifyListener = false;

    public TextButtonField(TextAndSecondControlComposite control) {
        super();
        this.control = control;
    }

    @Override
    public Control getControl() {
        return control;
    }

    public TextAndSecondControlComposite getTextButtonControl() {
        return control;
    }

    @Override
    protected Control getControlForDecoration() {
        return control.getTextControl();
    }

    @Override
    public String parseContent() {
        return super.prepareObjectForGet(control.getTextControl().getText());
    }

    @Override
    public void setValue(String newValue) {
        setText(super.prepareObjectForSet(newValue));
    }

    @Override
    public String getText() {
        return control.getTextControl().getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            control.getTextControl().setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String text) {
        control.getTextControl().insert(text);
    }

    @Override
    public void selectAll() {
        control.getTextControl().selectAll();
    }

    @Override
    protected void addListenerToControl() {
        ModifyListener ml = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                boolean immediatelyNotify = immediatelyNotifyListener | control.isImmediatelyNotifyListener();
                notifyChangeListeners(new FieldValueChangedEvent(TextButtonField.this), immediatelyNotify);
            }

        };

        control.getTextControl().addModifyListener(ml);
        control.getTextControl().addDisposeListener(new MyDisposeListener(ml));
    }

    /**
     * Dispose listener to remove modify listener from control when control is disposed.
     * 
     * @author Thorsten Guenther
     */
    private class MyDisposeListener implements DisposeListener {

        /**
         * Listener which has to be removed on dispose.
         */
        private ModifyListener ml;

        /**
         * Create a new Listener.
         * 
         * @param ml The modify listener to remove on dispose
         */
        MyDisposeListener(ModifyListener ml) {
            this.ml = ml;
        }

        @Override
        public void widgetDisposed(DisposeEvent e) {
            control.getTextControl().removeModifyListener(ml);
        }

    }

}
