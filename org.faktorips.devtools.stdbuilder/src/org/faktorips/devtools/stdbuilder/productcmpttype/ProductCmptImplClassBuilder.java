/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder that generates the Java source file (compilation unit) for the product component type
 * implementation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptImplClassBuilder extends BaseProductCmptTypeBuilder {

    public ProductCmptImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptImplClassBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getImplementationClassName(ipsSrcFile.getIpsObjectName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSuperclass() throws CoreException {
        String javaSupertype = ProductComponent.class.getName();
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype != null) {
            javaSupertype = getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return javaSupertype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getExtendedInterfaces() throws CoreException {
        return new String[] { GenType.getQualifiedName(getProductCmptType(), (StandardBuilderSet)getBuilderSet(), true) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String interfaceName = GenType.getUnqualifiedClassName(getProductCmptType(),
                (StandardBuilderSet)getBuilderSet(), true);
        appendLocalizedJavaDoc("CLASS", interfaceName, getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     * 
     * <pre>
     * public MotorPolicy(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName, policyComponentType);
     * }
     * </pre>
     */
    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        String className = getUnqualifiedClassName();
        appendLocalizedJavaDoc("CONSTRUCTOR", className, getIpsObject(), builder);
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String versionParam = getChangesInTimeNamingConvention(getIpsObject()).getVersionConceptNameSingular(locale);
        versionParam = StringUtils.uncapitalize(versionParam) + "Id";
        String[] argNames = new String[] { "repository", "id", "kindId", versionParam };
        String[] argTypes = new String[] { IRuntimeRepository.class.getName(), String.class.getName(),
                String.class.getName(), String.class.getName() };
        builder.methodBegin(Modifier.PUBLIC, null, className, argNames, argTypes);
        builder.append("super(repository, id, kindId, " + versionParam + ");");
        builder.methodEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        generateGetGenerationMethod(methodsBuilder);
        IPolicyCmptType policyCmptType = getPcType();
        if (policyCmptType != null && !policyCmptType.isAbstract()) {
            generateFactoryMethodsForPolicyCmptType(policyCmptType, methodsBuilder, new HashSet<IPolicyCmptType>());
        }
        if (mustGenerateMethodCreatePolicyComponentBase(getProductCmptType())) {
            generateMethodCreatePolicyCmptBase(methodsBuilder);
        }
    }

    private void generateFactoryMethodsForPolicyCmptType(IPolicyCmptType returnedTypeInSignature,
            JavaCodeFragmentBuilder methodsBuilder,
            Set<IPolicyCmptType> supertypesHandledSoFar) throws CoreException {

        if (returnedTypeInSignature != null && !returnedTypeInSignature.isAbstract()) {
            generateMethodCreatePolicyCmpt(returnedTypeInSignature, methodsBuilder);
        }
        IPolicyCmptType supertype = (IPolicyCmptType)returnedTypeInSignature.findSupertype(getIpsProject());
        if (supertype != null && !supertypesHandledSoFar.contains(supertype)) {
            supertypesHandledSoFar.add(supertype);
            generateFactoryMethodsForPolicyCmptType(supertype, methodsBuilder, supertypesHandledSoFar);
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorPolicy createMotorPolicy() {
     *     return new MotorPolicy(this);
     * }
     * </pre>
     */
    private void generateMethodCreatePolicyCmpt(IPolicyCmptType returnedTypeInSignature,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        IProductCmptType superType = getProductCmptType().findSuperProductCmptType(getIpsProject());
        if (!returnedTypeInSignature.equals(getPcType())) {
            appendOverrideAnnotation(methodsBuilder, false);
        } else if (superType != null) {
            IPolicyCmptType superPolicyCmptType = superType.findPolicyCmptType(getIpsProject());
            if (superPolicyCmptType != null && superPolicyCmptType.equals(getPcType())) {
                appendOverrideAnnotation(methodsBuilder, false);
            }
        } else {
            appendOverrideAnnotation(methodsBuilder, true);
        }
        ((StandardBuilderSet)getBuilderSet()).getGenerator(returnedTypeInSignature).generateSignatureCreatePolicyCmpt(
                methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return new ");
        methodsBuilder.appendClassName(((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType())
                .getQualifiedName(false));
        methodsBuilder.appendln("(this);");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IPolicyComponent createPolicyComponent() {
     *     return createMotorPolicy();
     * }
     * </pre>
     * 
     * or
     * 
     * <pre>
     * [Javadoc]
     * public IPolicyComponent createPolicyComponent() {
     *     return null;
     * }
     * </pre>
     */
    private void generateMethodCreatePolicyCmptBase(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        // TODO pk 17-07-2009: this is still not correct. Their are situations which I could not yet
        // figure out when an override of an implementation appears which doesn't have an override
        // annotation.
        CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod checkVisitor = new CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod(
                getIpsProject());
        checkVisitor.start(getProductCmptType().findSupertype(getIpsProject()));
        appendOverrideAnnotation(methodsBuilder, checkVisitor.isInterfaceImplementation());
        methodsBuilder.signature(Modifier.PUBLIC, IConfigurableModelObject.class.getName(),
                MethodNames.CREATE_POLICY_COMPONENT, new String[0], new String[0]);
        methodsBuilder.openBracket();
        if (mustGenerateMethodCreatePolicyComponentAsReturnNull(getProductCmptType())) {
            methodsBuilder.appendln("return null;");
        } else {
            methodsBuilder.append("return ");
            methodsBuilder.append(getStandardBuilderSet().getGenerator(getPcType()).getMethodNameCreatePolicyCmpt());
            methodsBuilder.appendln("();");
        }
        methodsBuilder.closeBracket();
    }

    private void generateGetGenerationMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_GENERATION", getIpsObject(), methodsBuilder);
        appendOverrideAnnotation(methodsBuilder, true);
        GenProductCmptType genProd = ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType());
        genProd.generateSignatureGetGeneration(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(genProd.getQualifiedClassNameForProductCmptTypeGen(true));
        methodsBuilder.append(")getRepository().getProductComponentGeneration(");
        methodsBuilder.append(MethodNames.GET_PRODUCT_COMPONENT_ID);
        methodsBuilder.append("(), ");
        methodsBuilder.append(genProd.getVarNameEffectiveDate());
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForProductCmptTypeAttribute(org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation derivedUnionAssociation,
            List<IAssociation> implementationAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected GenProductCmptTypeAttribute createGenerator(IProductCmptTypeAttribute a,
            LocalizedStringsSet localizedStringsSet) throws CoreException {
        // return null, as this builder does not need code for product component type attributes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected GenPolicyCmptTypeAttribute createGenerator(IPolicyCmptTypeAttribute a,
            LocalizedStringsSet localizedStringsSet) throws CoreException {
        // return null, as this builder does not need code for policy component type attributes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAssociation createGenerator(IProductCmptTypeAssociation a, LocalizedStringsSet stringsSet)
            throws CoreException {
        // return null, as this builder does not need code for product component type associations
        return null;
    }

    /**
     * Returns <code>true</code> if either the method <code>createPolicyComponent()</code> must
     * return <code>null</code> or an instance of a PolicyComponentType associated with this product
     * component type. The latter is the case if the following conditions hold <code>true</code>:
     * <ul>
     * <li>The corresponding policy component type is not abstract.</li>
     * <li>This product component type is the first in it's type hierarchy that refers to that
     * PolicyComponentType. Remember hat its possible to have something like Policy is based on
     * AbstractProduct with concrete sub types ProductA and ProductB where Policy is not abstract.
     * The implementation can be generated in the AbstractProduct class.</li>.
     * </ul>
     */
    private boolean mustGenerateMethodCreatePolicyComponentBase(IProductCmptType productCmptType) throws CoreException {
        if (mustGenerateMethodCreatePolicyComponentAsReturnNull(productCmptType)) {
            return true;
        }
        IPolicyCmptType policyType = productCmptType.findPolicyCmptType(getIpsProject());
        if (policyType == null || policyType.isAbstract()) {
            return false;
        }
        IProductCmptType supertype = productCmptType.findSuperProductCmptType(getIpsProject());
        if (supertype == null) {
            return true;
        }
        // return true if the super type does not refer to the same policy component type
        // no need to go up in the hierarchy, if the same policy component type is refered to
        // in the type hierarchy it must be in the supertype, otherwise the hierachy is
        // inconsistent.
        return !policyType.getQualifiedName().equals(supertype.getPolicyCmptType());
    }

    /**
     * Returns <code>true</code> if the method <code>createPolicyComponent</code> must be
     * implemented as <code>return null;</code>, otherwise false. This is the case if
     * <ul>
     * <li>The product component type's corresponding policy component type is null and</li>
     * <li>the product component type has no super type.</li>
     * </ul>
     */
    private boolean mustGenerateMethodCreatePolicyComponentAsReturnNull(IProductCmptType productCmptType)
            throws CoreException {
        if (productCmptType.hasSupertype()) {
            return false;
        }
        IPolicyCmptType policyType = productCmptType.findPolicyCmptType(getIpsProject());
        return policyType == null;
    }

    private class CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod extends ProductCmptTypeHierarchyVisitor {

        private boolean isInterfaceImplementation = true;

        public CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if (mustGenerateMethodCreatePolicyComponentBase(currentType)) {
                isInterfaceImplementation = false;
                return false;
            }
            return true;
        }

        public boolean isInterfaceImplementation() {
            return isInterfaceImplementation;
        }
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements, IIpsElement ipsElement) {
        /*
         * TODO AW: I can't delegate this to the GenProductCmptType generator because this generator
         * cannot be configured whether to generate the generation or the product itself. But this
         * makes a lot of difference in the returned generated Java elements. Making the
         * GenProductCmptType configurable in such a way would require many changes.
         */
        if (ipsElement instanceof IProductCmptType) {
            IProductCmptType productCmptType = (IProductCmptType)ipsElement;
            IType javaType = getGeneratedJavaType(productCmptType);
            javaElements.add(javaType);
        }
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
