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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;

/**
 * 
 * @author Daniel Hohenberger
 */
public class GenProdAssociationToMany extends GenProdAssociation {

    /**
     */
    public GenProdAssociationToMany(GenProductCmptType genProductCmptType, IProductCmptTypeAssociation association)
            throws CoreException {
        super(genProductCmptType, association);
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
            generateFieldToManyAssociation(builder);
            if (association.findMatchingPolicyCmptTypeAssociation(ipsProject) != null) {
                if (!isUseTypesafeCollections()) {
                    generateFieldCardinalityForAssociation(builder);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            generateMethodInterfaceGetManyRelatedCmpts(builder);
            generateMethodInterfaceGetManyRelatedCmptGens(builder);
            generateMethodInterfaceGetRelatedCmptAtIndex(builder);
            generateMethodInterfaceGetManyRelatedCmptLinks(builder);
            generateMethodInterfaceGetRelatedCmptLink(builder);
            generateMethodGetNumOfRelatedCmpts(builder);
        } else {
            generateMethodGetManyRelatedCmpts(builder);
            generateMethodGetManyRelatedCmptGens(builder);
            generateMethodGetRelatedCmptAtIndex(builder);
            generateMethodGetManyRelatedCmptLinks(builder);
            generateMethodGetRelatedCmptLink(builder);
            generateMethodAddRelatedCmpt(builder);
            generateMethodAddRelatedCmptWithCardinality(builder);
            if (association.constrainsPolicyCmptTypeAssociation(ipsProject)) {
                generateMethodGetCardinalityFor1ToManyAssociation(builder);
            }
            generateMethodGetNumOfRelatedProductCmpts(builder);
        }
    }

    /**
     * Code sample: [Javadoc]
     * 
     * <pre>
     * public Collection&lt;ILink&lt;ICoverageType&gt;&gt; getLinksForCoverageTypes();
     * </pre>
     * 
     */
    private void generateMethodInterfaceGetManyRelatedCmptLinks(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetManyRelatedCmptLinks(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Java 5 Code sample:
     * 
     * <pre>
     * public Collection&lt;ILink&lt;ICoverageType&gt;&gt; getLinksForCoverageTypes()
     * </pre>
     * 
     */
    private void generateSignatureGetManyRelatedCmptLinks(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetManyRelatedCmptLinks();
        String returnType = Collection.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName + "<"
                + getQualifiedInterfaceClassNameForTarget() + ">>";
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType,
                methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    String getMethodNameGetManyRelatedCmptLinks() {
        return getJavaNamingConvention().getMultiValueGetterMethodName(
                "LinksFor" + StringUtils.capitalize(getFieldNameToManyAssociation()));
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     *  public Collection&lt;ILink&lt;ICoverageType&gt;&gt; getLinksForCoverageTypes(){
     *      return productParts.get(productComponent.getId());
     *  }
     * </pre>
     * 
     */
    private void generateMethodGetRelatedCmptLink(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRelatedCmptLink(methodsBuilder);

        String fieldName = getFieldNameToManyAssociation();
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".get(productComponent.getId());");
        methodsBuilder.closeBracket();
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     *  public Collection&lt;ILink&lt;ICoverageType&gt;&gt; getLinksForCoverageTypes(){
     *      return Collections.unmodifiableCollection(coverageTypes.values());
     *  }
     * </pre>
     * 
     */
    private void generateMethodGetManyRelatedCmptLinks(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetManyRelatedCmptLinks(methodsBuilder);

        String fieldName = getFieldNameToManyAssociation();
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.appendClassName(Collections.class);
        methodsBuilder.append(".unmodifiableCollection(");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".values());");
        methodsBuilder.closeBracket();
    }

    private void generateMethodGetCardinalityFor1ToManyAssociation(JavaCodeFragmentBuilder methodsBuilder)
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
        frag.append(getFieldNameToManyAssociation());
        frag.append(".containsKey(");
        frag.append(params[0][0]);
        frag.append(".getId()) ? ");
        frag.append(getFieldNameToManyAssociation());
        frag.append(".get(");
        frag.append(params[0][0]);
        frag.append(".getId()).getCardinality() : null;");
        frag.appendCloseBracket();
        frag.append("return null;");
        frag.appendCloseBracket();
        methodsBuilder.append(frag);
    }

    /**
     * Code sample
     * 
     * <pre>
     * [javadoc]
     * private Map&lt;String, ILink&lt;IProductPart&gt;&gt; productParts = new LinkedHashMap&lt;String, ILink&lt;IProductPart&gt;&gt;(0);
     * </pre>
     */
    private void generateFieldToManyAssociation(JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        appendLocalizedJavaDoc("FIELD_TOMANY_ASSOCIATION", role, memberVarsBuilder);
        String type = Map.class.getName() + "<" + String.class.getName() + "," + Java5ClassNames.ILink_QualifiedName
                + "<" + getQualifiedInterfaceClassNameForTarget() + ">>";
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(LinkedHashMap.class.getName());
        fragment.append("<");
        fragment.appendClassName(String.class.getName());
        fragment.append(", ");
        fragment.appendClassName(Java5ClassNames.ILink_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget()
                + ">");
        fragment.append(">(0)");
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyAssociation(), fragment);
    }

    String getFieldNameToManyAssociation() {
        return getJavaNamingConvention().getMultiValueMemberVarName(getPropertyNameToManyAssociation());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverageType&gt; getCoverageTypes() {
     *     List&lt;ICoverageType&gt; result = new ArrayList&lt;ICoverageType&gt;(coverageTypes.size());
     *     for (ILink&lt;ICoverageType&gt; coverageType: coverageTypes.values()) {
     *         result.add(coverageType.getTarget());
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetManyRelatedCmpts(methodsBuilder);

        String fieldName = getFieldNameToManyAssociation();
        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
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
        methodsBuilder.append("for (");
        methodsBuilder.appendClassName(Java5ClassNames.ILink_QualifiedName);
        methodsBuilder.append("<");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("> ");
        methodsBuilder.append(getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular()));
        methodsBuilder.append(" : ");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".values()) {");
        methodsBuilder.appendln("result.add(");
        methodsBuilder.append(getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular()));
        methodsBuilder.appendln(".getTarget());");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return result;");

        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverageTypeGen&gt; getCoverageTypeGens(Calendar effectiveDate) {
     *    List&lt;ICoverageType&gt; targets = getCoverageTypes();
     *    List&lt;ICoverageTypeGen&gt; result = new ArrayList&lt;ICoverageTypeGen&gt;();
     *    for (ICoverageType target : targets) {
     *        ICoverageTypeGen gen = target.getCoverageTypeGen(effectiveDate);
     *        if(gen!=null){
     *            result.add(gen);
     *        }
     *    }
     *    return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmptGens(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetManyRelatedCmptGens(methodsBuilder);
        String targetClass = getQualifiedInterfaceClassNameForTarget();
        String targetGenClass = getQualifiedInterfaceClassNameForTargetGen();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(List.class.getName());
        methodsBuilder.append("<");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.appendln("> targets = ");
        methodsBuilder.append(getMethodNameGetManyRelatedCmpts());
        methodsBuilder.appendln("();");
        methodsBuilder.appendClassName(List.class.getName());
        methodsBuilder.append("<");
        methodsBuilder.appendClassName(targetGenClass);
        methodsBuilder.append("> result = new ");
        methodsBuilder.appendClassName(ArrayList.class.getName());
        methodsBuilder.append("<");
        methodsBuilder.appendClassName(targetGenClass);
        methodsBuilder.append(">();");
        methodsBuilder.append("for (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.appendln(" target : targets) {");
        methodsBuilder.appendClassName(targetGenClass);
        methodsBuilder.append(" gen = target.");
        methodsBuilder.append(getMethodNameGetProductCmptGenerationForTarget());
        methodsBuilder.appendln("(effectiveDate);");
        methodsBuilder.appendln("if(gen!=null){");
        methodsBuilder.appendln("result.add(gen);");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return result;");
        methodsBuilder.closeBracket();
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverageType&gt; getCoverageTypes();
     * </pre>
     */
    private void generateMethodInterfaceGetManyRelatedCmpts(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetManyRelatedCmpts(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public List&lt;ICoverageTypeGen&gt; getCoverageTypeGens();
     * </pre>
     */
    private void generateMethodInterfaceGetManyRelatedCmptGens(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_MANY_RELATED_CMPT_GENS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetManyRelatedCmptGens(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * 
     * Java 5 code sample
     * 
     * <pre>
     * [javadoc]
     *  public CoverageType getMainCoverageType(int index) {
     *      Iterator&lt;ILink&lt;ICoverageType&gt;&gt; it = coverageTypes.values().iterator();
     *      try {
     *          for (int i = 0; i &lt; index; i++) {
     *              it.next();
     *          }
     *          return it.next().getTarget();
     *      } catch (NoSuchElementException e) {
     *          throw new IndexOutOfBoundsException(e.getLocalizedMessage());
     *      }
     *  }
     * </pre>
     */
    private void generateMethodGetRelatedCmptAtIndex(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRelatedCmptsAtIndex(methodsBuilder);
        String fieldName = getFieldNameToManyAssociation();
        String targetClass = getQualifiedInterfaceClassNameForTarget();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(Iterator.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName + "<"
                + targetClass + ">>");
        methodsBuilder.appendln(" it = " + fieldName + ".values().iterator();");
        methodsBuilder.appendln("try {");
        methodsBuilder.appendln("for(int i=0; i<index; i++){");
        methodsBuilder.appendln("it.next();");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return it.next().getTarget();");
        methodsBuilder.append("} catch (");
        methodsBuilder.appendClassName(NoSuchElementException.class);
        methodsBuilder.appendln(" e) {");
        methodsBuilder.appendln("throw new IndexOutOfBoundsException(e.getLocalizedMessage());");
        methodsBuilder.appendln("}");
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
        // TODO extend JavaNamingConventions for association accessor an mutator methods
        return "get" + StringUtils.capitalize(association.getTargetRoleSingular());
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
     *     cardinalitiesForCoverage.put(target.getId(), new IntegerRange(0, Integer.MAX_VALUE));
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
     *         this.productParts.put(target.getId(), new ProductComponentLink&lt;ICoverageType&gt;(this, target));
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT", association.getTargetRoleSingular(), methodsBuilder);
        String methodName = getMethodNameAddRelatedCmpt();
        String[] argNames = new String[] { "target" };
        String[] argTypes = new String[] { getQualifiedInterfaceClassNameForTarget() };
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), "void", methodName,
                argNames, argTypes);
        String fieldName = getFieldNameToManyAssociation();
        methodsBuilder.openBracket();
        methodsBuilder.append(((GenProductCmptType)getGenType()).generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + fieldName + ".put(target.getId(), new ");
        methodsBuilder.appendClassName(Java5ClassNames.Link_QualifiedName + "<"
                + getQualifiedInterfaceClassNameForTarget() + ">");
        methodsBuilder.appendln("(this, target));");
        methodsBuilder.closeBracket();
    }

    String getMethodNameAddRelatedCmpt() {
        return "add" + StringUtils.capitalize(association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType target, IntegerRange cardinality) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     String[] tmp = new String[this.coverageTypes.length+1];
     *     System.arraycopy(coverageTypes, 0, tmp, 0, this.coverageTypes.length);
     *     tmp[tmp.length-1] = target.getId();
     *     this.coverageTypes = tmp;
     *     cardinalitiesForCoverage.put(target.getId(), cardinality);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType target, IntegerRange cardinality) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.coverageTypes.add(target.getId());
     *         this.productParts.put(target.getId(), new ProductComponentLink&lt;ICoverageType&gt;(this, target, cardinality));
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmptWithCardinality(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT_WITH_CARDINALITY", association.getTargetRoleSingular(),
                methodsBuilder);
        String methodName = getMethodNameAddRelatedCmpt();
        String[] argNames = new String[] { "target", "cardinality" };
        String[] argTypes = new String[] { getQualifiedInterfaceClassNameForTarget(), CardinalityRange.class.getName() };
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), "void", methodName,
                argNames, argTypes);
        String fieldName = getFieldNameToManyAssociation();
        methodsBuilder.openBracket();
        methodsBuilder.append(((GenProductCmptType)getGenType()).generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + fieldName + ".put(target.getId(), new ");
        methodsBuilder.appendClassName(Java5ClassNames.Link_QualifiedName + "<"
                + getQualifiedInterfaceClassNameForTarget() + ">");
        methodsBuilder.appendln("(this, target, cardinality));");
        methodsBuilder.closeBracket();
    }

    @Override
    protected void generateCodeGetNumOfRelatedProductCmptsInternal(JavaCodeFragmentBuilder builder)
            throws CoreException {
        builder.append(getMethodNameGetNumOfRelatedCmpts());
        builder.append("();");
    }

    @Override
    protected void generateCodeGetNumOfRelatedProductCmpts(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append(getFieldNameToManyAssociation());
        builder.appendln(".size();");
    }

    @Override
    protected void generateCodeGetRelatedCmptsInContainer(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String objectArrayVar = getFieldNameToManyAssociation() + "Objects";
        String getterMethod = getMethodNameGetManyRelatedCmpts() + "()";
        methodsBuilder.appendClassName(List.class.getName());
        methodsBuilder.append("<");
        methodsBuilder.appendClassName(getQualifiedInterfaceClassNameForTarget());
        methodsBuilder.append("> " + objectArrayVar + " = " + getterMethod + ";");
        methodsBuilder.appendln("for (");
        methodsBuilder.appendClassName(getQualifiedInterfaceClassNameForTarget());
        methodsBuilder.appendln(" " + getFieldNameToManyAssociation() + "Object : " + objectArrayVar + ") {");
        methodsBuilder.appendln("result.add(" + getFieldNameToManyAssociation() + "Object);");
        methodsBuilder.appendln("}");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateCodeForDerivedUnionAssociationDefinition(JavaCodeFragmentBuilder methodsBuilder)
            throws Exception {
        super.generateCodeForDerivedUnionAssociationDefinition(methodsBuilder);
        generateMethodGetNumOfRelatedCmpts(methodsBuilder);
    }

    @Override
    public void generateCodeForDerivedUnionAssociationImplementation(List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        super.generateCodeForDerivedUnionAssociationImplementation(implAssociations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmpts(implAssociations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmptsInternal(implAssociations, methodsBuilder);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * ftCoverageTypes = new String[associationElements.size()];
     * cardinalitiesForFtCoverage = new HashMap(associationElements.size());
     * for (int i = 0; i &lt; associationElements.size(); i++) {
     *     Element element = (Element)associationElements.get(i);
     *     ftCoverageTypes[i] = element.getAttribute(&quot;targetRuntimeId&quot;);
     *     addToCardinalityMap(cardinalitiesForFtCoverage, ftCoverageTypes[i], element);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * ftCoverageTypes = new LinkedHashMap&lt;String, ILink&lt;IFtCoverageType&gt;&gt;(associationElements.size());
     * for (Element element : associationElements) {
     *     ILink&lt;IFtCoverageType&gt; link = new Link&lt;IFtCoverageType&gt;(this);
     *     link.initFromXml(element);
     *     ftCoverageTypes.put(link.getTargetId(), link);
     * }
     * </pre>
     * 
     * {@inheritDoc}
     */
    @Override
    public void generateCodeForMethodDoInitReferencesFromXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) throws CoreException {
        String fieldName = getFieldNameToManyAssociation();
        builder.append("this.").append(fieldName);
        builder.appendln(" = new ");
        builder.appendClassName(LinkedHashMap.class.getName());
        builder.append("<");
        builder.appendClassName(String.class.getName());
        builder.append(", ");
        builder.appendClassName(Java5ClassNames.ILink_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget()
                + ">");
        builder.append(">(associationElements.size());");
        builder.append("for (");
        builder.appendClassName(Element.class);
        builder.appendln(" element : associationElements) {");
        builder.appendClassName(Java5ClassNames.ILink_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget()
                + ">");
        builder.append(" link = new ");
        builder.appendClassName(Java5ClassNames.Link_QualifiedName + "<" + getQualifiedInterfaceClassNameForTarget()
                + ">");
        builder.appendln("(this);");
        builder.appendln("link.initFromXml(element);");
        builder.append("this.").append(fieldName);
        builder.appendln(".put(link.getTargetId(), link);");
        builder.appendln("}");
    }

    @Override
    public void generateCodeForMethodWriteReferencesToXml(IPolicyCmptTypeAssociation policyCmptTypeAssociation,
            JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append("for("); //$NON-NLS-1$
        builder.appendClassName(IProductComponentLink.class);
        builder.append("<"); //$NON-NLS-1$
        builder.appendClassName(getQualifiedInterfaceClassNameForTarget());
        builder.append(">"); //$NON-NLS-1$
        builder.append(" link:"); //$NON-NLS-1$
        builder.append(getFieldNameToManyAssociation());
        builder.append(".values()){"); //$NON-NLS-1$
        builder.append("element.appendChild(((");//$NON-NLS-1$
        builder.appendClassName(IXmlPersistenceSupport.class);
        builder.append(")link).toXml(element.getOwnerDocument()));"); //$NON-NLS-1$
        builder.append("}"); //$NON-NLS-1$
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverageTypes();
     * </pre>
     */
    void generateMethodGetNumOfRelatedCmpts(JavaCodeFragmentBuilder builder) {
        String role = association.getTargetRolePlural();
        appendLocalizedJavaDoc("METHOD_GET_NUM_OF_RELATED_CMPTS", role, builder);
        generateSignatureGetNumOfRelatedCmpts(builder);
        builder.appendln(";");
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     * list.addAll(getLinksForProducts());
     * </pre>
     * 
     */
    @Override
    public void generateCodeForGetLinks(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.appendln("list.addAll(" + getMethodNameGetManyRelatedCmptLinks() + "());");
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        addMethodGetManyRelatedCmptsToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetNumOfRelatedCmptsToGeneratedJavaElements(javaElements, generatedJavaType);

        if (association.isDerivedUnion()) {
            return;
        }

        addMethodGetManyRelatedCmptGensToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRelatedCmptAtIndexToGeneratedJavaElements(javaElements, generatedJavaType);

        addMethodGetManyRelatedCmptLinksToGeneratedJavaElements(javaElements, generatedJavaType);
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

        if (association.isDerivedUnion()) {
            return;
        }

        addFieldToManyAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetManyRelatedCmptsToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetManyRelatedCmptGensToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRelatedCmptAtIndexToGeneratedJavaElements(javaElements, generatedJavaType);

        addMethodGetManyRelatedCmptLinksToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRelatedCmptLinkToGeneratedJavaElements(javaElements, generatedJavaType);

        addMethodAddRelatedCmptToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodAddRelatedCmptWithCardinalityToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetNumOfRelatedCmptsToGeneratedJavaElements(javaElements, generatedJavaType);

        try {
            if (association.constrainsPolicyCmptTypeAssociation(association.getIpsProject())) {
                addMethodGetCardinalityForAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFieldToManyAssociationToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IField field = generatedJavaType.getField(getFieldNameToManyAssociation());
        javaElements.add(field);
    }

    private void addMethodGetManyRelatedCmptsToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetManyRelatedCmpts(), new String[0]);
        javaElements.add(method);
    }

    private void addMethodGetManyRelatedCmptGensToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetManyRelatedCmpts(),
                new String[] { "Ljava.util.Calendar;" });
        javaElements.add(method);
    }

    private void addMethodGetRelatedCmptAtIndexToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetRelatedCmptAtIndex(), new String[] { "I" });
        javaElements.add(method);
    }

    private void addMethodGetManyRelatedCmptLinksToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetManyRelatedCmptLinks(), new String[0]);
        javaElements.add(method);
    }

    private void addMethodGetNumOfRelatedCmptsToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        IMethod method = generatedJavaType.getMethod(getMethodNameGetNumOfRelatedCmpts(), new String[0]);
        javaElements.add(method);
    }

    private void addMethodAddRelatedCmptToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        try {
            IMethod method = generatedJavaType
                    .getMethod(
                            getMethodNameAddRelatedCmpt(),
                            new String[] { "Q"
                                    + QNameUtil.getUnqualifiedName(getQualifiedInterfaceClassNameForTarget()) + ";" });
            javaElements.add(method);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMethodAddRelatedCmptWithCardinalityToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        try {
            IMethod method = generatedJavaType.getMethod(getMethodNameAddRelatedCmpt(),
                    new String[] { "Q" + QNameUtil.getUnqualifiedName(getQualifiedInterfaceClassNameForTarget()) + ";",
                            "Q" + IntegerRange.class.getSimpleName() + ";" });
            javaElements.add(method);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
