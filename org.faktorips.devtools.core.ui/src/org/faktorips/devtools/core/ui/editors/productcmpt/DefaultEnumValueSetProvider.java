/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;

/**
 * Default implementation of {@link IEnumValueSetProvider}. Supports the case where an
 * {@link IEnumValueSet} in the model (policy component type attribute) is configured by a product
 * component (config element).
 * 
 * @author Stefan Widmaier
 */
public class DefaultEnumValueSetProvider implements IEnumValueSetProvider {

    private final IConfigElement configElement;

    public DefaultEnumValueSetProvider(IConfigElement configElement) {
        this.configElement = configElement;
    }

    /**
     * Searches the {@link IPolicyCmptTypeAttribute} (of the {@link IConfigElement}) and returns its
     * IEnumValueSet or <code>null</code> if it does not define one. {@inheritDoc}
     */
    @Override
    public IEnumValueSet getSourceEnumValueSet() {
        try {
            IPolicyCmptTypeAttribute attribute = configElement.findPcTypeAttribute(configElement.getIpsProject());
            IEnumValueSet sourceSet = (IEnumValueSet)attribute.getValueSet();
            return sourceSet;
        } catch (CoreException e) {
            // FIXME throw CoreRuntimeException (FIPS 3.6)
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the {@link IConfigElement} this provider was created with. {@inheritDoc}
     */
    @Override
    public IConfigElement getTargetConfigElement() {
        return configElement;
    }

    @Override
    public String getSourceLabel() {
        return Messages.DefaultsAndRangesEditDialog_additionalValuesDefinedInModel;
    }

    @Override
    public String getTargetLabel() {
        return Messages.DefaultsAndRangesEditDialog_valueDefinedInProductCmpt;
    }

}
