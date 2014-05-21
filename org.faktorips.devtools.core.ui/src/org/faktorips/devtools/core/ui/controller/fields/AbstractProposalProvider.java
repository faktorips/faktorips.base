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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;

public abstract class AbstractProposalProvider implements IContentProposalProvider {

    private final IConfigElement configElement;

    private final UIDatatypeFormatter uiDatatypeFormatter;

    public AbstractProposalProvider(IConfigElement configElement, UIDatatypeFormatter uiDatatypeFormatter) {
        this.configElement = configElement;
        this.uiDatatypeFormatter = uiDatatypeFormatter;
    }

    public IConfigElement getConfigElement() {
        return configElement;
    }

    public UIDatatypeFormatter getUiDatatypeFormatter() {
        return uiDatatypeFormatter;
    }

    public ValueDatatype getDatatype() {
        try {
            return getConfigElement().findValueDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public IIpsProject getIpsProject() {
        return getConfigElement().getIpsProject();
    }

    public IValueSet getAllowedValueSet() {
        try {
            return getConfigElement().findPcTypeAttribute(getIpsProject()).getValueSet();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getFormatValue(String value) {
        return getUiDatatypeFormatter().formatValue(getDatatype(), value);
    }
}
