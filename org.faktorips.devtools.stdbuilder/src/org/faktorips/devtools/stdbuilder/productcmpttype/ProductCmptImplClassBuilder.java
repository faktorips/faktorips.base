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
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
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
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
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
        // TODO pk 2006-06-21 merge enabled at least until generator has been extended for
        // validation and information capabilities
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
        if (policyCmptType == null || !policyCmptType.isAbstract()) {
            // if policy component type is null, must generate to fullfill the contract of
            // IProductComponent.
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
        if (!returnedTypeInSignature.equals(getPcType())) {
            appendOverrideAnnotation(methodsBuilder, false);
        }
        IProductCmptType superType = getProductCmptType().findSuperProductCmptType(getIpsProject());
        if (superType != null) {
            IPolicyCmptType superPolicyCmptType = superType.findPolicyCmptType(getIpsProject());
            if (superPolicyCmptType != null && superPolicyCmptType.equals(getPcType())) {
                appendOverrideAnnotation(methodsBuilder, false);
            }
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
     */
    private void generateMethodCreatePolicyCmptBase(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        // TODO pk 17-07-2009: this is still not correct. Their are situations which I could not yet
        // figure out when an override of an implementation appears which doesn't have an override
        // annotation.
        if (StringUtils.isEmpty(getProductCmptType().getPolicyCmptType())) {
            appendOverrideAnnotation(methodsBuilder, !getProductCmptType().hasSupertype());
        } else if (getProductCmptType().hasSupertype()) {
            CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod checkVisitor = new CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod(
                    getIpsProject());
            checkVisitor.start(getProductCmptType().findSupertype(getIpsProject()));
            appendOverrideAnnotation(methodsBuilder, checkVisitor.isInterfaceImplementation());
        }
        methodsBuilder.signature(Modifier.PUBLIC, IConfigurableModelObject.class.getName(),
                MethodNames.CREATE_POLICY_COMPONENT, new String[0], new String[0]);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        if (getPcType() == null) {
            methodsBuilder.appendln("null;");
        } else {
            methodsBuilder.appendln(((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType())
                    .getMethodNameCreatePolicyCmpt()
                    + "();");
        }
        methodsBuilder.closeBracket();
    }

    private void generateGetGenerationMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_GENERATION", getIpsObject(), methodsBuilder);
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

    private class CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod extends ProductCmptTypeHierarchyVisitor {

        private boolean isInterfaceImplementation;
        private int counter = 0;
        private boolean firstAbstract = false;

        public CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if (counter == 0) {
                firstAbstract = currentType.isAbstract();
                counter++;
                return true;
            }
            if (!firstAbstract) {
                isInterfaceImplementation = true;
                return false;
            }
            counter++;
            isInterfaceImplementation = currentType.isAbstract();
            return isInterfaceImplementation;
        }

        public boolean isInterfaceImplementation() {
            if (counter == 1) {
                return firstAbstract;
            }
            return isInterfaceImplementation;
        }
    }
}
