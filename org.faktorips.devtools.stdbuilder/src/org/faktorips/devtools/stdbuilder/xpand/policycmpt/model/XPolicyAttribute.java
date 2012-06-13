/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;

public class XPolicyAttribute extends XAttribute {

    private DatatypeHelper valuesetDatatypeHelper;

    public XPolicyAttribute(IPolicyCmptTypeAttribute attribute, GeneratorModelContext model, ModelService modelService) {
        super(attribute, model, modelService);
        valuesetDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(attribute.getIpsProject(),
                getDatatypeHelper());
    }

    @Override
    public IPolicyCmptTypeAttribute getIpsObjectPartContainer() {
        return (IPolicyCmptTypeAttribute)super.getIpsObjectPartContainer();
    }

    /**
     * @return Returns the attribute.
     */
    @Override
    public IPolicyCmptTypeAttribute getAttribute() {
        return getIpsObjectPartContainer();
    }

    public boolean isGenerateField() {
        return getAttribute().getAttributeType() != AttributeType.DERIVED_ON_THE_FLY;
    }

    public boolean isGenerateGetter() {
        return true;
    }

    public boolean isGenerateSetter() {
        return !getAttribute().isDerived();
    }

    public boolean isGenerateDefaultInitialize() {
        return isOverwrite() && getAttribute().isChangeable();
    }

    public boolean isGenerateDefaultForDerivedAttribute() {
        try {
            IProductCmptTypeMethod formulaSignature = (getAttribute()).findComputationMethod(getIpsProject());
            return (!(getAttribute()).isProductRelevant() || formulaSignature == null || formulaSignature.validate(
                    getIpsProject()).containsErrorMsg());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
