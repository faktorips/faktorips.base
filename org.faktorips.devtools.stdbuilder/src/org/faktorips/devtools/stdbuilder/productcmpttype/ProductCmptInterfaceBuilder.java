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
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
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

    public ProductCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptInterfaceBuilder.class));
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
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
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
        // v2 - prufen, wann man die factory methode erzeugt. Signature koennte ja durchaus generiert werden.
        // und nur die implementierung nicht! 
        if (getPolicyCmptType()!=null && !getPolicyCmptType().isAbstract()) {
            generateMethodCreatePolicyCmpt(methodsBuilder);
        }
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public IProductGen getGeneration(Calendar effectiveDate);
     * </pre>
     */
    private void generateMethodGetGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IChangesOverTimeNamingConvention convention = getChangesInTimeNamingConvention(getIpsSrcFile());
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
        String generationInterface = productCmptGenInterfaceBuilder.getQualifiedClassName(type);
        String methodName = getMethodNameGetGeneration(type);
        String paramName = getVarNameEffectiveDate(type);
        methodsBuilder.signature(Modifier.PUBLIC, generationInterface, methodName, new String[]{paramName}, new String[]{Calendar.class.getName()});
    }
    
    public String getMethodNameGetGeneration(IProductCmptType type) throws CoreException {
        IChangesOverTimeNamingConvention convention = getChangesInTimeNamingConvention(type);
        String generationConceptName = convention.getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode(type));
        String generationConceptAbbreviation = convention.getGenerationConceptNameAbbreviation(getLanguageUsedInGeneratedSourceCode(type));
        return getLocalizedText(type, "METHOD_GET_GENERATION_NAME", new String[]{type.getName(), generationConceptAbbreviation, generationConceptName});
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public IPolicy createPolicy();
     * </pre>
     */
    private void generateMethodCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IPolicyCmptType policyCmptType = getPolicyCmptType();
        String policyCmptTypeName = policyCmptTypeInterfaceBuilder.getPolicyCmptTypeName(policyCmptType); 
        appendLocalizedJavaDoc("METHOD_CREATE_POLICY_CMPT", new String[]{policyCmptTypeName}, getIpsObject(), methodsBuilder);
        generateSignatureCreatePolicyCmpt(policyCmptType, methodsBuilder);
        methodsBuilder.append(';');
    }
    
    /**
     * Code sample:
     * <pre>
     * public IPolicy createPolicy()
     * </pre>
     */
    void generateSignatureCreatePolicyCmpt(IPolicyCmptType type, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType =policyCmptTypeInterfaceBuilder.getQualifiedClassName(type);
        String methodName = getMethodNameCreatePolicyCmpt(type);
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, new String[0], new String[0]);
    }
    
    /**
     * Returns the method name to create the concrete policy component class, e.g. createMotorPolicy.
     */
    public String getMethodNameCreatePolicyCmpt(IPolicyCmptType type) throws CoreException {
        String policyCmptConceptName = policyCmptTypeInterfaceBuilder.getPolicyCmptTypeName(type); 
        return getLocalizedText(type, "METHOD_CREATE_POLICY_CMPT_NAME", policyCmptConceptName);
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
    protected void generateCodeForProductCmptTypeAttribute(IAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder, JavaCodeFragmentBuilder constantBuilder) throws CoreException {
        
        //  nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForFormulaSignatureDefinition(IProductCmptTypeMethod method, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethod(IMethod method, JavaCodeFragmentBuilder fieldsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneContainerRelation(IProductCmptTypeAssociation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        
        //  nothing to do

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationDefinition(IProductCmptTypeAssociation containerRelation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationImplementation(IProductCmptTypeAssociation containerRelation, List implementationRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }
}
