package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IChangesInTimeNamingConvention;
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
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("INTERFACE", getConceptName(getIpsSrcFile()), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateMethodGetGeneration(methodsBuilder);
        generateMethodCreatePolicyCmpt(methodsBuilder);
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public IProductGen getGeneration(Calendar effectiveDate);
     * </pre>
     */
    private void generateMethodGetGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IChangesInTimeNamingConvention convention = getChangesInTimeNamingConvention(getIpsSrcFile());
        String generationConceptName = convention.getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode(getIpsObject()));
        appendLocalizedJavaDoc("METHOD_GET_GENERATION", generationConceptName, getIpsObject(), methodsBuilder);
        generateSignatureGetGeneration(getProductCmptType(), methodsBuilder);
        methodsBuilder.append(';');
    }
    
    /**
     * Code sample:
     * <pre>
     * public IProductGen getGeneration(Calendar effectiveDate)
     * </pre>
     */
    void generateSignatureGetGeneration(IProductCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String generationConceptName = getChangesInTimeNamingConvention(type).
            getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode(type));
        String generationInterface = productCmptGenInterfaceBuilder.getQualifiedClassName(getIpsSrcFile());
        String methodName = getLocalizedText(type, "METHOD_GET_GENERATION_NAME", generationConceptName);
        String paramName = getVarNameEffectiveDate(type);
        methodsBuilder.signature(Modifier.PUBLIC, generationInterface, methodName, new String[]{paramName}, new String[]{Calendar.class.getName()});
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public IPolicy createPolicy(Calendar effectiveDate);
     * </pre>
     */
    private void generateMethodCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String policyCmptConceptName = policyCmptTypeInterfaceBuilder.getConceptName(getIpsSrcFile()); 
        IChangesInTimeNamingConvention convention = getChangesInTimeNamingConvention(getIpsSrcFile());
        String effectiveDateConceptName = convention.getEffectiveDateConceptName(getLanguageUsedInGeneratedSourceCode(getIpsObject()));
        appendLocalizedJavaDoc("METHOD_CREATE_POLICY_CMPT", new String[]{policyCmptConceptName, effectiveDateConceptName}, getIpsObject(), methodsBuilder);
        generateSignatureCreatePolicyCmpt(getIpsSrcFile(), methodsBuilder);
        methodsBuilder.append(';');
    }
    
    /**
     * Code sample:
     * <pre>
     * public IPolicy createPolicy(Calendar effectiveDate)
     * </pre>
     */
    void generateSignatureCreatePolicyCmpt(IIpsSrcFile ipsSrcFile, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String policyCmptConceptName = policyCmptTypeInterfaceBuilder.getConceptName(ipsSrcFile); 
        String returnType =policyCmptTypeInterfaceBuilder.getQualifiedClassName(ipsSrcFile);
        String methodName = getLocalizedText(ipsSrcFile, "METHOD_CREATE_POLICY_CMPT_NAME", policyCmptConceptName);
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, 
                new String[]{getVarNameEffectiveDate(ipsSrcFile)}, new String[]{Calendar.class.getName()});
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
        
        //  nothing to do

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelation(IProductCmptTypeRelation containerRelation, List implementationRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        //  nothing to do
        
    }

}
