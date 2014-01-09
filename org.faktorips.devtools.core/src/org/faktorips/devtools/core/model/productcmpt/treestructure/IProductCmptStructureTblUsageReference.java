/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;

/**
 * A reference to a <code>ITableContentUsage</code> which is used in the
 * <code>IProductCmptStructure</code>
 * 
 * @author Joerg Ortmann
 */
public interface IProductCmptStructureTblUsageReference extends IProductCmptStructureReference {

    /**
     * @return The <code>IProductCmptTypeRelation</code> this reference refers to.
     */
    public ITableContentUsage getTableContentUsage();

}
