/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.inputformat.ValueSetFormat;

public class UIDatatypeFormatter {

    /**
     * Formats the given value according to the user preferences.
     * <p>
     * Note that this method delegates some calls to {@link DatatypeFormatter} in the core plugin.
     * Eventually the {@link DatatypeFormatter}-code should be moved to this class. See MTS#530
     * <p>
     * Supports the null-presentation mechanism.
     * 
     * @param datatype The data type the value is a value of.
     * @param value The value as string
     */
    public String formatValue(ValueDatatype datatype, String value) {
        return IpsUIPlugin.getDefault().getInputFormat(datatype).format(value);
    }

    /**
     * @see ValueSetFormat
     */
    public String formatValueSet(IValueSet valueSet) {
        return ValueSetFormat.newInstance(valueSet.getValueSetOwner()).format(valueSet);
    }

}
