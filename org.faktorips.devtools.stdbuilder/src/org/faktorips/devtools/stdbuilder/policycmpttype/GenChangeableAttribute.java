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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.EnumValueSet;

/**
 * Code generator for a changeable attribute.
 *  
 * @author Jan Ortmann
 */
public abstract class GenChangeableAttribute extends GenAttribute {

    // if the attribute's datatype is a primitive type, this datatype is the wrapper datatype for the primitive
    // otherwise its the attribute's original datatype.
    protected DatatypeHelper wrapperDatatypeHelper;
    
    public GenChangeableAttribute(IPolicyCmptTypeAttribute a, DefaultJavaSourceFileBuilder builder, LocalizedStringsSet stringsSet,
            boolean generateImplementation) throws CoreException {
        super(a, builder, stringsSet, generateImplementation);
        ArgumentCheck.isTrue(a.isChangeable());
        wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsProject(), datatypeHelper);        
    }
    
    protected abstract ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder();
    
    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isOverwritten()) {
            return;
        }
        if (isPublished() == isGeneratingInterface()) {
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
     * <pre>
     * public void setPremium(Money newValue)
     * </pre>
     */
    protected void generateSetterSignature(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNametSetPropertyValue(attributeName, datatypeHelper.getDatatype());
        String paramName = getParamNameForSetterMethod();
        methodsBuilder.signature(modifier, "void", methodName, new String[]{paramName}, new String[]{getJavaClassName()});
    }
    
    protected String getMethodNametSetPropertyValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper){
        return getJavaNamingConvention().getSetterMethodName(a.getName(), datatypeHelper.getDatatype());
    }
    
    /**
     * Returns the name of the parameter in the setter method for a property,
     * e.g. newValue.
     */
    protected String getParamNameForSetterMethod() {
        return getLocalizedText("PARAM_NEWVALUE_NAME", attributeName);
    }
    

    protected void generateFieldMaxRange(JavaCodeFragmentBuilder membersBuilder){
        appendLocalizedJavaDoc("FIELD_MAX_RANGE_FOR", attributeName, membersBuilder);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        JavaCodeFragment containsNullFrag = new JavaCodeFragment();
        containsNullFrag.append(range.getContainsNull());
        JavaCodeFragment frag = wrapperDatatypeHelper.newRangeInstance(
                createCastExpression(range.getLowerBound()), 
                createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()), 
                containsNullFrag);
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | 
                                      java.lang.reflect.Modifier.FINAL | 
                                      java.lang.reflect.Modifier.STATIC, 
                                      wrapperDatatypeHelper.getRangeJavaClassName(), 
                                      getFieldNameMaxRange(), frag);
    }
    
    protected String getFieldNameMaxRange(){
        return getLocalizedText("FIELD_MAX_RANGE_FOR_NAME", StringUtils.upperCase(attributeName));
    }
    
    protected void generateFieldMaxAllowedValuesFor(JavaCodeFragmentBuilder builder){
        appendLocalizedJavaDoc("FIELD_MAX_ALLOWED_VALUES_FOR", attributeName, attribute.getDescription(), builder);
        String[] valueIds = EMPTY_STRING_ARRAY;
        boolean containsNull = false;
        if(attribute.getValueSet() instanceof IEnumValueSet){
            IEnumValueSet set = (IEnumValueSet)attribute.getValueSet();
            valueIds = set.getValues();
            containsNull = set.getContainsNull();
        } else if(getDatatype() instanceof EnumDatatype){
            valueIds = ((EnumDatatype)getDatatype()).getAllValueIds(true);
            containsNull = true;
        } else{
            throw new IllegalArgumentException("This method can only be call with a value for parameter 'a' " +
                    "that is an IAttibute that bases on an EnumDatatype or contains an EnumValueSet.");
        }
        JavaCodeFragment frag = null;
        if(getDatatype().isPrimitive()){
            containsNull = false;
        }
        frag = wrapperDatatypeHelper.newEnumValueSetInstance(valueIds, containsNull);
        builder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | 
                java.lang.reflect.Modifier.FINAL | 
                java.lang.reflect.Modifier.STATIC, 
                EnumValueSet.class, 
                getFieldNameMaxAllowedValues(), frag);
    }
    
    protected String getFieldNameMaxAllowedValues(){
        return getLocalizedText("FIELD_MAX_ALLOWED_VALUES_FOR_NAME", StringUtils.upperCase(attributeName));
    }
    
    /**
     * Generates the sginature for the method to access an attribute's range of allowed values.
     */
    protected void generateSignatureGetRange(JavaCodeFragmentBuilder builder) throws CoreException {
        // TODO refactor
        getProductCmptGenInterfaceBuilder().generateSignatureGetRangeFor(attribute, datatypeHelper, builder);
    }

    /**
     * Generates the sginature for the method to access an attribute's set of allowed values.
     */
    protected void generateSignatureAllowedValues(JavaCodeFragmentBuilder builder) throws CoreException {
        getProductCmptGenInterfaceBuilder().generateSignatureGetAllowedValuesFor(attribute, getDatatype(), builder);
    }
    
    private JavaCodeFragment createCastExpression(String bound){
        JavaCodeFragment frag = new JavaCodeFragment();
        if(StringUtils.isEmpty(bound)){
            frag.append('(');
            frag.appendClassName(wrapperDatatypeHelper.getJavaClassName());
            frag.append(')');
        }
        frag.append(wrapperDatatypeHelper.newInstance(bound));
        return frag;
    }
    
    protected boolean isRangeValueSet() {
        return ValueSetType.RANGE==attribute.getValueSet().getValueSetType();
    }
    
    protected boolean isEnumValueSet() {
        return ValueSetType.ENUM==attribute.getValueSet().getValueSetType();        
    }

    protected boolean isAllValuesValueSet() {
        return ValueSetType.ALL_VALUES==attribute.getValueSet().getValueSetType();        
    }

    protected boolean isNotAllValuesValueSet() {
        return ValueSetType.ALL_VALUES!=attribute.getValueSet().getValueSetType();
    }
}
