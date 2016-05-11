/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
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
     * @throws CoreException if an error occurs
     */
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException;

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
     * <p>
     * See {@link ProductCmptPropertyType} for safe casts to a specific model element.
     * 
     * @see ProductCmptPropertyType
     * @see #getPropertyValueType()
     * 
     * @deprecated since 3.16 there is a difference between {@link PropertyValueType} and
     *             {@link ProductCmptPropertyType}. Use {@link #getPropertyValueType()} or
     *             {@link #getProductCmptPropertyType()} which is a direct reference to
     *             {@link PropertyValueType#getCorrespondingPropertyType()}.
     */
    @Deprecated
    public ProductCmptPropertyType getPropertyType();

    /**
     * Returns the type of the product definition property.
     * 
     * @see #getPropertyValueType()
     */
    public ProductCmptPropertyType getProductCmptPropertyType();

    /**
     * Returns the value.
     */
    public String getPropertyValue();

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

        public PropertyValueIdentifier(String propertyName) {
            super();
            this.propertyName = Preconditions.checkNotNull(propertyName);
        }

        public PropertyValueIdentifier(IPropertyValue p) {
            this(p.getPropertyName());
        }

        @Override
        public int hashCode() {
            return propertyName.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (getClass() != o.getClass()) {
                return false;
            }
            return StringUtils.equals(propertyName, ((PropertyValueIdentifier)o).propertyName);
        }

        @Override
        public IPropertyValue getValueFrom(ITemplatedValueContainer container) {
            ArgumentCheck.isInstanceOf(container, IPropertyValueContainer.class);
            IPropertyValueContainer propertyValueContainer = (IPropertyValueContainer)container;
            return propertyValueContainer.getPropertyValue(propertyName);
        }

    }

}
