/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.BaseProductCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.valueset.EnumValueSet;

/**
 * Code generator for a changeable attribute.
 * 
 * @author Jan Ortmann
 */
public class GenChangeableAttribute extends GenPolicyCmptTypeAttribute {

    public GenChangeableAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) throws CoreException {
        super(genPolicyCmptType, a);
        ArgumentCheck.isTrue(a.isChangeable());
    }

    /**
     * Generates the source code for a policy component type attribute marked as product relevant.
     * The generated code is part of the code generated for the product component type (not the
     * policy component type!)
     */
    public void generateCodeForProductCmptType(boolean generatesInterface,
            IIpsProject ipsProject,
            TypeSection mainSection) throws CoreException {
        if (!generatesInterface) {
            generateMemberVariablesForProductCmptTypeImpl(mainSection.getMemberVarBuilder(), ipsProject,
                    generatesInterface);
        }
        generateMethodsForProductCmptType(mainSection.getMethodBuilder(), ipsProject, generatesInterface);
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (isPublished() == generatesInterface) {
            generateAttributeNameConstant(builder);
            if (shouldGetAllowedSetOfValuesBeGenerated()) {
                generateConstantSetOfAllowedValues(builder);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setPremium(Money newValue)
     * </pre>
     */
    protected void generateSetterSignature(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNametSetPropertyValue(getAttribute().getName(), getDatatype());
        String paramName = getParamNameForSetterMethod();
        methodsBuilder.signature(modifier, "void", methodName, new String[] { paramName },
                new String[] { getJavaClassName() });
    }

    /**
     * Generates the signature for the method to access an attribute's set of allowed ENUM values.
     */
    public void generateSignatureGetSetOfAllowedValues(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetSetOfAllowedValues();
        methodsBuilder.signature(Modifier.PUBLIC, getJavaTypeForValueSet(getValueSet()), methodName,
                new String[] { "context" }, new String[] { IValidationContext.class.getName() });
    }

    /**
     * Generates the method to access an attribute's set of allowed ENUM values. This
     * generation-method is used for the method in the policy component type AND the product
     * component type generation.
     */
    public void generateMethodGetSetOfAllowedValuesInterface(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        String lookup = getLookupPrefixForMethodNameGetSetOfAllowedValues();
        appendLocalizedJavaDoc(lookup, getAttribute().getName(), methodsBuilder);
        generateSignatureGetSetOfAllowedValues(methodsBuilder);
        methodsBuilder.append(';');
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Integer getDefaultMinAge()
     * </pre>
     */
    void generateSignatureGetDefaultValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder)
            throws CoreException {

        String methodName = getMethodNameGetDefaultValue();
        builder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the name of the method that returns the default value for the attribute.
     */
    public String getMethodNameGetDefaultValue() {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameDefaultValue(),
                getDatatypeHelper().getDatatype());
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        if (!generatesInterface) {
            if (getAttribute().isOverwrite()) {
                return;
            }
            generateField(builder);
        }
    }

    /**
     * Generates the member variables for a policy component type attribute marked as product
     * relevant. The generated methods are part of the code generated for the product component type
     * (not the policy component type!).
     */
    protected void generateMemberVariablesForProductCmptTypeImpl(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            generateFieldDefaultValue(getDatatypeHelper(), builder);
            generateFieldForTheAllowedSetOfValues(builder);
        }
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (!generatesInterface) {
            if (shouldGetAllowedSetOfValuesBeGenerated()) {
                generateMethodGetSetOfAllowedValuesPolicyCmptTypeImpl(builder, ipsProject);
            }
            if (getAttribute().isOverwrite()) {
                return;
            }
            generateGetterImplementation(builder);
            generateSetterMethod(builder);
        }

        if (generatesInterface) {
            if (shouldGetAllowedSetOfValuesBeGenerated()) {
                generateMethodGetSetOfAllowedValuesInterface(builder);
            }
            if (getAttribute().isOverwrite()) {
                return;
            }
            generateGetterInterface(builder);
            generateSetterInterface(builder);
        }
    }

    /**
     * Generates the methods for a policy component type attribute marked as product relevant. The
     * generated methods are part of the code generated for the product component type (not the
     * policy component type!).
     */
    protected void generateMethodsForProductCmptType(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        if (!generatesInterface) {
            generateMethodGetDefaultValue(getDatatypeHelper(), builder, generatesInterface);
            generateMethodGetSetOfAllowedValuesForProductCmptType(builder);
        }

        if (generatesInterface) {
            generateMethodGetDefaultValue(getDatatypeHelper(), builder, generatesInterface);
            generateMethodGetSetOfAllowedValuesInterface(builder);
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge() {
     *     return minAge;
     * </pre>
     */
    private void generateMethodGetDefaultValue(DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder,
            boolean generatesInterface) throws CoreException {

        if (!generatesInterface) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            if (((IPolicyCmptTypeAttribute)getAttribute()).isOverwrite()) {
                appendOverrideAnnotation(methodsBuilder, getIpsProject(), false);
            } else if (getAttribute().getModifier().isPublished()) {
                appendOverrideAnnotation(methodsBuilder, getIpsProject(), true);
            }
            generateSignatureGetDefaultValue(datatypeHelper, methodsBuilder);
            methodsBuilder.openBracket();
            methodsBuilder.append("return ");
            methodsBuilder.append(getFieldNameDefaultValue());
            methodsBuilder.append(';');
            methodsBuilder.closeBracket();
        }

        if (generatesInterface) {
            appendLocalizedJavaDoc("METHOD_GET_DEFAULTVALUE", getAttribute().getName(), methodsBuilder);
            generateSignatureGetDefaultValue(datatypeHelper, methodsBuilder);
            methodsBuilder.append(';');
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setPremium(Money newValue) {
     *     this.premium = newValue;
     * }
     * </pre>
     */
    protected void generateSetterMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (isPublished()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), true);
        }
        generateSetterSignature(methodsBuilder);
        methodsBuilder.openBracket();
        ((GenPolicyCmptType)getGenType()).generateChangeListenerSupportBeforeChange(methodsBuilder,
                ChangeEventType.MUTABLE_PROPERTY_CHANGED, getDatatype().getJavaClassName(), getMemberVarName(),
                getParamNameForSetterMethod(), getStaticConstantPropertyName());
        methodsBuilder.append("this.");
        methodsBuilder.append(getMemberVarName());
        methodsBuilder.append(" = ");
        methodsBuilder.append(getDatatypeHelper().referenceOrSafeCopyIfNeccessary(getParamNameForSetterMethod()));
        methodsBuilder.appendln(";");
        ((GenPolicyCmptType)getGenType()).generateChangeListenerSupportAfterChange(methodsBuilder,
                ChangeEventType.MUTABLE_PROPERTY_CHANGED, getDatatype().getJavaClassName(), getMemberVarName(),
                getParamNameForSetterMethod(), getStaticConstantPropertyName());
        methodsBuilder.closeBracket();
    }

    private void generateGenerationAccess(JavaCodeFragment body, IIpsProject ipsProject) throws CoreException {
        GenProductCmptType genProductCmptType = getGenType().getBuilderSet().getGenerator(
                getProductCmptType(ipsProject));
        if (isPublished()) {
            body.append(genProductCmptType.getMethodNameGetProductCmptGeneration());
            body.append("().");
        } else { // Public
            body.append("((");
            body.append(getProductCmptType(ipsProject).getName()
                    + ((GenPolicyCmptType)getGenType()).getAbbreviationForGenerationConcept());
            body.append(")");
            body.append(genProductCmptType.getMethodNameGetProductCmptGeneration());
            body.append("()).");
        }
    }

    private void generateMethodGetSetOfAllowedValuesPolicyCmptTypeImpl(JavaCodeFragmentBuilder methodBuilder,
            IIpsProject ipsProject) throws CoreException {

        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        GenChangeableAttribute genOverwritten = getGeneratorForOverwrittenChangeableAttribute();
        if (genOverwritten != null) {
            if (genOverwritten.shouldGetAllowedSetOfValuesBeGenerated()) {
                appendOverrideAnnotation(methodBuilder, getIpsProject(), false);
            }
        } else if (getAttribute().getModifier().isPublished()) {
            appendOverrideAnnotation(methodBuilder, getIpsProject(), true);
        }
        generateSignatureGetSetOfAllowedValues(methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (isProductRelevant()) {
            generateGenerationAccess(body, ipsProject);
            body.append(getMethodNameGetSetOfAllowedValues());
            body.appendln("(context);");
        } else {
            if (getAttribute().isOverwrite()) {
                body.appendClassName(getGenType().getQualifiedName(true));
                body.append('.');
            }
            body.append(getConstantName(getValueSet()));
            body.appendln(";");
        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    /**
     * Generates a product component type's method that returns the set of allowed values for an
     * attribute
     */
    private void generateMethodGetSetOfAllowedValuesForProductCmptType(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getAttribute().isOverwrite()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), false);
        } else if (getAttribute().getModifier().isPublished()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), true);
        }
        generateSignatureGetSetOfAllowedValues(methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameSetOfAllowedValues());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    private void generateFieldForTheAllowedSetOfValues(JavaCodeFragmentBuilder memberVarBuilder) {
        String lookup = getLookupPrefixForFieldSetOfAllowedValues();
        appendLocalizedJavaDoc(lookup, getAttribute().getName(), memberVarBuilder);
        String fieldType = getJavaTypeForValueSet(getValueSet());
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, fieldType, getFieldNameSetOfAllowedValues());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * private Integer minAge;
     * </pre>
     */
    private void generateFieldDefaultValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder)
            throws CoreException {

        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", getAttribute().getName(), memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(getAttribute().getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaultValue(), defaultValueExpression);
    }

    public void generateInitialization(JavaCodeFragmentBuilder builder, IIpsProject ipsProject) throws CoreException {
        builder.append(getMemberVarName());
        builder.append(" = ");
        JavaCodeFragment body = new JavaCodeFragment();
        generateGenerationAccess(body, ipsProject);
        body.append(getMethodNameGetDefaultValue());
        builder.append(body);
        builder.append("();");
        builder.appendln();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setPremium(Money newValue);
     * </pre>
     */
    protected void generateSetterInterface(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String description = StringUtils.isEmpty(getAttribute().getDescription()) ? "" : SystemUtils.LINE_SEPARATOR
                + "<p>" + SystemUtils.LINE_SEPARATOR + getAttribute().getDescription();
        String[] replacements = new String[] { getAttribute().getName(), description };
        appendLocalizedJavaDoc("METHOD_SETVALUE", replacements, getAttribute().getName(), methodsBuilder);
        generateSetterSignature(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    public void generateInitializationForOverrideAttributes(JavaCodeFragmentBuilder builder, IIpsProject ipsProject)
            throws CoreException {

        JavaCodeFragment initialValueExpression = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        generateCallToMethodSetPropertyValue(initialValueExpression, builder);
    }

    private void generateCallToMethodSetPropertyValue(JavaCodeFragment value, JavaCodeFragmentBuilder builder) {
        builder.append(getMethodNametSetPropertyValue());
        builder.append('(');
        builder.append(value);
        builder.append(");");
    }

    private void generateConstantSetOfAllowedValues(JavaCodeFragmentBuilder builder) {
        if (isAbstractValueSet()) {
            return;
        }
        if (isUnrestrictedValueSet()) {
            return;
        }
        if (isRangeValueSet()) {
            generateConstantRangeOfAllowedValues(builder);
            return;
        } else if (isEnumValueSet()) {
            generateConstantEnumSetOfAllowedValues(builder);
            return;
        }
        throw new RuntimeException("Unknown type of value set " + getValueSet());
    }

    protected String getConstantName(IValueSet valueSet) {
        if (valueSet.isEnum()) {
            return getLocalizedText("FIELD_MAX_ALLOWED_VALUES_FOR_NAME", StringUtils
                    .upperCase(getAttribute().getName()));
        }
        if (valueSet.isRange()) {
            return getLocalizedText("FIELD_MAX_RANGE_FOR_NAME", StringUtils.upperCase(getAttribute().getName()));
        }
        throw new RuntimeException("Can't handle value set" + valueSet);
    }

    protected void generateConstantRangeOfAllowedValues(JavaCodeFragmentBuilder membersBuilder) {
        appendLocalizedJavaDoc("FIELD_MAX_RANGE_FOR", getAttribute().getName(), membersBuilder);
        IRangeValueSet range = (IRangeValueSet)((IPolicyCmptTypeAttribute)getAttribute()).getValueSet();
        JavaCodeFragment containsNullFrag = new JavaCodeFragment();
        containsNullFrag.append(range.getContainsNull());
        JavaCodeFragment frag = valuesetDatatypeHelper.newRangeInstance(createCastExpression(range.getLowerBound()),
                createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()), containsNullFrag,
                isUseTypesafeCollections());
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC, valuesetDatatypeHelper
                .getRangeJavaClassName(isUseTypesafeCollections()), getConstantName(getValueSet()), frag);
    }

    protected void generateConstantEnumSetOfAllowedValues(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("FIELD_MAX_ALLOWED_VALUES_FOR", getAttribute().getName(), getAttribute()
                .getDescription(), builder);
        String[] valueIds = EMPTY_STRING_ARRAY;
        boolean containsNull = false;
        if (((IPolicyCmptTypeAttribute)getAttribute()).getValueSet() instanceof IEnumValueSet) {
            IEnumValueSet set = (IEnumValueSet)((IPolicyCmptTypeAttribute)getAttribute()).getValueSet();
            valueIds = set.getValues();
            containsNull = set.getContainsNull();
        } else if (getDatatype() instanceof EnumDatatype) {
            valueIds = ((EnumDatatype)getDatatype()).getAllValueIds(true);
            containsNull = true;
        } else {
            throw new IllegalArgumentException("This method is only applicable to attributes "
                    + "based on an EnumDatatype or containing an EnumValueSet.");
        }
        JavaCodeFragment frag = null;
        if (getDatatype().isPrimitive()) {
            containsNull = false;
        }
        frag = valuesetDatatypeHelper.newEnumValueSetInstance(valueIds, containsNull, isUseTypesafeCollections());
        builder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC,
                isUseTypesafeCollections() ? Java5ClassNames.OrderedValueSet_QualifiedName + "<"
                        + valuesetDatatypeHelper.getJavaClassName() + ">" : EnumValueSet.class.getName(),
                getConstantName(getValueSet()), frag);
    }

    private JavaCodeFragment createCastExpression(String bound) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (StringUtils.isEmpty(bound)) {
            frag.append('(');
            frag.appendClassName(valuesetDatatypeHelper.getJavaClassName());
            frag.append(')');
        }
        frag.append(valuesetDatatypeHelper.newInstance(bound));
        return frag;
    }

    public GenChangeableAttribute getGeneratorForOverwrittenChangeableAttribute() throws CoreException {
        GenPolicyCmptTypeAttribute gen = getGeneratorForOverwrittenAttribute();
        if (gen instanceof GenChangeableAttribute) {
            return (GenChangeableAttribute)gen;
        }
        return null;
    }

    protected String getMethodNametSetPropertyValue() {
        return getJavaNamingConvention().getSetterMethodName(getAttribute().getName(), getDatatype());
    }

    /**
     * Returns the name of the parameter in the setter method for a property, e.g. newValue.
     */
    protected String getParamNameForSetterMethod() {
        return getLocalizedText("PARAM_NEWVALUE_NAME", getAttribute().getName());
    }

    protected boolean isAbstractValueSet() {
        return getValueSet().isAbstract();
    }

    protected boolean isRangeValueSet() {
        return getValueSet().isRange();
    }

    protected boolean isEnumValueSet() {
        return getValueSet().isEnum();
    }

    protected boolean isUnrestrictedValueSet() {
        return ValueSetType.UNRESTRICTED == getValueSet().getValueSetType();
    }

    protected boolean shouldGetAllowedSetOfValuesBeGenerated() {
        if (isUnrestrictedValueSet() && !isProductRelevant()) {
            return false;
        }
        if (isEnumValueSet() && DatatypeUtil.isEnumTypeWithSeparateContent(getDatatype())) {
            return false;
        }
        return true;
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        super.getGeneratedJavaElementsForImplementation(javaElements, generatedJavaType, ipsElement);

        if (isOverwrite() && !(isProductRelevant())) {
            return;
        }

        if (!(isProductRelevant() && isOverwrite())) {
            addMemberVarToGeneratedJavaElements(javaElements, generatedJavaType);
            addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
            addSetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
        if (isProductRelevant()) {
            addGetValueSetMethodToGeneratedJavaElements(javaElements, generatedJavaType);
            IType javaTypeProductCmptTypeGen = null;
            try {
                javaTypeProductCmptTypeGen = findGeneratedJavaTypeForProductCmptTypeGen(false);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (javaTypeProductCmptTypeGen != null) {
                addDefaultValueMemberVarToGeneratedJavaElements(javaElements, javaTypeProductCmptTypeGen);
                addValueSetMemberVarToGeneratedJavaElements(javaElements, javaTypeProductCmptTypeGen);
                addGetDefaultValueMethodToGeneratedJavaElements(javaElements, javaTypeProductCmptTypeGen);
                addGetValueSetMethodToGeneratedJavaElements(javaElements, javaTypeProductCmptTypeGen);
            }
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        super.getGeneratedJavaElementsForPublishedInterface(javaElements, generatedJavaType, ipsElement);

        if ((isOverwrite() && !(isProductRelevant())) || !(isPublished())) {
            return;
        }

        if (!(isProductRelevant() && isOverwrite())) {
            addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
            addSetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
        if (isProductRelevant()) {
            addGetValueSetMethodToGeneratedJavaElements(javaElements, generatedJavaType);
            IType javaTypeProductCmptTypeGen = null;
            try {
                javaTypeProductCmptTypeGen = findGeneratedJavaTypeForProductCmptTypeGen(true);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (javaTypeProductCmptTypeGen != null) {
                addGetDefaultValueMethodToGeneratedJavaElements(javaElements, javaTypeProductCmptTypeGen);
                addGetValueSetMethodToGeneratedJavaElements(javaElements, javaTypeProductCmptTypeGen);
            }
        }
    }

    /**
     * Searches and returns the Java type generated for the product component type generation of the
     * <tt>IProductCmptType</tt> configuring the <tt>IPolicyCmptType</tt> of the
     * <tt>IPolicyCmptTypeAttribute</tt> this generator is configured for.
     * <p>
     * Returns <tt>null</tt> if the <tt>IProductCmptType</tt> cannot be found.
     * 
     * @param forPublishedInterface Flag indicating whether to search for the published interface of the
     *            product component type generation (<tt>true</tt>) or for it's implementation (
     *            <tt>false</tt>).
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IProductCmptType</tt>.
     */
    public IType findGeneratedJavaTypeForProductCmptTypeGen(boolean forPublishedInterface) throws CoreException {
        IPolicyCmptType policyCmptType = (IPolicyCmptType)getGenType().getIpsPart();
        BaseProductCmptTypeBuilder productCmptTypeBuilder = forPublishedInterface ? getGenType().getBuilderSet()
                .getProductCmptGenInterfaceBuilder() : getGenType().getBuilderSet().getProductCmptGenImplClassBuilder();

        IProductCmptType productCmptType = policyCmptType.findProductCmptType(policyCmptType.getIpsProject());
        if (productCmptType == null) {
            return null;
        }
        return productCmptTypeBuilder.getGeneratedJavaType(productCmptType);
    }

    private void addDefaultValueMemberVarToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IField field = generatedJavaType.getField(getFieldNameDefaultValue());
        javaElements.add(field);
    }

    private void addValueSetMemberVarToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {

        IField field = generatedJavaType.getField(getFieldNameSetOfAllowedValues());
        javaElements.add(field);
    }

    private void addGetValueSetMethodToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetSetOfAllowedValues(), new String[] { "Q"
                + IValidationContext.class.getSimpleName() + ";" });
        javaElements.add(method);
    }

    private void addGetDefaultValueMethodToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetDefaultValue(), new String[] {});
        javaElements.add(method);
    }

}
