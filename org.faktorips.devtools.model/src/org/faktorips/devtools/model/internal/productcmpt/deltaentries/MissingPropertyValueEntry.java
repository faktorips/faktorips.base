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
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * 
 * @author Jan Ortmann
 */
public class MissingPropertyValueEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptProperty property;

    private final IPropertyValueContainer propertyValueContainer;

    private final PropertyValueType type;

    private ValueWithoutPropertyEntry predecessor;

    public MissingPropertyValueEntry(IPropertyValueContainer propertyValueContainer, IProductCmptProperty property,
            PropertyValueType type) {
        super(null);
        this.propertyValueContainer = propertyValueContainer;
        this.property = property;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation receives the type from the property because there is no property value.
     */
    @Override
    public PropertyValueType getPropertyType() {
        return type;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation receives the name from the property because there is no property value.
     */
    @Override
    public String getPropertyName() {
        return property.getPropertyName();
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder(
                IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(property));
        description.append(" ("); //$NON-NLS-1$
        description.append(getLocalizedLabel(getPropertyType()));
        description.append(')');
        if (hasPredecessorValue()) {
            IPropertyValueContainer predecessorContainer = getPredecessor().getPropertyValue()
                    .getPropertyValueContainer();
            String name = predecessorContainer.getName();
            if (predecessorContainer instanceof IProductCmptGeneration) {
                name = IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention()
                        .getGenerationConceptNameSingular() + ' ' + name;
            }
            description
                    .append(MessageFormat.format(Messages.MissingPropertyValueEntry_valueTransferedInformation, name));
        }
        return description.toString();
    }

    private String getLocalizedLabel(PropertyValueType propertyType) {
        switch (propertyType) {
            case ATTRIBUTE_VALUE:
                return Messages.MissingPropertyValueEntry_ATTRIBUTE_VALUE;
            case CONFIGURED_DEFAULT:
                return Messages.MissingPropertyValueEntry_CONFIGURED_DEFAULT;
            case CONFIGURED_VALUESET:
                return Messages.MissingPropertyValueEntry_CONFIGURED_VALUESET;
            case FORMULA:
                return Messages.MissingPropertyValueEntry_FORMULA;
            case TABLE_CONTENT_USAGE:
                return Messages.MissingPropertyValueEntry_TABLE_CONTENT_USAGE;
            case VALIDATION_RULE_CONFIG:
                return Messages.MissingPropertyValueEntry_VALIDATION_RULE_CONFIG;

            default:
                throw new IllegalStateException("Unknown property type: " + propertyType); //$NON-NLS-1$
        }
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_PROPERTY_VALUE;
    }

    /**
     * @param predecessor The predecessor to set.
     */
    public void setPredecessor(ValueWithoutPropertyEntry predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * @return Returns the predecessor.
     */
    public AbstractDeltaEntryForProperty getPredecessor() {
        return predecessor;
    }

    @Override
    public void fix() {
        IPropertyValue newPropertyValue = propertyValueContainer.newPropertyValue(property, type.getInterfaceClass());
        if (hasPredecessorValue()) {
            // if there was a predecessor value we copy the whole value
            IPropertyValue predecessorValue = getPredecessor().getPropertyValue();
            newPropertyValue.copyFrom(predecessorValue);
        }
    }

    private boolean hasPredecessorValue() {
        return getPredecessor() != null && getPredecessor().getPropertyValue() != null;
    }

}
