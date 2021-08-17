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

import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;

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
