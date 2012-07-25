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
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XFormulaSignature;

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

    @Override
    public String getFieldName() {
        if (isConstant()) {
            return getJavaNamingConvention().getConstantClassVarName(getName());
        } else {
            return super.getFieldName();
        }
    }

    /**
     * Returns true for all attributes except derived (on the fly) and overridden attributes.
     */
    public boolean isGenerateField() {
        return (isDerivedByExplicitMethodCall() || isChangeable()) && !isOverwrite();
    }

    public boolean isPublished() {
        return getAttribute().getModifier() == Modifier.PUBLISHED;
    }

    /**
     * Returns true for all attributes except for constant and overridden attributes.
     */
    public boolean isGenerateGetter() {
        return !isConstant() && !isOverwrite();
    }

    /**
     * Returns true for all attributes except for derived, constant and overridden attributes.
     */
    public boolean isGenerateSetter() {
        return !isDerived() && !isConstant() && !isOverwrite();
    }

    public boolean isDerived() {
        return getAttribute().isDerived();
    }

    public boolean isConstant() {
        return getAttribute().getAttributeType() == AttributeType.CONSTANT;
    }

    /**
     * @see PolicyCmptImplClassBuilder line 1049
     */
    public boolean isGenerateInitWithProductData() {
        return isProductRelevant() && isChangeable() && !isOverwrite();
    }

    public boolean isGenerateInitPropertiesFromXML() {
        return isGenerateField();
    }

    public boolean isGenerateDefaultInitialize() {
        return isOverwrite() && isChangeable();
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

    public boolean isGenerateGetAllowedValuesFor() {
        if (isDerived()) {
            return false;
        }
        if (isValueSetUnrestricted() && !isProductRelevant()) {
            return false;
        }
        if (isValueSetEnum() && isDatatypeContentSeperatedEnum()) {
            return false;
        }
        return true;
    }

    protected boolean isDatatypeContentSeperatedEnum() {
        return DatatypeUtil.isEnumTypeWithSeparateContent(getDatatype());
    }

    protected boolean isValueSetEnum() {
        return isValueSetOfType(ValueSetType.ENUM);
    }

    protected boolean isValueSetUnrestricted() {
        return isValueSetOfType(ValueSetType.UNRESTRICTED);
    }

    private boolean isValueSetOfType(ValueSetType valueSetType) {
        return getAttribute().getValueSet().getValueSetType() == valueSetType;
    }

    public boolean isConsiderInDeltaComputation() {
        return isPublished() && isFieldRequired();
    }

    public boolean isConsiderInCopySupport() {
        return isFieldRequired();
    }

    private boolean isChangeable() {
        return getAttribute().isChangeable();
    }

    /**
     * Returns <code>true</code> if a member variable is required for the type of attribute. This is
     * currently the case for changeable attributes and attributes that are derived by an explicit
     * method call.
     */
    public boolean isFieldRequired() {
        return (isChangeable() || isDerivedByExplicitMethodCall()) && !isOverwrite();
    }

    protected boolean isDerivedByExplicitMethodCall() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    protected boolean isDerivedOnTheFly() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_ON_THE_FLY;
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

    public String getConstantNamePropertyName() {
        return "PROPERTY_" + StringUtils.upperCase(getFieldName());
    }

    public String getOldValueVariable() {
        return " old" + StringUtils.capitalize(getFieldName());
    }

    public String getMethodNameGetAllowedValuesFor() {
        return "getSetOfAllowedValuesFor" + StringUtils.capitalize(getFieldName());
    }

    public String getMethodNameGetDefaultValue() {
        return "getDefaultValue" + StringUtils.capitalize(getFieldName());
    }

    public String getMethodNameComputeAttribute() {
        return getAttribute().getComputationMethodSignature();
    }

    public String getNewInstanceExpression() {
        JavaCodeFragment fragment = getDatatypeHelper().newInstanceFromExpression("propMap.get(\"" + getName() + "\")");
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

}
