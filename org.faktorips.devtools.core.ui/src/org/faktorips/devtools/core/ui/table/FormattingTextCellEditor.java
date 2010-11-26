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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.fields.InputFormat;
import org.faktorips.util.ArgumentCheck;

/**
 * IpsCellEditor that adheres to a configurable {@link InputFormat}.
 * 
 * @author Stefan Widmaier
 */
public class FormattingTextCellEditor extends TextCellEditor {

    private InputFormat format;

    /**
     * Creates a {@link FormattingTextCellEditor} with the given {@link Text}-Control and the given
     * format. Both arguments must not be <code>null</code>.
     * 
     * @param text the {@link Text} control to be used by this {@link FormattingTextCellEditor}
     * @param format the {@link InputFormat} to be used by this {@link FormattingTextCellEditor}
     */
    public FormattingTextCellEditor(Text text, InputFormat format) {
        super(text);
        ArgumentCheck.notNull(text);
        this.format = format;
        text.addVerifyListener(format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        return format.parse(getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(Object value) {
        setText(format.format(value));
    }

}
