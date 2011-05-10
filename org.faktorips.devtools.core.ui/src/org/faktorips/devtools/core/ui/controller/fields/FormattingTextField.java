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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.ArgumentCheck;

/**
 * Edit field that adheres to a configurable format. The generic type T is not the type that is
 * formatted in the text field but the type that is stored in the model!
 * 
 * @see EditField for details about generic type T
 */
public class FormattingTextField<T> extends AbstractTextField<T> {

    private final AbstractInputFormat<T> format;

    private final boolean formatOnFocusLost;

    /**
     * Creates a {@link FormattingTextField} with the given {@link Text}-Control and the given
     * format. Both arguments must not be <code>null</code>. The input would be formatted on focus
     * lost.
     * 
     * @param text the {@link Text} control to be used by this {@link FormattingTextField}
     * @param format the {@link AbstractInputFormat} to be used by this {@link FormattingTextField}
     */
    public FormattingTextField(final Text text, final AbstractInputFormat<T> format) {
        this(text, format, true);
    }

    /**
     * Creates a {@link FormattingTextField} with the given {@link Text}-Control and the given
     * format. Both arguments must not be <code>null</code>. You can specify wheater this control
     * should format the input after focus lost or not.
     * 
     * @param text the {@link Text} control to be used by this {@link FormattingTextField}
     * @param format the {@link AbstractInputFormat} to be used by this {@link FormattingTextField}
     * @param formatOnFocusLost True to format the input on focus lost
     */
    public FormattingTextField(final Text text, final AbstractInputFormat<T> format, boolean formatOnFocusLost) {
        super(text);
        ArgumentCheck.notNull(text);
        this.format = format;
        this.formatOnFocusLost = formatOnFocusLost;
        text.addVerifyListener(format);
        text.addFocusListener(new FocusListener() {

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

    @Override
    public T parseContent() {
        return getFormat().parse(getText(), supportsNull());
    }

    @Override
    public void setValue(T newValue) {
        setText(getFormat().format(newValue, supportsNull()));
    }

    public AbstractInputFormat<T> getFormat() {
        return format;
    }

    protected void formatText() {
        if (formatOnFocusLost) {
            String oldText = getText();
            String newText = format.format(getValue());
            if (!oldText.equals(newText)) {
                text.setText(newText);
            }
        }
    }
}
