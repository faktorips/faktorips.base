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
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract Field that handles a {@link Text} component.
 * 
 * @see EditField for details about generic type T
 * 
 * @author dirmeier
 */
public abstract class AbstractTextField<T> extends DefaultEditField<T> {

    private Text text;
    private boolean immediatelyNotifyListener = false;

    public AbstractTextField() {
        super();
    }

    public AbstractTextField(Text text) {
        this();
        ArgumentCheck.notNull(text);
        this.text = text;
    }

    @Override
    public Control getControl() {
        return text;
    }

    /**
     * Returns the text control this is an edit field for.
     */
    public Text getTextControl() {
        return text;
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            if (newText == null) {
                // AbstractNumberFormats call this method with null values
                if (supportsNullStringRepresentation()) {
                    newText = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                } else {
                    newText = ""; //$NON-NLS-1$
                }
            }
            text.setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String insertText) {
        text.insert(insertText);
    }

    @Override
    public void selectAll() {
        text.selectAll();
    }

    @Override
    protected void addListenerToControl() {
        final ModifyListener modifyListener = $ -> notifyChangeListeners(
                new FieldValueChangedEvent(AbstractTextField.this), immediatelyNotifyListener);
        text.addModifyListener(modifyListener);
        text.addDisposeListener($ -> text.removeModifyListener(modifyListener));
    }

}
