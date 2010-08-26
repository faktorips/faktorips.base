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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.EnumDatatype;

/**
 * An implementation of <code>AbstractEnumDatatypeBasedField</code> that displays the values of an
 * <code>EnumDatatype</code>. If the <code>EnumDatatype</code> supports value names these will be
 * displayed instead of the value ids.
 * 
 * @author Peter Kuntz
 * 
 */
public class EnumDatatypeField extends AbstractEnumDatatypeBasedField {

    public EnumDatatypeField(Combo combo, EnumDatatype datatype) {
        super(combo, datatype);
        reInitInternal();
    }

    private EnumDatatype getEnumDatatype() {
        return (EnumDatatype)getDatatype();
    }

    @Override
    protected List<String> getDatatypeValueIds() {
        List<String> ids = Arrays.asList(getEnumDatatype().getAllValueIds(true));
        return new ArrayList<String>(ids);
    }
}
