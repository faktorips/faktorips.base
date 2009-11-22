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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.w3c.dom.Element;

/**
 * Abstract code generator for an attribute.
 * 
 * @author Daniel Hohenberger
 */
public class GenProductCmptTypeAttribute extends GenAttribute {

    public GenProductCmptTypeAttribute(GenProductCmptType genProductCmptType, IProductCmptTypeAttribute a)
            throws CoreException {

        super(genProductCmptType, a, new LocalizedStringsSet(GenProductCmptTypeAttribute.class));
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
        String methodName = getMethodNameGetPropertyValue(getAttribute().getName(), getDatatype());
        methodsBuilder.signature(modifier, getJavaClassName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        // Nothing to do.
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

        String description = StringUtils.isEmpty(getAttribute().getDescription()) ? "" : SystemUtils.LINE_SEPARATOR
                + "<p>" + SystemUtils.LINE_SEPARATOR + getAttribute().getDescription();
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
    private void generateMethodSetValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_VALUE", getAttribute().getName(), methodsBuilder);
        String methodName = getSetterMethodName();
        String[] paramNames = new String[] { "newValue" };
        String[] paramTypes = new String[] { datatypeHelper.getJavaClassName() };
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(((GenProductCmptType)getGenType()).generateFragmentCheckIfRepositoryIsModifiable());
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
        appendLocalizedJavaDoc("FIELD_VALUE", StringUtils.capitalize(getAttribute().getName()), builder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(getAttribute().getDefaultValue());
        builder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(), getMemberVarName(),
                defaultValueExpression);
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
    public void generateCodeForPolicyCmptType(boolean generatesInterface, JavaCodeFragmentBuilder methodBuilder)
            throws CoreException {

        if (!generatesInterface) {
            String description = StringUtils.isEmpty(getAttribute().getDescription()) ? "" : SystemUtils.LINE_SEPARATOR
                    + "<p>" + SystemUtils.LINE_SEPARATOR + getAttribute().getDescription();
            methodBuilder.javaDoc(getLocalizedText("METHOD_GETVALUE_JAVADOC", new String[] { getAttribute().getName(),
                    description }), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            generateGetterSignature(methodBuilder);
            methodBuilder.openBracket();
            methodBuilder.append("return ");
            methodBuilder.append(((GenProductCmptType)getGenType()).getMethodNameGetProductCmptGeneration());
            methodBuilder.append("().");
            methodBuilder.append(getGetterMethodName());
            methodBuilder.append("();");
            methodBuilder.closeBracket();
        }
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        addMemberVarToGeneratedJavaElements(javaElements, generatedJavaType);
        addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        addSetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        addGetterMethodToGeneratedJavaElements(javaElements, generatedJavaType);
    }

}
