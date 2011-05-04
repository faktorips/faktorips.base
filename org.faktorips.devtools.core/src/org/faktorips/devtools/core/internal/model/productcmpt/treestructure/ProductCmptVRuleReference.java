/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
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

    private final IProductCmpt cmpt;
    private IValidationRuleConfig vRuleConfig;

    public ProductCmptVRuleReference(IProductCmptTreeStructure structure, ProductCmptStructureReference parent,
            IProductCmpt cmpt, IValidationRuleConfig ruleConfig) throws CycleInProductStructureException {
        super(structure, parent);
        this.cmpt = cmpt;
        vRuleConfig = ruleConfig;
    }

    @Override
    public IValidationRuleConfig getValidationRuleConfig() {
        return vRuleConfig;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        return cmpt;
    }

    @Override
    public IIpsObjectPart getWrapped() {
        return getValidationRuleConfig();
    }

}
