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

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.TextAndSecondControlComposite;

public abstract class AbstractTextButtonField<T> extends DefaultEditField<T> {

    private final TextAndSecondControlComposite control;

    private boolean immediatelyNotifyListener = false;

    public AbstractTextButtonField(TextAndSecondControlComposite control) {
        super();
        this.control = control;
    }

    @Override
    public TextAndSecondControlComposite getControl() {
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
        final ModifyListener ml = $ -> {
            boolean immediatelyNotify = immediatelyNotifyListener | control.isImmediatelyNotifyListener();
            notifyChangeListeners(new FieldValueChangedEvent(AbstractTextButtonField.this), immediatelyNotify);
        };

        control.getTextControl().addModifyListener(ml);
        control.getTextControl().addDisposeListener($ -> control.getTextControl().removeModifyListener(ml));
    }

}
