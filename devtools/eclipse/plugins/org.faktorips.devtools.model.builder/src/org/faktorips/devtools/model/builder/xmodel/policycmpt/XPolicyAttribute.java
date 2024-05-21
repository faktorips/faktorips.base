/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.policycmpt;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.builder.xmodel.GeneratorConfig;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.XAttribute;
import org.faktorips.devtools.model.builder.xmodel.XMethod;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.valueset.ValueSet;

public class XPolicyAttribute extends XAttribute {

    private GeneratorConfig generatorConfigOverride;

    public XPolicyAttribute(IPolicyCmptTypeAttribute attribute, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(attribute, modelContext, modelService);
    }

    /**
     * Creates a policy attribute with a generator config override, for example to generate code for
     * a non-overwritten supertype attribute with a child type's config.
     */
    public XPolicyAttribute(IPolicyCmptTypeAttribute attribute, GeneratorModelContext modelContext,
            ModelService modelService, GeneratorConfig generatorConfigOverride) {
        this(attribute, modelContext, modelService);
        this.generatorConfigOverride = generatorConfigOverride;
    }

    @Override
    public IPolicyCmptTypeAttribute getIpsObjectPartContainer() {
        return (IPolicyCmptTypeAttribute)super.getIpsObjectPartContainer();
    }

    @Override
    public GeneratorConfig getGeneratorConfig() {
        if (generatorConfigOverride != null) {
            return generatorConfigOverride;
        }
        return super.getGeneratorConfig();
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
     * <li>if the overriding attribute changes the datatype</li>
     * </ul>
     */
    public boolean isGenerateGetter(boolean generatingInterface) {
        if (isConstant()) {
            return false;
        } else {
            boolean getterIsDefinedHere = !isOverwrite() || generatingInterface
                    || !getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
            boolean attributeIsOrOverridesDerivedOnTheFly = isDerivedOnTheFly() || isOverwritingDerivedOnTheFly();
            boolean changesDatatype = isOverwrite() && !isSameDatatypeAsOverwritten();
            return getterIsDefinedHere || attributeIsOrOverridesDerivedOnTheFly || isOverwriteAbstract()
                    || changesDatatype;
        }
    }

    public boolean isSameDatatypeAsOverwritten() {
        ValueDatatype datatype = getDatatype();
        ValueDatatype overwrittenDatatype = getOverwrittenAttribute().getDatatype();
        return datatype instanceof EnumTypeDatatypeAdapter enumtype
                && overwrittenDatatype instanceof EnumTypeDatatypeAdapter overwrittenEnumtype
                        ? Objects.equals(enumtype.getEnumType(), overwrittenEnumtype.getEnumType())
                        : Objects.equals(datatype, overwrittenDatatype);
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
     * Returns the java class name for value set with wildcard type. For example an
     * <code>ValueSet&lt;? extends AbstractEnumType&gt;</code>
     *
     * @return the class name of the value set
     */
    public String getValueSetJavaClassNameWithWildcard(GenerateValueSetType generateValueSetType) {
        return getValueSetJavaClassName(generateValueSetType, true);
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
     */
    public boolean isIpsEnum() {
        return getDatatype() instanceof EnumTypeDatatypeAdapter;
    }

    /**
     * Returns whether the datatype is an enumeration that can be queried to get all of its values.
     */
    public boolean hasAllValuesMethod() {
        return getDatatype().isEnum() && getDatatype() instanceof GenericValueDatatype generic
                && IpsStringUtils.isNotBlank(generic.getAllValuesMethodName());
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

    public boolean isOverwritingValueSetEqualType() {
        return getValueSetType()
                .equals(getOverwrittenAttribute().getValueSetType());
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
        return getModelNode(polType, XPolicyCmptClass.class);
    }

    public boolean mark(boolean flag) {
        return flag;
    }

    public String getOldValueVariable() {
        return "old" + IpsStringUtils.toUpperFirstChar(getFieldName());
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
     *             is different from the by-type name and the method is generated for
     *             {@link GenerateValueSetType#GENERATE_BY_TYPE}.
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
            return new String[] { addImport(IValidationContext.class), "context" };
        } else {
            return new String[0];
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
        return prefix + IpsStringUtils.toUpperFirstChar(getFieldName());

    }

    public String getMethodNameSetAllowedValuesFor() {
        return "setAllowedValuesFor" + IpsStringUtils.toUpperFirstChar(getFieldName());
    }

    public String getFieldNameDefaultValue() {
        return "defaultValue" + IpsStringUtils.toUpperFirstChar(getFieldName());
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
        GenericValueDatatype datatype = (GenericValueDatatype)getDatatype();
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

    private boolean isAllValuesMethodWithCollection(GenericValueDatatype datatype) {
        return datatype.getAllValuesMethod().invokeStatic("") instanceof java.util.Collection;
    }

    /**
     * Returns the name of the field defined for the value set. This field name depends on the kind
     * of value set.
     */
    public String getFieldNameValueSet() {
        if (isValueSetUnrestricted() || isValueSetConfiguredDynamic()) {
            return "setOfAllowedValues" + IpsStringUtils.toUpperFirstChar(getFieldName());
        }
        if (isValueSetRange()) {
            return "rangeFor" + IpsStringUtils.toUpperFirstChar(getFieldName());
        }
        if (isValueSetEnum()) {
            return "allowedValuesFor" + IpsStringUtils.toUpperFirstChar(getFieldName());
        }
        if (isValueSetStringLength()) {
            return "maximumLength" + IpsStringUtils.toUpperFirstChar(getFieldName());
        }
        throw new RuntimeException(NLS.bind("Attribute {0} has an invalid value set type.", getAttribute()));
    }

    public boolean isValueSetConfiguredDynamic() {
        return getAttribute().isValueSetConfiguredByProduct() && getAttribute().getValueSet().isDerived();
    }

    public String getMethodNameGetDefaultValue() {
        return getJavaNamingConvention().getGetterMethodName(
                "DefaultValue" + IpsStringUtils.toUpperFirstChar(getFieldName()),
                getDatatype());
    }

    public String getMethodNameSetDefaultValue() {
        return "setDefaultValue" + IpsStringUtils.toUpperFirstChar(getFieldName());
    }

    public String getMethodNameComputeAttribute() {
        return getAttribute().getComputationMethodSignature();
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
     *             deprecated.
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
     *             {@link ValueSetMethods#ByValueSetType}.
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
    public enum GenerateValueSetType {
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
