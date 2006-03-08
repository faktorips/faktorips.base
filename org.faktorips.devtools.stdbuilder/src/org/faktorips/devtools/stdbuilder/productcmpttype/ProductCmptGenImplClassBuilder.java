/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Element;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenImplClassBuilder extends AbstractProductCmptTypeBuilder{

    private ProductCmptGenInterfaceBuilder interfaceBuilder;
    private ProductCmptImplClassBuilder productCmptTypeImplCuBuilder;
    private ProductCmptInterfaceBuilder productCmptTypeInterfaceBuilder;
    private PolicyCmptImplClassBuilder policyCmptTypeImplBuilder;
    
    public ProductCmptGenImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
        setMergeEnabled(true);
    }
    
    public void setInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        ArgumentCheck.notNull(builder);
        this.interfaceBuilder = builder;
    }
    
    public void setProductCmptTypeImplBuilder(ProductCmptImplClassBuilder builder) {
        ArgumentCheck.notNull(builder);
        productCmptTypeImplCuBuilder = builder;
    }
    
    public void setPolicyCmptTypeImplBuilder(PolicyCmptImplClassBuilder builder) {
        this.policyCmptTypeImplBuilder = builder;
    }
    
    public void setProductCmptTypeInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        this.productCmptTypeInterfaceBuilder = builder;
    }

    /**
     * If a policy component type contains an derived or computed attribute, the product component
     * generation class must be abstract, as the computation formulas are defined per generation. 
     * 
     * Overridden.
     */
    protected int getClassModifier() throws CoreException {
        int modifier = super.getClassModifier();
        if ((modifier & Modifier.ABSTRACT) > 0) {
            return modifier;
        }
        IAttribute[] attributes = getProductCmptType().findPolicyCmptyType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].isDerivedOrComputed()) {
                return modifier | Modifier.ABSTRACT;
            }
        }
        return modifier;
    }

    /**
     * Overridden.
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return getJavaNamingConvention().getImplementationClassName(getProductCmptType(ipsSrcFile).getName() + generationAbb);
    }

    /**
     * Overridden.
     */
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * Overridden.
     */
    protected String getSuperclass() throws CoreException {
        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype != null) {
            String pack = getPackage(supertype.getIpsSrcFile());
            return StringUtil.qualifiedName(pack, getUnqualifiedClassName(supertype.getIpsSrcFile()));
        }
        return ProductComponentGeneration.class.getName();
    }

    /**
     * Overridden.
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        // The implementation implements the published interface.
        return new String[] { interfaceBuilder.getQualifiedClassName(getIpsSrcFile()) };
    }

    /**
     * Overridden.
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CLASS", interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile()), getIpsObject(), builder);
    }

    /**
     * Overridden.
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder);
        builder.append("public ");
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(productCmptTypeImplCuBuilder.getQualifiedClassName(getIpsSrcFile()));
        builder.append(" productCmpt)");
        builder.openBracket();
        builder.appendln("super(productCmpt);");
        builder.closeBracket();
    }
    
    /**
     * Overridden.
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        generateMethodDoInitReferencesFromXml(methodsBuilder);
    }
    
    private void generateMethodDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        builder.methodBegin(Modifier.PROTECTED, Void.class, "doInitPropertiesFromXml", 
                new String[]{"configMap"}, new Class[]{Map.class});
        
        IAttribute[] attributes = getProductCmptType().getAttributes();
        boolean attributeFound = false;
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (a.validate().containsErrorMsg()) {
                continue;
            }
            if (!a.isProductRelevant() || a.isDerivedOrComputed()) {
                continue;
            }
            if (attributeFound == false) {
                builder.appendClassName(Element.class);
                builder.appendln(" configElement = null;");
                builder.appendClassName(String.class);
                builder.appendln(" value = null;");
                attributeFound = true;
            }
            Datatype datatype = getProductCmptType().getIpsProject().findDatatype(a.getDatatype());
            DatatypeHelper helper = getProductCmptType().getIpsProject().getDatatypeHelper(datatype);
            String memberVarName;
            if (a.isChangeable()) {
                memberVarName = getFieldNameDefaulValue(a);
            } else {
                memberVarName = getFieldNameValue(a);
            }
            builder.append("configElement = (");
            builder.appendClassName(Element.class);
            builder.append(")configMap.get(\"");
            builder.append(a.getName());
            builder.appendln("\");");
            builder.append("if (configElement != null) ");
            builder.openBracket();
            builder.append("value = ");
            builder.appendClassName(ValueToXmlHelper.class);
            builder.append(".getValueFromElement(configElement, \"Value\");");
            builder.append(memberVarName);
            builder.append(" = ");
            builder.append(helper.newInstanceFromExpression("value"));
            builder.appendln(";");
            builder.closeBracket();
        }
        builder.methodEnd();
    }
    
    private void generateMethodDoInitReferencesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        
        String[] argNames = new String[]{"relationMap"};
        String[] argTypes = new String[]{Map.class.getName()};
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitReferencesFromXml", argNames, argTypes);
        
        builder.appendln("super.doInitReferencesFromXml(relationMap);");
        
        // before the first relation we define a temp variable as follows:
        // Element relationElements = null;

        // for each 1-1 relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        //     vertragsteilePk = ((Element)relationElement.get(0)).getAttribute("target");
        // }
        // 
        // for each 1-many relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        //     vertragsteilPks[] = new VertragsteilPk[relationElements.length()];
        //     for (int i=0; i<vertragsteilsPks.length; i++) {
        //         vertragsteilPks[i] = ((Element)relationElement.get(i)).getAttribute("target");
        //         }
        //     }
        // }
        IProductCmptTypeRelation[] relations = getProductCmptType().getRelations();
        boolean relationFound = false;
        for (int i = 0; i < relations.length; i++) {
            IProductCmptTypeRelation r = relations[i];
            if (!r.isAbstract()) {
                if (relationFound == false) {
                    builder.appendln();
                    builder.appendClassName(List.class);
                    builder.append(" ");
                    relationFound = true;
                }
                builder.append("relationElements = (");
                builder.appendClassName(List.class);
                builder.append(") relationMap.get(");
                builder.appendQuoted(r.getName());
                builder.appendln(");");
                builder.append("if (relationElements != null) {");
                if (r.is1ToMany()) {
                    String fieldName = getFieldNameToManyRelation(r);
                    builder.append(fieldName);
                    builder.appendln(" = new ");
                    builder.appendClassName(String.class);
                    builder.appendln("[relationElements.size()];");
                    builder.appendln("for (int i=0; i<relationElements.size(); i++) {");
                    builder.append(fieldName);
                    builder.append("[i] = ((");
                    builder.appendClassName(Element.class);
                    builder.append(")relationElements.get(i)).getAttribute(\"target\");");
                    builder.appendln("}");
                } else {
                    builder.append(getFieldNameTo1Relation(r));
                    builder.append(" = ((");
                    builder.appendClassName(Element.class);
                    builder.append(")relationElements.get(0)).getAttribute(\"target\");");
                }
                builder.appendln("}");
            }
        }
        builder.methodEnd();
    }

    protected void generateCodeForChangeableAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateFieldDefaultValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetDefaultValue(a, datatypeHelper, methodsBuilder);
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer minAge;
     * </pre>
     */
    private void generateFieldDefaultValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", a.getName(), a, memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaulValue(a), defaultValueExpression);
    }    
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge() {
     *     return minAge;
     * </pre>
     */
    private void generateMethodGetDefaultValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetDefaultValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameDefaulValue(a));
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameDefaulValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameDefaultValue(a));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateFieldValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetValue(a, datatypeHelper, methodsBuilder);
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer taxRate;
     * </pre>
     */
    private void generateFieldValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_VALUE", StringUtils.capitalise(a.getName()), a, builder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        builder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameValue(a), defaultValueExpression);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getInterestRate() {
     *     return interestRate;
     * </pre>
     */
    private void generateMethodGetValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameValue(a));
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameValue(a));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodComputeValue(a, datatypeHelper, methodsBuilder);
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public abstract Money computePremium(Policy policy, Integer age);
     * </pre>
     */
    public void generateMethodComputeValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_COMPUTE_VALUE", StringUtils.capitalise(a.getName()), a, methodsBuilder);
        generateSignatureComputeValue(a, datatypeHelper,Modifier.ABSTRACT | Modifier.PUBLIC, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age)
     * </pre>
     */
    public void generateSignatureComputeValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        String methodName = getMethodNameComputeValue(a);
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(),
                methodName, BuilderHelper.extractParameterNames(parameters),
                StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, a.getIpsProject(), policyCmptTypeImplBuilder));
    }
    
    public String getMethodNameComputeValue(IAttribute a) {
        return getLocalizedText(a, "METHOD_COMPUTE_VALUE_NAME", StringUtils.capitalise(a.getName()));
    }

    /**
     * Overridden.
     */
    protected void generateCodeForNoneContainerRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (relation.is1ToMany()) {
            generateFieldToManyRelation(relation, memberVarsBuilder);
            generateMethodGetManyRelatedCmpts(relation, methodsBuilder);
            generateMethodGetRelatedCmptAtIndex(relation, methodsBuilder);
        } else {
            generateFieldTo1Relation(relation, memberVarsBuilder);
            generateMethodGet1RelatedCmpt(relation, memberVarsBuilder, methodsBuilder);
        }
        generateMethodGetNumOfRelatedProductCmpts(relation, methodsBuilder);        
    }
    
    private String getFieldNameToManyRelation(IProductCmptTypeRelation relation) throws CoreException {
        return getJavaNamingConvention().getMultiValueMemberVarName(interfaceBuilder.getPropertyNameToManyRelation(relation));
    }
    
    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * private CoverageType[] optionalCoverageTypes;
     * </pre>
     */
    private void generateFieldToManyRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalise(relation.getTargetRolePlural());
        appendLocalizedJavaDoc("FIELD_TOMANY_RELATION", role, relation, memberVarsBuilder);
        String type = String.class.getName() + "[]";
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyRelation(relation), new JavaCodeFragment("new String[0]"));
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[coverageTypes.length];
     *     for (int i = 0; i < result.length; i++) {
     *         result[i] = (ICoverageType) getRepository().getProductComponent(coverageTypes[i]);
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetManyRelatedCmpts(relation, methodsBuilder);

        String fieldName = getFieldNameToManyRelation(relation);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
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
        methodsBuilder.append(")getRepository().getProductComponent(");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln("[i]);");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return result;");
    
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType(int index) {
     *     return (ICoverageType) getRepository().getProductComponent(coverageTypes[index]);
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptAtIndex(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRelatedCmptsAtIndex(relation, methodsBuilder);
        String fieldName = getFieldNameToManyRelation(relation);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository().getProductComponent(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append("[index]);");
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameTo1Relation(IProductCmptTypeRelation relation) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameTo1Relation(relation));
    }

    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * private CoverageType mainCoverage;
     * </pre>
     */
    private void generateFieldTo1Relation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalise(relation.getTargetRoleSingular());
        appendLocalizedJavaDoc("FIELD_TO1_RELATION", role, relation, memberVarsBuilder);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, String.class, getFieldNameTo1Relation(relation), new JavaCodeFragment("null"));
    }

    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType() {
     *     return (CoveragePk) getRepository().getProductComponent(mainCoverageType);
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmpt(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGet1RelatedCmpt(relation, methodsBuilder);
        String fieldName = getFieldNameTo1Relation(relation);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository().getProductComponent(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationDefinition(IProductCmptTypeRelation containerRelation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationImplementation(IProductCmptTypeRelation containerRelation, List implRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMethodGetRelatedCmptsInContainer(containerRelation, implRelations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmpts(containerRelation, implRelations, methodsBuilder);
    }
    
    /**
     * Code sample where a 1-1 and a 1-many relation implement a container relation.
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[getNumOfCoverageTypes()];
     *     int index = 0;
     *     if (collisionCoverageType!=null) {
     *         result[index++] = getCollisionCoverageType();
     *     }
     *     for (int i=0; i<tplCoverageTypes.length; i++) {
     *         result[index++] = (ICoverageType)getRepository().getProductComponent(tplCoverageTypes[i]);
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptsInContainer(
            IProductCmptTypeRelation relation,
            List implRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainerRelation(relation, methodsBuilder);

        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRelatedCmpts(relation));
        methodsBuilder.appendln("()];");
        methodsBuilder.appendln("int index = 0;");
        for (Iterator it = implRelations.iterator(); it.hasNext();) {
            IProductCmptTypeRelation implRelation = (IProductCmptTypeRelation)it.next();
            if (implRelation.is1ToMany()) {
                String fieldName = getFieldNameToManyRelation(implRelation);
                methodsBuilder.appendln("for (int i=0; i<" + fieldName + ".length; i++) {");
                methodsBuilder.append("result[index++] = ");
                methodsBuilder.append(interfaceBuilder.getMethodNameGetRelatedCmptAtIndex(implRelation));
                methodsBuilder.appendln("(i);");
                methodsBuilder.appendln("}");
            } else {
                String fieldName = getFieldNameTo1Relation(implRelation);
                methodsBuilder.appendln("if (" + fieldName + "!=null) {");
                methodsBuilder.appendln("result[index++] = " + interfaceBuilder.getMethodNameGet1RelatedCmpt(implRelation) + "();");
                methodsBuilder.appendln("}");
            }
        }
        methodsBuilder.appendln("return result;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Generates the getNumOfXXX() method for none container relations. 
     * <p>
     * Code sample for 1-1 relations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageType==null ? 0 : 1;
     * }
     * </pre>
     * <p>
     * Code sample for 1-many relations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageTypes.length;
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmpts(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        if (relation.isAbstractContainer()) {
            throw new IllegalArgumentException("Relation needn't be a container relation.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRelatedCmpts(relation, builder);
        builder.openBracket();
        builder.append("return ");
        if (relation.is1ToMany()) {
            builder.append(getFieldNameToManyRelation(relation));
            builder.appendln(".length;");
        } else {
            builder.append(getFieldNameTo1Relation(relation));
            builder.appendln(" ==null ? 0 : 1;");
        }
        builder.closeBracket();
    }

    /**
     * Generates the getNumOfXXX() method for a container relation. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     int numOf = 0;
     *     numOf += getNumOfCollisionCoverages();
     *     numOf += getNumOfTplCoverages();
     *     return numOf;
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmpts(IProductCmptTypeRelation containerRelation, List implRelations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!containerRelation.isAbstractContainer()) {
            throw new IllegalArgumentException("Relation must be a container relation.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRelatedCmpts(containerRelation, builder);
        builder.openBracket();
        builder.appendln("int numOf = 0;");
        for (Iterator it = implRelations.iterator(); it.hasNext();) {
            IProductCmptTypeRelation relation = (IProductCmptTypeRelation)it.next();
            builder.append("numOf += ");
            builder.append(interfaceBuilder.getMethodNameGetNumOfRelatedCmpts(relation));
            builder.append("();");
        }
        builder.appendln("return numOf;");
        builder.closeBracket();
    }

}
