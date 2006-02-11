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
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
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
public class ProductCmptGenImplClassBuilder extends AbstractProductCmptTypeBuilder{

    private ProductCmptGenInterfaceBuilder interfaceBuilder;
    private ProductCmptImplClassBuilder productCmptTypeImplCuBuilder;
    private ProductCmptInterfaceBuilder productCmptTypeInterfaceBuilder;
    private PolicyCmptImplClassBuilder policyCmptTypeImplBuilder;
    
    public ProductCmptGenImplClassBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
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
                String fieldName = getMemberVarNameRelation(r);
                if (r.is1ToMany()) {
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
                    builder.append(fieldName);
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
    
    protected void generateCodeForConstantAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        // member variable
        String javaDoc = getLocalizedText(a, "JAVADOC_MEMBER_VAR_VALUE", a.getName());
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

    String getValuePropertyName(IAttribute a) {
        return getLocalizedText(a, "VALUE_PROPERTYNAME", StringUtils.capitalise(a.getName()));
    }
    
    private String getMemberVarNameValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(getValuePropertyName(a));
    }
    
    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // generate the abstract computation method
        String javaDoc = getLocalizedText(a, "JAVADOC_COMPUTE_METHOD", a.getName());
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        generateMethodComputeValue(a, datatypeHelper, Modifier.PUBLIC | Modifier.ABSTRACT, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    public void generateMethodComputeValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        String methodName = getMethodNameComputeValue(a);
        methodsBuilder.methodBegin(modifier, datatypeHelper.getJavaClassName(),
                methodName, BuilderHelper.extractParameterNames(parameters),
                StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, a.getIpsProject(), policyCmptTypeImplBuilder));
    }
    
    public String getMethodNameComputeValue(IAttribute a) {
        return getLocalizedText(a, "COMPUTE_METHODNAME", StringUtils.capitalise(a.getName()));
    }

    /**
     * Overridden.
     */
    protected void generateCodeForRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMemberVarRelation(relation, memberVarsBuilder);
        if (relation.is1ToMany()) {
            generateMethodRelationGetMany(relation, memberVarsBuilder, methodsBuilder);
        } else {
            generateMethodRelationGet1(relation, memberVarsBuilder, methodsBuilder);
        }
    }
    
    private void generateMemberVarRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String javaDoc = null; // TODO getLocalizedText("JAVADOC_MEMBER_VAR_DEFAULTVALUE", a.getName());
        memberVarsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String type = String.class.getName() + ( relation.is1ToMany() ? "[]" : "");
        String initValue = relation.is1ToMany() ? "new String[0]" : "null";
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getMemberVarNameRelation(relation), new JavaCodeFragment(initValue));
    }
    
    private String getMemberVarNameRelation(IProductCmptTypeRelation relation) throws CoreException {
        if (relation.is1ToMany()) {
            return getJavaNamingConvention().getMultiValueMemberVarName(getPropertyNameRelation(relation));
        } else {
            return getJavaNamingConvention().getMemberVarName(getPropertyNameRelation(relation));
        }
    }
    
    String getPropertyNameRelation(IProductCmptTypeRelation relation) throws CoreException {
        if (relation.is1ToMany()) {
            return StringUtils.capitalise(relation.getTargetRolePlural());
        } else {
            return StringUtils.capitalise(relation.getTargetRoleSingular());
        }
    }
    
    private void generateMethodRelationGetMany(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureRelationGetMany(relation, methodsBuilder);

        // Sample code
        //
        // CoverageType[] result = new CoverageType[coverageTypes.length];
        // for (int i = 0; i < result.length; i++) {
        //     result[i] = (CoveragePk) getRepository().getProductComponent(
        //     coveragePk[i]);
        // }
        // return result;
        String fieldName = getMemberVarNameRelation(relation);
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
    
    private void generateMethodRelationGet1(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureRelationGetMany(relation, methodsBuilder);

        // Sample code
        // return (CoveragePk) getRepository().getProductComponent(coverageType);
        String fieldName = getMemberVarNameRelation(relation);
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
    protected void generateCodeForContainerRelation(IProductCmptTypeRelation containerRelation, List implementationRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // TODO Auto-generated method stub
        
    }
    
    


}
