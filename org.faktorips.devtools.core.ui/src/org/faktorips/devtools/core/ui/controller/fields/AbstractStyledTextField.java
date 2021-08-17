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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractStyledTextField<T> extends DefaultEditField<T> {
    private StyledText styledText;
    private boolean immediatelyNotifyListener = false;

    public AbstractStyledTextField() {
        super();
    }

    public AbstractStyledTextField(StyledText styledText) {
        this();
        ArgumentCheck.notNull(styledText);
        this.styledText = styledText;
    }

    @Override
    public StyledText getControl() {
        return styledText;
    }

    @Override
    public String getText() {
        return styledText.getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            if (newText == null) {
                // AbstractNumberFormats call this method with null values
                if (supportsNullStringRepresentation()) {
                    styledText.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
                } else {
                    styledText.setText(""); //$NON-NLS-1$
                }
            } else {
                styledText.setText(newText);

            }
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String insertText) {
        styledText.insert(insertText);
    }

    @Override
    public void selectAll() {
        styledText.selectAll();
    }

    @Override
    protected void addListenerToControl() {
        final ExtendedModifyListener styledTextModifyListener = new StyledTextModifyListener();
        final ModifyListener modifyListener = $ -> notifyChangeListeners(
                new FieldValueChangedEvent(AbstractStyledTextField.this),
                immediatelyNotifyListener);
        styledText.addModifyListener(modifyListener);
        styledText.addExtendedModifyListener(styledTextModifyListener);
        styledText.addDisposeListener($ -> {
            styledText.removeModifyListener(modifyListener);
            styledText.removeExtendedModifyListener(styledTextModifyListener);
        });
    }
}
