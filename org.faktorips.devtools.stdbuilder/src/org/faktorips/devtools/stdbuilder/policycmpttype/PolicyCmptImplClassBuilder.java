package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.text.MessageFormat;
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

    private final static String ATTRIBUTE_FIELD_COMMENT = "ATTRIBUTE_FIELD_COMMENT";

    private final static String CONSTRUCTOR_POLICY_JAVADOC = "CONSTRUCTOR_POLICY_JAVADOC";

    private final static String VALIDATEDEPS_IMPLEMENTATION_JAVADOC = "VALIDATEDEPS_IMPLEMENTATION_JAVADOC";

    private final static String VALIDATESELF_IMPLEMENTATION_JAVADOC = "VALIDATESELF_IMPLEMENTATION_JAVADOC";

    private final static String CREATEMESSAGEFOR_POLICY_JAVADOC = "CREATEMESSAGEFOR_POLICY_JAVADOC";

    private final static String EXECMESSAGE_POLICY_JAVADOC = "EXECMESSAGE_POLICY_JAVADOC";

    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private final static String ATTRIBUTE_DERIVED_GETTER_JAVADOC = "ATTRIBUTE_DERIVED_GETTER_JAVADOC";

    private PolicyCmptInterfaceBuilder interfaceBuilder;
    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    private ProductCmptImplClassBuilder productCmptImplBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private ProductCmptGenImplClassBuilder productCmptGenImplBuilder;

    private static final String INITIALIZE_JAVADOC = "INITIALIZE_JAVADOC";

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
    
    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (interfaceBuilder == null) {
            builderName = PolicyCmptInterfaceBuilder.class.getName();
        }

        if (productCmptInterfaceBuilder == null) {
            builderName = ProductCmptInterfaceBuilder.class.getName();
        }

        if (productCmptImplBuilder == null) {
            builderName = ProductCmptImplClassBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }
    
    /**
     * Overriden.
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
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
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
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { getInterfaceName() };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        createInitMethod(methodsBuilder);
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
            interfaceBuilder.generateStaticAttributeVariable(attribute, datatypeHelper, memberVarsBuilder);
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
        interfaceBuilder.generateSignatureAttributeGetter(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
        methodsBuilder.append("().");
        methodsBuilder.append(this.productCmptGenInterfaceBuilder.getMethodNameGetValue(a, datatypeHelper));
        methodsBuilder.append("();");
        methodsBuilder.closeBracket();
    }
    
    private void generateMemberVarForAttribute(            
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilders) throws CoreException {
        
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        String comment = getLocalizedText(a, ATTRIBUTE_FIELD_COMMENT, a.getName());
        String fieldName = getMemberVarNameForAttribute(a);

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
        JavaCodeFragment paramFragment = new JavaCodeFragment();
        JavaCodeFragment variableFragement = new JavaCodeFragment();

        paramFragment.append('(');
        for (int i = 0; i < paramNames.length; i++) {
            variableFragement.appendClassName(paramTypes[i]);
            variableFragement.append(' ');
            variableFragement.append(paramNames[i]);
            variableFragement.append(" = ");
            Datatype paramDataype = a.getIpsProject().findDatatype(parameters[i].getDatatype());
            DatatypeHelper helper = a.getIpsProject().getDatatypeHelper(paramDataype);
            if (helper != null) {
                JavaCodeFragment nullExpressionFragment = helper.nullExpression();
                variableFragement.append(nullExpressionFragment);
            } else {
                variableFragement.append("null");
            }
            variableFragement.appendln(';');
            if (i > 0) {
                paramFragment.append(", ");
            }
            paramFragment.append(paramNames[i]);
        }
        paramFragment.append(")");

        JavaCodeFragment body = new JavaCodeFragment();
        body.appendln("// TODO Belegung der Berechnungsparameter implementieren");
        body.append(variableFragement);
        if (a.isProductRelevant()) {
            body.append(" return ((");
            body.appendClassName(productCmptGenImplBuilder.getQualifiedClassName(getIpsSrcFile()));
            body.append(')');
            body.append(interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()));
            body.append("()).");
            body.append(productCmptGenImplBuilder.getMethodNameComputeValue(a));
            body.append(paramFragment);
            body.append(";");
        }

        String javaDoc = getLocalizedText(a, ATTRIBUTE_DERIVED_GETTER_JAVADOC, a.getName());
        builder.method(Util.getJavaModifier(a.getModifier()), datatypeHelper.getJavaClassName(),
            getPcInterfaceGetValueMethodName(a), new String[0], new String[0], body, javaDoc,
            ANNOTATION_MODIFIABLE);
    }

    public String getMemberVarNameForAttribute(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(a.getName());
    }
    
    private void generateMethodAttributeGetterFromMemberVar(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureAttributeGetter(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getMemberVarNameForAttribute(a));
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }
    
    private void generateMethodAttributeSetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureAttributeSetter(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append(getMemberVarNameForAttribute(a));
        methodsBuilder.append(" = newValue;");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1To1Relation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (!relation.isReadOnlyContainer()) {
            generateMethodGetNumOfForNoneContainerRelation(relation, methodsBuilder);
            generateMethodGetRefObjectForNoneContainerRelation(relation, methodsBuilder);
        }
        PolicyCmptTypeImplRelationBuilder relationBuilder = new PolicyCmptTypeImplRelationBuilder(
                this);
        relationBuilder.build1To1Relation(fieldsBuilder, methodsBuilder, relation, null);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeFor1ToManyRelation(IRelation relation, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (!relation.isReadOnlyContainer()) {
            generateMethodGetNumOfForNoneContainerRelation(relation, methodsBuilder);
            generateMethodGetAllRefObjectsForNoneContainerRelation(relation, methodsBuilder);
        } 
        
        PolicyCmptTypeImplRelationBuilder relationBuilder = new PolicyCmptTypeImplRelationBuilder(
                this);
        relationBuilder.build1ToManyRelation(fieldsBuilder, methodsBuilder, relation, null);
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
     *     num += getNumOfHausratvertrag(); 
     *     num += getNumOfGlasvertrag();
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
            methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(relation));
            methodsBuilder.append("();");
        }
        methodsBuilder.append("return num;");
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
        
        methodsBuilder.appendClassName(classname);   
        methodsBuilder.append("[] elements;");
        
        methodsBuilder.append("int counter = 0;");
        for (int i = 0; i < subRelations.size(); i++) {
            IRelation subrel = (IRelation)subRelations.get(i);
            if (subrel.is1ToMany()) {
                methodsBuilder.append("elements = ");
                methodsBuilder.append(interfaceBuilder.getMethodNameGetAllRefObjects(subrel));
                methodsBuilder.append("();");
                methodsBuilder.append("for(int i=0; i<elements.length; i++) {");
                methodsBuilder.append("result[counter++] = elements[i]; }");
            } else {
                methodsBuilder.append("if(");
                methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(subrel));
                methodsBuilder.append("() > 0) {");
                methodsBuilder.append("result[counter++] = ");
                methodsBuilder.append(interfaceBuilder.getMethodNameGetRefObject(subrel));
                methodsBuilder.append("();}");    
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
            methodsBuilder.append("if(");
            IRelation subrel = (IRelation)subRelations.get(i);
            methodsBuilder.append(interfaceBuilder.getMethodNameGetNumOfRefObjects(subrel));
            methodsBuilder.append("() > 0)");
            methodsBuilder.openBracket();
            methodsBuilder.append("return ");
            methodsBuilder.append(interfaceBuilder.getMethodNameGetRefObject(subrel));
            methodsBuilder.append("();");
            methodsBuilder.closeBracket();
        }
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
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

        generateMethodGetNumOfForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        if (containerRelation.is1ToMany()) {
            generateMethodGetAllRefObjectsForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        } else {
            generateMethodGetRefObjectForContainerRelationImplementation(containerRelation, relations, methodsBuilder);
        }
        PolicyCmptTypeImplRelationBuilder relationBuilder = new PolicyCmptTypeImplRelationBuilder(
            this);
        relationBuilder.buildContainerRelation(memberVarsBuilder, methodsBuilder, containerRelation, relations);

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

        // TODO Methodenname von pcType holen
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
                        body.append("if(");
                        body.append(getPolicyCmptImplGetNumOfMethodName(r));
                        body.append("() > 0) { ml.add(");
                        body.append(getPolicyCmptImplGetMethodName(r));
                        body.append("().validate()); }");
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

        // TODO Methodenname von pcType holen
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
     * @return
     * @throws CoreException
     */
    private String getInterfaceName() throws CoreException {
        return interfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile());
    }

    private void generateConstructorWithProductCmptArg(JavaCodeFragmentBuilder builder)
            throws CoreException {
        /*
         * Beispiel: public MotorPolicy(MotorPolicyPk pc) { super(pc); ... } ... steht fuer
         * Initialisierung von produktrelevanten, aenderbaren Attributen
         */
        String javaDoc = getLocalizedText(getIpsObject(), CONSTRUCTOR_POLICY_JAVADOC, getUnqualifiedClassName());
        JavaCodeFragment fragment = new JavaCodeFragment();
        String[] paramTypes, paramNames;
        paramTypes = new String[] { 
                productCmptInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile()),
                Calendar.class.getName()};
        paramNames = new String[] { "productCmpt", "effectiveDate"};
        fragment.append("super(productCmpt, effectiveDate);");
        fragment.append("initialize();");
        builder.method(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
            paramNames, paramTypes, fragment, javaDoc, ANNOTATION_GENERATED);
    }

    private void generateConstructorWithoutProductCmptArg(JavaCodeFragmentBuilder builder)
        throws CoreException {
    /*
     * Beispiel: public MotorPolicy(GregorianCalendar effectiveDate) { super(pc); ... } ... steht fuer
     * Initialisierung von produktrelevanten, aenderbaren Attributen
     */
    String javaDoc = getLocalizedText(getIpsObject(), CONSTRUCTOR_POLICY_JAVADOC, getUnqualifiedClassName());
    JavaCodeFragment fragment = new JavaCodeFragment();
    String[] paramTypes, paramNames;
    paramTypes = new String[] {Calendar.class.getName()};
    paramNames = new String[] {"effectiveDate"};
    fragment.append("super(effectiveDate);");
    fragment.append("initialize();");
    builder.method(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(),
        paramNames, paramTypes, fragment, javaDoc, ANNOTATION_GENERATED);
    }

    private void createInitMethod(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText(getIpsObject(), INITIALIZE_JAVADOC);
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            fragment.append("super.initialize();");
        }
        IAttribute[] attributes = getPcType().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (!a.validate().containsErrorMsg()) {
                if (a.isProductRelevant() && a.getAttributeType() == AttributeType.CHANGEABLE) {
                    fragment.append(getPolicyFieldValueName(a));
                    fragment.append(" = ");
                    fragment.append(getAttributeValueFromPk(a));
                    fragment.append(";");
                }
            }
        }

        builder.method(java.lang.reflect.Modifier.PROTECTED, Datatype.VOID.getJavaClassName(),
            "initialize", new String[0], new String[0], fragment, javaDoc, ANNOTATION_GENERATED);
    }

    private void generateMethodGetProductCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
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

    private void generateMethodGetProductCmptGeneration(JavaCodeFragmentBuilder builder) throws CoreException {
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

    // duplicate in ProductCmptImplCuBuilder and ProductCmptInterfaceCuBuilder
    private String getPcInterfaceGetDefaultValueMethodName(IAttribute a) {
        return "getVorgabewert" + StringUtils.capitalise(a.getName());
    }

    // duplicate in ProductCmptInterfaceCuBuilder, ProductCmptImplCuBuilder
    private String getPcInterfaceGetValueMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param buffer
     * @param a
     * @throws CoreException
     */
    private String getAttributeValueFromPk(IAttribute a) throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = getPcInterfaceGetDefaultValueMethodName(a);
        } else {
            methodName = getPcInterfaceGetValueMethodName(a);
        }
        return interfaceBuilder.getMethodNameGetProductCmptGeneration(getProductCmptType()) + "()." + methodName + "()";
    }

    // Duplicate is also in ProductCmptGenerationBuilder
    private String getPolicyCmptImplComputeMethodName(IAttribute a) {
        return "compute" + StringUtils.capitalise(a.getName());
    }

    // -- from ext
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

    private String getPolicyFieldValueName(IAttribute a) {
        return a.getName();
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
        String comment = getLocalizedText(a, ATTRIBUTE_FIELD_COMMENT, a.getName());

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
        body.append(getPkGetter());
        body.append('.');
        body.append(methodName);
        body.append("();");
        createAttributeValueSetMethods(builder, a, datatype, helper, body);
    }

    private String getPkGetter() throws CoreException {
        return "get" + StringUtils.capitalise(productCmptInterfaceBuilder.getConceptName(getIpsSrcFile()))+ "()";
    }

    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
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