/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

public abstract class BaseProductCmptImplementationBuilder extends BaseProductCmptTypeBuilder {

    public BaseProductCmptImplementationBuilder(StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
    }

    protected void generateFactoryMethodsForPolicyCmptType(IPolicyCmptType returnedTypeInSignature,
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
    void generateMethodCreatePolicyCmpt(IPolicyCmptType returnedTypeInSignature, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
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
        getBuilderSet().getGenerator(returnedTypeInSignature).generateSignatureCreatePolicyCmpt(methodsBuilder);
        generateMethodBodyCreatePolicyCmpt(methodsBuilder);
    }

    /**
     * Generates the body of the create&lt;PolicyComponent&gt; method including open and closing
     * brackets.
     * 
     * @param methodsBuilder The builder to add the java code fragments
     * @throws CoreException in case of any exceptions
     */
    protected abstract void generateMethodBodyCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException;

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
    protected boolean mustGenerateMethodCreatePolicyComponentBase(IProductCmptType productCmptType)
            throws CoreException {
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
    protected boolean mustGenerateMethodCreatePolicyComponentAsReturnNull(IProductCmptType productCmptType)
            throws CoreException {
        if (productCmptType.hasSupertype()) {
            return false;
        }
        IPolicyCmptType policyType = productCmptType.findPolicyCmptType(getIpsProject());
        return policyType == null;
    }

    /**
     * Generates the signature of the createPolicyComponent method and delegates the generation of
     * the body to {@link #generateMethodBodyCreatePolicyCmptBase(JavaCodeFragmentBuilder)}
     * 
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
    protected void generateMethodCreatePolicyCmptBase(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod checkVisitor = new CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod(
                getIpsProject());
        checkVisitor.start((IProductCmptType)getProductCmptType().findSupertype(getIpsProject()));
        appendOverrideAnnotation(methodsBuilder, checkVisitor.isInterfaceImplementation());
        methodsBuilder.signature(Modifier.PUBLIC, IConfigurableModelObject.class.getName(),
                MethodNames.CREATE_POLICY_COMPONENT, new String[0], new String[0]);
        generateMethodBodyCreatePolicyCmptBase(methodsBuilder);
    }

    /**
     * Generates only the body of the createPolicyComponent method.
     * 
     * Code sample (signature and javadoc not generate here):
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
    protected void generateMethodBodyCreatePolicyCmptBase(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.openBracket();
        if (mustGenerateMethodCreatePolicyComponentAsReturnNull(getProductCmptType())) {
            methodsBuilder.appendln("return null;");
        } else {
            methodsBuilder.append("return ");
            methodsBuilder.append(getBuilderSet().getGenerator(getPcType()).getMethodNameCreatePolicyCmpt());
            methodsBuilder.appendln("();");
        }
        methodsBuilder.closeBracket();
    }

    class CheckIfInterfaceImplementationForCreateBasePolicyCmptMethod extends TypeHierarchyVisitor<IProductCmptType> {

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

}