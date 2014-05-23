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

import org.eclipse.jface.fieldassist.IContentProposalProvider;
/**
 * An <code>IContentProposalProvider</code> for EnumerationFields.
 */
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;

public abstract class AbstractEnumerationProposalProvider implements IContentProposalProvider {

    private final UIDatatypeFormatter uiDatatypeFormatter;
    private ValueDatatype valueDatatype;

    public AbstractEnumerationProposalProvider(ValueDatatype valueDatatype, UIDatatypeFormatter uiDatatypeFormatter) {
        this.valueDatatype = valueDatatype;
        this.uiDatatypeFormatter = uiDatatypeFormatter;
    }

    public ValueDatatype getValueDatatype() {
        return valueDatatype;
    }

    public UIDatatypeFormatter getUiDatatypeFormatter() {
        return uiDatatypeFormatter;
    }

    public String getFormatValue(String value) {
        return getUiDatatypeFormatter().formatValue(valueDatatype, value);
    }
}
