/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype.association;

import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.w3c.dom.Element;

/**
 * 
 * @author Daniel Hohenberger
 */
public class GenProdAssociationTo1 extends GenProdAssociation {

    public GenProdAssociationTo1(GenProductCmptType genProductCmptType, IProductCmptTypeAssociation association)
            throws CoreException {
        super(genProductCmptType, association);
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        // nothing to do
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            generateFieldTo1Association(builder);
        }
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            generateMethodInterfaceGet1RelatedCmpt(builder);
            generateMethodInterfaceGet1RelatedCmptGen(builder);
            generateMethodInterfaceGet1RelatedCmptLink(builder);
            generateMethodInterfaceGetRelatedCmptLink(builder);
            if (association.constrainsPolicyCmptTypeAssociation(ipsProject)) {
                generateMethodGetCardinalityForAssociation(builder);
            }
        } else {
            generateMethodGet1RelatedCmpt(builder);
            generateMethodGet1RelatedCmptGen(builder);
            generateMethodSet1RelatedCmpt(builder);
            generateMethodGet1RelatedCmptLink(builder);
            generateMethodGetRelatedCmptLink(builder);
            if (association.constrainsPolicyCmptTypeAssociation(ipsProject)) {
                generateMethodGetCardinalityFor1To1Association(builder);
            }
        }
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     *  public ILink&lt;ICoverageType&gt; getLinkForMainCoverageType(){
     *      return mainCoverageType;
     *  }
     * </pre>
     */
    private void generateMethodGet1RelatedCmptLink(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGet1RelatedCmptLink(methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameTo1Association());
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     *  public Collection&lt;ILink&lt;ICoverageType&gt;&gt; getLinksForCoverageTypes(){
     *      return productPart.getTargetId().equals(productComponent.getId()) ? productPart : null;
     *  }
     * </pre>
     */
    private void generateMethodGetRelatedCmptLink(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRelatedCmptLink(methodsBuilder);

        String fieldName = getFieldNameTo1Association();
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".getTargetId().equals(productComponent.getId()) ? " + fieldName + " : null;");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ILink&lt;ICoverageType&gt; getLinkForMainCoverageType();
     * </pre>
     */
    private void generateMethodInterfaceGet1RelatedCmptLink(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_1_RELATED_CMPT_LINK", association.getTargetRoleSingular(), builder);
        generateSignatureGet1RelatedCmptLink(builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ILink&lt;ICoverageType&gt; getLinkForMainCoverageType()
     * </pre>
     */
    private void generateSignatureGet1RelatedCmptLink(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGet1RelatedCmptLink();
        String returnType = Java5ClassNames.ILink_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget() + ">";
        builder.signature(Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    private void generateMethodGetCardinalityFor1To1Association(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetCardinalityForAssociation(methodsBuilder);
        String[][] params = getParamGetCardinalityForAssociation();
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendOpenBracket();
        frag.append("if(");
        frag.append(params[0][0]);
        frag.append(" != null)");
        frag.appendOpenBracket();
        frag.append("return ");
        frag.append(getFieldNameTo1Association());
        frag.append(" != null && ");
        frag.append(getFieldNameTo1Association());
        frag.append(".getTargetId().equals(");
        frag.append(params[0][0]);
        frag.append(".getId()) ? ");
        frag.append(getFieldNameTo1Association());
        frag.append(".getCardinality() : null;");
        frag.appendCloseBracket();
        frag.append("return null;");
        frag.appendCloseBracket();
        methodsBuilder.append(frag);
    }

    @Override
    protected void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder)
            throws CoreException {
        builder.append(getFieldNameTo1Association() + "==null ? 0 : 1;");
    }

    @Override
    protected void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getFieldNameTo1Association());
        builder.appendln(" ==null ? 0 : 1;");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * private CoverageType mainCoverage;
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [javadoc]
     * private ILink&lt;ISpecificProductPart&gt; specificProductPart = null;
     * </pre>
     */
    private void generateFieldTo1Association(JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRoleSingular());
        appendLocalizedJavaDoc("FIELD_TO1_ASSOCIATION", role, memberVarsBuilder);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, Java5ClassNames.ILink_QualifiedName + "<"
                + getQualifiedInterfaceClassNameForTarget() + ">", getFieldNameTo1Association(), new JavaCodeFragment(
                "null"));
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
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType() {
     *     return mainCoverageType != null ? mainCoverageType.getTarget() : null;
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGet1RelatedCmpt(methodsBuilder);
        String fieldName = getFieldNameTo1Association();
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(" != null ? ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".getTarget() : null;");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * public ICoverageTypeGen getCoverageTypeGen(Calendar effectiveDate) {
     *    return ((ICoverageType)getRepository().getExistingProductComponent(coverageType))
     *            .getCoverageTypeGen(effectiveDate);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [javadoc]
     * public ICoverageTypeGen getCoverageTypeGen(Calendar effectiveDate) {
     *    return coverageType != null ? coverageType.getTarget().getCoverageTypeGen(effectiveDate) : null;
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmptGen(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGet1RelatedCmptGen(methodsBuilder);
        String fieldName = getFieldNameTo1Association();
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(" != null ? ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".getTarget().");
        methodsBuilder.append(getMethodNameGetProductCmptGenerationForTarget());
        methodsBuilder.append("(effectiveDate) : null;");
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
     * [Javadoc]
     * public ICoverageTypeGen getMainCoverageType();
     * </pre>
     */
    void generateMethodInterfaceGet1RelatedCmptGen(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_1_RELATED_CMPT_GEN", association.getTargetRoleSingular(), builder);
        generateSignatureGet1RelatedCmptGen(builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     *  public void setMainCoverageType(ICoverageType target) {
     *      if (getRepository() != null &amp;&amp; !getRepository().isModifiable()) {
     *          throw new IllegalRepositoryModificationException();
     *      }
     *      mainCoverageType = target==null ? null : target.getId();
     *  }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [javadoc]
     *  public void setMainCoverageType(ICoverageType target) {
     *      if (getRepository() != null &amp;&amp; !getRepository().isModifiable()) {
     *          throw new IllegalRepositoryModificationException();
     *      }
     *      mainCoverageType = (target == null ? null : new Link&lt;ICoverageType&gt;(this, target));
     *  }
     * </pre>
     */
    private void generateMethodSet1RelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_1_RELATED_CMPT", association.getTargetRoleSingular(), methodsBuilder);

        String methodName = getMethodNameSet1RelatedCmpt();
        String[] argNames = new String[] { "target" };
        String[] argTypes = new String[] { getQualifiedInterfaceClassNameForTarget() };
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameTo1Association();
        methodsBuilder.openBracket();
        methodsBuilder.append(((GenProductCmptType)getGenType()).generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append(fieldName + " = (" + argNames[0] + "==null ? null : ");
        methodsBuilder.append("new ");
        methodsBuilder.appendClassName(Java5ClassNames.Link_QualifiedName + "<"
                + getQualifiedInterfaceClassNameForTarget() + ">");
        methodsBuilder.append("(this, " + argNames[0] + "));");
        methodsBuilder.closeBracket();
    }

    String getMethodNameSet1RelatedCmpt() {
        String propertyName = getPropertyNameTo1Association();
        return getJavaNamingConvention().getSetterMethodName(propertyName);
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

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverageTypeGen getMainCoverageType()
     * </pre>
     */
    void generateSignatureGet1RelatedCmptGen(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGet1RelatedCmpt();
        String returnType = getGenType().getBuilderSet().getGenerator(target)
                .getQualifiedClassNameForProductCmptTypeGen(true);
        builder.signature(Modifier.PUBLIC, returnType, methodName, new String[] { "effectiveDate" },
                new String[] { Calendar.class.getName() });
    }

    String getMethodNameGet1RelatedCmpt() {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameTo1Association(), Datatype.INTEGER);
    }

    @Override
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
        methodsBuilder.appendln("result.add(" + getMethodNameGet1RelatedCmpt() + "());");
        methodsBuilder.appendln("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * Element element = associationElements.get(0);
     * ftCoverageType = element.getAttribute(&quot;targetRuntimeId&quot;);
     * cardinalitiesForFtCoverageType = new HashMap&lt;String, IntegerRange&gt;(1);
     * addToCardinalityMap(cardinalitiesForFtCoverageType, ftCoverageType, element);
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * Element element = associationElements.get(0);
     * ftCoverageType = new Link&lt;IFtCoverageType&gt;(this);
     * ftCoverageType.initFromXml(element);
     * </pre>
     * 
     * {@inheritDoc}
     */
    @Override
    public void generateCodeForMethodDoInitReferencesFromXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) throws CoreException {
        String fieldName = getFieldNameTo1Association();
        builder.appendClassName(Element.class);
        builder.append(" element = associationElements.get(0);");
        builder.append(fieldName);
        builder.append(" = ");
        builder.append("new ");
        builder.appendClassName(Java5ClassNames.Link_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget()
                + ">");
        builder.appendln("(this);");
        builder.append(fieldName);
        builder.appendln(".initFromXml(element);");
    }

    @Override
    public void generateCodeForMethodWriteReferencesToXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) {
        builder.appendln("element.appendChild(((");
        builder.appendClassName(IXmlPersistenceSupport.class);
        builder.appendln(")");
        builder.append(getFieldNameTo1Association());
        builder.appendln(").toXml(element.getOwnerDocument()));");
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * list.addAll(getLinkForProduct());
     * </pre>
     */
    @Override
    public void generateCodeForGetLinks(JavaCodeFragmentBuilder methodsBuilder) {
        methodsBuilder.appendln("if(" + getMethodNameGet1RelatedCmptLink() + "() != null){");
        methodsBuilder.appendln("list.add(" + getMethodNameGet1RelatedCmptLink() + "());");
        methodsBuilder.appendln("}");
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        addMethodGet1RelatedCmptToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGet1RelatedCmptGenToGeneratedJavaElements(javaElements, generatedJavaType);

        addMethodGet1RelatedCmptLinkToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRelatedCmptLinkToGeneratedJavaElements(javaElements, generatedJavaType);

        try {
            if (association.constrainsPolicyCmptTypeAssociation(association.getIpsProject())) {
                addMethodGetCardinalityForAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        addFieldTo1AssociationToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGet1RelatedCmptToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGet1RelatedCmptGenToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSet1RelatedCmptToGeneratedJavaElements(javaElements, generatedJavaType);

        addMethodGet1RelatedCmptLinkToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRelatedCmptLinkToGeneratedJavaElements(javaElements, generatedJavaType);

        try {
            if (association.constrainsPolicyCmptTypeAssociation(association.getIpsProject())) {
                addMethodGetCardinalityForAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFieldTo1AssociationToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        IField field = generatedJavaType.getField(getFieldNameTo1Association());
        javaElements.add(field);
    }

    private void addMethodSet1RelatedCmptToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        try {
            IMethod method = generatedJavaType
                    .getMethod(
                            getMethodNameSet1RelatedCmpt(),
                            new String[] { "Q"
                                    + QNameUtil.getUnqualifiedName(getQualifiedInterfaceClassNameForTarget()) + ";" });
            javaElements.add(method);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMethodGet1RelatedCmptToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGet1RelatedCmpt(), new String[0]);
        javaElements.add(method);
    }

    private void addMethodGet1RelatedCmptGenToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGet1RelatedCmpt(),
                new String[] { "Ljava.util.Calendar;" });
        javaElements.add(method);
    }

    private void addMethodGet1RelatedCmptLinkToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGet1RelatedCmptLink(), new String[0]);
        javaElements.add(method);
    }

}
