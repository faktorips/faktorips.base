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

package org.faktorips.devtools.stdbuilder.policycmpttype.method;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for <tt>IMethod</tt>s used for <tt>IPolicyCmptType</tt>s.
 * 
 * @author Peter Erzberger
 */
public class GenMethod extends GenTypePart {

    public GenMethod(GenPolicyCmptType genPolicyCmptType, IMethod method) throws CoreException {
        super(genPolicyCmptType, method, new LocalizedStringsSet(GenMethod.class));
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        // Nothing to do.
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        // Nothing to do.
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        IMethod method = getMethod();
        try {
            Datatype returnType = ipsProject.findDatatype(method.getDatatype());
            if (!generatesInterface) {
                generateClassCodeForMethodDefinedInModel(method, returnType, builder);
            }
            if (generatesInterface) {
                generateInterfaceCodeForMethodDefinedInModelInterface(method, returnType, builder);
            }

        } catch (CoreException e) {
            throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building method " + method.getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                    + getGenType().getQualifiedName(generatesInterface), e));
        }
    }

    private Datatype[] getParameterDatatypes() {
        try {
            IParameter[] parameters = getMethod().getParameters();
            Datatype[] parameterDatatypes = new Datatype[parameters.length];
            for (int j = 0; j < parameterDatatypes.length; j++) {
                parameterDatatypes[j] = getIpsProject().findDatatype(parameters[j].getDatatype());
            }
            return parameterDatatypes;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getParameterClassNames(Datatype[] parameterDatatypes) {
        try {
            String[] parameterClassNames = new String[parameterDatatypes.length];
            for (int i = 0; i < parameterClassNames.length; i++) {
                parameterClassNames[i] = getGenType().getBuilderSet().getJavaClassName(parameterDatatypes[i]);
            }
            return parameterClassNames;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public IMethod getMethod() {
        return (IMethod)getIpsPart();
    }

    public String getMethodName() {
        return getMethod().getName();
    }

    protected void generateClassCodeForMethodDefinedInModel(IMethod method,
            Datatype returnType,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (method.getModifier() == org.faktorips.devtools.core.model.ipsobject.Modifier.PUBLISHED) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        }

        IMethod overiddenMethod = method.findOverriddenMethod(getIpsPart().getIpsProject());
        if (method.getModifier().isPublished()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), overiddenMethod == null);
        } else if (method.getModifier().isPublic() && overiddenMethod != null) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), false);
        }
        generateSignatureForMethodDefinedInModel(method, method.getJavaModifier(), returnType, methodsBuilder);
        if (method.isAbstract()) {
            methodsBuilder.appendln(";");
            return;
        }
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement model method.");
        methodsBuilder.append("throw new RuntimeException(\"Not implemented yet!\");");
        methodsBuilder.closeBracket();
    }

    protected void generateInterfaceCodeForMethodDefinedInModelInterface(IMethod method,
            Datatype returnType,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        methodsBuilder.javaDoc(method.getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code samples:
     * 
     * <pre>
     * public void calculatePremium(IPolicy policy)
     * public ICoverage getCoverageWithHighestSumInsured()
     * </pre>
     */
    protected void generateSignatureForMethodDefinedInModel(IMethod method,
            int javaModifier,
            Datatype returnType,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        StandardBuilderSet builderset = getGenType().getBuilderSet();
        String returnClassName = builderset.getJavaClassName(returnType);
        methodsBuilder.signature(javaModifier, returnClassName, getMethodName(), method.getParameterNames(),
                getParameterClassNames(getParameterDatatypes()));
    }

    public boolean isPublished() {
        return getMethod().getModifier().isPublished();
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        if (isPublished()) {
            addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

    private void addMethodToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        Datatype[] parameterDatatypes = getParameterDatatypes();
        String[] parameterTypeSignatures = new String[parameterDatatypes.length];
        for (int i = 0; i < parameterTypeSignatures.length; i++) {
            parameterTypeSignatures[i] = getJavaParameterTypeSignature(parameterDatatypes[i]);
        }
        org.eclipse.jdt.core.IMethod method = generatedJavaType.getMethod(getMethodName(), parameterTypeSignatures);
        javaElements.add(method);
    }

}
