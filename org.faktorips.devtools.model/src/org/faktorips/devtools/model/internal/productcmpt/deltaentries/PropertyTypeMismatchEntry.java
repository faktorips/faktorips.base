/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.type.IProductCmptProperty;

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
        String label = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(property);
        return MessageFormat.format(desc, label, property.getProductCmptPropertyType().getName(),
                value.getProductCmptPropertyType().getName());
    }

    @Override
    public void fix() {
        value.delete();
        propertyValueContainer.newPropertyValues(property);
    }

}
