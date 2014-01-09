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

import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;

/**
 * A Reference to an {@link IValidationRuleConfig} to be used in an
 * {@link IProductCmptTreeStructure}.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public interface IProductCmptVRuleReference extends IProductCmptStructureReference {
    /**
     * @return the referenced {@link IValidationRuleConfig}.
     */
    public IValidationRuleConfig getValidationRuleConfig();
}
