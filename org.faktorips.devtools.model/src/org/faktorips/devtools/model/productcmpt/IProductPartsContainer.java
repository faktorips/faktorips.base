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

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * This interface combines the {@link IPropertyValueContainer} and the
 * {@link IProductCmptLinkContainer}.
 * 
 * @author dirmeier
 */
public interface IProductPartsContainer extends IIpsObjectPartContainer {

    /**
     * Returns all property values for the given type. Returns an empty array if type is
     * <code>null</code> or no property values were found for the given type.
     */
    public <T extends IIpsObjectPart> List<T> getProductParts(Class<T> type);

    /**
     * Returns the product component for this container. If this container is a
     * {@link IProductCmptGeneration product component generation} the corresponding product
     * component is returned. If this is a {@link IProductCmpt product component} it returns itself.
     */
    public IProductCmpt getProductCmpt();

    /**
     * Returns the qualified name of the product component type this property value container is
     * based on.
     */
    public String getProductCmptType();

    /**
     * Finds the {@link IProductCmptType product component type} this container is based on.
     * 
     * @param ipsProject The IPS project which search path is used to search the type.
     * 
     * @return The product component type this link container is based on or <code>null</code> if
     *         the product component type can't be found.
     * 
     * @throws IpsException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns <code>true</code> if this container may contains parts that could change over time,
     * for example a product component generation. Returns <code>false</code> if the container could
     * only contains static parts.
     * <p>
     * If the container may contain both this method have to return <code>true</code>.
     * 
     * @return <code>true</code> if this container may contains parts that could change over time.
     */
    public boolean isChangingOverTimeContainer();

}
