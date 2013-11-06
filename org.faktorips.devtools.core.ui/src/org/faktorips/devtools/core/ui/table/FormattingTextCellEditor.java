/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.ui.inputFormat.AbstractInputFormat;
import org.faktorips.util.ArgumentCheck;

/**
 * IpsCellEditor that adheres to a configurable {@link AbstractInputFormat}.
 * <p>
 * The generic Type T is the type that is stored in the model. In most cases we simply store String
 * objects in the model, in these cases use String for the type T. The type is provided to the
 * {@link AbstractInputFormat}.
 * 
 * 
 * 
 * @author Stefan Widmaier
 */
public class FormattingTextCellEditor<T> extends TextCellEditor {

    private AbstractInputFormat<T> format;

    /**
     * Creates a {@link FormattingTextCellEditor} with the given {@link Text}-Control and the given
     * format. Both arguments must not be <code>null</code>.
     * 
     * @param text the {@link Text} control to be used by this {@link FormattingTextCellEditor}
     * @param format the {@link AbstractInputFormat} to be used by this
     *            {@link FormattingTextCellEditor}
     */
    public FormattingTextCellEditor(Text text, AbstractInputFormat<T> format) {
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
        @SuppressWarnings("unchecked")
        // the object value is provided by the framework and we cannot really check it
        T castedValue = (T)value;
        setText(format.format(castedValue));
    }

}
