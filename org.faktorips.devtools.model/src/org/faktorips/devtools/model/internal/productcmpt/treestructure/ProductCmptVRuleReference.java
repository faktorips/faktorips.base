/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.treestructure;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptVRuleReference;

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
