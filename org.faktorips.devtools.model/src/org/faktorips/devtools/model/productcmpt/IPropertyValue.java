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

import org.faktorips.devtools.model.exception.CoreRuntimeException;
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
    public static final String MSGCODE_PREFIX = "PROPERTYVALUE-"; //$NON-NLS-1$

    /**
     * Returns the name of the product definition property, this is a value of.
     * 
     * @see IProductCmptProperty
     */
    public String getPropertyName();

    /**
     * Returns the property this object provides a value for. Returns <code>null</code> if the
     * property can't be found.
     * 
     * @param ipsProject The IPS project which search path is used.
     * 
     * @throws CoreRuntimeException if an error occurs
     */
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the type of the property value.
     * 
     * See {@link PropertyValueType} for safe casts to a specific model element.
     * 
     * @see #getProductCmptPropertyType()
     */
    public PropertyValueType getPropertyValueType();

    /**
     * Returns the type of the product definition property.
     * 
     * @see #getPropertyValueType()
     */
    public ProductCmptPropertyType getProductCmptPropertyType();

    /**
     * Returns the value.
     */
    public Object getPropertyValue();

    /**
     * Returns the {@link IPropertyValueContainer} this property value belongs to.
     * 
     * @return The container this value belongs to
     */
    public IPropertyValueContainer getPropertyValueContainer();

    @Override
    public IPropertyValue findTemplateProperty(IIpsProject ipsProject);

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
            final int prime = 31;
            int result = 1;
            result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PropertyValueIdentifier other = (PropertyValueIdentifier)obj;
            if (!Objects.equals(propertyName, other.propertyName)) {
                return false;
            }
            return type == other.type;
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
