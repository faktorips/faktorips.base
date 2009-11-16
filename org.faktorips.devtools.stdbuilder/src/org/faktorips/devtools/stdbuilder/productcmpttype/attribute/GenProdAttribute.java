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

package org.faktorips.devtools.stdbuilder.productcmpttype.attribute;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptTypePart;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.w3c.dom.Element;

/**
 * Abstract code generator for an attribute.
 * 
 * @author Daniel Hohenberger
 */
public class GenProdAttribute extends GenProductCmptTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenProdAttribute.class);

    protected IProductCmptTypeAttribute attribute;
    protected String attributeName;
    protected DatatypeHelper datatypeHelper;
    protected String staticConstantPropertyName;
    protected String memberVarName;

    public GenProdAttribute(GenProductCmptType genProductCmptType, IProductCmptTypeAttribute a) throws CoreException {
        super(genProductCmptType, a, LOCALIZED_STRINGS);
        attribute = a;
        attributeName = a.getName();
        datatypeHelper = a.getIpsProject().findDatatypeHelper(a.getDatatype());
        if (datatypeHelper == null) {
            throw new NullPointerException("No datatype helper found for " + a);
        }
        staticConstantPropertyName = getLocalizedText("FIELD_PROPERTY_NAME", StringUtils.upperCase(a.getName()));
        memberVarName = getJavaNamingConvention().getMemberVarName(attributeName);
    }

    public IProductCmptTypeAttribute getProductCmptTypeAttribute() {
        return attribute;
    }

    public DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
    }

    public ValueDatatype getDatatype() {
        return (ValueDatatype)datatypeHelper.getDatatype();
    }

    public String getJavaClassName() {
        return datatypeHelper.getJavaClassName();
    }

    public boolean isPublished() {
        return attribute.getModifier().isPublished();
    }

    public boolean isNotPublished() {
        return !isPublished();
    }

    public boolean isOverwritten() {
        return attribute.isOverwrite();
    }

    public boolean isDerived() {
        return attribute.isDerived();
    }

    public String getGetterMethodName() {
        return getJavaNamingConvention().getGetterMethodName(attributeName, getDatatype());
    }

    public String getSetterMethodName() {
        return getJavaNamingConvention().getSetterMethodName(attributeName, getDatatype());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Money getPremium()
     * </pre>
     */
    protected void generateGetterSignature(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(attributeName, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, getJavaClassName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the name of the field/member variable that stores the values for the
     * property/attribute.
     */
    public String getMemberVarName() throws CoreException {
        return memberVarName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            generateFieldValue(datatypeHelper, builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (!generatesInterface) {
            generateMethodGetValue(datatypeHelper, builder);
            generateMethodSetValue(datatypeHelper, builder);
        } else {
            generateMethodGetValueInterface(datatypeHelper, builder);
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
    private void generateMethodGetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetValue(datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getMemberVarName());
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
    void generateMethodGetValueInterface(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder)
            throws CoreException {
        String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>"
                + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
        String[] replacements = new String[] { attributeName, description };
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
    private void generateMethodSetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_SET_VALUE", attributeName, methodsBuilder);
        String methodName = getSetterMethodName();
        String[] paramNames = new String[] { "newValue" };
        String[] paramTypes = new String[] { datatypeHelper.getJavaClassName() };
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(getGenProductCmptType().generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + getMemberVarName());
        methodsBuilder.appendln(" = newValue;");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Integer getTaxRate()
     * </pre>
     */
    void generateSignatureGetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getGetterMethodName();
        builder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * private Integer taxRate;
     * </pre>
     */
    private void generateFieldValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder)
            throws CoreException {
        appendLocalizedJavaDoc("FIELD_VALUE", StringUtils.capitalize(attributeName), builder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(attribute.getDefaultValue());
        builder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(), getMemberVarName(),
                defaultValueExpression);
    }

    public boolean isValidAttribute() throws CoreException {
        return !attribute.validate(attribute.getIpsProject()).containsErrorMsg();
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
        builder.append(attributeName);
        builder.appendln("\");");
        builder.append("if (configElement != null) ");
        builder.openBracket();
    }

    private void generateExtractValueFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append("value = ");
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".getValueFromElement(configElement, \"Value\");");
        builder.append(getMemberVarName());
        builder.append(" = ");
        if (getDatatypeHelper() instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)getDatatypeHelper();
            if (!enumHelper.getEnumType().isContainingValues()) {
                builder.append(enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(
                        enumHelper.getEnumType(), "value", new JavaCodeFragment("getRepository()")));
                builder.appendln(";");
                return;
            }
        }
        builder.append(getDatatypeHelper().newInstanceFromExpression("value"));
        builder.appendln(";");
    }

    /**
     * Generates the getter code in the policy component type for the attribute this is a generator
     * for.
     * 
     * Code sample:
     * 
     * <pre>
     * public Money getPremium() {
     *     IMotorProductGen motorProductGen = getMotorProductGen();
     *     if (motorProductGen == null) {
     *         return null;
     *     }
     *     return motorProductGen.getPremium();
     * }
     * </pre>
     */
    public void generateCodeForPolicyCmptType(boolean generatesInterface, JavaCodeFragmentBuilder methodBuilder)
            throws CoreException {
        if (!generatesInterface) {
            String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR
                    + "<p>" + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
            methodBuilder.javaDoc(getLocalizedText("METHOD_GETVALUE_JAVADOC",
                    new String[] { attributeName, description }), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            generateGetterSignature(methodBuilder);
            methodBuilder.openBracket();

            // IMotorProductGen motorProductGen = getMotorProductGen();
            String typeName = getGenProductCmptType().getQualifiedClassNameForProductCmptTypeGen(true);
            String varName = StringUtils.uncapitalize(getGenProductCmptType()
                    .getUnqualifiedClassNameForProductCmptTypeGen(false));
            methodBuilder.appendClassName(typeName);
            methodBuilder.append(' ');
            methodBuilder.append(varName);
            methodBuilder.append(" = ");
            methodBuilder.append(getGenProductCmptType().getMethodNameGetProductCmptGeneration());
            methodBuilder.appendln("();");

            // null handling for none primitive data types
            if (!getDatatype().isPrimitive()) {
                // if (motorProductGen == null) {
                // return null;
                // }
                methodBuilder.append("if (");
                methodBuilder.append(varName);
                methodBuilder.appendln(" == null) {");
                methodBuilder.appendln("return null;");
                methodBuilder.appendln('}');
            }

            // return motorProductGen.getPremium();
            methodBuilder.append("return ");
            methodBuilder.append(varName);
            methodBuilder.append('.');
            methodBuilder.append(getGetterMethodName());
            methodBuilder.appendln("();");

            methodBuilder.closeBracket();
        }
    }
}
