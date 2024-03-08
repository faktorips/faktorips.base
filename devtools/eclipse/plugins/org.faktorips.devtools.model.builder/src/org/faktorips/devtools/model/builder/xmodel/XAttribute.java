/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.util.DatatypeHelperUtil;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.DerivedValueSet;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.StringLengthValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

/**
 * Contains common behavior for product- and policy-attributes alike.
 *
 * @author widmaier
 */
public abstract class XAttribute extends AbstractGeneratorModelNode {

    public XAttribute(IAttribute attribute, GeneratorModelContext context, ModelService modelService) {
        super(attribute, context, modelService);
    }

    @Override
    public IAttribute getIpsObjectPartContainer() {
        return (IAttribute)super.getIpsObjectPartContainer();
    }

    public IAttribute getAttribute() {
        return getIpsObjectPartContainer();
    }

    public DatatypeHelper getDatatypeHelper() {
        return getDatatypeHelper(getAttribute().getDatatype());
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    public String getMethodNameSetterInternal() {
        return getJavaNamingConvention().getSetterMethodName(getName() + "Internal"); //$NON-NLS-1$
    }

    public String getMethodNameSetterInternalIfGenerateChangeSupport() {
        return getGeneratorConfig().isGenerateChangeSupport() ? getMethodNameSetterInternal() : getMethodNameSetter();
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName(), getDatatype());
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    public String fieldPropertyNameSuffix() {
        return getGeneratorConfig().isGenerateSeparatedCamelCase()
                ? StringUtil.camelCaseToUnderscore(getName()).toUpperCase()
                : getName().toUpperCase();
    }

    /**
     * Returns the source code of the default value of the chosen attribute.
     */
    public String getDefaultValueCode() {
        JavaCodeFragment newInstance = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
    }

    public final ValueDatatype getDatatype() {
        if (getDatatypeHelper() == null) {
            IAttribute attribute = getAttribute();
            throw new IpsException("When building \"" + getIpsProject().getName() + "\", the datatype \""
                    + attribute.getDatatype() + "\" of the attribute \"" + attribute.getIpsObject().getName()
                    + "#" + attribute.getName()
                    + "\" could not be found.");
        }
        return (ValueDatatype)getDatatypeHelper().getDatatype();
    }

    /**
     * Returns a string representing the java datatype for this attribute. This datatype may however
     * be a primitive or a complex type. In case of a complex type (e.g. BigDecimal) an import is
     * added and the unqualified type name is returned. In case of a primitive the name of the type
     * is returned without adding an import.
     */
    public String getJavaClassName() {
        String javaClassName = getDatatypeHelper().getJavaClassName();
        return addImport(javaClassName);
    }

    /**
     * Return the qualified java class name of the data type for this attribute.
     *
     * @see #getJavaClassName()
     */
    public String getQualifiedJavaClassName() {
        return getDatatypeHelper().getJavaClassName();
    }

    /**
     * Returns the code to create a new instance. The expression is the code to retrieve the value
     * from, e.g. another variable. The repositoryExpression is the code for getting a repository.
     * It may be needed for enumerations with separated content.
     *
     * @param expression The expression to get the value from
     * @param repositoryExpression the expression to get the repository
     * @return The code needed to create a new instance for a value set
     */
    public String getNewInstanceFromExpression(String expression, String repositoryExpression) {
        return getNewInstanceFromExpression(getDatatypeHelper(), expression, repositoryExpression);
    }

    /**
     * Returns the code to create a new instance. The expression is the code to retrieve the value
     * from, e.g. another variable. The repositoryExpression is the code for getting a repository.
     * It may be needed for enumerations with separated content.
     *
     * @param datatypeHelper The data type helper of the data type you need the new instance
     *            expression for
     * @param expression The expression to get the value from
     * @param repositoryExpression the expression to get the repository
     * @return The code needed to create a new instance for a value set
     */
    protected String getNewInstanceFromExpression(DatatypeHelper datatypeHelper,
            String expression,
            String repositoryExpression) {
        JavaCodeFragment fragment = DatatypeHelperUtil.getNewInstanceFromExpression(datatypeHelper, expression,
                repositoryExpression);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    /**
     * Returns <code>true</code> if this attributes data type is an enumeration-type with values in
     * type and separated content.
     *
     */
    public boolean isDatatypeExtensibleEnum() {
        return DatatypeUtil.isExtensibleEnumType(getDatatype());
    }

    public String getToStringExpression(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public boolean isPublished() {
        return getAttribute().getModifier().isPublished();
    }

    public boolean isOverwrite() {
        return getAttribute().isOverwrite();
    }

    public XAttribute getOverwrittenAttribute() {
        if (isOverwrite()) {
            IAttribute overwrittenAttribute = getAttribute().findOverwrittenAttribute(getIpsProject());
            return getModelNode(overwrittenAttribute, getClass());
        } else {
            throw new RuntimeException(
                    "Attribute is not overwritten so there is no overwritten attribute for " + getAttribute());
        }
    }

    public boolean isChangingOverTime() {
        return getAttribute().isChangingOverTime();
    }

    public String getReferenceOrSafeCopyIfNecessary(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().referenceOrSafeCopyIfNeccessary(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public String getConstantNamePropertyName() {
        return "PROPERTY_" + getFieldName().toUpperCase();
    }

    /**
     * Extension to {@link #getAnnotationsForPublishedInterface(AnnotatedJavaElementType, boolean)}
     * for attribute methods that are generated in either published interface or implementation
     * class depending on the {@link Modifier}. For published attributes, this method inherits the
     * behavior of {@link #getAnnotationsForPublishedInterface(AnnotatedJavaElementType, boolean)}.
     * If the attribute is not published, this method only returns annotations if the builder is
     * currently generating an implementation class.
     */
    public String getAnnotationsForPublishedInterfaceModifierRelevant(AnnotatedJavaElementType type,
            boolean isGeneratingInterface) {
        if (isPublished()) {
            return getAnnotationsForPublishedInterface(type, isGeneratingInterface);
        } else {
            if (!isGeneratingInterface) {
                return getAnnotations(type);
            } else {
                return "";
            }
        }
    }

    /**
     * Checks whether the attribute is abstract or not. The attribute is abstract if its datatype is
     * an abstract datatype.
     */
    public boolean isAbstract() {
        return getDatatype().isAbstract();
    }

    /**
     * Returns <code>true</code> if abstract getters should be generated for this attribute.
     */
    public boolean isGenerateAbstractMethods() {
        return !getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject()) && isAbstract() && !isOverwrite();
    }

    protected boolean isNonExtensibleEnumValueSet() {
        return isValueSetEnum() && !isDatatypeExtensibleEnum();
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

    public ValueSetType getValueSetType() {
        return getAttribute().getValueSet().getValueSetType();
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
     * Returns the java class name for value set. For example <code>ValueSet&lt;Integer&gt;</code>
     *
     * @return The class name of the value set
     */
    public String getValueSetJavaClassName(GenerateValueSetType generateValueSetType) {
        return getValueSetJavaClassName(generateValueSetType, false);
    }

    protected String getValueSetJavaClassName(GenerateValueSetType generateValueSetType, boolean useWildcards) {
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
        return addImport(getValuesetDatatypeHelper().getValueSetJavaClassName());
    }

    public DatatypeHelper getValuesetDatatypeHelper() {
        return DatatypeHelperUtil.getDatatypeHelperForValueSet(getAttribute().getIpsProject(), getDatatypeHelper());
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
        return constName.toUpperCase();
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
        } else if (isValueSetDerived()) {
            return newDerivedValueSetInstance();
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

    public String newDerivedValueSetInstance() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(DerivedValueSet.class);
        frag.append("<>"); //$NON-NLS-1$
        frag.append("()"); //$NON-NLS-1$
        addImport(DerivedValueSet.class);
        return frag.getSourcecode();
    }

    private JavaCodeFragment createCastExpression(String bound) {
        return getValuesetDatatypeHelper().createCastExpression(bound);
    }
}
