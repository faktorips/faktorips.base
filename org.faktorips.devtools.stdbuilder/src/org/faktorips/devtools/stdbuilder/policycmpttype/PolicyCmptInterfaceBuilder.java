package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.IPolicyComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    private final static String ATTRIBUTE_FIELD_COMMENT = "ATTRIBUTE_FIELD_COMMENT";

    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    public PolicyCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class));
        setMergeEnabled(true);
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }
    
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getPolicyCmptTypeName(ipsSrcFile));
    }
    
    public String getPolicyCmptTypeName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return StringUtils.capitalise(name);
    }
    
    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (productCmptInterfaceBuilder == null) {
            builderName = ProductCmptInterfaceBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException("One of the builders this builder depends on is not set: " + builderName);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IPolicyComponent.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(getPcType().getSupertype());
            javaSupertype = supertype == null ? javaSupertype : getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return new String[] { javaSupertype };
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("INTERFACE", getIpsObject().getName(), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        if (getPcType().isConfigurableByProductCmptType()) {
            generateMethodGetProductCmpt(methodsBuilder);
            generateMethodSetProductCmpt(methodsBuilder);
            generateMethodGetProductCmptGeneration(getProductCmptType(), methodsBuilder);
        }
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorProduct getMotorProduct();
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getProductCmptType().getName(), getPcType().getName()};
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT", replacements, getPcType(), methodsBuilder);
        generateSignatureGetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.append(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public IMotorProduct getMotorProduct()
     * </pre>
     */
    public void generateSignatureGetProductCmpt(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType = productCmptInterfaceBuilder.getQualifiedClassName(type);
        String methodName = getMethodNameGetProductCmpt(type);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }
    
    /**
     * Returns the name of the method to access the product component, e.g. getMotorProduct
     */
    public String getMethodNameGetProductCmpt(IProductCmptType type) throws CoreException {
        return getLocalizedText(type, "METHOD_GET_PRODUCTCMPT_NAME", type.getName());
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults);
     * </pre>
     */
    protected void generateMethodSetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getProductCmptType().getName(), StringUtils.uncapitalise(getProductCmptType().getName()), "initPropertiesWithConfiguratedDefaults"};
        appendLocalizedJavaDoc("METHOD_SET_PRODUCTCMPT", replacements, getProductCmptType(), methodsBuilder);
        generateSignatureSetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults)
     * </pre>
     */
    public void generateSignatureSetProductCmpt(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameSetProductCmpt(type);
        String[] paramTypes = new String[] { productCmptInterfaceBuilder.getQualifiedClassName(type), "boolean" };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, getMethodParamNamesSetProductCmpt(type), paramTypes);
    }
    
    /**
     * Returns the name of the method to set the product component, e.g. setMotorProduct
     */
    public String getMethodNameSetProductCmpt(IProductCmptType type) throws CoreException {
        return getLocalizedText(type, "METHOD_SET_PRODUCTCMPT_NAME", type.getName());
    }

    /**
     * Returns the method parameters for the method: setProductCmpt.
     */
    public String[] getMethodParamNamesSetProductCmpt(IProductCmptType type) throws CoreException {
        return new String[] { StringUtils.uncapitalise(type.getName()), "initPropertiesWithConfiguratedDefaults" };
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[]{getNameForGenerationConcept(type), type.getName(), type.findPolicyCmptyType().getName()};
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT_GENERATION", replacements, type, methodsBuilder);
        generateSignatureGetProductCmptGeneration(type, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    public void generateSignatureGetProductCmptGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String genName = productCmptGenInterfaceBuilder.getQualifiedClassName(type);
        String methodName = getMethodNameGetProductCmptGeneration(type);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, genName, methodName, 
                new String[0], new String[0]);
    }
    
    /**
     * Returns the name of the method to access the product component generation, e.g. getMotorProductGen
     */
    public String getMethodNameGetProductCmptGeneration(IProductCmptType type) throws CoreException {
        String[] replacements = new String[]{type.getName(), getAbbreviationForGenerationConcept(type), getNameForGenerationConcept(type)};
        return getLocalizedText(type, "METHOD_GET_PRODUCTCMPT_GENERATION_NAME", replacements);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(
            IMethod method,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, paramTypes, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code samples:
     * <pre>
     * public void calculatePremium(IPolicy policy)
     * public ICoverage getCoverageWithHighestSumInsured()
     * </pre>
     */
    public void generateSignatureForMethodDefinedInModel(
        IMethod method,
        int javaModifier,
        Datatype returnType,
        Datatype[] paramTypes,
        JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String[] paramClassNames = new String[paramTypes.length];
        for (int i = 0; i < paramClassNames.length; i++) {
            if (paramTypes[i] instanceof IPolicyCmptType) {
                paramClassNames[i] = getQualifiedClassName((IPolicyCmptType)paramTypes[i]);
            } else {
                paramClassNames[i] = paramTypes[i].getJavaClassName();
            }
        }
        String returnClassName;
        if  (returnType instanceof IPolicyCmptType) {
            returnClassName = getQualifiedClassName((IPolicyCmptType)returnType);
        } else {
            returnClassName = returnType.getJavaClassName();
        }
        methodsBuilder.signature(javaModifier, returnClassName, method.getName(), 
                method.getParameterNames(), paramClassNames);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForAttribute(IAttribute attribute, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (!(attribute.getModifier() == org.faktorips.devtools.core.model.pctype.Modifier.PUBLISHED)) {
            return;
        }
        super.generateCodeForAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
     
        if (attribute.isProductRelevant()) {
            String javaDoc = null; // TODO getLocalizedText(null, a.getName());
            methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
            generateSignatureAttributeGetter(attribute, datatypeHelper, methodsBuilder);
            methodsBuilder.appendln(";");
        } else {
            generateStaticAttributeVariable(attribute, datatypeHelper, memberVarsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForChangeableAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaDoc = null;
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureAttributeGetter(attribute, datatypeHelper, methodsBuilder);
        methodsBuilder.appendln(";");
        
        javaDoc = getLocalizedText(attribute, "ATTRIBUTE_INTERFACE_SETTER_JAVADOC", attribute.getName());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureAttributeSetter(attribute, datatypeHelper, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaDoc = null;
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureAttributeGetter(attribute, datatypeHelper, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String javaDoc = getLocalizedText(attribute, "ATTRIBUTE_INTERFACE_GETTER_JAVADOC", attribute.getName());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureAttributeGetter(attribute, datatypeHelper, methodsBuilder);
        methodsBuilder.appendln(";");

    }
    
    void generateStaticAttributeVariable(
            IAttribute a,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String comment = getLocalizedText(a, ATTRIBUTE_FIELD_COMMENT, a.getName());
        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        String varName = getJavaNamingConvention().getMemberVarName(a.getName());
        int modifier = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL | java.lang.reflect.Modifier.STATIC; 
        JavaCodeFragment initialValueExpression = helper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(modifier, helper.getJavaClassName(), varName, initialValueExpression);
    }
    
    public String getAttributeGetterMethodName(IAttribute a, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(a.getName(), datatype);
    }
    
    void generateSignatureAttributeGetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getAttributeGetterMethodName(a, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(), methodName, new String[0], new String[0]);
    }
    
    void generateSignatureAttributeSetter(
            IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getJavaNamingConvention().getSetterMethodName(a.getName(), datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, "void", methodName, new String[]{"newValue"}, new String[]{datatypeHelper.getJavaClassName()});
    }
    
    private String getPolicyCmptInterfaceGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private String getPolicyCmptInterfaceGetValueSetMethodName(IAttribute a) {
        return "getWertebereich" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @throws CoreException
     */
    private void createAttributeValueSetDeclaration(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        // TODO: Kommentare der Methoden in die Resourcendatei auslageern !
        if (a.getValueSet() != null && !a.getValueSet().isAllValues()) {
            String methodNameMax = getPolicyCmptInterfaceGetMaxValueSetMethodName(a);
            String methodName = getPolicyCmptInterfaceGetValueSetMethodName(a);
            String javaDocMax = getLocalizedText(a, JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
            String javaDoc = getLocalizedText(a, JAVA_GETTER_METHOD_VALUESET, a.getName());
            if (a.getValueSet().isRange()) {
                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        helper.getRangeJavaClassName(), methodNameMax, new String[0], new String[0], javaDocMax,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");

                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        helper.getRangeJavaClassName(), methodName, new String[0], new String[0], javaDoc,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");
            } else { // a.getValueSet().isEnum()

                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        datatype.getJavaClassName() + "[]", methodNameMax, new String[0], new String[0], javaDocMax,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");

                methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT,
                        datatype.getJavaClassName() + "[]", methodName, new String[0], new String[0], javaDoc,
                        ANNOTATION_GENERATED);
                methodsBuilder.appendln(";");
            }
        }
    }

    private String getPolicyCmptInterfaceValueSetFieldName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetField(JavaCodeFragmentBuilder memberVarsBuilder, IAttribute a, Datatype datatype, DatatypeHelper helper)
            throws CoreException {
        String fieldName = getPolicyCmptInterfaceValueSetFieldName(a);
        String dataTypeValueSet;
        JavaCodeFragment initialValueExpression = new JavaCodeFragment();

        if (a.getValueSet().isRange()) {
            dataTypeValueSet = helper.getRangeJavaClassName();
            initialValueExpression.append("new ");
            initialValueExpression.appendClassName(helper.getRangeJavaClassName());
            initialValueExpression.append("( ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet()).getLowerBound()));
            initialValueExpression.append(", ");
            initialValueExpression.append(helper.newInstance(((Range)a.getValueSet()).getUpperBound()));
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

        memberVarsBuilder.javaDoc(comment, ANNOTATION_GENERATED);
        memberVarsBuilder.varDeclaration(
                java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                        | java.lang.reflect.Modifier.STATIC, dataTypeValueSet, fieldName, initialValueExpression);
    }

    

    protected void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        PolicyCmptTypeInterfaceRelationBuilder relationBuilder = new PolicyCmptTypeInterfaceRelationBuilder(this);
        relationBuilder.buildRelation(methodsBuilder, relation);
    }
    
    /**
     * Empty implementation.
     * 
     * overidden
     */
    protected void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }
}