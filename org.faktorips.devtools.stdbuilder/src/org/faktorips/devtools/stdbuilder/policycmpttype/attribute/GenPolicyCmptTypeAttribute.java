/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.Range;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.OrderedValueSet;
import org.w3c.dom.Element;

/**
 * Abstract code generator for an <tt>IPolicyCmptTypeAttribute</tt>.
 * 
 * @author Jan Ortmann
 */
public abstract class GenPolicyCmptTypeAttribute extends GenAttribute {

    public static final String JAVA4_CLASS_EnumValueSet = "org.faktorips.valueset.EnumValueSet";

    protected final DatatypeHelper valuesetDatatypeHelper;

    private IProductCmptType productCmptType;

    private EnumTypeBuilder enumTypeBuilder;

    public GenPolicyCmptTypeAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) {
        super(genPolicyCmptType, a, new LocalizedStringsSet(GenPolicyCmptTypeAttribute.class));
        valuesetDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(a.getIpsProject(), getDatatypeHelper());
    }

    public void setEnumTypeBuilder(EnumTypeBuilder enumTypeBuilder) {
        this.enumTypeBuilder = enumTypeBuilder;
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
        String expr = propMapName + ".get(\"" + getAttribute().getName() + "\")";
        builder.append("this.").append(getMemberVarName() + " = ");
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
            return Java5ClassNames.ValueSet_QualifiedName + '<' + valuesetDatatypeHelper.getJavaClassName() + '>';

        }
        if (valueSet.isRange()) {
            return valuesetDatatypeHelper.getRangeJavaClassName(isUseTypesafeCollections());
        }
        if (valueSet.isEnum()) {
            return Java5ClassNames.OrderedValueSet_QualifiedName + '<' + valuesetDatatypeHelper.getJavaClassName()
                    + '>';
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

    public void generateExtractFromXML(JavaCodeFragmentBuilder builder) throws CoreException {
        generateGetElementFromConfigMapAndIfStatement(getAttribute().getName(), builder);
        generateExtractValueFromXml(getFieldNameDefaultValue(), getDatatypeHelper(), builder);
        generateExtractValueSetFromXml(this, getDatatypeHelper(), builder);
        builder.closeBracket(); // close if statement generated three lines above
    }

    private void generateGetElementFromConfigMapAndIfStatement(String attributeName, JavaCodeFragmentBuilder builder) {
        builder.append("configElement = configMap.get(\""); //$NON-NLS-1$
        builder.append(attributeName);
        builder.appendln("\");"); //$NON-NLS-1$
        builder.append("if (configElement != null) "); //$NON-NLS-1$
        builder.openBracket();
    }

    private void generateExtractValueFromXml(String memberVar, DatatypeHelper helper, JavaCodeFragmentBuilder builder)
            throws CoreException {

        builder.append("value = "); //$NON-NLS-1$
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".getValueFromElement(configElement, \"Value\");"); //$NON-NLS-1$
        builder.append(memberVar);
        builder.append(" = "); //$NON-NLS-1$
        builder.append(getCodeToGetValueFromExpression(helper, "value")); //$NON-NLS-1$
        builder.appendln(";"); //$NON-NLS-1$
    }

    private JavaCodeFragment getCodeToGetValueFromExpression(DatatypeHelper helper, String expression)
            throws CoreException {

        if (helper instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)helper;
            IEnumType enumType = enumHelper.getEnumType();
            if (!enumType.isContainingValues()) {
                return enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(enumType, expression,
                        new JavaCodeFragment("getRepository()")); //$NON-NLS-1$
            }
        }
        return helper.newInstanceFromExpression(expression);
    }

    private void generateExtractValueSetFromXml(GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder builder) throws CoreException {

        ValueSetType valueSetType = genPolicyCmptTypeAttribute.getValueSet().getValueSetType();
        JavaCodeFragment frag = new JavaCodeFragment();
        helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsProject(), helper);
        if (valueSetType.isRange()) {
            generateExtractRangeFromXml(genPolicyCmptTypeAttribute, helper, frag);
        } else if (valueSetType.isEnum()) {
            generateExtractEnumSetFromXml(genPolicyCmptTypeAttribute, helper, frag);
        } else if (valueSetType.isUnrestricted()) {
            generateExtractAnyValueSetFromXml(genPolicyCmptTypeAttribute, helper, frag);
        }
        builder.append(frag);
    }

    private void generateExtractAnyValueSetFromXml(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        generateInitValueSetVariable(attribute, helper, frag);
        generateExtractEnumSetFromXml(attribute, helper, frag);
        if (getIpsProject().isValueSetTypeApplicable(attribute.getDatatype(), ValueSetType.RANGE)) {
            generateExtractRangeFromXml(attribute, helper, frag);
        }
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateInitValueSetVariable(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        frag.append(attribute.getFieldNameSetOfAllowedValues());
        frag.append(" = "); //$NON-NLS-1$
        if (helper.getDatatype().isEnum()) {
            if (helper.getDatatype() instanceof EnumTypeDatatypeAdapter) {
                EnumTypeDatatypeAdapter enumAdapter = (EnumTypeDatatypeAdapter)helper.getDatatype();
                generateCreateValueSetContainingAllEnumValueForFipsEnumDatatype(attribute, helper, enumAdapter, frag);
            } else {
                generateCreateValueSetContainingAllEnumValueForRegisteredEnumClass(attribute, helper, frag);
            }
        } else {
            generateCreateUnrestrictedValueSet(helper, frag);
        }
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateCreateUnrestrictedValueSet(DatatypeHelper helper, JavaCodeFragment frag) {
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(UnrestrictedValueSet.class);
        frag.append("<"); //$NON-NLS-1$
        frag.appendClassName(helper.getJavaClassName());
        frag.append(">"); //$NON-NLS-1$
        frag.append("();"); //$NON-NLS-1$
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateCreateValueSetContainingAllEnumValueForFipsEnumDatatype(@SuppressWarnings("unused") GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            EnumTypeDatatypeAdapter enumAdapter,
            JavaCodeFragment frag) throws CoreException {

        String javaEnumName = getEnumTypeBuilder().getQualifiedClassName(enumAdapter.getEnumType());
        JavaCodeFragment code = new JavaCodeFragment();
        if (enumAdapter.getEnumType().isContainingValues()) {
            code.appendClassName(Arrays.class);
            code.append(".asList("); //$NON-NLS-1$
            code.appendClassName(javaEnumName);
            code.append(".values())"); //$NON-NLS-1$
        } else {
            code.append("getRepository().getEnumValues("); //$NON-NLS-1$
            code.appendClassName(javaEnumName);
            code.append(".class)"); //$NON-NLS-1$
        }
        frag.append(helper.newEnumValueSetInstance(code, new JavaCodeFragment("true"), isUseTypesafeCollections())); //$NON-NLS-1$
        frag.append(";"); //$NON-NLS-1$
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateCreateValueSetContainingAllEnumValueForRegisteredEnumClass(@SuppressWarnings("unused") GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) {
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(UnrestrictedValueSet.class);
        frag.append("<"); //$NON-NLS-1$
        frag.appendClassName(helper.getJavaClassName());
        frag.append(">"); //$NON-NLS-1$
        frag.append("();"); //$NON-NLS-1$
    }

    private void generateExtractEnumSetFromXml(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        frag.appendClassName(EnumValues.class);
        frag.append(" values = "); //$NON-NLS-1$
        frag.appendClassName(ValueToXmlHelper.class);
        frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");"); //$NON-NLS-1$
        frag.append("if (values != null)"); //$NON-NLS-1$
        frag.appendOpenBracket();
        frag.appendClassName(ArrayList.class);
        frag.append("<"); //$NON-NLS-1$
        frag.appendClassName(helper.getJavaClassName());
        frag.append(">"); //$NON-NLS-1$
        frag.append(" enumValues = new "); //$NON-NLS-1$
        frag.appendClassName(ArrayList.class);
        frag.append("<"); //$NON-NLS-1$
        frag.appendClassName(helper.getJavaClassName());
        frag.append(">"); //$NON-NLS-1$
        frag.append("();"); //$NON-NLS-1$
        frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)"); //$NON-NLS-1$
        frag.appendOpenBracket();
        frag.append("enumValues.add("); //$NON-NLS-1$
        frag.append(getCodeToGetValueFromExpression(helper, "values.getValue(i)")); //$NON-NLS-1$
        frag.appendln(");"); //$NON-NLS-1$
        frag.appendCloseBracket();
        frag.append(attribute.getFieldNameSetOfAllowedValues());
        frag.append(" = "); //$NON-NLS-1$
        frag.append(helper.newEnumValueSetInstance(new JavaCodeFragment("enumValues"), new JavaCodeFragment( //$NON-NLS-1$
                "values.containsNull()"), isUseTypesafeCollections())); //$NON-NLS-1$
        frag.appendln(";"); //$NON-NLS-1$
        frag.appendCloseBracket();
    }

    private void generateExtractRangeFromXml(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        frag.appendClassName(Range.class);
        frag.append(" range = "); //$NON-NLS-1$
        frag.appendClassName(ValueToXmlHelper.class);
        frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");"); //$NON-NLS-1$
        frag.append("if (range != null)"); //$NON-NLS-1$
        frag.appendOpenBracket();
        frag.append(attribute.getFieldNameSetOfAllowedValues());
        frag.append(" = "); //$NON-NLS-1$
        JavaCodeFragment newRangeInstanceFrag = helper.newRangeInstance(new JavaCodeFragment("range.getLower()"), //$NON-NLS-1$
                new JavaCodeFragment("range.getUpper()"), new JavaCodeFragment("range.getStep()"), //$NON-NLS-1$ //$NON-NLS-2$
                new JavaCodeFragment("range.containsNull()"), isUseTypesafeCollections()); //$NON-NLS-1$
        if (newRangeInstanceFrag == null) {
            throw new CoreException(new IpsStatus("The " + helper + " for the datatype " //$NON-NLS-1$ //$NON-NLS-2$
                    + helper.getDatatype().getName() + " doesn't support ranges.")); //$NON-NLS-1$
        }
        frag.append(newRangeInstanceFrag);
        frag.appendln(";"); //$NON-NLS-1$
        frag.appendCloseBracket();
    }

    /**
     * Generate toXML() code for the current policy component type attribute, including default
     * value and value set.
     * 
     * @param builder the fragment builder to append source code to
     */
    public void generateWriteToXML(JavaCodeFragmentBuilder builder) {
        builder.append(" configElement = element.getOwnerDocument().createElement(\"ConfigElement\");");

        builder.append("configElement.setAttribute(\"attribute\", \"");
        builder.append(getAttribute().getName());
        builder.append("\");");
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".addValueToElement(");
        builder.append(getDatatypeHelper().getToStringExpression(getFieldNameDefaultValue()));
        builder.append(", configElement, \"Value\");");

        generateWriteValueSetToXml(builder);

        builder.append("element.appendChild(configElement);");
    }

    private void generateWriteValueSetToXml(JavaCodeFragmentBuilder builder) {
        builder.append(" valueSetElement= element.getOwnerDocument().createElement(\"ValueSet\");");
        /*
         * Set abstract flag to false, as value sets can never be abstract at runtime. (A value set
         * is abstract if it does not define concrete values, only the type of value set, e.g. range
         * or enum)
         */
        builder.append("valueSetElement.setAttribute(\"abstract\", \"false\");");
        IValueSet valueSet = getValueSet();
        if (valueSet.isUnrestricted()) {
            // the ProductCmpt could define any type of value set
            generateCodeForUnrestrictedToXML(builder, true);
            generateCodeForRangeToXML(builder, true);
            generateCodeForEnumToXML(builder, true);
        } else if (valueSet.isRange()) {
            generateCodeForRangeToXML(builder, false);
        } else if (valueSet.isEnum()) {
            generateCodeForEnumToXML(builder, false);
        }
        builder.append("configElement.appendChild(valueSetElement);");
    }

    private void generateCodeForUnrestrictedToXML(JavaCodeFragmentBuilder builder, boolean generateInstanceOf) {
        if (generateInstanceOf) {
            builder.append("if (");
            builder.append(getFieldNameSetOfAllowedValues());
            builder.append(" instanceof ");
            builder.appendClassName(org.faktorips.valueset.UnrestrictedValueSet.class);
            builder.append(") {");
        }
        builder.appendClassName(Element.class);
        builder.append(" unrestrictedValueSetElement = element.getOwnerDocument().createElement(\"Unrestricted\");");
        builder.appendClassName(Element.class);
        builder.append(" valueElement = unrestrictedValueSetElement.getOwnerDocument().createElement(\"AllValues\");");
        builder.append("unrestrictedValueSetElement.appendChild(valueElement);");
        builder.append("valueSetElement.appendChild(unrestrictedValueSetElement);");
        if (generateInstanceOf) {
            builder.append("}");
        }
    }

    private void generateCodeForRangeToXML(JavaCodeFragmentBuilder builder, boolean generateInstanceOf) {
        if (generateInstanceOf) {
            builder.append("if (");
            builder.append(getFieldNameSetOfAllowedValues());
            builder.append(" instanceof ");
            builder.appendClassName(org.faktorips.valueset.Range.class);
            builder.append(") {");
            /*
             * Cast unrestricted valueSet to range to access its fields.
             */
            builder.appendClassName(org.faktorips.valueset.Range.class);
            appendGenericDatatypeClassname(builder);
            builder.append(" range= (");
            builder.appendClassName(org.faktorips.valueset.Range.class);
            appendGenericDatatypeClassname(builder);
            builder.append(")");
            builder.append(getFieldNameSetOfAllowedValues());
            builder.append(";");
        }
        builder.append("valueSetValuesElement = element.getOwnerDocument().createElement(\"Range\");");
        builder.append("valueSetValuesElement.setAttribute(\"containsNull\", Boolean.toString(");
        builder.append(getFieldNameSetOfAllowedValues());
        builder.append(".containsNull()));");
        generateWriteValueToXML(builder, generateRangeValueToString(".getLowerBound()", generateInstanceOf),
                "valueSetValuesElement", "LowerBound");
        generateWriteValueToXML(builder, generateRangeValueToString(".getUpperBound()", generateInstanceOf),
                "valueSetValuesElement", "UpperBound");
        // generate code even though step is always null at runtime (as of 09.2011)
        generateWriteValueToXML(builder, generateRangeValueToString(".getStep()", generateInstanceOf),
                "valueSetValuesElement", "Step");
        builder.append("valueSetElement.appendChild(valueSetValuesElement);");
        if (generateInstanceOf) {
            builder.append("}");
        }
    }

    protected void appendGenericDatatypeClassname(JavaCodeFragmentBuilder builder) {
        builder.append("<");
        // Convert primitive data type names to Object names (e.g. int to Integer)
        DatatypeHelper helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsProject(), getDatatypeHelper());
        builder.append(helper.getJavaClassName());
        builder.append(">");
    }

    protected String generateRangeValueToString(String getter, boolean generateInstanceOf) {
        String fieldName = generateInstanceOf ? "range" : getFieldNameSetOfAllowedValues();
        return getDatatypeHelper().getToStringExpression(fieldName + getter).toString();
    }

    protected void generateWriteValueToXML(JavaCodeFragmentBuilder builder,
            String value,
            String elementName,
            String tagName) {
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".addValueToElement(");
        builder.append(value);
        builder.append(", ");
        builder.append(elementName);
        builder.append(", \"");
        builder.append(tagName);
        builder.append("\");");
    }

    private void generateCodeForEnumToXML(JavaCodeFragmentBuilder builder, boolean generateInstanceOf) {
        if (generateInstanceOf) {
            builder.append("if (");
            builder.append(getFieldNameSetOfAllowedValues());
            builder.append(" instanceof ");
            builder.appendClassName(OrderedValueSet.class);
            builder.append("){");
            // builder.append("<");
            // builder.append(datatype.getJavaClassName());
            // builder.append(">) {");
        }
        builder.append("valueSetValuesElement = element.getOwnerDocument().createElement(\"Enum\");");
        builder.append("valueSetValuesElement.setAttribute(\"containsNull\", Boolean.toString(");
        builder.append(getFieldNameSetOfAllowedValues());
        builder.append(".containsNull()));");
        builder.append("for (");
        builder.append(getDatatype().getJavaClassName());
        builder.append(" value : ");
        builder.append(getFieldNameSetOfAllowedValues());
        builder.append(".getValues(true)) {");
        builder.appendClassName(Element.class);
        builder.append(" valueElement = element.getOwnerDocument().createElement(\"Value\");");
        generateWriteValueToXML(builder, getDatatypeHelper().getToStringExpression("value").toString(), "valueElement",
                "Data");
        builder.append("valueSetValuesElement.appendChild(valueElement);");
        builder.append("}");
        builder.append("valueSetElement.appendChild(valueSetValuesElement);");
        if (generateInstanceOf) {
            builder.append("}");
        }
    }

    private JavaSourceFileBuilder getEnumTypeBuilder() {
        return enumTypeBuilder;
    }

}
