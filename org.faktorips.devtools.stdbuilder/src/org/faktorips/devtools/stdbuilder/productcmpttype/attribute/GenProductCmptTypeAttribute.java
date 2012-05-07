/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype.attribute;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.faktorips.codegen.dthelpers.ListOfValueDatatypeHelper;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.faktorips.runtime.internal.MultiValueXmlHelper;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.w3c.dom.Element;

/**
 * Abstract code generator for an attribute.
 * 
 * @author Daniel Hohenberger
 */
public class GenProductCmptTypeAttribute extends GenAttribute {

    public GenProductCmptTypeAttribute(GenProductCmptType genProductCmptType, IProductCmptTypeAttribute a) {
        super(genProductCmptType, a, new LocalizedStringsSet(GenProductCmptTypeAttribute.class));
    }

    @Override
    public IProductCmptTypeAttribute getAttribute() {
        return (IProductCmptTypeAttribute)super.getAttribute();
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
        methodsBuilder.signature(modifier, getDeclarationJavaType(getDatatypeHelper()), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (isPublished() == generatesInterface) {
            generateAttributeNameConstant(builder);
        }
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        if (!generatesInterface) {
            generateFieldValue(getDatatypeHelper(), builder);
        }
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (!generatesInterface) {
            generateMethodGetValue(getDatatypeHelper(), builder);
            generateMethodSetValue(getDatatypeHelper(), builder);
        } else {
            generateMethodGetValueInterface(getDatatypeHelper(), builder);
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Integer getInterestRate() {
     *     return interestRate;
     * </pre>
     */
    private void generateMethodGetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) {
        appendJavaDocAndOverrideAnnotation(methodsBuilder, Overrides.INTERFACE_METHOD);
        generateSignatureGetValue(datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        if (getAttribute().isMultiValueAttribute()) {
            methodsBuilder.append(getNewInstanceExpression(datatypeHelper, getMemberVarName()));
        } else {
            methodsBuilder.append(getMemberVarName());
        }
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Integer getTaxRate();
     * </pre>
     */
    void generateMethodGetValueInterface(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) {

        String description = StringUtils.isEmpty(getDescriptionInGeneratorLanguage(getAttribute())) ? ""
                : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR
                        + getDescriptionInGeneratorLanguage(getAttribute());
        String[] replacements = new String[] { getAttribute().getName(), description };
        appendLocalizedJavaDoc("METHOD_GET_VALUE", replacements, builder);
        generateSignatureGetValue(datatypeHelper, builder);
        builder.append(';');
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setInterestRate(Decimal newValue) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.interestRate = newValue;
     * }
     * </pre>
     */
    private void generateMethodSetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) {
        appendLocalizedJavaDoc("METHOD_SET_VALUE", getAttribute().getName(), methodsBuilder);
        String methodName = getSetterMethodName();
        String[] paramNames = new String[] { "newValue" };
        String[] paramTypes = new String[] { getDeclarationJavaType(datatypeHelper).getSourcecode() };
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(((GenProductCmptType)getGenType()).generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + getMemberVarName());
        methodsBuilder.append(" = ");
        if (getAttribute().isMultiValueAttribute()) {
            methodsBuilder.append(getNewInstanceExpression(datatypeHelper, "newValue"));
        } else {
            methodsBuilder.appendln("newValue");
        }
        methodsBuilder.appendln(";");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Integer getTaxRate()
     * </pre>
     */
    void generateSignatureGetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) {
        String methodName = getGetterMethodName();
        builder.signature(Modifier.PUBLIC, getDeclarationJavaType(datatypeHelper), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the declaration for the datatype wrapped in the datatype helper.
     * 
     * @param datatypeHelper The datatype helper you need the declaration for
     * @return the declaration of the java type wrapped in the helper
     */
    protected JavaCodeFragment getDeclarationJavaType(DatatypeHelper datatypeHelper) {
        if (getAttribute().isMultiValueAttribute()) {
            ListOfValueDatatypeHelper listOfValueDatatypeHelper = new ListOfValueDatatypeHelper(getDatatype());
            return listOfValueDatatypeHelper.getDeclarationJavaTypeFragment();
        } else {
            JavaCodeFragment result = new JavaCodeFragment();
            result.appendClassName(datatypeHelper.getJavaClassName());
            return result;
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * private Integer taxRate;
     * </pre>
     */
    private void generateFieldValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("FIELD_VALUE", StringUtils.capitalize(getAttribute().getName()), builder);
        JavaCodeFragment defaultValueExpression = getNewInstanceExpression(datatypeHelper);
        builder.varDeclaration(Modifier.PRIVATE, getDeclarationJavaType(datatypeHelper).getSourcecode(),
                getMemberVarName(), defaultValueExpression);
    }

    protected JavaCodeFragment getNewInstanceExpression(DatatypeHelper datatypeHelper) {
        return getNewInstanceExpression(datatypeHelper, "");
    }

    protected JavaCodeFragment getNewInstanceExpression(DatatypeHelper datatypeHelper, String expression) {
        if (getAttribute().isMultiValueAttribute()) {
            return new ListOfValueDatatypeHelper(getDatatype()).newInstance(expression);
        } else {
            return datatypeHelper.newInstance(getAttribute().getDefaultValue());
        }
    }

    public boolean isValidAttribute() throws CoreException {
        return !getAttribute().validate(getAttribute().getIpsProject()).containsErrorMsg();
    }

    public void generateDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        generateGetElementFromConfigMapAndIfStatement(builder);
        generateExtractValueFromXml(builder);
        builder.closeBracket(); // close if statement generated two lines above
    }

    private void generateGetElementFromConfigMapAndIfStatement(JavaCodeFragmentBuilder builder) {
        builder.append("configElement = ");
        if (!isUseTypesafeCollections()) {
            builder.append("(");
            builder.appendClassName(Element.class);
            builder.append(")");
        }
        builder.append("configMap.get(\"");
        builder.append(getAttribute().getName());
        builder.appendln("\");");
        builder.append("if (configElement != null) ");
        builder.openBracket();
    }

    private void generateExtractValueFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        if (getAttribute().isMultiValueAttribute()) {
            generateExtractMultipleValuesFromXml(builder);
        } else {
            generateExtractSingleValueFromXml(builder);
        }
    }

    /**
     * Example code for String lists:
     * 
     * <pre>
     * List&lt;String&gt; valueList = MultiValueXmlHelper.getValuesFromXML(configElement);
     * this.multiValueString = valueList;
     * </pre>
     * 
     * Example code for other lists:
     * 
     * <pre>
     * List&lt;Integer&gt; valueList = new ArrayList&lt;Integer&gt;();
     * List&lt;String&gt; stringList = MultiValueXmlHelper.getValuesFromXML(configElement);
     * for (String stringValue : stringList) {
     *     int convertedValue = Integer.parseInt(stringValue);
     *     valueList.add(convertedValue);
     * }
     * this.multiInt = valueList;
     * </pre>
     */
    private void generateExtractMultipleValuesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getDeclarationJavaType(getDatatypeHelper()));
        builder.append(" valueList= ");
        if (getDatatype() instanceof StringDatatype) {
            /*
             * Optimization/avoidance of redundant code, as strings do not need to be "converted" to
             * another object.
             */
            builder.appendClassName(MultiValueXmlHelper.class);
            builder.append(".getValuesFromXML(configElement);");
        } else {
            builder.append(getNewInstanceExpression(getDatatypeHelper()));
            builder.append(";");
            builder.appendClassName(List.class);
            builder.appendGenerics(String.class);
            builder.append(" stringList= ");
            builder.appendClassName(MultiValueXmlHelper.class);
            builder.append(".getValuesFromXML(configElement);");
            builder.append("for(String stringValue:stringList){");
            builder.appendClassName(getDatatypeHelper().getJavaClassName());
            builder.append(" convertedValue= ");
            generateConvertValueFromStringVariable(builder, "stringValue");
            builder.append("valueList.add(convertedValue);");
            builder.append("}");
        }
        builder.append("this.").append(getMemberVarName());
        builder.append(" = valueList;");

    }

    protected void generateExtractSingleValueFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append("value = ");
        builder.appendClassName(ValueToXmlHelper.class);
        builder.appendln(".getValueFromElement(configElement, \"Value\");");
        builder.append("this.").append(getMemberVarName());
        builder.append(" = ");
        generateConvertValueFromStringVariable(builder, "value");
    }

    protected void generateConvertValueFromStringVariable(JavaCodeFragmentBuilder builder, String stringVariableName)
            throws CoreException {
        if (getDatatypeHelper() instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)getDatatypeHelper();
            IEnumType enumType = enumHelper.getEnumType();
            if (!enumType.isContainingValues()) {
                builder.append(enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(enumType,
                        stringVariableName, new JavaCodeFragment("getRepository()")));
                builder.appendln(";");
                return;
            }
        }
        builder.append(getDatatypeHelper().newInstanceFromExpression(stringVariableName));
        builder.appendln(";");
    }

    public void generateWritePropertyToXml(JavaCodeFragmentBuilder builder) {
        builder.append("attributeElement= element.getOwnerDocument().createElement(\"AttributeValue\");");
        builder.append("attributeElement.setAttribute(\"attribute\", \"");
        builder.append(getAttribute().getName());
        builder.append("\");");

        if (getAttribute().isMultiValueAttribute()) {
            generateWriteMultipleValuesToXml(builder);
        } else {
            generateWriteSingleValueToXml(builder);
        }

        builder.append("element.appendChild(attributeElement);");
    }

    private void generateWriteMultipleValuesToXml(JavaCodeFragmentBuilder builder) {
        if (getDatatype() instanceof StringDatatype) {
            builder.appendClassName(MultiValueXmlHelper.class);
            builder.append(".addValuesToElement(attributeElement, this.").append(getMemberVarName());
            builder.append(");");
        } else {
            builder.append("stringList= new ");
            builder.appendClassName(ArrayList.class);
            builder.appendGenerics(String.class);
            builder.append("();");
            builder.append("for(");
            builder.appendClassName(getObjectJavaClass());
            builder.append(" value:");
            builder.append("this.").append(getMemberVarName());
            builder.append("){String stringValue= ");
            builder.append((getDatatypeHelper()).getToStringExpression("value"));
            builder.append(";");
            builder.append("stringList.add(stringValue);");
            builder.append("}");
            builder.appendClassName(MultiValueXmlHelper.class);
            builder.append(".addValuesToElement(attributeElement, stringList);");
        }
    }

    protected String getObjectJavaClass() {
        if (getDatatype().isPrimitive()) {
            return getDatatype().getWrapperType().getJavaClassName();
        } else {
            return getDatatype().getJavaClassName();
        }
    }

    protected void generateWriteSingleValueToXml(JavaCodeFragmentBuilder builder) {
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".addValueToElement(");
        builder.append((getDatatypeHelper()).getToStringExpression(getMemberVarName()));
        builder.append(", attributeElement, \"Value\");");
    }

    /**
     * Generates the getter code for the attribute this is a generator for.
     * 
     * Code sample:
     * 
     * <pre>
     * public Money getPremium() {
     *     return getMotorProductGen().getPremium();
     * }
     * </pre>
     */
    public void generateCodeForPolicyCmptType(boolean generatesInterface, JavaCodeFragmentBuilder methodBuilder) {

        if (!generatesInterface) {
            String description = StringUtils.isEmpty(getDescriptionInGeneratorLanguage(getAttribute())) ? ""
                    : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR
                            + getDescriptionInGeneratorLanguage(getAttribute());
            methodBuilder
                    .javaDoc(
                            getLocalizedText("METHOD_GETVALUE_JAVADOC", new String[] { getAttribute().getName(),
                                    description }), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            generateGetterSignature(methodBuilder);
            methodBuilder.openBracket();
            methodBuilder.append("return ");
            if (getAttribute().isChangingOverTime()) {
                methodBuilder.append(((GenProductCmptType)getGenType()).getMethodNameGetProductCmptGeneration());
            } else {
                methodBuilder.append(((GenProductCmptType)getGenType()).getMethodNameGetProductCmpt());
            }
            methodBuilder.append("().");
            methodBuilder.append(getGetterMethodName());
            methodBuilder.append("();");
            methodBuilder.closeBracket();
        }
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        addMemberVarToGeneratedJavaElements(javaElements, generatedJavaType);
        addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        addSetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);

        if (getProductCmptType().isConfigurationForPolicyCmptType()) {
            IType javaTypePolicyCmptType = null;
            try {
                javaTypePolicyCmptType = findGeneratedJavaTypeForPolicyCmptType(false);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (javaTypePolicyCmptType != null) {
                addGetterMethodToGeneratedJavaElements(javaElements, javaTypePolicyCmptType);
            }
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        addStaticConstantToGeneratedJavaElements(javaElements, generatedJavaType);
        addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    private void addStaticConstantToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        if (isPublished()) {
            IField constantMember = generatedJavaType.getField(getStaticConstantPropertyName());
            javaElements.add(constantMember);
        }
    }

    /**
     * Searches and returns the Java type generated for the <tt>IPolicyCmptType</tt> configured by
     * the <tt>IProductCmptType</tt>.
     * <p>
     * Returns <tt>null</tt> if the <tt>IPolicyCmptType</tt> cannot be found.
     * 
     * @param forInterface Flag indicating whether to search for the published interface of the
     *            <tt>IPolicyCmptType</tt> (<tt>true</tt>) or for it's implementation (
     *            <tt>false</tt>).
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IPolicyCmptType</tt>.
     */
    public IType findGeneratedJavaTypeForPolicyCmptType(boolean forInterface) throws CoreException {
        BasePolicyCmptTypeBuilder policyCmptTypeBuilder = forInterface ? getGenType().getBuilderSet()
                .getPolicyCmptInterfaceBuilder() : getGenType().getBuilderSet().getPolicyCmptImplClassBuilder();

        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(getProductCmptType().getIpsProject());
        if (policyCmptType == null) {
            return null;
        }
        return policyCmptTypeBuilder.getGeneratedJavaTypes(policyCmptType).get(0);
    }

    /** Returns the <tt>IProductCmptType</tt> of the parent <tt>GenType</tt>. */
    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getGenType().getIpsPart();
    }

}
