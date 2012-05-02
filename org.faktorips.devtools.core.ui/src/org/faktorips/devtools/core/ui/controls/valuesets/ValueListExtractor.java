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

package org.faktorips.devtools.core.ui.controls.valuesets;

import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;

/**
 * Helper class to extract values (or value IDs respectively) from an {@link IEnumValueSet} or
 * {@link EnumDatatype}.
 * 
 * @author Stefan Widmaier
 */
public class ValueListExtractor {

    public static List<String> extractValues(EnumDatatype valueDatatype) {
        return Arrays.asList(valueDatatype.getAllValueIds(true));
    }

    public static List<String> extractValues(IEnumValueSet valueSet) {
        return valueSet.getValuesAsList();
    }

}
