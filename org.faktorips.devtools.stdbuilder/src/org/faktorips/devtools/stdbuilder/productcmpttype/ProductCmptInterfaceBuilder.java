package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder that generates the Java source file (compilation unit) for the
 * published interface of a product component type. 
 * 
 * @author Jan Ortmann
 */
public class ProductCmptInterfaceBuilder extends AbstractProductCmptTypeBuilder {

    private PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    public ProductCmptInterfaceBuilder(IJavaPackageStructure packageStructure, String kindId) throws CoreException {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptInterfaceBuilder.class));
        setMergeEnabled(true);
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptInterfaceBuilder builder) {
        this.policyCmptTypeInterfaceBuilder = builder;
    }

    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getConceptName(ipsSrcFile));
    }
    
    public String getConceptName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getProductCmptType(ipsSrcFile).getName();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = getLocalizedText("JAVADOC_INTERFACE", new String[]{getProductCmptType().getName()});
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateSignatureGetGeneration(methodsBuilder);
        methodsBuilder.append(';');
        generateSignatureCreatePolicyCmpt(methodsBuilder);
        methodsBuilder.append(';');
    }
    
    void generateSignatureGetGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String generationConceptName = getChangesInTimeNamingConvention().
        getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode());
        String javaDoc = getLocalizedText("JAVADOC_GET_GENERATION_METHOD", generationConceptName);
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String generationInterface = productCmptGenInterfaceBuilder.getQualifiedClassName(getIpsSrcFile());
        String methodName = getLocalizedText("GET_GENERATION_METHOD", generationConceptName);
        String paramName = getGetGenerationEffectiveDateParamName();
        methodsBuilder.signature(Modifier.PUBLIC, generationInterface, methodName, new String[]{paramName}, new String[]{Calendar.class.getName()});
    }

    String getGetGenerationEffectiveDateParamName() {
        return getLocalizedText("GET_GENERATION_METHOD_EEFECTIVEDATE_PARAM");
    }
    
    void generateSignatureCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType =policyCmptTypeInterfaceBuilder.getQualifiedClassName(getIpsSrcFile());
        String methodName = "create" + policyCmptTypeInterfaceBuilder.getConceptName(getIpsSrcFile());
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, 
                new String[]{"effectiveDate"}, new String[]{Calendar.class.getName()});
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
        // nothing to do, generating an interface
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        return null; // no superclass, generating an interface
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IProductComponent.class.getName();
        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype!=null) {
            javaSupertype = getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return new String[] { javaSupertype };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForChangeableAttribute(IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        //  nothing to do
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForRelation(IProductCmptTypeRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelation(IProductCmptTypeRelation containerRelation, List implementationRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // TODO Auto-generated method stub
        
    }

}
