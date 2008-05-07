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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
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
    public GenProdAssociationToMany(GenProductCmptType genProductCmptType, IProductCmptTypeAssociation association,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(genProductCmptType, association, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {

        } else {

        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (generatesInterface) {

        } else {
            generateFieldToManyAssociation(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(ipsProject) != null) {
                generateFieldCardinalityForAssociation(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            generateMethodInterfaceGetManyRelatedCmpts(builder);
            generateMethodInterfaceGetRelatedCmptAtIndex(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(ipsProject) != null) {
                generateMethodGetCardinalityForAssociation(builder);
            }
            generateMethodGetNumOfRelatedCmpts(builder);
        } else {
            generateMethodGetManyRelatedCmpts(builder);
            generateMethodGetRelatedCmptAtIndex(builder);
            generateMethodAddRelatedCmpt(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(ipsProject) != null) {
                generateMethodGetCardinalityFor1ToManyAssociation(builder);
            }
            generateMethodGetNumOfRelatedProductCmpts(builder);
        }
    }

    /**
     * Code sample
     * 
     * <pre>
     * [javadoc]
     * private String[] optionalCoverageTypes;
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * [javadoc]
     * private List&lt;String&gt; optionalCoverageTypes;
     * </pre>
     */
    private void generateFieldToManyAssociation(JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        appendLocalizedJavaDoc("FIELD_TOMANY_RELATION", role, memberVarsBuilder);
        if (isUseTypesafeCollections()) {
            String type = List.class.getName() + "<" + String.class.getName() + ">";
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("new ");
            fragment.appendClassName(ArrayList.class.getName());
            fragment.append("<");
            fragment.appendClassName(String.class.getName());
            fragment.append(">(0)");
            memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyAssociation(), fragment);
        } else {
            String type = String.class.getName() + "[]";
            memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyAssociation(),
                    new JavaCodeFragment("new String[0]"));
        }
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
     *     for (int i = 0; i &lt; coverageTypes.length; i++) {
     *         result[i] = (ICoverageType) getRepository().getProductComponent(coverageTypes[i]);
     *     }
     *     return result;
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverageType&gt; getCoverageTypes() {
     *     List&lt;ICoverageType&gt; result = new ArrayList&lt;ICoverageType&gt;(coverageTypes.size());
     *     for (String coverageType: coverageTypes) {
     *         result.add((ICoverageType) getRepository().getProductComponent(coverageType));
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetManyRelatedCmpts(association, methodsBuilder);

        String fieldName = getFieldNameToManyAssociation();
        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendClassName(List.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("> result = new ");
            methodsBuilder.appendClassName(ArrayList.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append(">(");
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln(".size());");
            methodsBuilder.append("for (String ");
            methodsBuilder.append(getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular()));
            methodsBuilder.append(" : ");
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln(") {");
            methodsBuilder.appendln("result.add((");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
            methodsBuilder.append(getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular()));
            methodsBuilder.appendln("));");
        } else {
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[] result = new ");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[");
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln(".length];");
            methodsBuilder.append("for (int i=0; i<result.length; i++) {");
            methodsBuilder.appendln("result[i] = (");
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln("[i]);");
        }
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
     * Code sample
     * 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType(int index) {
     *     return (ICoverageType) getRepository().getProductComponent(coverageTypes[index]);
     * }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType(int index) {
     *     return (ICoverageType) getRepository().getProductComponent(coverageTypes.get(index));
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptAtIndex(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRelatedCmptsAtIndex(methodsBuilder);
        String fieldName = getFieldNameToManyAssociation();
        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        if (isUseTypesafeCollections()) {
            methodsBuilder.append(".get(index));");
        } else {
            methodsBuilder.append("[index]);");
        }
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
        String returnType = getQualifiedInterfaceClassNameForTarget();
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
     * public void addCoverageType(ICoverageType target) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     String[] tmp = new String[this.coverageTypes.length+1];
     *     System.arraycopy(coverageTypes, 0, tmp, 0, this.coverageTypes.length);
     *     tmp[tmp.length-1] = target.getId();
     *     this.coverageTypes = tmp;
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType target) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.coverageTypes.add(target.getId());
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT", association.getTargetRoleSingular(), methodsBuilder);
        String methodName = "add" + StringUtils.capitalize(association.getTargetRoleSingular());
        String[] argNames = new String[] { "target" };
        String[] argTypes = new String[] { getQualifiedInterfaceClassNameForTarget() };
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), "void", methodName,
                argNames, argTypes);
        String fieldName = getFieldNameToManyAssociation();
        methodsBuilder.openBracket();
        methodsBuilder.append(getGenProductCmptType().generateFragmentCheckIfRepositoryIsModifiable());
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendln("this." + fieldName + ".add(target.getId());");
        } else {
            methodsBuilder.appendln("String[] tmp = new String[this." + fieldName + ".length+1];");
            methodsBuilder.appendln("System.arraycopy(this." + fieldName + ", 0, tmp, 0, this." + fieldName
                    + ".length);");
            methodsBuilder.appendln("tmp[tmp.length-1] = " + argNames[0] + "." + MethodNames.GET_PRODUCT_COMPONENT_ID
                    + "();");
            methodsBuilder.appendln("this." + fieldName + " = tmp;");
        }
        methodsBuilder.closeBracket();
    }

    protected void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder)
            throws CoreException {
        builder.append(getMethodNameGetNumOfRelatedCmpts());
        builder.append("();");
    }

    protected void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getFieldNameToManyAssociation());
        if (isUseTypesafeCollections()) {
            builder.appendln(".size();");
        }else{
            builder.appendln(".length;");
        }
    }

    protected void generateCodeGetRelatedCmptsInContainer(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String objectArrayVar = getFieldNameToManyAssociation() + "Objects";
        String getterMethod = getMethodNameGetManyRelatedCmpts(association) + "()";
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendClassName(List.class.getName());
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(getQualifiedInterfaceClassNameForTarget());
            methodsBuilder.append("> " + objectArrayVar + " = " + getterMethod + ";");
            methodsBuilder.appendln("for (");
            methodsBuilder.appendClassName(getQualifiedInterfaceClassNameForTarget());
            methodsBuilder.appendln(" " + getFieldNameToManyAssociation() + "Object : " + objectArrayVar + ") {");
            methodsBuilder.appendln("result.add(" + getFieldNameToManyAssociation() + "Object);");
            methodsBuilder.appendln("}");
        } else {
            methodsBuilder.appendClassName(getQualifiedInterfaceClassNameForTarget());
            methodsBuilder.append("[] " + objectArrayVar + " = " + getterMethod + ";");
            methodsBuilder.appendln("for (int i=0; i<" + objectArrayVar + ".length; i++) {");
            methodsBuilder.appendln("result[index++] = " + objectArrayVar + "[i];");
            methodsBuilder.appendln("}");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void generateCodeForDerivedUnionAssociationDefinition(JavaCodeFragmentBuilder methodsBuilder)
            throws Exception {
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
        if (isUseTypesafeCollections()) {
            builder.appendClassName(ArrayList.class.getName());
            builder.append("<");
            builder.appendClassName(String.class.getName());
            builder.append(">(associationElements.size());");
        }else{
            builder.appendClassName(String.class);
            builder.appendln("[associationElements.size()];");
        }
        if (policyCmptTypeAssociation != null) {
            builder.append(cardinalityFieldName);
            builder.append(" = new ");
            builder.appendClassName(HashMap.class);
            if (isUseTypesafeCollections()) {
                builder.append("<");
                builder.appendClassName(String.class.getName());
                builder.append(", ");
                builder.appendClassName(Java5ClassNames.IntegerRange_QualifiedName);
                builder.append(">");
            }
            builder.appendln("(associationElements.size());");
        }
        builder.appendln("for (int i=0; i<associationElements.size(); i++) {");
        builder.appendClassName(Element.class);
        builder.append(" element = ");
        if(!isUseTypesafeCollections()){
            builder.append("(");
            builder.appendClassName(Element.class);
            builder.appendln(")");
        }
        builder.appendln("associationElements.get(i);");
        builder.append(fieldName);
        if (isUseTypesafeCollections()) {
            builder.append(".add(");
            builder.appendln("element.getAttribute(\"" + ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID
                    + "\"));");
        }else{
            builder.append("[i] = ");
            builder.appendln("element.getAttribute(\"" + ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID
                    + "\");");
        }
        if (policyCmptTypeAssociation != null) {
            if(isUseTypesafeCollections()){
                builder.append(Java5ClassNames.ProductComponentGeneration_QualifiedName); // don't append as classname, the include would collide with the original
                builder.append(".");
            }
            builder.append("addToCardinalityMap(");
            builder.append(cardinalityFieldName);
            builder.append(", ");
            builder.append(fieldName);
            if (isUseTypesafeCollections()) {
                builder.append(".get(i), ");
            }else{
                builder.append("[i], ");
            }
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
