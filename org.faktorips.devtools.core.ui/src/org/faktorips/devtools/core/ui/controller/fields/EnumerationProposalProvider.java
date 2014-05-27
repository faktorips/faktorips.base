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
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;

/**
 * An implementation of {@link AbstractProposalProvider}. It provides proposals for
 * {@link EnumDatatype}s or {@link EnumValueSet}s.
 */
public class EnumerationProposalProvider extends AbstractProposalProvider {

    public EnumerationProposalProvider(ValueDatatype valueDatatype, IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        super(owner, valueDatatype, inputFormat);
    }

    @Override
    protected List<String> getAllowedValuesAsList() {
        IValueSource valueSource = new EnumValueSetValueSource(getValueSetOwner());
        if (!valueSource.isApplicable() && getValueDatatype().isEnum()) {
            return new EnumDatatypeValueSource(getValueDatatype()).getValues();
        }
        if (valueSource.isApplicable()) {
            return valueSource.getValues();
        }
        return new ArrayList<String>();
    }
}
