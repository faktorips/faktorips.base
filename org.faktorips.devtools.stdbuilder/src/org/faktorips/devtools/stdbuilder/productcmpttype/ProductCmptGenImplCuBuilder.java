package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.stdbuilder.backup.ProductCmptImplCuBuilder;
import org.faktorips.runtime.internal.ProductComponentGeneration;
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
public class ProductCmptGenImplCuBuilder extends AbstractProductCmptTypeBuilder{

    private ProductCmptGenInterfaceCuBuilder interfaceBuilder;
    private ProductCmptImplCuBuilder productCmptTypeImplCuBuilder;
    
    public ProductCmptGenImplCuBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptGenImplCuBuilder.class));
        setMergeEnabled(true);
    }
    
    public void setInterfaceBuilder(ProductCmptGenInterfaceCuBuilder builder) {
        ArgumentCheck.notNull(builder);
        this.interfaceBuilder = builder;
    }
    
    public void setProductCmptTypeImplBuilder(ProductCmptImplCuBuilder builder) {
        ArgumentCheck.notNull(builder);
        productCmptTypeImplCuBuilder = builder;
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
        // TODO refactor as soon as product component gives access to this information
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
        return getJavaNamingConvention().getImplementationClassName(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "PkGen");
    }

    /**
     * Overridden.
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText("JAVADOC_CLASS", interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile()));
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
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
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getLocalizedText("JAVADOC_CONSTRUCTOR", getUnqualifiedClassName()), ANNOTATION_GENERATED);
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
        
        generateDoInitPropertiesFromXml(methodsBuilder);
    }
    
    private void generateDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        
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
                memberVarName = getMemberVarNameDefaulValue(a);
            } else {
                memberVarName = getMemberVarNameValue(a);
            }
            builder.append("configElement = (");
            builder.appendClassName(Element.class);
            builder.append(")configMap.get(\"");
            builder.append(a.getName());
            builder.appendln("\");");
            builder.append("if (configElement != null) ");
            builder.openBracket();
            builder.appendln("value = configElement.getAttribute(\"value\");");
            builder.append(memberVarName);
            builder.append(" = ");
            builder.append(helper.newInstanceFromExpression("value"));
            builder.appendln(";");
            builder.closeBracket();
        }
        builder.methodEnd();
    }
    
    /**
     * Overridden.
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        // The implementation implements the published interface.
        return new String[] { interfaceBuilder.getQualifiedClassName(getIpsSrcFile()) };
    }

    protected void generateCodeForChangeableAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        // member variable
        String javaDoc = getLocalizedText("JAVADOC_MEMBER_VAR_DEFAULTVALUE", a.getName());
        memberVarsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getMemberVarNameDefaulValue(a), defaultValueExpression);
        
        // getter method
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetDefaultValue(a, datatypeHelper, Modifier.PUBLIC, methodsBuilder);
        methodsBuilder.append("return ");
        methodsBuilder.append(getMemberVarNameDefaulValue(a));
        methodsBuilder.append(';');
        methodsBuilder.methodEnd();
    }

    protected void generateCodeForConstantAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        // member variable
        String javaDoc = getLocalizedText("JAVADOC_MEMBER_VAR_VALUE", a.getName());
        memberVarsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getMemberVarNameValue(a), defaultValueExpression);
        
        // getter method
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetValue(a, datatypeHelper, Modifier.PUBLIC, methodsBuilder);
        methodsBuilder.append("return ");
        methodsBuilder.append(getMemberVarNameValue(a));
        methodsBuilder.append(';');
        methodsBuilder.methodEnd();
    }

    String getDefaultValuePropertyName(IAttribute a) {
        return getLocalizedText("DEFAULTVALUE_PROPERTYNAME", StringUtils.capitalise(a.getName()));
    }
    
    private String getMemberVarNameDefaulValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(getDefaultValuePropertyName(a));
    }
    
    String getValuePropertyName(IAttribute a) {
        return getLocalizedText("VALUE_PROPERTYNAME", StringUtils.capitalise(a.getName()));
    }
    
    private String getMemberVarNameValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(getValuePropertyName(a));
    }
    
    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // generate the abstract computation method
        String javaDoc = getLocalizedText("JAVADOC_COMPUTE_METHOD", a.getName());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateSignatureForComputationMethod(a, datatypeHelper, Modifier.PUBLIC | Modifier.ABSTRACT, methodsBuilder);
        methodsBuilder.append(';');
    }
    
    void generateSignatureForComputationMethod(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        String methodName = getComputationMethodName(a);
        methodsBuilder.methodBegin(modifier, datatypeHelper.getJavaClassName(),
                methodName, BuilderHelper.extractParameterNames(parameters),
                BuilderHelper.transformParameterTypesToJavaClassNames(a.getIpsProject(), parameters));
    }
    
    String getComputationMethodName(IAttribute a) {
        return getLocalizedText("COMPUTE_METHODNAME", StringUtils.capitalise(a.getName()));
    }

    /**
     * Overridden.
     */
    protected void generateCodeForRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        String javaDoc = "";
        memberVarsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        memberVarsBuilder.append("private String[] ");
        memberVarsBuilder.append(getMemberVarNameManyRelation(relation));
        memberVarsBuilder.append(" = new String[0];");
        generateCodeForGetManyMethod(relation, memberVarsBuilder, methodsBuilder);
    }

    private void generateCodeForGetManyMethod(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetManyRelated(relation, Modifier.PUBLIC, methodsBuilder);

        // Sample code
        //
        // CoverageType[] result = new CoverageType[coverageTypes.length];
        // for (int i = 0; i < result.length; i++) {
        //     result[i] = (CoveragePk) getRepository().getProductComponent(
        //     coveragePk[i]);
        // }
        // return result;
        String fieldName = getMemberVarNameManyRelation(relation);
        String targetClass = interfaceBuilder.getQualifiedClassName(relation.findTarget());
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
    
        methodsBuilder.methodEnd();
    }
    
    /**
     * Returns the name of the member variable that stores the references for the indicated 
     * 1-to-many relation. 
     */
    String getMemberVarNameManyRelation(IProductCmptTypeRelation relation) throws CoreException {
        return getJavaNamingConvention().getMultiValueMemberVarName(getManyRelationPropertyName(relation));
    }
    
    /*
     * Returns the name of the property (in the Java beans sense) that stored the referenced objects
     * in a 1-to-many relation. 
     */
    private String getManyRelationPropertyName(IProductCmptTypeRelation relation) {
        return getLocalizedText("TOMANY_RELATION_PROPERTYNAME", StringUtils.capitalise(relation.getTargetRolePlural()));
    }
    
    


}
