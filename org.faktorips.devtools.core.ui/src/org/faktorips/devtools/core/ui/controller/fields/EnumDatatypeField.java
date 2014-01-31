/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
