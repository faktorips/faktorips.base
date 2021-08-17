/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.template;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;

public interface ITemplatedValueContainer {

    /**
     * Returns the product component for this container. If this container is a
     * {@link IProductCmptGeneration product component generation} the corresponding product
     * component is returned. If this is a {@link IProductCmpt product component} it returns itself.
     */
    public IProductCmpt getProductCmpt();

    /**
     * @return <code>true</code> if this property container is itself defined as a template or is
     *         part of a template. <code>false</code> if it is a regular product component.
     */
    public boolean isProductTemplate();

    /**
     * Returns the name of the template of this property container. Returns <code>null</code> if
     * this container does not use a template.
     * 
     * @return The qualified name of the referenced template
     */
    public String getTemplate();

    /**
     * Returns <code>true</code> if this container is using a template. This is the case if
     * {@link IProductCmpt#getTemplate()} returns a non-empty value. That does not mean that the
     * referenced template actually exists.
     * 
     * @return <code>true</code> if there is a template specified by this product component
     */
    public boolean isUsingTemplate();

    /**
     * Returns the template object that is used by this property container if this property
     * container has specified a template. Returns {@code null} if no template is specified or the
     * specified template was not found.
     * 
     * @param ipsProject The project that should be used to search for the template
     * @return The property container that is specified as the template of this property container
     */
    public ITemplatedValueContainer findTemplate(IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if properties of this container are used in a template hierarchy,
     * <code>false</code> else. Returns <code>true</code> if this property's container is based on a
     * template or is a template itself.
     */
    public boolean isPartOfTemplateHierarchy();

}