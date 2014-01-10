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

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptVRuleReference;

/**
 * A reference to an {@link IValidationRuleConfig}. Used by {@link IProductCmptTreeStructure}.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ProductCmptVRuleReference extends ProductCmptStructureReference implements IProductCmptVRuleReference {

    private IValidationRuleConfig vRuleConfig;

    public ProductCmptVRuleReference(IProductCmptTreeStructure structure, ProductCmptStructureReference parent,
            IValidationRuleConfig ruleConfig) throws CycleInProductStructureException {
        super(structure, parent);
        vRuleConfig = ruleConfig;
    }

    @Override
    public IValidationRuleConfig getValidationRuleConfig() {
        return vRuleConfig;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        if (getValidationRuleConfig() == null) {
            return null;
        }

        return getValidationRuleConfig().getIpsObject();
    }

    @Override
    public IIpsObjectPart getWrapped() {
        return getValidationRuleConfig();
    }

}
