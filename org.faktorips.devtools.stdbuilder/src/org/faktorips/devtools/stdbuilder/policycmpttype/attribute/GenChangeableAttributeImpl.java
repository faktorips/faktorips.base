/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ModelObjectChangedEvent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.EnumValueSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenChangeableAttributeImpl extends GenChangeableAttribute {

    public GenChangeableAttributeImpl(IPolicyCmptTypeAttribute a, DefaultJavaSourceFileBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(a, builder, stringsSet, true);
    }

    /**
     * Returns the policy component implementation class builder.
     */
    private PolicyCmptImplClassBuilder getImplClassBuilder() {
        if (getJavaSourceFileBuilder() instanceof PolicyCmptImplClassBuilder) {
            return (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
        }
        return null;
    }

    /**
     * Returns the policy component interface builder.
     */
    private PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return getImplClassBuilder().getInterfaceBuilder();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isGeneratingPolicySide()) {
            if (isOverwritten()) {
                return;
            }
            generateField(builder);
        } else if (isGeneratingProductSide()) {
            generateFieldDefaultValue(datatypeHelper, builder);

            // if the datatype is a primitive datatype the datatypehelper will be switched to the
            // helper of the
            // wrapper type
            wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsProject(),
                    datatypeHelper);
            if (isRangeValueSet()) {
                generateFieldRangeFor(wrapperDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateFieldAllowedValuesFor(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isGeneratingPolicySide()) {
            if (isOverwritten()) {
                return;
            }
            if (isRangeValueSet()) {
                generateMethodGetRange(builder);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValues(builder);
            }
            generateGetterImplementation(builder);
            generateSetterMethod(builder);
        } else if (isGeneratingProductSide()) {
            generateMethodGetDefaultValue(datatypeHelper, builder);

            // if the datatype is a primitive datatype the datatypehelper will be switched to the
            // helper of the
            // wrapper type
            wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsProject(),
                    datatypeHelper);
            if (isRangeValueSet()) {
                generateMethodGetRangeForProd(wrapperDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValuesForProd(wrapperDatatypeHelper.getDatatype(), builder);
            }
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge() {
     *     return minAge;
     * </pre>
     */
    private void generateMethodGetDefaultValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetDefaultValue(datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameDefaultValue());
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
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
        methodsBuilder.append("this.");
        methodsBuilder.append(getMemberVarName());
        methodsBuilder.appendln(" = " + getParamNameForSetterMethod() + ";");
        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(),
                "MUTABLE_PROPERTY_CHANGED", getMemberVarName());
        methodsBuilder.closeBracket();
    }

    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder,
            String eventClassName,
            String eventConstant,
            String fieldName) {
        generateChangeListenerSupport(methodsBuilder, eventClassName, eventConstant, fieldName, null);
    }

    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder,
            String eventClassName,
            String eventConstant,
            String fieldName,
            String paramName) {
        if (isGenerateChangeListenerSupport()) {
            methodsBuilder.appendln("if (" + MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED + "()) {");
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(ModelObjectChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.appendClassName(eventClassName);
            methodsBuilder.append('.');
            methodsBuilder.append(eventConstant);
            methodsBuilder.append(", ");
            methodsBuilder.appendQuoted(fieldName);
            if (paramName != null) {
                methodsBuilder.append(", ");
                methodsBuilder.append(paramName);
            }
            methodsBuilder.appendln("));");
            methodsBuilder.appendln("}");
        }
    }

    private void generateMethodGetRange(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRangeFor(wrapperDatatypeHelper, methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (getPolicyCmptTypeAttribute().isProductRelevant() && getProductCmptType() != null) {
            generateGenerationAccess(body);
            body.append(getMethodNameGetRangeFor(wrapperDatatypeHelper.getDatatype()));
            body.appendln("(businessFunction);");
        } else {
            body.append(getFieldNameMaxRange());
            body.appendln(";");

        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    private void generateGenerationAccess(JavaCodeFragment body) throws CoreException {
        if(isPublished()){
            body.append(getInterfaceBuilder().getMethodNameGetProductCmptGeneration(getProductCmptType()));
            body.append("().");
        }else{ // Public
            body.append("((");
            body.append(getProductCmptType().getName()+getInterfaceBuilder().getAbbreviationForGenerationConcept(getProductCmptType()));
            body.append(")");
            body.append(getInterfaceBuilder().getMethodNameGetProductCmptGeneration(getProductCmptType()));
            body.append("()).");
        }
    }

    private void generateMethodGetAllowedValues(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetAllowedValuesFor(wrapperDatatypeHelper.getDatatype(), methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (isNotAllValuesValueSet() && isConfigurableByProduct() && getProductCmptType() != null) {
            generateGenerationAccess(body);
            body.append(getMethodNameGetAllowedValuesFor(wrapperDatatypeHelper.getDatatype()));
            body.appendln("(businessFunction);");
        } else {
            body.append(getFieldNameMaxAllowedValues());
            body.appendln(";");
        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    private void generateMethodGetRangeForProd(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRangeFor(helper, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameRangeFor());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    private void generateMethodGetAllowedValuesForProd(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetAllowedValuesFor(datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameAllowedValuesFor());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameRangeFor(IPolicyCmptTypeAttribute a){
        return getLocalizedText("FIELD_RANGE_FOR_NAME", StringUtils.capitalize(a.getName()));
    }
    
    private void generateFieldRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_RANGE_FOR", getPolicyCmptTypeAttribute().getName(), memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, helper.getRangeJavaClassName(), getFieldNameRangeFor()); 
    }
    
    private void generateFieldAllowedValuesFor(JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_ALLOWED_VALUES_FOR", getPolicyCmptTypeAttribute().getName(), memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, EnumValueSet.class, getFieldNameAllowedValuesFor()); 
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer minAge;
     * </pre>
     */
    private void generateFieldDefaultValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", getPolicyCmptTypeAttribute().getName(), memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(getPolicyCmptTypeAttribute().getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaultValue(), defaultValueExpression);
    }

    public void generateInitialization(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getMemberVarName());
        builder.append(" = ");
        JavaCodeFragment body = new JavaCodeFragment();
        generateGenerationAccess(body);
        body.append(getMethodNameGetDefaultValue(datatypeHelper));
        builder.append(body);
        builder.append("();");
        builder.appendln();
    } 

}
