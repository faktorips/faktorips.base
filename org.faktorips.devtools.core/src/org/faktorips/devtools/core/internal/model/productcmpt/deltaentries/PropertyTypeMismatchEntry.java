/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * 
 * @author Jan Ortmann
 */
public class PropertyTypeMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptProperty property;
    private final IPropertyValue value;
    private final IPropertyValueContainer propertyValueContainer;

    public PropertyTypeMismatchEntry(IPropertyValueContainer poIPropertyValueContainer, IProductCmptProperty property,
            IPropertyValue value) {
        super(value);
        this.propertyValueContainer = poIPropertyValueContainer;
        this.property = property;
        this.value = value;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.PROPERTY_TYPE_MISMATCH;
    }

    @Override
    public String getDescription() {
        String desc = Messages.PropertyTypeMismatchEntry_desc;
        String label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(property);
        return NLS.bind(desc, new Object[] { label, property.getProductCmptPropertyType().getName(),
                value.getPropertyType().getName() });
    }

    @Override
    public void fix() {
        value.delete();
        propertyValueContainer.newPropertyValue(property);
    }

}
