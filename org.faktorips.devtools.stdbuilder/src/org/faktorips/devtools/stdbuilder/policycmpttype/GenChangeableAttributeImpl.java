/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ModelObjectChangedEvent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenChangeableAttributeImpl extends GenChangeableAttribute {

    public GenChangeableAttributeImpl(IPolicyCmptTypeAttribute a, PolicyCmptImplClassBuilder builder, LocalizedStringsSet stringsSet) throws CoreException {
        super(a, builder, stringsSet, true);
    }
    
    /**
     * Returns the policy component implementation class builder.
     */
    private PolicyCmptImplClassBuilder getImplClassBuilder() {
        return (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
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
    protected ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return getImplClassBuilder().getProductCmptGenInterfaceBuilder();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isOverwritten()) {
            return;
        }
        generateField(builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
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
        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(), "MUTABLE_PROPERTY_CHANGED", getMemberVarName() );
        methodsBuilder.closeBracket();
    }

    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder, String eventClassName, String eventConstant, String fieldName) {
        generateChangeListenerSupport(methodsBuilder, eventClassName, eventConstant, fieldName, null);
    }
    
    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder, String eventClassName, String eventConstant, String fieldName, String paramName) {
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
            if(paramName != null) {
                methodsBuilder.append(", ");
                methodsBuilder.append(paramName);
            }
            methodsBuilder.appendln("));");
            methodsBuilder.appendln("}");
        }
    }


    private void generateMethodGetRange(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        getProductCmptGenInterfaceBuilder().generateSignatureGetRangeFor(attribute, wrapperDatatypeHelper, methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (attribute.isProductRelevant() && getProductCmptType()!=null){
            body.append(getInterfaceBuilder().getMethodNameGetProductCmptGeneration(getProductCmptType()));
            body.append("().");
            body.append(getProductCmptGenInterfaceBuilder().getMethodNameGetRangeFor(attribute, wrapperDatatypeHelper.getDatatype()));
            body.appendln("(businessFunction);");
        }
        else{
            body.append(getFieldNameMaxRange());
            body.appendln(";");
            
        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }
    
    private void generateMethodGetAllowedValues(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        getProductCmptGenInterfaceBuilder().generateSignatureGetAllowedValuesFor(attribute, wrapperDatatypeHelper.getDatatype(), methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if( isNotAllValuesValueSet() && isConfigurableByProduct() && getProductCmptType()!=null) {
            body.append(getInterfaceBuilder().getMethodNameGetProductCmptGeneration(getProductCmptType()));
            body.append("().");
            body.append(getProductCmptGenInterfaceBuilder().getMethodNameGetAllowedValuesFor(attribute, wrapperDatatypeHelper.getDatatype()));
            body.appendln("(businessFunction);");
        }
        else{
            body.append(getFieldNameMaxAllowedValues());
            body.appendln(";");
        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }
    
}
