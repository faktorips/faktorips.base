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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder that generates the Java source file (compilation unit) for the
 * product component type implementation. 
 * 
 * @author Jan Ortmann
 */
public class ProductCmptImplClassBuilder extends BaseProductCmptTypeBuilder {

    private ProductCmptInterfaceBuilder interfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private PolicyCmptImplClassBuilder policyCmptImplClassBuilder;
    
    public ProductCmptImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptImplClassBuilder.class));
        //TODO pk 2006-06-21 merge enabled at least until generator has been extended for validation and information capabilities  
        setMergeEnabled(true);
    }
    
    public void setInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        interfaceBuilder = builder;
    }
    
    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        this.productCmptGenInterfaceBuilder = builder;
    }
    
    public void setPolicyCmptImplClassBuilder(PolicyCmptImplClassBuilder builder) {
        this.policyCmptImplClassBuilder = builder;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getImplementationClassName(ipsSrcFile.getIpsObjecName());
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
        String javaSupertype = ProductComponent.class.getName();
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
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
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String interfaceName = interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile());
        appendLocalizedJavaDoc("CLASS", interfaceName, getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     * <pre>
     * public MotorPolicy(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName, policyComponentType);
     * } 
     * </pre>
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        String className = getUnqualifiedClassName();
        appendLocalizedJavaDoc("CONSTRUCTOR", className, getIpsObject(), builder);
        Locale locale = getLanguageUsedInGeneratedSourceCode(getIpsObject());
        String versionParam = getChangesInTimeNamingConvention(getIpsObject()).getVersionConceptNameSingular(locale);
        versionParam = StringUtils.uncapitalize(versionParam) + "Id";
        String[] argNames = new String[] { "repository", "id", "kindId", versionParam };
        String[] argTypes = new String[] { IRuntimeRepository.class.getName(), String.class.getName(), String.class.getName(), String.class.getName() };
        builder.methodBegin(Modifier.PUBLIC, null, className, argNames, argTypes);
        builder.append("super(repository, id, kindId, " + versionParam + ");");
        builder.methodEnd();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateGetGenerationMethod(methodsBuilder);
        IPolicyCmptType policyCmptType = getPcType();
        if (policyCmptType!=null && !policyCmptType.isAbstract()) {
            generateFactoryMethodsForPolicyCmptType(policyCmptType, methodsBuilder, new HashSet());
        }
        if (policyCmptType==null || !policyCmptType.isAbstract()) {
            // if policy component type is null, must generate to fullfill the contract of IProductComponent. 
            generateMethodCreatePolicyCmptBase(methodsBuilder); 
        } 
    }
    
    private void generateFactoryMethodsForPolicyCmptType(
            IPolicyCmptType returnedTypeInSignature, 
            JavaCodeFragmentBuilder methodsBuilder,
            Set supertypesHandledSoFar) throws CoreException {
        
        if (returnedTypeInSignature!=null && !returnedTypeInSignature.isAbstract()) {
            generateMethodCreatePolicyCmpt(returnedTypeInSignature, methodsBuilder);
        }
        IPolicyCmptType supertype = (IPolicyCmptType)returnedTypeInSignature.findSupertype(getIpsProject());
        if (supertype!=null && !supertypesHandledSoFar.contains(supertype)) {
            supertypesHandledSoFar.add(supertype);
            generateFactoryMethodsForPolicyCmptType(supertype, methodsBuilder, supertypesHandledSoFar);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorPolicy createMotorPolicy() {
     *     return new MotorPolicy(this);
     * }
     * </pre>
     */
    private void generateMethodCreatePolicyCmpt(IPolicyCmptType returnedTypeInSignature, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureCreatePolicyCmpt(returnedTypeInSignature, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return new ");
        methodsBuilder.appendClassName(policyCmptImplClassBuilder.getQualifiedClassName(getPcType()));
        methodsBuilder.appendln("(this);");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IPolicyComponent createPolicyComponent() {
     *     return createMotorPolicy();
     * }
     * </pre>
     */
    private void generateMethodCreatePolicyCmptBase(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodsBuilder.signature(Modifier.PUBLIC, IConfigurableModelObject.class.getName(), MethodNames.CREATE_POLICY_COMPONENT, new String[0], new String[0]);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        if (getPcType()==null) {
            methodsBuilder.appendln("null;");
        } else {
            methodsBuilder.appendln(interfaceBuilder.getMethodNameCreatePolicyCmpt(getPcType()) + "();");
        }
        methodsBuilder.closeBracket();
    }

    private void generateGetGenerationMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_GENERATION", getIpsObject(), methodsBuilder);
        interfaceBuilder.generateSignatureGetGeneration(getProductCmptType(), methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(productCmptGenInterfaceBuilder.getQualifiedClassName(getIpsSrcFile()));
        methodsBuilder.append(")getRepository().getProductComponentGeneration(");
        methodsBuilder.append(MethodNames.GET_PRODUCT_COMPONENT_ID);
        methodsBuilder.append("(), ");
        methodsBuilder.append(interfaceBuilder.getVarNameEffectiveDate(getIpsObject()));
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation containerAssociation, List implementationAssociations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
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

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(
            IMethod method,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
//      nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAttribute createGenerator(IProductCmptTypeAttribute a, LocalizedStringsSet localizedStringsSet)
            throws CoreException {
        // return null, as this builder does not need code for product component type attributes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet localizedStringsSet)
            throws CoreException {
        // TODO return null, as this builder does not need code for policy component type attributes
        return null;
    }
    
    
}
