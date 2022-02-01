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
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
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
     * Returns true for all attributes except for derived on the fly, constant, overridden and
     * abstract derived by explicit method call attributes.
     */
    public boolean isGenerateSetter() {
        boolean noDuplicateOverwrite = !isOverwrite() || isAttributeTypeChangedByOverwrite() || isOverwriteAbstract();
        boolean abstractComputed = isDerivedByExplicitMethodCall() && isAbstract();
        return !isDerivedOnTheFly() && !abstractComputed && !isConstant()
                && noDuplicateOverwrite;
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
    public String getValueSetJavaClassName() {
        return getValueSetJavaClassName(false);
    }

    /**
     * Returns the java class name for value set with wildcard type. For example an
     * <code>ValueSet&lt;? extends AbstractEnumType&gt;</code>
     * 
     * @return the class name of the value set
     */
    public String getValueSetJavaClassNameWithWildcard() {
        return getValueSetJavaClassName(true);
    }

    private String getValueSetJavaClassName(boolean useWildcards) {
        String wildcards = useWildcards ? "? extends " : "";
        if (isValueSetUnrestricted() || isValueSetDerived()) {
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
            // CSOFF: BooleanExpressionComplexity
            return (isProductRelevant() && isChangeable())
                    || !isValueSetUnrestricted()
                    || isUnrestrictedButStillNeedsAllowedValues()
                    || isNotConfiguredOverrideConfigured();
            // CSON: BooleanExpressionComplexity
        }
    }

    private boolean isUnrestrictedButStillNeedsAllowedValues() {
        boolean doesNotContainNull = !isValueSetContainingNull() && !getDatatype().isPrimitive();
        boolean overwritesDerivedValueSet = isOverwrite() && getOverwrittenAttribute().isValueSetDerived();
        return isValueSetUnrestricted() && (doesNotContainNull || overwritesDerivedValueSet);
    }

    public boolean isOverrideGetAllowedValuesFor() {
        if (!isOverwrite()) {
            return false;
        }
        boolean overwrittenAttributeSuitedForOverride = getOverwrittenAttribute()
                .isGenerateGetAllowedValuesForAndGetDefaultValue()
                && getOverwrittenAttribute().getMethodNameGetAllowedValuesFor()
                        .equals(getMethodNameGetAllowedValuesFor());
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
        // CSOFF: BooleanExpressionComplexity
        boolean isGenerateForValueSetType = isValueSetRange() || isNonExtensibleEnumValueSet()
                || isUnrestrictedButStillNeedsAllowedValues() || isValueSetStringLength()
                || isNotConfiguredOverrideConfigured();
        // CSON: BooleanExpressionComplexity
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
        return getAttribute().getValueSet().getValueSetType() == valueSetType;
    }

    private boolean isValueSetContainingNull() {
        return getAttribute().getValueSet().isContainsNull();
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

    public boolean isDerivedByExplicitMethodCall() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    public boolean isDerivedOnTheFly() {
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

    public String getMethodNameGetAllowedValuesFor() {
        String prefix;
        if (isValueSetEnum()) {
            prefix = "getAllowedValuesFor";
        } else if (isValueSetRange()) {
            prefix = "getRangeFor";
        } else {
            prefix = "getSetOfAllowedValuesFor";
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
        String name = getName();
        if (getGeneratorConfig().isGenerateSeparatedCamelCase()) {
            name = StringUtil.camelCaseToUnderscore(name, false);
        }
        String constName = StringUtils.upperCase(name);
        if (isValueSetRange()) {
            return "MAX_ALLOWED_RANGE_FOR_" + constName;
        } else if (isValueSetStringLength()) {
            return "MAX_ALLOWED_STRING_LENGTH_FOR_" + constName;
        } else {
            return "MAX_ALLOWED_VALUES_FOR_" + constName;
        }
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
     * Returns the java doc key used to localize the java doc. The key depends on the kind of the
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

}
