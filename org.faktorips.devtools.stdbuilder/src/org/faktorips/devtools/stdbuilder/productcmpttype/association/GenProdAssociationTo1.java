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
public class GenProdAssociationTo1 extends GenProdAssociation {

    /**
     * @param part
     * @param builder
     * @param stringsSet
     * @throws CoreException
     */
    public GenProdAssociationTo1(GenProductCmptType genProductCmptType, IProductCmptTypeAssociation association,
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
            generateFieldTo1Association(builder);
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
            generateMethodInterfaceGet1RelatedCmpt(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(ipsProject) != null) {
                generateMethodGetCardinalityForAssociation(builder);
            }
        } else {
            generateMethodGet1RelatedCmpt(builder);
            generateMethodSet1RelatedCmpt(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(ipsProject) != null) {
                generateMethodGetCardinalityFor1ToManyAssociation(builder);
            }
        }
    }

    private String getFieldNameTo1Association() throws CoreException {
        return getJavaNamingConvention().getMemberVarName(getPropertyNameTo1Association());
    }

    String getPropertyNameTo1Association() {
        String role = StringUtils.capitalize(association.getTargetRoleSingular());
        return getLocalizedText("PROPERTY_TO1_RELATION_NAME", role);
    }

    protected void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder)
            throws CoreException {
        builder.append(getFieldNameTo1Association() + "==null ? 0 : 1;");
    }

    protected void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getFieldNameTo1Association());
        builder.appendln(" ==null ? 0 : 1;");
    }

    /**
     * Code sample for
     * 
     * <pre>
     * [javadoc]
     * private CoverageType mainCoverage;
     * </pre>
     */
    private void generateFieldTo1Association(JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRoleSingular());
        appendLocalizedJavaDoc("FIELD_TO1_RELATION", role, memberVarsBuilder);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, String.class, getFieldNameTo1Association(),
                new JavaCodeFragment("null"));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType() {
     *     return (CoveragePk) getRepository().getProductComponent(mainCoverageType);
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGet1RelatedCmpt(methodsBuilder);
        String fieldName = getFieldNameTo1Association();
        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public CoverageType getMainCoverageType();
     * </pre>
     */
    void generateMethodInterfaceGet1RelatedCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_1_RELATED_CMPT", association.getTargetRoleSingular(), builder);
        generateSignatureGet1RelatedCmpt(builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * public void setMainCoverageType(ICoverageType target) {
     *     mainCoverageType = target==null ? null : target.getId();
     * }
     * </pre>
     */
    private void generateMethodSet1RelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_1_RELATED_CMPT", association.getTargetRoleSingular(), methodsBuilder);

        String propName = getPropertyNameTo1Association();
        String methodName = getJavaNamingConvention().getSetterMethodName(propName, Datatype.INTEGER);
        String[] argNames = new String[] { "target" };
        String[] argTypes = new String[] { getQualifiedInterfaceClassNameForTarget() };
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameTo1Association();
        methodsBuilder.openBracket();
        methodsBuilder.append(getGenProductCmptType().generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append(fieldName + " = (" + argNames[0] + "==null ? null : " + argNames[0] + "."
                + MethodNames.GET_PRODUCT_COMPONENT_ID + "() );");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public CoverageType getMainCoverageType()
     * </pre>
     */
    void generateSignatureGet1RelatedCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGet1RelatedCmpt();
        String returnType = getQualifiedInterfaceClassNameForTarget();
        builder.signature(Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    String getMethodNameGet1RelatedCmpt() throws CoreException {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameTo1Association(), Datatype.INTEGER);
    }

    protected void generateCodeGetRelatedCmptsInContainer(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String accessCode;
        if (association.isDerivedUnion()) {
            // if the implementation association is itself a container association, use the access
            // method
            accessCode = getMethodNameGet1RelatedCmpt() + "()";
        } else {
            // otherwise use the field.
            accessCode = getFieldNameTo1Association();
        }
        methodsBuilder.appendln("if (" + accessCode + "!=null) {");
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendln("result.add(" + getMethodNameGet1RelatedCmpt() + "());");
        } else {
            methodsBuilder.appendln("result[index++] = " + getMethodNameGet1RelatedCmpt() + "();");
        }
        methodsBuilder.appendln("}");
    }

    public void generateCodeForMethodDoInitReferencesFromXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) throws CoreException {
        String cardinalityFieldName = policyCmptTypeAssociation == null ? "" : getFieldNameCardinalityForAssociation();
        String fieldName = getFieldNameTo1Association();
        builder.appendClassName(Element.class);
        builder.append(" element = ");
        if(!isUseTypesafeCollections()){
            builder.append("(");
            builder.appendClassName(Element.class);
            builder.appendln(")");
        }
        builder.appendln("associationElements.get(0);");
        builder.append(fieldName);
        builder.append(" = ");
        builder.appendln("element.getAttribute(\"" + ProductCmptGenImplClassBuilder.XML_ATTRIBUTE_TARGET_RUNTIME_ID
                + "\");");
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
            builder.appendln("(1);");
            if(isUseTypesafeCollections()){
                builder.append(Java5ClassNames.ProductComponentGeneration_QualifiedName); // don't append as classname, the include would collide with the original
                builder.append(".");
            }
            builder.append("addToCardinalityMap(");
            builder.append(cardinalityFieldName);
            builder.append(", ");
            builder.append(fieldName);
            builder.appendln(", element);");
        }
    }

}
