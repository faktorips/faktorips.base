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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XFormulaSignature;
import org.faktorips.valueset.ValueSet;

public class XPolicyAttribute extends XAttribute {

    private DatatypeHelper valuesetDatatypeHelper;

    public XPolicyAttribute(IPolicyCmptTypeAttribute attribute, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(attribute, modelContext, modelService);
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

    public boolean isPublished() {
        return getAttribute().getModifier() == Modifier.PUBLISHED;
    }

    public boolean isGenerateGetter() {
        return !isConstant();
    }

    public boolean isGenerateSetter() {
        return !isDerived() && !isConstant();
    }

    private boolean isDerived() {
        return getAttribute().isDerived();
    }

    public boolean isConstant() {
        return getAttribute().getAttributeType() == AttributeType.CONSTANT;
    }

    public boolean isRequiringInitWithProductData() {
        return isProductRelevant() && !isDerived();
    }

    public boolean isRequiringInitFromXML() {
        return !isDerived() && !isConstant();
    }

    public boolean isGenerateDefaultInitialize() {
        return isOverwrite() && getAttribute().isChangeable();
    }

    private String getValueSetClass() {
        return addImport(ValueSet.class);
    }

    public String getDatatypeClass() {
        return addImport(valuesetDatatypeHelper.getJavaClassName());
    }

    public boolean isGenerateDefaultForDerivedAttribute() {
        try {
            IProductCmptTypeMethod formulaSignature = (getAttribute()).findComputationMethod(getIpsProject());
            return !getAttribute().isProductRelevant() || formulaSignature == null
                    || formulaSignature.validate(getIpsProject()).containsErrorMsg();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public XFormulaSignature getFormulaSignature() {
        IProductCmptTypeMethod method = getComputationMethod();
        return getModelNode(method, XFormulaSignature.class);
    }

    private IProductCmptTypeMethod getComputationMethod() {
        try {
            return getAttribute().findComputationMethod(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isProductRelevant() {
        return getAttribute().isProductRelevant();
    }

    public String getProductGenerationClassOrInterfaceName() {
        return getXPolicyCmptClass().getProductGenerationClassOrInterfaceName();
    }

    public String getProductGenerationClassName() {
        return getXPolicyCmptClass().getProductGenerationClassName();
    }

    public String getProductGenerationArgumentName() {
        return getXPolicyCmptClass().getProductGenerationArgumentName();
    }

    public String getMethodNameGetProductCmptGeneration() {
        return getXPolicyCmptClass().getMethodNameGetProductCmptGeneration();
    }

    private XPolicyCmptClass getXPolicyCmptClass() {
        IPolicyCmptType polType = getIpsObjectPartContainer().getPolicyCmptType();
        XPolicyCmptClass xPolicyCmptClass = getModelNode(polType, XPolicyCmptClass.class);
        return xPolicyCmptClass;
    }

    public String getFieldPropertyName() {
        return "PROPERTY_" + StringUtils.upperCase(getFieldName());
    }

    public String getOldValueVariable() {
        return " old" + StringUtils.capitalize(getFieldName());
    }

    public String getMethodNameGetAllowedValuesFor() {
        return "getSetOfAllowedValuesFor" + StringUtils.capitalize(getFieldName());
    }

    public String getMethodNameComputeAttribute() {
        return getAttribute().getComputationMethodSignature();
    }

    public String getNewInstanceExpression() {
        JavaCodeFragment fragment = getDatatypeHelper().newInstanceFromExpression(
                "propMap.get(\"" + getAttributeName() + "\")");
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

}
