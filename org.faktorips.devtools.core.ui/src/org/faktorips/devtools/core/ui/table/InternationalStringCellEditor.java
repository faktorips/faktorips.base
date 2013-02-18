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

import java.util.Locale;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controls.InternationalStringControl;

/**
 * A cell editor using the {@link InternationalStringControl} to enter values in different
 * languages.
 */
public class InternationalStringCellEditor extends AbstractLocalizedStringCellEditor {

    public InternationalStringCellEditor(Locale locale, InternationalStringControl control) {
        super(locale, control);
    }

    @Override
    public InternationalStringControl getControl() {
        return (InternationalStringControl)super.getControl();
    }

    @Override
    protected Text getTextControl() {
        return getControl().getTextControl();
    }

}
