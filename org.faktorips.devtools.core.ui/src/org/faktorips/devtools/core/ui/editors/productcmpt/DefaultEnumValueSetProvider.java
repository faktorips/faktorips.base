/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;

/**
 * Default implementation of {@link IEnumValueSetProvider}. Supports the case where an
 * {@link IEnumValueSet} in the model (policy component type attribute) is configured by a product
 * component (config element).
 * 
 * @author Stefan Widmaier
 */
public class DefaultEnumValueSetProvider implements IEnumValueSetProvider {

    private final IConfiguredValueSet configuredValueSet;

    public DefaultEnumValueSetProvider(IConfiguredValueSet configuredValueSet) {
        this.configuredValueSet = configuredValueSet;
    }

    /**
     * Searches the {@link IPolicyCmptTypeAttribute} (of the {@link IConfigElement}) and returns its
     * IEnumValueSet or <code>null</code> if it does not define one. {@inheritDoc}
     */
    @Override
    public IEnumValueSet getSourceEnumValueSet() {
        IPolicyCmptTypeAttribute attribute = configuredValueSet.findPcTypeAttribute(configuredValueSet
                .getIpsProject());
        return (IEnumValueSet)attribute.getValueSet();
    }

    /**
     * Returns the {@link IConfigElement} this provider was created with.
     */
    @Override
    public IConfiguredValueSet getTargetConfiguredValueSet() {
        return configuredValueSet;
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
