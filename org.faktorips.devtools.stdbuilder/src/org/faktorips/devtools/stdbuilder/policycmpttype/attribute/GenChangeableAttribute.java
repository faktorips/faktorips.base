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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.valueset.EnumValueSet;

/**
 * Code generator for a changeable attribute.
 * 
 * @author Jan Ortmann
 */
public class GenChangeableAttribute extends GenAttribute {

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
            generateMemberVariablesForProductCmptType(mainSection.getMemberVarBuilder(), ipsProject, generatesInterface);
        }
        generateMethodsForProductCmptType(mainSection.getMethodBuilder(), ipsProject, generatesInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (isPublished() == generatesInterface) {
            generateAttributeNameConstant(builder);
            if (isRangeValueSet()) {
                generateFieldMaxRange(builder);
            } else if (isEnumValueSet()) {
                generateFieldMaxAllowedValuesFor(builder);
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
        String methodName = getMethodNametSetPropertyValue(attributeName, datatypeHelper.getDatatype());
        String paramName = getParamNameForSetterMethod();
        methodsBuilder.signature(modifier, "void", methodName, new String[] { paramName },
                new String[] { getJavaClassName() });
    }

    protected String getMethodNametSetPropertyValue() {
        return getJavaNamingConvention().getSetterMethodName(attribute.getName(), datatypeHelper.getDatatype());
    }

    /**
     * Returns the name of the parameter in the setter method for a property, e.g. newValue.
     */
    protected String getParamNameForSetterMethod() {
        return getLocalizedText("PARAM_NEWVALUE_NAME", attributeName);
    }

    protected void generateFieldMaxRange(JavaCodeFragmentBuilder membersBuilder) {
        appendLocalizedJavaDoc("FIELD_MAX_RANGE_FOR", attributeName, membersBuilder);
        IRangeValueSet range = (IRangeValueSet)getPolicyCmptTypeAttribute().getValueSet();
        JavaCodeFragment containsNullFrag = new JavaCodeFragment();
        containsNullFrag.append(range.getContainsNull());
        JavaCodeFragment frag = valuesetDatatypeHelper.newRangeInstance(createCastExpression(range.getLowerBound()),
                createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()), containsNullFrag,
                isUseTypesafeCollections());
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC, valuesetDatatypeHelper
                .getRangeJavaClassName(isUseTypesafeCollections()), getFieldNameMaxRange(), frag);
    }

    protected String getFieldNameMaxRange() {
        return getLocalizedText("FIELD_MAX_RANGE_FOR_NAME", StringUtils.upperCase(attributeName));
    }

    protected void generateFieldMaxAllowedValuesFor(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("FIELD_MAX_ALLOWED_VALUES_FOR", attributeName, attribute.getDescription(), builder);
        String[] valueIds = EMPTY_STRING_ARRAY;
        boolean containsNull = false;
        if (getPolicyCmptTypeAttribute().getValueSet() instanceof IEnumValueSet) {
            IEnumValueSet set = (IEnumValueSet)getPolicyCmptTypeAttribute().getValueSet();
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
                getFieldNameMaxAllowedValues(), frag);
    }

    protected String getFieldNameMaxAllowedValues() {
        return getLocalizedText("FIELD_MAX_ALLOWED_VALUES_FOR_NAME", StringUtils.upperCase(attributeName));
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

    protected boolean isRangeValueSet() {
        return getPolicyCmptTypeAttribute().getValueSet().isRange();
    }

    protected boolean isEnumValueSet() {
        return getPolicyCmptTypeAttribute().getValueSet().isEnum();
    }

    protected boolean isUnrestrictedValueSet() {
        return ValueSetType.UNRESTRICTED == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    /**
     * Generates the signature for the method to access an attribute's allowed RANGE of values.
     */
    // TODO can be deleted
    public void generateSignatureGetRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetSetOfAllowedValues();
        String rangeClassName = helper.getRangeJavaClassName(isUseTypesafeCollections());
        methodsBuilder.signature(Modifier.PUBLIC, rangeClassName, methodName, new String[] { "context" },
                new String[] { IValidationContext.class.getName() });
    }

    /**
     * Generates the signature for the method to access an attribute's set of allowed ENUM values.
     */
    // TODO can be deleted
    public void generateSignatureGetAllowedValuesFor(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetSetOfAllowedValues();
        methodsBuilder.signature(Modifier.PUBLIC,
                isUseTypesafeCollections() ? Java5ClassNames.OrderedValueSet_QualifiedName + "<"
                        + datatype.getJavaClassName() + ">" : EnumValueSet.class.getName(), methodName,
                new String[] { "context" }, new String[] { IValidationContext.class.getName() });
    }

    /**
     * Generates the signature for the method to access an attribute's set of allowed ENUM values.
     */
    public void generateSignatureGetSetOfAllowedValues(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetSetOfAllowedValues();
        methodsBuilder.signature(Modifier.PUBLIC, getJavaTypeForValueSet(getValueSet()), methodName,
                new String[] { "context" }, new String[] { IValidationContext.class.getName() });
    }

    /**
     * Generates the method to access an attribute's set of allowed ENUM values.
     */
    public void generateMethodGetSetOfAllowedValues(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        String lookup = getLookupPrefixForMethodNameGetSetOfAllowedValues();
        appendLocalizedJavaDoc(lookup, getAttributeName(), methodsBuilder);
        generateSignatureGetSetOfAllowedValues(datatype, methodsBuilder);
        methodsBuilder.append(';');
    }

    /**
     * Generates the method to access an attribute's set of allowed ENUM values.
     */
    // TODO can be deleted
    public void generateMethodGetAllowedValuesFor(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_ALLOWED_VALUES_FOR", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
        generateSignatureGetAllowedValuesFor(datatype, methodsBuilder);
        methodsBuilder.append(';');
    }

    // TODO can be deleted
    public void generateMethodGetRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_RANGE_FOR", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
        generateSignatureGetRangeFor(helper, methodsBuilder);
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
        String methodName = getMethodNameGetDefaultValue(datatypeHelper);
        builder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the name of the method that returns the default value for the attribute.
     */
    public String getMethodNameGetDefaultValue(DatatypeHelper datatypeHelper) {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameDefaultValue(),
                datatypeHelper.getDatatype());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            if (isOverwritten()) {
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
    protected void generateMemberVariablesForProductCmptType(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            generateFieldDefaultValue(datatypeHelper, builder);

            // if the datatype is a primitive datatype the datatypehelper will be switched to
            // the helper of the wrapper type
            valuesetDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(ipsProject, datatypeHelper);
            generateFieldForTheAllowedSetOfValues(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (!generatesInterface) {
            if (isRangeValueSet()) {
                generateMethodGetRange(builder, ipsProject);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValues(builder, ipsProject);
            }
            if (isOverwritten()) {
                return;
            }
            generateGetterImplementation(builder);
            generateSetterMethod(builder);
        }

        if (generatesInterface) {
            if (isOverwritten()) {
                return;
            }
            generateGetterInterface(builder);
            generateSetterInterface(builder);
            if (isRangeValueSet()) {
                generateMethodGetRangeFor(valuesetDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValuesFor(valuesetDatatypeHelper.getDatatype(), builder);
            }
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
            generateMethodGetDefaultValue(datatypeHelper, builder, generatesInterface);

            if (isRangeValueSet()) {
                generateMethodGetRangeForProductCmptType(valuesetDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedEnumValuesForProductCmptType(valuesetDatatypeHelper.getDatatype(), builder);
            }
        }

        if (generatesInterface) {
            generateMethodGetDefaultValue(datatypeHelper, builder, generatesInterface);

            // if the datatype is a primitive datatype the datatypehelper will be switched to the
            // helper of the wrapper type
            valuesetDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(ipsProject, datatypeHelper);
            if (isEnumValueSet()) {
                generateMethodGetAllowedValuesFor(valuesetDatatypeHelper.getDatatype(), builder);
            } else if (isRangeValueSet()) {
                generateMethodGetRangeFor(valuesetDatatypeHelper, builder);
            }
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
            if (getPolicyCmptTypeAttribute().isOverwrite()) {
                appendOverrideAnnotation(methodsBuilder, false);
            }
            generateSignatureGetDefaultValue(datatypeHelper, methodsBuilder);
            methodsBuilder.openBracket();
            methodsBuilder.append("return ");
            methodsBuilder.append(getFieldNameDefaultValue());
            methodsBuilder.append(';');
            methodsBuilder.closeBracket();
        }

        if (generatesInterface) {
            appendLocalizedJavaDoc("METHOD_GET_DEFAULTVALUE", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
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
        generateSetterSignature(methodsBuilder);
        methodsBuilder.openBracket();
        getGenPolicyCmptType().generateChangeListenerSupportBeforeChange(methodsBuilder,
                ChangeEventType.MUTABLE_PROPERTY_CHANGED, getDatatype().getJavaClassName(), getMemberVarName(),
                getParamNameForSetterMethod(), getStaticConstantPropertyName());
        methodsBuilder.append("this.");
        methodsBuilder.append(getMemberVarName());
        methodsBuilder.append(" = ");
        methodsBuilder.append(datatypeHelper.referenceOrSafeCopyIfNeccessary(getParamNameForSetterMethod()));
        methodsBuilder.appendln(";");
        getGenPolicyCmptType().generateChangeListenerSupportAfterChange(methodsBuilder,
                ChangeEventType.MUTABLE_PROPERTY_CHANGED, getDatatype().getJavaClassName(), getMemberVarName(),
                getParamNameForSetterMethod(), getStaticConstantPropertyName());
        methodsBuilder.closeBracket();
    }

    private void generateMethodGetRange(JavaCodeFragmentBuilder methodBuilder, IIpsProject ipsProject)
            throws CoreException {
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getPolicyCmptTypeAttribute().isOverwrite()) {
            appendOverrideAnnotation(methodBuilder, false);
        }
        generateSignatureGetRangeFor(valuesetDatatypeHelper, methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (getPolicyCmptTypeAttribute().isProductRelevant() && getProductCmptType(ipsProject) != null) {
            generateGenerationAccess(body, ipsProject);
            body.append(getMethodNameGetSetOfAllowedValues());
            body.appendln("(context);");
        } else {
            if (attribute.isOverwrite()) {
                body.appendClassName(getGenPolicyCmptType().getQualifiedName(true));
                body.append('.');
            }
            body.append(getFieldNameMaxRange());
            body.appendln(";");

        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    private void generateGenerationAccess(JavaCodeFragment body, IIpsProject ipsProject) throws CoreException {
        GenProductCmptType genProductCmptType = getGenPolicyCmptType().getBuilderSet().getGenerator(
                getProductCmptType(ipsProject));
        if (isPublished()) {
            body.append(genProductCmptType.getMethodNameGetProductCmptGeneration());
            body.append("().");
        } else { // Public
            body.append("((");
            body.append(getProductCmptType(ipsProject).getName()
                    + getGenPolicyCmptType().getAbbreviationForGenerationConcept());
            body.append(")");
            body.append(genProductCmptType.getMethodNameGetProductCmptGeneration());
            body.append("()).");
        }
    }

    private void generateMethodGetAllowedValues(JavaCodeFragmentBuilder methodBuilder, IIpsProject ipsProject)
            throws CoreException {
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getPolicyCmptTypeAttribute().isOverwrite()) {
            appendOverrideAnnotation(methodBuilder, false);
        }
        generateSignatureGetAllowedValuesFor(valuesetDatatypeHelper.getDatatype(), methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (!isUnrestrictedValueSet() && isConfigurableByProduct() && getProductCmptType(ipsProject) != null) {
            generateGenerationAccess(body, ipsProject);
            body.append(getMethodNameGetSetOfAllowedValues());
            body.appendln("(context);");
        } else {
            if (attribute.isOverwrite()) {
                body.appendClassName(getGenPolicyCmptType().getQualifiedName(true));
                body.append('.');
            }
            body.append(getFieldNameMaxAllowedValues());
            body.appendln(";");
        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    // TODO kann geloescht werden
    private void generateMethodGetRangeForProductCmptType(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getPolicyCmptTypeAttribute().isOverwrite()) {
            appendOverrideAnnotation(methodsBuilder, false);
        }
        generateSignatureGetRangeFor(helper, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameForSetOfAllowedValues());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    /**
     * Gnerates a product component type's method that returns the set of allowed values for an
     * attribute
     */
    // TODO kann geloescht werden
    private void generateMethodGetAllowedEnumValuesForProductCmptType(Datatype datatype,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getPolicyCmptTypeAttribute().isOverwrite()) {
            appendOverrideAnnotation(methodsBuilder, false);
        }
        generateSignatureGetAllowedValuesFor(datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameForSetOfAllowedValues());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    /**
     * Gnerates a product component type's method that returns the set of allowed values for an
     * attribute
     */
    private void generateMethodGetSetOfAllowedValuesForProductCmptType(Datatype datatype,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (getPolicyCmptTypeAttribute().isOverwrite()) {
            appendOverrideAnnotation(methodsBuilder, false);
        }
        generateSignatureGetSetOfAllowedValues(datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameForSetOfAllowedValues());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    private void generateFieldForTheAllowedSetOfValues(JavaCodeFragmentBuilder memberVarBuilder) {
        if (getValueSet().isUnrestricted()) {
            return; // TODO
        }
        String lookup = getLookupPrefixForFieldSetOfAllowedValues();
        appendLocalizedJavaDoc(lookup, getAttributeName(), memberVarBuilder);
        String fieldType = getJavaTypeForValueSet(getValueSet());
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, fieldType, getFieldNameForSetOfAllowedValues());
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
        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", getPolicyCmptTypeAttribute().getName(), memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(getPolicyCmptTypeAttribute()
                .getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaultValue(), defaultValueExpression);
    }

    public void generateInitialization(JavaCodeFragmentBuilder builder, IIpsProject ipsProject) throws CoreException {
        builder.append(getMemberVarName());
        builder.append(" = ");
        JavaCodeFragment body = new JavaCodeFragment();
        generateGenerationAccess(body, ipsProject);
        body.append(getMethodNameGetDefaultValue(datatypeHelper));
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
        String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>"
                + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
        String[] replacements = new String[] { attributeName, description };
        appendLocalizedJavaDoc("METHOD_SETVALUE", replacements, attributeName, methodsBuilder);
        generateSetterSignature(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    public void generateInitializationForOverrideAttributes(JavaCodeFragmentBuilder builder, IIpsProject ipsProject)
            throws CoreException {
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(attribute.getDefaultValue());
        generateCallToMethodSetPropertyValue(initialValueExpression, builder);
    }

    private void generateCallToMethodSetPropertyValue(JavaCodeFragment value, JavaCodeFragmentBuilder builder) {
        builder.append(getMethodNametSetPropertyValue());
        builder.append('(');
        builder.append(value);
        builder.append(");");
    }

}
