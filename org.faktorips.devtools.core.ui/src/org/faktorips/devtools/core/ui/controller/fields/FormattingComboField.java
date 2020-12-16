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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.util.ArgumentCheck;

/**
 * Edit field that adheres to a configurable format. The generic type T is not the type that is
 * formatted in the text field but the type that is stored in the model!
 * 
 * @see EditField for details about generic type T
 */
public class FormattingComboField<T> extends AbstractComboField<T> {

    private final IInputFormat<T> format;

    /**
     * Creates a {@link FormattingComboField} with the given {@link Combo}-Control and the given
     * format. Both arguments must not be <code>null</code>. The input would be formatted on focus
     * lost.
     * 
     * @param combo the {@link Combo} control to be used by this {@link FormattingComboField}
     * @param format the {@link AbstractInputFormat} to be used by this {@link FormattingComboField}
     */
    public FormattingComboField(final Combo combo, final IInputFormat<T> format) {
        this(combo, format, true);
    }

    /**
     * Creates a {@link FormattingComboField} with the given {@link Combo}-Control and the given
     * format. Both arguments must not be <code>null</code>. You can specify whether this control
     * should format the input after focus lost or not.
     * 
     * @param combo the {@link Combo} control to be used by this {@link FormattingComboField}
     * @param format the {@link AbstractInputFormat} to be used by this {@link FormattingComboField}
     * @param formatOnFocusLost True to format the input on focus lost
     */
    public FormattingComboField(Combo combo, IInputFormat<T> format, boolean formatOnFocusLost) {
        super(combo);
        ArgumentCheck.notNull(combo);
        this.format = format;
        if (format instanceof VerifyListener) {
            combo.addVerifyListener((VerifyListener)format);
        }
        if (formatOnFocusLost) {
            combo.addFocusListener(new FocusListener() {

                @Override
                public void focusLost(FocusEvent e) {
                    formatText();
                }

                @Override
                public void focusGained(FocusEvent e) {
                    // do nothing
                }

            });
        }
    }

    @Override
    public T parseContent() {
        return getFormat().parse(getText(), supportsNullStringRepresentation());
    }

    @Override
    public void setValue(T newValue) {
        setText(getFormat().format(newValue, supportsNullStringRepresentation()));
    }

    public IInputFormat<T> getFormat() {
        return format;
    }

    protected void formatText() {
        String oldText = getText();
        String newText = format.format(getValue());
        if (!oldText.equals(newText)) {
            getComboControl().setText(newText);
        }
    }
}
