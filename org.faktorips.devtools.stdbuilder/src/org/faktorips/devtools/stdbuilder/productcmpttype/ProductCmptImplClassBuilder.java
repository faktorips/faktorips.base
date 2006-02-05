package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.runtime.RuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder that generates the Java source file (compilation unit) for the
 * product component type implementation. 
 * 
 * @author Jan Ortmann
 */
public class ProductCmptImplClassBuilder extends AbstractProductCmptTypeBuilder {

    private ProductCmptInterfaceBuilder interfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private PolicyCmptImplClassBuilder policyCmptImplClassBuilder;
    
    /**
     * @param packageStructure
     * @param kindId
     * @param localizedStringsSet
     */
    public ProductCmptImplClassBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptImplClassBuilder.class));
    }
    
    public void setInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        interfaceBuilder = builder;
    }
    
    /**
     * @param productCmptGenInterfaceBuilder The productCmptGenInterfaceBuilder to set.
     */
    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }
    
    /**
     * @param policyCmptImplClassBuilder The policyCmptImplClassBuilder to set.
     */
    public void setPolicyCmptImplClassBuilder(PolicyCmptImplClassBuilder builder) {
        this.policyCmptImplClassBuilder = builder;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getImplementationClassName(getProductCmptType(ipsSrcFile).getName());
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText("JAVADOC_CLASS", interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile()));
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateGetGenerationMethod(methodsBuilder);
        generateMethodCreatePolicyCmpt(methodsBuilder);
    }
    
    private void generateGetGenerationMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        interfaceBuilder.generateSignatureGetGeneration(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(productCmptGenInterfaceBuilder.getQualifiedClassName(getIpsSrcFile()));
        methodsBuilder.append(")getRepository().getProductComponentGeneration(getQualifiedName(), ");
        methodsBuilder.append(interfaceBuilder.getGetGenerationEffectiveDateParamName());
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }
    
    private void generateMethodCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureCreatePolicyCmpt(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return new ");
        methodsBuilder.appendClassName(policyCmptImplClassBuilder.getQualifiedClassName(getIpsSrcFile()));
        methodsBuilder.appendln("(this, effectiveDate);");
        methodsBuilder.closeBracket();
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
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        /*
         * public MotorPolicy(RuntimeRepository repository, String qName, Class policyComponentType) {
         *     super(registry, qName, policyComponentType); 
         * }
         */
        String className = getUnqualifiedClassName();
        String javaDoc = getLocalizedText("JAVADOC_CONSTRUCTOR", className);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super(repository, qName, policyComponentType);");
        builder.method(Modifier.PUBLIC, null, className, new String[] { "repository", "qName",
                "policyComponentType" }, new String[] { RuntimeRepository.class.getName(),
                String.class.getName(), Class.class.getName() }, body, javaDoc,
            ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        String javaSupertype = ProductComponent.class.getName();
        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype!=null) {
            javaSupertype = getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return javaSupertype;
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[]{interfaceBuilder.getQualifiedClassName(getProductCmptType())};
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForChangeableAttribute(IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        // nothing to do at the moment

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        // nothing to do at the moment
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        // nothing to do at the moment
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForRelation(IProductCmptTypeRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // TODO Auto-generated method stub

    }

}
