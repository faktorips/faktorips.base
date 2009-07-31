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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptTypePart;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract code generator for an attribute.
 * 
 * @author Jan Ortmann
 */
public abstract class GenAttribute extends GenPolicyCmptTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenAttribute.class);

    private IProductCmptType productCmptType;

    protected IAttribute attribute;
    protected String attributeName;
    protected DatatypeHelper datatypeHelper;
    protected String staticConstantPropertyName;
    protected String memberVarName;


    public GenAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a) throws CoreException {
        super(genPolicyCmptType, a, LOCALIZED_STRINGS);
        attribute = a;
        attributeName = a.getName();
        datatypeHelper = a.getIpsProject().findDatatypeHelper(a.getDatatype());
        if (datatypeHelper == null) {
            throw new NullPointerException("No datatype helper found for " + a);
        }
        staticConstantPropertyName = getLocalizedText("FIELD_PROPERTY_NAME", StringUtils.upperCase(a.getName()));
        memberVarName = getJavaNamingConvention().getMemberVarName(attributeName);
    }

    public void generate(boolean generatesInterface, IIpsProject ipsProject, TypeSection mainSection)
            throws CoreException {
        if (generatesInterface && !getPolicyCmptTypeAttribute().getModifier().isPublished()) {
            return;
        }
        super.generate(generatesInterface, ipsProject, mainSection);
    }

    public IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute() {
        return (IPolicyCmptTypeAttribute)attribute;
    }

    protected IProductCmptType getProductCmptType(IIpsProject ipsProject) throws CoreException {
        if (productCmptType == null) {
            productCmptType = getPolicyCmptTypeAttribute().getPolicyCmptType().findProductCmptType(ipsProject);
        }
        return productCmptType;
    }

    public ValueDatatype getDatatype() {
        return (ValueDatatype)datatypeHelper.getDatatype();
    }

    public DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
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

    public boolean isConfigurableByProduct() {
        return getPolicyCmptTypeAttribute().isProductRelevant();
    }

    public boolean isDerivedOnTheFly() {
        return getPolicyCmptTypeAttribute().getAttributeType() == AttributeType.DERIVED_ON_THE_FLY;
    }

    public boolean isDerivedByExplicitMethodCall() {
        return getPolicyCmptTypeAttribute().getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    public String getGetterMethodName() {
        return getJavaNamingConvention().getGetterMethodName(attributeName, getDatatype());
    }

    public String getSetterMethodName() {
        return getJavaNamingConvention().getSetterMethodName(attributeName, getDatatype());
    }

    public String getStaticConstantPropertyName() {
        return staticConstantPropertyName;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public final static String PROPERTY_PREMIUM = &quot;premium&quot;;
     * </pre>
     */
    protected void generateAttributeNameConstant(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_PROPERTY_NAME", attributeName, builder);
        builder.append("public final static ");
        builder.appendClassName(String.class);
        builder.append(' ');
        builder.append(staticConstantPropertyName);
        builder.append(" = ");
        builder.appendQuoted(attributeName);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Money getPremium();
     * </pre>
     */
    protected void generateGetterInterface(JavaCodeFragmentBuilder builder) throws CoreException {
        String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>"
                + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
        
        String[] replacements = new String[] { attributeName, description };
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
    protected void generateGetterImplementation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateGetterSignature(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(datatypeHelper.referenceOrSafeCopyIfNeccessary(memberVarName));
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }

    /**
     * Returns <code>true</code> if a member variable is required to the type of attribute. This
     * is currently the case for changeable attributes and attributes that are derived by an
     * explicit method call.
     */
    public boolean isMemberVariableRequired() {
        return (getPolicyCmptTypeAttribute().isChangeable() || isDerivedByExplicitMethodCall()) && !isOverwritten();
    }

    public boolean needsToBeConsideredInDeltaComputation() {
        return isPublished() && isMemberVariableRequired() && !isOverwritten();
    }

    public void generateDeltaComputation(JavaCodeFragmentBuilder methodsBuilder, String deltaVar, String otherVar)
            throws CoreException {
        methodsBuilder.append(deltaVar);
        methodsBuilder.append('.');
        methodsBuilder.append(MethodNames.MODELOBJECTDELTA_CHECK_PROPERTY_CHANGE);
        methodsBuilder.append("(");
        methodsBuilder.appendClassName(getGenPolicyCmptType().getQualifiedName(true));
        methodsBuilder.append(".");
        methodsBuilder.append(staticConstantPropertyName);
        methodsBuilder.append(", ");
        methodsBuilder.append(memberVarName);
        methodsBuilder.append(", ");
        methodsBuilder.append(otherVar);
        methodsBuilder.append(".");
        methodsBuilder.append(memberVarName);
        methodsBuilder.appendln(", options);");
    }

    public void generateInitPropertiesFromXml(JavaCodeFragmentBuilder builder, JavaCodeFragment repositoryExpression)
            throws CoreException {
        String propMapName = "propMap";
        builder.append("if (" + propMapName + ".containsKey(");
        builder.appendQuoted(attributeName);
        builder.appendln(")) {");
        String expr = (isUseTypesafeCollections() ? "" : "(String)") + propMapName + ".get(\"" + attributeName + "\")";
        builder.append(getMemberVarName() + " = ");
        if (datatypeHelper instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)datatypeHelper;
            if (!enumHelper.getEnumType().isContainingValues()) {
                builder.append(enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(enumHelper.getEnumType(), expr,
                        repositoryExpression));
                builder.appendln(";");
                builder.appendln("}");
                return;
            }
        }
        builder.append(datatypeHelper.newInstanceFromExpression(expr));
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
    protected void generateGetterSignature(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(attributeName, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, getJavaClassName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    protected void generateField(JavaCodeFragmentBuilder memberVarsBuilders) throws CoreException {
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(attribute.getDefaultValue());
        String comment = getLocalizedText("FIELD_ATTRIBUTE_VALUE_JAVADOC", attributeName);
        String fieldName = getMemberVarName();

        memberVarsBuilders.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);

        if (isGenerateJaxbSupport()) {
            String annotationParam = "name=\"" + attributeName + "\"";
            if (!datatypeHelper.getDatatype().isPrimitive()) {
                annotationParam += ",nillable=true";
            }
            memberVarsBuilders.annotationLn("javax.xml.bind.annotation.XmlElement", annotationParam);
            if (datatypeHelper instanceof EnumTypeDatatypeHelper) {
                EnumXmlAdapterBuilder xmlAdapterBuilder = getGenPolicyCmptType().getBuilderSet().getBuildersByClass(EnumXmlAdapterBuilder.class).get(0);
                EnumTypeDatatypeHelper enumDatatypeHelper = (EnumTypeDatatypeHelper)datatypeHelper;
                if (!enumDatatypeHelper.getEnumType().isContainingValues()) {
                    memberVarsBuilders.annotationClassValueLn("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter", "value",
                            xmlAdapterBuilder.getQualifiedClassName(enumDatatypeHelper.getEnumType()));
                }
            }
        }

        memberVarsBuilders.varDeclaration(java.lang.reflect.Modifier.PRIVATE, getJavaClassName(), fieldName,
                initialValueExpression);
    }

    /**
     * Returns the name of the field/member variable that stores the values for the
     * property/attribute.
     */
    public String getMemberVarName() throws CoreException {
        return memberVarName;
    }

    public String getFieldNameDefaultValue() throws CoreException {
        return getJavaNamingConvention().getMemberVarName(getPropertyNameDefaultValue());
    }

    String getPropertyNameDefaultValue() {
        return getLocalizedText("PROPERTY_DEFAULTVALUE_NAME", StringUtils.capitalize(getPolicyCmptTypeAttribute()
                .getName()));
    }

    public String getFieldNameRangeFor() {
        return getLocalizedText("FIELD_RANGE_FOR_NAME", StringUtils.capitalize(getPolicyCmptTypeAttribute().getName()));
    }

    public String getFieldNameAllowedValuesFor() {
        return getLocalizedText("FIELD_ALLOWED_VALUES_FOR_NAME", StringUtils.capitalize(getPolicyCmptTypeAttribute()
                .getName()));
    }

    public String getMethodNameGetAllowedValuesFor() throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_ALLOWED_VALUES_FOR_NAME", StringUtils.capitalize(attributeName)),
                getDatatype());
    }

    public String getMethodNameGetRangeFor() throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_RANGE_FOR_NAME", StringUtils.capitalize(attributeName)), getDatatype());
    }

}
