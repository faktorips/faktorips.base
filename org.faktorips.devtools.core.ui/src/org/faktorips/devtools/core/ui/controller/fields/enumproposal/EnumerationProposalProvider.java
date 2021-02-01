/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * An implementation of {@link AbstractProposalProvider} for EnumerationFields. It provides
 * proposals for {@link EnumDatatype}s or {@link EnumValueSet}s.
 */
public class EnumerationProposalProvider extends AbstractProposalProvider {

    public EnumerationProposalProvider(ValueDatatype valueDatatype, IValueSetOwner owner,
            IInputFormat<String> inputFormat) {
        super(owner, valueDatatype, inputFormat);
    }

    @Override
    protected IValueSource createValueSource(IValueSetOwner valueSetOwner, ValueDatatype datatype) {
        EnumValueSource enumValueSource = new EnumValueSource(valueSetOwner, datatype);
        return new EnsureContainsNullForConfigElementValueSource(valueSetOwner, datatype, enumValueSource);
    }
}
