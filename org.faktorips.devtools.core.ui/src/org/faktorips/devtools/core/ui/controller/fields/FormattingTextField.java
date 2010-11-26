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

import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;

/**
 * Edit field that adheres to a configurable format.
 */
public class FormattingTextField extends TextField {

    private InputFormat format;

    /**
     * Creates a {@link FormattingTextField} with the given {@link Text}-Control and the given
     * format. Both arguments must not be <code>null</code>.
     * 
     * @param text the {@link Text} control to be used by this {@link FormattingTextField}
     * @param format the {@link InputFormat} to be used by this {@link FormattingTextField}
     */
    public FormattingTextField(Text text, InputFormat format) {
        super(text);
        ArgumentCheck.notNull(text);
        this.format = format;
        text.addVerifyListener(format);
    }

    @Override
    public Object parseContent() {
        return format.parse(getText(), supportsNull());
    }

    @Override
    public void setValue(Object newValue) {
        setText(format.format(newValue, supportsNull()));
    }

}
