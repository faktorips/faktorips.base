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

import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A reference to a <code>IProductCmptTypeRelation</code> which is used in the
 * <code>IProductCmptStructure</code>
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptTypeAssociationReference extends IProductCmptStructureReference {

    /**
     * Returns the <code>IProductCmptTypeAssociation</code> this reference refers to.
     */
    IProductCmptTypeAssociation getAssociation();

}
