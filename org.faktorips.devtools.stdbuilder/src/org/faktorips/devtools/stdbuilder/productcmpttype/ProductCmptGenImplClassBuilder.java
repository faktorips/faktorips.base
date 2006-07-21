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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Range;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.DefaultEnumValueSet;
import org.faktorips.valueset.EnumValueSet;
import org.faktorips.valueset.IntegerRange;
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
        return getClassModifier(getProductCmptType().findPolicyCmptyType(), modifier);
    }
    
    private int getClassModifier(IPolicyCmptType type, int modifier) throws CoreException {
        IAttribute[] attributes = type.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].isDerivedOrComputed() && attributes[i].isProductRelevant()) {
                // note: type can't be an instanceof ProductCmptType, as derived or computed policy cmpt type attributes
                // aren't  not product cmpt attributes!
                return modifier | Modifier.ABSTRACT;
            }
        }
        IPolicyCmptType supertype = type.findSupertype();
        if (supertype!=null) {
            return getClassModifier(supertype, modifier);
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
        
        builder.appendln("super.doInitPropertiesFromXml(configMap);");
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
            ValueDatatype datatype = a.findDatatype();
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
            
            if(AttributeType.CHANGEABLE.equals(a.getAttributeType())){
                ValueSetType valueSetType = a.getValueSet().getValueSetType();
                JavaCodeFragment frag = new JavaCodeFragment();
                if(ValueSetType.RANGE.equals(valueSetType)){
                    frag.appendClassName(Range.class);
                    frag.append(" range = ");
                    frag.appendClassName(ValueToXmlHelper.class);
                    frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");");
                    frag.append(getFieldNameRangeFor(a));
                    frag.append(" = ");
                    JavaCodeFragment newRangeInstanceFrag = helper.newRangeInstance(
                            new JavaCodeFragment("range.getLower()"), new JavaCodeFragment("range.getUpper()"),
                            new JavaCodeFragment("range.getStep()"), new JavaCodeFragment("range.containsNull()"));
                    if(newRangeInstanceFrag == null){
                        throw new CoreException(new IpsStatus("The " + helper + " for the datatype " +  datatype.getName() + " doesn't support ranges."));
                    }
                    frag.append(newRangeInstanceFrag);
                    frag.appendln(";");
                }
                else if(ValueSetType.ENUM.equals(valueSetType)){
                    frag.appendClassName(EnumValues.class);
                    frag.append(" values = ");
                    frag.appendClassName(ValueToXmlHelper.class);
                    frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");");
                    frag.appendClassName(ArrayList.class);
                    frag.append(" enumValues = new ");
                    frag.appendClassName(ArrayList.class);
                    frag.append("();");
                    frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)");
                    frag.appendOpenBracket();
                    frag.append("enumValues.add(");
                    frag.append(helper.newInstanceFromExpression("values.getValue(i)"));
                    frag.appendln(");");
                    frag.appendCloseBracket();
                    frag.append(getFieldNameAllowedValuesFor(a));
                    frag.append(" = new ");
                    frag.appendClassName(DefaultEnumValueSet.class);
                    frag.append("(enumValues, values.containsNull(), ");
                    frag.append(helper.nullExpression());
                    frag.appendln(");");
                }
                builder.append(frag);
            }
            
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
        //     vertragsteilePk = ((Element)relationElement.get(0)).getAttribute("targetRuntimeId");
        // }
        // 
        // for each 1-many relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        //     vertragsteilPks[] = new VertragsteilPk[relationElements.length()];
        //     for (int i=0; i<vertragsteilsPks.length; i++) {
        //         vertragsteilPks[i] = ((Element)relationElement.get(i)).getAttribute("targetRuntimeId");
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
                    String cardinalityFieldName = getFieldNameCardinalityForRelation(r);
                    builder.append(fieldName);
                    builder.appendln(" = new ");
                    builder.appendClassName(String.class);
                    builder.appendln("[relationElements.size()];");
                    builder.append(cardinalityFieldName);
                    builder.append(" = new ");
                    builder.appendClassName(HashMap.class);
                    builder.appendln("(relationElements.size());");
                    builder.appendln("for (int i=0; i<relationElements.size(); i++) {");
                    builder.appendClassName(Element.class);
                    builder.append(" element = (");
                    builder.appendClassName(Element.class);
                    builder.appendln(")relationElements.get(i);");
                    builder.append(fieldName);
                    builder.append("[i] = ");
                    builder.appendln("element.getAttribute(\"" + IProductCmptRelation.PROPERTY_TARGET_RUNTIME_ID + "\");");
                    builder.append("addToCardinalityMap(");
                    builder.append(cardinalityFieldName);
                    builder.append(", ");
                    builder.append(fieldName);
                    builder.append("[i], ");
                    builder.appendln("element);");
                    builder.appendln("}");
                } else {
                    String fieldName = getFieldNameTo1Relation(r);
                    String cardFieldName = getFieldNameCardinalityForRelation(r);
                    builder.appendClassName(Element.class);
                    builder.append(" element = (");
                    builder.appendClassName(Element.class);
                    builder.appendln(")relationElements.get(0);");
                    builder.append(fieldName);
                    builder.append(" = ");
                    builder.appendln("element.getAttribute(\"" + IProductCmptRelation.PROPERTY_TARGET_RUNTIME_ID + "\");");
                    builder.append(cardFieldName);
                    builder.append(" = new ");
                    builder.appendClassName(HashMap.class);
                    builder.appendln("(1);");
                    builder.append("addToCardinalityMap(");
                    builder.append(cardFieldName);
                    builder.append(", ");
                    builder.append(fieldName);
                    builder.appendln(", element);");
                }
                builder.appendln("}");
            }
        }
        builder.methodEnd();
    }

    protected void generateCodeForChangeableAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateFieldDefaultValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetDefaultValue(a, datatypeHelper, methodsBuilder);
        
        if(!datatypeHelper.getDatatype().isPrimitive()){
            if(ValueSetType.RANGE.equals(a.getValueSet().getValueSetType())){
                generateFieldRangeFor(a , datatypeHelper, memberVarsBuilder);
                generateMethodGetRangeFor(a, datatypeHelper, methodsBuilder);
            }
            else if(ValueSetType.ENUM.equals(a.getValueSet().getValueSetType()) ||
                    datatypeHelper.getDatatype() instanceof EnumDatatype){
                generateFieldAllowedValuesFor(a, memberVarsBuilder);
                generateMethodGetAllowedValuesFor(a, datatypeHelper.getDatatype(), methodsBuilder);
            }
        }
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
        generateMethodSetValue(a, datatypeHelper, methodsBuilder);
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
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setInterestRate(Decimal newValue) {
     *     if (getRepository()!=null && !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.interestRate = newValue;
     * }
     * </pre>
     */
    private void generateMethodSetValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_SET_VALUE", a.getName(), a, methodsBuilder);
        String methodName = getJavaNamingConvention().getSetterMethodName(interfaceBuilder.getPropertyNameValue(a), datatypeHelper.getDatatype());
        String[] paramNames = new String[]{"newValue"};
        String[] paramTypes = new String[]{datatypeHelper.getJavaClassName()};
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + getFieldNameValue(a));
        methodsBuilder.appendln(" = newValue;");
        methodsBuilder.closeBracket();
    }
    
    private JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()." + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
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
        generateSignatureComputeValue(a, datatypeHelper,Modifier.ABSTRACT | Modifier.PUBLIC, false, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age)
     * </pre>
     */
    public void generateSignatureComputeValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, boolean withFinalParameters, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        String methodName = getMethodNameComputeValue(a);
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(),
                methodName, BuilderHelper.extractParameterNames(parameters),
                StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, a.getIpsProject(), policyCmptTypeImplBuilder),
                withFinalParameters);
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
            generateMethodAddRelatedCmpt(relation, methodsBuilder);
        } else {
            generateFieldTo1Relation(relation, memberVarsBuilder);
            generateMethodGet1RelatedCmpt(relation, methodsBuilder);
            generateMethodSet1RelatedCmpt(relation, methodsBuilder);
//            generateMethodGetCardinalityFor1To1Relation(relation, methodsBuilder);
//            generateFieldCardinalityFor1To1Relation(relation, memberVarsBuilder);
        }
        generateMethodGetCardinalityFor1ToManyRelation(relation, methodsBuilder);
        generateFieldCardinalityForRelation(relation, memberVarsBuilder);
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
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType[] target) {
     *     if (getRepository()!=null && !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     String[] tmp = new String[coverageTypes.length+1];
     *     System.arraycopy(coverageTypes, 0, tmp, 0, coverageTypes.length);
     *     tmp[tmp.length-1] = target.getId();
     *     coverageTypes = tmp;
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmpt(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IProductCmptType target = relation.findTarget();
        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT", relation.getTargetRoleSingular(), relation, methodsBuilder);
        String methodName = "add" + StringUtils.capitalise(relation.getTargetRoleSingular());
        String[] argNames = new String[]{"target"};
        String[] argTypes = new String[]{productCmptTypeInterfaceBuilder.getQualifiedClassName(target)};
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), 
                "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameToManyRelation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.appendln("String[] tmp = new String[this." + fieldName + ".length+1];");
        methodsBuilder.appendln("System.arraycopy(this." + fieldName + ", 0, tmp, 0, this." + fieldName + ".length);");
        methodsBuilder.appendln("tmp[tmp.length-1] = " + argNames[0] + "."  + MethodNames.GET_PRODUCT_COMPONENT_ID + "();");
        methodsBuilder.appendln("this." + fieldName + " = tmp;");
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
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType() {
     *     return (CoveragePk) getRepository().getProductComponent(mainCoverageType);
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmpt(
            IProductCmptTypeRelation relation, 
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
     * Code sample:
     * <pre>
     * [javadoc]
     * public void setMainCoverageType(ICoverageType target) {
     *     mainCoverageType = target==null ? null : target.getId();
     * }
     * </pre>
     */
    private void generateMethodSet1RelatedCmpt(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_1_RELATED_CMPT", relation.getTargetRoleSingular(), relation, methodsBuilder);

        String propName = interfaceBuilder.getPropertyNameTo1Relation(relation);
        String methodName = getJavaNamingConvention().getSetterMethodName(propName, Datatype.INTEGER);
        String[] argNames = new String[]{"target"};
        String[] argTypes = new String[]{productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget())};
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameTo1Relation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append(fieldName + " = (" + argNames[0] + "==null ? null : " + argNames[0] + "." + MethodNames.GET_PRODUCT_COMPONENT_ID + "() );");
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
     *     ITplCoverageType[] tplCoverageTypesObjects = getTplcCoverageTypes();
     *     for (int i=0; i<tplCoverageTypesObjects.length; i++) {
     *         result[index++] = tplCoverageTypes[i];
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
                String objectArrayVar = getFieldNameToManyRelation(implRelation) + "Objects";
                String getterMethod = interfaceBuilder.getMethodNameGetManyRelatedCmpts(implRelation) + "()";
                methodsBuilder.appendClassName(productCmptTypeInterfaceBuilder.getQualifiedClassName(implRelation.findTarget()));
                methodsBuilder.append("[] " + objectArrayVar + " = " + getterMethod + ";");
                methodsBuilder.appendln("for (int i=0; i<" + objectArrayVar + ".length; i++) {");
                methodsBuilder.appendln("result[index++] = " + objectArrayVar + "[i];");
                methodsBuilder.appendln("}");
            } else {
                String accessCode;
                if (implRelation.isAbstractContainer()) {
                    // if the implementation relation is itself a container relation, use the access method
                    accessCode = interfaceBuilder.getMethodNameGet1RelatedCmpt(implRelation) + "()";
                } else {
                    // otherwise use the field.
                    accessCode = getFieldNameTo1Relation(implRelation);
                }
                methodsBuilder.appendln("if (" + accessCode + "!=null) {");
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
    
    private void generateMethodGetCardinalityFor1ToManyRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("@inheritDoc", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetCardinalityForRelation(relation, methodsBuilder);
        String[][] params = interfaceBuilder.getParamGetCardinalityForRelation(relation);
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendOpenBracket();
        frag.append("if(");
        frag.append(params[0][0]);
        frag.append(" != null)");
        frag.appendOpenBracket();
        frag.append("return ");
        frag.append('(');
        frag.appendClassName(IntegerRange.class);
        frag.append(')');
        frag.append(getFieldNameCardinalityForRelation(relation));
        frag.append(".get(");
        frag.append(params[0][0]);
        frag.append(".getId());");
        frag.appendCloseBracket();
        frag.append("return null;");
        frag.appendCloseBracket();
        methodsBuilder.append(frag);
    }

    public String getFieldNameCardinalityForRelation(IProductCmptTypeRelation relation) throws CoreException{
        return getLocalizedText(relation, "FIELD_CARDINALITIES_FOR_NAME", relation.findPolicyCmptTypeRelation().getTargetRoleSingular());
    }
    
    private void generateFieldCardinalityForRelation(
            IProductCmptTypeRelation relation, JavaCodeFragmentBuilder fieldsBuilder) throws CoreException{
        appendLocalizedJavaDoc("FIELD_CARDINALITIES_FOR", relation.findPolicyCmptTypeRelation().getTargetRoleSingular(), relation, fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment();
        expression.append(" new ");
        expression.appendClassName(HashMap.class);
        expression.append("(0);");
        fieldsBuilder.varDeclaration(Modifier.PRIVATE, Map.class, getFieldNameCardinalityForRelation(relation), expression);
    }
    
    private void generateMethodGetRangeFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRangeFor(a, helper, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameRangeFor(a));
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameRangeFor(IAttribute a){
        return getLocalizedText(a, "FIELD_RANGE_FOR_NAME", StringUtils.capitalise(a.getName()));
    }
    
    private void generateFieldRangeFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_RANGE_FOR", a.getName(), a, memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, helper.getRangeJavaClassName(), getFieldNameRangeFor(a)); 
    }

    private void generateMethodGetAllowedValuesFor(IAttribute a, Datatype datatype, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllowedValuesFor(a, datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameAllowedValuesFor(a));
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameAllowedValuesFor(IAttribute a){
        return getLocalizedText(a, "FIELD_ALLOWED_VALUES_FOR_NAME", StringUtils.capitalise(a.getName()));
    }
    
    private void generateFieldAllowedValuesFor(IAttribute a, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_ALLOWED_VALUES_FOR", a.getName(), a, memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, EnumValueSet.class, getFieldNameAllowedValuesFor(a)); 
    }

}
