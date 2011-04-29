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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.message.MessageList;

public class TextButtonField extends StringValueEditField {

    TextButtonControl control;

    protected boolean immediatelyNotifyListener = false;

    public TextButtonField(TextButtonControl control) {
        super();
        this.control = control;
    }

    @Override
    public Control getControl() {
        return control;
    }

    public TextButtonControl getTextButtonControl() {
        return control;
    }

    @Override
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(control.getTextControl(), list);
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
