/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.internal.datatype.DynamicEnumDatatype;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.XMethod;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.template.ClassNames;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.StringLengthValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

public class XPolicyAttribute extends XAttribute {

    public XPolicyAttribute(IPolicyCmptTypeAttribute attribute, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(attribute, modelContext, modelService);
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

    public DatatypeHelper getValuesetDatatypeHelper() {
        return StdBuilderHelper.getDatatypeHelperForValueSet(getAttribute().getIpsProject(), getDatatypeHelper());
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
     * Returns <code>true</code> for constant attributes and attributes that require a member
     * variable.
     * 
     * @see #isRequireMemberVariable()
     */
    public boolean isGenerateField() {
        return isRequireMemberVariable() || isConstant();
    }

    /**
     * Returns whether a getter is to be generated:
     * <ul>
     * <li>if an interface is generated or interface generation is turned off (so that we can add
     * all model information in annotations)
     * <li>if the attribute does not overwrite a supertype attribute</li>
     * <li>if the attribute does overwrite a derived-on-the-fly attribute and is itself marked as
     * changeable</li>
     * <li>if the attribute is derived-on-the-fly (because it has to be manually implemented)</li>
     * </ul>
     */
    public boolean isGenerateGetter(boolean generatingInterface) {
        if (isConstant()) {
            return false;
        } else {
            boolean getterIsDefinedHere = !isOverwrite() || generatingInterface
                    || !getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
            boolean attributeIsOrOverridesDerivedOnTheFly = isDerivedOnTheFly() || isOverwritingDerivedOnTheFly();
            return getterIsDefinedHere || attributeIsOrOverridesDerivedOnTheFly || isOverwriteAbstract();
        }
    }

    private boolean isOverwritingDerivedOnTheFly() {
        if (isOverwrite()) {
            XPolicyAttribute overwrittenAttribute = getOverwrittenAttribute();
            return overwrittenAttribute.isDerivedOnTheFly() || overwrittenAttribute.isOverwritingDerivedOnTheFly();
        } else {
            return false;
        }
    }

    /**
     * Returns true for all attributes except for derived, constant and overridden attributes.
     */
    public boolean isGenerateSetter() {
        boolean noDuplicateOverwrite = !isOverwrite() || isAttributeTypeChangedByOverwrite() || isOverwriteAbstract();
        return !isDerived() && !isConstant() && noDuplicateOverwrite;
    }

    /**
     * Returns {@code true} if internal setters should be generated.
     * <p>
     * This is the case if both {@link #isGenerateSetter()} and
     * {@link GeneratorConfig#isGenerateChangeSupport()} return {@code true}.
     */
    public boolean isGenerateSetterInternal() {
        return isGenerateSetter() && getGeneratorConfig().isGenerateChangeSupport();
    }

    public boolean isOverwriteAbstract() {
        return isOverwrite() && getOverwrittenAttribute().isAbstract();
    }

    public boolean isDerived() {
        return getAttribute().isDerived();
    }

    public boolean isConstant() {
        return getAttribute().getAttributeType() == AttributeType.CONSTANT;
    }

    public boolean isGenerateInitWithProductData() {
        return isProductRelevant() && isChangeable() && isGenerateInitWithProductDataBecauseOfOverwrite();
    }

    private boolean isGenerateInitWithProductDataBecauseOfOverwrite() {
        return !isOverwrite() || isAttributeTypeChangedByOverwrite() || !getOverwrittenAttribute().isProductRelevant();
    }

    public boolean isGenerateInitWithoutProductData() {
        return !isProductRelevant() && isChangeable();
    }

    public boolean isGenerateInitPropertiesFromXML() {
        return isRequireMemberVariable();
    }

    public boolean isGenerateDefaultInitialize() {
        return isOverwrite() && isChangeable() && !isAttributeTypeChangedByOverwrite();
    }

    @Override
    public XPolicyAttribute getOverwrittenAttribute() {
        return (XPolicyAttribute)super.getOverwrittenAttribute();
    }

    /**
     * Returns the java class name for value set. For example <code>ValueSet&lt;Integer&gt;</code>
     * 
     * @return The class name of the value set
     */
    public String getValueSetJavaClassName(GenerateValueSetType generateValueSetType) {
        return getValueSetJavaClassName(generateValueSetType, false);
    }

    /**
     * Returns the java class name for value set with wildcard type. For example an
     * <code>ValueSet&lt;? extends AbstractEnumType&gt;</code>
     * 
     * @return the class name of the value set
     */
    public String getValueSetJavaClassNameWithWildcard(GenerateValueSetType generateValueSetType) {
        return getValueSetJavaClassName(generateValueSetType, true);
    }

    private String getValueSetJavaClassName(GenerateValueSetType generateValueSetType, boolean useWildcards) {
        String wildcards = useWildcards ? "? extends " : "";
        if (generateValueSetType.isGenerateUnified() || isValueSetUnrestricted() || isValueSetDerived()) {
            String valueSetClass = addImport(ValueSet.class);
            return valueSetClass + "<" + wildcards + getJavaClassUsedForValueSet() + ">";
        } else if (isValueSetEnum()) {
            String valueSetClass = addImport(OrderedValueSet.class);
            return valueSetClass + "<" + wildcards + getJavaClassUsedForValueSet() + ">";
        } else if (isValueSetRange()) {
            // call this method to add import statement the type
            getValuesetDatatypeHelper().getJavaClassName();
            return addImport(getValuesetDatatypeHelper().getRangeJavaClassName(true));
        } else if (isValueSetStringLength()) {
            return addImport(ValueSet.class) + "<String>";
        } else {
            throw new RuntimeException("Unexpected valueset type for attribute " + getName());
        }
    }

    /**
     * Adds an import for the datatype's java class name and returns it. The java class may differ
     * for value sets. For example when the type is primitive we need to use the wrapped type
     * instead.
     * 
     * @return The name of the java class used for value sets.
     */
    public String getJavaClassUsedForValueSet() {
        return addImport(getValuesetDatatypeHelper().getJavaClassName());
    }

    public String getValueSetNullValueCode() {
        JavaCodeFragment nullValueCode = getValuesetDatatypeHelper().nullExpression();
        addImport(nullValueCode.getImportDeclaration());
        return nullValueCode.getSourcecode();
    }

    /**
     * Returns the code to create a new instance of and element stored in a value set. The
     * expression is the code to retrieve the value from, e.g. another variable. The
     * repositoryExpression is the code to for getting a repository. It may be needed for
     * enumerations with separated content.
     * 
     * @param expression The expression to get the value from
     * @param repositoryExpression the expression to get the repository
     * @return The code needed to create a new instance for a value set
     */
    public String getValueSetNewInstanceFromExpression(String expression, String repositoryExpression) {
        return getNewInstanceFromExpression(getValuesetDatatypeHelper(), expression, repositoryExpression);
    }

    public String getToStringExpression() {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(getFieldNameDefaultValue());
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    @Override
    public String getDefaultValueCode() {
        if (isDatatypeExtensibleEnum()) {
            return "null";
        } else {
            return super.getDefaultValueCode();
        }
    }

    /**
     * Returns true if the data type is an enumeration defined as Faktor-IPS Enum.
     * 
     */
    public boolean isIpsEnum() {
        return getDatatype() instanceof EnumTypeDatatypeAdapter;
    }

    public boolean isJavaEnum() {
        return getDatatype().isEnum();
    }

    public boolean isRangeSupported() {
        return isValueSetTypeSupported(ValueSetType.RANGE);
    }

    public boolean isEnumValueSetSupported() {
        return isValueSetTypeSupported(ValueSetType.ENUM);
    }

    private boolean isValueSetTypeSupported(ValueSetType valueSetType) {
        return getIpsProject().isValueSetTypeApplicable(getDatatype(), valueSetType);
    }

    public String getNewRangeExpression(String lowerBoundExp,
            String upperBoundExp,
            String stepExp,
            String containsNullExp) {
        JavaCodeFragment newRangeInstance = getValuesetDatatypeHelper().newRangeInstance(
                new JavaCodeFragment(lowerBoundExp), new JavaCodeFragment(upperBoundExp), new JavaCodeFragment(stepExp),
                new JavaCodeFragment(containsNullExp), false);
        addImport(newRangeInstance.getImportDeclaration());
        return newRangeInstance.getSourcecode();
    }

    public String newEnumValueSetInstance(String valueCollection, String containsNullExpression) {
        JavaCodeFragment newEnumExpression = getValuesetDatatypeHelper().newEnumValueSetInstance(
                new JavaCodeFragment(valueCollection), new JavaCodeFragment(containsNullExpression), true);
        addImport(newEnumExpression.getImportDeclaration());
        return newEnumExpression.getSourcecode();
    }

    public boolean isGenerateDefaultForOnTheFlyDerivedAttribute() {
        if (!isDerivedOnTheFly()) {
            return false;
        } else if (!getAttribute().isProductRelevant()) {
            return true;
        } else {
            IProductCmptTypeMethod formulaSignature = (getAttribute()).findComputationMethod(getIpsProject());
            return formulaSignature == null || formulaSignature.validate(getIpsProject()).containsErrorMsg();
        }
    }

    public XMethod getFormulaSignature() {
        IProductCmptTypeMethod method = getComputationMethod();
        return getModelNode(method, XMethod.class);
    }

    private IProductCmptTypeMethod getComputationMethod() {
        return getAttribute().findComputationMethod(getIpsProject());
    }

    public boolean isProductRelevant() {
        return getAttribute().isProductRelevant();
    }

    public boolean isValueSetConfiguredByProduct() {
        return getAttribute().isValueSetConfiguredByProduct();
    }

    /**
     * Returns whether this attribute is product relevant at some point in the class hierarchy.
     */
    public boolean isProductRelevantInHierarchy() {
        return isProductRelevant() || isOverwrite() && getOverwrittenAttribute().isProductRelevantInHierarchy();
    }

    public boolean isGenerateGetAllowedValuesForAndGetDefaultValue() {
        if (isConstant()) {
            return false;
        } else {
            return (isProductRelevant() && isChangeable())
                    || isValueSet()
                    || isNotConfiguredOverrideConfigured();
        }
    }

    public boolean isOverrideGetAllowedValuesFor() {
        if (!isOverwrite()) {
            return false;
        }
        boolean overwrittenAttributeSuitedForOverride = getOverwrittenAttribute()
                .isGenerateGetAllowedValuesForAndGetDefaultValue()
                && isMethodNameGetAllowedValuesEqualIncludingUnifyMethodsSetting(getOverwrittenAttribute(),
                        GenerateValueSetType.GENERATE_BY_TYPE);
        return overwrittenAttributeSuitedForOverride || getOverwrittenAttribute().isOverrideGetAllowedValuesFor();
    }

    public boolean isOverrideGetAllowedValuesFor(GenerateValueSetType type) {
        if (!isOverwrite()) {
            return false;
        }
        boolean overwrittenAttributeSuitedForOverride = getOverwrittenAttribute()
                .isGenerateGetAllowedValuesForAndGetDefaultValue()
                && isMethodNameGetAllowedValuesEqualIncludingUnifyMethodsSetting(getOverwrittenAttribute(),
                        type);
        return overwrittenAttributeSuitedForOverride || getOverwrittenAttribute().isOverrideGetAllowedValuesFor();
    }

    public boolean isOverrideSetAllowedValuesFor() {
        if (!isOverwrite()) {
            return false;
        }
        boolean overwrittenAttributeSuitedForOverride = getOverwrittenAttribute()
                .isGenerateGetAllowedValuesForAndGetDefaultValue();
        return overwrittenAttributeSuitedForOverride || getOverwrittenAttribute().isOverrideSetAllowedValuesFor();
    }

    public boolean isOverrideGetDefaultValue() {
        return isOverwrite() && getOverwrittenAttribute().isGenerateGetAllowedValuesForAndGetDefaultValue();
    }

    public boolean isGenerateConstantForValueSet() {
        // NonExtensibleEnumValueSet können nicht generiert werden da die Werte aus einem Repository
        // geladen werden, das im statischen Kontext nicht bekannt ist. Siehe
        // https://jira.faktorzehn.de/browse/FIPS-3981 dazu.
        boolean isGenerateForValueSetType = !isValueSetEnum() || isNonExtensibleEnumValueSet()
                || isNotConfiguredOverrideConfigured();
        return isConcreteOrNotProductRelevant() && isGenerateForValueSetType;
    }

    private boolean isNotConfiguredOverrideConfigured() {
        return !isValueSetConfiguredByProduct() && isOverrideGetAllowedValuesFor()
                && getOverwrittenAttribute().isValueSetConfiguredByProduct();
    }

    private boolean isConcreteOrNotProductRelevant() {
        return !isAbstract() && (!isAbstractValueSet() || !isProductRelevant());
    }

    private boolean isNonExtensibleEnumValueSet() {
        return isValueSetEnum() && !isDatatypeExtensibleEnum();
    }

    public boolean isOverwritingValueSetEqualType() {
        return getValueSetType()
                .equals(getOverwrittenAttribute().getValueSetType());
    }

    public boolean isValueSet() {
        return getAttribute().getValueSet() != null;
    }

    public boolean isValueSetEnum() {
        return isValueSetOfType(ValueSetType.ENUM);
    }

    public boolean isValueSetRange() {
        return isValueSetOfType(ValueSetType.RANGE);
    }

    public boolean isValueSetUnrestricted() {
        return isValueSetOfType(ValueSetType.UNRESTRICTED);
    }

    public boolean isValueSetDerived() {
        return isValueSetOfType(ValueSetType.DERIVED);
    }

    public boolean isValueSetStringLength() {
        return isValueSetOfType(ValueSetType.STRINGLENGTH);
    }

    private boolean isValueSetOfType(ValueSetType valueSetType) {
        return getValueSetType() == valueSetType;
    }

    public boolean isAbstractValueSet() {
        return getAttribute().getValueSet().isAbstract();
    }

    public boolean isConsiderInDeltaComputation() {
        return isPublished() && isRequireMemberVariable();
    }

    public boolean isConsiderInCopySupport() {
        return isRequireMemberVariable();
    }

    public boolean isChangeable() {
        return getAttribute().isChangeable();
    }

    /**
     * Returns <code>true</code> if a member variable is required for the type of attribute. This is
     * currently the case for changeable attributes and attributes that are derived by an explicit
     * method call. But not for constant attributes as they require a constant but not a variable.
     */
    public boolean isRequireMemberVariable() {
        boolean hasExplicitValue = isChangeable() || isDerivedByExplicitMethodCall();
        boolean cantUseMemberFromParent = !isOverwrite() || isAttributeTypeChangedByOverwrite();
        boolean overwritesAbstractAttribute = isOverwrite() && getOverwrittenAttribute().isAbstract();
        return hasExplicitValue && (cantUseMemberFromParent || overwritesAbstractAttribute) && !isAbstract();
    }

    @Override
    public boolean isAbstract() {
        return super.isAbstract() && (isProductRelevant() || getPolicyCmptNode().isAbstract());
    }

    protected boolean isDerivedByExplicitMethodCall() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    protected boolean isDerivedOnTheFly() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_ON_THE_FLY;
    }

    public boolean isAttributeTypeChangedByOverwrite() {
        return isOverwrite() && isChangeable() && isOverwritingDerivedOnTheFly();
    }

    /**
     * Returns the name of the type where this interface is defined. For published attributes this
     * is the name of the interface (if there are any generated) for public interfaces it is the
     * name of the implementation.
     * 
     */
    public String getTypeName() {
        return getPolicyCmptNode().getSimpleName(BuilderAspect
                .getValue(getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject()) && isPublished()));
    }

    public String getProductGenerationClassName() {
        return getPolicyCmptNode().getProductCmptGenerationClassName();
    }

    public String getProductGenerationArgumentName() {
        return getPolicyCmptNode().getProductCmptNode().getNameForVariable();
    }

    public String getMethodNameGetProductCmptGeneration() {
        return getPolicyCmptNode().getMethodNameGetProductCmptGeneration();
    }

    public String getMethodNameGetProductCmpt() {
        return getPolicyCmptNode().getMethodNameGetProductCmpt();
    }

    public XPolicyCmptClass getPolicyCmptNode() {
        IPolicyCmptType polType = getIpsObjectPartContainer().getPolicyCmptType();
        XPolicyCmptClass xPolicyCmptClass = getModelNode(polType, XPolicyCmptClass.class);
        return xPolicyCmptClass;
    }

    public boolean mark(boolean flag) {
        return flag;
    }

    public String getOldValueVariable() {
        return "old" + StringUtils.capitalize(getFieldName());
    }

    public boolean isMethodNameGetAllowedValuesEqualIncludingUnifyMethodsSetting(XPolicyAttribute overwritten,
            GenerateValueSetType valueSetType) {
        ValueSetMethods setting = getUnifyValueSetSettingFormAttribute(overwritten);
        GenerateValueSetType genValueSet = GenerateValueSetType.mapFromSettings(setting,
                valueSetType);
        return isMethodNameGetAllowedValuesEqualWithOverwrittenAttribute(overwritten, genValueSet);
    }

    public boolean isMethodNameGetAllowedValuesEqualWithOverwrittenAttribute(XPolicyAttribute overwritten,
            GenerateValueSetType genValueSet) {
        return overwritten.getMethodNameGetAllowedValuesFor(genValueSet)
                .equals(getMethodNameGetAllowedValuesFor(genValueSet))
                && hasSameSignature(overwritten, genValueSet);
    }

    private boolean hasSameSignature(XPolicyAttribute overwritten, GenerateValueSetType genValueSet) {
        return Arrays.equals(overwritten.getAllowedValuesMethodParameterSignature(genValueSet),
                getAllowedValuesMethodParameterSignature(genValueSet));
    }

    public boolean isConditionForOverrideAnnotation(GenerateValueSetTypeRule rule) {
        if (!isOverwrite()) {
            return false;
        }
        ValueSetMethods superSetting = getUnifyValueSetSettingFormAttribute(getOverwrittenAttribute());
        GenerateValueSetType superGenMode = GenerateValueSetType.mapFromSettings(superSetting, rule.getFromMethod());

        String thisMethodName = getMethodNameGetAllowedValuesFor(rule.getFromMethod());
        String superMethodName = getOverwrittenAttribute().getMethodNameGetAllowedValuesFor(superGenMode);

        String[] thisMethodSignature = getAllowedValuesMethodParameterSignature(rule.getFromMethod());
        String[] superMethodSignature = getOverwrittenAttribute()
                .getAllowedValuesMethodParameterSignature(superGenMode);

        if (thisMethodName.equals(superMethodName) && Arrays.equals(thisMethodSignature, superMethodSignature)) {
            if (getOverwrittenAttribute().isValueSetUnrestricted() && !isValueSetUnrestricted()) {
                if (isOverwritingValueSetWithMoreConcreteType(rule.getFromMethod())) {
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isOverwritingValueSetWithMoreConcreteType(GenerateValueSetType type) {
        boolean isOverwrittenAndGenerateAllowedValues = isOverwrite()
                && getOverwrittenAttribute().isGenerateGetAllowedValuesForAndGetDefaultValue();
        return isOverwrittenAndGenerateAllowedValues
                && isMethodNameGetAllowedValuesEqualWithOverwrittenAttribute(getOverwrittenAttribute(), type)
                && !isOverwritingValueSetEqualType() && !getOverwrittenAttribute().isValueSetDerived();
    }

    public boolean isOverwritingValueSetWithMoreConcreteTypeForByType() {
        return isOverwrite() && getOverwrittenAttribute().isGenerateGetAllowedValuesForAndGetDefaultValue()
                && !isMethodNameGetAllowedValuesEqualWithOverwrittenAttribute(getOverwrittenAttribute(),
                        GenerateValueSetType.GENERATE_BY_TYPE);
    }

    public boolean isOverwritingValueSetWithMoreConcreteTypeForByTypeWithBothTypeParent() {
        return isOverwrite() && getOverwrittenAttribute().isGenerateGetAllowedValuesForAndGetDefaultValue()
                && (getAttribute().getValueSet().compareTo(getOverwrittenAttribute().getAttribute().getValueSet()) != 0
                        || getValueSetType() == ValueSetType.DERIVED);
    }

    public boolean isOverwritingValueSetWithDerived() {
        return isOverwrite() && getOverwrittenAttribute().isGenerateGetAllowedValuesForAndGetDefaultValue()
                && isValueSetDerived();
    }

    /**
     * Whether an allowed-values-method should be deprecated.
     * <p>
     * The by-type method is marked as deprecated, if both methods are generated.
     * 
     * @return {@code true} If the unify-value-set setting is <em>both</em>, the unified method name
     *         is different from the by-type name and the method is generated for
     *         {@link GenerateValueSetType#GENERATE_BY_TYPE}.
     */
    public boolean isGetAllowedValuesMethodDeprecated(GenerateValueSetTypeRule rule) {
        if (!rule.isFromDeprecated()) {
            return false;
        }
        return rule.getFromMethod().isGenerateByType()
                && getUnifyValueSetMethodsSetting().isBoth();
    }

    public String[] getAllowedValuesMethodParameterSignature(GenerateValueSetType type) {
        if (type.isGenerateByType()) {
            return new String[] { ClassNames.IValidationContext(this), "context" };
        } else {
            return new String[] {};
        }
    }

    public String allowedValuesMethodParameter(GenerateValueSetType caller,
            GenerateValueSetType called) {
        if (called.isGenerateByType()) {
            if (caller.isGenerateByType()) {
                return "context";
            } else {
                return "null";
            }
        } else {
            return "";
        }
    }

    public ValueSetType getValueSetType() {
        return getAttribute().getValueSet().getValueSetType();
    }

    public boolean isOverwritingAttributeWithDifferentValueSetTypeAndGenerateValueSetType() {
        if (isOverwrite() && getOverwrittenAttribute().isGenerateGetAllowedValuesForAndGetDefaultValue()) {
            ValueSetMethods superSetting = getUnifyValueSetSettingFormAttribute(getOverwrittenAttribute());
            return (superSetting.isByValueSetType() || superSetting.isBoth())
                    && !getUnifyValueSetMethodsSetting().isByValueSetType()
                    && !isOverwritingValueSetEqualType();
        }
        return false;
    }

    /**
     * Creates the method name for the getter of the allowed values of a {@link ValueSet}. If
     * {@link GenerateValueSetType#GENERATE_BY_TYPE} add a different prefix for enums and ranges.
     * 
     * @param valueSetMethods If the method should be generated by-type or by a unified name.
     * @return The method name.
     */
    public String getMethodNameGetAllowedValuesFor(GenerateValueSetType valueSetMethods) {
        String prefix;
        if (valueSetMethods.isGenerateByType()) {
            ValueSetType valueSetType = getValueSetType();
            if (isOverwritingAttributeWithDifferentValueSetTypeAndGenerateValueSetType()) {
                valueSetType = getOverwrittenAttribute().getValueSetType();
            }
            if (valueSetType == ValueSetType.ENUM) {
                prefix = "getAllowedValuesFor";
            } else if (valueSetType == ValueSetType.RANGE) {
                prefix = "getRangeFor";
            } else {
                prefix = "getSetOfAllowedValuesFor";
            }
        } else {
            prefix = "getAllowedValuesFor";
        }
        return prefix + StringUtils.capitalize(getFieldName());

    }

    public String getMethodNameSetAllowedValuesFor() {
        return "setAllowedValuesFor" + StringUtils.capitalize(getFieldName());
    }

    public String getFieldNameDefaultValue() {
        return "defaultValue" + StringUtils.capitalize(getFieldName());
    }

    public String getConstantNameValueSet() {
        String constName = convertNameForConstant(getName());
        if (isValueSetRange()) {
            return "MAX_ALLOWED_RANGE_FOR_" + constName;
        } else if (isValueSetStringLength()) {
            return "MAX_ALLOWED_STRING_LENGTH_FOR_" + constName;
        } else {
            return "MAX_ALLOWED_VALUES_FOR_" + constName;
        }
    }

    public String getConstantNameDefaultValue() {
        return "DEFAULT_VALUE_FOR_" + convertNameForConstant(getFieldName());
    }

    private String convertNameForConstant(String name) {
        String constName = name;
        if (getGeneratorConfig().isGenerateSeparatedCamelCase()) {
            constName = StringUtil.camelCaseToUnderscore(constName, false);
        }
        return StringUtils.upperCase(constName);
    }

    /**
     * Returns the code needed to instantiate a value set.
     * <p>
     * It is used to generate the code for the value set constant if there is a value set defined in
     * the model.
     * 
     * @return The code that instantiates the defined value set.
     */
    public String getValuesetCode() {
        JavaCodeFragment result;
        if (isValueSetRange()) {
            IRangeValueSet range = (IRangeValueSet)getAttribute().getValueSet();
            if (range.isEmpty()) {
                result = new JavaCodeFragment("new ");
                result.appendClassName(getValuesetDatatypeHelper().getRangeJavaClassName(true));
                result.append("()");
            } else {
                JavaCodeFragment containsNullFrag = new JavaCodeFragment();
                containsNullFrag.append(range.isContainsNull());
                result = getValuesetDatatypeHelper().newRangeInstance(createCastExpression(range.getLowerBound()),
                        createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()),
                        containsNullFrag, true);
            }
        } else if (isValueSetEnum()) {
            String[] valueIds;
            boolean containsNull;
            if (getAttribute().getValueSet().isEnum()) {
                IEnumValueSet set = (IEnumValueSet)(getAttribute()).getValueSet();
                valueIds = set.getValues();
                containsNull = !getDatatype().isPrimitive() && set.isContainsNull();
            } else if (getDatatype() instanceof EnumDatatype) {
                valueIds = ((EnumDatatype)getDatatype()).getAllValueIds(true);
                containsNull = !getDatatype().isPrimitive();
            } else {
                throw new IllegalArgumentException("This method is only applicable to attributes "
                        + "based on an EnumDatatype or containing an EnumValueSet.");
            }
            result = getValuesetDatatypeHelper().newEnumValueSetInstance(valueIds, containsNull, true);
        } else if (isValueSetStringLength()) {
            IStringLengthValueSet stringy = (IStringLengthValueSet)getAttribute().getValueSet();
            result = new JavaCodeFragment("new ");
            result.appendClassName(StringLengthValueSet.class);
            result.append(String.format("(%1$s, %2$s)", stringy.getMaximumLength(), stringy.isContainsNull()));
        } else {
            result = getUnrestrictedValueSetCode();
        }
        addImport(result.getImportDeclaration());
        return result.getSourcecode();
    }

    private JavaCodeFragment getUnrestrictedValueSetCode() {
        JavaCodeFragment result = new JavaCodeFragment();
        result.append("new "); //$NON-NLS-1$
        result.appendClassName(UnrestrictedValueSet.class);
        result.append("<>("); //$NON-NLS-1$
        result.append(getAttribute().getValueSet().isContainsNull());
        result.appendln(")"); //$NON-NLS-1$
        return result;
    }

    private JavaCodeFragment createCastExpression(String bound) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (StringUtils.isEmpty(bound) && !getValuesetDatatypeHelper().getDatatype().hasNullObject()) {
            frag.append('(');
            frag.appendClassName(getValuesetDatatypeHelper().getJavaClassName());
            frag.append(')');
        }
        frag.append(getValuesetDatatypeHelper().newInstance(bound));
        return frag;
    }

    /**
     * Returns the code to get all values of the enum data type.
     * <p>
     * The method assumes that the data type is an Faktor-IPS Enum (@see {@link #isIpsEnum()}) and
     * returns the code that gets all values of this enum data type. If the enum has separated
     * content the repository expression is needed to access these values.
     */
    public String getAllEnumValuesCode(String repositoryExpression) {
        EnumTypeDatatypeAdapter enumDatatype = ((EnumTypeDatatypeAdapter)getDatatype());
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        if (enumDatatype.getEnumType().isInextensibleEnum()) {
            javaCodeFragment.appendClassName(Arrays.class).append(".asList(").append(getJavaClassName())
                    .append(".values())");
        } else {
            javaCodeFragment.append(repositoryExpression).append(".").append("getEnumValues(")
                    .append(getJavaClassName()).append(".class)");
        }
        addImport(javaCodeFragment.getImportDeclaration());
        return javaCodeFragment.getSourcecode();
    }

    public String getAllJavaEnumValuesCode() {
        DynamicEnumDatatype datatype = (DynamicEnumDatatype)getDatatype();
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        if (isAllValuesMethodWithCollection(datatype)) {
            javaCodeFragment.append(getJavaClassName())
                    .append("." + datatype.getAllValuesMethodName() + "()");
        } else {
            javaCodeFragment.appendClassName(Arrays.class).append(".asList(").append(getJavaClassName())
                    .append("." + datatype.getAllValuesMethodName() + "())");
        }
        addImport(javaCodeFragment.getImportDeclaration());
        return javaCodeFragment.getSourcecode();
    }

    private boolean isAllValuesMethodWithCollection(DynamicEnumDatatype datatype) {
        return datatype.getAllValuesMethod().invokeStatic("") instanceof java.util.Collection;
    }

    /**
     * Returns the name of the field defined for the value set. This field name depends on the kind
     * of value set.
     */
    public String getFieldNameValueSet() {
        if (isValueSetUnrestricted()) {
            return "setOfAllowedValues" + StringUtils.capitalize(getFieldName());
        }
        if (isValueSetRange()) {
            return "rangeFor" + StringUtils.capitalize(getFieldName());
        }
        if (isValueSetEnum()) {
            return "allowedValuesFor" + StringUtils.capitalize(getFieldName());
        }
        if (isValueSetStringLength()) {
            return "maximumLength" + StringUtils.capitalize(getFieldName());
        }
        throw new RuntimeException(NLS.bind("Attribute {0} has an invalid value set type.", getAttribute()));
    }

    public String getMethodNameGetDefaultValue() {
        return getJavaNamingConvention().getGetterMethodName("DefaultValue" + StringUtils.capitalize(getFieldName()),
                getDatatype());
    }

    public String getMethodNameSetDefaultValue() {
        return "setDefaultValue" + StringUtils.capitalize(getFieldName());
    }

    public String getMethodNameComputeAttribute() {
        return getAttribute().getComputationMethodSignature();
    }

    /**
     * Returns the javadoc key used to localize the java doc. The key depends on the kind of the
     * allowed value set and of the kind of artifact you want to generate, identified by the prefix.
     * <p>
     * For example the if the allowed values are configured as range and you want to generate a
     * field for this range you call this method with prefix "FIELD". The method adds the suffix
     * "_RANGE" and returns the key "FIELD_RANGE". Use this key with method
     * {@link #localizedJDoc(String)} to access the translation from property file with the key
     * "FIELD_RANGE_JAVADOC".
     * 
     */
    public String getJavadocKey(String prefix) {
        if (isValueSetRange()) {
            return prefix + "_RANGE";
        } else if (isValueSetEnum()) {
            return prefix + "_ALLOWED_VALUES";
        } else {
            return prefix + "_SET_OF_ALLOWED_VALUES";
        }
    }

    /**
     * This method returns the qualified name of the java class name corresponding to the data type.
     * There is no need to use this qualified name anywhere but we need to be exactly compatible to
     * old code generator.
     * <p>
     * TODO Remove this method an its call in DefaultAndAllowedValues#writeAttributeToXML
     * 
     */
    public String getJavaClassQualifiedName() {
        return getDatatypeHelper().getJavaClassName();
    }

    /**
     * This method returns the qualified name of the java class name corresponding to the data type.
     * There is no need to use this qualified name anywhere but we need to be exactly compatible to
     * old code generator.
     * <p>
     * TODO Remove this method an its call in DefaultAndAllowedValues#writeAttributeToXML
     * 
     */
    public String getJavaClassQualifiedNameUsedForValueSet() {
        return getValuesetDatatypeHelper().getJavaClassName();
    }

    /**
     * Marks the template
     * allowedValuesMethodForNotOverrideAttributesButDifferentUnifyValueSetSettings deprecated.
     * 
     * @param valueSetMethods If the method should be generated by-type or by a unified name.
     * @return If this setting is Both and the generated method name is by-type mark the method as
     *         deprecated.
     */
    public boolean isDeprecatedGetAllowedValuesMethodForNotOverrideAttributesButDifferentUnifyValueSetSettings(
            GenerateValueSetType valueSetMethods) {
        // use getBaseGeneratorConfig here since getGeneratorConfig will use the attribute to find
        // the config, the attribute at this point is from a super type and therefore maybe
        // different than expected
        return valueSetMethods.isGenerateByType() && getUnifyValueSetMethodsSetting().isBoth();
    }

    /**
     * The annotation should only be created on the method matching the generator settings. If the
     * setting is {@link ValueSetMethods#Both} the unified method should be used.
     * 
     * @param rule the Rule
     * @return {@code true} if the settings match
     */
    public boolean isPublishedInterfaceModifierRelevant(GenerateValueSetTypeRule rule) {
        GenerateValueSetType setting = GenerateValueSetType
                .mapFromSettings(getUnifyValueSetMethodsSetting(),
                        GenerateValueSetType.GENERATE_UNIFIED);
        return rule.getFromMethod().equals(setting);
    }

    /**
     * If using the by-type setting and overwriting an attribute with a more concrete type, the used
     * name was already the unified one.
     * 
     * @return {@code true} if both the attribute and the super attribute are
     *         {@link ValueSetMethods#ByValueSetType}.
     */
    public boolean isGenerateAllowedValuesMethodWithMoreConcreteTypeForByType() {
        if (!isOverwrite()) {
            return false;
        }
        ValueSetMethods thisSetting = getUnifyValueSetMethodsSetting();
        ValueSetMethods superSetting = getUnifyValueSetSettingFormAttribute(getOverwrittenAttribute());
        return thisSetting.isByValueSetType()
                && (superSetting.isBoth() || superSetting.isByValueSetType());
    }

    public boolean isGenerateAllowedValuesMethodWithMoreConcreteTypeForByTypeWithBothTypeParent() {
        if (!isOverwrite()) {
            return false;
        }
        ValueSetMethods thisSetting = getUnifyValueSetMethodsSetting();
        ValueSetMethods superSetting = getUnifyValueSetSettingFormAttribute(getOverwrittenAttribute());
        return thisSetting.isByValueSetType() && superSetting.isBoth();
    }

    private ValueSetMethods getUnifyValueSetSettingFormAttribute(XPolicyAttribute attribute) {
        return getContext()
                .getGeneratorConfig(attribute.getAttribute().getIpsObject())
                .getValueSetMethods();
    }

    private ValueSetMethods getUnifyValueSetMethodsSetting() {
        return getContext().getBaseGeneratorConfig().getValueSetMethods();
    }

    public boolean isUnifyValueSetSettingBothFromSuperType() {
        return isOverwrite() && getUnifyValueSetSettingFormAttribute(getOverwrittenAttribute()).isBoth();
    }

    /**
     * If this project uses the setting {@link ValueSetMethods#Unified} and the super project uses
     * {@link ValueSetMethods#Both} and a overwritten attribute is not product configured in the
     * super type, generate only the unified methods.
     */
    public boolean isGenerateGetterAllowedValues(GenerateValueSetTypeRule rule) {
        if (getUnifyValueSetMethodsSetting().isUnified()) {
            return !(isUnifyValueSetSettingBothFromSuperType() && !getOverwrittenAttribute().isProductRelevant()
                    && rule.getFromMethod().isGenerateByType());
        }
        return true;
    }

    /**
     * Enum to generate a specific get valueset method.
     * 
     */
    public static enum GenerateValueSetType {
        GENERATE_UNIFIED {
            @Override
            public GenerateValueSetType inverse() {
                return GENERATE_BY_TYPE;
            }
        },
        GENERATE_BY_TYPE {
            @Override
            public GenerateValueSetType inverse() {
                return GENERATE_UNIFIED;
            }
        };

        /**
         * Convenience method to return the opposing {@link GenerateValueSetType} for a given one.
         * Will throw an {@link IllegalArgumentException} if the type cannot be matched.
         * 
         * @return The opposing type.
         */
        public abstract GenerateValueSetType inverse();

        public boolean isGenerateByType() {
            return GENERATE_BY_TYPE.equals(this);
        }

        public boolean isGenerateUnified() {
            return GENERATE_UNIFIED.equals(this);
        }

        /**
         * Convenience map method for the {@link ValueSetMethods} setting to its corresponding
         * builder enum. Note that the {@link ValueSetMethods#Both} setting is not a valid
         * instruction for the source code builder. Therefore the {@code defaultValue} is used to
         * handle this case.
         * 
         * @param setting The setting from the project settings, in case of
         *            {@link ValueSetMethods#Both} the defaultValue will be returned.
         * @param defaultValue The default to return if the settings can not be matched or the
         *            setting is {@link ValueSetMethods#Both}.
         * @return The enum used to determine the name of the value set method.
         */
        public static GenerateValueSetType mapFromSettings(ValueSetMethods setting, GenerateValueSetType defaultValue) {
            if (setting != null) {
                switch (setting) {
                    case ByValueSetType:
                        return GENERATE_BY_TYPE;
                    case Unified:
                        return GENERATE_UNIFIED;
                    default:
                        break;
                }
            }
            return defaultValue;
        }
    }
}
