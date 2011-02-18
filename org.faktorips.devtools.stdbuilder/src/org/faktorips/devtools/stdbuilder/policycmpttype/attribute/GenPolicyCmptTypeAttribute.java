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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.ValueSet;

/**
 * Abstract code generator for an <tt>IPolicyCmptTypeAttribute</tt>.
 * 
 * @author Jan Ortmann
 */
public abstract class GenPolicyCmptTypeAttribute extends GenAttribute {

    public static final String JAVA4_CLASS_EnumValueSet = "org.faktorips.valueset.EnumValueSet";

    protected final DatatypeHelper valuesetDatatypeHelper;

    private IProductCmptType productCmptType;

    public GenPolicyCmptTypeAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) {

        super(genPolicyCmptType, a, new LocalizedStringsSet(GenPolicyCmptTypeAttribute.class));
        valuesetDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(a.getIpsProject(), getDatatypeHelper());
    }

    @Override
    public void generate(boolean generatesInterface, IIpsProject ipsProject, TypeSection mainSection)
            throws CoreException {

        if (generatesInterface && !(isPublished())) {
            return;
        }
        super.generate(generatesInterface, ipsProject, mainSection);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Money getPremium();
     * </pre>
     */
    protected void generateGetterInterface(JavaCodeFragmentBuilder builder) {
        String description = StringUtils.isEmpty(getDescriptionInGeneratorLanguage(getAttribute())) ? ""
                : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR
                        + getDescriptionInGeneratorLanguage(getAttribute());

        String[] replacements = new String[] { getAttribute().getName(), description };
        appendLocalizedJavaDoc("METHOD_GETVALUE", replacements, builder);
        generateGetterSignature(builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Money getPremium() {
     *     return premium;
     * }
     * </pre>
     */
    protected void generateGetterImplementation(JavaCodeFragmentBuilder methodsBuilder) {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (isPublished()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), true);
        }
        generateGetterSignature(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getDatatypeHelper().referenceOrSafeCopyIfNeccessary(getMemberVarName()));
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }

    public void generateDeltaComputation(JavaCodeFragmentBuilder methodsBuilder, String deltaVar, String otherVar) {

        methodsBuilder.append(deltaVar);
        methodsBuilder.append('.');
        methodsBuilder.append(MethodNames.MODELOBJECTDELTA_CHECK_PROPERTY_CHANGE);
        methodsBuilder.append("(");
        methodsBuilder.appendClassName(getGenType().getQualifiedName(true));
        methodsBuilder.append(".");
        methodsBuilder.append(getStaticConstantPropertyName());
        methodsBuilder.append(", ");
        methodsBuilder.append(getMemberVarName());
        methodsBuilder.append(", ");
        methodsBuilder.append(otherVar);
        methodsBuilder.append(".");
        methodsBuilder.append(getMemberVarName());
        methodsBuilder.appendln(", options);");
    }

    public void generateInitPropertiesFromXml(JavaCodeFragmentBuilder builder, JavaCodeFragment repositoryExpression)
            throws CoreException {

        String propMapName = "propMap";
        builder.append("if (" + propMapName + ".containsKey(");
        builder.appendQuoted(getAttribute().getName());
        builder.appendln(")) {");
        String expr = (isUseTypesafeCollections() ? "" : "(String)") + propMapName + ".get(\""
                + getAttribute().getName() + "\")";
        builder.append(getMemberVarName() + " = ");
        if (getDatatypeHelper() instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)getDatatypeHelper();
            if (!enumHelper.getEnumType().isContainingValues()) {
                builder.append(enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(
                        enumHelper.getEnumType(), expr, repositoryExpression));
                builder.appendln(";");
                builder.appendln("}");
                return;
            }
        }
        builder.append(getDatatypeHelper().newInstanceFromExpression(expr));
        builder.appendln(";");
        builder.appendln("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Money getPremium()
     * </pre>
     */
    protected void generateGetterSignature(JavaCodeFragmentBuilder methodsBuilder) {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(getAttribute().getName(), getDatatype());
        methodsBuilder.signature(modifier, getJavaClassName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    protected void generateField(JavaCodeFragmentBuilder memberVarsBuilders) {
        JavaCodeFragment initialValueExpression = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        String comment = getLocalizedText("FIELD_ATTRIBUTE_VALUE_JAVADOC", getAttribute().getName());
        String fieldName = getMemberVarName();

        memberVarsBuilders.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);

        getGenType().getBuilderSet().addAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD,
                getAttribute(), memberVarsBuilders);
        getGenType().getBuilderSet().addAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD,
                getAttribute(), memberVarsBuilders);

        memberVarsBuilders.varDeclaration(java.lang.reflect.Modifier.PRIVATE, getJavaClassName(), fieldName,
                initialValueExpression);
    }

    /**
     * Returns <code>true</code> if a member variable is required to the type of attribute. This is
     * currently the case for changeable attributes and attributes that are derived by an explicit
     * method call.
     */
    public boolean isMemberVariableRequired() {
        return ((getAttribute()).isChangeable() || isDerivedByExplicitMethodCall()) && !getAttribute().isOverwrite();
    }

    public boolean needsToBeConsideredInDeltaComputation() {
        return isPublished() && isMemberVariableRequired() && !getAttribute().isOverwrite();
    }

    public String getFieldNameDefaultValue() {
        return getJavaNamingConvention().getMemberVarName(getPropertyNameDefaultValue());
    }

    String getPropertyNameDefaultValue() {
        return getLocalizedText("PROPERTY_DEFAULTVALUE_NAME", StringUtils.capitalize(getAttribute().getName()));
    }

    public IValueSet getValueSet() {
        return (getAttribute()).getValueSet();
    }

    /**
     * Returns the fully qualified name of the Java type that represents the given value set in the
     * generated code.
     */
    protected String getJavaTypeForValueSet(IValueSet valueSet) {
        if (valueSet.isUnrestricted()) {
            if (isUseTypesafeCollections()) {
                return Java5ClassNames.ValueSet_QualifiedName + '<' + valuesetDatatypeHelper.getJavaClassName() + '>';
            } else {
                return ValueSet.class.getName();
            }
        }
        if (valueSet.isRange()) {
            return valuesetDatatypeHelper.getRangeJavaClassName(isUseTypesafeCollections());
        }
        if (valueSet.isEnum()) {
            if (isUseTypesafeCollections()) {
                return Java5ClassNames.OrderedValueSet_QualifiedName + '<' + valuesetDatatypeHelper.getJavaClassName()
                        + '>';
            } else {
                return JAVA4_CLASS_EnumValueSet;
            }
        }
        throw new RuntimeException("Can't handle value set " + valueSet);
    }

    public String getFieldNameSetOfAllowedValues() {
        String lookup = getLookupPrefixForFieldSetOfAllowedValues() + "_NAME";
        return getLocalizedText(lookup, StringUtils.capitalize(getAttribute().getName()));
    }

    protected String getLookupPrefixForFieldSetOfAllowedValues() {
        if (getValueSet().isRange()) {
            return "FIELD_RANGE_FOR";
        }
        if (getValueSet().isEnum()) {
            return "FIELD_ALLOWED_VALUES_FOR";
        }
        if (getValueSet().isUnrestricted()) {
            return "FIELD_SET_OF_ALLOWED_VALUES_FOR";
        }
        throw new RuntimeException("Can't handle value set " + getValueSet());
    }

    /**
     * Returns the name of the method to access an attribute's set of allowed ENUM values.
     */
    public String getMethodNameGetSetOfAllowedValues() {
        String lookup = getLookupPrefixForMethodNameGetSetOfAllowedValues() + "_NAME";
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText(lookup, StringUtils.capitalize(getAttribute().getName())),
                valuesetDatatypeHelper.getDatatype());
    }

    protected String getLookupPrefixForMethodNameGetSetOfAllowedValues() {
        if (getValueSet().isRange()) {
            return "METHOD_GET_RANGE_FOR";
        }
        if (getValueSet().isEnum()) {
            return "METHOD_GET_ALLOWED_VALUES_FOR";
        }
        if (getValueSet().isUnrestricted()) {
            return "METHOD_GET_SET_OF_ALLOWED_VALUES_FOR";
        }
        throw new RuntimeException("Can't handle value set " + getValueSet());
    }

    public GenPolicyCmptTypeAttribute getGeneratorForOverwrittenAttribute() throws CoreException {
        if (!getAttribute().isOverwrite()) {
            return null;
        }
        IPolicyCmptTypeAttribute overwritten = (getAttribute())
                .findOverwrittenAttribute(getAttribute().getIpsProject());
        if (overwritten == null) {
            return null;
        }
        GenPolicyCmptType typeGenerator = new GenPolicyCmptType(overwritten.getPolicyCmptType(), getGenType()
                .getBuilderSet());
        return typeGenerator.getGenerator(overwritten);
    }

    protected IProductCmptType getProductCmptType(IIpsProject ipsProject) throws CoreException {
        if (productCmptType == null) {
            productCmptType = (getAttribute()).getPolicyCmptType().findProductCmptType(ipsProject);
        }
        return productCmptType;
    }

    public boolean isProductRelevant() {
        return (getAttribute()).isProductRelevant();
    }

    public boolean isDerivedOnTheFly() {
        return (getAttribute()).getAttributeType() == AttributeType.DERIVED_ON_THE_FLY;
    }

    public boolean isDerivedByExplicitMethodCall() {
        return (getAttribute()).getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    @Override
    public GenPolicyCmptType getGenType() {
        return (GenPolicyCmptType)super.getGenType();
    }

    @Override
    public IPolicyCmptTypeAttribute getAttribute() {
        return (IPolicyCmptTypeAttribute)super.getAttribute();
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (!(isPublished())) {
            IField propertyConstant = generatedJavaType.getField(getStaticConstantPropertyName());
            javaElements.add(propertyConstant);
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (isPublished()) {
            IField propertyConstant = generatedJavaType.getField(getStaticConstantPropertyName());
            javaElements.add(propertyConstant);
        }
    }

}
