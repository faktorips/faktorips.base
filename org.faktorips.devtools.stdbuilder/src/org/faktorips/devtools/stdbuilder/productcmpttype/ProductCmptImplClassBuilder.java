/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
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
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
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

    public ProductCmptImplClassBuilder(StandardBuilderSet builderSet, String kindId) {
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
        return new String[] { GenType.getQualifiedName(getProductCmptType(), getStandardBuilderSet(), true) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        String interfaceName = GenType.getUnqualifiedClassName(getProductCmptType(), getStandardBuilderSet(), true);
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
        generateMethodDoInitPropertiesFromXml(methodsBuilder);
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
        if (returnedTypeInSignature == null) {
            return;
        }
        if (!returnedTypeInSignature.isAbstract()) {
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
        boolean interfaceMethodImplementation = true;
        if (!returnedTypeInSignature.equals(getPcType())) {
            interfaceMethodImplementation = false;
        } else if (superType != null) {
            IPolicyCmptType superPolicyCmptType = superType.findPolicyCmptType(getIpsProject());
            if (superPolicyCmptType != null && superPolicyCmptType.equals(getPcType())) {
                interfaceMethodImplementation = false;
            }
        }
        appendOverrideAnnotation(methodsBuilder, interfaceMethodImplementation);
        getStandardBuilderSet().getGenerator(returnedTypeInSignature).generateSignatureCreatePolicyCmpt(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return new ");
        methodsBuilder.appendClassName(getStandardBuilderSet().getGenerator(getPcType()).getQualifiedName(false));
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
        CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod checkVisitor = new CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod(
                getIpsProject());
        checkVisitor.start((IProductCmptType)getProductCmptType().findSupertype(getIpsProject()));
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
        GenProductCmptType genProd = getStandardBuilderSet().getGenerator(getProductCmptType());
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

    @Override
    protected boolean isChangingOverTimeContainer() {
        return false;
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

    private class CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod extends
            TypeHierarchyVisitor<IProductCmptType> {

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
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        // Nothing to do
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
