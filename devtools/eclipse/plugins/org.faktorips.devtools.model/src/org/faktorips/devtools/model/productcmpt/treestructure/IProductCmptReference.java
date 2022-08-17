/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.treestructure;

import java.util.GregorianCalendar;

import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;

/**
 * A reference to a <code>IProductCmpt</code> used in a <code>IProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptReference extends IProductCmptStructureReference {

    /**
     * Returns the <code>IProductCmpt</code> this reference refers to.
     */
    IProductCmpt getProductCmpt();

    /**
     * Return the link this reference refers to. May be null.
     */
    IProductCmptLink getLink();

    /**
     * 
     * Returns this {@link IProductCmptReference} if it references the searched product component's
     * qualified name. If not it searches all children in the same way and returns the result.
     * <p>
     * This method does only return the first occurrence in the structure. There may be more than
     * one {@link IProductCmptReference} referencing the specified product component.
     * 
     * @param prodCmptQualifiedName the qualified name of the searched {@link IProductCmpt}
     * @return the {@link IProductCmptReference} referencing the indicated {@link IProductCmpt}, or
     *             <code>null</code> if none was found.
     */
    IProductCmptReference findProductCmptReference(String prodCmptQualifiedName);

    /**
     * Checks whether this product component reference has outgoing associations as children
     * 
     * @return true if there are any associations defined in the corresponding type
     */
    boolean hasAssociationChildren();

    /**
     * Returns the minimum validTo Date from the {@link IProductCmptGeneration} and it's child
     * References
     */
    GregorianCalendar getValidTo();

    @Override
    IProductCmptTypeAssociationReference getParent();

}
