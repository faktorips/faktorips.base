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

import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.StyledTextAndSecondControlComposite;

public abstract class AbstractStyledTextButtonField<T> extends DefaultEditField<T> {
    private final StyledTextAndSecondControlComposite control;

    private boolean immediatelyNotifyListener = false;

    public AbstractStyledTextButtonField(StyledTextAndSecondControlComposite control) {
        super();
        this.control = control;
    }

    @Override
    public StyledTextAndSecondControlComposite getControl() {
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
        final ExtendedModifyListener styledTextModifyListener = new StyledTextModifyListener();
        final ModifyListener ml = $ -> {
            boolean immediatelyNotify = immediatelyNotifyListener | control.isImmediatelyNotifyListener();
            notifyChangeListeners(new FieldValueChangedEvent(AbstractStyledTextButtonField.this),
                    immediatelyNotify);
        };

        control.getTextControl().addModifyListener(ml);
        control.getTextControl().addExtendedModifyListener(styledTextModifyListener);
        control.getTextControl().addDisposeListener($ -> {
            control.getTextControl().removeModifyListener(ml);
            control.getTextControl().removeExtendedModifyListener(styledTextModifyListener);
        });
    }

}
