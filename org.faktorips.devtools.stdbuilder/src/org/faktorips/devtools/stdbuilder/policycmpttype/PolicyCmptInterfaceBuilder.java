package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.stdbuilder.Util;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.runtime.IPolicyComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    private final static String ATTRIBUTE_FIELD_COMMENT = "ATTRIBUTE_FIELD_COMMENT";

    private final static String ATTRIBUTE_MAX_VALUESET_JAVADOC = "ATTRIBUTE_MAX_VALUESET_JAVADOC";
    private final static String ATTRIBUTE_VALUESET_JAVADOC = "ATTRIBUTE_VALUESET_JAVADOC";

    private final static String PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC = "PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC";
    private final static String PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC = "PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC";
    private final static String PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC = "PRODUCT_CMPT_IMPLEMENTATION_GETTER_JAVADOC";
    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_GETTER_METHOD_VALUESET = "JAVA_GETTER_METHOD_VALUESET";

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    public PolicyCmptInterfaceBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class));
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
        return getJavaNamingConvention().getPublishedInterfaceName(getConceptName(ipsSrcFile));
    }
    
    public String getConceptName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return StringUtils.capitalise(name);
    }

    private void generateMethodGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(getIpsObject(), PRODUCT_CMPT_INTERFACE_GETTER_JAVADOC);
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetProductCmpt(getIpsSrcFile(), methodsBuilder);
        methodsBuilder.append(";");
    }
    
    public void generateSignatureGetProductCmpt(IIpsSrcFile ipsSrcFile, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType = productCmptInterfaceBuilder.getQualifiedClassName(ipsSrcFile);
        String methodName = getMethodNameGetProductCmpt(ipsSrcFile);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }
    
    public void generateSignatureSetProductCmpt(IIpsSrcFile ipsSrcFile, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameSetProductCmpt(ipsSrcFile);
        String[] paramTypes = new String[] { productCmptInterfaceBuilder.getQualifiedClassName(ipsSrcFile), "boolean" };
        String[] paramNames = new String[] { "pc", "isInitMode" };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
    }
    
    private void generateMethodSetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(getIpsObject(), PRODUCT_CMPT_INTERFACE_SETTER_JAVADOC);
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureSetProductCmpt(getIpsSrcFile(), methodsBuilder);
        methodsBuilder.appendln(";");
    }

    public void generateSignatureGetProductCmptGeneration(IIpsSrcFile ipsScrFile, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String genName = productCmptGenInterfaceBuilder.getQualifiedClassName(ipsScrFile);
        String methodName = getMethodNameGetProductCmptGeneration(ipsScrFile);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, genName, methodName, 
                new String[0], new String[0]);
    }
    
    public String getMethodNameGetProductCmpt(IIpsSrcFile ipsScrFile) throws CoreException {
        return "get" + StringUtils.capitalise(productCmptInterfaceBuilder.getConceptName(ipsScrFile));
    }

    public String getMethodNameSetProductCmpt(IIpsSrcFile ipsScrFile) throws CoreException {
        return "set" + StringUtils.capitalise(productCmptInterfaceBuilder.getConceptName(ipsScrFile));
    }

    public String getMethodNameGetProductCmptGeneration(IIpsSrcFile ipsScrFile) throws CoreException {
        return "get" + StringUtils.capitalise(productCmptGenInterfaceBuilder.getConceptName(ipsScrFile));
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

    private String getPolicyCmptInterfaceSetMethodName(IAttribute a) {
        return "set" + StringUtils.capitalise(a.getName());
    }

    private String getPolicyCmptInterfaceGetMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    private void buildMethods(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IMethod[] methods = getPcType().getMethods();
        for (int i = methods.length - 1; i >= 0; i--) {
            if (!methods[i].validate().containsErrorMsg()) {
                try {
                    buildMethod(methodsBuilder, methods[i]);
                } catch (Exception e) {
                    addToBuildStatus(new IpsStatus(IStatus.ERROR, "Error building method " + methods[i].getName()
                            + " of " + getPcType(), e));
                }
            }
        }
    }

    private String getPolicyCmptInterfacePublishedInterfaceMethodName(IMethod m) {
        return m.getName();
    }

    private void buildMethod(JavaCodeFragmentBuilder methodsBuilder, IMethod method) throws CoreException {
        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        Datatype datatype = getPcType().getIpsProject().findDatatype(method.getDatatype());
        String methodName = getPolicyCmptInterfacePublishedInterfaceMethodName(method);

        methodsBuilder.methodBegin(java.lang.reflect.Modifier.ABSTRACT | Util.getJavaModifier(method.getModifier()),
                datatype.getJavaClassName(), methodName, method.getParameterNames(), BuilderHelper
                        .transformParameterTypesToJavaClassNames(method.getIpsProject(), method.getParameters()),
                method.getDescription(), ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptInterfaceValueSetFiedName(IAttribute a) {
        return "maxWertebereich" + StringUtils.capitalise(a.getName());
    }

    private void createAttributeValueSetField(JavaCodeFragmentBuilder memberVarsBuilder, IAttribute a, Datatype datatype, DatatypeHelper helper)
            throws CoreException {
        String fieldName = getPolicyCmptInterfaceValueSetFiedName(a);
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
            String[] elements = ((EnumValueSet)a.getValueSet()).getElements();
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

    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (productCmptInterfaceBuilder == null) {
            builderName = ProductCmptInterfaceBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException("One of the builders this builder depends on is not set: " + builderName);
        }
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        generateMethodGetProductCmpt(methodsBuilder);
        generateMethodSetProductCmpt(methodsBuilder);
        
        // getProductComponentGeneration()
        String javaDoc = ""; 
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureGetProductCmptGeneration(getIpsSrcFile(), methodsBuilder);
        methodsBuilder.append(";");

        buildMethods(methodsBuilder);
    }

    protected boolean generatesInterface() {
        return true;
    }

    /**
     * Empty implementation
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * Returns null. This method will not be called for an interface builder.
     * 
     * @see org.faktorips.devtools.core.builder.AbstractPcTypeBuilder#getSuperclass()
     */
    protected String getSuperclass() throws CoreException {
        return null;
    }

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