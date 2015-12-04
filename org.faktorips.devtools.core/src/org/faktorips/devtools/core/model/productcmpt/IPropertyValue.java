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
import org.faktorips.devtools.core.internal.model.productcmpt.TemplateValueSettings;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * Base interface for properties stored in product component generations like formulas, table
 * content usages, product component attributes and configuration elements.
 * 
 * @author Jan Ortmann
 */
public interface IPropertyValue extends IIpsObjectPart {

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "PROPERTYVALUE-"; //$NON-NLS-1$

    /** The name of the template value status property. */
    public static final String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this property value's template status is invalid.
     * 
     * @see TemplateValueSettings
     */
    public static final String MSGCODE_INVALID_TEMPLATE_STATUS = MSGCODE_PREFIX + "InvalidTemplateStatus"; //$NON-NLS-1$

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

    /**
     * Returns the current template status of this property value. It specifies whether a value is
     * is defined in this object or inherited from a template.
     * 
     * @return this property value's template status (e.g. whether it is inherited from a parent
     *         template or not).
     * 
     * @see TemplateValueStatus
     */
    public TemplateValueStatus getTemplateValueStatus();

    /**
     * Sets the next valid template value status. The template value status order is defined in
     * {@link TemplateValueStatus}.
     * 
     */
    public void switchTemplateValueStatus();

    /**
     * Finds the property value in the template hierarchy (parent or grand*-parent template) that
     * has the status {@link TemplateValueStatus#DEFINED} and thus is used as a template value for
     * this property value.
     * 
     * If there is no template or no parent template defines such a property value, this method
     * returns <code>null</code>.
     * 
     * Note: This method does <em>not</em> find the property value that provides the actual value.
     * Instead it finds the closest template value. E.g. in case this property value overrides a
     * value from its template, this method still finds the template property value (even though the
     * value is overridden).
     * 
     * @param ipsProject The {@link IIpsProject} used to search the template hierarchy
     * @return the property that should be used as template or <code>null</code> if there is no such
     *         property.
     */
    public IPropertyValue findTemplateProperty(IIpsProject ipsProject);

    /**
     * Checks whether this property value can configure the template value status or not. This is
     * the case if its container uses a template or if the container itself is a product template.
     * If it is a normal product component that does not use templates, the template value status
     * should always be {@link TemplateValueStatus#DEFINED}
     * 
     * @return <code>true</code> if the corresponding container is using a template or if itself is
     *         a template.
     */
    boolean isConfiguringTemplateValueStatus();

}
