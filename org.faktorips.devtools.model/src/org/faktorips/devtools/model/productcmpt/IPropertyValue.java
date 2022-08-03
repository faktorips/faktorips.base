/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.Objects;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.util.ArgumentCheck;

/**
 * Base interface for properties stored in product component generations like formulas, table
 * content usages, product component attributes and configuration elements.
 * 
 * @author Jan Ortmann
 */
public interface IPropertyValue extends ITemplatedValue {

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "PROPERTYVALUE-"; //$NON-NLS-1$

    /**
     * Returns the name of the product definition property, this is a value of.
     * 
     * @see IProductCmptProperty
     */
    String getPropertyName();

    /**
     * Returns the property this object provides a value for. Returns <code>null</code> if the
     * property can't be found.
     * 
     * @param ipsProject The IPS project which search path is used.
     * 
     * @throws IpsException if an error occurs
     */
    IProductCmptProperty findProperty(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the type of the property value.
     * 
     * See {@link PropertyValueType} for safe casts to a specific model element.
     * 
     * @see #getProductCmptPropertyType()
     */
    PropertyValueType getPropertyValueType();

    /**
     * Returns the type of the product definition property.
     * 
     * @see #getPropertyValueType()
     */
    ProductCmptPropertyType getProductCmptPropertyType();

    /**
     * Returns the value.
     */
    Object getPropertyValue();

    /**
     * Returns the {@link IPropertyValueContainer} this property value belongs to.
     * 
     * @return The container this value belongs to
     */
    IPropertyValueContainer getPropertyValueContainer();

    @Override
    IPropertyValue findTemplateProperty(IIpsProject ipsProject);

    /** A identifier that identifies property values by means of their property's name. */
    static class PropertyValueIdentifier implements ITemplatedValueIdentifier {

        private final String propertyName;

        private final PropertyValueType type;

        public PropertyValueIdentifier(String propertyName, PropertyValueType type) {
            super();
            this.propertyName = Objects.requireNonNull(propertyName);
            this.type = type;
        }

        public PropertyValueIdentifier(IPropertyValue p) {
            this(p.getPropertyName(), p.getPropertyValueType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyName, type);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            PropertyValueIdentifier other = (PropertyValueIdentifier)obj;
            return Objects.equals(propertyName, other.propertyName)
                    && type == other.type;
        }

        @Override
        public IPropertyValue getValueFrom(ITemplatedValueContainer container) {
            ArgumentCheck.isInstanceOf(container, IPropertyValueContainer.class);
            IPropertyValueContainer propertyValueContainer = (IPropertyValueContainer)container;
            return propertyValueContainer.getPropertyValue(propertyName, type.getInterfaceClass());
        }

        public String getPropertyName() {
            return propertyName;
        }

        public PropertyValueType getType() {
            return type;
        }

    }

}
