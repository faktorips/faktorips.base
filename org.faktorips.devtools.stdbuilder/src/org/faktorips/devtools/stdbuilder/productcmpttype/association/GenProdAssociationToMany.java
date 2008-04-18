/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype.association;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.BaseProductCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;
import org.w3c.dom.Element;

/**
 * 
 * @author Daniel Hohenberger
 */
public class GenProdAssociationToMany extends GenProdAssociation {

    /**
     * @param association
     * @param builder
     * @param stringsSet
     * @throws CoreException
     */
    public GenProdAssociationToMany(IProductCmptTypeAssociation association, BaseProductCmptTypeBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(association, builder, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        if (generatesInterface) {

        } else {

        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {

        } else {
            generateFieldToManyAssociation(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(getIpsProject()) != null) {
                generateFieldCardinalityForAssociation(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, boolean generatesInterface) throws CoreException {
        if (generatesInterface) {
            generateMethodInterfaceGetManyRelatedCmpts(builder);
            generateMethodInterfaceGetRelatedCmptAtIndex(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(getIpsProject()) != null) {
                generateMethodGetCardinalityForAssociation(builder);
            }
            generateMethodGetNumOfRelatedCmpts(builder);
        } else {
            generateMethodGetManyRelatedCmpts(builder);
            generateMethodGetRelatedCmptAtIndex(builder);
            generateMethodAddRelatedCmpt(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(getIpsProject()) != null) {
                generateMethodGetCardinalityFor1ToManyAssociation(builder);
            }
            generateMethodGetNumOfRelatedProductCmpts(builder);
        }
    }

    /**
     * Code sample for
     * 
     * <pre>
     * [javadoc]
     * private CoverageType[] optionalCoverageTypes;
     * </pre>
     */
    private void generateFieldToManyAssociation(JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        appendLocalizedJavaDoc("FIELD_TOMANY_RELATION", role, memberVarsBuilder);
        String type = String.class.getName() + "[]";
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyAssociation(), new JavaCodeFragment(
                "new String[0]"));
    }

    private String getFieldNameToManyAssociation() throws CoreException {
        return getJavaNamingConvention().getMultiValueMemberVarName(getPropertyNameToManyAssociation(association));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[coverageTypes.length];
     *     for (int i = 0; i &lt; result.length; i++) {
     *         result[i] = (ICoverageType) getRepository().getProductComponent(coverageTypes[i]);
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetManyRelatedCmpts(association, methodsBuilder);

        String fieldName = getFieldNameToManyAssociation();
        String targetClass = getProductCmptInterfaceBuilder().getQualifiedClassName(
                association.findTarget(getIpsProject()));
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".length];");

        methodsBuilder.appendln("for (int i=0; i<result.length; i++) {");
        methodsBuilder.appendln("result[i] = (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln("[i]);");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return result;");

        methodsBuilder.closeBracket();
    }

    /**
     * Code sample: [Javadoc]
     * 
     * <pre>
     * public CoverageType[] getCoverageTypes();
     * </pre>
     */
    private void generateMethodInterfaceGetManyRelatedCmpts(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetManyRelatedCmpts(association, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample for
     * 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType(int index) {
     *     return (ICoverageType) getRepository().getProductComponent(coverageTypes[index]);
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptAtIndex(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRelatedCmptsAtIndex(methodsBuilder);
        String fieldName = getFieldNameToManyAssociation();
        String targetClass = getProductCmptInterfaceBuilder().getQualifiedClassName(
                association.findTarget(getIpsProject()));
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append("[index]);");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public CoverageType getCoverageType(int index);
     * </pre>
     */
    void generateMethodInterfaceGetRelatedCmptAtIndex(JavaCodeFragmentBuilder builder) throws CoreException {
        String role = association.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_RELATED_CMPT_AT_INDEX", role, builder);
        generateSignatureGetRelatedCmptsAtIndex(builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public CoverageType getCoverageType(int index)
     * </pre>
     */
    void generateSignatureGetRelatedCmptsAtIndex(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetRelatedCmptAtIndex();
        IProductCmptType target = association.findTargetProductCmptType(getIpsProject());
        String returnType = getProductCmptInterfaceBuilder().getQualifiedClassName(target);
        builder.signature(Modifier.PUBLIC, returnType, methodName, new String[] { "index" }, new String[] { "int" });
    }

    public String getMethodNameGetRelatedCmptAtIndex() {
        return getJavaNamingConvention().getGetterMethodName(association.getTargetRoleSingular(), Datatype.INTEGER);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType[] target) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     String[] tmp = new String[coverageTypes.length+1];
     *     System.arraycopy(coverageTypes, 0, tmp, 0, coverageTypes.length);
     *     tmp[tmp.length-1] = target.getId();
     *     coverageTypes = tmp;
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        IProductCmptType target = association.findTargetProductCmptType(getIpsProject());
        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT", association.getTargetRoleSingular(), methodsBuilder);
        String methodName = "add" + StringUtils.capitalize(association.getTargetRoleSingular());
        String[] argNames = new String[] { "target" };
        String[] argTypes = new String[] { getProductCmptInterfaceBuilder().getQualifiedClassName(target) };
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), "void", methodName,
                argNames, argTypes);
        String fieldName = getFieldNameToManyAssociation();
        methodsBuilder.openBracket();
        methodsBuilder.append(getGenImplClassBuilder().generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.appendln("String[] tmp = new String[this." + fieldName + ".length+1];");
        methodsBuilder.appendln("System.arraycopy(this." + fieldName + ", 0, tmp, 0, this." + fieldName + ".length);");
        methodsBuilder.appendln("tmp[tmp.length-1] = " + argNames[0] + "." + MethodNames.GET_PRODUCT_COMPONENT_ID
                + "();");
        methodsBuilder.appendln("this." + fieldName + " = tmp;");
        methodsBuilder.closeBracket();
    }

    protected void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder)
            throws CoreException {
        builder.append(getMethodNameGetNumOfRelatedCmpts());
        builder.append("();");
    }

    protected void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getFieldNameToManyAssociation());
        builder.appendln(".length;");
    }

    protected void generateCodeGetRelatedCmptsInContainer(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String objectArrayVar = getFieldNameToManyAssociation() + "Objects";
        String getterMethod = getMethodNameGetManyRelatedCmpts(association) + "()";
        methodsBuilder.appendClassName(getProductCmptInterfaceBuilder().getQualifiedClassName(
                association.findTarget(getIpsProject())));
        methodsBuilder.append("[] " + objectArrayVar + " = " + getterMethod + ";");
        methodsBuilder.appendln("for (int i=0; i<" + objectArrayVar + ".length; i++) {");
        methodsBuilder.appendln("result[index++] = " + objectArrayVar + "[i];");
        methodsBuilder.appendln("}");
    }

    /**
     * {@inheritDoc}
     */
    public void generateCodeForDerivedUnionAssociationDefinition(JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        super.generateCodeForDerivedUnionAssociationDefinition(methodsBuilder);
        generateMethodGetNumOfRelatedCmpts(methodsBuilder);
    }

    public void generateCodeForDerivedUnionAssociationImplementation(List implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        super.generateCodeForDerivedUnionAssociationImplementation(implAssociations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmpts(implAssociations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmptsInternal(implAssociations, methodsBuilder);
    }

    public void generateCodeForMethodDoInitReferencesFromXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) throws CoreException {
        String cardinalityFieldName = policyCmptTypeAssociation == null ? "" : getFieldNameCardinalityForAssociation();
        String fieldName = getFieldNameToManyAssociation();
        builder.append(fieldName);
        builder.appendln(" = new ");
        builder.appendClassName(String.class);
        builder.appendln("[associationElements.size()];");
        if (policyCmptTypeAssociation != null) {
            builder.append(cardinalityFieldName);
            builder.append(" = new ");
            builder.appendClassName(HashMap.class);
            builder.appendln("(associationElements.size());");
        }
        builder.appendln("for (int i=0; i<associationElements.size(); i++) {");
        builder.appendClassName(Element.class);
        builder.append(" element = (");
        builder.appendClassName(Element.class);
        builder.appendln(")associationElements.get(i);");
        builder.append(fieldName);
        builder.append("[i] = ");
        builder.appendln("element.getAttribute(\"" + ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID
                + "\");");
        if (policyCmptTypeAssociation != null) {
            builder.append("addToCardinalityMap(");
            builder.append(cardinalityFieldName);
            builder.append(", ");
            builder.append(fieldName);
            builder.append("[i], ");
            builder.appendln("element);");
        }
        builder.appendln("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverageTypes();
     * </pre>
     */
    void generateMethodGetNumOfRelatedCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        String role = association.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_NUM_OF_RELATED_CMPTS", role, builder);
        generateSignatureGetNumOfRelatedCmpts(builder);
        builder.appendln(";");
    }

}
