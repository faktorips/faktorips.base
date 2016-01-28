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

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;

/**
 * An interface for product component properties ({@link IPropertyValue} or {@link IProductCmptLink}
 * ) that could be configured using templates.
 */
public interface ITemplatedProperty {

    /** The name of the template value status property. */
    public static final String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$

    /** Validation message code to indicate that this property's template status is invalid. */
    public static final String MSGCODE_INVALID_TEMPLATE_VALUE_STATUS = "TEMPLATEDPROPERTY-InvalidTemplateValueStatus"; //$NON-NLS-1$

    /**
     * Sets this property's template status (e.g. whether it is inherited from a parent template or
     * not).
     * 
     * @param status the new template status
     */
    public void setTemplateValueStatus(TemplateValueStatus status);

    /**
     * Returns the current template status of this property. It specifies whether a property is is
     * defined in this object or inherited from a template.
     * 
     * @return this property's template status (e.g. whether it is inherited from a parent template
     *         or not).
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
     * Finds the property in the template hierarchy (parent or grand*-parent template) that has the
     * status {@link TemplateValueStatus#DEFINED} and thus is used as a template property for this
     * property.
     * 
     * If there is no template or no parent template defines such a property, this method returns
     * <code>null</code>.
     * 
     * Note: This method does <em>not</em> find the property that provides the actual value. Instead
     * it finds the closest template property. E.g. in case this property overrides a property from
     * its template, this method still finds the template property (even though the property is
     * overridden).
     * 
     * @param ipsProject The {@link IIpsProject} used to search the template hierarchy
     * @return the property that should be used as template or <code>null</code> if there is no such
     *         property.
     */
    public ITemplatedProperty findTemplateProperty(IIpsProject ipsProject);

    /**
     * Checks whether this property can configure the template value status or not. This is the case
     * if its container uses a template or if the container itself is a product template. If it is a
     * normal product component that does not use templates, the template value status should always
     * be {@link TemplateValueStatus#DEFINED}
     * 
     * @return <code>true</code> if the corresponding container is using a template or if itself is
     *         a template.
     */
    boolean isConfiguringTemplateValueStatus();

    /**
     * Get the {@link IIpsProject} of this property.
     * 
     * @return The parent {@link IIpsProject} of this
     */
    public IIpsProject getIpsProject();

    /**
     * Returns the {@link ITemplatedPropertyContainer} which is the parent of this object
     * 
     * @return the container this object belongs to
     */
    public ITemplatedPropertyContainer getTemplatedPropertyContainer();

}
