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

/**
 * A cell editor using the {@link Text} control to enter values in a given language. In contrast to
 * {@link InternationalStringCellEditor} this cell editor can only modify a text for one locale. The
 * {@link InternationalStringCellEditor} has additionally a button to edit the values in other
 * languages.
 * 
 * @see InternationalStringCellEditor
 */
public class LocalizedStringCellEditor extends AbstractLocalizedStringCellEditor {

    public LocalizedStringCellEditor(Text control) {
        super(control);
    }

    @Override
    public Text getControl() {
        return (Text)super.getControl();
    }

    @Override
    protected Text getTextControl() {
        return getControl();
    }

}
