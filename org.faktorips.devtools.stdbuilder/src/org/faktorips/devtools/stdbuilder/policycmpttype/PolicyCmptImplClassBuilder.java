package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.Util;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.internal.DefaultPolicyComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class PolicyCmptImplClassBuilder extends BasePolicyCmptTypeBuilder {

    private final static String VALIDATEDEPS_IMPLEMENTATION_JAVADOC = "VALIDATEDEPS_IMPLEMENTATION_JAVADOC";

    private final static String VALIDATESELF_IMPLEMENTATION_JAVADOC = "VALIDATESELF_IMPLEMENTATION_JAVADOC";

    private final static String CREATEMESSAGEFOR_POLICY_JAVADOC = "CREATEMESSAGEFOR_POLICY_JAVADOC";

    private final static String EXECMESSAGE_POLICY_JAVADOC = "EXECMESSAGE_POLICY_JAVADOC";

    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private PolicyCmptInterfaceBuilder interfaceBuilder;
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    private ProductCmptImplClassBuilder productCmptImplBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplBuilder;

    public PolicyCmptImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptImplClassBuilder.class));
        setMergeEnabled(true);
    }

    public void setInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder) {
        this.interfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return interfaceBuilder;
    }

    public void setProductCmptImplBuilder(ProductCmptImplClassBuilder productCmptImplBuilder) {
        this.productCmptImplBuilder = productCmptImplBuilder;
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }
    
    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }
    
    public void setProductCmptGenImplBuilder(ProductCmptGenImplClassBuilder builder) {
        this.productCmptGenImplBuilder = builder;
    }

    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        String javaSupertype = DefaultPolicyComponent.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(
                getPcType().getSupertype());
            if (supertype != null) {
                javaSupertype = getQualifiedClassName(supertype.getIpsSrcFile());
            }
        }
        return javaSupertype;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return getJavaNamingConvention().getImplementationClassName(StringUtils.capitalise(name));
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { interfaceBuilder.getQualifiedClassName(getPcType()) };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        generateConstructorWithoutProductCmptArg(builder);
        if (getPcType().isConfigurableByProductCmptType()) {
            generateConstructorWithProductCmptArg(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodInitialize(methodsBuilder);
        if (getPcType().isConfigurableByProductCmptType()) {
            generateMethodGetProductCmpt(methodsBuilder);
            generateMethodGetProductCmptGeneration(methodsBuilder);
            generateMethodSetProductCmpt(methodsBuilder);
        }
        buildValidation(methodsBuilder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
     
        if (attribute.isProductRelevant()) {
            generateMethodAttributeGetterFromProductCmpt(attribute, datatypeHelper, methodsBuilder);
        } else {
            if (attribute.getModifier() == org.faktorips.devtools.core.model.pctype.Modifier.PUBLISHED) {
                return;
            }
            interfaceBuilder.generateFieldConstPropertyValue(attribute, datatypeHelper, memberVarsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForChangeableAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMemberVarForAttribute(attribute, datatypeHelper, memberVarsBuilder);
        generateMethodAttributeGetterFromMemberVar(attribute, datatypeHelper, methodsBuilder);
        generateMethodAttributeSetter(attribute, datatypeHelper, methodsBuilder);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMethodAttributeDerivedGetter(attribute, datatypeHelper, methodsBuilder);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        generateMemberVarForAttribute(attribute, datatypeHelper, memberVarsBuilder);
        generateMethodAttributeGetterFromMemberVar(attribute, datatypeHelper, methodsBuilder);
    }
    
    void generateMethodAttributeGetterFromProductCmpt(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaDoc = null; // getLocalizedText(null, a.getName()); // TODO
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
        methodsBuilder.append("().");
        methodsBuilder.append(this.productCmptGenInterfaceBuilder.getMethodNameGetValue(a, datatypeHelper));
        methodsBuilder.append("();");
        methodsBuilder.closeBracket();
    }
    
    protected void generateMemberVarForAttribute(            
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilders) throws CoreException {
        
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        String comment = getLocalizedText(a, "FIELD_ATTRIBUTE_VALUE_JAVADOC", a.getName());
        String fieldName = getFieldNameForAttribute(a);

        memberVarsBuilders.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilders.varDeclaration(java.lang.reflect.Modifier.PRIVATE, datatypeHelper.getJavaClassName(), fieldName,
            initialValueExpression);
    }
    
    private void generateMethodAttributeDerivedGetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder builder) throws CoreException {
        
        Parameter[] parameters = a.getFormulaParameters();
        String[] paramNames = BuilderHelper.extractParameterNames(parameters);
        String[] paramTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(
                parameters, a.getIpsProject(), this);

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_MODIFIABLE);
        interfaceBuilder.generateSignatureGetPropertyValue(a, datatypeHelper, builder);
        builder.openBracket();
        builder.appendln("// TODO Belegung der Berechnungsparameter implementieren");
        JavaCodeFragment paramFragment = new JavaCodeFragment();

        paramFragment.append('(');
        for (int i = 0; i < paramNames.length; i++) {
            builder.appendClassName(paramTypes[i]);
            builder.append(' ');
            builder.append(paramNames[i]);
            builder.append(" = ");
            Datatype paramDataype = a.getIpsProject().findDatatype(parameters[i].getDatatype());
            DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(paramDataype);
            if (helper != null) {
                JavaCodeFragment nullExpressionFragment = helper.nullExpression();
                builder.append(nullExpressionFragment);
            } else {
                builder.append("null");
            }
            builder.appendln(";");
            if (i > 0) {
                paramFragment.append(", ");
            }
            paramFragment.append(paramNames[i]);
        }
        paramFragment.append(")");

        if (a.isProductRelevant()) {
            builder.append(" return ((");
            builder.appendClassName(productCmptGenImplBuilder.getQualifiedClassName(getIpsSrcFile()));
            builder.append(')');
            builder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
            builder.append("()).");
            builder.append(productCmptGenImplBuilder.getMethodNameComputeValue(a));
            builder.append(paramFragment);
            builder.append(";");
        }
        builder.closeBracket();
    }

    /**
     * Returns the name of the field/member variable that stores the values
     * for the property/attribute.
     */
    public String getFieldNameForAttribute(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(a.getName());
    }
    
    /**
     * Code sample:
     * <pre>
     * public Money getPremium() {
     *     return premium;
     * }
     * </pre>
     */
    private void generateMethodAttributeGetterFromMemberVar(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameForAttribute(a));
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * public void setPremium(Money newValue) {
     *     this.premium = newValue;
     * }
     * </pre>
     */
    protected void generateMethodAttributeSetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetPropertyValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("this.");
        methodsBuilder.append(getFieldNameForAttribute(a));
        methodsBuilder.append(" = " + interfaceBuilder.getParamNameForSetPropertyValue(a) + ";");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1To1Relation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (!relation.isReadOnlyContainer()) {
            IPolicyCmptType target = relation.findTarget();
            generateFieldForRelation(relation, target, fieldsBuilder);
            generateMethodGetRefObjectForNoneContainerRelation(relation, methodsBuilder);
            generateMethodSetRefObject(relation, methodsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyRelation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        IPolicyCmptType target = relation.findTarget();
        if (relation.isReadOnlyContainer()) {
            generateMethodContainsObjectForContainerRelation(relation, methodsBuilder);
        } else {
            generateFieldForRelation(relation, target, fieldsBuilder);
            generateMethodGetNumOfForNoneContainerRelation(relation, methodsBuilder);
            generateMethodContainsObjectForNoneContainerRelation(relation, methodsBuilder);
            generateMethodGetAllRefObjectsForNoneContainerRelation(relation, methodsBuilder);
            generateMethodAddObject(relation, methodsBuilder);
            generateMethodRemoveObject(relation, methodsBuilder);
        }
    }
    
    protected void generateFieldForRelation(
            IRelation relation,
            IPolicyCmptType target,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaClassname = relation.is1ToMany() ? List.class.getName()
                : getQualifiedClassName(target);
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();
        if (relation.is1ToMany()) {
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(ArrayList.class);
            initialValueExpression.append("()");
        } else {
            initialValueExpression.append("null");
        }
        String comment = getLocalizedText(relation, "FIELD_RELATION_JAVADOC", relation.getName());
        methodsBuilder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, javaClassname, getFieldNameForRelation(relation),
            initialValueExpression);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     return coverages.size();
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfForNoneContainerRelation(IRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRefObjects(relation, methodsBuilder);
        methodsBuilder.openBracket();
        String field = getFieldNameForRelation(relation);
        if (relation.is1ToMany()) {
            methodsBuilder.appendln("return " + field + ".size();");
        } else {
            methodsBuilder.appendln("return " + field + "==null ? 0 : 1;");
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages() {
     *     int num = 0; 
     *     num += getNumOfCollisionsCoverages(); 
     *     num += tplCoverage==null ? 0 : 1;
     *     return num;
     * }
     * </pre>
     */
    protected void generateMethodGetNumOfForContainerRelationImplementation(
            IRelation containerRelation, 
            List implRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRefObjects(containerRelation, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("int num = 0;");
        for (int i = 0; i < implRelations.size(); i++) {
            methodsBuilder.appendln();
            IRelation relation = (IRelation)implRelations.get(i);
            methodsBuilder.append("num += ");
            if (relation.is1ToMany()) {
                methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(relation) + "();");
            } else {
                String field = getFieldNameForRelation(relation);
                methodsBuilder.append(field + "==null ? 0 : 1;");
            }
        }
        methodsBuilder.append("return num;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     return coverages.contains(objectToTest);
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForNoneContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String paramName = interfaceBuilder.getParamNameForContainsObject(relation);
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainsObject(relation, methodsBuilder);
        
        methodsBuilder.openBracket();
        String field = getFieldNameForRelation(relation);
        methodsBuilder.appendln("return " + field + ".contains(" + paramName + ");");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     ICoverage[] targets = getCoverages();
     *     for (int i = 0; i < targets.length; i++) {
     *         if (targets[i] == objectToTest)
     *             return true;
     *     }
     *     return false;
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String paramName = interfaceBuilder.getParamNameForContainsObject(relation);
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainsObject(relation, methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(interfaceBuilder.getQualifiedClassName(relation.findTarget()));
        methodsBuilder.append("[] targets = ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetAllRefObjects(relation));
        methodsBuilder.append("();");
        methodsBuilder.append("for(int i=0;i < targets.length;i++) {");
        methodsBuilder.append("if(targets[i] == " + paramName + ") return true; }");
        methodsBuilder.append("return false;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages() {
     *     return (ICoverage[])coverages.toArray(new ICoverage[coverages.size()]);
     * }
     * </pre>
     */
    protected void generateMethodGetAllRefObjectsForNoneContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllRefObjects(relation, methodsBuilder);
        String className = getQualifiedClassName(relation.findTarget());
        String field = getFieldNameForRelation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return (");
        methodsBuilder.appendClassName(className);
        methodsBuilder.append("[])");
        methodsBuilder.append(field);
        methodsBuilder.append(".toArray(new ");
        methodsBuilder.appendClassName(className);
        methodsBuilder.append('[');
        methodsBuilder.append(field);
        methodsBuilder.append(".size()]);");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages() {
     *     ICoverage[] result = new ICoverage[getNumOfCoverages()];
     *     ICoverage[] elements;
     *     counter = 0;
     *     elements = getTplCoverages();
     *     for (int i = 0; i < elements.length; i++) {
     *         result[counter] = elements[i];
     *         counter++;
     *     }
     *     return result;
     * }
     * </pre>
     */
    protected void generateMethodGetAllRefObjectsForContainerRelationImplementation(
            IRelation relation,
            List subRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllRefObjects(relation, methodsBuilder);
        String classname = interfaceBuilder.getQualifiedClassName(relation.findTarget());
        
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(classname);
        methodsBuilder.append("[] result = new ");       
        methodsBuilder.appendClassName(classname);
        methodsBuilder.append("[" + interfaceBuilder.getMethodNameGetNumOfRefObjects(relation) + "()];");       

        boolean elementsVarDefined = false;
        methodsBuilder.append("int counter = 0;");
        for (int i = 0; i < subRelations.size(); i++) {
            IRelation subrel = (IRelation)subRelations.get(i);
            if (subrel.is1ToMany()) {
                if (!elementsVarDefined) {
                    methodsBuilder.appendClassName(classname);   
                    methodsBuilder.append("[] ");
                    elementsVarDefined = true;
                }
                String method = interfaceBuilder.getMethodNameGetAllRefObjects(subrel);
                methodsBuilder.appendln("elements = " + method + "();");
                methodsBuilder.appendln("for (int i=0; i<elements.length; i++) {");
                methodsBuilder.appendln("result[counter++] = elements[i];");
                methodsBuilder.appendln("}");
            } else {
                String method = interfaceBuilder.getMethodNameGetRefObject(subrel);
                methodsBuilder.appendln("if (" + method + "()!=null) {");
                methodsBuilder.appendln("result[counter++] = " + method + "();");
                methodsBuilder.appendln("}");    
            }
        }
        methodsBuilder.append("return result;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return coverage;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectForNoneContainerRelation(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(relation, methodsBuilder);
        String field = getFieldNameForRelation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.appendln("return " + field + ";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     if(getNumOfTplCoverage() > 0) { 
     *         return getTplCoverage(); 
     *     } 
     *     if (getNumOfCollisionCoverage() > 0) { 
     *         return getCollisionCoverage(); 
     *     } 
     *     return null;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectForContainerRelationImplementation(
            IRelation relation,
            List subRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRefObject(relation, methodsBuilder);
        methodsBuilder.openBracket();
        for (int i = 0; i < subRelations.size(); i++) {
            IRelation subrel = (IRelation)subRelations.get(i);
            String field = getFieldNameForRelation(subrel);
            methodsBuilder.appendln("if (" + field + "!=null) {");
            methodsBuilder.appendln("return " + field + ";");
            methodsBuilder.appendln("}");
        }
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd) {
     *     if(objectToAdd == null) { 
     *         throw new IllegalArgumentException("Can't add null to ...");
     *     }
     *     if (coverages.contains(objectToAdd)) { 
     *         return; 
     *     }
     *     coverages.add(objectToAdd);
     * }
     * </pre>
     */
    protected void generateMethodAddObject (
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureAddObject(relation, methodsBuilder);
        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForAddObject(relation);
        IRelation reverseRelation = relation.findReverseRelation();
        methodsBuilder.openBracket();
        methodsBuilder.append("if (" + paramName + " == null) {");
        methodsBuilder.append("throw new ");
        methodsBuilder.appendClassName(NullPointerException.class);
        methodsBuilder.append("(\"Can't add null to relation " + relation.getName() + " of \" + this); }");
        methodsBuilder.append("if(");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".contains(" + paramName + ")) { return; }");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".add(" + paramName + ");");
        if (reverseRelation != null) {
            String targetClass = interfaceBuilder.getQualifiedClassName(relation.findTarget());
            methodsBuilder.append(generateCodeToSynchronizeReverseRelation(paramName, targetClass, relation, reverseRelation));
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void removeMotorCoverage(IMotorCoverage objectToRemove) {
     *     if (objectToRemove == null) {
     *          return;
     *      }
     *      if (motorCoverages.remove(objectToRemove)) {
     *          objectToRemove.setMotorPolicy(null);
     *      }
     *  }
     * </pre>
     */
    protected void generateMethodRemoveObject(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForRemoveObject(relation);
        IRelation reverseRelation = relation.findReverseRelation();

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureRemoveObject(relation, methodsBuilder);

        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + "== null) {return;}");
        
        if (reverseRelation != null) {
            methodsBuilder.append("if(");
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(".remove(" + paramName + ")");
        if (reverseRelation != null) {
            methodsBuilder.append(") {");
            methodsBuilder.append(generateCodeToCleanupOldReference(relation, reverseRelation, paramName));
            methodsBuilder.append(" }");
        } else {
            methodsBuilder.append(';');
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *     if (refObject == homeContract)
     *         return;
     *     IHomeContract oldRefObject = homeContract;
     *     homeContract = null;
     *     if (oldRefObject != null) {
     *          oldRefObject.setHomePolicy(null);
     *     }
     *     homeContract = (HomeContract) refObject;
     *     if (refObject != null && refObject.getHomePolicy() != this) {
     *         refObject.setHomePolicy(this);
     *     }
     * }
     * </pre>
     */
    protected void generateMethodSetRefObject(
            IRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        String fieldname = getFieldNameForRelation(relation);
        String paramName = interfaceBuilder.getParamNameForSetObject(relation);
        IPolicyCmptType target = relation.findTarget();
        IRelation reverseRelation = relation.findReverseRelation();

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetObject(relation, methodsBuilder);
        
        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + " == ");
        methodsBuilder.append(fieldname);
        methodsBuilder.append(") return;");
        if (reverseRelation != null) {
            methodsBuilder.appendClassName(getQualifiedClassName(target));
            methodsBuilder.append(" oldRefObject = ");
            methodsBuilder.append(fieldname);
            methodsBuilder.append(';');
            methodsBuilder.append(fieldname);
            methodsBuilder.append(" = null;");
            methodsBuilder.append(generateCodeToCleanupOldReference(relation, reverseRelation, "oldRefObject"));
        }
        methodsBuilder.append(fieldname);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(getQualifiedClassName(target));
        methodsBuilder.append(")" + paramName +";");
        if (reverseRelation != null) {
            methodsBuilder.append(generateCodeToSynchronizeReverseRelation(fieldname, 
                    getQualifiedClassName(target), relation, reverseRelation));
        }
        methodsBuilder.closeBracket();
    }
    
    private JavaCodeFragment generateCodeToSynchronizeReverseRelation(
            String varName,
            String varClassName,
            IRelation relation,
            IRelation reverseRelation) throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        if (!relation.is1ToMany()) {
            code.append(varName + " != null && ");
        }
        if (reverseRelation.is1ToMany()) {
            code.append("! " + varName + ".");
            code.append(interfaceBuilder.getMethodNameContainsObject(reverseRelation) + "(this)");
        } else {
            code.append(varName + ".");
            code.append(interfaceBuilder.getMethodNameGetRefObject(reverseRelation));
            code.append("() != this");
        }
        code.append(") {");
        if (reverseRelation.is1ToMany()) {
            code.append(varName + "." + interfaceBuilder.getMethodNameAddObject(reverseRelation));
        } else {
            String targetClass = getQualifiedClassName(relation.findTarget());
            if (!varClassName.equals(targetClass)) {
                code.append("((");
                code.appendClassName(targetClass);
                code.append(")" + varName + ").");
            } else {
                code.append(varName + ".");
            }
            code.append(interfaceBuilder.getMethodNameSetObject(reverseRelation));
        }
        code.appendln("(this);");
        code.appendln("}");
        return code;
    }
    
    
    private JavaCodeFragment generateCodeToCleanupOldReference(
            IRelation relation, 
            IRelation reverseRelation,
            String varToCleanUp) throws CoreException {
        
        JavaCodeFragment body = new JavaCodeFragment();
        if (!relation.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (reverseRelation.is1ToMany()) {
            String removeMethod = interfaceBuilder.getMethodNameRemoveObject(reverseRelation);
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String targetClass = getQualifiedClassName(relation.findTarget());
            String setMethod = interfaceBuilder.getMethodNameSetObject(reverseRelation);
            body.append("((");
            body.appendClassName(targetClass);
            body.append(")" + varToCleanUp + ")." + setMethod + "(null);");
        }
        if (!relation.is1ToMany()) {
            body.append(" }");
        }
        return body;
    }

    /**
     * Returns the name of field/member var for the relation.
     */
    public String getFieldNameForRelation(IRelation relation) throws CoreException {
        if (relation.is1ToMany()) {
            return getJavaNamingConvention().getMemberVarName(relation.getTargetRolePlural());
        } else {
            return getJavaNamingConvention().getMemberVarName(relation.getTargetRoleSingular());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationImplementation(
            IRelation containerRelation,
            List relations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        if (containerRelation.is1ToMany()) {
            generateMethodGetNumOfForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
            generateMethodGetAllRefObjectsForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        } else {
            generateMethodGetRefObjectForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        }
    }

    private void buildValidation(JavaCodeFragmentBuilder builder) throws CoreException {
        createMethodValidateSelf(builder, getPcType().getAttributes());
        createMethodValidateDependants(builder);
        buildExecRule(builder);
        buildMessageForRule(builder);
    }

    String getPolicyCmptImplGetNumOfMethodName(IRelation r) {
        return "getAnzahl" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    String getPolicyCmptImplGetMethodName(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    String getPolicyCmptImplGetAllMethodName(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    private void createMethodValidateDependants(JavaCodeFragmentBuilder builder)
            throws CoreException {
        /*
         * public void validateDependants(MessageList ml) { if(NumOfRelToMany() > 0) { TargetType[]
         * rels = GetAllRelationToMany(); for (int i = 0; i < rels.length; i++) {
         * ml.add(rels[i].validate()); } if (NumOfRelTo1() > 0) {
         * ml.add(GetRelationTo1().validate()); } }
         */

        String methodName = "validateDependants";
        IRelation[] relations = getPcType().getRelations();

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml);");
        for (int i = 0; i < relations.length; i++) {
            IRelation r = relations[i];
            if (!r.validate().containsErrorMsg()) {
                if (r.getRelationType() == RelationType.COMPOSITION
                        && StringUtils.isEmpty(r.getContainerRelation())) {
                    body.appendln();
                    if (r.is1ToMany()) {
                        IPolicyCmptType target = r.getIpsProject().findPolicyCmptType(r.getTarget());
                        body.append("if(");
                        body.append(getPolicyCmptImplGetNumOfMethodName(r));
                        body.append("() > 0) { ");
                        body.appendClassName(interfaceBuilder
                                .getQualifiedClassName(target.getIpsSrcFile()));
                        body.append("[] rels = ");
                        body.append(getPolicyCmptImplGetAllMethodName(r));
                        body.append("();");
                        body.append("for (int i = 0; i < rels.length; i++)");
                        body.append("{ ml.add(rels[i].validate()); } }");
                    } else {
                        String field = getFieldNameForRelation(r);
                        body.append("if (" + field + "!=null) {");
                        body.append("ml.add(" + field + ".validate());");
                        body.append("}");
                    }
                }
            }
        }

        String javaDoc = getLocalizedText(getPcType(), VALIDATEDEPS_IMPLEMENTATION_JAVADOC, getPcType()
                .getName());

        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "ml" }, new String[] { MessageList.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    private void createMethodValidateSelf(JavaCodeFragmentBuilder builder, IAttribute[] attributes)
            throws CoreException {
        /*
         * public void validateSelf(MessageList ml) { super.validateSelf(ml); }
         */
        String methodName = "validateSelf";
        String javaDoc = getLocalizedText(getIpsObject(), VALIDATESELF_IMPLEMENTATION_JAVADOC, getPcType()
                .getName());

        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml);");
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            body.append("execRule");
            body.append(StringUtils.capitalise(r.getName()));
            body.append("(ml);");
        }
        // buildValidationValueSet(body, attributes); wegschmeissen ??
        builder.method(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(),
            methodName, new String[] { "ml" }, new String[] { MessageList.class.getName() }, body,
            javaDoc, ANNOTATION_GENERATED);
    }

    private void buildMessageForRule(JavaCodeFragmentBuilder builder) throws JavaModelException {
        // nur Behandlung der Fehlermeldungen in dieser Klasse
        // Eigentliche Pruefung muss in Extension-Klassen impl. werden
        // protected Message createMessageForRulePlzVorhanden(Object p0, Object p1) {
        // Object[] replacements = new Object[2];
        // replacements[0] = p0;
        // replacements[1] = p1;
        // MessageFormat mf = new MessageFormat("Die Postleitzahl muss eingegeben werden.");
        // return new Message("HRP01", mf.format(replacements), Message.ERROR); }
        IValidationRule[] rules = getPcType().getRules();

        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            String[] paramNameList = BuilderHelper.extractMessageParameters(r.getMessageText());
            String[] paramTypeList = new String[paramNameList.length];
            JavaCodeFragment body = new JavaCodeFragment();
            if (paramNameList.length > 0) {
                body.append("Object[] replacements = new Object[");
                body.append(paramNameList.length);
                body.append("];");
                for (int j = 0; j < paramNameList.length; j++) {
                    paramTypeList[j] = Object.class.getName();
                    body.append("replacements[");
                    body.append(j);
                    body.append("] = ");
                    body.append(paramNameList[j]);
                    body.append(';');
                }
                body.appendClassName(MessageFormat.class);
                body.append(" mf = new ");
                body.appendClassName(MessageFormat.class);
                body.append("(\"");
                body.append(BuilderHelper.transformMessage(r.getMessageText()));
                body.append("\");");
            }
            body.append("return new ");
            body.appendClassName(Message.class);
            body.append("(\"");
            body.append(r.getMessageCode());
            body.append("\", ");
            if (paramNameList.length > 0) {
                body.append("mf.format(replacements), ");
            } else {
                body.append('\"');
                body.append(r.getMessageText());
                body.append("\", ");
            }

            body.append(r.getMessageSeverity().getJavaSourcecode());
            body.append(");");

            String methodName = "createMessageForRule" + StringUtils.capitalise(r.getName());

            String javaDoc = getLocalizedText(r, CREATEMESSAGEFOR_POLICY_JAVADOC, r.getName());

            builder.method(java.lang.reflect.Modifier.PROTECTED, Message.class.getName(),
                methodName, paramNameList, paramTypeList, body, javaDoc, ANNOTATION_GENERATED);
        }
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Policy(Product productCmpt, Date effectiveDate) {
     *     super(productCmpt, effectiveDate);
     *     initialize();
     * }
     * </pre>
     */
    protected void generateConstructorWithProductCmptArg(JavaCodeFragmentBuilder builder)
            throws CoreException {

        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getPcType(), builder);
        String[] paramNames = new String[] { "productCmpt", "effectiveDate"};
        String[] paramTypes = new String[] { 
                productCmptInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile()),
                Calendar.class.getName()};
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
                paramNames, paramTypes);
        builder.append("super(productCmpt, effectiveDate);");
        builder.append("initialize();");
        builder.methodEnd();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Policy(Product productCmpt, Date effectiveDate) {
     *     super(productCmpt, effectiveDate);
     *     initialize();
     * }
     * </pre>
     */
    protected void generateConstructorWithoutProductCmptArg(JavaCodeFragmentBuilder builder)
        throws CoreException {

        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getPcType(), builder);
        String[] paramTypes = new String[] {Calendar.class.getName()};
        String[] paramNames = new String[] {"effectiveDate"};
        
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
                paramNames, paramTypes);
        builder.append("super(effectiveDate);");
        builder.append("initialize();");
        builder.methodEnd();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * protected void initialize() {
     *     super.initialize();
     *     paymentMode = getProductGen().getDefaultPaymentMode();
     *     ... and so for other properties
     * }
     * </pre>
     */
    protected void generateMethodInitialize(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_INITIALIZE", getPcType(), builder);
        builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, Datatype.VOID.getJavaClassName(),
                "initialize", new String[0], new String[0]);
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            builder.append("super.initialize();");
        }
        IAttribute[] attributes = getPcType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (!a.validate().containsErrorMsg()) {
                if (a.isProductRelevant() && a.getAttributeType() == AttributeType.CHANGEABLE) {
                    DatatypeHelper datatype = a.getIpsProject().findDatatypeHelper(a.getDatatype());
                    builder.append(getFieldNameForAttribute(a));
                    builder.append(" = ");
                    builder.append(getMethodNameGetValueFromProductCmpt(a, datatype));
                    builder.append(";");
                }
            }
        }
        builder.methodEnd();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IProduct getProduct() {
     *     return (IProduct) getProductComponent();
     * }
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetProductCmpt(getProductCmptType(), builder);
        builder.openBracket();
        String productCmptInterfaceQualifiedName = productCmptInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile());
        builder.append("return (");
        builder.appendClassName(productCmptInterfaceQualifiedName);
        builder.append(")getProductComponent();"); // don't use getMethodNameGetProductComponent() as this results in a recursive call
        // we have to call the generic superclass method here
        builder.closeBracket();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IProductGen getProductGen() {
     *     return (IProductGen) getProduct().getProductGen(getEffectiveFrom());
     * }
     * </pre>
     */
    protected void generateMethodGetProductCmptGeneration(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetProductCmptGeneration(getProductCmptType(), builder);
        builder.openBracket();
        builder.append("return (");
        builder.appendClassName(productCmptGenInterfaceBuilder.getQualifiedClassName(getIpsSrcFile()));
        builder.append(")");
        builder.append(interfaceBuilder.getMethodNameGetProductCmpt(getProductCmptType()));
        builder.append("().");
        builder.append(productCmptInterfaceBuilder.getMethodNameGetGeneration(getPolicyCmptType().findProductCmptType()));
        builder.append('(');
        builder.append(getEffectiveDateMethodName());
        builder.appendln("());");
        builder.closeBracket();
    }
    
    private void generateMethodSetProductCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureSetProductCmpt(getProductCmptType(), builder);
        String[] paramNames = interfaceBuilder.getMethodParamNamesSetProductCmpt(getProductCmptType());
        builder.openBracket();
        builder.appendln("setProductCmpt(" + paramNames[0] + ");");
        builder.appendln("if(" + paramNames[1] + ") { initialize(); }");
        builder.closeBracket();
    }

    /**
     * 
     */
    private String getMethodNameGetValueFromProductCmpt(IAttribute a, DatatypeHelper datatype) throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = productCmptGenInterfaceBuilder.getMethodNameGetDefaultValue(a, datatype);
        } else {
            methodName = productCmptGenInterfaceBuilder.getMethodNameGetValue(a, datatype);
        }
        return interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()) + "()." + methodName + "()";
    }

    // TODO brauchen wir das noch??? -- from ext
    private JavaCodeFragment getMethodBodyReturningDefaultOrNull(IAttribute a, Datatype datatype) {
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln(" // TODO implement method");
        body.append("return ");
        JavaCodeFragment initialValueExpression = null;
        DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(datatype);
        if (helper != null) {
            helper.newInstance(a.getDefaultValue());
        }
        if (initialValueExpression != null) {
            body.append(initialValueExpression);
            body.append(";");
        } else {
            body.append("null;");
        }
        return body;
    }

    // --from ext
    private void buildExecRule(JavaCodeFragmentBuilder builder) throws JavaModelException {
        // private void execRulePlzVorhanden(MessageList ml) {
        // if (false) {
        // ml.add(createMessageForRulePlzVorhanden()); } }
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            String javaDoc = getLocalizedText(getIpsObject(), EXECMESSAGE_POLICY_JAVADOC, r.getName());
            JavaCodeFragment body = new JavaCodeFragment();
            body.append("if(true) ");
            body.appendOpenBracket();
            body.append("ml.add(createMessageForRule");
            body.append(StringUtils.capitalise(r.getName()));
            body.append('(');
            String[] paramList = BuilderHelper.extractMessageParameters(r.getMessageText());
            for (int j = 0; j < paramList.length; j++) {
                if (j > 0) {
                    body.append(", ");
                }
                body.append('\"');
                body.append(paramList[j]);
                body.append('\"');
            }
            body.append("));");
            body.appendCloseBracket();

            builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.VOID.getJavaClassName(),
                "execRule" + StringUtils.capitalise(r.getName()), new String[] { "ml" },
                new String[] { MessageList.class.getName() }, body, javaDoc, ANNOTATION_MODIFIABLE);
        }
    }

    // --

    private String getPolicyValueSetFieldName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetField(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {

        String fieldName = getPolicyValueSetFieldName(a);
        String dataTypeValueSet;
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();

        if (a.getValueSet().isRange()) {
            dataTypeValueSet = helper.getRangeJavaClassName();
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(helper.getRangeJavaClassName());
            initialValueExpression.append("( ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet())
                    .getLowerBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet())
                    .getUpperBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet()).getStep()));
            initialValueExpression.append(" ) ");
        } else {
            dataTypeValueSet = datatype.getJavaClassName() + "[]";
            initialValueExpression = new JavaCodeFragment();
            String[] elements = ((EnumValueSet)a.getValueSet()).getValues();
            initialValueExpression.append("{ ");
            for (int i = 0; i < elements.length; i++) {
                if (i > 0) {
                    initialValueExpression.append(", ");
                }
                if (elements[i].equals("null")) {
                    initialValueExpression.append(helper.nullExpression());
                } else {
                    initialValueExpression.append(helper.newInstance(elements[i]));
                }
            }
            initialValueExpression.append(" }");
        }
        String comment = getLocalizedText(a, "FIELD_ATTRIBUTE_VALUESET_JAVADOC", a.getName());

        builder.javaDoc(comment, ANNOTATION_GENERATED);
        builder.varDeclaration(java.lang.reflect.Modifier.PROTECTED
                | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC,
            dataTypeValueSet, fieldName, initialValueExpression);
    }

    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper,
            String fieldName) throws CoreException {
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(fieldName);
        body.append(";");
        createAttributeValueSetMethods(builder, a, datatype, helper, body);
    }

    private String getPolicyImplGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private String getPolicyImplGetValueSetMethodName(IAttribute a) {
        return "getWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper,
            JavaCodeFragment bodyMax) throws CoreException {

        String methodNameMax = getPolicyImplGetMaxValueSetMethodName(a);
        String methodName = getPolicyImplGetValueSetMethodName(a);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(methodNameMax);
        body.append("();");
        String javaDocMax = getLocalizedText(a, JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
        String javaDoc = getLocalizedText(a, JAVA_GETTER_METHOD_VALUESET, a.getName());

        if (a.getValueSet().isRange()) {

            builder.method(Util.getJavaModifier(a.getModifier()), helper.getRangeJavaClassName(),
                methodNameMax, new String[] {}, new String[] {}, bodyMax, javaDocMax);

            builder.method(Util.getJavaModifier(a.getModifier()), helper.getRangeJavaClassName(),
                methodName, new String[] {}, new String[] {}, body, javaDoc, ANNOTATION_MODIFIABLE);
        } else {

            builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName()
                    + "[]", methodNameMax, new String[] {}, new String[] {}, bodyMax, javaDocMax,
                ANNOTATION_GENERATED);

            builder.method(Util.getJavaModifier(a.getModifier()), datatype.getJavaClassName()
                    + "[]", methodName, new String[] {}, new String[] {}, body, javaDoc,
                ANNOTATION_MODIFIABLE);
        }
    }

    private void createAttributeValueSetMethods(JavaCodeFragmentBuilder builder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {

        String methodName = getPolicyImplGetMaxValueSetMethodName(a);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("return ");
        body.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
        body.append('.');
        body.append(methodName);
        body.append("();");
        createAttributeValueSetMethods(builder, a, datatype, helper, body);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(IMethod method, Datatype returnType, Datatype[] paramTypes, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (method.getModifier()==Modifier.PUBLISHED) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_MODIFIABLE);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_MODIFIABLE);
        }
        interfaceBuilder.generateSignatureForMethodDefinedInModel(method, method.getJavaModifier(),
                returnType, paramTypes, methodsBuilder);
        if (method.isAbstract()) {
            methodsBuilder.appendln(";");
            return;
        }
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement model method.");
        methodsBuilder.append("throw new RuntimeException(\"Not implemented yet!\");");
        methodsBuilder.closeBracket();
    }
}