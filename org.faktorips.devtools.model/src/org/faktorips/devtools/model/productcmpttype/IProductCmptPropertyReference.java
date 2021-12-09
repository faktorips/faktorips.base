/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpttype;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.IType;

/**
 * An {@link IIpsObjectPart} that references an {@link IProductCmptProperty}.
 * <p>
 * Such references are created by product component types as soon as the user changes the ordering
 * of product component properties in their respective categories.
 * <p>
 * References to IPS object parts via their names are not always the best solution as these are
 * fragile with respect to the 'Rename' refactoring. Therefore, an
 * {@link IProductCmptPropertyReference} utilizes the part id. As the part id is not necessarily
 * unique across types, the {@link IpsObjectType} of the poperty's {@link IType} is stored as well.
 * 
 * @since 3.6
 * 
 * @see IProductCmptProperty
 * @see IProductCmptCategory
 * @see IProductCmptType
 */
public interface IProductCmptPropertyReference extends IIpsObjectPart {

    public static final String PROPERTY_REFERENCED_PART_ID = "referencedPartId"; //$NON-NLS-1$

    public static final String PROPERTY_REFERENCED_IPS_OBJECT_TYPE = "referencedIpsObjectType"; //$NON-NLS-1$

    /**
     * Sets the referenced {@link IProductCmptProperty}.
     */
    public void setReferencedProperty(IProductCmptProperty property);

    /**
     * Returns whether the given {@link IProductCmptProperty} is identified by this
     * {@link IProductCmptPropertyReference}.
     */
    public boolean isReferencedProperty(IProductCmptProperty property);

    /**
     * Returns the referenced {@link IProductCmptProperty} or null if it cannot be found.
     * 
     * @throws CoreRuntimeException if an error occurs during the search
     */
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the part id of the referenced {@link IProductCmptProperty}.
     * <p>
     * Note that we reference the part id instead of the part's name due to the independence of the
     * part id with respect to the 'Rename' refactoring.
     */
    public String getReferencedPartId();

    /**
     * Sets the part id of the referenced {@link IProductCmptProperty}.
     * 
     * @see #getReferencedPartId()
     */
    public void setReferencedPartId(String partId);

    /**
     * Returns the {@link IpsObjectType} of the {@link IType} that is the origin of the referenced
     * {@link IProductCmptProperty}.
     * <p>
     * As the part id of an {@link IProductCmptProperty} is not necessarily unique across types,
     * this information is essential to be able to correctly determine the referenced
     * {@link IProductCmptProperty}.
     */
    public IpsObjectType getReferencedIpsObjectType();

    /**
     * Sets the {@link IpsObjectType} of the {@link IType} that is the origin of the referenced
     * {@link IProductCmptProperty}.
     * 
     * @see #getReferencedIpsObjectType()
     */
    public void setReferencedIpsObjectType(IpsObjectType ipsObjectType);

}
