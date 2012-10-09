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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.XMethod;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
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
     * Returns true for all attributes except for constant and overridden attributes.
     */
    public boolean isGenerateGetter() {
        return !isConstant() && (!isOverwrite() || isDerivedOnTheFly());
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

    public boolean isGenerateInitWithProductData() {
        return isProductRelevant() && isChangeable() && !isOverwrite();
    }

    public boolean isGenerateInitPropertiesFromXML() {
        return isRequireMemberVariable();
    }

    public boolean isGenerateDefaultInitialize() {
        return isOverwrite() && isChangeable();
    }

    public XPolicyAttribute getOverwrittenAttribute() {
        if (isOverwrite()) {
            try {
                IPolicyCmptTypeAttribute overwrittenAttribute = getAttribute()
                        .findOverwrittenAttribute(getIpsProject());
                return getModelNode(overwrittenAttribute, XPolicyAttribute.class);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        } else {
            throw new RuntimeException("Attribute is not overwritten so there is no overwritten attribute for "
                    + getAttribute());
        }
    }

    /**
     * Returns the java class name for value set. For example an
     * <code>ValueSet&lt;Integer&gt;</code>
     * 
     * @return The class name of the value set
     */
    public String getValueSetJavaClassName() {
        if (isValueSetUnrestricted()) {
            if (isProductRelevant()) {
                String valueSetClass = addImport(ValueSet.class);
                return valueSetClass + "<" + getJavaClassUsedForValueSet() + ">";
            } else {
                String valueSetClass = addImport(UnrestrictedValueSet.class);
                return valueSetClass + "<" + getJavaClassUsedForValueSet() + ">";
            }
        } else if (isValueSetEnum()) {
            String valueSetClass = addImport(OrderedValueSet.class);
            return valueSetClass + "<" + getJavaClassUsedForValueSet() + ">";
        } else if (isValueSetRange()) {
            // call this method to add import statement the type
            valuesetDatatypeHelper.getJavaClassName();
            return addImport(valuesetDatatypeHelper.getRangeJavaClassName(true));
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
        return addImport(valuesetDatatypeHelper.getJavaClassName());
    }

    public String getValueSetNullValueCode() {
        JavaCodeFragment nullValueCode = valuesetDatatypeHelper.nullExpression();
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
        return getNewInstanceFromExpression(valuesetDatatypeHelper, expression, repositoryExpression);
    }

    public String getToStringExpression() {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(getFieldNameDefaultValue());
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
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
        try {
            return getIpsProject().isValueSetTypeApplicable(getDatatype(), valueSetType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getNewRangeExpression(String lowerBoundExp,
            String upperBoundExp,
            String stepExp,
            String containsNullExp) {
        JavaCodeFragment newRangeInstance = valuesetDatatypeHelper.newRangeInstance(
                new JavaCodeFragment(lowerBoundExp), new JavaCodeFragment(upperBoundExp),
                new JavaCodeFragment(stepExp), new JavaCodeFragment(containsNullExp), false);
        addImport(newRangeInstance.getImportDeclaration());
        return newRangeInstance.getSourcecode();
    }

    public String newEnumValueSetInstance(String valueCollection, String containsNullExpression) {
        JavaCodeFragment newEnumExpression = valuesetDatatypeHelper.newEnumValueSetInstance(new JavaCodeFragment(
                valueCollection), new JavaCodeFragment(containsNullExpression), true);
        addImport(newEnumExpression.getImportDeclaration());
        return newEnumExpression.getSourcecode();
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

    public XMethod getFormulaSignature() {
        IProductCmptTypeMethod method = getComputationMethod();
        return getModelNode(method, XMethod.class);
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
        if (isChangeable()) {
            if (isValueSetUnrestricted() && !isProductRelevant()) {
                return false;
            }
            if (isValueSetEnum() && isDatatypeContentSeparatedEnum()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isGenerateConstantForValueSet() {
        return !isAbstractValueSet() && (isValueSetRange() || isValueSetEnum());
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

    private boolean isValueSetOfType(ValueSetType valueSetType) {
        return getAttribute().getValueSet().getValueSetType() == valueSetType;
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
        return (isChangeable() || isDerivedByExplicitMethodCall()) && !isOverwrite();
    }

    protected boolean isDerivedByExplicitMethodCall() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    protected boolean isDerivedOnTheFly() {
        return getAttribute().getAttributeType() == AttributeType.DERIVED_ON_THE_FLY;
    }

    /**
     * Returns the name of the type where this interface is defined. For published attributes this
     * is the name of the interface (if there are any generated) for public interfaces it is the
     * name of the implementation.
     * 
     */
    public String getTypeName() {
        return getPolicyCmptNode().getSimpleName(
                BuilderAspect.getValue(isGeneratePublishedInterfaces() && isPublished()));
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

    public String getFieldNameDefaultValue() {
        return "defaultValue" + StringUtils.capitalize(getFieldName());
    }

    public String getConstantNameValueSet() {
        String name = getName();
        if (isGenerateSeparatedCamelCase()) {
            name = StringUtil.camelCaseToUnderscore(name, false);
        }
        String constName = StringUtils.upperCase(name);
        if (isValueSetEnum()) {
            return "MAX_ALLOWED_VALUES_FOR_" + constName;
        }
        if (isValueSetRange()) {
            return "MAX_ALLOWED_RANGE_FOR_" + constName;
        }
        throw new RuntimeException("Can't handle value set " + getAttribute().getValueSet());
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
            JavaCodeFragment containsNullFrag = new JavaCodeFragment();
            containsNullFrag.append(range.getContainsNull());
            result = valuesetDatatypeHelper.newRangeInstance(createCastExpression(range.getLowerBound()),
                    createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()),
                    containsNullFrag, true);
        } else if (isValueSetEnum()) {
            String[] valueIds;
            boolean containsNull;
            if ((getAttribute()).getValueSet() instanceof IEnumValueSet) {
                IEnumValueSet set = (IEnumValueSet)(getAttribute()).getValueSet();
                valueIds = set.getValues();
                containsNull = !getDatatype().isPrimitive() && set.getContainsNull();
            } else if (getDatatype() instanceof EnumDatatype) {
                valueIds = ((EnumDatatype)getDatatype()).getAllValueIds(true);
                containsNull = !getDatatype().isPrimitive();
            } else {
                throw new IllegalArgumentException("This method is only applicable to attributes "
                        + "based on an EnumDatatype or containing an EnumValueSet.");
            }
            result = valuesetDatatypeHelper.newEnumValueSetInstance(valueIds, containsNull, true);
        } else {
            throw new RuntimeException("Can't handle value set " + getAttribute().getValueSet());
        }
        addImport(result.getImportDeclaration());
        return result.getSourcecode();
    }

    private JavaCodeFragment createCastExpression(String bound) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (StringUtils.isEmpty(bound) && !valuesetDatatypeHelper.getDatatype().hasNullObject()) {
            frag.append('(');
            frag.appendClassName(valuesetDatatypeHelper.getJavaClassName());
            frag.append(')');
        }
        frag.append(valuesetDatatypeHelper.newInstance(bound));
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
        if (enumDatatype.getEnumType().isContainingValues()) {
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
        throw new RuntimeException(NLS.bind("Attribute {0} has an invalid value set type.", getAttribute()));
    }

    public String getMethodNameGetDefaultValue() {
        return getJavaNamingConvention().getGetterMethodName("DefaultValue" + StringUtils.capitalize(getFieldName()),
                getDatatype());
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
     * Returns the getValueByIdentifier code if and only if this attribute's datatype is an enum
     * type with separate content.
     * 
     * @throws NullPointerException if this attribute's datatype is no enum of if there are no
     *             separate contents.
     */
    public String getValueByIdentifier(String expression, String repositoryExpression) {
        try {
            EnumTypeDatatypeHelper enumHelper = getDatatypeHelperForContentSeparatedEnum();
            JavaCodeFragment valueByIdentifierFragment = enumHelper.getEnumTypeBuilder()
                    .getCallGetValueByIdentifierCodeFragment(enumHelper.getEnumType(), expression,
                            new JavaCodeFragment(repositoryExpression));
            return valueByIdentifierFragment.getSourcecode();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns the {@link EnumTypeDatatypeHelper} if and only if this attribute's datatype is an
     * enum type with separate content.
     * 
     * @throws NullPointerException if this attribute's datatype is no enum of if there are no
     *             separate contents.
     */
    private EnumTypeDatatypeHelper getDatatypeHelperForContentSeparatedEnum() {
        if (getDatatypeHelper() instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)getDatatypeHelper();
            if (!enumHelper.getEnumType().isContainingValues()) {
                return enumHelper;
            }
        }
        throw new NullPointerException(NLS.bind("The datatype of attribute {0} is no enum type with separate content.",
                getAttribute()));
    }

    /**
     * This method returns the qualified name of the java class name corresponding to the data type.
     * There is no need to use this qualified name anywhere but we need to be exactyl compatible to
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
     * There is no need to use this qualified name anywhere but we need to be exactyl compatible to
     * old code generator.
     * <p>
     * TODO Remove this method an its call in DefaultAndAllowedValues#writeAttributeToXML
     * 
     */
    public String getJavaClassQualifiedNameUsedForValueSet() {
        return valuesetDatatypeHelper.getJavaClassName();
    }

}
