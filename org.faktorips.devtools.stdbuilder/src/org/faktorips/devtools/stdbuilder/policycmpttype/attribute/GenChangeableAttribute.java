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
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.EnumValueSet;

/**
 * Code generator for a changeable attribute.
 * 
 * @author Jan Ortmann
 */
public abstract class GenChangeableAttribute extends GenAttribute {

    // if the attribute's datatype is a primitive type, this datatype is the wrapper datatype for
    // the primitive
    // otherwise its the attribute's original datatype.
    protected DatatypeHelper wrapperDatatypeHelper;

    public GenChangeableAttribute(IPolicyCmptTypeAttribute a, DefaultJavaSourceFileBuilder builder,
            LocalizedStringsSet stringsSet, boolean generateImplementation) throws CoreException {
        super(a, builder, stringsSet, generateImplementation);
        ArgumentCheck.isTrue(a.isChangeable());
        wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsProject(), datatypeHelper);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isGeneratingPolicySide()) {
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

    protected String getMethodNametSetPropertyValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper) {
        return getJavaNamingConvention().getSetterMethodName(a.getName(), datatypeHelper.getDatatype());
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
        JavaCodeFragment frag = wrapperDatatypeHelper.newRangeInstance(createCastExpression(range.getLowerBound()),
                createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()), containsNullFrag);
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC, wrapperDatatypeHelper.getRangeJavaClassName(),
                getFieldNameMaxRange(), frag);
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
            throw new IllegalArgumentException("This method can only be call with a value for parameter 'a' "
                    + "that is an IAttibute that bases on an EnumDatatype or contains an EnumValueSet.");
        }
        JavaCodeFragment frag = null;
        if (getDatatype().isPrimitive()) {
            containsNull = false;
        }
        frag = wrapperDatatypeHelper.newEnumValueSetInstance(valueIds, containsNull);
        builder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC, EnumValueSet.class, getFieldNameMaxAllowedValues(), frag);
    }

    protected String getFieldNameMaxAllowedValues() {
        return getLocalizedText("FIELD_MAX_ALLOWED_VALUES_FOR_NAME", StringUtils.upperCase(attributeName));
    }

    private JavaCodeFragment createCastExpression(String bound) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (StringUtils.isEmpty(bound)) {
            frag.append('(');
            frag.appendClassName(wrapperDatatypeHelper.getJavaClassName());
            frag.append(')');
        }
        frag.append(wrapperDatatypeHelper.newInstance(bound));
        return frag;
    }

    protected boolean isRangeValueSet() {
        return ValueSetType.RANGE == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    protected boolean isEnumValueSet() {
        return ValueSetType.ENUM == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    protected boolean isAllValuesValueSet() {
        return ValueSetType.ALL_VALUES == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    protected boolean isNotAllValuesValueSet() {
        return ValueSetType.ALL_VALUES != getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    /**
     * Generates the signature for the method to access an attribute's set of allowed values.
     * 
     * @param datatype
     */
    public void generateSignatureGetAllowedValuesFor(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetAllowedValuesFor(datatype);
        methodsBuilder.signature(Modifier.PUBLIC, EnumValueSet.class.getName(), methodName,
                new String[] { "businessFunction" }, new String[] { String.class.getName() });
    }

    public String getMethodNameGetAllowedValuesFor(Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_ALLOWED_VALUES_FOR_NAME", StringUtils
                        .capitalize(getPolicyCmptTypeAttribute().getName())), datatype);
    }

    public void generateMethodGetAllowedValuesFor(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_ALLOWED_VALUES_FOR", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
        generateSignatureGetAllowedValuesFor(datatype, methodsBuilder);
        methodsBuilder.append(';');
    }

    public String getMethodNameGetRangeFor(Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_RANGE_FOR_NAME", StringUtils.capitalize(getPolicyCmptTypeAttribute()
                        .getName())), datatype);
    }

    public void generateMethodGetRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_RANGE_FOR", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
        generateSignatureGetRangeFor(helper, methodsBuilder);
        methodsBuilder.append(';');
    }

    public void generateSignatureGetRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetRangeFor(helper.getDatatype());
        String rangeClassName = helper.getRangeJavaClassName();
        methodsBuilder.signature(Modifier.PUBLIC, rangeClassName, methodName, new String[] { "businessFunction" },
                new String[] { String.class.getName() });
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
}
