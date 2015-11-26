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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue.TemplateValueStatus;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * Base interface for properties stored in product component generations like formulas, table
 * content usages, product component attributes and configuration elements.
 * 
 * @author Jan Ortmann
 */
public interface IPropertyValue extends IIpsObjectPart {

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
     * Returns the type of the product definition property.
     * <p>
     * See {@link ProductCmptPropertyType} for safe casts to a specific model element.
     * 
     * @see ProductCmptPropertyType
     */
    public ProductCmptPropertyType getPropertyType();

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

    /**
     * Sets this property value's template status (e.g. whether it is inherited from a parent
     * template or not).
     * 
     * @param status the new template status
     */
    public void setTemplateValueStatus(TemplateValueStatus status);

}
